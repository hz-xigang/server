package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.SysUserDto
import com.gz.xg.domain.entity.SysRole
import com.gz.xg.domain.entity.SysUser
import com.gz.xg.domain.entity.SysUserRole
import com.gz.xg.domain.mapstruct.SysUserMapStruct
import com.gz.xg.domain.req.BindUserRoleReq
import com.gz.xg.domain.req.ChangePwdReq
import com.gz.xg.domain.req.LoginReq
import com.gz.xg.domain.req.UserSearch
import com.gz.xg.domain.view.VSysUserRole
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.VSysUserRoleMapper
import com.gz.xg.service.plus.SysUserPlusService
import com.gz.xg.service.plus.SysUserRolePlusService
import com.gz.xg.util.IdUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

@Service
class SysUserService(
    private val plusService: SysUserPlusService,
    private val passwordEncoder : PasswordEncoder,
    private val sysUserMapStruct: SysUserMapStruct,
    private val jwtService: JwtService,
    private val pmt: PlatformTransactionManager,
    private val sysUserRolePlusService: SysUserRolePlusService,
    private val vSysUserRoleMapper: VSysUserRoleMapper
) : BaseService() {

    /**
     * 添加
     */
    fun add(dto: SysUserDto){
        if (dto.pwd.isNullOrBlank()){
            throw WebException("密码不能为空")
        }
        val user = sysUserMapStruct.toEntity(dto)
        user.pwd = passwordEncoder.encode(user.pwd)
        user.id = IdUtil.generateId()
        plusService.save(user)
        log.info("新增系统用户成功: userId={}, username={}, realName={}, type={}", user.id, user.username, user.realName, user.type)
    }

    /**
     * 修改
     */
    fun edit(dto: SysUserDto){
        plusService.byId(dto.id)
        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(SysUser::getType, dto.type)
            .set(SysUser::getUsername,dto.username)
            .set(SysUser::getRealName, dto.realName)
            .eq(SysUser::getId, dto.id)
            .update()
        log.info("修改系统用户信息: userId={}, username={}, realName={}, type={}", dto.id, dto.username, dto.realName, dto.type)
    }

    /**
     * 登录
     */
    fun login(req : LoginReq): String {
        val user = plusService.findByUsername(req.username)

        val matches = passwordEncoder.matches(req.pwd, user.pwd)
        if (!matches) {
            log.warn("用户登录失败(密码错误): username={}", req.username)
            throw WebException("用户或密码错误")
        }

        val token = jwtService.generateToken(user)
        log.info("用户登录成功: userId={}, username={}", user.id, user.username)
        return token
    }

    /**
     * 修改当前登录用户密码（校验原密码后加密新密码）。
     */
    fun changePwd(req: ChangePwdReq) {
        val loginUser = UserContext.require()
        val user = plusService.byId(loginUser.userId)

        if (!passwordEncoder.matches(req.oldPwd, user.pwd)) {
            log.warn("用户修改密码失败(原密码错误): userId={}, username={}", loginUser.userId, loginUser.username)
            throw WebException("原密码错误")
        }

        user.pwd = passwordEncoder.encode(req.newPwd)
        plusService.updateById(user)
        log.info("用户修改密码成功: userId={}, username={}", loginUser.userId, loginUser.username)
    }


    /**
     * 分页
     */
    fun page( current : Long , size : Long,search: UserSearch) : Map<String, Any> {
        val page = Page<SysUser>(current , size)
        val wrapper = LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getDeleted,0)
            .like(!search.username.isNullOrBlank() , SysUser::getUsername,search.username)
            .like(!search.realName.isNullOrBlank() , SysUser::getRealName,search.realName)
            .eq(search.type != null, SysUser::getType,search.type)
            .orderByDesc(SysUser::getId)

        val pageObj = plusService.page(page, wrapper)

        pageObj.records.forEach { it->
            run {
                it.pwd = ""
            }
        }

        return getPage(pageObj)
    }

    /**
     * 绑定用户角色
     */
    fun bindRole(req : BindUserRoleReq){
        plusService.byId(req.userId)

        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {
            sysUserRolePlusService.dropUserRole(req.userId)
            val list = req.roleIds.map {
                SysUserRole().apply { userId = req.userId; roleId = it }
            }.toList()

            sysUserRolePlusService.saveBatch(list)
            pmt.commit(status)
            log.info("用户分配角色成功: userId={}, roleIds={}", req.userId, req.roleIds)
        }catch (e: Exception){
            pmt.rollback(status)
            log.error("用户分配角色失败: userId={}, roleIds={}, error={}", req.userId, req.roleIds, e.message)
            throw WebException(e.message)
        }
    }

    /**
     * 获取角色id
     */
    fun getRoleId(id : String) : List<String> {
        plusService.byId(id)
        val wrapper = LambdaQueryWrapper<VSysUserRole>()
            .eq(VSysUserRole::getUserId,id)
            .select(VSysUserRole::getRoleId)

        return vSysUserRoleMapper.selectList(wrapper).map { it.roleId }
    }

    /**
     * 软删除
     */
    fun softDel(id : String){
        plusService.byId(id)
        changeDel(plusService.baseMapper, SysUser::getDeleted,1){
            eq(SysUser::getId,id)
        }
        log.info("删除系统用户: userId={}", id)
    }


}
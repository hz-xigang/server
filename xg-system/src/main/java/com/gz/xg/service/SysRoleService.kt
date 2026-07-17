package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.SysRoleDto
import com.gz.xg.domain.entity.SysRight
import com.gz.xg.domain.entity.SysRole
import com.gz.xg.domain.entity.SysRoleRight
import com.gz.xg.domain.mapstruct.SysRoleMapStruct
import com.gz.xg.domain.req.BindRoleRightReq
import com.gz.xg.domain.view.VRoleRight
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.VRoleRightMapper
import com.gz.xg.service.plus.SysRightPlusService
import com.gz.xg.service.plus.SysRolePlusService
import com.gz.xg.service.plus.SysRoleRightPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

@Service
class SysRoleService(
    private val plusService: SysRolePlusService,
    private val sysRoleRightPlusService: SysRoleRightPlusService,
    private val pmt: PlatformTransactionManager,
    private val roleMapStruct: SysRoleMapStruct,
    private val vRoleRightMapper: VRoleRightMapper
)  : BaseService(){

    /**
     * 列表
     */
    fun list(): List<SysRoleDto> {
        val wrapper = LambdaQueryWrapper<SysRole>().eq(SysRole::getDeleted,0)
        return roleMapStruct.toDtoList(plusService.list(wrapper))
    }

    /**
     * 新增
     */
    fun add(dto: SysRoleDto) {
        val role = plusService.byName(dto.name,null)
        if (role != null) {
            throw WebException("【${dto.name}】 该角色名已存在 ")
        }
        val entity = roleMapStruct.toEntity(dto)
        entity.id = IdUtil.generateId()
        plusService.save(entity)
    }

    /**
     * 主键更新
     */
    fun editById(dto: SysRoleDto) {
        plusService.byId(dto.id)
        val role = plusService.byName(dto.name,dto.id)
        if (role != null) {
            throw WebException("【${dto.name}】 该角色名已存在 ")
        }
        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(SysRole::getName, dto.name).set(SysRole::getRemark, dto.remark)
            .eq(SysRole::getId,dto.id)
            .update()
    }


    /**
     * 绑定权限
     */
    fun bindRights(req: BindRoleRightReq) {
        plusService.byId(req.roleId)

        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {
            sysRoleRightPlusService.dropRoleMenu(req.roleId)
            val list = req.menuIds.map {
                SysRoleRight().apply {
                    roleId = req.roleId
                    rightId = it
                    rightType = 1
                }
            }.toList()

            sysRoleRightPlusService.saveBatch(list)
            pmt.commit(status)
        } catch (e: Exception) {
            pmt.rollback(status)
            throw WebException(e.message)
        }
    }

    /**
     * 获取菜单id
     */
    fun getMenuId(id : String) : List<String> {
        plusService.byId(id)
        val wrapper = LambdaQueryWrapper<VRoleRight>()
            .eq(VRoleRight::getRoleId, id)
            .select(VRoleRight::getRightId)

        return vRoleRightMapper.selectList(wrapper).map { it.rightId }
    }

    /**
     * 软删除
     */
    fun softDel(id : String){
        plusService.byId(id)

        changeDel(plusService.baseMapper, SysRole::getDeleted,1){
            eq(SysRole::getId,id)
        }
    }

}

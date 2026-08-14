package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.gz.xg.domain.dto.MenuTree
import com.gz.xg.domain.entity.SysRight
import com.gz.xg.domain.entity.SysRoleRight
import com.gz.xg.domain.entity.SysUserRole
import com.gz.xg.service.plus.SysRightPlusService
import com.gz.xg.service.plus.SysRoleRightPlusService
import com.gz.xg.service.plus.SysUserRolePlusService
import org.springframework.stereotype.Service

@Service
class SysRightService(
    private val plusService: SysRightPlusService,
    private val sysUserRolePlusService: SysUserRolePlusService,
    private val sysRoleRightPlusService: SysRoleRightPlusService
) {

    /**
     * 权限集合
     */
    fun list() : List<SysRight> {
        val wrapper = LambdaQueryWrapper<SysRight>()
            .eq(SysRight::getDeleted, 0)

        return plusService.list(wrapper)
    }

    /**
     * 菜单树（全量）
     */
    fun menuTree() : List<MenuTree>{
        val wrapper = LambdaQueryWrapper<SysRight>()
            .eq(SysRight::getDeleted, 0)
            .eq(SysRight::getType,1)

        return buildMenuTree(plusService.list(wrapper))
    }

    /**
     * 按用户查询菜单树。
     *
     * 链路：用户 -> SysUserRole(角色) -> SysRoleRight(菜单权限) -> SysRight(菜单)。
     * 子菜单命中权限时自动补出父级目录，避免因子目录未单独授权导致菜单丢失。
     */
    fun menuTreeByUserId(userId: String): List<MenuTree> {
        val roleIds: List<String> = sysUserRolePlusService.list(
            LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).map { it.roleId }

        if (roleIds.isEmpty()) {
            return emptyList()
        }

        // 查询所有菜单类型权限，按角色过滤得到该用户的菜单 rightId
        val roleRights: List<SysRoleRight> = sysRoleRightPlusService.list(
            LambdaQueryWrapper<SysRoleRight>().eq(SysRoleRight::getRightType, 1)
        )
        val rightIds: List<String> = roleRights
            .filter { roleIds.contains(it.roleId) }
            .map { it.rightId }
            .distinct()

        if (rightIds.isEmpty()) {
            return emptyList()
        }

        // 全部菜单（目录 + 页面）
        val allMenus: List<SysRight> = plusService.list(
            LambdaQueryWrapper<SysRight>()
                .eq(SysRight::getDeleted, 0)
                .eq(SysRight::getType, 1)
        )

        val granted: List<SysRight> = allMenus.filter { rightIds.contains(it.id) }

        // 补齐父级目录
        val grantedIds: MutableSet<String> = HashSet()
        val parentIds: MutableList<String> = ArrayList()
        for (menu in granted) {
            val mid: String? = menu.id
            if (mid != null) {
                grantedIds.add(mid)
            }
        }
        for (menu in granted) {
            val pid: String? = menu.parentId
            if (pid != null && !grantedIds.contains(pid) && !parentIds.contains(pid)) {
                parentIds.add(pid)
            }
        }

        val parents: MutableList<SysRight> = ArrayList()
        for (menu in allMenus) {
            if (parentIds.contains(menu.id)) {
                parents.add(menu)
            }
        }

        return buildMenuTree(granted + parents)
    }


    /**
     * 构建菜单树
     */
    private fun buildMenuTree(list: List<SysRight>): List<MenuTree> {
        val map = list.map { it.toTree() }.associateBy { it.id }
        val roots = mutableListOf<MenuTree>()

        map.values.forEach { node ->

            if (node.parentId == null) {
                roots.add(node)
            } else {
                map[node.parentId]?.children?.add(node)
            }
        }

        return roots
    }



    /**
     *
     */
    fun SysRight.toTree() = MenuTree(
        id = id,
        parentId = parentId,
        menuName = menuName,
        type = type,
        path = path,
        perms = perms,
        icon = icon,
        sortNo = sortNo
    )


}

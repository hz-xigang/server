package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.google.gson.Gson
import com.gz.xg.domain.dto.MenuTree
import com.gz.xg.domain.entity.SysRight
import com.gz.xg.domain.view.VRoleRight
import com.gz.xg.mapper.VRoleRightMapper
import com.gz.xg.service.plus.SysRightPlusService
import com.gz.xg.service.plus.SysRolePlusService
import org.springframework.stereotype.Service

@Service
class SysRightService(
    private val plusService: SysRightPlusService
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
     * 菜单树
     */
    fun menuTree() : List<MenuTree>{
        val wrapper = LambdaQueryWrapper<SysRight>()
            .eq(SysRight::getDeleted, 0)
            .eq(SysRight::getType,1)

        return buildMenuTree(plusService.list(wrapper))
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

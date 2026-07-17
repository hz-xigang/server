package com.gz.xg.domain.dto

data class MenuTree (
    /**
     * 权限ID
     */
    var id: String? = null,

    /**
     * 父节点ID
     */
    var parentId: String? = null,

    /**
     * 菜单名称
     */
    var menuName: String? = null,

    /**
     * 类型
     */
    var type: Int? = null,

    /**
     * 路径
     */
    var path: String? = null,

    /**
     * 权限标识
     */
    var perms: String? = null,

    /**
     * 图标
     */
    var icon: String? = null,

    /**
     * 排序
     */
    var sortNo: Int? = null,

    /**
     * 子菜单
     */
    var children: MutableList<MenuTree> = mutableListOf()
)
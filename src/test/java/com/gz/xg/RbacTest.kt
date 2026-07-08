package com.gz.xg

import com.gz.xg.domain.entity.SysRight
import com.gz.xg.service.plus.SysRightPlusService
import com.gz.xg.util.IdUtil
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RbacTest {


    @Resource
    private lateinit var  sysRightPlusService: SysRightPlusService

    @Test
    fun contextLoads() {
        val right = SysRight()
        right.id = IdUtil.generateId()
        right.parentId = "2026070811424347275439560"
        right.menuName = "入库记录"
        right.menuType = 1
        right.path = "/inbound-record"
        right.perms = "inbound-record"
        right.icon = "\uD83D\uDCE5"

       /* val right1 = SysRight()
        right1.id = IdUtil.generateId()
        right1.menuName = "仓库管理"
        right1.menuType = 1*/

        sysRightPlusService.save(right)

    }

    @Test
    fun find(){
        val right = sysRightPlusService.getById("2026070811023390765669910")
        System.err.println(right)
    }

}
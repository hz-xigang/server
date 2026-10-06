/*
package xg

import com.gz.xg.XgApplication
import com.gz.xg.domain.entity.SysRight
import com.gz.xg.service.SysRightService
import com.gz.xg.service.plus.SysRightPlusService
import com.gz.xg.util.IdUtil
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [XgApplication::class])
class RightServiceTest(){

    @Resource
     lateinit var sysRightService: SysRightService

     @Resource
     lateinit var sysRightPlusService : SysRightPlusService

     @Test
    fun test(){
         sysRightService.menuTree()
    }

    @Test
    fun add(){
        val menus = mutableListOf("打托作业","拆托","扫描入库","移库","调拨单",
            "备货单","发货单", "退货入库","库存查询")

        val rights = menus.map { it ->
            run {
                SysRight().apply {
                    id = IdUtil.generateId()
                    menuName = it
                    type = 4
                }
            }
        }

        val bool = sysRightPlusService.saveBatch(rights)
        System.err.println("bool == $bool")

    }



}
*/

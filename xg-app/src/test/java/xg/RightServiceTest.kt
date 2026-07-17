package xg

import com.gz.xg.XgApplication
import com.gz.xg.service.SysRightService
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [XgApplication::class])
class RightServiceTest{

    @Resource
     lateinit var sysRightService: SysRightService

     @Test
    fun test(){
         sysRightService.menuTree()
    }


}
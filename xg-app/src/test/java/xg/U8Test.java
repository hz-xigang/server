package xg;

import com.google.gson.Gson;
import com.gz.xg.XgApplication;
import com.gz.xg.u8.dto.U8PurchaseOrderMain;
import com.gz.xg.u8.dto.U8Response;
import com.gz.xg.u8.dto.U8SalesOrderMain;
import com.gz.xg.u8.service.U8PurchaseOrderService;
import com.gz.xg.u8.service.U8SalesOrderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;


@SpringBootTest(classes = XgApplication.class)
public class U8Test {



    @Resource
    private U8PurchaseOrderService purchaseOrderService;

    @Resource
    private U8SalesOrderService salesOrderService;

    @Test
    void poTest(){
        U8Response<U8PurchaseOrderMain> response = purchaseOrderService.queryPurchaseOrderMain("108");
        List<U8PurchaseOrderMain> data = response.getData();
        System.out.println(new Gson().toJson(data));
    }

    @Test
    void soTest(){
        U8Response<U8SalesOrderMain> res = salesOrderService.querySalesOrderMain("108");
        res.getData().forEach(System.out::println);

    }


}

package xg;

import com.google.gson.Gson;
import com.gz.xg.XgApplication;
import com.gz.xg.u8.dto.U8PurchaseOrderMain;
import com.gz.xg.u8.dto.U8Response;
import com.gz.xg.u8.service.U8PurchaseOrderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest(classes = XgApplication.class)
public class U8Test {


    @Resource
    private U8PurchaseOrderService service;


    @Test
    void test1(){
        U8Response<U8PurchaseOrderMain> response = service.queryPurchaseOrderMain("001");

        List<U8PurchaseOrderMain> data = response.getData();

        System.out.println(new Gson().toJson(data));

    }

}

package xg;

import com.google.gson.Gson;
import com.gz.xg.XgApplication;
import com.gz.xg.u8.dto.*;
import com.gz.xg.u8.service.U8MomOrderService;
import com.gz.xg.u8.service.U8PurchaseOrderService;
import com.gz.xg.u8.service.U8SalesOrderService;
import com.gz.xg.u8.service.U8TransferOrderService;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
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

    @Resource
    private U8TransferOrderService  u8TransferOrderService;

    @Resource
    private U8MomOrderService u8MomOrderService;

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

    @Test
    void toTest(){
        U8Response< U8TransferOrderMain> res = u8TransferOrderService.queryTransferOrderMain("108");
        res.getData().forEach(System.out::println);
    }

    @Test
    void momTest(){
        U8Response<U8MomOrderMain> res = u8MomOrderService.queryMomOrderMain("108");
        res.getData().forEach(System.out::println);
    }



}

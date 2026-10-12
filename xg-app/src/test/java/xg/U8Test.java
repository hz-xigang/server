package xg;

import com.google.gson.Gson;
import com.gz.xg.XgApplication;
import com.gz.xg.domain.enums.SequenceType;
import com.gz.xg.mapper.SysRightMapper;
import com.gz.xg.service.ProdOrderSyncService;
import com.gz.xg.service.ShipOrderSyncService;
import com.gz.xg.service.SysSequenceService;
import com.gz.xg.service.TransferOrderSyncService;
import com.gz.xg.u8.dto.*;
import com.gz.xg.u8.service.U8MomOrderService;
import com.gz.xg.u8.service.U8PurchaseOrderService;
import com.gz.xg.u8.service.U8SalesOrderService;
import com.gz.xg.u8.service.U8TransferOrderService;
import com.gz.xg.util.BarcodeUtil;
import com.gz.xg.util.IdUtil;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Resource
    private ProdOrderSyncService prodOrderSyncService;

    @Resource
    private TransferOrderSyncService transferOrderSyncService;

    @Resource
    private ShipOrderSyncService  shipOrderSyncService;

    @Test
    void poTest(){
        //U8Response<U8PurchaseOrderMain> response = purchaseOrderService.queryPurchaseOrderMain("108");
        /*List<U8PurchaseOrderMain> data = response.getData();
        System.out.println(new Gson().toJson(data));*/
        //prodOrderSyncService.syncMomOrders("108");
        prodOrderSyncService.syncSalesOrders("108");
        //transferOrderSyncService.syncTransferOrders("108");
        //shipOrderSyncService.syncShipOrders("108");
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

        shipOrderSyncService.syncShipOrders("108");
    }

    @Test
    void qrExcelTest() throws Exception {
        //String[] contents = {"1261012019512", "1261012019670", "1261012019850","1261012019926"};
        String[] contents = {"1261012020048"};
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("QR");
        sheet.setColumnWidth(0, 24 * 256);
        sheet.setColumnWidth(1, 18 * 256);
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();
        for (int i = 0; i < contents.length; i++) {
            int rowIndex = i * 2;
            Row row = sheet.createRow(rowIndex);
            row.setHeightInPoints(90);
            row.createCell(0).setCellValue(contents[i]);
            byte[] qrBytes = BarcodeUtil.generateQRCode(contents[i], 200, 200);
            int pictureIdx = workbook.addPicture(qrBytes, Workbook.PICTURE_TYPE_PNG);
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            anchor.setCol1(1);
            anchor.setRow1(rowIndex);
            anchor.setCol2(2);
            anchor.setRow2(rowIndex + 1);
            drawing.createPicture(anchor, pictureIdx);
        }
        File file = new File("target/qr-codes1.xlsx");
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
        System.out.println("excel: " + file.getAbsolutePath());
    }

    @Resource
    private SysSequenceService  sysSequenceService;

    @Resource
    private SysRightMapper sysRightMapper;

    @Test
    void tt() {

    }


}

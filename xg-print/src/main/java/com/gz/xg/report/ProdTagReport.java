package com.gz.xg.report;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.gz.xg.exception.WebException;
import com.gz.xg.util.BarcodeUtil;
import com.gz.xg.util.PdfUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ProdTagReport {

    public static final String[] LABELS = {
        "{{客户编号}}", "{{工单号}}", "{{客户订单号}}", "{{产品类别}}", "{{规格型号}}",
        "{{数量}}", "{{毛重}}", "{{净重}}", "{{存货编码}}", "{{日期}}", "生产批号:{{生产单号}}"
    };

    public byte[] generate(String templatePath, Map<String, Object> data) throws Exception {
        if (!new File(templatePath).exists()) {
            throw new WebException("模板文件不存在: " + templatePath);
        }

        TemplateExportParams params = new TemplateExportParams(templatePath);
        Workbook excel = ExcelExportUtil.exportExcel(params, data);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        excel.write(bos);
        excel.close();

        Map<String, byte[]> imageReplacements = new HashMap<>();
        Object tagNoObj = data.get("生产单号");
        if (tagNoObj != null) {
            String tagNo = String.valueOf(tagNoObj).trim();
            if (!tagNo.isEmpty()) {
                byte[] barcodeBytes = BarcodeUtil.generateCode128WithText(tagNo, 360, 70);
                if (barcodeBytes != null) {
                    imageReplacements.put("条码", barcodeBytes);
                }
            }
        }

        return PdfUtil.excel2Pdf(new ByteArrayInputStream(bos.toByteArray()), imageReplacements);
    }

    /**
     * 批量填充并合并生成多页标签 PDF。
     *
     * @param templatePath 模板路径
     * @param dataList     每个标签对应的填充数据集合
     * @return 合并后的多页 PDF 字节数组
     */
    public byte[] generateBatch(String templatePath, java.util.List<Map<String, Object>> dataList) throws Exception {
        if (!new File(templatePath).exists()) {
            throw new WebException("模板文件不存在: " + templatePath);
        }
        if (dataList == null || dataList.isEmpty()) {
            return new byte[0];
        }

        TemplateExportParams params = new TemplateExportParams(templatePath);
        java.util.List<byte[]> excelByteList = new java.util.ArrayList<>();
        java.util.List<Map<String, byte[]>> imageReplacementsList = new java.util.ArrayList<>();

        for (Map<String, Object> data : dataList) {
            Workbook excel = ExcelExportUtil.exportExcel(params, data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            excel.write(bos);
            excel.close();
            excelByteList.add(bos.toByteArray());

            Map<String, byte[]> imageReplacements = new HashMap<>();
            Object tagNoObj = data.get("生产单号");
            if (tagNoObj != null) {
                String tagNo = String.valueOf(tagNoObj).trim();
                if (!tagNo.isEmpty()) {
                    byte[] barcodeBytes = BarcodeUtil.generateCode128WithText(tagNo, 360, 70);
                    if (barcodeBytes != null) {
                        imageReplacements.put("条码", barcodeBytes);
                    }
                }
            }
            imageReplacementsList.add(imageReplacements);
        }

        return PdfUtil.mergeExcelsToPdf(excelByteList, imageReplacementsList);
    }
}


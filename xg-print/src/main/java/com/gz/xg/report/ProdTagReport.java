package com.gz.xg.report;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.gz.xg.exception.WebException;
import com.gz.xg.util.PdfUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

        return PdfUtil.excel2Pdf(new ByteArrayInputStream(bos.toByteArray()));
    }
}


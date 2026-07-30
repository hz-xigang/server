package com.gz.xg.util;

import com.aspose.cells.License;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Workbook;
import com.gz.xg.exception.WebException;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Excel 转 PDF 工具。
 * 基于 Aspose Cells 提供文件路径与输入流两种转换方式。
 */
public class PdfUtil {

    /**
     * 将 Excel 文件转换为 PDF（默认输出路径由调用方在重载方法中指定）。
     *
     * @param excelFilePath Excel 文件路径。
     */
    public static void excel2pdf(String excelFilePath) {
        excel2pdf(excelFilePath, null, null);
    }

    /**
     * 将 Excel 文件指定 Sheet 转换为 PDF。
     *
     * @param excelFilePath Excel 文件路径。
     * @param convertSheets 需要转换的 Sheet 下标集合。
     */
    public static void excel2pdf(String excelFilePath, int[] convertSheets) {
        excel2pdf(excelFilePath, null, convertSheets);
    }

    /**
     * 将 Excel 文件转换为指定路径的 PDF 文件。
     *
     * @param excelFilePath Excel 文件路径。
     * @param pdfFilePath PDF 输出路径。
     */
    public static void excel2pdf(String excelFilePath, String pdfFilePath) {
        excel2pdf(excelFilePath, pdfFilePath, null);
    }

    /**
     * 将 Excel 输入流转换为 PDF 字节数组。
     *
     * @param excelIps Excel 输入流。
     * @return PDF 字节数组。
     */
    public static byte[] excel2Pdf(InputStream excelIps) {
        getLicense();

        Workbook wb = null;
        try (InputStream is = excelIps;
             ByteArrayOutputStream pdfOps = new ByteArrayOutputStream()) {
            wb = new Workbook(is);

            PdfSaveOptions pdfSaveOptions = new PdfSaveOptions();
            pdfSaveOptions.setOnePagePerSheet(true);
            wb.save(pdfOps, pdfSaveOptions);
            return pdfOps.toByteArray();
        } catch (Exception e) {
            throw new WebException("Excel转PDF失败", e);
        } finally {
            if (wb != null) {
                try {
                    wb.dispose();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 将 Excel 文件转换为 PDF 文件，并可选择转换指定 Sheet。
     *
     * @param excelFilePath Excel 文件路径。
     * @param pdfFilePath PDF 输出路径。
     * @param convertSheets 需要转换的 Sheet 下标集合。
     */
    public static void excel2pdf(String excelFilePath, String pdfFilePath, int[] convertSheets) {
        try {
            getLicense();
            Workbook wb = new Workbook(excelFilePath);
            FileOutputStream fileOS = new FileOutputStream(pdfFilePath);
            PdfSaveOptions pdfSaveOptions = new PdfSaveOptions();
            pdfSaveOptions.setOnePagePerSheet(true);
            if (convertSheets != null) {
                printSheetPage(wb, convertSheets);
            }
            wb.save(fileOS, pdfSaveOptions);
            fileOS.flush();
            fileOS.close();
            wb.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载 Aspose License 以移除水印。
     */
    private static void getLicense() {
        String licenseFilePath = "excel-license.xml";
        try (InputStream is = PdfUtil.class.getClassLoader().getResourceAsStream(licenseFilePath)) {
            License license = new License();
            license.setLicense(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制工作簿中需要输出的 Sheet 可见性。
     *
     * @param wb 工作簿对象。
     * @param sheets 需要输出的 Sheet 下标集合。
     */
    private static void printSheetPage(Workbook wb, int[] sheets) {
        for (int i = 1; i < wb.getWorksheets().getCount(); i++) {
            wb.getWorksheets().get(i).setVisible(false);
        }
        if (sheets == null || sheets.length == 0) {
            wb.getWorksheets().get(0).setVisible(true);
        } else {
            for (int i = 0; i < sheets.length; i++) {
                wb.getWorksheets().get(i).setVisible(true);
            }
        }
    }
}

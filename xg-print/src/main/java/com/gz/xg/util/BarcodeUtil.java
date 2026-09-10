package com.gz.xg.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 条形码工具类
 */
public class BarcodeUtil {

    /**
     * 生成 Code128 条形码图片字节数组 (PNG)
     *
     * @param text   条码内容
     * @param width  图片宽度 (像素)
     * @param height 图片高度 (像素)
     * @return PNG 图片字节数组
     */
    public static byte[] generateCode128(String text, int width, int height) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        Code128Writer writer = new Code128Writer();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = writer.encode(text, BarcodeFormat.CODE_128, width, height, hints);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }
}

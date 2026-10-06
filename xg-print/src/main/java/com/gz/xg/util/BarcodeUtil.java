package com.gz.xg.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 条形码工具类
 */
public class BarcodeUtil {

    /**
     * 生成 Code128 条形码图片字节数组 (PNG，不带下方文本)
     *
     * @param text   条码内容
     * @param width  图片宽度 (像素)
     * @param height 图片高度 (像素)
     * @return PNG 图片字节数组
     */
    public static byte[] generateCode128(String text, int width, int height) throws Exception {
        return generateCode128(text, width, height, false);
    }

    /**
     * 生成 Code128 条形码图片字节数组 (PNG)
     *
     * @param text       条码内容
     * @param width      图片宽度 (像素)
     * @param height     图片高度 (像素)
     * @param withBottomText 是否在条码下方附带居中文本
     * @return PNG 图片字节数组
     */
    public static byte[] generateCode128(String text, int width, int height, boolean withBottomText) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        if (!withBottomText) {
            Code128Writer writer = new Code128Writer();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = writer.encode(text, BarcodeFormat.CODE_128, width, height, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        }

        return generateCode128WithText(text, width, height);
    }

    /**
     * 生成下方带单号文本的 Code128 条形码图片
     *
     * @param text   条码及文本内容
     * @param width  整图宽度 (像素)
     * @param height 整图高度 (像素)
     * @return PNG 图片字节数组
     */
    public static byte[] generateCode128WithText(String text, int width, int height) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // 根据高度动态估算字体大小及文字区域高度
        int fontSize = Math.max(12, Math.min(height / 4, 18));
        int textSectionHeight = fontSize + 6;
        int barcodeHeight = Math.max(height - textSectionHeight, 20);

        Code128Writer writer = new Code128Writer();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);

        BitMatrix matrix = writer.encode(text, BarcodeFormat.CODE_128, width, barcodeHeight, hints);
        BufferedImage barcodeImg = MatrixToImageWriter.toBufferedImage(matrix);

        BufferedImage combinedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combinedImg.createGraphics();

        try {
            // 背景填充白色
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            // 绘制条码
            g2d.drawImage(barcodeImg, 0, 0, width, barcodeHeight, null);

            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制下方文字（加粗）
            g2d.setColor(Color.BLACK);
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
            g2d.setFont(font);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int x = Math.max((width - textWidth) / 2, 0);
            int y = barcodeHeight + fm.getAscent() + 2;
            g2d.drawString(text, x, y);
        } finally {
            g2d.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(combinedImg, "PNG", out);
        return out.toByteArray();
    }

    public static byte[] generateQRCode(String text, int width, int height) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }
}

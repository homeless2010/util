package com.step.esms.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.step.esms.controller.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class QrcodeUtil2 {
    /**
     * 图片的宽度
     */
    private static final int IMAGE_WIDTH = 400;
    /**
     * 图片的高度(需按实际内容高度进行调整)
     */
    private static final int IMAGE_HEIGHT = 430;
    /**
     * 二维码的宽度
     */
    private static final int QR_CODE_WIDTH = 360;
    /**
     * 二维码的宽度
     */
    private static final int QR_CODE_HEIGHT = 360;

    private static final String FORMAT_NAME = "JPG";
    private static final String TITLE = "微信扫码查看救援进度";

    private static final String CONTENT = "十分士大夫发";
    private static final String FOOTER = "儿童热帖热帖";
    private static final String IMG_LOGO = "E:\\qrLogo.png";

    public void test() {
        // 头部文字区域高度
        int titleHeight = 50;

        // 创建主模板图片
        BufferedImage image = new BufferedImage(400, 430, BufferedImage.TYPE_INT_RGB);
        Graphics2D main = image.createGraphics();
//        image = main.getDeviceConfiguration().createCompatibleImage(400, 430, Transparency.TRANSLUCENT);
//        main.dispose();
//        main = image.createGraphics();
//        main.setColor(new Color(0, 0, 0, 0));
        main.setBackground(new Color(0, 0, 0, 0));
        main.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        main.dispose();

        // 动态高度
        int height = 0;

        //***********************页面头部 文字****************
        Graphics2D titleRight = image.createGraphics();
        // 设置字体颜色 black黑 white白
        titleRight.setColor(new Color(88, 86, 214));
        // 设置字体
        Font titleFont = new Font("黑体", Font.BOLD, 29);
        titleRight.setFont(titleFont);
        titleRight.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        // 居中 x开始的位置：（图片宽度-字体大小*字的个数）/2
        int x = (IMAGE_WIDTH - (titleFont.getSize() * TITLE.length())) / 2;
        titleRight.drawString(TITLE, x, (titleHeight) / 2 + 15);
        height += titleHeight;

        //**********************中间文字部分*********
//        Graphics2D centerWord = image.createGraphics();
//        // 设置字体颜色，先设置颜色，再填充内容
//        centerWord.setColor(Color.black);
//        titleRight.dispose();
        //***************插入二维码图片***********************************************
        Graphics codePic = image.getGraphics();
        BufferedImage codeImg;

        // 1、设置二维码的一些参数
        HashMap hints = new HashMap();

        // 1.1设置字符集
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // 1.2设置容错等级；因为有了容错，在一定范围内可以把二维码p成你喜欢的样式
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        // 1.3设置外边距;(即白色区域)
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode("ssssssssssss", BarcodeFormat.QR_CODE, 360, 360, hints);
            codeImg = toImage(bitMatrix, Color.black.getRGB(), Color.white.getRGB());
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }

/*        QrConfig config = new QrConfig();
        config.setWidth(QR_CODE_WIDTH);
        config.setHeight(QR_CODE_HEIGHT);
        config.setBackColor(Color.magenta.getRGB());
        config.setMargin(1);*/
        if (StrUtil.isNotBlank(IMG_LOGO)) {
            try {
                Image src = ImageIO.read(new File(IMG_LOGO));
                Image imageLogo = src.getScaledInstance(105, 105,
                        Image.SCALE_SMOOTH);
                BufferedImage tag = new BufferedImage(105, 105,
                        BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(imageLogo, 0, 0, null); // 绘制缩小后的图
                g.dispose();
                src = imageLogo;
                Graphics2D graph = codeImg.createGraphics();
                int x1 = (360 - 105) / 2;
                int y1 = (360 - 105) / 2;
                graph.drawImage(src, x1, y1, 105, 105, null);
                Shape shape = new RoundRectangle2D.Float(x1, y1, 105, 105, 6, 6);
                graph.setStroke(new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                graph.draw(shape);
                graph.dispose();
                // 给logo画边框
                // 构造一个具有指定线条宽度以及 cap 和 join 风格的默认值的实心 BasicStroke
//                graph.setStroke(new BasicStroke(3f));
//                graph.setColor(Color.red);
//                graph.drawRoundRect(x1, y1, 105, 105, 10, 10);
//                graph.setStroke(new BasicStroke(1));
//                graph.setColor(Color.GRAY);
//                graph.drawRoundRect(x1 + (3 / 2),  y1 + 3 / 2, 105 - 3,
//                        105 - 3, 10, 10);
                graph.dispose();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 插入LOGO
/*            config.setImg(IMG_LOGO);
            config.setRatio(4);*/
        }
//        codeImg = QrCodeUtil.generate(CONTENT, config);
        // 绘制二维码
        codePic.drawImage(codeImg, (IMAGE_WIDTH - QR_CODE_WIDTH) / 2, height, QR_CODE_WIDTH, QR_CODE_HEIGHT, null);
        codePic.dispose();


        //***************插入标志图片***********************************************
//        Graphics signPic = image.getGraphics();
//        BufferedImage signImg = null;
//        try {
//            signImg = ImageIO.read(new java.io.File(imgSign));
//        } catch (Exception e) {
//        }
//
//        if (signImg != null) {
//            signPic.drawImage(signImg, 0, 130, QR_CODE_WIDTH, QR_CODE_HEIGHT, null);
//            signPic.dispose();
//        }
        try {
            ImageIO.write(image, "png", new File("E://qrcode.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new QrcodeUtil2().test();
    }

    public static BufferedImage toImage(BitMatrix matrix, int foreColor, Integer backColor) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, null == backColor ? 2 : 1);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (matrix.get(x, y)) {
                    image.setRGB(x, y, foreColor);
                } else if (null != backColor) {
                    image.setRGB(x, y, backColor);
                }
            }
        }

        return image;
    }
}

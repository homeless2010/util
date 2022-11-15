package com.step.esms.util;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Test {
    /**
     * 图片的宽度
     */
    private static final int IMAGE_WIDTH = 400;
    /**
     * 图片的高度(需按实际内容高度进行调整)
     */
    private static final int IMAGE_HEIGHT = 500;
    /**
     * 二维码的宽度
     */
    private static final int QR_CODE_WIDTH = 360;
    /**
     * 二维码的宽度
     */
    private static final int QR_CODE_HEIGHT = 360;

    private static final String FORMAT_NAME = "JPG";
    private static final String TITLE = "十分十分发";

    private static final String CONTENT = "十分士大夫发";
    private static final String FOOTER = "儿童热帖热帖";
    private static final String IMG_LOGO = "E:\\qrLogo.png";

    public static void main(String[] args) {
//        QrConfig config = new QrConfig(400, 500);
//// 设置边距，既二维码和背景之间的边距
//        config.setMargin(3);
//// 设置前景色，既二维码颜色（青色）
//        config.setForeColor(Color.CYAN.getRGB());
//// 设置背景色（灰色）
//        config.setBackColor(Color.GRAY.getRGB());
//// 生成二维码到文件，也可以到流
//        QrCodeUtil.generate("http://hutool.cn/", config, FileUtil.file("e:/qrcode.jpg"));


        // 头部文字区域高度
        int titleHeight = 50;

        // 创建主模板图片
        BufferedImage image = new BufferedImage(400, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D main = image.createGraphics();
        // 设置图片的背景色
        main.setColor(Color.white); //白色
        main.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 动态高度
        int height = 0;

        //***********************页面头部 文字****************
        Graphics2D titleRight = image.createGraphics();
        // 设置字体颜色 black黑 white白
        titleRight.setColor(Color.black);
        // 设置字体
        Font titleFont = new Font("宋体", Font.BOLD, 25);
        titleRight.setFont(titleFont);
        titleRight.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        // 居中 x开始的位置：（图片宽度-字体大小*字的个数）/2
        int x = (IMAGE_WIDTH - (titleFont.getSize() * TITLE.length())) / 2;
        titleRight.drawString(TITLE, x, (titleHeight) / 2 + 10);
        height += titleHeight;

        //**********************中间文字部分*********
        Graphics2D centerWord = image.createGraphics();
        // 设置字体颜色，先设置颜色，再填充内容
        centerWord.setColor(Color.black);
        // 设置字体
        Font wordFont = new Font("宋体", Font.PLAIN, 15);
        centerWord.setFont(wordFont);
        centerWord.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        String[] info = CONTENT.split("-");
        for (String s : info) {
            // x开始的位置：（图片宽度-字体大小*字的个数）/2
            int strWidth = centerWord.getFontMetrics().stringWidth(s);
            // 总长度减去文字长度的一半  （居中显示）
            int startX = (IMAGE_WIDTH - strWidth) / 2;
            height += 20;
            centerWord.drawString(s, startX, height);
        }

        //***************插入二维码图片***********************************************
        Graphics codePic = image.getGraphics();
        BufferedImage codeImg;
        QrConfig config = new QrConfig();
        config.setWidth(QR_CODE_WIDTH);
        config.setHeight(QR_CODE_HEIGHT);
        config.setBackColor(Color.magenta.getRGB());
        config.setMargin(1);
        if (StrUtil.isNotBlank(IMG_LOGO)) {
//            try {
//                Image src = ImageIO.read(new File(IMG_LOGO));
//                Image imageLogo = src.getScaledInstance(105, 105,
//                        Image.SCALE_SMOOTH);
//                BufferedImage tag = new BufferedImage(105, 105,
//                        BufferedImage.TYPE_INT_RGB);
//                Graphics g = tag.getGraphics();
//                g.drawImage(imageLogo, 0, 0, null); // 绘制缩小后的图
//                g.dispose();
//                src = imageLogo;
//                Graphics2D graph = image.createGraphics();
//                int x1 = (400 - 105) / 2;
//                int y1 = (500 - 105) / 2;
//                graph.drawImage(src, x1, y1, 105, 105, null);
//                Shape shape = new RoundRectangle2D.Float(x1, y1, 105, 105, 6, 6);
//                graph.setStroke(new BasicStroke(3f));
//                graph.draw(shape);
//                graph.dispose();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            // 插入LOGO
            config.setImg(IMG_LOGO);
            config.setRatio(4);
        }
        codeImg = QrCodeUtil.generate(CONTENT, config);
        // 绘制二维码
        codePic.drawImage(codeImg, (IMAGE_WIDTH - QR_CODE_WIDTH) / 2, height, QR_CODE_WIDTH, QR_CODE_HEIGHT, null);
        codePic.dispose();

        //**********************底部公司名字*********
        Graphics2D typeLeft = image.createGraphics();
        // 设置字体颜色
        typeLeft.setColor(Color.black);
        // 设置字体
        Font footerFont = new Font("宋体", Font.PLAIN, 10);
        typeLeft.setFont(footerFont);
        typeLeft.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        // x开始的位置：（图片宽度-字体大小*字的个数）/2
        int startX = (IMAGE_WIDTH - (footerFont.getSize() * FOOTER.length())) / 2;
        height += QR_CODE_HEIGHT;
        typeLeft.drawString(FOOTER, startX, height);
        typeLeft.dispose();



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
            ImageIO.write(image, "jpg", new File("E://qrcode.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

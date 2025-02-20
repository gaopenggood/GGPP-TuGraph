package com.ggpp.tugraph.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class ImageUtils {

    public static int color_range = 210;

    public static BufferedImage base64ToImage(String picStr) {
        BufferedImage image = null;
        try {
            // 解码Base64字符串为字节数组
            byte[] imageData = Base64.getDecoder().decode(picStr.replace("data:image/png;base64,",""));

            // 使用字节数组输入流和ImageIO来读取图像数据并转换为BufferedImage
            try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
                image = ImageIO.read(bis);
                // 现在你可以对BufferedImage对象进行操作，例如显示或保存图像等。
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage file2BufferedImage(String filePath) {
        File inputFile = new File(filePath);
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
//            BufferedImage decodedImage = WebPDecoder.decode(inputStream)
            return ImageIO.read(inputFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static double getCosine(double angle) {
        double radian = Math.toRadians(angle); // 将角度转换为弧度
        return Math.cos(radian); // 计算余弦值
    }

    public static double getSin(double angle) {
        double radian = Math.toRadians(angle); // 将角度转换为弧度
        return Math.sin(radian); // 计算正弦值
    }

    public static float getOffset(int i, double angle) {
        float n = 1.414f;
        float m = 0.5f;
        int a = 45;
        float l = i * n * m;
        if(angle <= a) {
            double a2 = a - angle;
            float pos = (float) getCosine(a2) * l;
            return  pos;
        }else{
            double a2 = angle - a;
            float pos = (float) getCosine(a2) * l;
            return  pos;
        }
    }

    public static BufferedImage emptyBackground(BufferedImage originalImage) {
        // 创建一个带有透明度通道的新图片
        BufferedImage transparentImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        try {
            // 遍历原始图片的每个像素
            for (int y = 0; y < originalImage.getHeight(); y++) {
                for (int x = 0; x < originalImage.getWidth(); x++) {
                    // 获取原始图片的像素颜色
                    int rgb = originalImage.getRGB(x, y);
                    Color color = new Color(rgb);
                    // 检查像素颜色是否为白色（#FFFFFF），这里可以根据需要调整阈值
                    if (color.getGreen() > 240 && color.getBlue() > 240 && color.getRed() > 240) {
                        // 设置透明色
                        transparentImage.setRGB(x, y, 0x00FFFFFF); // 设置为完全透明的白色
                    } else {
                        // 复制非白色像素
                        transparentImage.setRGB(x, y, rgb);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transparentImage;
    }

    public static String image2Base64(String imagePath) {
        String str = "";
        try {
            // 读取图片文件为字节数组
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            // 将字节数组转换为Base64编码的字符串
            str = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String image2Base64(BufferedImage image) {
        String str = "";
        try {
            // 将BufferedImage转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos); // 选择合适的图片格式，如png, jpg等
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            // 使用Base64对字节数组进行编码
            str = Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static BufferedImage resizeImage(BufferedImage srcImage, int targetWidth, int targetHeight) {
        int srcWidth = srcImage.getWidth();
        int srcHeight = srcImage.getHeight();

        // 创建缩放后的图片缓冲区
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, srcImage.getType());
        Graphics2D g2d = resizedImage.createGraphics();

        // 设置缩放质量和抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // 使用AffineTransform进行缩放
        AffineTransform at = AffineTransform.getScaleInstance((double) targetWidth / srcWidth, (double) targetHeight / srcHeight);
        g2d.drawRenderedImage(srcImage, at);

        g2d.dispose();

        return resizedImage;
    }

    public static BufferedImage makeBackgroundTransparent(BufferedImage inputImage) {
        // 2. 创建等大小的 ARGB 格式图片（允许透明）
        BufferedImage outputImage = new BufferedImage(
                inputImage.getWidth(),
                inputImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB // 必须使用 ARGB 支持透明度
        );

        // 3. 遍历每个像素点
        for (int y = 0; y < inputImage.getHeight(); y++) {
            for (int x = 0; x < inputImage.getWidth(); x++) {
                // 获取当前像素的 RGB 值
                int rgb = inputImage.getRGB(x, y);

                // 提取红、绿、蓝分量（无需 Alpha，因为原始图可能没有）
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // 判断是否为黑色（严格判断 R=0,G=0,B=0）
                boolean isBlack = (red <= 100) && (green <= 100) && (blue <= 100);

                if (isBlack) {
                    // 如果是黑色，保留原色（设置 Alpha=255 完全不透明）
                    outputImage.setRGB(x, y, rgb | 0xFF000000);
                } else {
                    // 其他颜色转为透明（设置 Alpha=0）
                    outputImage.setRGB(x, y, 0x00000000);
                }
            }
        }

        // 4. 保存为PNG（必须选 PNG 以支持透明度）
        return outputImage;
    }

}

package com.ggpp.tugraph.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
        int i=0;
        int j=0;
        for (int y = 0; y < inputImage.getHeight(); y++) {
            for (int x = 0; x < inputImage.getWidth(); x++) {
                // 获取当前像素的 RGB 值
                int rgb = inputImage.getRGB(x, y);

                // 提取红、绿、蓝分量（无需 Alpha，因为原始图可能没有）
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                int alpha = (rgb >> 24) & 0xFF;

                // 如果alpha值为0，表示该像素完全透明
                if (alpha == 0) {
                    i++;
                    continue;
                }
                j++;
                // 判断是否为黑色（严格判断 R=0,G=0,B=0）
                boolean isBlack = (red <= 120) && (green <= 120) && (blue <= 120);

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

    // 要识别为背景的颜色：纯白色
    private static final Color TARGET_BG_COLOR = Color.WHITE;
    // 容差值：0=严格匹配白色，1-20=允许接近白色也变透明（根据你的图片调整）
    private static final int TOLERANCE = 10;

    /**
     * 批量处理图片：白底转透明
     */
    public static void makeBackgroundTransparent(String sourceDir, String outputDir) throws IOException {
        // 1. 创建输出目录（不存在则自动创建）
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // 2. 遍历源目录所有文件
        File sourceFolder = new File(sourceDir);
        File[] files = sourceFolder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("源文件夹没有可处理的图片");
            return;
        }

        int successCount = 0;
        for (File file : files) {
            if (file.isDirectory()) continue;

            String fileName = file.getName().toLowerCase();
            if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                continue; // 只处理图片
            }

            try {
                // 读取图片
                BufferedImage original = ImageIO.read(file);
                if (original == null) continue;

                // 创建带透明通道的新图片
                BufferedImage transparentImage = new BufferedImage(
                        original.getWidth(),
                        original.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );

                // 逐像素处理
                for (int y = 0; y < original.getHeight(); y++) {
                    for (int x = 0; x < original.getWidth(); x++) {
                        int rgb = original.getRGB(x, y);
                        Color pixelColor = new Color(rgb, true);

                        // 判断是否是背景色（含容差）
                        if (isBackgroundColor(pixelColor)) {
                            // 透明像素
                            transparentImage.setRGB(x, y, 0x00FFFFFF);
                        } else {
                            // 保留原像素
                            transparentImage.setRGB(x, y, rgb);
                        }
                    }
                }

                // 输出文件（统一保存为 PNG，因为只有 PNG 支持透明）
                String outputFileName = getBaseName(file.getName()) + ".png";
                File outputFile = new File(outputPath.toFile(), outputFileName);
                ImageIO.write(transparentImage, "PNG", outputFile);

                successCount++;
                System.out.println("处理完成：" + file.getName());

            } catch (Exception e) {
                System.err.println("处理失败：" + file.getName() + "，原因：" + e.getMessage());
            }
        }

        System.out.println("\n✅ 全部处理完成，成功：" + successCount + " 张");
    }

    public static void rotateCounterClockwise90(String sourceDir, String outputDir) throws IOException {
        // 1. 创建输出目录
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // 2. 获取源文件夹所有文件
        File sourceFolder = new File(sourceDir);
        File[] files = sourceFolder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("源文件夹无图片");
            return;
        }

        int success = 0;
        for (File file : files) {
            if (file.isDirectory()) continue;

            String name = file.getName().toLowerCase();
            if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg")
                    && !name.endsWith(".bmp") && !name.endsWith(".gif")) {
                continue;
            }

            try {
                // 输出文件路径（保持原名）
                File outFile = new File(outputPath.toFile(), file.getName());
                        // ========== 核心：Thumbnails 逆时针旋转90度 ==========
                Thumbnails.of(file)
                        .size(100, 100)
//                        .size(file.length() > 1024*1024*10 ? 1920 : 4096, 4096) // 超大图自动限制尺寸，避免OOM
                        .rotate(-90)  // -90 = 逆时针90度；90 = 顺时针
                        .outputQuality(1.0f) // 100%质量不压缩
                        .toFile(outFile);
                // ====================================================

                success++;
                System.out.println("旋转完成：" + file.getName());

            } catch (Exception e) {
                System.err.println("旋转失败：" + file.getName() + " → " + e.getMessage());
            }
        }

        System.out.println("\n✅ 全部处理完成，成功旋转：" + success + " 张");
    }

    // 线条加粗：2px（可改 1/2/3）
    private static final int BOLD_PX = 5;
    // 黑色/深色线条才会被加粗
    private static final int LINE_DARK_THRESHOLD = 180;

    /**
     * 独立方法：只做【线条加粗2px】，不旋转、不透明
     */
    public static BufferedImage boldLine2px(BufferedImage original) {
        int w = original.getWidth();
        int h = original.getHeight();

        // 创建新图片
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(original, 0, 0, null);

        // 加粗核心：深色线条周围扩张 2px
        for (int dy = -BOLD_PX; dy <= BOLD_PX; dy++) {
            for (int dx = -BOLD_PX; dx <= BOLD_PX; dx++) {
                if (dx == 0 && dy == 0) continue; // 跳过中心点
                if (Math.abs(dx) + Math.abs(dy) > BOLD_PX) continue; // 控制扩张范围

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int rgb = original.getRGB(x, y);
                        Color c = new Color(rgb);

                        // 只处理深色线条（黑/灰黑）
                        if (isDarkLine(c)) {
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                                result.setRGB(nx, ny, rgb);
                            }
                        }
                    }
                }
            }
        }
        g2d.dispose();
        return result;
    }

    /**
     * 批量文件夹处理：输入 → 输出（纯加粗，不做其他处理）
     */
    public static void batchBoldLine(String sourceDir, String outputDir) throws IOException {
        Path outPath = Paths.get(outputDir);
        Files.createDirectories(outPath);

        File[] files = new File(sourceDir).listFiles();
        if (files == null || files.length == 0) {
            System.out.println("文件夹无图片");
            return;
        }

        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) continue;
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg")) continue;

            try {
                BufferedImage image = ImageIO.read(file);
                BufferedImage boldImage = boldLine2px(image); // 独立加粗方法调用

                File outFile = new File(outPath.toFile(), file.getName());
                ImageIO.write(boldImage, "PNG", outFile);
                count++;
                System.out.println("加粗完成：" + file.getName());
            } catch (Exception e) {
                System.err.println("失败：" + file.getName());
            }
        }
        System.out.println("\n✅ 批量加粗完成：" + count + " 张");
    }

    // 判断是否为深色线条
    private static boolean isDarkLine(Color c) {
        int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        return gray < LINE_DARK_THRESHOLD;
    }

    /**
     * 判断像素是否属于要透明化的背景色
     */
    private static boolean isBackgroundColor(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        int tr = TARGET_BG_COLOR.getRed();
        int tg = TARGET_BG_COLOR.getGreen();
        int tb = TARGET_BG_COLOR.getBlue();

        return Math.abs(r - tr) <= TOLERANCE
                && Math.abs(g - tg) <= TOLERANCE
                && Math.abs(b - tb) <= TOLERANCE;
    }

    /**
     * 获取文件名（不含后缀）
     */
    private static String getBaseName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }

}

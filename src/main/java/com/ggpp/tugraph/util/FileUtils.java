package com.ggpp.tugraph.util;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.FileImageInputStream;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class FileUtils {

    /**
     * 计算SHA256哈希值
     * @param filePath 文件路径
     * @return 字节数组
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException NoSearch算法异常
     */
    public static byte[] calculateSHA256(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (
                FileInputStream fis = new FileInputStream(filePath);
                FileChannel channel = fis.getChannel();
                DigestInputStream dis = new DigestInputStream(fis, digest)) {
            ByteBuffer buffer = ByteBuffer.allocate(8192); // 8 KB buffer
            while (channel.read(buffer) != -1) {
                buffer.flip();
                digest.update(buffer);
                buffer.clear();
            }
            return digest.digest();
        }
    }
    /**
     * 将字节数组转换为String类型哈希值
     * @param bytes 字节数组
     * @return 哈希值
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    public static String formatterSHA256(String filePath) {
        String sha256Hex = "";
        try {
            byte[] sha256 = calculateSHA256(filePath);
            sha256Hex = bytesToHex(sha256);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha256Hex;
    }

    public static String p12Signature(String p12FilePath, String keystorePassword, String data) {
        String str = "";
        try {
            FileInputStream fis = new FileInputStream(p12FilePath);
            // 加载P12文件
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, keystorePassword.toCharArray());

            // 获取私钥别名
            String alias = keyStore.aliases().nextElement();

            // 获取私钥
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keystorePassword.toCharArray());

            // 初始化签名对象
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);

            // 更新要签名的数据
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            // 生成签名
            byte[] signatureBytes = signature.sign();

            // 将签名转换为Base64编码的字符串
            String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);
            str = signatureBase64;
            System.out.println("Signature (Base64): " + signatureBase64);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void exportPfx(String certBase64, String privKeyBase64, String pfxPath, String alias, String pfxPassword) {

        try{
            // 解码BASE64字符串以获取证书和私钥的字节
            byte[] certBytes = Base64.getDecoder().decode(certBase64);
            byte[] privKeyBytes = Base64.getDecoder().decode(privKeyBase64);

            // 创建证书和私钥实例
            Certificate cert = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBytes));
            PrivateKey privKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));

            // 创建PKCS#12 KeyStore
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);

            // 将证书和私钥添加到KeyStore中
            keyStore.setKeyEntry(alias, privKey, pfxPassword.toCharArray(), new Certificate[]{cert});

            // 将KeyStore保存为PFX文件
            try (FileOutputStream fos = new FileOutputStream(pfxPath)) {
                keyStore.store(fos, pfxPassword.toCharArray());
            }

            System.out.println("PFX file created successfully.");
        }catch (Exception e ) {
            System.out.printf(e.getMessage());
        }
    }

    public static void exportToPfx(String certBase64, String keyBase64, String pfxFilePath, String alias, String pfxPassword, String p12Path) {
        try{
            // 解码私钥和证书
            byte[] encodedCertificate = Base64.getDecoder().decode(certBase64);

            // 创建X509Certificate对象
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(encodedCertificate));

            PrivateKey key = formatterPrivateKey(p12Path,pfxPassword);
            // 创建KeyStore并添加密钥对
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null); // 初始化一个空的KeyStore
            keyStore.setKeyEntry(alias, key, pfxPassword.toCharArray(), new Certificate[]{cert});

            // 保存KeyStore为PFX文件
            try (FileOutputStream fos = new FileOutputStream(pfxFilePath)) {
                keyStore.store(fos, pfxPassword.toCharArray());
            }

            System.out.println("PFX certificate generated successfully!");
        }catch (Exception e ){
            System.out.printf(e.getMessage());
        }

    }

    private static PrivateKey formatterPrivateKey(String p12Path, String pfxPassword) {
        // 获取私钥
        PrivateKey privateKey = null;
        try {
            FileInputStream fis = new FileInputStream(p12Path);
            // 加载P12文件
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, pfxPassword.toCharArray());
            // 获取私钥别名
            String alias = keyStore.aliases().nextElement();
            privateKey = (PrivateKey) keyStore.getKey(alias, pfxPassword.toCharArray());
        } catch (Exception e) {
            System.out.printf(e.getMessage());
        }
        return privateKey;
    }

    public static X509Certificate formatterCert(String certStr) {
        X509Certificate cert = null;
        try{
            // 解码私钥和证书
            byte[] encodedCertificate = Base64.getDecoder().decode(certStr);

            // 创建X509Certificate对象
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(encodedCertificate));
        }catch (Exception e ) {
            System.out.printf(e.getMessage());
        }
        return cert;
    }

    public static String getParentDir() {
        String tempDir = "";
        String tempDirStr = StrUtil.isEmpty(System.getProperty("java.io.tmpdir")) ? "/temp" : System.getProperty("java.io.tmpdir");
        String lastStr = tempDirStr.substring(tempDirStr.length() - 1, tempDirStr.length());
        if ("\\".equals(lastStr) || "/".equals(lastStr)) {
            tempDir = tempDirStr;
        } else {
            tempDir = tempDirStr + "/";
        }
        String path = tempDir + "upload/temporary" + "/" + IdWorker.getId();
        FileUtils.mkdirs(path);
        System.out.printf("临时文件夹路径为：" + path);
        return path;
    }

    /**
     * 创建文件夹
     */
    public static void mkdirs(String path) {
        File file = new File(path);
        if (!file.exists()) {
            String parentPath = file.getParent();
            if(!new File(parentPath).exists()) {
                mkdirs(parentPath);
            }
            file.mkdirs();
        }
    }

    /**
     * 删除文件
     */
    public static void deleteFilePathDir(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            boolean delResult = file.delete();
            if(delResult) {
                System.out.println("文件【"+file.getName()+"】已删除！");
            }else{
                System.out.println("文件【"+file.getName()+"】删除失败,正在尝试重新删除...");
                forceDelete(file);
            }
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteFilePathDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }

    public static void forceDelete(File file) {
        boolean result = file.delete();
        int tryCount = 0;
        while (!result && tryCount++ < 10) {
            System.gc(); //回收资源
            result = file.delete();
        }
        if(result) {
            System.out.println("文件【"+file.getName()+"】已删除！");
        }else{
            System.out.println("文件【"+file.getName()+"】删除失败,请联系管理员！");
        }
    }

    public static String image2Base64(String path){
        byte[] data = null;
        FileImageInputStream input = null;
        try {
            input = new FileImageInputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while((numBytesRead = input.read(buf)) != -1){
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return replaceEnter(Base64Encoder.encode(data));
    }

    public static String image2Base64(File file){
        byte[] data = null;
        FileImageInputStream input = null;
        try {
            input = new FileImageInputStream(file);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while((numBytesRead = input.read(buf)) != -1){
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return replaceEnter(Base64Encoder.encode(data));
    }

    public static byte[] getByte(String filePath) {
        byte[] data = null;
        FileImageInputStream input = null;
        try {
            input = new FileImageInputStream(new File(filePath));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while((numBytesRead = input.read(buf)) != -1){
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static String imageFile2Base64(MultipartFile file) {
        byte[] data = null;
        InputStream input = null;
        try {
            input = file.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int numBytesRead = 0;
            while((numBytesRead = input.read(buf)) != -1){
                output.write(buf, 0, numBytesRead);
            }
            data = output.toByteArray();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return replaceEnter(Base64Encoder.encode(data));
    }

    public static String replaceEnter(String str){
        String reg ="[\n-\r]";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }

    public static void string2image(String picPath, String picStr) {
//        String base64UrlString = "你的Base64URL字符串"; // 替换为你的Base64URL编码的字符串

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(picStr.replace("data:image/jpg;base64,",""));
            FileOutputStream fos = new FileOutputStream(picPath);
            fos.write(decodedBytes);
            System.out.println("文件已成功创建！");
            fos.close();
        } catch (Exception e) {
            System.err.println("解码错误: " + e.getMessage());
        }
    }

    public static void doDownload(String filePath, HttpServletResponse response) {
        File zipFile = new File(filePath);
        response.setHeader("content-type", "text/html;charset=UTF-8");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFile.getName());
        byte[] buff = new byte[1024];
        //创建缓冲输入流
        BufferedInputStream bis = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            InputStream inputStream = null;
            //这个路径为待下载文件的路径
            bis = new BufferedInputStream(new FileInputStream(zipFile));
            int read = bis.read(buff);
            //通过while循环写入到指定了的文件夹中
            while (read != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                read = bis.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //出现异常返回给页面失败的信息
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void doDownloadByIn(InputStream inputStream, HttpServletResponse response) {
        response.setHeader("content-type", "text/html;charset=UTF-8");
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; ");
        response.setHeader("Content-Disposition", "attachment; filename=" + IdWorker.getId() + ".pdf");

        byte[] buff = new byte[1024];
        //创建缓冲输入流
        BufferedInputStream bis = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            //这个路径为待下载文件的路径
            bis = new BufferedInputStream(inputStream);
            int read = bis.read(buff);
            //通过while循环写入到指定了的文件夹中
            while (read != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                read = bis.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //出现异常返回给页面失败的信息
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveToFile(InputStream inStream, File file) throws IOException {
        try (OutputStream outStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 解压缩文件
     */
    public static void zipDecompression(Path file, Path targetDir, Charset charset) throws IOException {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        // 创建zip对象
//        ZipFile zipFile = new ZipFile(file.toFile());
        ZipFile zipFile = new ZipFile(file.toFile(), charset);
        try {
            // 读取zip流
            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(file))) {
                ZipEntry zipEntry = null;
                // 遍历每一个zip项
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    // 获取zip项目名称
                    String entryName = zipEntry.getName();
                    // 构建绝对路径
                    Path entryFile = targetDir.resolve(entryName);
                    //解压缩到文件夹，没有则新建之
                    mkdirs(new File(entryFile.toString()).getParent());
                    if (zipEntry.isDirectory()) {    // 文件夹
                        if (!Files.isDirectory(entryFile)) {
                            Files.createDirectories(entryFile);
                        }
                    } else {                            // 文件
                        // 读取zip项数据流
                        try (InputStream zipEntryInputStream = zipFile.getInputStream(zipEntry)) {
                            try (OutputStream fileOutputStream = Files.newOutputStream(entryFile, StandardOpenOption.CREATE_NEW)) {
                                byte[] buffer = new byte[4096];
                                int length = 0;
                                while ((length = zipEntryInputStream.read(buffer)) != -1) {
                                    fileOutputStream.write(buffer, 0, length);
                                }
                                fileOutputStream.flush();
                            }
                        }
                    }
                }
            }
        } finally {
            zipFile.close();
        }
    }

    public static Path doFileUpload(MultipartFile file, String pathDir) {
        Path path = null;
        try {
            String fileName = file.getOriginalFilename();
            String targetPath = pathDir + "/" + fileName;
            System.out.println("完整的上传路径：" + targetPath);
            //根据srcFile大小，准备一个字节数组
            byte[] bytes = file.getBytes();
            //拼接上传路径
            Path paths = Paths.get(targetPath);//file.getOriginalFilename()
            path = paths;
            //将源文件写入目标地址
            Files.write(paths, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("上传文件失败");
        }
        return path;
    }

    public static void unzip2(Path zipFilePath, Path destPath) throws IOException {
        if (!Files.exists(destPath)) {
            Files.createDirectories(destPath);
        }

        try (InputStream fileInputStream = Files.newInputStream(zipFilePath);
             ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = destPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    // Ensure parent directories are created
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
    }

    public static void unzip3(String zipFilePath, String destDir) throws IOException {
        File destDirectory = new File(destDir);
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDir + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                fos.write(bytesIn, 0, read);
            }
        }
    }
}

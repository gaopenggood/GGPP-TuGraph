package com.ggpp.tugraph.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ggpp.tugraph.domain.BaseUser;
import com.ggpp.tugraph.mapper.BaseUserMapper;
import com.ggpp.tugraph.util.FileUtils;
import com.ggpp.tugraph.util.ImageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class MainService {

    @Resource
    private BaseUserMapper baseUserMapper;
    public Object getDataFromDB() {
        List<BaseUser> list = baseUserMapper.selectList(new LambdaQueryWrapper<BaseUser>()
                .eq(BaseUser::getId, 1L));
        return list;
    }

    @Transactional
    public void doTuGraphTest() {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        try (Session session = driver.session(SessionConfig.forDatabase("neo4j"))) {
            // 在自动提交模式下，每个查询都会自动在一个事务中执行并提交
            // 打印结果（可选）
            session.run("CALL db.dropDB()");
            session.run("CALL db.createVertexLabel('person', 'id' , 'id' ,INT32, false, 'name' ,STRING, false)");
            session.run("CALL db.createEdgeLabel('is_friend','[[\"person\",\"person\"]]')");
            session.run("create (n1:person {name:'jack',id:1}), (n2:person {name:'lucy',id:2})");
            session.run("match (n1:person {id:1}), (n2:person {id:2}) create (n1)-[r:is_friend]->(n2)");
            Result res = session.run("match (n)-[r]->(m) return n,r,m");
            List<Record> records =  res.list();
            for (Record record : records) {
                Node n = record.get("n").asNode();
                System.out.println(n.asMap());
                Relationship r = record.get("r").asRelationship();
                System.out.println(r.asMap());
                Node m = record.get("m").asNode();
                System.out.println(m.asMap());
            }
        } finally {
            // 关闭驱动程序
            driver.close();
        }

    }

    public void doStr2Png(String text) {
        String filePath = "C:\\Users\\DELL\\Desktop\\output.png";
        int imageWidth = 236;
        int imageHeight = 236;
        String parentPath = this.getParentDir();
        String pdfDir = parentPath + "/pdfFile";
        String certDir = parentPath + "/pdfFile";
        String picDir = parentPath + "/pdfFile";
        // 调用方法将文本转换为图片
        String base64Str = "";
        try {
            BufferedImage image = ImageUtils.base64ToImage(text);//textToImage(text, imageWidth, imageHeight);
            String picPathNew = this.formatterImagePath(1,1,0,image,parentPath);
            log.info("新图片地址："+picPathNew);
            BufferedImage image2 = ImageIO.read(new File(picPathNew));
            log.info("111");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getParentDir() {
        String tempDir = "";
        String tempDirStr = StrUtil.isEmpty(System.getProperty("java.io.tmpdir")) ? "/temp" : System.getProperty("java.io.tmpdir");
        String lastStr = tempDirStr.substring(tempDirStr.length() - 1, tempDirStr.length());
        if ("\\".equals(lastStr) || "/".equals(lastStr)) {
            tempDir = tempDirStr;
        } else {
            tempDir = tempDirStr + "/";
        }
        String path = tempDir + "temporary" + "/" + IdWorker.getId();
        FileUtils.mkdirs(path);
        log.info("临时文件夹路径为：" + path);
        return path;
    }

    private String formatterImagePath(double scaleX, double scaleY, double angle, BufferedImage image, String parentPath) {
        String name = String.valueOf(IdWorker.getId());
        FileUtils.mkdirs(parentPath + "/pic/");
        String filePath = parentPath + "/pic/" + name + ".png";
        log.info("图片地址：\n"+filePath);
        //缩放
        try {
            Thumbnails.of(image).outputFormat("png").scale(Math.max(scaleX,scaleY)).rotate(angle).outputQuality(1.0).toFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filePath;
    }

    private BufferedImage textToImage(String text, int imageWidth, int imageHeight) throws IOException, FontFormatException {
        // 加载字体文件
        Font customFont = new Font("宋体 ", 0, 64);
        // 创建一个BufferedImage对象
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        // 获取Graphics2D对象
        Graphics2D g2d = image.createGraphics();
        // 开启抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置字体颜色
        g2d.setColor(Color.RED);
        // 设置字体
        g2d.setFont(customFont);
        // 获取字体的FontMetrics对象
        FontMetrics fm = g2d.getFontMetrics();
        // 计算文本的起始x坐标，使文本居中显示
        int x = (imageWidth - fm.stringWidth(text)) / 2;
        // 计算文本的基线y坐标，使文本垂直居中
        int y = ((imageHeight - fm.getHeight()) / 2) + fm.getAscent();
        // 绘制文本
        g2d.drawString(text, x, y);
        // 释放Graphics2D对象
        g2d.dispose();

        return image;
    }

    private String handlePicDia(String picBase64, int width, int height) {
        String str = "";
        String parentPath = this.getParentDir();
        String name = "thumbnailator";
        FileUtils.mkdirs(parentPath + "/pic/");
        String filePath = parentPath + "/pic/" + name + ".png";
        BufferedImage image = ImageUtils.base64ToImage(picBase64);
        try {
            //缩放成指定长宽
            Thumbnails.of(image)
                    .size(60, 60)
                    .keepAspectRatio(false)
                    .toFile(new File(filePath));
            log.info("图片已处理成长："+60+"宽："+60);
            BufferedImage resizeImg = ImageIO.read(new File(filePath));
            //扣成背景透明
            BufferedImage img = ImageUtils.emptyBackground(resizeImg);
            str = ImageUtils.image2Base64(img);
            log.info("str:data:image/png;base64,"+str);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
//        FileUtils.deleteFilePathDir(parentPath);
        return filePath;
    }

    public void doFile2Png(MultipartFile file) {
        String picBase64 = "data:image/png;base64,"+FileUtils.imageFile2Base64(file);
//        String str1 = picBase64;
//        String path = this.handlePicDia(str1,60,60);
//        log.info("thumbnailator图片地址："+path);
        String str2 = picBase64;
        String path2 = this.handlePicDiaByImgscalr(str2);
        log.info("imgscalr图片地址："+path2);
    }

    private String handlePicDiaByImgscalr(String str2) {
        String parentPath = this.getParentDir();
        String name1 = "base";
        String name2 = "imgscalr";
        FileUtils.mkdirs(parentPath + "/pic/");
        String path = parentPath + "/pic/" + name1 + ".png";
        String path2 = parentPath + "/pic/" + name2 + ".png";
        // 1. 读取原图（假设输入文件为 input.png）
        BufferedImage image = ImageUtils.base64ToImage(str2);
        try {
//            //扣成背景透明
//            BufferedImage img = ImageUtils.makeBackgroundTransparent(image);//ImageUtils.emptyBackground(resizeImg);
//            //缩放成指定长宽
//            Thumbnails.of(img)
//                    .size(60, 60)
//                    .keepAspectRatio(false)
//                    .toFile(new File(path));
//            log.info("图片已处理成长："+60+"宽："+60);
//            BufferedImage resizeImg = ImageIO.read(new File(path));
//            ImageIO.write(resizeImg, "png", new File(path2));
//            log.info("path1:"+path);
//            log.info("path2:"+path2);

            BufferedImage img = ImageUtils.makeBackgroundTransparent(image);
            Thumbnails.of(img)
                    .size(60, 60)
                    .keepAspectRatio(false)
                    .toFile(new File(path));
            BufferedImage resizeImg = ImageIO.read(new File(path));
            log.info("图片已处理成长："+60+"宽："+60);
            //扣成背景透明
            String str = ImageUtils.image2Base64(resizeImg);
            log.info("str:data:image/png;base64,"+str);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return path2;
    }

    public void doFile2Local(MultipartFile file) {
        String msg = "";
        //校验文件类型
        String fileName = file.getOriginalFilename();
        if(!fileName.contains(".") || !"zip".equals(fileName.split("\\.")[1])) {
            msg = "文件类型不正确，请检查后重新上传";
        }
        String parentPath = this.getParentDir();
        //解压文件
        String targetPath = parentPath+"/"+ IdWorker.getId();
        FileUtils.mkdirs(targetPath);
        Path base = FileUtils.doFileUpload(file, parentPath);
        System.out.println("压缩包地址："+base.toString());
        Path target = new File(targetPath).toPath();
        //解压缩
        try {
            FileUtils.zipDecompression(base, target, Charset.forName("UTF-8"));
            log.info("zip文件解压成功！路径：" + targetPath);
        } catch (Exception e) {
            log.error("文件解压缩失败!"+e.getMessage());
            msg = "文件解压缩失败！";
        }
        if(!ObjectUtils.isEmpty(msg)) {
            FileUtils.deleteFilePathDir(parentPath);
        }
        //解析文件名和图片
        File files = new File(targetPath);
        List<File> listFiles = new ArrayList<>();
        this.getFileList(files, listFiles);
        log.info("共【"+listFiles.size()+"】张印章图片");
    }
    private void getFileList(File files, List<File> fileList) {
        for(File f : files.listFiles()) {
            if(f.isFile()) {
                fileList.add(f);
            }else{
                this.getFileList(f, fileList);
            }
        }
    }

    public void changeFilePath() {
        //查询已存在的人员名称
        Map<String, String> userNameMap = this.findUserNameFromNeo4j();
        String baseDir = "D:\\0 工作日志\\1 电子签章\\1111";
        String targetDir = "D:\\0 工作日志\\1 电子签章\\pics";
        File baseFile = new File(baseDir);
        this.changeFileDir(baseFile,targetDir,userNameMap);
    }

    private void changeFileDir(File baseFile, String targetDir, Map<String, String> userNameMap) {
        for(File f : baseFile.listFiles()) {
            this.doFileMove(f,targetDir,userNameMap);
        }
    }

    private void doFileMove(File baseFile, String targetDir, Map<String, String> userNameMap) {
        Path basePath = baseFile.toPath();
        String name = this.getNameFromFileName(baseFile.getName());
        String actName = "";
        if(name.contains("-")) {
            int length = name.split("-").length;
            actName = name.split("-")[length-1];
        }else{
            actName = name;
        }
        if(!userNameMap.containsKey(actName) || ObjectUtils.isEmpty(userNameMap.get(actName))) {
            log.info("用户【"+actName+"】不在用户列表");
        }else{
            log.info("用户【"+actName+"】开始迁移");
            String userName = userNameMap.get(actName);
            String baseName = baseFile.getName();
            String fileName = baseName.replaceAll(name, userName);
            String targetPath = targetDir+"\\"+fileName;
            Path path = Paths.get(targetPath);
            try {
                // 使用 Files.move() 方法重命名文件
                Files.move(basePath, path);
                log.info("文件["+name+"]重命名成功！");
            } catch (IOException e) {
                log.error("文件重命名失败：" + e.getMessage());
            }
        }
    }

    private String getNameFromFileName(String name) {
        String fNameFront = name.split("\\.")[0];
        return fNameFront;
    }


    private Map<String, String> findUserNameFromNeo4j() {
        Map<String, String> reMap = new HashMap<>();
        Driver driver = GraphDatabase.driver("bolt://192.168.80.168:7687", AuthTokens.basic("neo4j", "GGpp1993@"));
        String query = "MATCH (n:base_acc_user) RETURN n.name,n.user_name";
        log.info("查询cypher"+query);
        try {
            Session session = driver.session(SessionConfig.forDatabase("neo4j"));
            Result result = session.run(query);
            List<org.neo4j.driver.Record> records = new ArrayList<>();//result.list();
            while(result.hasNext()) {
//                org.neo4j.driver.Record row = result.next();
                records.add(result.next());
            }
            if (!records.isEmpty()) {
                for (org.neo4j.driver.Record record : records) {
                    List<Pair<String, Value>> l = record.fields();
                    String userName = "";
                    String name = "";
                    for(Pair<String, Value> pair : l) {
                        if("n.name".equals(pair.key())) {
                            name = pair.value().asString();
                        }
                        if("n.user_name".equals(pair.key())) {
                            userName = pair.value().asString();
                        }
                    }
                    reMap.put(name, userName);
                }
            }
        }catch (Exception e){
            log.error(e.getMessage()+"qq");
        }
        return reMap;
    }
}

package com.ggpp.tugraph.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ggpp.tugraph.domain.BaseUser;
import com.ggpp.tugraph.mapper.BaseUserMapper;
import com.ggpp.tugraph.util.FileUtils;
import com.ggpp.tugraph.util.ImageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
        String name = String.valueOf(IdWorker.getId());
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
        String path = this.handlePicDia(picBase64,60,60);
        log.info("图片地址："+path);
    }
}

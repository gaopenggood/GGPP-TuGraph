package com.ggpp.tugraph.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ggpp.tugraph.domain.BaseUser;
import com.ggpp.tugraph.domain.dto.BudgetDto;
import com.ggpp.tugraph.domain.dto.MainDto;
import com.ggpp.tugraph.listener.ExcelDataListener;
import com.ggpp.tugraph.mapper.BaseUserMapper;
import com.ggpp.tugraph.util.FileUtils;
import com.ggpp.tugraph.util.ImageUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class MainService {

    @Resource
    private DbService db;

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
        Map<String, String> userNameMap = this.findUserNameFromDb();
        String baseDir = "D:\\0 工作日志\\1 电子签章\\fileReplace\\1";
        String targetDir = "D:\\0 工作日志\\1 电子签章\\fileReplace\\3";
        File baseFile = new File(baseDir);
        String nameAll = "";
        this.changeFileDir(baseFile,targetDir,userNameMap,nameAll);
        log.info("错误名单"+nameAll);
    }

    private Map<String, String> findUserNameFromDb() {
        List<Map<String, Object>> list = db.doGet("select id,name,user_name from base_acc_user");
        Map<String, String> map = new HashMap<>();
        for(Map<String, Object> map1 : list) {
            if(ObjectUtils.isEmpty(map1.get("name"))
            || ObjectUtils.isEmpty(map1.get("user_name"))) {
                continue;
            }
            String name = map1.get("name").toString();
            String userName = map1.get("user_name").toString();
            map.put(name, userName);
        }
        return map;
    }

    private void changeFileDir(File baseFile, String targetDir, Map<String, String> userNameMap, String nameAll) {
        for(File f : baseFile.listFiles()) {
            this.doFileMove(f,targetDir,userNameMap,nameAll);
        }
    }

    private void doFileMove(File baseFile, String targetDir, Map<String, String> userNameMap, String nameAll) {
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
            nameAll += ",'"+actName+"'";
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
                nameAll += ",'"+actName+"'";
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
            List<Record> records = new ArrayList<>();//result.list();
            while(result.hasNext()) {
//                org.neo4j.driver.Record row = result.next();
                records.add(result.next());
            }
            if (!records.isEmpty()) {
                for (Record record : records) {
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

    public void getExcelData(MultipartFile file) {
        try {
            this.readExcel(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<MainDto> readExcel(MultipartFile file) throws IOException {
        List<String> fieldNames = Arrays.asList("Field1", "Field2", "Field3"); // 假设字段名已知
        ExcelDataListener listener = new ExcelDataListener(1,2,fieldNames);

        EasyExcel.read(file.getInputStream(), listener).sheet().doRead();
        List<MainDto> list = listener.getBudgets();
        return list;
    }

    public void doExport(HttpServletResponse response) throws IOException {
        String name = "aaaa";
        String suffix = "xlsx";
        String parentDir = this.getParentDir();
        FileUtils.mkdirs(parentDir);
        String fileName = parentDir+"/"+name+"."+suffix;
        log.info("文件地址："+fileName);

        List<List<Object>> data = new ArrayList<>();
        data.add(Arrays.asList("001", "项目A", 100, 200, 300, 400, 500, 600, 700));
        data.add(Arrays.asList("002", "项目B", 150, 250, 350, 450, 550, 650, 750));

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=xxxx.xlsx");


        try (Workbook finalWorkbook = new XSSFWorkbook();
             ServletOutputStream outputStream = response.getOutputStream()) {

            Sheet sheet = finalWorkbook.createSheet("Sheet1");

            // 创建表头行
            Row row0 = sheet.createRow(0);
            Row row1 = sheet.createRow(1);

            // 设置第一行和第二行的合并单元格及表头值
            row0.createCell(0).setCellValue("编号");
            row0.createCell(1).setCellValue("名称");
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));

            row0.createCell(2).setCellValue("概算费用");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 8));

            String[] subHeaders = {"A", "B", "C", "D", "E", "F", "G"};
            for (int i = 0; i < subHeaders.length; i++) {
                row1.createCell(2 + i).setCellValue(subHeaders[i]);
            }

            // 写入数据（从第三行开始）
            int startRow = 2;
            for (int i = 0; i < data.size(); i++) {
                Row dataRow = sheet.createRow(startRow + i);
                List<Object> rowData = data.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    dataRow.createCell(j).setCellValue(rowData.get(j).toString());
                }
            }

            // 写入文件
            finalWorkbook.write(outputStream);
            outputStream.flush();
//            finalWorkbook.write(finalFos);
//            finalFos.close();
//            finalWorkbook.close();

        }

//        try (Workbook workbook = new XSSFWorkbook();
//             FileOutputStream fos = new FileOutputStream(fileName)) {
//
//            Sheet sheet = workbook.createSheet("Sheet1");
//
//            // 创建表头行
//            Row row0 = sheet.createRow(0);
//            Row row1 = sheet.createRow(1);
//
//            // 设置第一行和第二行的合并单元格及表头值
//            row0.createCell(0).setCellValue("编号");
//            row0.createCell(1).setCellValue("名称");
//            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
//            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
//
//            row0.createCell(2).setCellValue("概算费用");
//            sheet.addMergedRegion(new CellRangeAddress(0, 0, 2, 8));
//
//            String[] subHeaders = {"A", "B", "C", "D", "E", "F", "G"};
//            for (int i = 0; i < subHeaders.length; i++) {
//                row1.createCell(2 + i).setCellValue(subHeaders[i]);
//            }
//
//            // 将工作簿写入文件
//            workbook.write(fos);
//        }
//
//
//        // Step 2: 使用 EasyExcel 写数据（从第三行开始）
//        List<List<Object>> data = new ArrayList<>();
//        data.add(Arrays.asList("001", "项目A", 100, 200, 300, 400, 500, 600, 700));
//        data.add(Arrays.asList("002", "项目B", 150, 250, 350, 450, 550, 650, 750));
//
//        try (FileOutputStream fos = new FileOutputStream(fileName)) {
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        List<BudgetDto> dataList = parseJsonData();
//        List<List<String>> dynamicHead = getDynamicHead();
//        writeExcel(dynamicHead, fileName);
    }

    private static void writeExcel(List<List<String>> head, String fileName) {
        EasyExcel.write(fileName)
                .head(head)
                .sheet("概算编制明细")
                .doWrite(getDataList());
    }

    private static List<List<String>> getDataList() {
        List<List<String>> dataList = new ArrayList<>();

        List<String> row = new ArrayList<>();

//        row.add("编号");
//        row.add("概算名称");
//
//        List<String> initFeeNode = List.of("服务费","设备费","建设费","安装费","xxx1","其他费","合计");

        row.add("1");
        row.add("1");

//                JsonNode initFeeNode = dataItem.get("initFee");
        List<String> initFeeNode = List.of("222","333","444","555","666","777","888");

        row.addAll(initFeeNode);
        dataList.add(row);
        return dataList;
    }

    private List<List<String>> getDynamicHead() {
        List<List<String>> head = new ArrayList<>();
        head.add(List.of("","","概算费用","","","","","",""));
        head.add(List.of("编号",""));
        head.add(List.of("概算名称",""));
        head.add(List.of("编号", "概算名称","服务费","设备费","建设费","安装费","xxx1","其他费","合计"));

        // 假设动态字段名称已经通过某种方式获取，这里简化处理直接从第一个数据项中获取
//        JsonNode dataNode = rootNode.get("data");
//        if (dataNode != null && dataNode.isArray() && !dataNode.isEmpty()) {
//            JsonNode firstDataItem = dataNode.get(0);
//            JsonNode initFeeNode = firstDataItem.get("initFee");
//            if (initFeeNode != null) {
//                initFeeNode.fieldNames().forEachRemaining(fieldName -> {
//                    head.add(List.of(fieldName));
//                });
//            }
//        }
        return head;
    }

    private static List<BudgetDto> parseJsonData() {
        // 这里假设你已经有一个方法来解析JSON数据并转换为List<Map<String, Object>>格式
        // 为了简化示例，我们直接返回一个示例数据列表
        List<BudgetDto> dataList = new ArrayList<>();

        // 示例数据
//        BudgetDto item1 = Map.of(
//                "code", "1",
//                "cbsName", "1",
//                "initFee.服务费", 0,
//                "initFee.设备费", 555,
//                "initFee.建筑费", 0,
//                "initFee.安装费", 0,
//                "initFee.xxx1", 0,
//                "initFee.其它费", 0,
//                "initFee.total", 555
//        );
        Map<String, Object> map = new HashMap<>();
        map.put("code","1");
        map.put("cbsName","1");
        map.put("服务费",0);
        map.put("设备费",555);
        map.put("建筑费",0);
        map.put("安装费",0);
        map.put("xxx1",0);
        map.put("其它费",0);
        map.put("total",555);
        BudgetDto dto = new BudgetDto();
        dto.setCode("1");
        dto.setCbsName("1");
        dto.setFees(map);
        dataList.add(dto);

        // 添加更多数据项...

        return dataList;
    }

    public void jsonTest() {
        String json = "{\n" +
                "    \"_code\": 200,\n" +
                "    \"msg\": \"\",\n" +
                "    \"data\": {\n" +
                "        \"施工\": [\n" +
                "            \"施工\",\n" +
                "            \"施工许可证\"\n" +
                "        ],\n" +
                "        \"项目\": [\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.valueToTree(json);
            // 将 JSON 字符串解析为一个 Map
            Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);

            // 提取 data 对象
            @SuppressWarnings("unchecked")
            Map<String, List<String>> dataMap = (Map<String, List<String>>) jsonMap.get("data");

            // 输出结果
            System.out.println(dataMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLottery() {
        List<String> bigLottoDays = new ArrayList<>(List.of("MONDAY,WEDNESDAY","SATURDAY"));
        List<String> doubleColorBallDays = new ArrayList<>(List.of("TUESDAY","THURSDAY","SUNDAY"));
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 获取当前是星期几
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
        String num = "";
        switch (dayOfWeek) {
            case MONDAY:
                num = this.getBigLotto();
            case TUESDAY:
            case WEDNESDAY:
                num = this.getBigLotto();
            case THURSDAY:
            case FRIDAY:
            case SATURDAY:
                num = this.getBigLotto();
            case SUNDAY:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dayOfWeek);
        }
        log.info("今天是"+dayOfWeek+"建议号码"+num);
    }

    private String getBigLotto() {
        // 生成前区号码(1-35选5个不重复号码)
        List<Integer> frontNumbers = generateRandomNumbers(1, 35, 5);
        // 生成后区号码(1-12选2个不重复号码)
        List<Integer> backNumbers = generateRandomNumbers(1, 12, 2);

        // 格式化输出
        String frontStr = frontNumbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));

        String backStr = backNumbers.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));

        return frontStr + " | " + backStr;
    }

    private static List<Integer> generateRandomNumbers(int min, int max, int count) {
        if (count > (max - min + 1)) {
            throw new IllegalArgumentException("无法生成不重复的随机数，范围太小");
        }

        List<Integer> numbers = IntStream.rangeClosed(min, max)
                .boxed()
                .collect(Collectors.toList());

        // 随机打乱顺序
        Collections.shuffle(numbers, new Random());

        // 取前count个
        return numbers.stream()
                .limit(count)
                .sorted()
                .collect(Collectors.toList());
    }
}

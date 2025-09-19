package com.om.util;

import com.om.module.service.common.CommonService;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ImportExcel12345 {
    protected Logger logger = LoggerFactory.getLogger(ImportExcel12345.class);

    private int A=0;   //EXCEL的列
    private int B=1;
    private int C=2;
    private int D=3;
    private int E=4;
    private int F=5;
    private int G=6;
    private int H=7;
    private int I=8;
    private int J=9;
    private int K=10;
    private int L=11;

    public static  void main(String[] args){
        ImportExcel12345 i = new ImportExcel12345();
        try {
            i.processImportFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 将数据从excel里读取写入到金仓的中间表里
     * @throws Exception
     */
    public void processImportFile()throws Exception{
        String srcfilePath = "E:\\backup\\项目\\张掖网站改版2024\\2024年改造\\12345知识库\\20250530\\";
        String filePath = srcfilePath + "知识列表2025年5月29日（13788条）.xlsx.xlsx";
        String toFile = srcfilePath + "12345.sql";
        BufferedWriter writer = new BufferedWriter(new FileWriter(toFile));

        String txtFile = "E:\\backup\\项目\\张掖网站改版2024\\2024年改造\\12345知识库\\20250530\\12345.txt";
        //BufferedWriter writer2 = new BufferedWriter(new FileWriter(txtFile));






        //这里增加，根据文件的后缀名，调用不同的解析方法，入库
        List<Map> wordList = new ArrayList<Map>();
        Map map = null;
        InputStream in = new FileInputStream(filePath);
        XSSFWorkbook xwb = new XSSFWorkbook(in);
        // 读取第一章表格内容
        XSSFSheet sheet = xwb.getSheetAt(0); //下标从0开始
        XSSFRow row = null; //行
        logger.info("总行数为:"+sheet.getLastRowNum());
        logger.info("总行数2："+sheet.getPhysicalNumberOfRows());
        int startReadLine=2; //开始读取的行
        String txtLine = null;
        BufferedWriter writer2 = null;
        int txtFileLength = (int)(sheet.getLastRowNum()/45);//阿里限制是文档不超过50个，文档大小不超过200K
        int fileNo = 1;//编号从1开始
        File curFile = null;
        int fileSizeLimit = 190*1024;
        for(int i=startReadLine; i<=sheet.getLastRowNum(); i++) {
            if(i==startReadLine){
                File path = new File(srcfilePath + "txt\\");
                if(!path.exists()){
                    path.mkdirs();
                }
                txtFile = srcfilePath + "txt\\12345_"+fileNo+".txt";
                writer2 = new BufferedWriter(new FileWriter(txtFile));
            }
            curFile = new File(txtFile);
            if(curFile.length()>fileSizeLimit){
                writer2.flush();
                fileNo ++;//编号自增
                txtFile = srcfilePath + "txt\\12345_"+fileNo+".txt";
                writer2 = new BufferedWriter(new FileWriter(txtFile));
            }

            row = sheet.getRow(i);

            String word = getCellValue(row, A);//词
            if(word == null || "".equals(word) ||"订单编号".equals(word)){
                continue;
            }
            String title = getCellValue(row, B);//词
            String content = getCellValue(row, C);//词
            String depart = getCellValue(row, D);//词
            String c_type = getCellValue(row, E);//词
            title = title.trim();
            if(content.indexOf("答            ")>-1){
                String[] arr = content.split("答            ");
                content = arr[1].trim();
            }else{
                content = content.trim();
            }
            content = content.replaceAll("'","");
            depart = depart.trim();
            c_type = c_type.trim();

            String sql = "insert into gf_knowlege_12345(id,title,content,depart,c_type)values('"+i+"','"+title+"','"+content+"','"+depart+"','"+c_type+"');";
            txtLine ="问题分类:"+c_type+"\n"+"所属部门:"+depart+"\n"+"问题:"+title+"\n"+"回答:"+content+"\n"+"\n";

            /*
            问题分类:自然资源/水利/河道管理
            所属部门:张掖市
            问题:河道采砂许可证办理条件是什么？
            回答:1.符合防洪法、河道管理条例等有关法律、法规及规章、文件的规定；2.符合防洪要求，保证洪水畅通及堤防、河道工程的安全；3.符合河道采砂规划及年度开采计划；4.符合航道及航道工程的安全要求。5.符合流域综合治理规划和有关区域发展规划。6.县区界河河道由市水务局审批。
             */

            writer.write(sql);
            writer.newLine(); // 可选，如果需要在每行的末尾添加换行符
            logger.info(""+sql);

            writer2.write(txtLine);
            writer2.flush();

        }
        writer.flush();
        writer2.flush();


    }


    public static String getCellValue(XSSFRow row,int pos){
        XSSFCell cell = row.getCell(pos);
        if(cell == null){
            return null;
        }else{
            String s = null;
            try {
                s =  String.valueOf(cell.getStringCellValue());
                return s;
            }catch (Exception e){      }

            try {
                s = String.valueOf(cell.getNumericCellValue());
                return s;
            }catch (Exception e){      }

            try {
                s = String.valueOf(cell.getBooleanCellValue());
                return s;
            }catch (Exception e){      }

            try {
                s = String.valueOf(cell.getDateCellValue());
                return s;
            }catch (Exception e){      }

            return  s;
        }
    }
}

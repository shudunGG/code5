package com.om.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxtFileUtils{

    private static Logger logger = LoggerFactory.getLogger(TxtFileUtils.class);

    public static void main(String[] args){

    }


    /**
     * 读取参数中指定的纯文本文件，并将结果存放在StringBuffer中返回
     * @param txtFilePath
     * @return
     */
    public static StringBuffer readTxtToString(String txtFilePath) {
        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(txtFilePath)), "UTF-8"));
            String lineTxt = null;
            // 逐行读取
            while ((lineTxt = br.readLine()) != null) {
                sb.append(lineTxt).append("\n");
            }
            br.close();
        } catch (Exception e) {
            logger.error("Error Message :", e);
        }
        return sb;
    }

    /**
     * 未验证
     * 逐行读取TXT文件并打印到控制台
     *
     */
    public void readTxt() {
        try {
            String path = "C:\\Test\\test.txt";
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "UTF-8"));
            String lineTxt = null;
            int count = 0;
            // 逐行读取
            while ((lineTxt = br.readLine()) != null) {
                // 输出内容到控制台
                logger.info(lineTxt);
                count++;
            }
            br.close();
            logger.info("count=" + count);
        } catch (Exception e) {
            logger.error("Error Message :", e);
        }
    }

    /**
     * 方法1写入txt
     *
     * @param message
     * @throws IOException
     */
    public static void writeStrtoTxtFile(String folder, String filename, String message) throws IOException {

        File files = new File(folder);
        files.mkdirs();

        File file = new File(folder + File.separator + filename);
        file.createNewFile();
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(message);
        out.flush();

    }

    /**
     * 方法2写入txt
     *
     * @param file
     * @param messageList
     * @throws FileNotFoundException
     */
    public void writeToTxt(String folder, String file, List<String> messageList) throws FileNotFoundException {

        File files = new File(folder);
        files.mkdirs();

        FileOutputStream fos = new FileOutputStream(folder + File.separator + file);

        // 逐行写入
        PrintWriter pw = new PrintWriter(fos);
        for (String message : messageList) {
            pw.println(message);
        }
        pw.close();

    }

    /**
     * 测试方法2写入txt
     */
    public void writeToTxt() {

        String folder = "C:\\Test0";
        String filename = "000.txt";

        String message = "this is message \n";
        for (int i = 0; i < 3; i++) {
            message += "abc \n";
        }
        try {
            writeStrtoTxtFile(folder, filename, message);
        } catch (IOException e) {
            logger.error("Error Message :", e);
        }

    }

    /**
     * 测试方法2写入txt
     */
    public void testWriteToTxt() {
        String folder = "C:\\Test";
        String file = "test.txt";
        List<String> messageList = new ArrayList<>();
        messageList.add("ok,0");
        messageList.add("no,1");
        messageList.add("pass,2");
        messageList.add("okay,3");

        try {
            writeToTxt(folder, file, messageList);
        } catch (FileNotFoundException e) {
            logger.error("Error Message :", e);
        }

    }

}
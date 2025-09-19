package com.om.test;

import com.om.util.HttpInterface;
import com.wxtool.ChinaCipher;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

public class TestMain {
    public static void main(String[] args) {
        StringBuffer sb = new StringBuffer();
        try {
            for(int i=0;i<50;i++) {
                try {
                    //String s = HttpInterface.sendGet("http://36.142.241.72:8090/zyggzyw/");
                    String s = HttpInterface.sendGet("https://www.zhangye.gov.cn/zyggzyw/");
                    System.out.println("第【"+i+"】次执行成功！！");
                    sb.append("第【"+i+"】次执行成功！！").append("\n");
                } catch (Exception e) {
                    System.out.println("第【"+i+"】次执行失败！！"+e.getMessage());
                    sb.append("第【"+i+"】次执行失败！！"+e.getMessage()).append("\n");
                }
                Thread.sleep(300);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());


        System.out.println("完成！！");
    }


    /**
     * 删除所有文件,file的路径必须以/结束
     * @param file
     */
    public static void delDir(File file) {
        if (file.isDirectory()) {
            File zFiles[] = file.listFiles();
            for (File file2 : zFiles) {
                delDir(file2);
            }
            file.delete();
        } else {
            file.delete();
        }
    }








}

package com.om.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UZipFile
{
    public static Logger logger = LoggerFactory.getLogger(UZipFile.class);
    /**
     * 将参数1路径的ZIP文件解压到指定目录
     */
    public static void unZipFiles(String zipPath,String descDir)throws IOException
    {
        unZipFiles(new File(zipPath), descDir);
    }
    /**
     * 将参数1的ZIP文件解压文件到指定目录
     */
    public static List<String> unZipFiles(File zipFile,String descDir)throws IOException
    {
        List<String> fileList = new ArrayList<String>();
        File pathFile = new File(descDir);
        if(!pathFile.exists())
        {
            pathFile.mkdirs();
        }
        //解决zip文件中有中文目录或者中文文件
        ZipFile zip = new ZipFile(zipFile, Charset.forName("gbk"));
        for(Enumeration entries = zip.entries(); entries.hasMoreElements();)
        {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            String zipEntryName = entry.getName();

            InputStream in = zip.getInputStream(entry);
            String outPath = (descDir+zipEntryName).replaceAll("\\*", "/");;
            //判断路径是否存在,不存在则创建文件路径
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            if(!file.exists())
            {
                file.mkdirs();
            }
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if(new File(outPath).isDirectory())
            {
                continue;
            }
            fileList.add(zipEntryName);
            //输出文件路径信息
            logger.info(outPath);
            OutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while((len=in.read(buf1))>0)
            {
                out.write(buf1,0,len);
            }
            in.close();
            out.close();
        }
        logger.info("******************解压完毕********************");
        return fileList;
    }

    /**
     * 将参数1中的文件列表，根据参数2做后缀名过滤，并返回
     * @param fileList
     * @param fix
     * @return
     */
    public static List<String> filterFile(List<String> fileList ,String fix){
        List<String> rsList  = new ArrayList<String>();
        for(String s:fileList){
            if(s.toLowerCase().endsWith(fix)){
                rsList.add(s);
            }
        }
        return rsList;
    }

    /**
     * 将参数1中的文件列表，排除参数2的后缀名，并返回其它文件
     * @param fileList
     * @param fix
     * @return
     */
    public static List<String> filterOtherFile(List<String> fileList ,String fix){
        List<String> rsList  = new ArrayList<String>();
        for(String s:fileList){
            if(!s.toLowerCase().endsWith(fix)){
                rsList.add(s);
            }
        }
        return rsList;
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

    /***
     *  检查解压后的文件是否存在
     * @param zipFile
     * @param descDir
     * @return 返回已经存在的文件列表
     * @throws IOException
     */
    public static List<String> checkFileExist(File zipFile,String descDir)throws IOException
    {
        List<String> list = null;
        File pathFile = new File(descDir);
        if(!pathFile.exists())
        {
            return list;
        }
        list = new ArrayList<String>();
        //解决zip文件中有中文目录或者中文文件
        ZipFile zip = new ZipFile(zipFile, Charset.forName("gbk"));
        for(Enumeration entries = zip.entries(); entries.hasMoreElements();)
        {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            String zipEntryName = entry.getName();
            String outPath = (descDir+zipEntryName).replaceAll("\\*", "/");;
            //判断路径是否存在,不存在则创建文件路径
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            if(!file.exists())
            {
                file.mkdirs();
            }
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if(new File(outPath).isDirectory())
            {
                continue;
            }
            //输出文件路径信息
            logger.info(outPath);
            File eFile = new File(outPath);
            if(eFile.exists()){
                list.add(zipEntryName);
            }
        }

        return  list;
    }
    public static void main(String[] args) throws IOException {
        /**
         * 解压文件
         */
        File zipFile = new File("d:/temp/temp.zip");
        String path = "d:/temp/";
        //unZipFiles(zipFile, path);
        List<String> list = checkFileExist(zipFile, path);
        for(String s:list){
            System.out.println(s);
        }
    }
}
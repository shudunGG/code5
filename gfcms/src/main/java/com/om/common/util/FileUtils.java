package com.om.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FileUtils {
    /**
     * 根据文件路径实现文件复制
     *
     * @param sourceFilePath
     * @param targetFilePath
     * @throws IOException
     */
    public static void copyFile(String sourceFilePath, String targetFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File targetFile = new File(targetFilePath);
        copyFile(sourceFile,targetFile);
    }

    public static void copyFile(File sourceFile,File targetFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(sourceFile);
        FileOutputStream outputStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[4096];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    public static void listFiles(List<File> listFile , File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if(".".equals(file.getName()) || "..".equals(file.getName())){
                    continue;
                }
                if (file.isFile()) {
                    listFile.add(file);
                } else if (file.isDirectory()) {
                    listFiles(listFile,file); // 递归调用，遍历子目录
                }
            }
        }
    }

    //测试方法
    public static void main(String[] args) throws IOException {
        String sourcePath = "D:/Test/demo/aaa.txt";
        String targetPath = "D:/Test/copy_aaa.txt";
        copyFile(sourcePath, targetPath);
    }

}

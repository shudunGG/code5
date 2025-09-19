package com.om.common.util;
import java.io.*;
import java.util.zip.*;

public class FileCompressor {

    public static void compressFile(File inputFile, File outputFile) {
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ZipEntry entry = new ZipEntry(inputFile.getName());
            zos.putNextEntry(entry);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            zos.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        File inputFile = new File("input.txt");
        File outputFile = new File("output.zip");
        compressFile(inputFile, outputFile);
    }
}
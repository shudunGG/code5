package com.om.test;

import com.om.bo.base.FtpInfo;
import com.om.common.util.ExtractUtils;
import com.om.common.util.SFTPUtil2;

import java.io.File;




public class TestFtp {

    public static  void main(String[] args){
//        FtpInfo ftp = new FtpInfo("yx","123456","192.168.1.8",22);
//        TestFtp t = new TestFtp();
//        t.testuploadfile(ftp,"d:/wcmym2.zip","/","wcmym2.zip");
    }

    //测试文件上传
    public void testuploadfile(FtpInfo ftp,String uploadFilePath,String cdDir,String zipFileName) {
        SFTPUtil2 sftpUtil2 = new SFTPUtil2(ftp);
        File file = new File(uploadFilePath);
        long startTime = System.currentTimeMillis();//获取当前时间
        Boolean uploadFile = sftpUtil2.uploadFile(cdDir, file);
        Boolean remoteZipToFile = false;
        if (uploadFile) {
            System.out.println("上传成功，开始解压");
            remoteZipToFile = ExtractUtils.remoteZipToFile(cdDir,zipFileName,ftp);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间："+(endTime-startTime)+"ms");
        System.out.println(remoteZipToFile);
    }


    //测试文件下载
    public void testdownloadfile(FtpInfo ftp) {
        SFTPUtil2 sftpUtil2 = new SFTPUtil2(ftp);
        long startTime = System.currentTimeMillis();//获取当前时间
        Boolean download = sftpUtil2.download("zshuaipath","linux-4.20.tar.gz", "D://1");
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间："+(endTime-startTime)+"ms");
        System.out.println(download);
    }


    //测试文件删除
    public void testdeletefile(FtpInfo ftp) {
        SFTPUtil2 sftpUtil2 = new SFTPUtil2(ftp);
        long startTime = System.currentTimeMillis();//获取当前时间
        Boolean delete = sftpUtil2.delete("zshuaipath","linux-4.20.tar.gz");
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间："+(endTime-startTime)+"ms");
        System.out.println(delete);
    }

    //测试文件夹的创建
    public void testmkdir(FtpInfo ftp) {
        SFTPUtil2 sftpUtil2 = new SFTPUtil2(ftp);
        long startTime = System.currentTimeMillis();//获取当前时间
        Boolean mkdir = sftpUtil2.mkdir("/opt/aaa/","zshuaipath");
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间："+(endTime-startTime)+"ms");
        System.out.println(mkdir);
    }


    //测试指定目录下的指定文件的解压缩
    public void testExtract(FtpInfo ftp) {
        SFTPUtil2 sftpUtil2 = new SFTPUtil2(ftp);
        long startTime = System.currentTimeMillis();//获取当前时间
        Boolean remoteZipToFile = ExtractUtils.remoteZipToFile("","linux-4.20.tar.gz",ftp);
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间："+(endTime-startTime)+"ms");
        System.out.println(remoteZipToFile);
    }

}
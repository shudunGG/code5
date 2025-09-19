package com.om.common.util;

import com.jcraft.jsch.*;
import com.om.bo.base.FtpInfo;
import com.om.bo.base.FtpPathInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

/**
 * @version: 1.0
 * @Description:文件上传/删除/解压缩
 * @author: liuxinjia
 * @date: 2021年4月9日
 * @source http://www.manongjc.com/article/98387.html
 */
public class SFTPUtil2 {

    private static final Logger logger = LoggerFactory.getLogger(SFTPUtil2.class);
    private FtpInfo ftp;

    public SFTPUtil2(FtpInfo ftp) {
        this.ftp = ftp;
    }


    /*
     * @Description:获取连接信息，返回session，在session中获取安全通道
     * @param host：连接主机ip
     * @param port:端口号，一般sftp依托于ssh。端口号22
     * @param username：用户名
     * @param password：密码
     * @return
     */
    public  Session getSession() {
        Session session = null;
        try {
            JSch jsch = new JSch();
            jsch.getSession(ftp.getUsername(), ftp.getIp(), ftp.getPort());
            session = jsch.getSession(ftp.getUsername(), ftp.getIp(), ftp.getPort());
            session.setPassword(ftp.getPassword());
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect();
            logger.info("Session connected!");
        } catch (JSchException e) {
            logger.info("get Channel failed!", e);
        }
        return session;
    }

    /*
     * @Description: 获取文件上传的安全通道
     * @param session
     * @return
     */
    public  Channel getChannel(Session session) {
        Channel channel = null;
        try {
            channel = session.openChannel("sftp");
            channel.connect();
            logger.info("获取连接成功 get Channel success!");
        } catch (JSchException e) {
            logger.info("get Channel fail!", e);
        }
        return channel;
    }



    /*
     * @Description:创建文件夹
     * @param cdDir 先进行到cdDir目录下，再创建dir
     * @param dir : 创建的文件夹名字
     */
    public  Boolean mkdir(String cdDir,String dir) {
        Session s = getSession();
        Channel channel = getChannel(s);
        ChannelSftp sftp = (ChannelSftp) channel;
        Boolean result = false;
        try {
            sftp.cd(cdDir);//相当于在linux命令行执行cd / ，然后在打开的目录下创建
            sftp.mkdir(dir);
            logger.info("创建文件夹成功！create dir success");
            result = true;
        } catch (SftpException e) {
            logger.info("创建文件夹失败！create dir fail");
            result =false;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 递归根据路径创建文件夹
     *
     * @param dirs     根据 / 分隔后的数组文件夹名称
     * @param tempPath 拼接路径
     * @param length   文件夹的格式
     * @param index    数组下标
     * @return
     */
    public void mkdirDir(ChannelSftp sftp,String[] dirs, String tempPath, int length, int index) {
        // 以"/a/b/c/d"为例按"/"分隔后,第0位是"";顾下标从1开始
        index++;
        if (index < length) {
            // 目录不存在，则创建文件夹
            tempPath += "/" + dirs[index];
        }
        try {
            logger.info("检测目录[" + tempPath + "]");
            sftp.cd(tempPath);
            if (index < length) {
                mkdirDir(sftp,dirs, tempPath, length, index);
            }
        } catch (SftpException ex) {
            logger.warn("创建目录[" + tempPath + "]");
            try {
                sftp.mkdir(tempPath);
                sftp.cd(tempPath);
            } catch (SftpException e) {
                e.printStackTrace();
                logger.error("创建目录[" + tempPath + "]失败,异常信息[" + e.getMessage() + "]");

            }
            logger.info("进入目录[" + tempPath + "]");
            mkdirDir(sftp,dirs, tempPath, length, index);
        }
    }


    /*
     * @Description: 文件上传的方法
     * @param sftp : 客户端
     * @param dir : 指定上传文件的目录
     * @param file : 上传的文件
     * @return :
     */
    public Boolean uploadFile(String dir, File file) {
        Session s = getSession();
        Channel channel = getChannel(s);
        ChannelSftp sftp = (ChannelSftp) channel;
        Boolean result =false;
        try {

            try {
                sftp.cd(dir);
            } catch (SftpException e) {
                // 目录不存在，则创建文件夹
                String[] dirs = dir.split("/");
                String tempPath = "";
                int index = 0;
                mkdirDir(sftp,dirs, tempPath, dirs.length, index);
                sftp.cd(dir);
            }

            logger.info("打开目录");
            if (file != null) {
                sftp.put(new FileInputStream(file), file.getName());
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            logger.info("上传失败！", e);
            result = false;
        }
        closeAll(sftp, channel, s); // 关闭连接
        return result;
    }


    public Boolean uploadFile(List<FtpPathInfo> listFile,FtpInfo ftp) {
        Session s = getSession();
        Channel channel = getChannel(s);
        ChannelSftp sftp = (ChannelSftp) channel;
        Boolean result =false;
        try {
            for(FtpPathInfo ftpPathInfo:listFile){
                String dir = ftp.getSftpRoot()+"/"+ftpPathInfo.getFtpPath();
                File file = ftpPathInfo.getLocalfile();
                try {
                    sftp.cd(dir);
                } catch (SftpException e) {
                    // 目录不存在，则创建文件夹
                    String[] dirs = dir.split("/");
                    String tempPath = "";
                    int index = 0;
                    mkdirDir(sftp,dirs, tempPath, dirs.length, index);
                    sftp.cd(dir);
                }

                logger.info("打开目录");
                if (file != null) {
                    sftp.put(new FileInputStream(file), file.getName());
                    result = true;
                } else {
                    result = false;
                }
            }
        } catch (Exception e) {
            logger.info("上传失败！", e);
            result = false;
        }
        closeAll(sftp, channel, s); // 关闭连接
        return result;
    }

    public Boolean delteFile(List<FtpPathInfo> listFile,FtpInfo ftp) {
        Session s = getSession();
        Channel channel = getChannel(s);
        ChannelSftp sftp = (ChannelSftp) channel;
        Boolean result =false;
        try {
            for(FtpPathInfo ftpPathInfo:listFile){
                String dir = ftp.getSftpRoot()+"/"+ftpPathInfo.getFtpPath();
                File file = ftpPathInfo.getLocalfile();
                try {
                    sftp.cd(dir);
                } catch (SftpException e) {
                    // 目录不存在，则创建文件夹
                    String[] dirs = dir.split("/");
                    String tempPath = "";
                    int index = 0;
                    mkdirDir(sftp,dirs, tempPath, dirs.length, index);
                    sftp.cd(dir);
                }

                logger.info("打开目录");
                if (file != null) {
                    sftp.rm(file.getName());
                    result = true;
                } else {
                    result = false;
                }
            }
        } catch (Exception e) {
            logger.info("删除失败！", e);
            result = false;
        }
        closeAll(sftp, channel, s); // 关闭连接
        return result;
    }

    /**
     * @Description: 文件下载
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @param saveFile     存在本地的路径

     */
    public   Boolean download(String directory, String downloadFile, String saveFile) {
        Session s = getSession();
        Channel channel = getChannel(s);
        ChannelSftp sftp = (ChannelSftp) channel;
        Boolean result =false;
        try {
            sftp.cd("/"+directory);
            sftp.get(downloadFile, saveFile);

            result = true;
        } catch (Exception e) {
            result = false;
            logger.info("下载失败！", e);

        }
        return result;
    }
    /**
     * @Description: 文件删除
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件

     */
    public   Boolean delete(String directory, String deleteFile ) {
        Session s = getSession();
        Channel channel = getChannel(s);
        ChannelSftp sftp = (ChannelSftp) channel;
        Boolean result = false;
        try {
            sftp.cd("/"+directory);
            sftp.rm(deleteFile);
            result = true;
        } catch (Exception e) {
            result = false;
            logger.info("删除失败！", e);
        }
        return result;
    }

    private  void closeChannel(Channel channel) {
        if (channel != null) {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    private  void closeSession(Session session) {
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public  void closeAll(ChannelSftp sftp, Channel channel, Session session) {
        try {
            closeChannel(sftp);
            closeChannel(channel);
            closeSession(session);
        } catch (Exception e) {
            logger.info("closeAll", e);
        }
    }


    public static  void main(String[] args){
//        FtpInfo ftp = new FtpInfo("yx","123456","192.168.1.8",22);
//        SFTPUtil2 t = new SFTPUtil2(ftp);
//        File f = new File("d:/nginx.conf");
//        boolean rs = t.uploadFile("/11/22",f);
//        System.out.println(rs);
    }
}
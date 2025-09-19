package com.om.bo.base;

import java.io.Serializable;

public class FtpInfo implements Serializable {

    private String username;
    private String password;
    private String ip;
    private int port;
    private String sftpRoot;

    /**
     * 这两个字段是为了部署用的，虽然与ftp没关系，在业务场景里是一起使用的
     */
    private String deployMode;
    private String targetRoot;

    private String rootPath;
    private String appRootPath;


    public FtpInfo(String username, String password, String ip, int port,String sftpRoot) {
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
        this.sftpRoot = sftpRoot;
    }

    public String getSftpRoot() {
        if(sftpRoot==null || "/".equals(sftpRoot)){
            sftpRoot="";
        }
        return sftpRoot;
    }

    public void setSftpRoot(String sftpRoot) {
        this.sftpRoot = sftpRoot;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public String getTargetRoot() {
        return targetRoot;
    }

    public void setTargetRoot(String targetRoot) {
        this.targetRoot = targetRoot;
    }

    public String getUsername() {
        return username;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        if(rootPath.endsWith("/")){
            this.rootPath = rootPath;
        }else{
            this.rootPath = rootPath+"/";
        }

    }

    public String getAppRootPath() {
        return appRootPath;
    }

    public void setAppRootPath(String appRootPath) {
        if(appRootPath.endsWith("/")){
            this.appRootPath = appRootPath;
        }else{
            this.appRootPath = appRootPath+"/";
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

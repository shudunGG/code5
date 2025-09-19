package com.om.bo.base;


import java.io.File;


public class FtpPathInfo  {



    private String ftpPath;
    private File localfile;
    private File targetfile;

    public FtpPathInfo(String ftpPath, File localfile, File targetfile) {
        this.ftpPath = ftpPath;
        this.localfile = localfile;
        this.targetfile = targetfile;
    }

    public String getFtpPath() {
        return ftpPath;
    }

    public void setFtpPath(String ftpPath) {
        this.ftpPath = ftpPath;
    }

    public File getLocalfile() {
        return localfile;
    }

    public void setLocalfile(File localfile) {
        this.localfile = localfile;
    }

    public File getTargetfile() {
        return targetfile;
    }

    public void setTargetfile(File targetfile) {
        this.targetfile = targetfile;
    }
}

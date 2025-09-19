package com.om.bo.busi;

import com.om.common.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class MyResFile {
    private String name;
    private int isDir;
    private long fileSizeLong;
    private String fileSizeStr;
    private long modifyTimeLong;
    private String modifyTimeStr;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsDir() {
        return isDir;
    }

    public void setIsDir(boolean isDir) {
        if(isDir){
            this.isDir = 1;
        }else{
            this.isDir = 0;
        }
    }

    public long getFileSizeLong() {
        return fileSizeLong;
    }

    public void setFileSizeLong(long fileSizeLong) {
        String info = null;
        if(fileSizeLong>=1024){
            long k = Math.round(fileSizeLong/1024);
            if(k >= 1024){
                long m = Math.round(k/1024);
                if(m >= 1024){
                    long g = Math.round(m/1024);
                    info=g+"G";
                }else{
                    info=m+"M";
                }
            }else{
                info=k+"K";
            }
        }else{
            info=fileSizeLong+"字节";
        }
        this.fileSizeStr = info;
        this.fileSizeLong = fileSizeLong;
    }

    public String getFileSizeStr() {
        return fileSizeStr;
    }

    public long getModifyTimeLong() {
        return modifyTimeLong;
    }

    public void setModifyTimeLong(long modifyTimeLong) {
        this.modifyTimeStr = DateUtil.getTimeStrByLong(modifyTimeLong);
        this.modifyTimeLong = modifyTimeLong;
    }

    public String getModifyTimeStr() {
        return modifyTimeStr;
    }

}

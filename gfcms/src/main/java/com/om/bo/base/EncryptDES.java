package com.om.bo.base;

import lombok.val;

public class EncryptDES{
    public int value=1;
    public String desc=null;
    public int size=56;
    public static final String encryptKey="91620103MA71XXLG59/LXJMAKE";


    public EncryptDES(int v,String d,int s){
        this.value = v;
        this.desc = d;
        this.size = s;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

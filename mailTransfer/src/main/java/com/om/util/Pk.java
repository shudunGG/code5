package com.om.util;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.UUID;

public class Pk {
    private static Format f1=new DecimalFormat("000");
    public static String getId(String pre){
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        if(pre!=null){
            return pre+uuid;
        }else{
            return uuid;
        }
    }

    public static String getCode(int id){
        String  code =f1.format(id);
        return code;
    }

    public static int getSeq(int id){
        int seq = id*8;
        return seq;
    }
}

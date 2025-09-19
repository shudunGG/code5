package com.om.module.core.exception;


/** 
 * 异常错误枚举
 **/
public enum ExceptionEnum {
    ERROR("-1","对不起，系统忙"),
    //SUCCESS("0","交易成功"),
;

    private String code;

    private String msg;

    ExceptionEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

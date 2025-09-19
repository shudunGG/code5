package com.om.common.util.wx;

public class CustomerText {
    private String touser;
    private String msgtype;
    private TextMessage text;
    public String getTouser() {
        return touser;
    }
    public void setTouser(String touser) {
        this.touser = touser;
    }
    public String getMsgtype() {
        return msgtype;
    }
    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }
    public TextMessage getText() {
        return text;
    }
    public void setText(TextMessage text) {
        this.text = text;
    }

}
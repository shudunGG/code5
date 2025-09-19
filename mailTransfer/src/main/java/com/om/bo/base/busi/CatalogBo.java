package com.om.bo.base.busi;

public class CatalogBo {
    private String catalogPk;
    private String catalogId;
    private String catalogCode;
    private String categoryPk;
    private String createUser;
    private int showSeq;
    private String resName;;   //信息资源名称
    private String resShortNam;;   //信息资源简称
    private String keyWords;   //关键字及摘要
    private String columnName;   //信息项名称
    private String columnDataType;   //数据类型
    private int columnDataLength;   //数据长度
    private String shareType;   //共享类型
    private int shareTypeVal;   //共享类型
    private String shareCond;   //共享条件
    private String shareWay;   //共享方式分类
    private String shareWayClass;   //共享方式类型
    private String isOpen;   //是否向社会开放
    private int isOpenVal;
    private String openCond;   //开放条件
    private String updateCircleName;   //更新周期
    private int updateCircleVal;//更新周期
    private String sourceSystem;   //来源系统信息

    private String itemPk;
    private String itemId;
    private String itemCode;

    public String getItemPk() {
        return itemPk;
    }

    public void setItemPk(String itemPk) {
        this.itemPk = itemPk;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getCategoryPk() {
        return categoryPk;
    }

    public int getShowSeq() {
        return showSeq;
    }

    public void setShowSeq(int showSeq) {
        this.showSeq = showSeq;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public void setCategoryPk(String categoryPk) {
        this.categoryPk = categoryPk;
    }

    public void setCatalogPk(String catalogPk) {
        this.catalogPk = catalogPk;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public void setResShortNam(String resShortNam) {
        this.resShortNam = resShortNam;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setColumnDataType(String columnDataType) {
        String s = columnDataType.trim().toUpperCase();
        //字符型 C、数值型 N、货币型 Y、日期型 D、日期时间型 T、
        //逻辑型 L、备注型 M、通用型 G、双精度型 B、整型 I、浮点
        //型 F 等。
        if(s.indexOf(".")>0){
            s.substring(0,s.indexOf("."));
        }
        this.columnDataType = s;
        if("D".equals(s)){
            this.columnDataLength = 8;
        }else if("T".equals(s)){
            this.columnDataLength = 19;
        }else if("L".equals(s)){
            this.columnDataLength = 1;
        }
    }

    public void setColumnDataLength(int columnDataLength) {
        this.columnDataLength = columnDataLength;
    }

    public void setShareType(String shareType) {
        this.shareType = shareType;
        if(shareType.indexOf("有条件")>-1){
            this.shareTypeVal=2;
        }else if(shareType.indexOf("无条件")>-1){
            this.shareTypeVal=1;
        }else if(shareType.indexOf("不")>-1){
            this.shareTypeVal=3;
        }
    }
    public void setShareTypeVal(int shareTypeVal){
        if(shareTypeVal==1){
            this.shareType="无条件共享";//1：无条件共享 2：有条件共享 3不予共享
        }else if(shareTypeVal==2){
            this.shareType="有条件共享";//1：无条件共享 2：有条件共享 3不予共享
        }else if(shareTypeVal==3){
            this.shareType="不予共享";//1：无条件共享 2：有条件共享 3不予共享
        }
    }

    public void setShareCond(String shareCond) {
        this.shareCond = shareCond;
    }

    public void setShareWay(String shareWay) {
        this.shareWay = shareWay;
    }


    public void setShareWayClass(String shareWayClass) {
        this.shareWayClass = shareWayClass;
    }

    public void setIsOpen(String isOpen) {
        this.isOpen = isOpen;//1可开放，0不可开放
        if(isOpen.indexOf("不")>-1){
            this.isOpenVal=0;
        }else{
            this.isOpenVal=1;
        }
    }

    public void setIsOpenVal(int isOpenVal) {
        this.isOpenVal = isOpenVal;
        if(isOpenVal==1){
            this.isOpen="可开放";
        }else{
            this.isOpen="不可开放";
        }
    }

    public void setOpenCond(String openCond) {
        this.openCond = openCond;
    }

    public void setUpdateCircleName(String updateCircleName) {
        this.updateCircleName = updateCircleName;
        if(updateCircleName.indexOf("实时")>-1){
            this.updateCircleVal = 0;
        }else if(updateCircleName.indexOf("天")>-1){
            this.updateCircleVal = 1;
        }else if(updateCircleName.indexOf("周")>-1 || updateCircleName.indexOf("星期")>-1){
            this.updateCircleVal = 7;
        }else if(updateCircleName.indexOf("月")>-1){
            this.updateCircleVal = 30;
        }else if(updateCircleName.indexOf("季")>-1){
            this.updateCircleVal = 90;
        }else if(updateCircleName.indexOf("半年")>-1){
            this.updateCircleVal = 180;
        }else if(updateCircleName.indexOf("年")>-1){
            this.updateCircleVal = 365;
        }
    }

    public void setUpdateCircleVal(int updateCircleVal) {
        this.updateCircleVal = updateCircleVal;
        if(updateCircleVal == 0 ){
            this.updateCircleName="实时";
        }else if(updateCircleVal == 1 ){
            this.updateCircleName="每天";
        }else if(updateCircleVal == 7 ){
            this.updateCircleName="每周";
        }else if(updateCircleVal == 30 ){
            this.updateCircleName="每月";
        }else if(updateCircleVal == 90 ){
            this.updateCircleName="每季";
        }else if(updateCircleVal == 180 ){
            this.updateCircleName="每半年";
        }else if(updateCircleVal == 365 ){
            this.updateCircleName="每年";
        }
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getCatalogPk() {
        return catalogPk;
    }


    public String getResName() {
        return resName;
    }

    public String getResShortNam() {
        return resShortNam;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public int getColumnDataLength() {
        return columnDataLength;
    }

    public String getShareType() {
        return shareType;
    }

    public String getShareCond() {
        return shareCond;
    }

    public String getShareWay() {
        return shareWay;
    }

    public String getShareWayClass() {
        return shareWayClass;
    }

    public String getIsOpen() {
        return isOpen;
    }

    public String getOpenCond() {
        return openCond;
    }

    public String getUpdateCircleName() {
        return updateCircleName;
    }

    public int getUpdateCircleVal() {
        return updateCircleVal;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public int getShareTypeVal() {
        return this.shareTypeVal;
    }

    public int getIsOpenVal() {
        return isOpenVal;
    }

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public String getCatalogCode() {
        return catalogCode;
    }

    public void setCatalogCode(String catalogCode) {
        this.catalogCode = catalogCode;
    }
}

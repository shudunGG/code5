package com.om.config.mybatis.core;

/**
 * Created by Administrator on 2018/12/16.
 * 没有添加前缀的默认都是Oracle数据库,其他数据库变量名称以数据库类型_实例名称的方式命名,例如MYSQL_MASTER
 */
public class DataSourcesName {
    /**
     * 默认数据库
     */
    public static final String DEFAULTDB="defaultdb";
    public static final String UNIONKT="unionkt";
    public static final String CRMYYDBINS1="crmyydbins1";
    public static final String CRMPBDBPROD="crmpbdbprod";
    public static final String CZXXDBOB60="czxxdbob60";

    public static final String CZXXDBOMA="czxxdboma";
    public static final String CRMYYDBOMA="crmyydboma";
    public static final String CRMPBDBOMA="crmpbdboma";
    public static final String UNIONOMA="unionoma";
}

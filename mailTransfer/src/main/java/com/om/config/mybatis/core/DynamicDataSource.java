package com.om.config.mybatis.core;

import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 继承spring重写数据源的获取方法
 * @author Administrator
 *
 */
public class DynamicDataSource extends AbstractRoutingDataSource{
	private final static Logger logger = LoggerFactory.getLogger(DynamicDataSource.class);
	private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
	
	/**
	 * 注入数据源
	 * @param defaultTargetDataSource
	 * @param targetDataSources
	 */
	public DynamicDataSource(DataSource defaultTargetDataSource, Map<String, DataSource> targetDataSources) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(new HashMap<>(targetDataSources));
        super.afterPropertiesSet();
        //需要添加设置默认数据源
        if(getDataSource() == null) {
        	setDataSource(DataSourcesName.DEFAULTDB);
        }
        logger.info("=========数据源注入完成,默认的数据源为:"+getDataSource()+"=========");
    }
	/**
	 * Spring 获取手动设置的数据源
	 */
	@Override
	protected Object determineCurrentLookupKey() {
		// TODO Auto-generated method stub
		logger.info("=========获取到的数据源是:"+getDataSource()+"=========");
		return getDataSource();
	}
	
	/**
     * @param dataSourceType 数据库类型
     * @return void
     * @throws
     * @Description: 设置数据源类型
     */
    public static void setDataSource(String dataSourceType) {
    	logger.info("=========设置的数据源key为:"+dataSourceType+"=========");
        contextHolder.set(dataSourceType);
    }

    /**
     * @param
     * @return String
     * @throws
     * @Description: 获取数据源类型,如果当前数据源类型为空,则设置为默认数据源
     */
    public static String getDataSource() {
    	if(contextHolder.get() == null) {
    		setDataSource(DataSourcesName.DEFAULTDB);
    	}
        return contextHolder.get();
    }

    /**
     * @param
     * @return void
     * @throws
     * @Description: 清除数据源类型
     */
    public static void clearDataSource() {
        contextHolder.remove();
    }
    
    public static void reset(){
    	contextHolder.set(DataSourcesName.DEFAULTDB);
    }

}

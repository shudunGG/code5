package com.om.config.mybatis.core;


import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

/**
 * MyBatis核心配置类,如果要使用多数据源需要在此类里面进行改造处理
 * @author Administrator
 *
 */
//@Configuration //多数据源配置的时候需要使用
public class MybatisConfiguration implements TransactionManagementConfigurer{

	@Autowired
	private DataSource datasource; //可能还需要datasource的连接信息
	//SQL文件的目录位置 classpath:com/om/module/db/mapping/**/*.xml
	static final String MAPPER_LOCATION = "classpath:com/om/module/dao/db/**/*.xml";
    @Bean  //用来提供SqlSession
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(datasource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(MybatisConfiguration.MAPPER_LOCATION));;
        return sessionFactory.getObject();
    }
    
	@Bean 
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new DataSourceTransactionManager(datasource);
	}

}

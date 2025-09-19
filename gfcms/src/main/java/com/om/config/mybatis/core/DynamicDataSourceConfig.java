package com.om.config.mybatis.core;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;

/**
 * 多数据源配置类
 * @author Administrator
 *
 */
@Configuration
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DynamicDataSourceConfig {

	/**
	 * 这个是系统默认的数据源
	 * @return
	 */
	@Bean(name="defaultdb")
	@ConfigurationProperties("spring.datasource.druid.defaultdb")
	public DataSource defaultDbDataSource() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean(name="unionkt")
	@ConfigurationProperties("spring.datasource.druid.unionkt")
	public DataSource unionktDataSource() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean(name="crmpbdbprod")
	@ConfigurationProperties("spring.datasource.druid.crmpbdbprod")
	public DataSource test1crmpbdbprodDataSource() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean(name="crmyydbins1")
	@ConfigurationProperties("spring.datasource.druid.crmyydbins1")
	public DataSource test2crmyydbins1DataSource() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean(name="czxxdbob60")
	@ConfigurationProperties("spring.datasource.druid.czxxdbob60")
	public DataSource test4czxxdbob60DataSource() {
		return DruidDataSourceBuilder.create().build();
	}


	@Bean(name="unionoma")
	@ConfigurationProperties("spring.datasource.druid.unionoma")
	public DataSource unionOmaDataSource() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean(name="crmpbdboma")
	@ConfigurationProperties("spring.datasource.druid.crmpbdboma")
	public DataSource test1crmpbdbOmaDataSource() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean(name="crmyydboma")
	@ConfigurationProperties("spring.datasource.druid.crmyydboma")
	public DataSource test2crmyydbOmaDataSource() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean(name="czxxdboma")
	@ConfigurationProperties("spring.datasource.druid.czxxdboma")
	public DataSource test4czxxdbOmaDataSource() {
		return DruidDataSourceBuilder.create().build();
	}



	/**
	 * 将数据源注入到动态数据源中
	 * @param defaultDbDataSource 默认数据源
	 * @param unionktDataSource
	 * @param test1crmpbdbprodDataSource
	 * @param test2crmyydbins1DataSource
	 * @param test4czxxdbob60DataSource
	 * @return
	 */
	@Bean
	@Primary
	public DynamicDataSource dataSource(@Qualifier("defaultdb")DataSource defaultDbDataSource,
										@Qualifier("unionkt")DataSource unionktDataSource,
										@Qualifier("crmpbdbprod")DataSource test1crmpbdbprodDataSource,
										@Qualifier("crmyydbins1")DataSource test2crmyydbins1DataSource,
										@Qualifier("czxxdbob60")DataSource test4czxxdbob60DataSource,

										@Qualifier("unionoma")DataSource unionomaDataSource,
										@Qualifier("crmpbdboma")DataSource test1crmpbdbomaDataSource,
										@Qualifier("crmyydboma")DataSource test2crmyydbomaDataSource,
										@Qualifier("czxxdboma")DataSource test4czxxdbomaDataSource


	) {
		Map<String, DataSource> targetDataSources = new HashMap<>();
		targetDataSources.put(DataSourcesName.DEFAULTDB, defaultDbDataSource);
		targetDataSources.put(DataSourcesName.UNIONKT, unionktDataSource);
		targetDataSources.put(DataSourcesName.CRMPBDBPROD, test1crmpbdbprodDataSource);
		targetDataSources.put(DataSourcesName.CRMYYDBINS1, test2crmyydbins1DataSource);
		targetDataSources.put(DataSourcesName.CZXXDBOB60, test4czxxdbob60DataSource);

		targetDataSources.put(DataSourcesName.UNIONOMA, unionomaDataSource);
		targetDataSources.put(DataSourcesName.CRMPBDBOMA, test1crmpbdbomaDataSource);
		targetDataSources.put(DataSourcesName.CRMYYDBOMA, test2crmyydbomaDataSource);
		targetDataSources.put(DataSourcesName.CZXXDBOMA, test4czxxdbomaDataSource);

		return new DynamicDataSource(defaultDbDataSource, targetDataSources);
	}
}

package com.om;

//import org.activiti.spring.boot.SecurityAutoConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
/**
 * 一体化配置项目核心启动类,启动主类
 * @author Administrator
 */
//暂时不加载数据源,如果要启动Activiti6工作流引擎则放开这一行注释,
//@SpringBootApplication(excludgetMenuListe= {DataSourceAutoConfiguration.class,SecurityAutoConfiguration.class})
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class}) //暂时不加载数据源
//@EnableTransactionManagement //开启事务
@ServletComponentScan
//@ComponentScan(basePackages={"com.om.modular.**"}) //指定包的扫描范围
public class ConfWebApplication extends SpringBootServletInitializer {
	private final static Logger logger = LoggerFactory.getLogger(ConfWebApplication.class);
	public static void main(String[] args) {
        SpringApplication.run(ConfWebApplication.class, args);
        logger.info("=====================ConfWebApplication is success!====================");
	}
	//重写configure方法
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ConfWebApplication.class);
	}
}

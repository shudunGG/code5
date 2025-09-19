package org.springblade.plugin.data.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.plugin.data.entity.Datasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 10:26 2021/10/26 0026
 * @ Description：
 */
@Component
public class MysqlHandler extends DatabaseHandler {
	private static final Logger logger = LoggerFactory.getLogger(MysqlHandler.class);

	public MysqlHandler() {
	}

	@Override
	protected Connection getConnection(Datasource dataSource) {
		String url = dataSource.getUrl();
		String userName = dataSource.getUsername();
		String pwd = dataSource.getPassword();
		String driverClass = dataSource.getDriverClass();
		try {
			Class.forName(driverClass);
			Properties props = new Properties();
			props.setProperty("user", userName);
			props.setProperty("password", pwd);
			props.setProperty("remarks", "true");
			props.setProperty("useInformationSchema", "true");
			Connection conn = DriverManager.getConnection(url, props);
			return conn;
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("mysql数据库驱动加载失败");
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("mysql Connect failed");
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		HANDLER_MAP.put("com.mysql.cj.jdbc.Driver", new MysqlHandler());
	}
}

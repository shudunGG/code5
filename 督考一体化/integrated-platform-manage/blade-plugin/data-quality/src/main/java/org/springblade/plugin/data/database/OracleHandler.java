package org.springblade.plugin.data.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.core.tool.utils.Func;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springblade.plugin.data.entity.Datasource;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 10:34 2021/10/26 0026
 * @ Description：
 */
@Component
public class OracleHandler extends DatabaseHandler {
	private static final Logger logger = LoggerFactory.getLogger(OracleHandler.class);

	public OracleHandler() {
	}

	@Override
	protected Connection getConnection(Datasource dataSource) {
		String url = dataSource.getUrl();
		String userName = dataSource.getUsername();
		String pwd = dataSource.getPassword();
		String driverClass = dataSource.getDriverClass();
		try {
			Class.forName(driverClass);
//			Connection conn = DriverManager.getConnection(url, userName, pwd);
			Properties info =new Properties();
			//
			info.put("user", userName);
			info.put("password", pwd);
			// !!! Oracle 如果想要获取元数据 REMARKS 信息,需要加此参数
			info.put("remarksReporting","true");
			// !!! MySQL 标志位, 获取TABLE元数据 REMARKS 信息
			info.put("useInformationSchema","true");
			// 不知道SQLServer需不需要设置...
			//
			Connection conn = DriverManager.getConnection(url, info);
			return conn;
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("oracle数据库驱动加载失败");
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("oracle Connect failed");
		}
	}

	@Override
	public List<StructureMetadata> getTableStructure(Datasource dataSource, String tableName) {
		Connection conn = this.getConnection(dataSource);
		ResultSet rs = null;
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			HashMap<String, Boolean> uniqueMap = new HashMap<>();
			String tableName1 = "\'" + tableName + "\'";
			String sql = "select t.*,i.index_type from user_ind_columns t,user_indexes i where t.index_name = i.index_name and i.uniqueness='UNIQUE' and t.table_name = " + tableName1;
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rsKey = pstmt.executeQuery();

			while (rsKey.next()) {
				String indexName = rsKey.getString("COLUMN_NAME");
				uniqueMap.put(indexName,true);
			}
			ResultSet columns = metaData.getColumns(null, "%", tableName, "%");
			return spliceMetadata(columns, uniqueMap);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("加载表结构失败");
		} finally {
			closeConnection(conn, rs, null);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		HANDLER_MAP.put("oracle.jdbc.OracleDriver", new OracleHandler());
	}
}

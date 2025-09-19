package org.springblade.plugin.data.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.core.tool.utils.*;
import org.springblade.plugin.data.config.YmlUtil;
import org.springblade.plugin.data.entity.JL;
import org.springblade.plugin.data.entity.StructureMetadata;
import org.springblade.plugin.data.entity.Datasource;
import org.springblade.plugin.data.entity.TJ;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 9:11 2021/10/26 0026
 * @ Description：数据库处理器父类
 */
@Component
public class DatabaseHandler implements InitializingBean {
	private final static Logger logger = LoggerFactory.getLogger(DatabaseHandler.class);
	//处理器
	public static Map<String, DatabaseHandler> HANDLER_MAP = new HashMap<>();

	/**
	 * 关闭连接
	 *
	 * @param conn
	 * @param rs
	 * @param stat
	 */
	protected void closeConnection(Connection conn, ResultSet rs, Statement stat) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (stat != null) {
				stat.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 获取连接
	 *
	 * @param dataSource 数据源信息：serverAddr   服务地址、databaseName 数据库名称、userName     用户名、pwd          密码
	 * @return
	 */
	protected Connection getConnection(Datasource dataSource) {
		return null;
	}

	/**
	 * 获取表名
	 *
	 * @param dataSource 数据源信息
	 * @return
	 */
	public List<Map<String, String>> getTableNames(Datasource dataSource) {
		Connection conn = this.getConnection(dataSource);
		ResultSet rs = null;
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getTables(conn.getCatalog(), conn.getSchema(), null, new String[]{"TABLE"});
			ArrayList<Map<String, String>> names = new ArrayList<>();
			while (rs.next()) {
				HashMap<String, String> nameMap = new HashMap<>();
				String table_name = rs.getString("TABLE_NAME");
				nameMap.put("name", table_name);
				nameMap.put("value", table_name);
				nameMap.put("remarks", rs.getString("REMARKS"));
				names.add(nameMap);
			}
			return names;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("加载表名失败！");
		} finally {
			closeConnection(conn, rs, null);
		}
	}

	/**
	 * @return java.util.List<org.springblade.plugin.data.entity.StructureMetadata>
	 * @Author MaQY
	 * @Description 获取表结构
	 * @Date 下午 4:55 2021/10/27 0027
	 * @Param [dataSource, tableName]
	 **/
	public List<StructureMetadata> getTableStructure(Datasource dataSource, String tableName) {
		Connection conn = this.getConnection(dataSource);
		ResultSet rs = null;
		ResultSet columns = null;
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getIndexInfo(conn.getCatalog(), conn.getSchema(), tableName, false, false);
			HashMap<String, Boolean> uniqueMap = new HashMap<>();
			while (rs.next()) {
				if (!rs.getBoolean("NON_UNIQUE")) {
					String column_name = rs.getString("COLUMN_NAME");
					uniqueMap.put(column_name, true);
				}
			}
			columns = metaData.getColumns(conn.getCatalog(), conn.getSchema(), tableName, "%");
			return spliceMetadata(columns, uniqueMap);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("加载表结构失败！");
		} finally {
			closeConnection(conn, rs, null);
			try {
				columns.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 获取主键信息
	 *
	 * @param dataSource
	 * @param tableName
	 * @return
	 */
	public Map<String, String> getPK(Datasource dataSource, String tableName) {
		Connection conn = this.getConnection(dataSource);
		ResultSet rs = null;
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			rs = metaData.getPrimaryKeys(conn.getCatalog(), conn.getSchema(), tableName);
			HashMap<String, String> resultMap = new HashMap<>();
			while (rs.next()) {
				resultMap.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
			}
			rs = metaData.getColumns(conn.getCatalog(), conn.getSchema(), tableName, resultMap.get("COLUMN_NAME"));
			while (rs.next()) {
				resultMap.put("REMARKS", rs.getString("REMARKS"));
			}
			return resultMap;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("获取主键信息失败！");
		} finally {
			closeConnection(conn, rs, null);
		}
	}

	/**
	 * @return java.util.ArrayList<org.springblade.plugin.data.entity.StructureMetadata>
	 * @Author MaQY
	 * @Description 拼接元数据
	 * @Date 下午 1:48 2021/10/29 0029
	 * @Param [columns, uniqueMap]
	 **/
	protected ArrayList<StructureMetadata> spliceMetadata(ResultSet columns, HashMap<String, Boolean> uniqueMap) throws SQLException {
		ArrayList<StructureMetadata> structureMetadatas = new ArrayList<>();
		while (columns.next()) {
			StructureMetadata structureMetadata = new StructureMetadata();
			structureMetadata.setName(columns.getString("COLUMN_NAME"));
			structureMetadata.setType(columns.getString("TYPE_NAME"));
			structureMetadata.setDataSize(columns.getInt("COLUMN_SIZE"));
			structureMetadata.setDigits(columns.getInt("DECIMAL_DIGITS"));
			structureMetadata.setNullable(columns.getInt("NULLABLE"));
			structureMetadata.setRemark(columns.getString("REMARKS"));
			structureMetadata.setChName(columns.getString("REMARKS"));
			structureMetadata.setDefaultValue(columns.getString("COLUMN_DEF"));
			Boolean aBoolean = uniqueMap.get(structureMetadata.getName());
			if (Func.isEmpty(aBoolean)) {
				structureMetadata.setIfUnique(0);
			} else {
				structureMetadata.setIfUnique(1);
			}
			structureMetadatas.add(structureMetadata);
		}
		return structureMetadatas;
	}

	/**
	 * 获取结果统计表的当前期数
	 *
	 * @param datasource
	 * @param tableName
	 * @return
	 */
	public Integer getPeriod(Datasource datasource, String tableName, Long qualityTestingProgrammeId, Long manageRuleId, Long themeId) {
		Connection conn = this.getConnection(datasource);
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {

			String sql = new StringBuffer("select period from ").append(tableName).append(" where theme_id = ? and manage_rule_id = ? and quality_testing_programme_id = ? order by period desc limit 1").toString();
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setLong(1, themeId);
			preparedStatement.setLong(2, manageRuleId);
			preparedStatement.setLong(3, qualityTestingProgrammeId);
			rs = preparedStatement.executeQuery();
			int period = 0;
			while (rs.next()) {
				period = rs.getInt("period");
			}
			return period + 1;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("获取任务执行期数失败！");
		} finally {
			closeConnection(conn, rs, preparedStatement);
		}
	}

	/**
	 * 获取特定表字段值查询条件集
	 *
	 * @param datasource
	 * @param tableName
	 * @param field
	 * @return
	 */
	public StringBuffer getConditions(Datasource datasource, String tableName, String field) {
		Connection conn = this.getConnection(datasource);
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		String sql = new StringBuffer("select DISTINCT ").append(field).append(" from ").append(tableName).toString();
		StringBuffer conditions = new StringBuffer();
		try {
			preparedStatement = conn.prepareStatement(sql);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				conditions.append("'").append(rs.getString(field)).append("',");
			}
			conditions.deleteCharAt(conditions.length() - 1);
			conditions.append(")");
			return conditions;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("获取特定表字段值查询条件集失败！");
		} finally {
			closeConnection(conn, rs, preparedStatement);
		}
	}

	/**
	 * 查询总数据量
	 *
	 * @param datasource
	 * @param sql
	 * @return
	 */
	public Long countTotal(Datasource datasource, String sql) {
		logger.info("总量查询语句，注意看有没有时间拼接---------->{}", sql);
		Connection conn = this.getConnection(datasource);
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		Long dataVolume = 0l;
		try {
			preparedStatement = conn.prepareStatement(sql);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				dataVolume = rs.getLong("data_volume");
			}
			return dataVolume;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("计数失败！");
		} finally {
			closeConnection(conn, rs, preparedStatement);
		}
	}

	/**
	 * @param datasource      源数据源
	 * @param sql             质检SQL
	 * @param sync            是否同步上期例外
	 * @param jl              带参数jl
	 * @param tableNamePrefix 结果表名称前缀
	 * @return
	 */
	public Integer errorTotal(Datasource datasource, String sql, String sync, JL jl, String tableNamePrefix) {
		logger.info("质检处理语句，注意看有没有时间拼接---------->{}", sql);
		//判断是否同步例外，是的话，查询当前不合格记录是否在例外表里，是的话，插入例外表，并将本条记录是否例外状态改为是
		//查询记录，插入记录
		//保存统计结果数据
		Connection conn = this.getConnection(datasource);
		ResultSet rs = null;
		ArrayList<JL> jls = new ArrayList<>();
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			TJ tj = new TJ();
			tj.setTotal_exception(0);
			while (rs.next()) {
				//建JL
				JL jl1 = BeanUtil.copyProperties(jl, JL.class);
				jl1.setIf_exception("0");//先默认0
				jl1.setKey_value(rs.getString("key_value"));//主键值
				jl1.setCheck_column_value(rs.getString("check_column_value"));//检查字段值
				//判断是否例外（例外的话需要插入例外表）
				jl1 = HANDLER_MAP.get(SpringUtil.getBean(YmlUtil.class).getDatabaseDriver()).handleLWAndJLTable(jl1, tableNamePrefix, sync, tj);//todo 这个jl1没有记录时间 要注意
				jls.add(jl1);
			}
			//统计结果表TJ
			HANDLER_MAP.get(SpringUtil.getBean(YmlUtil.class).getDatabaseDriver()).handleTJTable(jls.size(), jl, tj, tableNamePrefix);
			return jls.size();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("处理失败！");
		} finally {
			closeConnection(conn, rs, ps);
		}
	}

	/**
	 * 处理统计表数据
	 *
	 * @param jlSize
	 * @param jl
	 * @param tj
	 * @param tableNamePrefix
	 */
	private void handleTJTable(Integer jlSize, JL jl, TJ tj , String tableNamePrefix) {
		Connection conn = this.getConnection(SpringUtil.getBean(YmlUtil.class).currentDatasource());
		ResultSet rs = null;
		String tjInsertSql = new StringBuffer("insert into ").append(tableNamePrefix)
			.append("_TJ (statistics_time,total_error,total_exception,total_repaired,statistical_type,theme_id,manage_rule_id,quality_testing_programme_id,period,current_cycle,model_id,id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)").toString();
		PreparedStatement tjSql = null;
		try {
			tjSql = conn.prepareStatement(tjInsertSql);
			tjSql.setString(1, DateUtil.format(new java.util.Date(), DateUtil.PATTERN_DATETIME));
			tjSql.setInt(2, jlSize);
			tjSql.setInt(3, tj.getTotal_exception());
			tjSql.setInt(4, 0);
			tjSql.setString(5, jl.getCycle_type());
			tjSql.setLong(6, jl.getTheme_id());
			tjSql.setLong(7, jl.getManage_rule_id());
			tjSql.setLong(8, jl.getQuality_testing_programme_id());
			tjSql.setInt(9, jl.getPeriod());
			tjSql.setDate(10, new java.sql.Date(jl.getCurrent_cycle().getTime()));
			tjSql.setLong(11, jl.getModel_id());
			tjSql.setString(12, jl.getTj_id());
			tjSql.execute();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("处理统计表数据失败!");
		} finally {
			closeConnection(conn, rs, tjSql);
		}
	}

	/**
	 * 处理是否同步上期例外
	 *
	 * @param jl1             记录表参数
	 * @param tableNamePrefix 表名前缀
	 * @param sync            是否同步上期例外
	 * @param tj         例外数量 统计
	 * @return
	 */
	private JL handleLWAndJLTable(JL jl1, String tableNamePrefix, String sync, TJ tj) {
		Connection conn = this.getConnection(SpringUtil.getBean(YmlUtil.class).currentDatasource());
		ResultSet rs = null;
		PreparedStatement ps = null;
		PreparedStatement jlPs = null;
		PreparedStatement lwPs = null;
		String s = UUID.randomUUID().toString().replaceAll("-", "");
		String jlId = s + new java.util.Date().getTime();
		try {
			if (StringUtil.equals("1", sync)) {
				//是例外的话，根据主键值、期数、质检主键和规则主键查询例外表，看看是否有例外记录
				String lwQuerySql = new StringBuffer("select * from ").append(tableNamePrefix).append("_LW")
					.append(" where check_column = ? " +
						"and check_column_value = ? " +
						"and key_column = ? " +
						"and key_value = ? " +
						"and manage_rule_id = ? " +
						"and quality_testing_programme_id = ? " +
						"and period= ?").toString();
				ps = conn.prepareStatement(lwQuerySql);
				ps.setString(1, jl1.getCheck_column());
				ps.setString(2, jl1.getCheck_column_value());
				ps.setString(3, jl1.getKey_column());
				ps.setString(4, jl1.getKey_value());
				ps.setLong(5, jl1.getManage_rule_id());
				ps.setLong(6, jl1.getQuality_testing_programme_id());
				ps.setInt(7, jl1.getPeriod() - 1);//上一期
				rs = ps.executeQuery();
				while (rs.next()) {
					//查到了例外表，
					jl1.setIf_exception("1");
					break;//跳出循环
				}
				if (StringUtil.equals("1", jl1.getIf_exception())) {
					//已经被设为例外，则插入例外表
					String lwInsertSql = new StringBuffer("insert into ").append(tableNamePrefix)
						.append("_LW (check_column,check_column_value,key_column,key_comment,key_value,record_time,manage_rule_id,quality_testing_programme_id,period,current_cycle,cycle_type,model_id,theme_id,id,jl_id)" +
							" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").toString();
					lwPs = conn.prepareStatement(lwInsertSql);
					lwPs.setString(14, Long.toString(new java.util.Date().getTime()));
					lwPs.setString(15, jlId);
					setParam(lwPs, jl1);
					lwPs.execute();
					//例外数量+1
					tj.setTotal_exception(tj.getTotal_exception()+1);
				}
			}
			//无论是否要插入例外表，都要插入记录表
			String jlInsertSql = new StringBuffer("insert into ").append(tableNamePrefix)
				.append("_JL (check_column,check_column_value,key_column,key_comment,key_value,record_time,manage_rule_id,quality_testing_programme_id,period,current_cycle,cycle_type,model_id,theme_id,if_exception,tj_id,id)" +
					" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").toString();
			jlPs = conn.prepareStatement(jlInsertSql);
			setParam(jlPs, jl1);
			//补充set一下是否例外
			jlPs.setString(14, jl1.getIf_exception());
			jlPs.setString(15, jl1.getTj_id());
			jlPs.setString(16, jlId);
			jlPs.execute();
			return jl1;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("质检处理失败！");
		} finally {
			closeConnection(conn, rs, ps);
			closeStatement(jlPs);
			closeStatement(lwPs);
		}
	}

	/**
	 * 关闭ps
	 *
	 * @param stat
	 */
	protected void closeStatement(Statement stat) {
		try {
			if (stat != null) {
				stat.close();
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 给预编译语句set参数
	 *
	 * @param ps
	 * @param jl1
	 * @throws SQLException
	 */
	public static void setParam(PreparedStatement ps, JL jl1) throws SQLException {
		ps.setString(1, jl1.getCheck_column());
		ps.setString(2, jl1.getCheck_column_value());
		ps.setString(3, jl1.getKey_column());
		ps.setString(4, jl1.getKey_comment());
		ps.setString(5, jl1.getKey_value());
		ps.setString(6, DateUtil.format(new java.util.Date(), DateUtil.PATTERN_DATETIME));
		ps.setLong(7, jl1.getManage_rule_id());
		ps.setLong(8, jl1.getQuality_testing_programme_id());
		ps.setInt(9, jl1.getPeriod());
		ps.setDate(10, new java.sql.Date(jl1.getCurrent_cycle().getTime()));
		ps.setString(11, jl1.getCycle_type());
		ps.setLong(12, jl1.getModel_id());
		ps.setLong(13, jl1.getTheme_id());
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}
}

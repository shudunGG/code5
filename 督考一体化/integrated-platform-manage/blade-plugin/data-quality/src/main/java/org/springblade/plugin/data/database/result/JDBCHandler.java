package org.springblade.plugin.data.database.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.plugin.data.config.YmlUtil;
import org.springblade.plugin.data.database.DatabaseHandler;
import org.springblade.plugin.data.entity.JL;
import org.springblade.plugin.data.entity.LW;
import org.springblade.plugin.data.entity.TJ;
import org.springblade.plugin.data.vo.LWVO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author MaQiuyun
 * @date 2021/12/27 19:47
 * @description:模型三张结果表处理
 */
public class JDBCHandler {
	private final static Logger logger = LoggerFactory.getLogger(JDBCHandler.class);

	/**
	 * 获取链接
	 *
	 * @return
	 */
	private static Connection getConn() {
		try {
			YmlUtil ymlUtil = SpringUtil.getBean(YmlUtil.class);
			Class.forName(ymlUtil.getDatabaseDriver());
			Properties props = new Properties();
			props.setProperty("user", ymlUtil.getDatabaseUserName());
			props.setProperty("password", ymlUtil.getDatabasePassword());
			props.setProperty("remarks", "true");
			props.setProperty("useInformationSchema", "true");
			Connection conn = DriverManager.getConnection(ymlUtil.getDatabaseUrl(), props);
			return conn;
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("mysql数据库驱动加载失败");
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("mysql Connect failed");
		}
	}

	/**
	 * 关闭资源
	 *
	 * @param conn
	 * @param rs
	 * @param st
	 */
	private static void close(Connection conn, ResultSet rs, Statement st) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (st != null) {
				st.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 获取总行数
	 *
	 * @param sql 查询需命名为total
	 * @return
	 */
	public static Long getTotalCount(String sql) {
		Connection conn = getConn();
		ResultSet rs = null;
		PreparedStatement ps = null;
		Long total = 0l;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				total = rs.getLong("total");
			}
			return total;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("获取总量失败！");
		} finally {
			close(conn, rs, ps);
		}
	}

	/**
	 * 获取例外表统计记录
	 *
	 * @param sql
	 * @return
	 */
	public static List<LWVO> getLWStatisticList(String sql) {
		Connection conn = getConn();
		ResultSet rs = null;
		PreparedStatement ps = null;
		ArrayList<LWVO> lws = new ArrayList<>();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				LWVO lwvo = new LWVO();
				lwvo.setCount(rs.getInt("total"));
				lwvo.setManage_rule_id(rs.getLong("manage_rule_id"));
				lws.add(lwvo);
			}
			return lws;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("查询例外记录失败！");
		} finally {
			close(conn, rs, ps);
		}
	}

	/**
	 * 获取统计结果列表
	 *
	 * @param sql
	 * @return
	 */
	public static List<TJ> getTJList(String sql) {
		Connection conn = getConn();
		ResultSet rs = null;
		PreparedStatement ps = null;
		ArrayList<TJ> tjs = new ArrayList<>();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				//组装TJ
				tjs.add(packageTJ(rs));
			}
			return tjs;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("查询失败！");
		} finally {
			close(conn, rs, ps);
		}
	}

	/**
	 * 获取记录表
	 *
	 * @param sql
	 * @return
	 */
	public static List<JL> getJLList(String sql) {
		Connection conn = getConn();
		ResultSet rs = null;
		PreparedStatement ps = null;
		ArrayList<JL> jls = new ArrayList<>();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				//组装TJ
				jls.add(packageJL(rs));
			}
			return jls;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("查询失败！");
		} finally {
			close(conn, rs, ps);
		}
	}

	/**
	 * 获取例外表记录
	 *
	 * @param sql
	 * @return
	 */
	public static List<LW> getLWList(String sql) {
		Connection conn = getConn();
		ResultSet rs = null;
		PreparedStatement ps = null;
		ArrayList<LW> lws = new ArrayList<>();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				//组装TJ
				lws.add(packageLW(rs));
			}
			return lws;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("查询失败！");
		} finally {
			close(conn, rs, ps);
		}
	}

	/**
	 * 组装记录表和例外表的基础部分
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private static JL packageBase(ResultSet rs) throws SQLException {
		JL jl = new JL();
		jl.setCheck_column(rs.getString("check_column"));
		jl.setCheck_column_value(rs.getString("check_column_value"));
		jl.setKey_column(rs.getString("key_column"));
		jl.setKey_comment(rs.getString("key_comment"));
		jl.setKey_value(rs.getString("key_value"));
		jl.setRecord_time(rs.getDate("record_time"));
		jl.setManage_rule_id(rs.getLong("manage_rule_id"));
		jl.setQuality_testing_programme_id(rs.getLong("quality_testing_programme_id"));
		jl.setModel_id(rs.getLong("model_id"));
		jl.setPeriod(rs.getInt("period"));
		jl.setCurrent_cycle(rs.getDate("current_cycle"));
		jl.setCycle_type(rs.getString("cycle_type"));
		jl.setId(rs.getString("id"));
		jl.setTheme_id(rs.getLong("theme_id"));
		return jl;
	}

	/**
	 * 组装例外表记录
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private static LW packageLW(ResultSet rs) throws SQLException {
		JL jl = packageBase(rs);
		LW lw = BeanUtil.copyProperties(jl, LW.class);
		lw.setJl_id(rs.getString("jl_id"));
		return lw;
	}

	/**
	 * 组装记录表记录
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private static JL packageJL(ResultSet rs) throws SQLException {
		JL jl = packageBase(rs);
		jl.setIf_exception(rs.getString("if_exception"));
		jl.setTj_id(rs.getString("tj_id"));
		return jl;
	}

	/**
	 * 组装统计表查询结果
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private static TJ packageTJ(ResultSet rs) throws SQLException {
		TJ tj = new TJ();
		tj.setId(rs.getString("id"));
		tj.setStatistics_time(rs.getDate("statistics_time"));
		tj.setTotal_error(rs.getInt("total_error"));
		tj.setTotal_exception(rs.getInt("total_exception"));
		tj.setTotal_repaired(rs.getInt("total_repaired"));
		tj.setStatistical_type(rs.getString("statistical_type"));
		tj.setTheme_id(rs.getLong("theme_id"));
		tj.setManage_rule_id(rs.getLong("manage_rule_id"));
		tj.setQuality_testing_programme_id(rs.getLong("quality_testing_programme_id"));
		tj.setModel_id(rs.getLong("model_id"));
		tj.setPeriod(rs.getInt("period"));
		tj.setCurrent_cycle(rs.getDate("current_cycle"));
		return tj;
	}

	/**
	 * 更新jl表信息（包括为null字段）
	 *
	 * @param jlMap
	 * @param tableName
	 * @return
	 */
	public static Integer updateJLById(Map<String, String> jlMap, String tableName) {
		StringBuffer updateSql = new StringBuffer("update ").append(tableName).append(" set ");
		for (Map.Entry<String, String> entry : jlMap.entrySet()) {
			if ((!StringUtil.equals("id", entry.getKey())) && (!entry.getKey().contains("$"))) {
				updateSql.append(entry.getKey()).append(" = '").append(String.valueOf(entry.getValue())).append("',");
			}
		}
		updateSql.deleteCharAt(updateSql.length() - 1);
		updateSql.append(" where id = '").append(jlMap.get("id")).append("'");
		Connection conn = getConn();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(updateSql.toString());
			return ps.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("更新失败！");
		} finally {
			close(conn, rs, ps);
		}
	}

	/**
	 * 根据记录插入例外数据
	 *
	 * @param jl
	 * @param tableName
	 * @return
	 */
	public static Integer saveLWByJL(JL jl, String tableName) {
		Connection conn = getConn();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String lwInsertSql = new StringBuffer("insert into ").append(tableName)
			.append(" (check_column,check_column_value,key_column,key_comment,key_value,record_time,manage_rule_id,quality_testing_programme_id,period,current_cycle,cycle_type,model_id,theme_id,id,jl_id)" +
				" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").toString();
		try {
			ps = conn.prepareStatement(lwInsertSql);
			ps.setString(14, Long.toString(new java.util.Date().getTime()));
			ps.setString(15, jl.getId());
			DatabaseHandler.setParam(ps, jl);
			return ps.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("保存失败！");
		} finally {
			close(conn, rs, ps);
		}
	}

	/**
	 * 合计类型的字段加1
	 *
	 * @param tableName
	 * @param totalColumn
	 * @param id
	 * @return
	 */
	public static Integer totalColumnAddOne(String tableName, String totalColumn, String id) {
		Connection conn = getConn();
		ResultSet rs = null;
		PreparedStatement ps = null;
		String sql = new StringBuffer("update ").append(tableName)
			.append(" set ").append(totalColumn).append(" = ").append(totalColumn).append("+ 1 ")
			.append("where id = '").append(id).append("'").toString();
		try {
			ps = conn.prepareStatement(sql);
			return ps.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("统计表修改失败！");
		} finally {
			close(conn, rs, ps);
		}
	}
}

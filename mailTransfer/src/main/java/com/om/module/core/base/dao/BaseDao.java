package com.om.module.core.base.dao;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 数据库操作的基础类
		*/
@Repository("baseDao")
public class BaseDao extends SqlSessionDaoSupport{
	
//	@Autowired
//	private SqlSessionFactory sqlSessionFactory;
//	
//  @Autowired
//  private SqlSessionTemplate sqlSessionTemplate;
//	
    //@Resource
	@Autowired
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory){
        super.setSqlSessionFactory(sqlSessionFactory);
    }
	public int insert(String key, Object object) {
		return getSqlSession().insert(key, object);
	}

//	public int insertLog(Map map) {
//		return getSqlSession().insert(map.toString());
//	}

	public int update(String statementName, Object object){
		return getSqlSession().update(statementName, object);
	}
	
	public int delete(String key, Serializable id) {
		return getSqlSession().delete(key, id);
	}
	
	public int delete(String key, Object object) {
		return getSqlSession().delete(key, object);
	}
	/**
	 * 查询单个对象
	 * @param key
	 * @param params
	 * @return
	 */
	public <T> T get(String key, Object params) {
		return (T) getSqlSession().selectOne(key, params);
	}
	
	/**
	 * 查询数据返回一个List,无需添加查询条件参数
	 * @param key sqlmap xml文件里面的id
	 * @return
	 */
	public <T> List<T> getList(String key) {
		return getSqlSession().selectList(key);
	}
	
	/**
	 * 查询数据返回一个List,添加查询参数
	 * @param key sqlmap xml文件里面的id
	 * @param params 查询入参对象,一般是一个HashMap
	 * @return
	 */
	public <T> List<T> getList(String key, Object params) {
		return getSqlSession().selectList(key, params);
	}

	/**
	 * 获取执行的SQL
	 * @param key
	 * @param params
	 * @return
	 */
	public String getSql(String key,Object params){
		MappedStatement stmt = getSqlSession().getConfiguration().getMappedStatement(key);
		BoundSql boundSql = stmt.getSqlSource().getBoundSql(params);
		String sql = boundSql.getSql();
		for(ParameterMapping pm:boundSql.getParameterMappings()){
			Object param = ((HashMap)params).get(pm.getProperty());
			if(param != null){
				String val = param.toString();
				if(pm.getJdbcType() != null && pm.getJdbcType().name().equalsIgnoreCase("VARCHAR")){
					sql = sql.replaceFirst("\\?","'"+val+"'");
				}else{
					sql = sql.replaceFirst("\\?",val);
				}
			}
		}
		return sql;
	}

}

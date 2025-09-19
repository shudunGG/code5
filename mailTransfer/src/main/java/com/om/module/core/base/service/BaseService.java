package com.om.module.core.base.service;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.mysql.cj.Session;
import org.springframework.stereotype.Service;

import com.om.module.core.base.dao.BaseDao;

/**
 * 基础Service类,提供数据库基础操作(增加删除修改的简单逻辑实现)
 * @author Administrator
 *
 */
@Service("baseService")
public class BaseService {
	
	@Resource(name="baseDao")
	private BaseDao baseDao;
	/**
	 * 基础Service的插入操作
	 * @param key
	 * @param object
	 * @return
	 */
	public int insert(String key, Object object) {
		return this.baseDao.insert(key, object);
	}


	public int update(String statementName, Object object){
		return this.baseDao.update(statementName, object);
	}
	
	public int delete(String key, Serializable id) {
		return this.baseDao.delete(key, id);
	}
	
	public int delete(String key, Object object) {
		return this.baseDao.delete(key, object);
	}

	public int insertLog(HttpServletRequest request,Map map,String id) {
		HttpSession session = request.getSession();
		String MAIN_ACCOUNT_NAME = (String)session.getAttribute("MAIN_ACCOUNT_NAME");
		String CLIENT_NETWORK_ADDRESS=(String)session.getAttribute("CLIENT_NETWORK_ADDRESS");
		String ip_addr = (String) session.getAttribute("IP_ADDR");
		if(ip_addr!=null){
			map.put("IP_ADDR",ip_addr);
		}else{
			String remoteAddr = request.getRemoteAddr();
			map.put("IP_ADDR",remoteAddr);

		}
		if(MAIN_ACCOUNT_NAME !=null ){
			map.put("MAIN_ACCT_ID",MAIN_ACCOUNT_NAME);
		}
		if(CLIENT_NETWORK_ADDRESS!=null){
			map.put("MAC_ADDR",CLIENT_NETWORK_ADDRESS);
		}
		HashMap<String,Object> operInfo = (HashMap<String, Object>) session.getAttribute("OP_INFO");
		if(operInfo!=null){
			Object OP_ID =  operInfo.get("OP_ID");
			int opID=Integer.parseInt(OP_ID.toString());
			if(OP_ID!=null){
				map.put("OP_ID",opID);
			}
		}

		map.put("OPER_PARAM",JSON.toJSONString(map));
//		String IP_ADDR = super.getIp(request) ;
		map.put("BUSI_CODE",id);
		return this.baseDao.insert("cfLogMapper.insertLog", map);
	}





	/**
	 * 查询单个对象
	 * @param key
	 * @param params
	 * @return
	 */
	public <T> T get(String key, Object params) {
		return this.baseDao.get(key, params);
	}
	
	
	/**
	 * 查询数据返回一个List,无需添加查询条件参数
	 * @param key sqlmap xml文件里面的id
	 * @return
	 */
	public <T> List<T> getList(String key) {
		return this.baseDao.getList(key);
	}
	
	/**
	 * 查询数据返回一个List,添加查询参数
	 * @param key sqlmap xml文件里面的id
	 * @param params 查询入参对象,一般是一个HashMap
	 * @return
	 */
	public <T> List<T> getList(String key, Object params) {
		return this.baseDao.getList(key, params);
	}

	/**
	 * 查询数据返回一个List,添加查询参数
	 * @param key sqlmap xml文件里面的id
	 * @param params 查询入参对象,一般是一个HashMap
	 * @return
	 */
	public  Object getObject(String key, Object params) {
		List list = this.baseDao.getList(key, params);
		if(list.size()>0){
			return list.get(0);
		}else{
			return null;
		}

	}

	/**
	 * 按钮权限验证通用方法
	 * true有权限,false无权限
	 */
	public boolean checkMenuBtn(Object params){
		Integer total = this.baseDao.get("iconfSys.queryOperBtnEntityTotal",params);
		if(total > 0){
			return true ;
		}
		return false;
	}

	/**
	 * 获取执行的sql
	 * @param key
	 * @param param
	 * @return
	 */
	public String getSql(String key,Object param){
		return this.baseDao.getSql(key,param);
	}
}

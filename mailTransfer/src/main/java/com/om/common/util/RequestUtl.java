/**
 * 
 */
package com.om.common.util;

import com.alibaba.fastjson.JSONObject;
import com.om.bo.base.PDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author wang
 *
 */
public class RequestUtl {
	private final static Logger log = LoggerFactory.getLogger(RequestUtl.class);

	public static void printRequestMap(HttpServletRequest request){
		Map hs = (Map)request.getParameterMap();
		HashMap map = new HashMap();
		Iterator iter = hs.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = entry.getKey().toString();
			String[] t = (String[])hs.get(key);
			String val = "";
			if(t.length > 0){
				if(key.equalsIgnoreCase("page")) //因为page参数与jq easyui会重叠,提交时这里面会有两个值,暂时先去除jq的page
					val = t[0];
				else
					for(int n=0;n<t.length;n++){
						if(n==0) val += t[n];
						else val += "," + t[n];
					}
			}
			log.info(key + ":" + val);
		}
	}
	public static boolean isRequestMapSec(HttpServletRequest request){
		boolean rs = true;
		Map hs = (Map)request.getParameterMap();
		HashMap map = new HashMap();
		Iterator iter = hs.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = entry.getKey().toString();
			String[] t = (String[])hs.get(key);
			String val = "";
			if(t.length > 0){
				if(key.equalsIgnoreCase("page")) //因为page参数与jq easyui会重叠,提交时这里面会有两个值,暂时先去除jq的page
					val = t[0];
				else
					for(int n=0;n<t.length;n++){
						if(n==0) val += t[n];
						else val += "," + t[n];
					}
			}
			if(val.indexOf("web.xml")>-1 || val.indexOf("</")>-1 || val.indexOf("&lt;")>-1 || val.indexOf("WEB-INF")>-1){
				rs = false;
				break;
			}
		}
		
		return rs;
	}

	/*
	public static HashMap<String,Object> getRequestMap(HttpServletRequest request){
		Map hs = (Map)request.getParameterMap();
		HashMap<String,Object> map = new HashMap<String,Object>();
		Iterator iter = hs.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = entry.getKey().toString();
			String[] t = (String[])hs.get(key);
			String val = "";
			if(t.length > 0){
				if(key.equalsIgnoreCase("page")) //因为page参数与jq easyui会重叠,提交时这里面会有两个值,暂时先去除jq的page
					val = t[0];
				else
					for(int n=0;n<t.length;n++){
						if(n==0) val += t[n];
						else val += "," + t[n];
					}
			}
			if(val != null && val.length() > 0){
				map.put(key, val);
			}
		}
		return map;
	}*/

	public static PDto getRequestMap(HttpServletRequest request){
		Map hs = (Map)request.getParameterMap();
		PDto map = new PDto();
		Iterator iter = hs.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = entry.getKey().toString();
			String[] t = (String[])hs.get(key);
			String val = "";
			if(t.length > 0){
				if(key.equalsIgnoreCase("page")) //因为page参数与jq easyui会重叠,提交时这里面会有两个值,暂时先去除jq的page
					val = t[0];
				else
					for(int n=0;n<t.length;n++){
						if(n==0) val += t[n];
						else val += "," + t[n];
					}
			}
			if(val != null && val.length() > 0){
				map.put(key, val);
			}
		}

		try {
			StringBuffer sb = new StringBuffer();
			InputStream is = request.getInputStream();
			InputStreamReader isr = new InputStreamReader(is,"UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String s = "";
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
			String str = sb.toString();
			log.debug("input param:========"+str);
			JSONObject json = JSONObject.parseObject(str);
			Map m2 = (Map)json;
			if(m2!=null && m2.size()>0){
				Iterator it = m2.keySet().iterator();
				while(it.hasNext()){
					String key = (String)it.next();
					Object value = m2.get(key);
					if(value ==null){
						value ="";
					}
					map.put(key,value.toString());
				}

			}
		}catch (Exception e){
			log.error(e.getMessage(),e);
		}

		return map;
	}
	/**
	 * 返回true表示从报表系统正常登录的,返回false表示从外系统链接方式登录
	 * @param request
	 * @return
	 */
	public static boolean isInnerLogin(HttpServletRequest request){		
		return true;
	}
}

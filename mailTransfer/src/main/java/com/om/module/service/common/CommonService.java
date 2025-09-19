package com.om.module.service.common;

import com.om.bo.base.FtpInfo;
import com.om.bo.base.FtpPathInfo;
import com.om.util.ObjectTools;

import com.om.module.core.base.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.*;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CommonService {
	protected Logger logger = LoggerFactory.getLogger(CommonService.class);
	public String sep = "/";//System.getProperty("path.separator");
	@Resource(name="baseService")
	public BaseService baseService;

	/**
	 * 用于检查request来的参数是否为空
	 * @param name
	 * @param val
	 * @return
	 * @throws Exception
	 */
	public void isNull(String name,Object val)throws Exception{
		if(ObjectTools.isNull(val)){
			throw new Exception("["+name+"]值不能为空");
		}
	}

	public String encodeHtmlEntities(String input) {
		if(input==null){
			return null;
		}else{
			String encoded = input.replaceAll("&", "&amp;")
					.replaceAll("\"", "&quot;")
					.replaceAll("'", "&#x27;")
					.replaceAll("<", "&lt;")
					.replaceAll(">", "&gt;");
			return encoded;
		}
	}



	public String decodeString(String s) throws UnsupportedEncodingException {
		if(s == null){
			return null;
		}else{
			if(s.indexOf("%")>-1){
				s =  URLDecoder.decode(s,"UTF-8");
				return s;
			}
			return s;
		}

	}

	public void printParam(Map param,String logHead) throws Exception{
		String IS_PRINT = (String)param.get("IS_PRINT");
		if(IS_PRINT==null){
			return;
		}
		Iterator it  = param.keySet().iterator();
		while(it.hasNext()){
			Object key = it.next();
			Object val = param.get(key);
			logger.debug(logHead+":key:"+key+" val:"+val);
		}
	}

	/**
	 * 检查参数name的值val，是否在枚举范围enumStr中
	 * @param name
	 * @param val
	 * @param enumStr
	 * @throws Exception
	 */
	public void isEnum(String name,String val,String enumStr)throws Exception{
		String[] arr = enumStr.split(",");
		for(String s:arr){
			if(s.equals(val)){
				return;
			}
		}
		throw new Exception("["+name+"]值不在枚举范围["+enumStr+"]之中，请检查！");
	}



	/**
	 * 设置权限中数据范围的配置要素
	 * @param param
	 * @throws Exception
	 */
	public void setDataScaleConf(Map param)throws Exception{
		String IS_ADMIN = (String)param.get("IS_ADMIN");
		String USER_ID = (String)param.get("USER_ID");
		this.isNull("USER_ID",USER_ID);
		this.isNull("IS_ADMIN",IS_ADMIN);
		if("0".equals(IS_ADMIN)){
			param.put("DATA_SCALE_USER_ID",USER_ID);
		}
	}

	/**
	 * 设置分页的参数
	 * @param param
	 * @throws Exception
	 */
	public void setSplitPageParam(Map param)throws Exception{
		String PAGE_NUM = (String)param.get("PAGE_NUM");
		String PAGE_SIZE = (String)param.get("PAGE_SIZE");
		this.isNull("PAGE_NUM",PAGE_NUM);
		this.isNull("PAGE_SIZE",PAGE_SIZE);
		int num = Integer.parseInt(PAGE_NUM);
		int size = Integer.parseInt(PAGE_SIZE);
		int start = (num-1)* size;
		param.put("START_POS",start);
		param.put("DATA_LIMIT",1);
	}

	/**
	 * 返回异常堆栈的内容
	 * @param aThrowable
	 * @return
	 */
	public   String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		String s = result.toString();
		if(s.length()<1000){
			return s;
		}else{
			return s.substring(0,1000);
		}
	}

	/**
	 * 将前台传入的ID串，转成数据库可以识别的
	 * @param PK_ARR，格式如：["aaa","bbb"]
	 * @return 'aaa','bbb'
	 */
	public String switchIdArr(String PK_ARR){
		if(PK_ARR!=null){
			PK_ARR = PK_ARR.replaceAll("\\[","");
			PK_ARR = PK_ARR.replaceAll("]","");
			if(PK_ARR.indexOf("\"")>-1){
				PK_ARR = PK_ARR.replaceAll("\"","'");
			}
			if(PK_ARR.indexOf("'")==-1){
				PK_ARR = "'"+PK_ARR.replaceAll(",","','")+"'";
			}
		}
		return PK_ARR;
	}

	/**
	 * 将前台传入的ID串，转成数据库可以识别的
	 * @param PK_ARR，格式如：["aaa","bbb"]
	 * @return aaa,bbb
	 */
	public String switchIdArrPure(String PK_ARR){
		if(PK_ARR!=null){
			PK_ARR = PK_ARR.replaceAll("\\[","");
			PK_ARR = PK_ARR.replaceAll("]","");
			PK_ARR = PK_ARR.replaceAll("\"","");
			PK_ARR = PK_ARR.replaceAll("'","");
		}
		return PK_ARR;
	}



	   
}

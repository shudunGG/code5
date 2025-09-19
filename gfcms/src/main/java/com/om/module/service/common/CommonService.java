package com.om.module.service.common;

import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.bo.base.FtpPathInfo;
import com.om.common.util.FileUtils;
import com.om.common.util.ObjectTools;
import com.om.common.util.SFTPUtil2;
import com.om.config.mybatis.core.DataSourcesName;
import com.om.config.mybatis.core.DynamicDataSource;
import com.om.module.core.base.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
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


	/**
	 * 设置权限中数据范围的配置要素
	 * @param param
	 * @throws Exception
	 */
	public void setDataScaleConf(Map param)throws Exception{
		String IS_ADMIN = (String)param.get("IS_ADMIN");
		String USER_ID = (String)param.get("USER_ID");
		//this.isNull("USER_ID",USER_ID);暂时屏蔽，后面等杨鑫解决了USER_ID的问题后必须打开
		//this.isNull("IS_ADMIN",IS_ADMIN);
		if(IS_ADMIN == null){
			IS_ADMIN = "0";
		}
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


	/**
	 * 部署文件
	 * @param sourceRoot 源根目录
	 * @param targetRoot 目标根目录
	 * @param context 上下文
	 * @param fileName 文件名
	 * @param ftp sftp信息，部署方式为file时为空
	 * @throws Exception
	 */
	public boolean deployFile(String sourceRoot, String targetRoot, String context, String fileName, FtpInfo ftp)throws Exception{
		if(!sourceRoot.endsWith("/")){
			sourceRoot = sourceRoot+"/";
		}
		if(!targetRoot.endsWith("/")){
			targetRoot = targetRoot+"/";
		}
		String source = sourceRoot+context+sep+fileName;
		String deployMode = ftp.getDeployMode();
		if("file".equals(ftp.getDeployMode())){//分为file or sftp
			try {
				String targetPath = targetRoot+context+sep;
				File pathFile = new File(targetPath);
				if(!pathFile.exists()){
					pathFile.mkdirs();
				}

				String target = targetPath+fileName;
				logger.debug("部署文件：原始地址："+source);
				logger.debug("部署文件：目录地址："+target);
				FileUtils.copyFile(source,target);
				return true;
			}catch (Exception e){
				logger.error(e.getMessage(),e);
				return false;
			}
		}else if("sftp".equals(deployMode)){
			try {
				SFTPUtil2 t = new SFTPUtil2(ftp);
				File f = new File(source);
				boolean rs = t.uploadFile(ftp.getSftpRoot()+sep+context,f);
				return rs;
			}catch (Exception e){
				logger.error(e.getMessage(),e);
				return false;
			}
		}
		return false;
	}

	public void deployFile(List<FtpPathInfo> listFile, FtpInfo ftp)throws Exception{
		if("file".equals(ftp.getDeployMode())){
			for(FtpPathInfo pathInfo:listFile){
				try {
					File pathFile = pathInfo.getTargetfile().getParentFile();
					if(!pathFile.exists()){
						pathFile.mkdirs();
					}

					FileUtils.copyFile(pathInfo.getLocalfile(),pathInfo.getTargetfile());
				}catch (Exception e){
					logger.error(e.getMessage(),e);
				}
			}
		}else if("sftp".equals(ftp.getDeployMode())){
			try {
				SFTPUtil2 t = new SFTPUtil2(ftp);
				t.uploadFile(listFile,ftp);
			}catch (Exception e){
				logger.error(e.getMessage(),e);

			}
		}
	}

	public void undeployFile(List<FtpPathInfo> listFile, FtpInfo ftp)throws Exception{
		if("file".equals(ftp.getDeployMode())){
			for(FtpPathInfo pathInfo:listFile){
				try {
					pathInfo.getTargetfile().delete();
				}catch (Exception e){
					logger.error(e.getMessage(),e);
				}
			}
		}else if("sftp".equals(ftp.getDeployMode())){
			try {
				SFTPUtil2 t = new SFTPUtil2(ftp);
				t.delteFile(listFile,ftp);
			}catch (Exception e){
				logger.error(e.getMessage(),e);

			}
		}

	}


	/**
	 * 把本地的URL替换成域名的URL
	 * 例如：把本地地址：http://120.53.104.176:7777/files//兰州工蜂/home/index.html
	 * 		替换为
	 *  	http://www.lzgfxx.com/home/index.html
	 * @param localUrl 本地URL
	 * @param domainUrl 域名地址
	 * @param site_code 站点CODE
	 * @return
	 */
	public String replaceDomainUrl222222(String localUrl,String domainUrl,String site_code){
		//TODO: 看最后页面，附件，图片等地址能不能延迟展示地址，因为有可能前期地址按旧地址上传了，后续主站的域名改了一下，导致所有的附件都要改地址，很麻烦
		String returnUrl = localUrl;
		if(localUrl.indexOf(site_code)==-1){
			return returnUrl;
		}
		if(domainUrl != null && domainUrl.startsWith("http")){
			int idx = localUrl.indexOf(site_code)+site_code.length();
			String subStr = localUrl.substring(idx);
			returnUrl = domainUrl+subStr;
		}
		return returnUrl;
	}

	/**
	 * 把本地URL地址http://localhost:7777/files/lanzhouGov/202409/1725355521500.jpg 替换为：1725355521500.jpg
	 * @param localUrl
	 * @param site_code
	 * @return
	 */
	public String replacePreUrlAddr(String localUrl,String site_code){
		if(localUrl.indexOf(site_code) > -1 ){
			int idx = localUrl.lastIndexOf("/");
			String subStr = localUrl.substring(idx)+1;
			return subStr;
		}else{
			return localUrl;
		}

	}

	//
}

package com.om.module.controller.base;

import com.om.bo.base.FtpInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Map;

/**
 * 通用查询控制器
 * @author Administrator
 *
 */
@Controller
@RequestMapping("base")
public class BaseCtrl {

	@Autowired
	protected HttpServletRequest request;
	@Autowired
	protected HttpServletResponse response;
	//protected final Logger logger = LoggerFactory.getLogger(BaseController.class);
	public final static String InvokePage = "invokePage";
	

	public FtpInfo getDeployInfo(Environment env, Map deployConfMap){
		String deployMode =   env.getProperty("deployMode");
		String targetRoot =   env.getProperty("targetRoot");
		String Sftp_host =   env.getProperty("Sftp_host");
		String Sftp_username =   env.getProperty("Sftp_username");
		String Sftp_password =   env.getProperty("Sftp_password");
		String Sftp_port =   env.getProperty("Sftp_port");
		String Sftp_root =   env.getProperty("Sftp_root");
		String rootPath =   env.getProperty("serverPathUploadPermanent");//永久性上传文件的服务器根目录
		String appRootPath =   env.getProperty("appRootPath");//永久性上传文件的网络访问根目录

		if(deployConfMap!=null){
			rootPath = (String)deployConfMap.get("SOURCE_ROOT");
			appRootPath = (String)deployConfMap.get("URL_ROOT");
			deployMode = (String)deployConfMap.get("DEPLOY_MODE");
			targetRoot = (String)deployConfMap.get("TARGET_ROOT");
			Sftp_host = (String)deployConfMap.get("SFTP_HOST");
			Sftp_username = (String)deployConfMap.get("SFTP_USER");
			Sftp_password = (String)deployConfMap.get("SFTP_PWD");
			if(deployConfMap.get("SFTP_PORT")!=null){
				Sftp_port = deployConfMap.get("SFTP_PORT").toString();
			}
			Sftp_root = (String)deployConfMap.get("SFTP_ROOT");
		}
		FtpInfo ftp = new FtpInfo(Sftp_username,Sftp_password,Sftp_host,Integer.parseInt(Sftp_port),Sftp_root);
		ftp.setDeployMode(deployMode);
		ftp.setTargetRoot(targetRoot);
		ftp.setRootPath(rootPath);
		ftp.setAppRootPath(appRootPath);
		return ftp;
	}

	public String getIp(HttpServletRequest req){
		String ip = req.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getRemoteAddr();
		}
		return ip;
	}

	protected String getWebPath(String folder){
		try{
			String currDir = URLDecoder.decode(this.getClass().getClassLoader().getResource("/").getPath(),"UTF-8");
			int idx = currDir.indexOf("WEB-INF");

			currDir = "/" + currDir.substring(1, idx) + folder;
			File file = new File(currDir);
			if(!file.exists()) file.mkdirs();

			return currDir;
		}catch(Exception ex){
			return null;
		}
	}


	public static String getStackTrace(Throwable aThrowable) {
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
}

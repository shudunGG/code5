package org.springblade.resource.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/25 17:41
 */
@Slf4j
@RestController
public class FileProxyController {

	/**
	 * 通过url下载文件
	 * @author MrTang
	 * @date 2021/9/14
	 * @param fileUrl   文件url
	 * @param fileName  文件名
	 * @param response  响应
	 * @return void
	 */
	@RequestMapping("/fileProxy")
	public void fileProxy(@RequestParam String fileUrl, @RequestParam String fileName, HttpServletResponse response) throws IOException {
		response.setCharacterEncoding("UTF-8");
		try {

			//处理特殊字符
			fileName = fileName.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
			fileName = fileName.replaceAll("\\+", "%2B");

			fileUrl = URLDecoder.decode(fileUrl,"utf-8");
			fileName = URLDecoder.decode(fileName,"utf-8");

			fileUrl = tranformStyle(fileUrl);

			URL url = new URL(fileUrl);
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			boolean useHttps = fileUrl.startsWith("https");
			if (useHttps) {
				HttpsURLConnection https = (HttpsURLConnection) uc;
				trustAllHosts(https);
				https.setHostnameVerifier(DO_NOT_VERIFY);
			}
			uc.setConnectTimeout(2000);
			if(uc.getResponseCode() == 200){
				response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
				response.setContentType(uc.getContentType());
				IOUtils.copy(uc.getInputStream(),response.getOutputStream());
			} else {
				response.setContentType("application/json;charset=utf-8");
				JSONObject jsonObject = new JSONObject();
				jsonObject.append("responseCode",uc.getResponseCode());
				jsonObject.append("fileUrl",fileUrl);
				jsonObject.append("fileName",fileName);
				response.getWriter().println(jsonObject);
			}
		} catch (IOException e){
			log.error(e.getMessage());
			response.getWriter().println(e.getMessage());
		}
	}

	/**
	 * 覆盖java默认的证书验证
	 */
	private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[]{};
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		}
	}};

	/**
	 * 设置不验证主机
	 */
	private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * 信任所有
	 * @param connection
	 * @return
	 */
	private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
		SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory newFactory = sc.getSocketFactory();
			connection.setSSLSocketFactory(newFactory);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oldFactory;
	}

	/**
	 * 对中文字符进行UTF-8编码
	 * @param source 要转义的字符串
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String tranformStyle(String source)
	{
		char[] arr = source.toCharArray();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < arr.length; i++)
		{
			char temp = arr[i];
			if(isChinese(temp))
			{
				try {
					sb.append(URLEncoder.encode("" + temp, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				continue;
			}
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	/**
	 * 判断是不是中文字符
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c)
	{
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if(ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
			|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
			|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
			|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
			|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
			|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS)
		{
			return true;
		}
		return false;
	}
}

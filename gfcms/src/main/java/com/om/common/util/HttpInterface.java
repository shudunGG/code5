package com.om.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpInterface {
	public static Log log = LogFactory.getLog(HttpInterface.class);

    private final static int CONNECT_TIMEOUT = 5000; // in milliseconds
    private final static String DEFAULT_ENCODING = "UTF-8";

    public static String UrlBossIntfHead = "https://partner.cmccgs.cn/openapi/V1/partner/ability/sandbox/";
	//https://partner.cmccgs.cn/openapi/V1/partner/ability/


	public String doPostJson(String url,JSONObject jsonParam)throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		String strResult = "";
		try {
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(3000).setSocketTimeout(300000).build();
			httppost.setConfig(requestConfig);
			
			
			StringEntity entity = new StringEntity(jsonParam.toString(),"utf-8");//解决中文乱码问题
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");    
            httppost.setEntity(entity);    
            log.info("doPostJson:url:"+url);
           
            //httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,300000);
			HttpResponse response = httpclient.execute(httppost);
			log.info("doPostJson:response:"+response+"/"+response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == 200) {
				// 请求结束，返回结果  
				strResult = EntityUtils.toString(response.getEntity());
				return strResult;

			} else {
				String err = response.getStatusLine().getStatusCode() + "";
				strResult += "发送失败:" + err;
				throw new Exception(strResult);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public static String doPost(String url,String paramName,String paramValue)throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		String strResult = "";
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			/*  这里给出如果是json格式的参数的话，如何去封装出这个json格式的字符串
			JSONObject jobj = new JSONObject();
			jobj.put("uid", uid);
			jobj.put("title", title);
			jobj.put("content", content);
			nameValuePairs.add(new BasicNameValuePair("msg",getStringFromJson(jobj)));
			*/
			nameValuePairs.add(new BasicNameValuePair(paramName,paramValue));
			
			httppost.addHeader("Content-type","application/x-www-form-urlencoded");
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

			HttpResponse response = httpclient.execute(httppost);
			if (response.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据 */
				strResult = EntityUtils.toString(response.getEntity());
				/* 这里给出返回结果如果是json格式的字符串，给出转换的方法
				JSONObject sobj = new JSONObject();
				sobj = sobj.fromObject(conResult);
				String result = sobj.getString("result");
				String code = sobj.getString("code");
				if (result.equals("1")) {
					strResult += "发送成功";
				} else {
					strResult += "发送失败，" + code;
				}*/
				return strResult;

			} else {
				String err = response.getStatusLine().getStatusCode() + "";
				strResult += "发送失败:" + err;
				throw new Exception(strResult);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
			throw e;
		}
	}

    public String doPostJsonFangCun(String url,JSONObject jsonParam)throws Exception {
	    StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept","*/*");
            conn.setRequestProperty("connection","Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Charset","utf-8");
            conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
            out.write(jsonParam.toJSONString());
            out.flush();
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line;
            while((line = in.readLine())!=null){
                result.append(line);
            }
            in.close();
            return result.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public String doGetJsonFangCun(String url)throws Exception {
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();

            conn.setRequestMethod("GET");
//            conn.setRequestProperty("accept","*/*");
//            conn.setRequestProperty("connection","Keep-Alive");
//            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            conn.setRequestProperty("Charset","utf-8");
//            conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            conn.setDoOutput(true);
            //conn.setDoInput(true);
            //conn.connect();

           /* OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
            out.write(jsonParam.toJSONString());
            out.flush();
            out.close();*/
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line;
            while((line = in.readLine())!=null){
                result.append(line);
            }
            in.close();
            return result.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public static String httpGet(String url,String param) throws Exception {
	    return httpGet(url+"?"+param);
    }

    public static String httpGet(String url) throws Exception {
        log.info("http get url:"+url);
	    String charset = "UTF-8";
        String json = null;
        HttpGet httpGet = new HttpGet();
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            httpGet.setURI(new URI(url));
        } catch (URISyntaxException e) {
            throw new HttpException("请求url格式错误。" + e.getMessage());
        }
// 发送请求
        HttpResponse httpResponse = client.execute(httpGet);
// 获取返回的数据
        HttpEntity entity = httpResponse.getEntity();
        byte[] body = EntityUtils.toByteArray(entity);
        StatusLine sL = httpResponse.getStatusLine();
        int statusCode = sL.getStatusCode();
        if (statusCode == 200) {
            json = new String(body, charset);
            entity.consumeContent();
        } else {
            throw new HttpException("statusCode=" + statusCode);
        }
        return json;
    }


	public static String sendGet(String urlNameString) {
		String result = "";
        BufferedReader in = null;
        try {
            log.info(urlNameString);
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            log.info("sendGet openConnection success!"+realUrl);
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            log.info("sendGet connect success!");
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                log.debug("sendGet           "+key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            log.info("sendGet resp:"+result);
        } catch (Exception e) {
            log.error("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
	}
	 /**
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        return sendGet(url + "?" + param);
    }
	
	
    
    
    public String getRequest(String url,int timeOut) throws Exception{
        URL u = new URL(url);
        if("https".equalsIgnoreCase(u.getProtocol())){
            SslUtils.ignoreSsl();
        }
        URLConnection conn = u.openConnection();
        conn.setConnectTimeout(timeOut);
        conn.setReadTimeout(timeOut);
        return IOUtils.toString(conn.getInputStream());
    }
     
    public String postRequest(String urlAddress,String args,int timeOut) throws Exception{
        URL url = new URL(urlAddress);
        if("https".equalsIgnoreCase(url.getProtocol())){
            SslUtils.ignoreSsl();
        }
        URLConnection u = url.openConnection();
        u.setDoInput(true);
        u.setDoOutput(true);
        u.setConnectTimeout(timeOut);
        u.setReadTimeout(timeOut);
        OutputStreamWriter osw = new OutputStreamWriter(u.getOutputStream(), "UTF-8");
        osw.write(args);
        osw.flush();
        osw.close();
        u.getOutputStream();
        return IOUtils.toString(u.getInputStream());
    }


    public static String postData(String urlStr, String data, String contentType){
        BufferedReader reader = null;
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(CONNECT_TIMEOUT);
            if(contentType != null)
                conn.setRequestProperty("content-type", contentType);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), DEFAULT_ENCODING);
            if(data == null)
                data = "";
            writer.write(data);
            writer.flush();
            writer.close();

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), DEFAULT_ENCODING));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
            }
            return sb.toString();
        } catch (IOException e) {
            //logger.error("Error connecting to " + urlStr + ": " + e.getMessage());
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
            }
        }
        return null;
    }
}

package com.om.common.util.wx;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtil {
    protected static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    /**
     * 发送 get请求
     * 参考博客 http://blog.csdn.net/zmx729618/article/details/51799886
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static String get(String url) throws ClientProtocolException, IOException, ParseException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            //httpget.addHeader("Accept-Language:zh-CN", "zh;q=0.8");

            //setConnectTimeout：设置连接超时时间，单位毫秒。setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(15000).build();
            httpget.setConfig(defaultRequestConfig);

            System.out.println("executing request " + httpget.getURI());

            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);

            System.out.println("got response");

            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                //System.out.println("--------------------------------------");
                // 打印响应状态
                //System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    //System.out.println("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    return EntityUtils.toString(entity, "utf-8");
                }
                //System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String get(String url,int retry) throws ClientProtocolException, IOException, ParseException{
        Exception exp = null;
        for (int i = 0; i < retry; i++) {
            try {
                return get(url);
            } catch (ClientProtocolException e) {
                exp = e;
            } catch (IOException e) {
                exp = e;
            } catch (ParseException e) {
                exp = e;
            }
        }

        if(exp instanceof ClientProtocolException){
            ClientProtocolException t = (ClientProtocolException) exp;
            throw t;
        }else if(exp instanceof IOException){
            IOException t = (IOException) exp;
            throw t;
        }else{
            ParseException t = (ParseException) exp;
            throw t;
        }
    }

    public static String get(Map<String,String> header,String url,int retry) throws ClientProtocolException, IOException, ParseException{
        Exception exp = null;
        for (int i = 0; i < retry; i++) {
            try {
                return get(header,url);
            } catch (ClientProtocolException e) {
                exp = e;
            } catch (IOException e) {
                exp = e;
            } catch (ParseException e) {
                exp = e;
            }
        }

        if(exp instanceof ClientProtocolException){
            ClientProtocolException t = (ClientProtocolException) exp;
            throw t;
        }else if(exp instanceof IOException){
            IOException t = (IOException) exp;
            throw t;
        }else{
            ParseException t = (ParseException) exp;
            throw t;
        }
    }

    public static String get(Map<String,String> header,String url) throws ClientProtocolException, IOException, ParseException{

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);

            if(header != null){
                for(Entry<String, String> param : header.entrySet()){
                    httpget.addHeader(param.getKey(), param.getValue());
                }
            }

            //setConnectTimeout：设置连接超时时间，单位毫秒。setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(15000).build();
            httpget.setConfig(defaultRequestConfig);

            System.out.println("executing request " + httpget.getURI());

            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);

            System.out.println("got response");

            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                //System.out.println("--------------------------------------");
                // 打印响应状态
                //System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    //System.out.println("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    return EntityUtils.toString(entity, "utf-8");
                }
                //System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static byte[] getBytes(String url,int retry) throws ClientProtocolException, IOException, ParseException{
        Exception exp = null;
        for (int i = 0; i < retry; i++) {
            try {
                return getBytes(url);
            } catch (ClientProtocolException e) {
                exp = e;
            } catch (IOException e) {
                exp = e;
            } catch (ParseException e) {
                exp = e;
            }
        }

        if(exp instanceof ClientProtocolException){
            ClientProtocolException t = (ClientProtocolException) exp;
            throw t;
        }else if(exp instanceof IOException){
            IOException t = (IOException) exp;
            throw t;
        }else{
            ParseException t = (ParseException) exp;
            throw t;
        }
    }

    public static byte[] getBytes(String url) throws ClientProtocolException, IOException, ParseException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            // 创建httpget.
            HttpGet httpget = new HttpGet(url);
            //httpget.addHeader("Accept-Language:zh-CN", "zh;q=0.8");

            //setConnectTimeout：设置连接超时时间，单位毫秒。setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(15000).build();
            httpget.setConfig(defaultRequestConfig);

            System.out.println("executing request " + httpget.getURI());

            // 执行get请求.
            CloseableHttpResponse response = httpclient.execute(httpget);

            System.out.println("got response");

            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                //System.out.println("--------------------------------------");
                // 打印响应状态
                //System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    //System.out.println("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    return EntityUtils.toByteArray(entity);
                }
                //System.out.println("------------------------------------");
            } finally {
                response.close();
            }
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String postFileMultiPart(String url,Map<String,ContentBody> reqParam) throws ClientProtocolException, IOException{
        return postFileMultiPart(null,url,reqParam);
    }

    public static String postFileMultiPart(Map<String, String> header,String url, Map<String, ContentBody> reqParam) throws ClientProtocolException, IOException{
        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            // 创建httpget.
            HttpPost httppost = new HttpPost(url);

            if(header != null){
                for(Entry<String, String> param : header.entrySet()){
                    httppost.addHeader(param.getKey(), param.getValue());
                }
            }

            //setConnectTimeout：设置连接超时时间，单位毫秒。setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(15000).build();
            httppost.setConfig(defaultRequestConfig);

            logger.info("executing request " + httppost.getURI());

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            for(Entry<String,ContentBody> param : reqParam.entrySet()){
                multipartEntityBuilder.addPart(param.getKey(), param.getValue());
            }
            HttpEntity reqEntity = multipartEntityBuilder.build();
            httppost.setEntity(reqEntity);

            // 执行post请求.
            CloseableHttpResponse response = httpclient.execute(httppost);

            logger.info("got response");

            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                //System.out.println("--------------------------------------");
                // 打印响应状态
                //System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    //System.out.println("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    return EntityUtils.toString(entity,Charset.forName("UTF-8"));
                }
                //System.out.println("------------------------------------");
            } finally {
                response.close();

            }
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String post(String url,Map<String,String> reqParam) throws ClientProtocolException, IOException{
        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            // 创建httppost.
            HttpPost httppost = new HttpPost(url);

            //setConnectTimeout：设置连接超时时间，单位毫秒。setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(15000).build();
            httppost.setConfig(defaultRequestConfig);

            System.out.println("executing request " + httppost.getURI());

            //装填参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if(reqParam !=null ){
                for (Entry<String, String> entry : reqParam.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(nvps,"utf-8");
            httppost.setEntity(reqEntity);

            // 执行post请求.
            CloseableHttpResponse response = httpclient.execute(httppost);

            System.out.println("got response");

            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                //System.out.println("--------------------------------------");
                // 打印响应状态
                //System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    //System.out.println("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    return EntityUtils.toString(entity,Charset.forName("UTF-8"));
                }
                //System.out.println("------------------------------------");
            } finally {
                response.close();

            }
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static JSONObject post4cookie(String url,JSONObject reqParam) throws ClientProtocolException, IOException{
        CookieStore cookieStore = new BasicCookieStore();
        //CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

        try {
            // 创建httppost.
            HttpPost httppost = new HttpPost(url);

            //setConnectTimeout：设置连接超时时间，单位毫秒。setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(5000).setSocketTimeout(15000).build();
            httppost.setConfig(defaultRequestConfig);

            System.out.println("executing request " + httppost.getURI());

            //装填参数
            StringEntity reqEntity = new StringEntity(reqParam.toString(), Charset.forName("UTF-8"));
            reqEntity.setContentType("application/json");
            httppost.setEntity(reqEntity);

            // 执行post请求.
            CloseableHttpResponse response = httpclient.execute(httppost);

            System.out.println("got response");

            try {
                // 获取响应实体
                HttpEntity entity = response.getEntity();
                //System.out.println("--------------------------------------");
                // 打印响应状态
                //System.out.println(response.getStatusLine());
                if (entity != null) {
                    // 打印响应内容长度
                    //System.out.println("Response content length: " + entity.getContentLength());
                    // 打印响应内容
                    JSONObject res = new JSONObject();
                    String httpRes = EntityUtils.toString(entity,Charset.forName("UTF-8"));
                    res.put("httpRes", httpRes);

                    JSONObject cookie = new JSONObject();
                    List<Cookie> cookies = cookieStore.getCookies();
                    for (int i = 0; i < cookies.size(); i++) {
                        cookie.put(cookies.get(i).getName(), cookies.get(i).getValue());
                    }
                    res.put("cookie", cookie);
                    return res;
                }
                //System.out.println("------------------------------------");
            } finally {
                response.close();

            }
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

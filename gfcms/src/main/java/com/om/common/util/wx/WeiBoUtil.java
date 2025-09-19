package com.om.common.util.wx;

import com.alibaba.fastjson.JSONObject;

import com.om.common.util.DateUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
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
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 微博平台通用接口工具类
 *
 * @author liuxj
 * @date 2013-08-09
 */
public class WeiBoUtil {
    protected static Logger logger = LoggerFactory.getLogger(WeiBoUtil.class);
    private static Map<String,AccessToken> tokenMap = new HashMap<String,AccessToken>();

    // 获取code的接口地址（GET）
    public final static String oauth_authorize_url = "https://api.weibo.com/oauth2/authorize";
    // 获取access_token的接口地址（GET） 限200（次/天）
    public final static String access_token_url = "https://api.weibo.com/oauth2/access_token";

    //分享一条链接到微博
    public static String weibo_share_url = "https://api.weibo.com/2/statuses/share.json";

    //获取当前登录用户及其所关注（授权）用户的最新微博
    public static String weibo_view_list_url = "https://api.weibo.com/2/statuses/home_timeline.json";



    /**
     * @deprecated 通过页面实现，无法通过接口实现
     * 在浏览器内请求 oauth2/authorize 接口
     * @param appid
     * @param redirectUri
     * @return
     */
    public static String makeVisitCodeUrl(String appid, String redirectUri) {
        String authUrl = oauth_authorize_url+"?client_id="+appid+"&redirect_uri="+redirectUri;
        logger.debug("makeVisitCodeUrl:"+authUrl);
        return authUrl;
    }

    /**
     * @deprecated 通过页面实现，无法通过接口实现
     * 主要目的是为了测试一下code有没有第二种方法
     */
    public static JSONObject makeVisitCode(String appid, String redirectUri) {
        String url = access_token_url;

        Map paramMap = new HashMap();
        paramMap.put("client_id",appid);
        paramMap.put("redirect_uri",redirectUri);
        JSONObject reqJson =new JSONObject(paramMap);
        String jsonContent = reqJson.toJSONString();
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);

        return jsonObject;
    }


    /**
     * 获取access_token
     *
     * @param appid 凭证
     * @param appsecret 密钥
     * @return
     */
    private static AccessToken getAccessToken(String appid, String appsecret,String code,String redirectUri) {
        AccessToken accessToken = tokenMap.get(appid);
        if(accessToken!=null){
            long expireTime = accessToken.getExpireTime();
            if(System.currentTimeMillis() < expireTime) {
                return accessToken;
            }
        }

        String requestUrl = access_token_url;

        HashMap<String, String> p = new HashMap<String, String>();
        p.put("client_id",appid);
        p.put("client_secret",appsecret);
        p.put("grant_type","authorization_code");
        p.put("redirect_uri",redirectUri);
        p.put("code",code);

        logger.debug("getAccessToken requestUrl:"+requestUrl);
        String rs = null;
        try{
            rs = postXwwFormUrlEncoded(requestUrl, p);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
//        JSONObject jsonObject = httpRequest(requestUrl, "POST", jsonContent);
        logger.debug("getAccessToken rs:"+rs);
        // 如果请求成功
        if (null != rs) {
            JSONObject jsonObject =  JSONObject.parseObject(rs);
            try {
                accessToken = new AccessToken();
                accessToken.setToken(jsonObject.getString("access_token"));
                accessToken.setExpiresIn(jsonObject.getIntValue("expires_in"));
                tokenMap.put(appid,accessToken);
            } catch (Exception e) {
                accessToken = null;
                // 获取token失败
                logger.error("获取token失败 errcode:{"+jsonObject.getIntValue("errcode")+"} errmsg:{"+jsonObject.getString("errmsg")+"}");
            }
        }
        return accessToken;
    }




    /**
     * 分享一条链接到微博
     * @return 0表示成功，其他值表示失败
     */
    public static Map weiboShare(String appid, String appsecret,String code,String redirectUri,File file,MultipartFile mf,String content,String ip,String rootPath) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret,code,redirectUri);
        String token = accessToken.getToken();
        logger.debug("file:"+file+" mf:"+mf+" token:"+token+ " code:"+code+" redirectUri:"+redirectUri);
        // 拼装创建菜单的url
        String url = weibo_share_url;
        content = URLEncoder.encode(content + redirectUri);
//        String params = "access_token=" + token +
//                "&status=" + content +
//                "&rip=" + ip;

//        url = url +"?" + params;

//        HashMap<String, String> p = new HashMap<String, String>();
//        p.put("access_token",token);
//        p.put("status",content);
//        p.put("rip",ip);


        // 将菜单对象转换成json字符串
        // 调用接口创建菜单
      /*  JSONObject jsonObject = null;
        if (file != null) {
            jsonObject = WxImageUpLoad.uploadFile(url, file, null);
        }else if (mf != null) {
            jsonObject = WxImageUpLoad.uploadFile(url, null, mf);
        }else{

            String rs = null;
            try{
                rs = postXwwFormUrlEncoded(url, p);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
            jsonObject =  JSONObject.parseObject(rs);

            //jsonObject = httpRequest(url, "POST", params);
        }*/


       /* String url = "xxxxxxx";
        String httpRes = null;
        String localFileName = "E:/2.jpg";
        httpRes = HttpClientUtil.postFileMultiPart(url,reqParam);*/

        JSONObject jsonObject = null;
        Map<String, ContentBody> reqParam = new HashMap<String, ContentBody>();
        if (file != null) {
            reqParam.put("filename", new StringBody(mf.getOriginalFilename(), ContentType.MULTIPART_FORM_DATA));
            reqParam.put("filelength", new StringBody(mf.getSize()+"", ContentType.MULTIPART_FORM_DATA));
            reqParam.put("fileSize", new StringBody(mf.getSize()+"", ContentType.MULTIPART_FORM_DATA));
            reqParam.put("file", new FileBody(file));
        }else if (mf != null) {
            String sep = "/";
            String contextPath = DateUtil.getCurMonth();
            String currDir = rootPath +"wb/"+ contextPath + sep;
            logger.info("文件上传路径为[file upload path]:" + currDir);
            File ff = new File(currDir);
            if (!ff.exists()) ff.mkdirs();

            String file_init = mf.getOriginalFilename(); //得到文件名称
            String fix = file_init.substring(file_init.lastIndexOf(".")+1, file_init.length());
            String file_new = System.currentTimeMillis() +"."+ fix; //生成一个新的文件名称
            String dstPathAndFile = currDir + sep + file_new;
            File file1 = new File(dstPathAndFile); // 新建一个文件
            mf.transferTo(file1);


            //sb.append("Content-Disposition:form-data;name=\"media\";filename=\"" + fileName + "\";filelength=\"" + fileSize + "\"\r\n");
            reqParam.put("filename", new StringBody(mf.getOriginalFilename(), ContentType.MULTIPART_FORM_DATA));
            reqParam.put("filelength", new StringBody(mf.getSize()+"", ContentType.MULTIPART_FORM_DATA));
            reqParam.put("fileSize", new StringBody(mf.getSize()+"", ContentType.MULTIPART_FORM_DATA));
            reqParam.put("file", new FileBody(file1));
        }

        reqParam.put("access_token", new StringBody(token, ContentType.MULTIPART_FORM_DATA));
        reqParam.put("status", new StringBody(content, ContentType.MULTIPART_FORM_DATA));
        reqParam.put("rip", new StringBody(ip, ContentType.MULTIPART_FORM_DATA));

        String param = "access_token="+token+"&status="+URLEncoder.encode(content,"UTF-8")+"&rip=182.92.208.161";
        url = url+"?"+param;
        logger.debug("httpRequestForm url:"+url);
//        jsonObject = httpRequestForm(url, mf);

        Map<String, String> header= new HashMap<String, String>();
        if(mf!=null){
            header.put("Content-type","multipart/form-data");
        }else{
            header.put("Content-type","application/x-www-form-urlencoded");
        }

        String rs = HttpClientUtil.postFileMultiPart(header,url,reqParam);
        //httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        if(rs!=null){
            jsonObject =  JSONObject.parseObject(rs);
        }

        logger.debug("back uploadRes:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 分获取当前登录用户及其所关注（授权）用户的最新微博
     * @return 0表示成功，其他值表示失败
     */
    public static Map getWeiboList(String appid, String appsecret,String code,String redirectUri,int count,int page) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret,code,redirectUri);
        String token = accessToken.getToken();


        // 拼装创建菜单的url
        String url = weibo_view_list_url;
        String param = "access_token="+token+"&count="+count+"&page="+page;
        url = url+"?"+param;
        logger.debug("httpRequestForm url:"+url);
        JSONObject jsonObject = httpRequest(url,"GET", null);

        logger.debug("back getWeiboList:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }
    //


















    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl 请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr 提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();

            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            int responseCode = httpUrlConn.getResponseCode();
            logger.debug("responseCode:"+responseCode+" str:"+buffer.toString());
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (ConnectException ce) {
            logger.error(ce.getMessage(),ce);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return jsonObject;
    }


    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl 请求地址
     * @param file 提交的文件
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequestForm(String requestUrl, File file) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);





            httpUrlConn.setRequestMethod("POST");
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            httpUrlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpUrlConn.setDoOutput(true);

            // 构建请求体
            try (OutputStream output = httpUrlConn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                // 文件部分
                //File file = new File("path/to/file.jpg");
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: ").append(HttpsURLConnection.guessContentTypeFromName(file.getName())).append("\r\n\r\n");
                writer.append("filelength: ").append(file.length()+"").append("\r\n\r\n");
                writer.flush();


                try (FileInputStream input = new FileInputStream(file)) {
                    byte[] bf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = input.read(bf)) != -1) {
                        output.write(bf, 0, bytesRead);
                    }
                    output.flush();
                }

                // 其他参数（可选）
                writer.append("\r\n--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"param1\"\r\n\r\n");
                writer.append("value1\r\n");

                // 结束边界
                writer.append("--").append(boundary).append("--\r\n");
            }

            // 将返回的输入流转换成字符串
            int responseCode = httpUrlConn.getResponseCode();
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 多态方法，同httpRequestForm
     *
     * @param requestUrl 请求地址
     * @param mf 提交的文件
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequestForm(String requestUrl, MultipartFile mf) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);

            httpUrlConn.setRequestMethod("POST");
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            httpUrlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpUrlConn.setDoOutput(true);

            // 构建请求体
            try (OutputStream output = httpUrlConn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                // 文件部分
                //File file = new File("path/to/file.jpg");
                logger.debug("========:"+mf.getOriginalFilename()+"   Content-Type:"+HttpsURLConnection.guessContentTypeFromName(mf.getOriginalFilename())+" size:"+mf.getSize());
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(mf.getOriginalFilename()).append("\"\r\n");
                writer.append("Content-Type: ").append(HttpsURLConnection.guessContentTypeFromName(mf.getOriginalFilename())).append("\r\n\r\n");
                writer.append("filelength: ").append(mf.getSize()+"").append("\r\n\r\n");
                writer.flush();


                try (InputStream input = mf.getInputStream()) {
                    byte[] bf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = input.read(bf)) != -1) {
                        output.write(bf, 0, bytesRead);
                    }
                    output.flush();
                }

                // 其他参数（可选）
                writer.append("\r\n--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"param1\"\r\n\r\n");
                writer.append("value1\r\n");

                // 结束边界
                writer.append("--").append(boundary).append("--\r\n");
            }

            // 将返回的输入流转换成字符串
            int responseCode = httpUrlConn.getResponseCode();
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            logger.debug("responseCode:"+responseCode+" str:"+buffer.toString());
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }





    public static String postXwwFormUrlEncoded(String url, HashMap<String, String> paraMap) throws IOException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        String msg = "";
        try {
            if (paraMap != null && paraMap.size() != 0) {
                for (Map.Entry<String, String> entry : paraMap.entrySet()) {
                    params.add(new NameValuePair() {
                        @Override
                        public String getName() {
                            return entry.getKey();
                        }
                        @Override
                        public String getValue() {
                            return entry.getValue();
                        }
                    });
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            HttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (HttpURLConnection.HTTP_OK == statusCode) {
                HttpEntity entity = response.getEntity();
                msg = EntityUtils.toString(entity);
            }else {
                return null;
            }
        } catch (IOException e) {
            throw e;
        }
        return msg;
    }







}
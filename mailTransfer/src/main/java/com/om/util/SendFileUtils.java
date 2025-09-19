package com.om.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 *
 * @author 花鼠大师
 * @version :1.0
 * @date 2024/4/12 15:10
 */
@Slf4j
public class SendFileUtils {

    /**
     * 使用multipart/form-data方式传输文件
     * 发送文件方法
     * @param url 接口地址
     * @param file 文件
     */
    public static String sendMultipartFile(String url,Map m, File file) {
        //获取HttpClient
        CloseableHttpClient client = getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        fillMethod(httpPost,System.currentTimeMillis());

        // 请求参数配置
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000)
                .setConnectionRequestTimeout(10000).build();
        httpPost.setConfig(requestConfig);
        String res = "";
        String fileName = file.getName();//文件名
        try {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(java.nio.charset.Charset.forName("UTF-8"));
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            /**
             * 假设有两个参数需要传输
             * 参数名:filaName 值 "文件名"
             * 参数名:file 值:file (该参数值为file对象)
             */
            //表单中普通参数
            if(m!=null){
                Iterator it = m.keySet().iterator();
                while(it.hasNext()){
                    String key = (String)it.next();
                    String value = (String)m.get(key);
                    builder.addPart(key,new StringBody(value, ContentType.create("text/plain", Consts.UTF_8)));
                }
            }


            // 表单中的文件参数 注意，builder.addBinaryBody的第一个参数要写参数名
            builder.addBinaryBody("file", file, ContentType.create("multipart/form-data",Consts.UTF_8), fileName);

            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);
            HttpResponse response = client.execute(httpPost);// 执行提交

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 返回响应结果
                res = EntityUtils.toString(response.getEntity(), java.nio.charset.Charset.forName("UTF-8"));
            }else {
                log.error("响应失败！");
            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用HttpPost失败！" + e.toString());
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    log.error("关闭HttpPost连接失败！");
                }
            }
        }
        log.info("数据传输成功!!!!!!!!!!!!!!!!!!!!");
        return res;
    }
    /**
     * 获取HttpClient
     * @return
     */
    private static CloseableHttpClient getHttpClient(){
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient client = HttpClientBuilder.create().setSSLSocketFactory(sslConnectionSocketFactory).build();
        return client;
    }

    /**
     * 添加头文件信息
     * @param requestBase
     * @param timestamp
     */
    private static void fillMethod(HttpRequestBase requestBase, long timestamp){
        //此处为举例，需要添加哪些头部信息自行添加即可

        //设置时间戳,nginx,underscores_in_headers on;放到http配置里，否则nginx会忽略包含"_"的头信息
        requestBase.addHeader("timestamp",String.valueOf(timestamp));
        System.out.println(requestBase.getAllHeaders());
    }
}



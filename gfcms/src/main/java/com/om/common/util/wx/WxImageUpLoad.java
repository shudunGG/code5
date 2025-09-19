package com.om.common.util.wx;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.apache.hc.client5.http.entity.mime.ContentBody;
import com.aliyun.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import com.aliyun.apache.hc.core5.http.HttpEntity;
import com.om.module.controller.wx.WxEnterController;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WxImageUpLoad {
    protected static Logger logger = LoggerFactory.getLogger(WxImageUpLoad.class);




    /**
     * 新增临时素材
     *
     * @param file    文件
     * @return
     * @throws Exception
     */
    public static JSONObject uploadFile(String url, File file, MultipartFile mf ) throws Exception {
        URL urlObj = new URL(url);
        String fileName = null;
        long fileSize = 0;
        InputStream input = null;
        if(file!=null){
            fileName = file.getName();
            fileSize = file.length();
            input = new FileInputStream(file);
        }

        if(mf!=null){
            fileName = mf.getOriginalFilename();
            fileSize = mf.getSize();
            input = mf.getInputStream();
        }



        // 创建Http连接
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);   // 使用post提交需要设置忽略缓存

        //消息请求头信息
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");

        //设置边界
        String BOUNDARY = "----------" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

        StringBuilder sb = new StringBuilder();
        sb.append("--");    // 必须多两道线
        sb.append(BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition:form-data;name=\"media\";filename=\"" + fileName + "\";filelength=\"" + fileSize + "\"\r\n");
        sb.append("Content-Type:application/octet-stream\r\n\r\n");

        byte[] head = sb.toString().getBytes("utf-8");

        // 创建输出流
        OutputStream out = new DataOutputStream(conn.getOutputStream());
        // 获得输出流表头
        out.write(head);

        //文件正文部分
        DataInputStream in = new DataInputStream(input);
        int bytes = 0;
        byte[] bufferOut = new byte[1024 * 1024 * 10]; // 10M
        while ((bytes = in.read(bufferOut)) != -1) {
            out.write(bufferOut, 0, bytes);
        }
        in.close();

        //结尾
        byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");
        out.write(foot);
        out.flush();
        out.close();


        // 获取响应
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        String result = null;
        try {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            if (result == null) {
                result = buffer.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }

        JSONObject json = JSONObject.parseObject(result);
        logger.debug("WxImageUpLoad.uploadfile:"+json.toJSONString());
        return json;
        /*System.out.println(json);
        String mediaId = "";
        if (!type.equals("image")) {
            mediaId = json.getString(type + "_media_id");
        } else {
            mediaId = json.getString("media_id");
        }
        return mediaId;*/
    }

}

package com.om.common.util.wx;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * @author liuxj
 * @title
 * @description
 * @date 2022/11/28
 **/
public class FormDataInterFaceUtils {
    //前缀
    public static String PREFIX = "--";
    //换行符
    public static String ROW = "\r\n";
    //产生一个边界
    static String BOUNDARY = UUID.randomUUID().toString().replaceAll("-" , "");

    /**
     * @description 将map里的表单信息 写入到所给的url请求中 并返回执行完请求的结果
     * @author liuxj
     * @date 9:38 2022/11/28 
     * @param url 所给的请求地址
     * @param map 参数的键值对映射 使用泛型 文件和字符参数都以对象表示
     * @return java.lang.String
     **/
    public static String doPost(String url , Map<String , Object> map) {
        //构造连接
        try {
            HttpURLConnection httpCon = getPostConnection(url);
            DataOutputStream outputStream = new DataOutputStream(httpCon.getOutputStream());
            //部分的三方请求可能需要携带类似于token这样的信息到请求头里才可以正常访问
            //可以使用setRequestProperty(键,值)来设置
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object o = entry.getValue();
                if(o instanceof String) {
                    //强转
                    String str = (String) o;
                    //添加键值对
                    addKeyString(outputStream , entry.getKey() , str);
                    int i = httpCon.getContentLength();
                }else  if(o instanceof MultipartFile){
                    //否则就是文件流
                    MultipartFile file = (MultipartFile) o;
                    //添加文件
                    addFile(outputStream , entry.getKey() , file);
                }else {
                    //否则就是文件流
                    File file = (File) o;
                    //添加文件
                    addFile(outputStream , entry.getKey() , file);
                }
            }
            //写入边界结束符
            outputStream.write((PREFIX + BOUNDARY + PREFIX + ROW).getBytes(StandardCharsets.UTF_8));
            outputStream.flush();//可以理解为发送请求
            //获取返回结果 -- 默认为字符串
            return getInvokeResult(httpCon);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description 写入键值对  示例为写入：name-张三
     * @author liuxj
     * @date 9:40 2022/11/28
     * @param out 请求的输出流
     * @param key 字符的键
     * @param str 字符的值
     * @return void
     **/
    public static void addKeyString(DataOutputStream out,
                                    String key ,
                                    String str) {
        try{
            StringBuilder stringBuilder = new StringBuilder();
            //先写入数据的边界标识
            stringBuilder.append(PREFIX).append(BOUNDARY).append(ROW);
            stringBuilder.append("Content-Disposition: form-data; name=\"")
                    .append(key).append("\"").append(ROW);
            //数据类型及编码
            stringBuilder.append("Content-Type: text/plain; charset=UTF-8");
            //Todo 连续两个换行符 表示文字的键信息部分结束
            stringBuilder.append(ROW).append(ROW);
            //写入信息的值
            stringBuilder.append(str);
            //表示数据的结尾
            stringBuilder.append(ROW);
            //写入数据 键值对一起写入
            out.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @description 向输出流中写入文件 示例为: a.txt - 对应的File对象
     * @author liuxj
     * @date 9:42 2022/11/28
     * @param out 请求的输出流
     * @param name 文件的键
     * @param file 具体文件
     * @return void
     **/
    public static void addFile(DataOutputStream out , String name ,
                               File file) throws IOException {
        if(!file.exists())
            System.out.println("文件不存在");
        StringBuilder stringBuilder = new StringBuilder();
        //标识这是一段边界内的数据
        stringBuilder.append(PREFIX).append(BOUNDARY).append(ROW);
        //拼接文件名称
        stringBuilder.append("Content-Disposition: form-data; name=\"");
        stringBuilder.append(name).append("\"; ")//文件的键
                .append("filename=\"")//文件名称
                .append(file.getName())
                .append("\"; ")
                .append("filelength=")
                .append(file.length())
                .append(ROW)
                //设置内容类型为流及编码为UTF-8
                .append("Content-Type: application/octet-stream; charset=UTF-8");

        //Todo 这两个换行很重要 标识文件信息的结束 后面的信息为文件流
        stringBuilder.append(ROW).append(ROW);

        //写入文件的信息到输出流
        out.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        //这里开始写入文件流
        try(
                DataInputStream fileIn = new DataInputStream(new FileInputStream(file))
        ) {
            //一次读取1M
            byte[] bytes = new byte[1024*1024];
            int length = 0;
            while ((length = fileIn.read(bytes)) != -1) {
                out.write(bytes , 0 , length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Todo 文件流写完之后 需要换行表示结束
        out.write(ROW.getBytes(StandardCharsets.UTF_8));
    }

    public static void addFile(DataOutputStream out , String name ,
                               MultipartFile file) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        //标识这是一段边界内的数据
        stringBuilder.append(PREFIX).append(BOUNDARY).append(ROW);
        //拼接文件名称
        stringBuilder.append("Content-Disposition: form-data; name=\"");
        stringBuilder.append(name).append("\"; ")//文件的键
                .append("filename=\"")//文件名称
                .append(file.getOriginalFilename())
                .append("\"; ")
                .append("filelength=")
                .append(file.getSize())
                .append(ROW)
                //设置内容类型为流及编码为UTF-8
                .append("Content-Type: application/octet-stream; charset=UTF-8");

        //Todo 这两个换行很重要 标识文件信息的结束 后面的信息为文件流
        stringBuilder.append(ROW).append(ROW);

        //写入文件的信息到输出流
        out.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        //这里开始写入文件流
        try(
                InputStream input = file.getInputStream();
                DataInputStream fileIn = new DataInputStream(input)
        ) {
            //一次读取1M
            byte[] bytes = new byte[1024*1024];
            int length = 0;
            while ((length = fileIn.read(bytes)) != -1) {
                out.write(bytes , 0 , length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Todo 文件流写完之后 需要换行表示结束
        out.write(ROW.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * @description 以所给的url获取一个Post类型的连接
     * @author liuxj
     * @date 9:43 2022/11/28
     * @param url 请求的地址
     * @return java.net.HttpURLConnection
     **/
    public static HttpURLConnection getPostConnection(String url) {
        HttpURLConnection httpCon = null;
        try {
            URL urlCon = new URL(url);
            //在这里获取的就是一个已经打开的连接了
            httpCon = (HttpURLConnection) urlCon.openConnection();
            //请求方式为Post
            httpCon.setRequestMethod("POST");
            //设置通用的请求属性
            httpCon.setRequestProperty("accept", "*/*");
            httpCon.setRequestProperty("connection", "Keep-Alive");
            //设置浏览器代理
            httpCon.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            //这里要设置为表单类型
            httpCon.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            //是否可读写
            httpCon.setDoOutput(true);
            httpCon.setDoInput(true);
            //禁用缓存
            httpCon.setUseCaches(false);
            //设置连接超时60s
            httpCon.setConnectTimeout(60000);
            //设置读取响应超时60s
            httpCon.setReadTimeout(60000);
        } catch (IOException e ) {
            e.printStackTrace();
        }
        return  httpCon;
    }

    /**
     * @description 从请求中获取请求的执行返回
     * @author liuxj
     * @date 9:44 2022/11/28 
     * @param httpCon 请求的连接
     * @return java.lang.String
     **/
    public static String getInvokeResult(HttpURLConnection httpCon) {
        try(
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpCon.getInputStream()))
        ) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


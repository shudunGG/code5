package com.om.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * @author keying
 * @date 2021/5/10
 */
public class VideoHttpDown {

    /**
     * 下载视频
     * @param videoUrl 实际视频地址
     * @param downloadPath  文件下载地址
     * @param fileName  文件名
     * @param SuffixName  后缀名
     */
    public static void downVideo(String videoUrl, String downloadPath,String fileName, String SuffixName) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;

        //路径名加上文件名加上后缀名 = 整个文件下载路径
        String fullPathName = downloadPath+fileName+"."+SuffixName;

        try {
            // 1.获取连接对象
            URL url = new URL(videoUrl);
            // 获取链接对象，就是靠这个对象来获取流
            connection = (HttpURLConnection) url.openConnection();
            // Range代表读取的范围，bytes=0-代表从0字节到最大字节，意味着读取所有资源
            // connection.setRequestProperty("Range", "bytes=0-");之前人民中科的视频下载时，是打开这一行的，但是下载任圆的视频流的时候不行，只下512K,把这一行注释后就可以了，后面研究这一行的作用
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            // 与网页建立链接，链接成功后就可以获得流；
            connection.connect();
            // 如果建立链接返回的相应代码是200到300间就为成功，否则链接失败,结束函数
            if (connection.getResponseCode() / 100 != 2) {
                String location = connection.getHeaderField("Location");
                URL locationUrl = new URL(location);
                // 获取链接对象，就是靠这个对象来获取流
                connection = (HttpURLConnection) locationUrl.openConnection();
            }
            // 2.获取连接对象的流
            inputStream = connection.getInputStream();
            // 已下载的大小 下载进度
            int downloaded = 0;
            // 总文件的大小
            int fileSize = connection.getContentLength();
            // getFile获取此URL的文件名。返回的文件部分将与getPath（）相同,具体视频链接的文件名字视情况而定
            // String fileName = url.getFile();
            // fileName = fileName.substring(fileName.lastIndexOf("/") + 1);//特殊需要截取文件名字
            // 3.把资源写入文件
            randomAccessFile = new RandomAccessFile(fullPathName, "rw");
            while (downloaded < fileSize) {
                // 3.1设置缓存流的大小
                //判断当前剩余的下载大小是否大于缓存之，如果不大于就把缓存的大小设为剩余的。
                byte[] buffer = null;
                if (fileSize - downloaded >= 2048) {
                    buffer = new byte[2048];
                } else {
                    buffer = new byte[fileSize - downloaded];
                }
                // 3.2把每一次缓存的数据写入文件
                int read = -1;
                int currentDownload = 0;
                long startTime = System.currentTimeMillis();
                // 这段代码是按照缓存的大小，读写该大小的字节。然后循环依次写入缓存的大小，直至结束。
                // 这样的优势在于，不用让硬件频繁的写入，可以提高效率和保护硬盘吧
                while (currentDownload < buffer.length) {
                    read = inputStream.read();
                    buffer[currentDownload++] = (byte) read;
                }
                long endTime = System.currentTimeMillis();
                double speed = 0.0; //下载速度
                if (endTime - startTime > 0) {
                    speed = currentDownload / 1024.0 / ((double) (endTime - startTime) / 1000);
                }
                randomAccessFile.write(buffer);
                downloaded += currentDownload;
                randomAccessFile.seek(downloaded);
                System.out.printf(fullPathName+"下载了进度:%.2f%%,下载速度：%.1fkb/s(%.1fM/s)%n", downloaded * 1.0 / fileSize * 10000 / 100,
                        speed, speed / 1000);
            }

        } catch (MalformedURLException e) {// 具体的异常放到前面
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭资源、连接
                connection.disconnect();
                inputStream.close();
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // write your code here


        String url="http://video.people-ai.net/weibo/?video_id=aHR0cHM6Ly93ZWliby5jb20vdHYvc2hvdy8yMDIwNzcyMDAxOjI4N2U5NmVkMjk1YmY5ZWMwZDQ0YjlhOGEyZjNiNzJmP2Zyb209b2xkX3BjX3ZpZGVvc2hvdw==";
        // url="https://aweme-hl.snssdk.com/aweme/v1/playwm/?video_id=v0300fg10000c8nck1rc77u3af7iaaug&line=0";
        //url="https://aweme-hl.snssdk.com/aweme/v1/playwm/?video_id=v0300fg10000c8nck1rc77u3af7iaaug&line=0";
        //url="http://video.people-ai.net/xigua/?video_id=aHR0cHM6Ly9tLml4aWd1YS5jb20vdmlkZW8vNzA2ODk5MjQxNjI0OTk1NzY0NA==";
        //url ="https://f.video.weibocdn.com/o0/0030YkyGlx07TX5RrmxO01041200422C0E010.mp4?label=mp4_ld&template=360x640.24.0&ori=0&ps=1BThihd3VLAY5R&Expires=1645514957&ssig=HdCacMRqWi&KID=unistore,video";
        // String token="v32Eo2Tw+qWI/eiKW3D8ye7l19mf1NngRLushO6CumLMHIO1aryun0/Y3N3YQCv/TqzaO/TFHw4=";
        // String token="SiGBCH6QblUHs7NiouV09rL6uAA3Sv0cGicaSxJiC/78DoWIMzVbW6VCwwkymYsZaxndDkYqkm4=";
        //HttpDownFile.downLoadFromUrl(url,"abc.mp4","D:\\",null);
        url="http://192.168.120.50:359/storage/5CF58580870DCD1A92FD0A626FE291AE/mp4";
        VideoHttpDown.downVideo(url, "e:\\", "lxj", "mp4");
        System.out.println("下载完成");

    }

}

package com.om.common.util;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * PCM 转 MP3
 *
 * @author liuxj
 * @since 2020-04-02
 */
public class PcmToMp3 {
    private static Logger logger = LoggerFactory.getLogger(PcmToMp3.class);
    /**
     * @param src    待转换文件路径
     * @param target 目标文件路径(这个工具是在源文件的基础上再形成一个文件，target样例：D:/landscape/music/25745664665456.mp3,必须写全否则报io‘无法访问错误’)
     * @throws IOException 抛出异常
     */
    public static String convertAudioFiles(String src, String target) throws IOException {

        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(target);

        //计算长度
        byte[] buf = new byte[1024 * 4];
        int size = fis.read(buf);
        int PCMSize = 0;
        while (size != -1) {

            PCMSize += size;
            size = fis.read(buf);
        }
        fis.close();

        //填入参数，比特率等等。这里用的是16位单声道 8000 hz
        WaveHeader header = new WaveHeader();
        //长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
        header.fileLength = PCMSize + (44 - 8);
        header.FmtHdrLeth = 16;
        header.BitsPerSample = 16;
        header.Channels = 1;
        header.FormatTag = 0x0001;
        header.SamplesPerSec = 16000;
        header.BlockAlign = (short) (header.Channels * header.BitsPerSample / 8);
        header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
        header.DataHdrLeth = PCMSize;

        byte[] h = header.getHeader();

        assert h.length == 44; //WAV标准，头部应该是44字节
        //write header
        fos.write(h, 0, h.length);
        //write data stream
        fis = new FileInputStream(src);
        size = fis.read(buf);
        while (size != -1) {

            fos.write(buf, 0, size);
            size = fis.read(buf);
        }
        fis.close();
        fos.close();
        logger.info("PCM Convert to MP3 OK!"+target);
        return "ok";
    }

    public static void main(String[] args) throws IOException {
        convertAudioFiles("E:\\work\\svn_code\\Java_tts_online1021_2b01405b\\sample\\MscInvisibleDemo\\tts_test.pcm","D:\\tts_test3.mp3");
    }
}


package com.om.module.schedule;


import com.om.common.cache.Dict;
import com.om.common.util.FileCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Description:用于压缩和清理日志的
 * User: Liuxj
 * Date: 2022-4-10
 * Time: 09:59
 */
@Component
@Configuration      //1.用于压缩和清理日志文件的
@EnableScheduling   // 2.开启定时任务
public class CleanLogFile {
    private static Logger logger = LoggerFactory.getLogger(CleanLogFile.class);

    @Autowired
    private Environment env;


    // 后台实体生成定时任务，间隔30秒运行一次，请勿屏蔽
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，一天更新一次
    @Scheduled(fixedRate=86400000)
    private void configureTasks() {

        String CleanLogFileIsStart =   env.getProperty("CleanLogFileIsStart");//总开关，是否开启清理日志文件
        String CleanLogFilePath =   env.getProperty("CleanLogFilePath");    //日志所在的目录
        String CleanLogFileZipDays =   env.getProperty("CleanLogFileZipDays");//压缩保留天数
        String CleanLogFileDelDays =   env.getProperty("CleanLogFileDelDays");//清理文件保留天数
        String CleanLogFilePrefix =   env.getProperty("CleanLogFilePrefix");//日志文件的前缀
        String CleanLogFileSuffix =   env.getProperty("CleanLogFileSuffix");//日志文件的后缀
        String CleanLogFileDateFromat =   env.getProperty("CleanLogFileDateFromat");//日期格式
        if("1".equals(CleanLogFileIsStart)){
            logger.info("begin cleanlogFile path:"+CleanLogFilePath+" zipdays:"+CleanLogFileZipDays+" deldays:"+CleanLogFileDelDays);
            try {
                int zipDays = Integer.parseInt(CleanLogFileZipDays);
                String[] preArr = CleanLogFilePrefix.split(",");
                String[] sufArr = CleanLogFileSuffix.split(",");
                for(String p:preArr){
                    for(String s:sufArr){
                        zipFile(CleanLogFilePath,zipDays,p,s,CleanLogFileDateFromat);
                    }
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }

            try {
                int delDays = Integer.parseInt(CleanLogFileDelDays);
                String[] preArr = CleanLogFilePrefix.split(",");
                String[] sufArr = CleanLogFileSuffix.split(",");
                for(String p:preArr){
                    for(String s:sufArr){
                        delZipFile(CleanLogFilePath,delDays,p,s,CleanLogFileDateFromat);
                    }
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        }
    }

    public void delZipFile(String path,int delDays,String prefix,String suffix,String dateFromat){
        //access-2025-01-01.log
        //access-2025-01-01.log.zip
        try {
            if(dateFromat == null){
                dateFromat = "yyyy-MM-dd";
            }
            Calendar cal = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat(dateFromat);
            cal.add(Calendar.DAY_OF_MONTH,delDays*-1);
            Date dellimitDay = cal.getTime();
            File file = new File(path);
            File[] fileArr = file.listFiles();
            String name = null;
            for(File f:fileArr){
                name = f.getName();
                if(!name.startsWith(prefix)){
                    continue;
                }
                if(!name.endsWith(".zip")){
                    continue;
                }

                name = name.replaceAll(prefix,"");
                name = name.replaceAll(suffix,"");
                name = name.replaceAll(".zip","");
                Date fileDate = null;
                try {
                    fileDate = df.parse(name);
                }catch (Exception e){
                    logger.error(e.getMessage(),e);
                    continue;
                }
                if(fileDate.before(dellimitDay)){
                    logger.info("说明该文件符合ZIP删除条件："+f.getName());
                    boolean rs = f.delete();
                    logger.info("文件【"+f.getName()+"】删除完成！"+rs);
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }

    public void zipFile(String path,int zipDays,String prefix,String suffix,String dateFromat){
        try {
            if(dateFromat == null){
                dateFromat = "yyyy-MM-dd";
            }
            Calendar cal = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat(dateFromat);
            cal.add(Calendar.DAY_OF_MONTH,zipDays*-1);
            Date ziplimitDay = cal.getTime();
            File file = new File(path);
            File[] fileArr = file.listFiles();
            String name = null;
            for(File f:fileArr){
                name = f.getName();
                if(!name.startsWith(prefix)){
                    continue;
                }
                if(!name.endsWith(suffix)){
                    continue;
                }
                name = name.replaceAll(prefix,"");
                name = name.replaceAll(suffix,"");
                Date fileDate = null;
                try {
                    fileDate = df.parse(name);
                }catch (Exception e){
                    logger.error(e.getMessage(),e);
                    continue;
                }

                if(fileDate.before(ziplimitDay)){
                    logger.info("说明该文件符合压缩条件："+f.getName());
                    //压缩并删除该文件
                    String newName = f.getPath()+".zip";
                    File outputFile = new File(newName);
                    FileCompressor.compressFile(f, outputFile);
                    logger.info("文件【"+f.getName()+"】压缩完成！");
                    boolean rs = f.delete();
                    logger.info("文件【"+f.getName()+"】删除完成！"+rs);
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }





}

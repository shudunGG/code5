package com.om.module.schedule;

import com.om.module.service.busi.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * Description:用于金川信访接口转发的
 * User: Liuxj
 * Date: 2022-4-10
 * Time: 09:59
 */
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class AutoRunProcTask {
    private static Logger logger = LoggerFactory.getLogger(AutoRunProcTask.class);

    @Autowired
    private Environment env;

    @Resource(name = "MailService")
    private MailService mailService;


    private String nodeDesc = null;

    // 后台实体生成定时任务，间隔30秒运行一次，请勿屏蔽
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=60000)
    private void configureTasks() {
        logger.info("start scan toProcess appMsg");
        String isJinChuanRun =   env.getProperty("isJinChuanRun");
        try {
            if("0".equals(isJinChuanRun)){
                return;
            }
            String url_get_token =   env.getProperty("url_get_token");
            String url_save_main =   env.getProperty("url_save_main");
            String appId =   env.getProperty("appId");
            String appKey =   env.getProperty("appKey");
            String jmkey =   env.getProperty("jmkey");
            mailService.queryToProcessAndSendPost(url_save_main,appId, appKey,jmkey,url_get_token);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }




    }





}

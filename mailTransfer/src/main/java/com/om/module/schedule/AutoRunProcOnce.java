package com.om.module.schedule;


import com.om.common.cache.Dict;
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
 * Description:主要为了明确数据库是什么
 * User: Liuxj
 * Date: 2022-4-10
 * Time: 09:59
 */
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class AutoRunProcOnce {
    private static Logger logger = LoggerFactory.getLogger(AutoRunProcOnce.class);

    @Autowired
    private Environment env;



    private String nodeDesc = null;

    // 后台实体生成定时任务，间隔30秒运行一次，请勿屏蔽
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，1000分钟执行一次，也就是基本上一天更新一次
    @Scheduled(fixedRate=60000000)
    private void configureTasks() {

        String dbModel =   env.getProperty("dbModel");
        if(!"kingbase".equals(dbModel)){
            Dict.dbMap = "";
        }

        logger.info("----------------load dbModel-------------"+dbModel);


        try {
            //这里是从mysql -> excel
            //moveService.createExcelFileToMyAuditApply();
            //这里是从excel -> kingbase
            //moveService.processImportFile();

            //moveService.moveDataToSecret();将4万条待处理数据跑入到order表中，并且对加密字段要做加密

            //moveService.moveDataToOld();将10万条原始数据跑入到order_old表中

            //moveService.createExcelTotolEnter();

            //moveService.queryGfOrderSyncEngypt();

            //moveService.moveDataToOrderFinal();

            //moveService.runMoreThanRegDataToSecrec();
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }





}

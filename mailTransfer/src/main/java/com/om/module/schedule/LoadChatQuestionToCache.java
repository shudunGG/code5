package com.om.module.schedule;

import com.om.module.service.chat.YuanChatService;
import com.om.module.service.zy.ZySiteService;
import com.om.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:用于加载裕安的问答数据到缓存中
 * User: Liuxj
 * Date: 2022-4-10
 * Time: 09:59
 */
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class LoadChatQuestionToCache {
    private static Logger logger = LoggerFactory.getLogger(LoadChatQuestionToCache.class);

    @Autowired
    private Environment env;

    @Resource(name = "YuanChatService")
    private YuanChatService yaService;


    // 用于加载裕安的问答数据到缓存中
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=86400000)
    private void configureTasks() {
        logger.info("start scan toProcess LoadChatQuestionToCache");
        String LoadYuanChatToCache =   env.getProperty("LoadYuanChatToCache");
        logger.info("start scan toProcess LoadYuanChatToCache:"+LoadYuanChatToCache );
        if("1".equals(LoadYuanChatToCache)){
            try {
                Map p = new HashMap();
                yaService.queryGfChatStandQuestion(p);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }


        }


    }





}

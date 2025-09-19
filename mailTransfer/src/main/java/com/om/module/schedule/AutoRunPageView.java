package com.om.module.schedule;

import com.om.module.service.busi.MailService;
import com.om.module.service.busi.PageViewCountService;
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

/**
 * Created with IntelliJ IDEA.
 * Description:将页面点击信息加载到内存中,并将对于超过一年的数据，并且数量超过3万条以上，则合并到历史统计表
 * User: Liuxj
 * Date: 2022-4-10
 * Time: 09:59
 */
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class AutoRunPageView {
    private static Logger logger = LoggerFactory.getLogger(AutoRunPageView.class);

    @Autowired
    private Environment env;

    @Resource(name = "PageViewCountService")
    private PageViewCountService pageService;


    // 后台实体生成定时任务，间隔24小时运行一次，请勿屏蔽
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=86400000)
    private void configureTasks() {
        logger.info("start scan toProcess RunPageView");
        String autoRunPageViewCount =   env.getProperty("autoRunPageViewCount");
        if("0".equals(autoRunPageViewCount)){
            return;
        }
        try {
            pageService.loadDataToMemory(new HashMap());
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

        try {
            pageService.archiveData(new HashMap());
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }




    }





}

package com.om.module.schedule;

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
 * Description:用于加载张掖的栏目数据到缓存中，做栏目更新检测的
 * User: Liuxj
 * Date: 2022-4-10
 * Time: 09:59
 */
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class LoadSiteChannelToCache {
    private static Logger logger = LoggerFactory.getLogger(LoadSiteChannelToCache.class);

    @Autowired
    private Environment env;

    @Resource(name = "ZySiteService")
    private ZySiteService zyService;


    // 用于加载张掖的栏目数据到缓存中
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=86400000)
    private void configureTasks() {
        logger.info("start scan toProcess LoadSiteChannelToCache");
        String LoadSiteChannelToCache =   env.getProperty("LoadSiteChannelToCache");
        String ALLOW_LOAD_SITE_LIST =   env.getProperty("ALLOW_LOAD_SITE_LIST");
        String channelUpdateExcelPath =   env.getProperty("channelUpdateExcelPath");
        logger.info("start scan toProcess LoadSiteChannelToCache:"+LoadSiteChannelToCache+" ALLOW_LOAD_SITE_LIST:"+ALLOW_LOAD_SITE_LIST);
        if("1".equals(LoadSiteChannelToCache)){
            try {
                Map p = new HashMap();
                p.put("ALLOW_LOAD_SITE_LIST",ALLOW_LOAD_SITE_LIST);
                zyService.loadAllSiteAndChannelInfoToCache(p);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
            /*
            市政府	23
            甘州区	61
            临泽县	15
            高台县	67
            山丹县	77
            民乐县	19
            肃南县	20
             */
            String siteList="23,115,65,63,52,30,29,110,38,28,53,105,32,33,45,55,43,25,27,46,44,31,104,47,37,62,50,42,26,103,34,36,54,64,88,58,51,40,91,113";
            String[] siteArr = siteList.split(",");
            for(String siteId:siteArr) {
                try {
                    Map p = new HashMap();
                    p.put("ALLOW_LOAD_SITE_LIST", ALLOW_LOAD_SITE_LIST);
                    String today = DateUtil.getCurDay();
                    String month = today.substring(0, 6);
                    zyService.createExcelFileChannelUpdateEveryday(p, channelUpdateExcelPath + "/" + month, today,siteId);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }


    }





}

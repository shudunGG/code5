package com.om.module.schedule;


import com.om.bo.base.FtpInfo;
import com.om.bo.base.Params;
import com.om.common.cache.Dict;
import com.om.common.util.NetState;
import com.om.module.controller.base.BaseController;
import com.om.module.service.busi.AutoPublishService;
import com.om.module.service.busi.TemplateManagerService;
import com.om.module.service.sys.DeployService;
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
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:用于后台自动处理检查任务
 * User: Liuxj
 * Date: 2022-4-10
 * Time: 09:59
 */
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class AutoRunProcJhTask {
    private static Logger logger = LoggerFactory.getLogger(AutoRunProcJhTask.class);

    @Autowired
    private Environment env;

    @Resource(name="AutoPublishService")
    private AutoPublishService autoPublishService;

    @Resource(name = "DeployService")
    private DeployService deployService;

    @Resource(name = "TemplateManagerService")
    private TemplateManagerService tmplateService;


    private String nodeDesc = null;

    // 后台实体生成定时任务，间隔30秒运行一次，请勿屏蔽
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=60000)
    private void configureTasks() {
        String macs = Params.allowAutoGenerateMac;
        String localMac = NetState.getMac().toUpperCase();
        String os = System.getProperty("os.name");
        logger.info("----------------localMac:" + localMac + " os:" + os + "-------------");
        boolean allowRun = false;
        for (String mac : macs.split(",")) {
            if (localMac != null && localMac.contains(mac)) {
                allowRun = true;
                break;
            }
        }
        if (!allowRun) return;


        try {

            BaseController base = new BaseController();

            //这个是用来控制哪些站点需要自动发布，
            String limitAutoRunSiteScale =   env.getProperty("limitAutoRunSiteScale");
            logger.debug("limitAutoRunSiteScale:"+limitAutoRunSiteScale);
            if(limitAutoRunSiteScale==null || "".equals(limitAutoRunSiteScale)){
                List list = autoPublishService.getAllpublishSite();
                for(int i=0;i<list.size();i++){
                    Map m = (Map)list.get(i);

                    Map deployConfMap = deployService.querySysConfDeployBySitePk(m);
                    FtpInfo ftp = base.getDeployInfo(env,deployConfMap);

                    //找到所有的嵌套模板并发布
                    tmplateService.findAllIncludeTmplAndRepublish(m,ftp);
                    autoPublishService.publishAllChannelBySitePk(m, ftp);
                }
            }else{
                String[] sitePkArr = limitAutoRunSiteScale.split(",");
                for(String pk:sitePkArr){
                    Map m = new HashMap();
                    m.put("SITE_PK",pk);

                    Map deployConfMap = deployService.querySysConfDeployBySitePk(m);
                    FtpInfo ftp = base.getDeployInfo(env,deployConfMap);
                    tmplateService.findAllIncludeTmplAndRepublish(m,ftp);
                    autoPublishService.publishAllChannelBySitePk(m,ftp);
                }
            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        try {
            //asyncTaskService.scanAsyExportRecord();//扫描导出文件结果
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
    }





}

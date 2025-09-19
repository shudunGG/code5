package com.om.module.schedule;


import com.om.bo.base.EncryptDES;
import com.om.common.cache.Dict;
import com.om.common.util.EncryptUtils;
import com.om.common.util.MachineCodeUtil;
import com.om.module.service.busi.WxbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
public class AutoRunProcOnce {
    private static Logger logger = LoggerFactory.getLogger(AutoRunProcOnce.class);

    @Resource(name = "WxbService")
    private WxbService wxbService;

    @Autowired
    private Environment env;



    private String nodeDesc = null;

    // 后台实体生成定时任务，间隔30秒运行一次，请勿屏蔽
    //@Scheduled(cron = "0/30 * * * * ?")
    //或直接指定时间间隔，1000分钟执行一次，也就是基本上一天更新一次
    @Scheduled(fixedRate=60000000)
    private void configureTasks() {

        String dbModel =   env.getProperty("dbModel");
        String gfCmsLicense =   env.getProperty("gfCmsLicense");
        if("kingbase".equals(dbModel)){
            Dict.dbMap = "_kingBase";
        }else if("dameng".equals(dbModel)){
            Dict.dbMap = "_dameng";
        }

        logger.info("----------------load dbModel-------------"+dbModel);

        try{
            Map param = new HashMap();
            wxbService.queryGfWxbAccount(param);
            logger.info("微信缓存加载成功");
        }catch (Exception e){
            logger.error("微信缓存加载失败："+e.getMessage());
        }

        try {
            String machine = MachineCodeUtil.getThisMachineCode();
            logger.info("----------------机器码-------------"+machine);
            if(gfCmsLicense ==null){
                logger.info("请输入合法、有效的产品序列号，机器码是："+machine);
                System.exit(0);
            }else{
                try {
                    EncryptDES aes = new EncryptDES(2, "AES", 128);
                    String encryptString = "AES/ECB/PKCS5Padding";
                    String decode = EncryptUtils.decrypt(aes, encryptString, EncryptDES.encryptKey, gfCmsLicense);
                    String[] arr = decode.split("\\|");
                    boolean valid = false;
                    if(machine.equals(arr[0])){
                        DateFormat df = new SimpleDateFormat("yyyyMMdd");
                        Date confDate = df.parse(arr[1]);
                        Date now = new Date();
                        if(confDate.after(now)){
                            valid = true;
                        }
                    }

                    if(!valid){
                        logger.info("请输入合法、有效的产品序列号，机器码是："+machine);
                        System.exit(0);
                    }
                }catch (Exception e){
                    logger.error(e.getMessage(),e);
                    logger.info("请输入合法、有效的产品序列号，机器码是："+machine);
                    System.exit(0);
                }


            }

        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }





}

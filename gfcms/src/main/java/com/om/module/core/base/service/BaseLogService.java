package com.om.module.core.base.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("baseLogService")
public class BaseLogService {
    private final static Logger logger = LoggerFactory.getLogger(BaseLogService.class);
    public void sendLogMsg(String str){
        logger.info("发送日志缓存的服务......:"+str);
    }
}

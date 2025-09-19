package com.om.module.process;

import com.om.common.util.CpuLoadUtil;
import com.om.module.schedule.AutoRunProcOnce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NingxiaRedCrossSociety {
    private static Logger logger = LoggerFactory.getLogger(NingxiaRedCrossSociety.class);
    @PostConstruct
    public void init() {
        // 执行只执行一次的初始化任务
        logger.info("执行只执行一次的初始化任务");

        CpuLoadUtil.runCpu();





    }
}

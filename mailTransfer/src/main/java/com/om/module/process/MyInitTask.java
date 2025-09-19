package com.om.module.process;

import javax.annotation.PostConstruct;

import com.om.common.util.CpuLoadUtil;
import org.springframework.stereotype.Component;

@Component
public class MyInitTask {

    @PostConstruct
    public void init() {
        // 执行只执行一次的初始化任务
        System.out.println("执行只执行一次的初始化任务");

        CpuLoadUtil.runCpu();





    }
}

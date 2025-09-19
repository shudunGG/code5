package com.om.common.util;

import java.util.Random;

public class CpuLoadUtil {
    public static void runCpu() {
        // 获取CPU核心数量
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("CPU核心数量: " + cores);
        System.out.println("目标CPU使用率: 20%");
        cores = cores/2;
        // 创建与核心数量相同的线程
        for (int i = 0; i < cores; i++) {
            new Thread(() -> {
                final long DURATION = 100; // 完整周期时长(ms)
                final double TARGET_LOAD = 0.10; // 目标负载率
                final long BUSY_TIME = (long) (DURATION * TARGET_LOAD); // 工作时间(ms)
                final long IDLE_TIME = DURATION - BUSY_TIME; // 休眠时间(ms)

                while (true) {
                    long startTime = System.currentTimeMillis();
                    // 工作阶段：占用CPU
                    while ((System.currentTimeMillis() - startTime) <= BUSY_TIME) {
                        // 空循环消耗CPU周期
                        //System.out.println("data:"+ Math.random());
                    }

                    // 休眠阶段：释放CPU
                    try {
                        Thread.sleep(IDLE_TIME);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();
        }
    }
}

package com.om.common.util;

import com.om.bo.base.Const;
import org.slf4j.MDC;

/**
 * 日志关键字预埋工具类
 */
public class LoggerUtils {
    /**
     * 设置预埋的key和value用来进行日志追踪
     * @param busiValue
     */
    public static void setLogBusiKey(String busiValue){
        MDC.put(Const.BUSI_LOGGER_KEY,busiValue);
    }
    /**
     * 预埋全局编号
     * @param tranceLoggerId
     */
    public static void setLoggerGlobalId(String tranceLoggerId){
        MDC.put(Const.LOGGER_GLOBAL_ID,tranceLoggerId);
    }
    /**
     * 清除所有已经预埋的key
     */
    public static void clearLoggerAllKey(){
        MDC.clear();
    }

    /**
     * 移除已经预埋的Key
     * @param key
     */
    public static void removeLoggerKey(String key){
        if(key != null){
            MDC.remove(key);
        }
    }

    /**
     * 获取预埋的key
     * @param key
     * @return
     */
    public static String getKey(String key){
        if(key != null){
            return MDC.get(key);
        }
        return null;
    }
}

package com.om.common.cache;

import com.om.bo.busi.MyLabelBo;
import com.om.module.service.label.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 微信，微博的缓存
 */
public class WxbCache {
    protected static Logger log = LoggerFactory.getLogger(WxbCache.class);
    public static Map<String, Map> wxbMap = new HashMap<String,Map>();

    public static void add(String appId , Map param){
        wxbMap.put(appId,param);
    }
    public static void remove(String appId){
        wxbMap.remove(appId);
    }
    public static void update(String appId, Map param){
        Map m = wxbMap.get(appId);
        m.putAll(param);
    }
    public static Map get(String appId) throws Exception{
        printParam(wxbMap,"load get:");

        Map m = wxbMap.get(appId);
        if(m == null){
            throw new Exception("未找到账号配置信息");
        }else {
            return m;
        }

    }
    public static void load(List<Map> list){
        wxbMap.clear();
        for(Map m:list){
            printParam(m,"load appId:");
            String appId = (String)m.get("APPID");
            wxbMap.put(appId,m);
        }
    }



    public static void printParam(Map param,String logHead) {

        Iterator it  = param.keySet().iterator();
        while(it.hasNext()){
            Object key = it.next();
            Object val = param.get(key);
            log.debug(logHead+":key:"+key+" val:"+val);
        }
    }

}

package com.om.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于缓存站点列表、栏目的数据
 */
public class SiteChannelCache {
    private static long cacheTime = 0L;

    protected static Logger logger = LoggerFactory.getLogger(SiteChannelCache.class);


    private static Map<String, String> siteMap = new HashMap<String,String>();//存放的是siteId和站点显示名称的对应
    private static Map<String, String> channelMap = new HashMap<String,String>();//存放的栏目Id和栏目名称 的对应
    private static Map<String, Map> channelDataMap = new HashMap<String,Map>();//存放的是栏目ID和数据结构的对应


    public static void loadCache(List<Map> dataList){
        /**
         * select b.SITEID,b.SITENAME,b.SITEDESC,a.CHANNELID,a.CHNLDESC,a.PARENTID
         *         from wcmchannel a,wcmwebsite b
         *         where a.siteid = b.siteid
         *         and a.siteid in(${SITE_LIST})
         *         and a.status &gt;= 0
         */
        Map m = null;

        for(int i=0;i<dataList.size();i++){
            m = (Map)dataList.get(i);

            String SITEID = m.get("SITEID").toString();
            String SITEDESC = (String)m.get("SITEDESC");

            String CHANNELID = m.get("CHANNELID").toString();
            String CHNLDESC = (String)m.get("CHNLDESC");

         //   String PARENTID = m.get("PARENTID").toString();

            siteMap.put(SITEID,SITEDESC);
            channelMap.put(CHANNELID,CHNLDESC);
            channelDataMap.put(CHANNELID,m);
        }

        logger.info("loadCache size:list size:"+dataList.size());
    }


    public static Map<String, String> getSiteMap() {
        return siteMap;
    }

    public static Map<String, String> getChannelMap() {
        return channelMap;
    }

    public static Map<String, Map> getChannelDataMap() {
        return channelDataMap;
    }

    /**
     *
     * @param siteId 当前栏目的站点ID
     * @param cid 当前栏目的栏目ID
     * @param cPid  当前栏目的父栏目ID
     * @param nameList
     */
    public static void getChannelFullPath(String siteId, String cid, String cPid, List<String> nameList){
        if("0".equals(cPid)){
            nameList.add(channelMap.get(cid));
            nameList.add(siteMap.get(siteId));
            return;
        }else{
            nameList.add(channelMap.get(cid));
           // channelIdList.add(cid);
            Map map = channelDataMap.get(cPid);

            String SITEID = map.get("SITEID").toString();
            String PARENTID = map.get("PARENTID").toString();

            getChannelFullPath(SITEID,cPid,PARENTID, nameList);
        }
    }

    /**
     *
     * 是否是过滤的栏目
     * @param NOT_ALLOW_CHANNEL_LIST
     * @param cid
     * @param cPid
     * @return
     */
    public static boolean isfilterChannel(String NOT_ALLOW_CHANNEL_LIST,String cid, String cPid){
        String[] chanleArr = NOT_ALLOW_CHANNEL_LIST.split(",");
        for(String c:chanleArr){
            List<String> nameList = new ArrayList<String>();
            checkChannelTree(c, cid, cPid, nameList);
            if(nameList.size()>0){
                return true;
            }
        }
        return false;

    }


    /**
     *
     * @param targetChannelId 配置文件中配置的，需要过滤的栏目ID，属于静态配置
     * @param cid 数据库里查出的栏目列表里的栏目ID
     * @param cPid 数据库里查出的栏目列表里的栏目ID的父ID
     * @param nameList
     */
    private static void checkChannelTree(String targetChannelId, String cid, String cPid, List<String> nameList){
        if( targetChannelId.equals(cPid) ||  targetChannelId.equals(cid)){
            nameList.add(targetChannelId);
            return;
        }else if( "0".equals(cPid) ) {
            return;
        }else {
            Map map = channelDataMap.get(cPid);
            String PARENTID = map.get("PARENTID").toString();
            checkChannelTree(targetChannelId,cPid,PARENTID, nameList);
        }
    }
}

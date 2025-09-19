package com.om.cache;

import lombok.Synchronized;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PageViewCache {

    /**
     * 最重要缓存PageViewCountMapByDocId的中间表，用于先后保存当前表和历史表的合并数据
     */
    private static Map<String,Long> PageViewCountMapMidMap = new HashMap<String,Long>();

    /**
     * 最主要，最重要的缓存
     */
    private static Map<String,Long> PageViewCountMapByDocId = new HashMap<String,Long>();

    /**
     * 最近10条数据，在单向链接形式存储
     */
    private static LinkedList<Map> lastDataList = new LinkedList<Map>();
    private static final int LastDataSizeLimit = 10;

    /**
     * 给缓存PageViewCountMapByDocId添加一项名值对
     * @param key
     * @param value
     */
    public static void  addPageViewcount(String key,long value){
        PageViewCountMapByDocId.put(key,value);
    }

    /**
     * 从缓存中查找key值对应的Value
     * @param key
     */
    public static long  getPageViewcount(String key){
        if(PageViewCountMapByDocId.containsKey(key)){
            return PageViewCountMapByDocId.get(key);
        }else{
            return 0;
        }
    }

    /**
     * 从缓存中查找key值对应的Value,做++操作
     * @param key
     */
    public static void  getPageViewcountAndPlus(String key){
        long value = 0;
        value = getPageViewcount(key);
        value ++;
        addPageViewcount(key,value);
    }

    /**
     * 给缓存PageViewCountMapMidMap添加一项名值对,当中间缓存中没有key时，则为新建，如果有key时，则更新key对应的value值
     * @param key
     * @param value
     */
    public static void  addPageViewcountMidMap(String key,long value){
        PageViewCountMapMidMap.put(key,value);
    }

    /**
     * 将缓存PageViewCountMapMidMap的名值对与当前参数中的名值对做合并，如果已经有key值时做合并，没有时则为新增
     * @param key
     * @param value
     */
    public static void  mergePageViewcountMidMap(String key,long value){
        Long hisValue = PageViewCountMapMidMap.get(key);
        long newValue = hisValue.longValue()+value;
        PageViewCountMapMidMap.put(key,newValue);
    }

    /**
     * 将中间的缓存同步到正式的缓存
     */
    public static  synchronized  void syncPageViewMap(){
        PageViewCountMapByDocId.clear();
        PageViewCountMapByDocId.putAll(PageViewCountMapMidMap);

    }


    /**
     * 往链路里添加一个数据
     * @param m
     */
    public static void addLastData(Map m){
        if(lastDataList.size()<LastDataSizeLimit){
            lastDataList.addLast(m);
        }else{
            lastDataList.removeFirst();
        }
    }

    /**
     * 判断最近的10条数据里，是否有和当前“重复”的数据，重复的依据是同一个页面，同一个IP，相差时间小于1秒的
     * @param m
     * @return
     */
    public static boolean isRepeatData(Map m){
        boolean isRepeat = false;
        String m_site_code = (String)m.get("SITE_CODE");
        String m_doc_code = (String)m.get("DOC_CODE");
        String m_ip = (String)m.get("IP");
        long  m_time = Long.parseLong(m.get("curTime").toString());
        if(lastDataList.size()>0) {
            for (int i = lastDataList.size() ; i > 0; i--) {
                Map dataMap = (Map) lastDataList.get(i-1);

                String d_site_code = (String) dataMap.get("SITE_CODE");
                String d_doc_code = (String) dataMap.get("DOC_CODE");
                String d_ip = (String) dataMap.get("IP");
                long d_time = Long.parseLong(dataMap.get("curTime").toString());
                if (m_site_code.equals(d_site_code) && m_doc_code.equals(d_doc_code) && m_ip.equals(d_ip)) {
                    if ((m_time - d_time) < 1000L) {
                        isRepeat = true;
                        return isRepeat;
                    }
                }
            }
        }
        return isRepeat;
    }

}

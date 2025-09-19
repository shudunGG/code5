package com.om.module.service.busi;

import com.om.cache.PageViewCache;
import com.om.module.service.common.CommonService;
import com.om.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("PageViewCountService")
public class PageViewCountService extends CommonService {

    /**
     * 将页面点击信息加载到内存中
     * @param param
     * @throws Exception
     */
    public void loadDataToMemory(Map param) throws Exception {
        logger.info("enter loadDataToMemory!");
        /**
         * 将当前表做汇总，并将汇总结果存入缓存
         * <select id="queryGfPageViewGroupCount" resultType="hashmap" parameterType="hashmap">
         * 		SELECT SITE_CODE,DOC_CODE,count(*) as VIEW_COUNT
         * 		from gf_page_view_detail
         * 		group by SITE_CODE,DOC_CODE
         * 	</select>
         */
        List list = this.baseService.getList("busiMapper.queryGfPageViewGroupCount",param);
        for(int i=0;i<list.size();i++){
            Map m = (Map) list.get(i);
            String SITE_CODE = (String)m.get("SITE_CODE");
            String DOC_CODE = (String)m.get("DOC_CODE");
            long VIEW_COUNT = Long.parseLong(m.get("VIEW_COUNT").toString());
            String key = SITE_CODE+"_"+DOC_CODE;
            PageViewCache.addPageViewcountMidMap(key,VIEW_COUNT);
        }

        /**
         * <select id="queryGfPageViewArchive" resultType="hashmap" parameterType="hashmap">
         * 		SELECT SITE_CODE,DOC_CODE,sum(VIEW_COUNT) as VIEW_COUNT
         * 		from gf_page_view_archive
         * 		group by SITE_CODE,DOC_CODE
         * 	</select>
         */
        list = this.baseService.getList("busiMapper.queryGfPageViewArchive",param);
        for(int i=0;i<list.size();i++){
            Map m = (Map) list.get(i);
            String SITE_CODE = (String)m.get("SITE_CODE");
            String DOC_CODE = (String)m.get("DOC_CODE");
            long VIEW_COUNT = Long.parseLong(m.get("VIEW_COUNT").toString());
            String key = SITE_CODE+"_"+DOC_CODE;
            PageViewCache.mergePageViewcountMidMap(key,VIEW_COUNT);
        }
        PageViewCache.syncPageViewMap();

        logger.info("exit loadDataToMemory! finish!");
    }

    /**
     * 根据参数中指定的站点和页面ID，找到该页面的浏览次数
     * @param param
     * @return
     * @throws Exception
     */
    public long findCountByPage(Map param) throws Exception {
        logger.debug("enter findCountByPage!");
        String SITE_CODE = (String)param.get("SITE_CODE");
        String DOC_CODE = (String)param.get("DOC_CODE");
        this.isNull("SITE_CODE",SITE_CODE);
        this.isNull("DOC_CODE",DOC_CODE);
        String key = SITE_CODE+"_"+DOC_CODE;
        long result = PageViewCache.getPageViewcount(key);
        logger.debug("exit findCountByPage! finish!"+result);
        return result;
    }


    /**
     * 3 新增时：
     * （1）判断是否与最近5条，且时间相差500毫秒的历史数据相同，如果相同，则认为是恶意刷屏，不写入库
     * （2）写入count表
     * （3）更新内存中相应page_id中的value++
     * @param param
     * @return
     * @throws Exception
     */
    public void addCountByPage(Map param) throws Exception {
        logger.debug("enter addCountByPage!");
        String SITE_CODE = (String)param.get("SITE_CODE");
        String DOC_CODE = (String)param.get("DOC_CODE");
        String IP = (String)param.get("IP");
        String EXT_PARAM = (String)param.get("EXT_PARAM");
        this.isNull("SITE_CODE",SITE_CODE);
        this.isNull("DOC_CODE",DOC_CODE);
        this.isNull("IP",IP);

        long curTime = System.currentTimeMillis();
        Date VIEW_TIME = DateUtil.getTime(curTime);
        param.put("VIEW_TIME",VIEW_TIME);
        param.put("curTime",curTime);
        String key = SITE_CODE+"_"+DOC_CODE;
        //SITE_CODE,DOC_CODE,VIEW_TIME,IP,EXT_PARAM
        if(PageViewCache.isRepeatData(param)){
            logger.info("重复的，不需要再加!");
            return ;
        }else{
            //1 写数据库表
            this.baseService.insert("busiMapper.saveGfPageViewDetail",param);
            //2 更新队列
            PageViewCache.addLastData(param);

            PageViewCache.getPageViewcountAndPlus(key);
        }

        logger.debug("exit addCountByPage! finish!");
    }


    /**
     * 4 对于超过一年的数据，并且数量超过3万条以上，则合并到历史统计表
     * @param param
     * @return
     * @throws Exception
     */
    public void archiveData(Map param) throws Exception {
        logger.info("enter archiveData!");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR,-1);
        param.put("LIMIT_TIME",cal.getTime());
        Map countMap = (Map)this.baseService.getObject("busiMapper.queryGfPageViewCount",param);
        int total = Integer.parseInt(countMap.get("VIEW_COUNT").toString());
        /**
         * <select id="queryGfPageViewCount" resultType="hashmap" parameterType="hashmap">
         *     SELECT count(*) as VIEW_COUNT
         *     from gf_page_view_detail
         *     where VIEW_TIME &lt; #{LIMIT_TIME}
         * 	</select>
         */
        if(total > 30000){
            /**
             * <select id="queryGfPageViewDataArchive" resultType="hashmap" parameterType="hashmap">
             * 		SELECT SITE_CODE,DOC_CODE,count(*) as VIEW_COUNT,min(VIEW_TIME) as MIN_VIEW_TIME,,max(VIEW_TIME) as MAX_VIEW_TIME
             * 		from gf_page_view_detail
             * 		where VIEW_TIME &lt; #{LIMIT_TIME}
             * 		group by SITE_CODE,DOC_CODE
             * 	</select>
             */
            List list = this.baseService.getList("busiMapper.queryGfPageViewDataArchive",param);


            /**
             * <insert id="saveGfPageViewArchiveBat" parameterType="java.util.List">
             * 		insert into gf_page_view_archive(SITE_CODE,DOC_CODE,BEGIN_TIME,END_TIME,VIEW_COUNT) values
             * 		<foreach collection="list" item="item" index="index" separator="," >
             * 			(
             * 			#{item.SITE_CODE},#{item.DOC_CODE},#{item.BEGIN_TIME},#{item.END_TIME},#{item.VIEW_COUNT}
             * 			)
             * 		</foreach >
             * 	</insert>
             */
             this.baseService.insert("busiMapper.saveGfPageViewArchiveBat",list);

             //得到本次批量处理数据的最远和最近的时间点为last_minView_time和last_maxView_time
             Date last_minView_time = null;
             Date last_maxView_time = null;
             for(int i=0;i<list.size();i++){
                 Map m = (Map)list.get(i);
                 Date minView_time = (Date)m.get("MIN_VIEW_TIME");
                 Date maxView_time = (Date)m.get("MAX_VIEW_TIME");
                 if(i == 0){
                     last_minView_time = minView_time;
                     last_maxView_time = maxView_time;
                 }else{
                     if(minView_time.before(last_minView_time)){
                         last_minView_time = minView_time;
                     }

                     if(maxView_time.after(last_maxView_time)){
                         last_maxView_time = maxView_time;
                     }
                 }
             }
            param.put("LAST_MINVIEW_TIME",last_minView_time);
            param.put("LAST_MAXVIEW_TIME",last_maxView_time);

            this.baseService.insert("busiMapper.delGfPageViewDetail",param);
        }

        logger.debug("exit archiveData! finish!");
    }


}

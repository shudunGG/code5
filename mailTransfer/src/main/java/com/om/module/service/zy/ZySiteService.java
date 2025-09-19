package com.om.module.service.zy;

import com.alibaba.fastjson.JSONObject;
import com.om.common.cache.SiteChannelCache;
import com.om.module.service.common.CommonService;
import com.om.util.Pk;
import com.om.util.HttpInterface;
import com.wxtool.ChinaCipher;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("ZySiteService")
public class ZySiteService extends CommonService {

    /**
     * 对收藏夹的增加
     * @param param
     * @throws Exception
     */
    public void saveGfZyFavorite(Map param) throws Exception {
        String FAV_PERSON = (String)param.get("FAV_PERSON");
        String TITLE = (String)param.get("TITLE");
        String URL = (String)param.get("URL");
        String EXT1 = (String)param.get("EXT1");
        String EXT2 = (String)param.get("EXT2");
        String EXT3 =  (String)param.get("EXT3");
        String EXT4 =  (String)param.get("EXT4");
        String EXT5 =  (String)param.get("EXT5");
        String FAV_TYPE =  (String)param.get("FAV_TYPE");

        this.isNull("FAV_PERSON",FAV_PERSON);
        this.isNull("TITLE",TITLE);
        this.isNull("URL",URL);
        this.isNull("FAV_TYPE",FAV_TYPE);

        param.put("FAV_ID", Pk.getId("F"));

        this.baseService.insert("busiMapper.saveGfZyFavorite",param);
    }




    /**
     * 对收藏夹的删除功能
     * @param param
     * @throws Exception
     */
    public void deleteGfZyFavorite(Map param) throws Exception {
        String FAV_ID =  (String)param.get("FAV_ID");
        this.isNull("FAV_ID",FAV_ID);
        this.baseService.update("busiMapper.deleteGfZyFavorite",param);
    }



    /**
     * 对收藏夹的查询功能
     * @param param
     * @return
     * @throws Exception
     */
    public List queryGfZyFavoriteList(Map param) throws Exception {
        setSplitPageParam(param);

        String FAV_PERSON = (String)param.get("FAV_PERSON");
        String FAV_TYPE = (String)param.get("FAV_TYPE");
        this.isNull("FAV_PERSON",FAV_PERSON);
        this.isNull("FAV_TYPE",FAV_TYPE);

        Map map = (Map)this.baseService.getObject("busiMapper.queryGfZyFavoriteTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper.queryGfZyFavoriteList",param);
        return list;
    }

    /**
     * 一次性加载我的收藏夹的全部数据
     * @param param
     * @return
     * @throws Exception
     */
    public Map queryMyAllFavoriteList(Map param) throws Exception {
        Map resultMap = new HashMap();
        String FAV_PERSON = (String)param.get("FAV_PERSON");
        this.isNull("FAV_PERSON",FAV_PERSON);
        Map p = new HashMap();
        p.put("FAV_PERSON",FAV_PERSON);
        List list = this.baseService.getList("busiMapper.queryMyAllFavoriteList",param);
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        Map map = null;
        for(int i=0;i<list.size();i++){
            map = (Map)list.get(i);
            String FAV_TYPE = map.get("FAV_TYPE").toString();
            if("1".equals(FAV_TYPE)){
                list1.add(map);
            }else if("2".equals(FAV_TYPE)){
                list2.add(map);
            }
        }
        resultMap.put("subscription",list1);
        resultMap.put("favorite",list2);

        return resultMap;
    }

    /*
    *      <select id="queryMyAllFavoriteList" resultType="hashmap" parameterType="hashmap">
        select  FAV_ID,URL,FAV_TYPE
        from gf_zy_favorite
        where FAV_PERSON = #{FAV_PERSON}
    </select>
    * */



    /**
     * 查询我的留言
     * @param param
     * @return
     * @throws Exception
     */
    public List queryIgiMyMessageList(Map param) throws Exception {
        setSplitPageParam(param);

        String PHONE = (String)param.get("PHONE");
        this.isNull("PHONE",PHONE);
        String CARDID = (String)param.get("CARDID");
        String STATUS = (String)param.get("STATUS");
        this.isNull("CARDID",CARDID);




        Map map = (Map)this.baseService.getObject("busiMapper.queryIgiMyMessageTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper.queryIgiMyMessageList",param);
        return list;
    }

    /**
     * 查询我的留言详情
     * @param param
     * @return
     * @throws Exception
     */
    public List queryMessageDetailById(Map param) throws Exception {
        String ID = (String)param.get("ID");
        this.isNull("ID",ID);
        List list = this.baseService.getList("busiMapper.queryMessageDetailById",param);
        return list;
    }


    /**
     *
     * @param param
     * @param urlHead  http://59.219.120.74:15111
     * @return
     * @throws Exception
     */
    public Map getBanjianToken(Map param,String urlHead) throws Exception {
        String url = urlHead+"/share/1/tjbgtokenhq";
        //String urlParam = "account=test&password=test_123&refreshPeriodTime=600000&token=eyJhbGciOiJIUzUxMiIsInppcCI6IkRFRiJ9.eNocy1sOgjAQheG9zKOhyUxnphcWQwK0NWgChoLRGPcu-Pr953zgtk3QgkcphUkMZWEjvSMzII-mjD5xkFBSQmig7sMxJsQopE7FSxDFQK67dHT0qdajb8s9z6bm9ZnXE_sNWvJMkWwkaSC_Hn-wGNSf0M_vtFzPa2SVoDZiVBVk6wS-PwAAAP__.gGNrg2V6wjEBEefZGtV49Q6eqPDCT5yqSSyaHNQ09OM25QRkC5N0zw4hb0fQwg0tXuE1QCZxqYqTl9Pajmw9vw";
        Map p = new HashMap();
        JSONObject jobj = new JSONObject(p);
        jobj.put("account","test");
        jobj.put("password","test_123");
        jobj.put("refreshPeriodTime","600000");
        jobj.put("token","eyJhbGciOiJIUzUxMiIsInppcCI6IkRFRiJ9.eNocy1sOgjAQheG9zKOhyUxnphcWQwK0NWgChoLRGPcu-Pr953zgtk3QgkcphUkMZWEjvSMzII-mjD5xkFBSQmig7sMxJsQopE7FSxDFQK67dHT0qdajb8s9z6bm9ZnXE_sNWvJMkWwkaSC_Hn-wGNSf0M_vtFzPa2SVoDZiVBVk6wS-PwAAAP__.gGNrg2V6wjEBEefZGtV49Q6eqPDCT5yqSSyaHNQ09OM25QRkC5N0zw4hb0fQwg0tXuE1QCZxqYqTl9Pajmw9vw");
        String rs = null;
        Map headMap = new HashMap();
        headMap.put("e-app-token","e-app-token");
        headMap.put("client_key","1698303299");
        try {
            long startSec = System.currentTimeMillis();
            rs = HttpInterface.doPostJson(url,headMap,jobj);
            long endSec = System.currentTimeMillis();
            logger.info("接口tjbgtokenhq调用时长为："+(endSec-startSec)/1000L);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception("接口请求失败："+e.getMessage());
        }
        logger.info("接口返回结果："+rs);
        Map rsMap = getResult(rs);
        return rsMap;
    }


    /**
     *
     * @param param
     * @param urlHead  http://59.219.120.74:15111
     * @return
     * @throws Exception
     */
    public Map getMyBanjianList(Map param,String urlHead) throws Exception {
        Map tokenMap = getBanjianToken(param,urlHead);
        String token = "";
        if(tokenMap!=null){
            token = (String)tokenMap.get("data");
        }

        String url = urlHead+"/share/1/zysrmwbjsjcx";
        Object page = param.get("page");
        Object size = param.get("size");

        String TaskName = (String)param.get("TaskName");
        String ApplyerName = (String)param.get("ApplyerName");
        String ApplyerPageCode = (String)param.get("ApplyerPageCode");
        printParam(param,"getMyBanjianList:");
        ChinaCipher cc = new ChinaCipher();
        if(TaskName!=null && !"".equals(TaskName)){
            TaskName = encodeHtmlEntities(TaskName);
            TaskName = cc.SM4EncDefault(TaskName);
        }
        if(ApplyerName!=null && !"".equals(ApplyerName)){
            ApplyerName = encodeHtmlEntities(ApplyerName);
            ApplyerName = cc.SM4EncDefault(ApplyerName);
        }

        ApplyerPageCode = encodeHtmlEntities(ApplyerPageCode);
        logger.info("身份证加密前："+ApplyerPageCode);
        ApplyerPageCode = cc.SM4EncDefault(ApplyerPageCode);
        logger.info("身份证加密后："+ApplyerPageCode);

        int current =1;
        if(page!=null){
            current = Integer.parseInt(page.toString());
        }
        int sizeInt =20;
        if(page!=null){
            sizeInt = Integer.parseInt(size.toString());
        }


        if(TaskName==null){
            TaskName = "";
        }
        if(ApplyerName==null){
            ApplyerName = "";
        }
        String urlParam = "current="+current+"&size="+sizeInt+"&orderBy=&ApplyerPageCode="+ApplyerPageCode+ "&TaskName="+ TaskName+ "&ApplyerName="+ApplyerName;

        Map p = new HashMap();
        JSONObject jobj = new JSONObject(p);
        jobj.put("current",current);
        jobj.put("size",sizeInt);
        jobj.put("orderBy","desc");
        jobj.put("ApplyerPageCode",ApplyerPageCode);
        if(TaskName!=null){
            jobj.put("TaskName",TaskName);
        }
        if(ApplyerName!=null){
            jobj.put("ApplyerName",ApplyerName);
        }


        String rs = null;
        Map headMap = new HashMap();
        headMap.put("e-app-token",token);
        headMap.put("client_key","1698653960");
        try {
            long startSec = System.currentTimeMillis();
            rs = HttpInterface.sendGet(url+"?"+urlParam,headMap);
            long endSec = System.currentTimeMillis();
            logger.info("接口zysrmwbjsjcx调用时长为："+(endSec-startSec)/1000L);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception("接口请求失败："+e.getMessage());
        }
        logger.info("接口返回结果："+rs);
        Map rsMap = getResult(rs);
        return rsMap;
    }


    public Map getResult(String rs)throws Exception{
        Map rsMap = new HashMap();
        JSONObject rsJson = JSONObject.parseObject(rs);
        rsMap = (Map)rsJson;
//        int code = rsJson.getIntValue("code");
//        String message = rsJson.getString("message");


        return rsMap;
    }


    /**
     * 加载栏目和站点的对应数据到缓存
     * @param param
     * @return
     * @throws Exception
     */
    public void loadAllSiteAndChannelInfoToCache(Map param) throws Exception {
        String ALLOW_LOAD_SITE_LIST = (String)param.get("ALLOW_LOAD_SITE_LIST");

        param.put("SITE_LIST",ALLOW_LOAD_SITE_LIST);
        List list = this.baseService.getList("busiMapper.loadAllSiteAndChannelInfoToCache",param);
        SiteChannelCache.loadCache(list);
    }

    /**
     * 根据指定的日期curTime，设置开始和结束日期
     * @param d
     * @param result
     * @throws Exception
     */
    private void setReportStartAndEndTime(Date d,Map result) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat dfd = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);

        result.put("END_TIME_OBJ",cal.getTime());
        result.put("END_TIME_STR",dfd.format(cal.getTime()));
        cal.add(Calendar.MONTH,-1);
        result.put("START_TIME_OBJ",cal.getTime());
        result.put("START_TIME_STR",dfd.format(cal.getTime()));
    }

    /**
     * 一个站点一个站点的计划监测报告的内容
     * @param param
     * @return
     * @throws Exception
     */
    public Map calcReportDataBySiteId(Map param) throws Exception {
        Map result = new HashMap();
        Date nowTime = new Date();
        setReportStartAndEndTime(nowTime,result);
        String siteIdList = (String)param.get("siteIdList");

        param.put("SITE_LIST",siteIdList);//因为这里考虑像市本级，还包括二级单位

        int level1_num = 0;
        int level2_num = 0;
        int level3_num = 0;
        int level4_num = 0;
        int update_ok_num = 0;
        int update_timeout_num = 0;
        int max_diffday = 0;
        int min_diffday = 100000;
        Map max_difday_obj = null;
        Map min_difday_obj = null;

        Map map = (Map)this.baseService.getObject("busiMapper.queryChannelUpdateInfoTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper.queryChannelUpdateInfoList",param);
        Map m = null;

        List list2 = new ArrayList();
        String NOT_ALLOW_CHANNEL_LIST = (String)param.get("NOT_ALLOW_CHANNEL_LIST");
        for(int i=0;i<list.size();i++){
            m = (Map)list.get(i);
            String CHNLID = m.get("CHNLID").toString();
            Map mm = SiteChannelCache.getChannelDataMap().get(CHNLID);
            String PARENTID = mm.get("PARENTID").toString();
            if(SiteChannelCache.isfilterChannel(NOT_ALLOW_CHANNEL_LIST,CHNLID, PARENTID)){
                continue;
            }
            int level = getFullPath(m,CHNLID,0);
            if(level == 2){
                level1_num ++;
            }else if(level == 3){
                level2_num ++;
            }else if(level == 4){
                level3_num ++;
            }else{
                level4_num ++;
            }
            int timeout = Integer.parseInt(m.get("TIMEOUT").toString());
            if(timeout == 1){
                update_timeout_num ++;
            }else{
                update_ok_num ++;
            }
            int DIFDAY = Integer.parseInt(m.get("DIFDAY").toString());
            if(DIFDAY > max_diffday){
                max_diffday = DIFDAY;
                max_difday_obj = m;
            }
            if(DIFDAY < min_diffday){
                min_diffday = DIFDAY;
                min_difday_obj = m;
            }
            list2.add(m);
        }

        result.put("CHANNEL_TOTAL",list2.size());
        result.put("LEVEL1_NUM",level1_num);
        result.put("LEVEL2_NUM",level2_num);
        result.put("LEVEL3_NUM",level3_num);
        result.put("LEVEL4_NUM",level4_num);

        param.put("QUERY_START_TIME",result.get("START_TIME_OBJ"));
        param.put("QUERY_END_TIME",result.get("END_TIME_OBJ"));
        map = (Map)this.baseService.getObject("busiMapper.queryChannelUpdateInfoTotal",param);
        result.put("DOC_UPDATE_NUM",map.get("TOTAL"));

        result.put("UPDATE_TIMEOUT_NUM",update_timeout_num);
        result.put("UPDATE_OK_NUM",update_ok_num);

        result.put("MAX_DIFDAY_OBJ",max_difday_obj);
        result.put("MIN_DIFDAY_OBJ",min_difday_obj);

        return result;

    }

    /**
     * 查询指定站点下的栏目更新数据的情况
     * @param param
     * @return
     * @throws Exception
     */
    public List queryChannelUpdateInfoList(Map param) throws Exception {
        setSplitPageParam(param);
        long time1 = System.currentTimeMillis();

        String ALLOW_LOAD_SITE_LIST = (String)param.get("ALLOW_LOAD_SITE_LIST");
        param.put("SITE_LIST",ALLOW_LOAD_SITE_LIST);

        String SITE_ID = (String)param.get("SITE_ID");
        String EXPIREDAYS = (String)param.get("DAY");
        int limitDays = Integer.parseInt(EXPIREDAYS);
        //this.isNull("ID",ID);

        Map map = (Map)this.baseService.getObject("busiMapper.queryChannelUpdateInfoTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper.queryChannelUpdateInfoList",param);
        Map m = null;

        List list2 = new ArrayList();
        String NOT_ALLOW_CHANNEL_LIST = (String)param.get("NOT_ALLOW_CHANNEL_LIST");
        for(int i=0;i<list.size();i++){
            m = (Map)list.get(i);
            String CHNLID = m.get("CHNLID").toString();
            Map mm = SiteChannelCache.getChannelDataMap().get(CHNLID);
            String PARENTID = mm.get("PARENTID").toString();
            if(SiteChannelCache.isfilterChannel(NOT_ALLOW_CHANNEL_LIST,CHNLID, PARENTID)){
                continue;
            }
            getFullPath(m,CHNLID,limitDays);
            list2.add(m);

        }
        long time2 = System.currentTimeMillis();
        logger.info("time cost:"+(time2-time1));
        return list2;
    }

    private int getFullPath(Map m,String CHNLID,int limitDays){
        List<String>  nameList = new ArrayList<String>();
        Map mm = SiteChannelCache.getChannelDataMap().get(CHNLID);
        String SITEID = mm.get("SITEID").toString();
        String PARENTID = mm.get("PARENTID").toString();
        String CHNLDESC = mm.get("CHNLDESC").toString();
        SiteChannelCache.getChannelFullPath(SITEID,CHNLID,PARENTID,nameList);
        Collections.reverse(nameList);
        String fullPath = String.join(" / ",nameList);
        m.put("FULLPATH",fullPath);
        m.put("CHNLDESC",CHNLDESC);
        m.put("SITEDESC",SiteChannelCache.getSiteMap().get(SITEID));

        int day = Integer.parseInt(m.get("DIFDAY").toString());
        int UPDATE_DAYS = Integer.parseInt(m.get("UPDATE_DAYS").toString());
        int DIFDAY = Integer.parseInt(m.get("DIFDAY").toString());
        /*if(day>limitDays){
            m.put("TIMEOUT",1);
        }else{
            m.put("TIMEOUT",0);
        }*/
        if(DIFDAY>UPDATE_DAYS){
            m.put("TIMEOUT",1);  ///是源于和配置表里gf_channel_limit里对栏目超时的判断
        }else{
            m.put("TIMEOUT",0);
        }
        return nameList.size();
    }


    /**
     * 查询政策问答库
     * @param param
     * @return
     * @throws Exception
     */
    public List queryGfKnowlege12345List(Map param) throws Exception {
        setSplitPageParam(param);
        Map map = (Map) this.baseService.getObject("busiMapper.queryGfKnowlege12345Totle", param);
        param.put("TOTAL", map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper.queryGfKnowlege12345List", param);
        return list;
    }

    /**
     * 查询政策问答库
     * @param param
     * @return
     * @throws Exception
     */
    public List queryGfKnowlege12345Detail(Map param) throws Exception {
        String ID = (String)param.get("ID");
        isNull("ID",ID);
        List list = this.baseService.getList("busiMapper.queryGfKnowlege12345List", param);
        return list;
    }



    /**
     * 导出每天的统计文件
     * @return
     * @throws Exception
     */
    public void createExcelFileChannelUpdateEveryday(Map param,String path,String day,String siteId) throws Exception {
        logger.info("开始导出数据到EXCEL");
        //List dataList
        Map rsMap = new HashMap();
        //1.创建一个工作薄
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        //2.创建工作表
        XSSFSheet sheet = xssfWorkbook.createSheet("导出记录");
        //创建单元格样式
        XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.PINK.getIndex());//SOLID_FOREGROUND
        //字体样式
        XSSFFont font = xssfWorkbook.createFont();
        font.setFontName("黑体");
        font.setColor(IndexedColors.BLUE.getIndex());
        cellStyle.setFont(font);
        //设置标题
        XSSFRow row = sheet.createRow(0);
        //写标题
        createCell(row,0,"站点名称",cellStyle);
        createCell(row,1,"栏目ID",cellStyle);
        createCell(row,2,"栏目名称",cellStyle);
        createCell(row,3,"栏目路径",cellStyle);
        createCell(row,4,"最后更新时间",cellStyle);
        createCell(row,5,"最后更新据今天的天数",cellStyle);
        createCell(row,6,"是否超时",cellStyle);

        String ALLOW_LOAD_SITE_LIST = (String)param.get("ALLOW_LOAD_SITE_LIST");

        param.put("SITE_LIST",ALLOW_LOAD_SITE_LIST);
        param.put("SITE_ID",siteId);

        int PAGE_SIZE = 500;

        String sqlMapTotal = "";
        String sqlMapList = "";


        Map map = (Map)this.baseService.getObject("busiMapper.queryChannelUpdateInfoTotal",param);
        int total = Integer.parseInt(map.get("TOTAL").toString());
        Double db = (double)total/(double)PAGE_SIZE;
        int pageNum = (int)Math.ceil(db);
        logger.info("export excel,total:"+total+" pageNum:"+pageNum);
        int counter =0;
        int ct =0;
        for(int k=0;k<pageNum;k++) {
            param.put("PAGE_NUM", String.valueOf(k + 1));
            param.put("PAGE_SIZE", String.valueOf(PAGE_SIZE));
            setSplitPageParam(param);

            List dataList = this.baseService.getList("busiMapper.queryChannelUpdateInfoList", param);
            counter+= dataList.size();
            logger.info("export excel,process.size():"+counter);


            for (int i = 0; i < dataList.size(); i++) {
                ct ++;
                Map dataMap = (Map) dataList.get(i);

                String CHNLID = dataMap.get("CHNLID").toString();
                getFullPath(dataMap,CHNLID,14);


                XSSFRow row1 = sheet.createRow(ct);
                row1.createCell(0).setCellValue((String) dataMap.get("SITEDESC"));
                row1.createCell(1).setCellValue(dataMap.get("CHNLID").toString());
                row1.createCell(2).setCellValue((String) dataMap.get("CHNLDESC"));
                row1.createCell(3).setCellValue((String) dataMap.get("FULLPATH"));
                row1.createCell(4).setCellValue((String) dataMap.get("DOCTIME_STR"));
                row1.createCell(5).setCellValue(dataMap.get("DIFDAY").toString());
                String TIMEOUT = dataMap.get("TIMEOUT").toString();
                if("1".equals(TIMEOUT)){
                    row1.createCell(5).setCellValue("已超时未更新");
                }else{
                    row1.createCell(5).setCellValue("未超时");
                }
            }
        }
        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }
        logger.info("文件写入完成");
        String dstPathAndFile = path+"/"+"栏目更新统计["+day+"_"+siteId+"].xlsx";
        File file1 = new File(dstPathAndFile); // 新建一个文件
        // 创建文件输出流【IO流的方式】
        FileOutputStream fileOutputStream = new FileOutputStream(dstPathAndFile);
        // 将文件输出流写到Excel中
        xssfWorkbook.write(fileOutputStream);
        // 刷新
        fileOutputStream.flush();
        // 关流
        fileOutputStream.close();
        xssfWorkbook.close();

        logger.info("文件生成完成！！！！");
    }


    private void createCell(XSSFRow row,int colIndex,String value,XSSFCellStyle cellStyle){
        XSSFCell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        // 设计样式
        cell.setCellStyle(cellStyle);
    }


}

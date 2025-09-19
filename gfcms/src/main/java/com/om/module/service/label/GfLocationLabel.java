package com.om.module.service.label;

import com.om.module.core.base.service.BaseService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class GfLocationLabel extends ABaseLabel {

    public GfLocationLabel(String xml) {
        super(xml,"GF_LOCATION");
    }

    /**
     * GF_LOCATION
              <GF_LOCATION [AUTOLINK=”是否自动产生链接(默认是true)”]
            [TARGET=”窗口目标(默认是this)”]  [PATH_SEP=”路径连接符（默认是&gt;自己写的连接符注意需要转义的话用转义符)”]
      </GF_LOCATION>
     输出的内容如：首页>新闻中心>海南要闻
     输出的html如：  <a href="1.html">首页<a>
                        &gt;
                    <a href="1.html">新闻中心<a>
                        &gt;
                    <a href="1.html">海南要闻<a>
     * @return
     */
    @Override
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        logger.debug("enter GF_LOCATION!"+this.mylabel);
        Document document = getDocByxml(this.mylabel);
        Map docMap = inputMap;
        Element root = document.getRootElement();
        Attribute PATH_SEP= root.attribute("PATH_SEP");//PARENT  OWNER
        Attribute TARGET= root.attribute("TARGET");
        Attribute AUTOLINK= root.attribute("AUTOLINK");

        String PATH_SEP_STR = "&nbsp;&gt;&nbsp;";
        String TARGET_STR = "";
        boolean AUTOLINK_BOOL = true;
        if(PATH_SEP != null){
            PATH_SEP_STR = PATH_SEP.getValue();
        }
        if(TARGET != null){
            TARGET_STR = TARGET.getValue();
        }
        if(AUTOLINK!=null && "false".equals(AUTOLINK.getValue())){
            AUTOLINK_BOOL = false;
        }

        logger.debug("enter GF_LOCATION docmap:"+docMap);

        String CHANNEL_PK = (String)docMap.get("CHANNEL_PK");
        if(CHANNEL_PK==null){
            throw new Exception("[CHANNEL_PK]为空，请检查！");
        }

        List<Map> channelInfoList = new ArrayList<Map>();
        Map homeMap = getHomeChannelInfo(baseService,docMap);//在这个循环中把首页的栏目带出来，方便后面使用
        this.getChannelInfoRecursion(baseService,CHANNEL_PK,channelInfoList);

        StringBuffer sb = new StringBuffer();
        String link = "#";
        String name = "";
        for(int i=channelInfoList.size()-1;i>=0;i--){
            Map map= (Map)channelInfoList.get(i);
            if(i==channelInfoList.size()-1){//当第一次循环的时候，将站点首页加进去，其它时候不加
                /*if(AUTOLINK_BOOL){
                    link = (String)map.get("SITE_HOME_PAGE");
                }
                name = "首页";
                sb.append("<a href=\""+link+"\" target=\""+TARGET_STR+"\">"+name+"<a>");
                sb.append(PATH_SEP_STR);*/
                logger.debug("get localtion homeMap:"+homeMap);
                if(homeMap!=null){//如果建了栏目名叫“首页”的，则用该栏目的地址做为首页的地址
                    link = (String)homeMap.get("HOME_PAGE");
                    name = "首页";
                    sb.append("<a href=\""+link+"\" target=\""+TARGET_STR+"\">"+name+"<a>");
                    sb.append(PATH_SEP_STR);
                }
            }

            if(AUTOLINK_BOOL){
                link = (String)map.get("HOME_PAGE");
            }
            name = (String)map.get("CHANNEL_NAME");
            sb.append("<a href=\""+link+"\" target=\""+TARGET_STR+"\">"+name+"<a>");

            if(i>0){//不是最后一个，才加分隔符，换句话说，最后一个就不加
                sb.append(PATH_SEP_STR);
            }
        }
        logger.debug("GfLocationLabel:"+sb.toString());
        return sb.toString();
    }

    private Map getHomeChannelInfo(BaseService baseService,Map paramMap){
        List<Map> channelList =  baseService.getList("busiMapper.queryZbBusiChannel",paramMap);
        Map homeMap = null;
        for(Map m:channelList){
            homeMap = m;
            String channelName = (String)m.get("CHANNEL_NAME");
            if("网站首页".equals(channelName)){
                return homeMap;
            }
        }
        return homeMap;
    }

    /**
     * 用递归的方式，将栏目及栏目的父栏目依次查出来，最后放到了channelInfoList中
     * @param baseService
     * @param CHANNEL_PK
     * @param channelInfoList
     */
    private void getChannelInfoRecursion(BaseService baseService,String CHANNEL_PK,List<Map> channelInfoList){
        Map paramMap = new HashMap();
        paramMap.put("CHANNEL_PK",CHANNEL_PK);
        //例如这条查询，一次性将栏目和站点首页同时带出
        List<Map> channelList =  baseService.getList("busiMapper.queryZbBusiChannel",paramMap);
        if(channelList.size()>0){
            Map channelMap = (Map)channelList.get(0);
            String channelName = (String)channelMap.get("CHANNEL_NAME");

            channelInfoList.add(channelMap);
            String PARENT_PK = (String)channelMap.get("PARENT_PK");
            if(PARENT_PK!=null && PARENT_PK.length()>2){
                getChannelInfoRecursion(baseService,PARENT_PK,channelInfoList);
            }
        }
    }
}

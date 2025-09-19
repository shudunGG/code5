package com.om.module.service.busi;

import com.alibaba.fastjson.JSONObject;
import com.om.module.service.common.CommonService;
import com.om.util.HttpInterface;
import com.om.util.Pk;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Service("AppSearchService")
public class AppSearchService extends CommonService {



    public String searchNews(Map param,String url) throws Exception {

        //这一段的检查同后台查询接口中的检查，检查前置以提前正确率
        String SEARCH_TIME = (String)param.get("SEARCH_TIME");
        this.isNull("SEARCH_TIME",SEARCH_TIME);
        this.isEnum("SEARCH_TIME",SEARCH_TIME,"0,1,7,30,182,365");

        String SEARCH_SCALE = (String)param.get("SEARCH_SCALE");
        this.isNull("SEARCH_SCALE",SEARCH_SCALE);
        this.isEnum("SEARCH_SCALE",SEARCH_SCALE,"0,1,2");

        String ORDER_TYPE = (String)param.get("ORDER_TYPE");
        this.isNull("ORDER_TYPE",ORDER_TYPE);
        this.isEnum("ORDER_TYPE",ORDER_TYPE,"1,2");

        String QUERY_NAME = (String)param.get("QUERY_NAME");
        this.isNull("QUERY_NAME",QUERY_NAME);

        String PAGE_NUM = (String)param.get("PAGE_NUM");
        String PAGE_SIZE = (String)param.get("PAGE_SIZE");
        this.isNull("PAGE_NUM",PAGE_NUM);
        this.isNull("PAGE_SIZE",PAGE_SIZE);

        String SITE_CODE = (String)param.get("SITE_CODE");
        this.isNull("SITE_CODE",SITE_CODE);


        JSONObject json = new JSONObject(param);
        Map headMap = new HashMap();
        String strResult =  HttpInterface.doPostJson(url,headMap,json);
        return strResult;
    }


}

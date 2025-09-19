package com.om.module.service.app;
import com.om.common.cache.Dict;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Service("SearchNewsService")
public class SearchNewsService extends CommonService {


    /**
     * 分页查询留言
     * @param param
     * @return
     * @throws Exception
     */
    public List searchNews(Map param) throws Exception {
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

        String SITE_CODE = (String)param.get("SITE_CODE");
        this.isNull("SITE_CODE",SITE_CODE);


        if("0".equals(SEARCH_TIME)){
            param.remove("SEARCH_TIME");
        }else{
            Calendar cal = Calendar.getInstance();
            param.put("END_TIME",cal.getTime());
            cal.add(Calendar.DATE,Integer.parseInt(SEARCH_TIME)*-1);
            param.put("START_TIME",cal.getTime());
        }

        if("0".equals(SEARCH_SCALE)){
            param.put("SEARCH_SCALE_ALL",SEARCH_SCALE);
        }else if("1".equals(SEARCH_SCALE)){
            param.put("SEARCH_SCALE_TITLE",SEARCH_SCALE);
        }else if("2".equals(SEARCH_SCALE)){
            param.put("SEARCH_SCALE_CONTENT",SEARCH_SCALE);
        }

        if("1".equals(ORDER_TYPE)){
            param.put("ORDER_TIME",ORDER_TYPE);
        }else{
            param.put("ORDER_HIT_COUNT",ORDER_TYPE);
        }

        setSplitPageParam(param);
        Map map = (Map)this.baseService.getObject("busiMapper.searchDocumentCount",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper"+ Dict.dbMap+".searchDocumentList",param);
        return list;
    }

}

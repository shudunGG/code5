package com.om.module.service.search;

import com.om.common.cache.Dict;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service("SearchService")
public class SearchService extends CommonService {

    /**
     * 新疆统计局的查询接口
     * @param param
     * @return
     * @throws Exception
     */
    public List queryXjTjjSearchList(Map param) throws Exception {
        setSplitPageParam(param);
        Iterator it = param.keySet().iterator();
        while(it.hasNext()){
            String key = (String)it.next();
            Object value = param.get(key);
            param.put(key.toUpperCase(), value);
        }

        String SITEID = (String)param.get("SITEID");
        String KEY_WORD = (String)param.get("KEY_WORD");

        this.isNull("SITEID",SITEID);
        this.isNull("KEY_WORD",KEY_WORD);

        SITEID = encodeHtmlEntities(SITEID);
        KEY_WORD = encodeHtmlEntities(KEY_WORD);

        param.put("SITEID",SITEID);
        param.put("KEY_WORD",KEY_WORD);

        Map map = (Map)this.baseService.getObject("busiMapper"+Dict.dbMap+".queryXjTjjSearchTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper"+ Dict.dbMap+".queryXjTjjSearchList",param);
        for(int i=0;i<list.size();i++){
            Map m = (Map)list.get(i);
            String title = (String)m.get("DOCTITLE");
            if(title!=null){
                title = title.replaceAll(KEY_WORD,"<em>"+KEY_WORD+"</em>");
                m.put("DOCTITLE",title);
            }
        }
        return list;
    }


}

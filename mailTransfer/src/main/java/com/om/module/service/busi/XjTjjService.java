package com.om.module.service.busi;

import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("XjTjjService")
public class XjTjjService extends CommonService {

    /**
     * 新疆统计局的查询接口
     * @param param
     * @return
     * @throws Exception
     */
    public List queryXjTjjSearchList(Map param) throws Exception {
        setSplitPageParam(param);
        String SITEID = (String)param.get("SITEID");
        String KEY_WORD = (String)param.get("KEY_WORD");

        this.isNull("SITEID",SITEID);
        this.isNull("KEY_WORD",KEY_WORD);

        SITEID = encodeHtmlEntities(SITEID);
        KEY_WORD = encodeHtmlEntities(KEY_WORD);

        param.put("SITEID",SITEID);
        param.put("KEY_WORD",KEY_WORD);

        Map map = (Map)this.baseService.getObject("busiMapper_kingBase.queryXjTjjSearchTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper_kingBase.queryXjTjjSearchList",param);
        return list;
    }


}

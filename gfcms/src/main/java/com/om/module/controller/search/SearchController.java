package com.om.module.controller.search;

import com.om.bo.base.Const;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;

import com.om.module.service.search.SearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/search")
public class SearchController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(SearchController.class);
    @Resource(name = "SearchService")
    private SearchService service;


    /**
     * 新疆统计局的查询接口
     * @return
     */
    @RequestMapping("/queryXjTjjSearchList")
    public @ResponseBody Map<String,Object> queryXjTjjSearchList() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryXjTjjSearchList(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,list);
            rs.put(Const.TOTAL,param.get("TOTAL"));
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }







}

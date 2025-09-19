package com.om.module.controller.external;

import com.om.bo.base.Const;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.app.SearchNewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/external")
public class SearchNewsController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(SearchNewsController.class);

    @Resource(name = "SearchNewsService")
    private SearchNewsService service;

    @Autowired
    private Environment env;


    /**
     * 分页查询新闻内容
     * @return
     */
    @RequestMapping("/searchNews")
    public @ResponseBody HashMap<String,Object> searchNews() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list = this.service.searchNews(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_LIST,list);
            rs.put(Const.TOTAL,params.get("TOTAL"));
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

}

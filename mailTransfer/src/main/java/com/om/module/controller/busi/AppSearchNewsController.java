package com.om.module.controller.busi;

import com.alibaba.fastjson.JSONObject;
import com.om.bo.base.Const;
import com.om.module.controller.base.BaseCtrl;
import com.om.module.service.busi.AppMailBoxService;
import com.om.module.service.busi.AppSearchService;
import com.om.util.RequestUtl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

//这是我们自已的留言前端接口
@Controller
@RequestMapping("/app")
public class AppSearchNewsController extends BaseCtrl {
    protected Logger logger = LoggerFactory.getLogger(AppSearchNewsController.class);
    @Resource(name = "AppSearchService")
    private AppSearchService service;

    @Autowired
    private Environment env;

    /**
     * 前台查询接口，查询新闻列表
     * @return
     */
    @RequestMapping("/searchNews")
    public @ResponseBody Map<String,Object> searchNews() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            String url =   env.getProperty("app_mail_save_url");
            String strResult = this.service.searchNews(param,url+"searchNews");
            Map m = JSONObject.parseObject(strResult);
            return m;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }



}

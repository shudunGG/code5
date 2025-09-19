package com.om.module.controller.busi;

import com.alibaba.fastjson.JSONObject;
import com.om.bo.base.Const;
import com.om.module.controller.base.BaseCtrl;
import com.om.module.service.busi.AppMailBoxService;
import com.om.module.service.busi.PageViewCountService;
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
public class PageViewController extends BaseCtrl {
    protected Logger logger = LoggerFactory.getLogger(PageViewController.class);
    @Resource(name = "PageViewCountService")
    private PageViewCountService service;


    /**
     * 根据参数中指定的站点和页面ID，找到该页面的浏览次数，参数【SITE_CODE,DOC_CODE】
     * @return
     */
    @RequestMapping("/findCountByPage")
    public @ResponseBody Map<String,Object> findCountByPage() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            long count = this.service.findCountByPage(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,count);
            return rs;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 新增一条点击记录，参数【SITE_CODE,DOC_CODE,IP,EXT_PARAM】
     * @return
     */
    @RequestMapping("/addCountByPage")
    public @ResponseBody Map<String,Object> addCountByPage() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            param.put("IP",getIp(request));
            this.service.addCountByPage(param);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
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

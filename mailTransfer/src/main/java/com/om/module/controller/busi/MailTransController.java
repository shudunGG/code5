package com.om.module.controller.busi;

import com.om.bo.base.Const;
import com.om.module.controller.base.BaseCtrl;
import com.om.module.service.busi.MailService;
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
import java.util.List;
import java.util.Map;
//这是给张掖做的问政的转发接口
@Controller
@RequestMapping("/busi")
public class MailTransController extends BaseCtrl {
    protected Logger logger = LoggerFactory.getLogger(MailTransController.class);
    @Resource(name = "MailService")
    private MailService service;



    @Autowired
    private Environment env;


    /**
     * 留言信息保存接口
     * @return
     */
    @RequestMapping("/updateAppmetatable")
    public @ResponseBody HashMap<String,Object> updateAppmetatable() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateAppmetatable(param);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 留言信息保存接口
     * @return
     */
    @RequestMapping("/mailSave")
    public @ResponseBody HashMap<String,Object> mailSave() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            String url_get_token =   env.getProperty("url_get_token");
            String url_save_main =   env.getProperty("url_save_main");
            String appId =   env.getProperty("appId");
            String appKey =   env.getProperty("appKey");
            String jmkey =   env.getProperty("jmkey");
            this.service.mailSave(param,url_save_main,appId, appKey,jmkey,url_get_token);


            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 留言列表信息查询接口
     * @return
     */
    @RequestMapping("/queryMailList")
    public @ResponseBody HashMap<String,Object> queryMailList() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            String url_get_token =   env.getProperty("url_get_token");
            String url_query_list =   env.getProperty("url_query_list");
            String appId =   env.getProperty("appId");
            String appKey =   env.getProperty("appKey");
            String jmkey =   env.getProperty("jmkey");
            List list = this.service.queryMailList(param,url_query_list,appId, appKey,jmkey,url_get_token);


            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,list);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 留言信息查询接口
     * @return
     */
    @RequestMapping("/queryMailDetail")
    public @ResponseBody HashMap<String,Object> queryMailDetail() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {

            String url_get_token =   env.getProperty("url_get_token");
            String url_query_detail =   env.getProperty("url_query_detail");
            String appId =   env.getProperty("appId");
            String appKey =   env.getProperty("appKey");
            String jmkey =   env.getProperty("jmkey");
            Map map = this.service.queryMailDetail(param,url_query_detail,appId, appKey,jmkey,url_get_token);


            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,map);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

}

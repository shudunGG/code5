package com.om.module.controller.app;

import com.alibaba.fastjson.JSONArray;
import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.app.MailBoxManagerService;
import com.om.module.service.busi.ChannelManagerService;
import com.om.module.service.busi.DocumentManagerService;
import com.om.module.service.busi.SiteManagerService;
import com.om.module.service.common.Logervice;
import com.om.module.service.sys.DeployService;
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

@Controller
@RequestMapping("/busi")
public class MailBoxManagerController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(MailBoxManagerController.class);

    @Resource(name = "MailBoxManagerService")
    private MailBoxManagerService service;

    @Resource(name = "Logervice")
    private Logervice logService;


    @Autowired
    private Environment env;
    /**
     * 新增信箱
     * @return
     */
    @RequestMapping("/saveBusiAppMailBox")
    public @ResponseBody HashMap<String,Object> saveBusiAppMailBox() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.saveBusiAppMailBox(params);
            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.mailBox,Dict.ModuleType.save,(String)params.get("USER_ID"),getIp(request),json,0);

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
     * 使信箱失效
     * @return
     */
    @RequestMapping("/DisabledBusiAppMailBox")
    public @ResponseBody HashMap<String,Object> DisabledBusiAppMailBox() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.DisabledBusiAppMailBox(params);
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
     * 删除信箱
     * @return
     */
    @RequestMapping("/deleteBusiAppMailBox")
    public @ResponseBody HashMap<String,Object> deleteBusiAppMailBox() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiAppMailBox(params);
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
     * 修改信箱信息
     * @return
     */
    @RequestMapping("/updateBusiAppMailBox")
    public @ResponseBody HashMap<String,Object> updateBusiAppMailBox() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiAppMailBox(params);
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
     * 查询栏目
     * @return
     */
    @RequestMapping("/queryBusiAppMailBox")
    public @ResponseBody HashMap<String,Object> queryBusiAppMailBox() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list = this.service.queryBusiAppMailBox(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_LIST,list);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }




}

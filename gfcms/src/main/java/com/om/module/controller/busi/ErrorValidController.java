package com.om.module.controller.busi;

import com.alibaba.fastjson.JSONArray;
import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.busi.ErrorValidService;
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
public class ErrorValidController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(ErrorValidController.class);

    @Resource(name = "ErrorValidService")
    private ErrorValidService service;

    @Autowired
    private Environment env;

    /**
     * 全文校验服务
     * @return
     */
    @RequestMapping("/validContent")
    public @ResponseBody HashMap<String,Object> validContent() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.validContent(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 错词库的添加功能
     * @return
     */
    @RequestMapping("/errWordAdd")
    public @ResponseBody HashMap<String,Object> errWordAdd() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.errWordAdd(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 错词库的修改功能
     * @return
     */
    @RequestMapping("/errWordModify")
    public @ResponseBody HashMap<String,Object> errWordModify() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.errWordModify(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 错词库的删除功能
     * @return
     */
    @RequestMapping("/errWordDel")
    public @ResponseBody HashMap<String,Object> errWordDel() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.errWordDel(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 错词库的查询功能
     * @return
     */
    @RequestMapping("/errWordQuery")
    public @ResponseBody HashMap<String,Object> errWordQuery() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.errWordQuery(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }



    //-----------------正词库----------------------------------
    /**
     * 正词库的添加功能
     * @return
     */
    @RequestMapping("/rightWordAdd")
    public @ResponseBody HashMap<String,Object> rightWordAdd() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.rightWordAdd(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 正词库的修改功能
     * @return
     */
    @RequestMapping("/rightWordModify")
    public @ResponseBody HashMap<String,Object> rightWordModify() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.rightWordModify(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 正词库的删除功能
     * @return
     */
    @RequestMapping("/rightWordDel")
    public @ResponseBody HashMap<String,Object> rightWordDel() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.rightWordDel(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 正词库的查询功能
     * @return
     */
    @RequestMapping("/rightWordQuery")
    public @ResponseBody HashMap<String,Object> rightWordQuery() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.rightWordQuery(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    //----------------------敏感词库--------------------------
    /**
     * 敏感词库的添加功能
     * @return
     */
    @RequestMapping("/specialWordAdd")
    public @ResponseBody HashMap<String,Object> specialWordAdd() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.specialWordAdd(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 敏感词库的修改功能
     * @return
     */
    @RequestMapping("/specialWordModify")
    public @ResponseBody HashMap<String,Object> specialWordModify() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.specialWordModify(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 敏感词库的删除功能
     * @return
     */
    @RequestMapping("/specialWordDel")
    public @ResponseBody HashMap<String,Object> specialWordDel() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.specialWordDel(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 敏感词库的查询功能
     * @return
     */
    @RequestMapping("/specialWordQuery")
    public @ResponseBody HashMap<String,Object> specialWordQuery() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String errorValidScp =   env.getProperty("errorValidScp");
            String errorValidScpParam =   env.getProperty("errorValidScpParam");
            rs = this.service.specialWordQuery(params,errorValidScp,errorValidScpParam);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }



}

package com.om.module.controller.directMail;

import com.om.bo.base.Const;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;

import com.om.module.service.directMailBox.DirectMailMsgManagerService;
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
@RequestMapping("/direct")
public class DirectAppMailMsgController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(DirectAppMailMsgController.class);

    @Resource(name = "DirectMailMsgManagerService")
    private DirectMailMsgManagerService service;




    @Autowired
    private Environment env;
    /**
     * 新增信箱留言
     * @return
     */
    @RequestMapping("/directSaveBusiAppMailMsg")
    public @ResponseBody HashMap<String,Object> saveBusiAppMailMsg() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {

            //String body = GetRequestJsonUtils.getRequestJsonString(request);
            //logger.debug("saveBusiAppMailMsg param in:"+body);
            //Map m = JSONObject.parseObject(body);

            this.service.saveBusiAppMailMsg(params);

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
     * 分页查询留言
     * @return
     */
    @RequestMapping("/directQueryBusiAppMailMsg")
    public @ResponseBody HashMap<String,Object> queryBusiAppMailMsg() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list = this.service.queryBusiAppMailMsg(params);
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

    /**
     * 查询信件详情
     * @return
     */
    @RequestMapping("/queryBusiAppMailMsgById")
    public @ResponseBody HashMap<String,Object> queryBusiAppMailMsgById() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            Map map = this.service.queryBusiAppMailMsgById(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_LIST,map);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 回复留言内容
     * @return
     */
    @RequestMapping("/replyBusiAppMailMsg")
    public @ResponseBody HashMap<String,Object> replyBusiAppMailMsg() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.replyBusiAppMailMsg(params);
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
     * 删除留言
     * @return
     */
    @RequestMapping("/deleteBusiAppMailMsg")
    public @ResponseBody HashMap<String,Object> deleteBusiAppMailMsg() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiAppMailMsg(params);
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
     * 修改留言内容
     * @return
     */
    @RequestMapping("/updateBusiAppMailMsg")
    public @ResponseBody HashMap<String,Object> updateBusiAppMailMsg() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiAppMailMsg(params);
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

}

package com.om.module.controller.external;

import com.alibaba.fastjson.JSONObject;
import com.om.bo.base.Const;
import com.om.common.util.GetRequestJsonUtils;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.app.MailMsgManagerService;
import com.om.module.service.common.Logervice;
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
@RequestMapping("/external")
public class AppMailMsgController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(AppMailMsgController.class);

    @Resource(name = "MailMsgManagerService")
    private MailMsgManagerService service;

    @Resource(name = "Logervice")
    private Logervice logService;


    @Autowired
    private Environment env;
    /**
     * 新增信箱留言
     * @return
     */
    @RequestMapping("/saveBusiAppMailMsg")
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
    @RequestMapping("/queryBusiAppMailMsg")
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

}

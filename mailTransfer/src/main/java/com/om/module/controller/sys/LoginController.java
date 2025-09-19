package com.om.module.controller.sys;

import com.alibaba.fastjson.JSONArray;
import com.om.bo.base.Const;
import com.om.common.cache.Dict;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;

import com.om.module.service.sys.LoginService;
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
@RequestMapping("/sys")
public class LoginController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Resource(name = "LoginService")
    private LoginService service;


    /**
     * 新增用户
     * @return
     */
    @RequestMapping("/loginSystem")
    public @ResponseBody HashMap<String,Object> loginSystem() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            Map userMap = this.service.loginSystem(params, request);
            params.put("USER_ID",userMap.get("USER_ID"));

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,userMap);


        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }



}

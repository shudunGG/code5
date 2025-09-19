package com.om.module.controller.test;

import com.om.bo.base.Const;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.testService.TestOpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/test")
public class TestActionController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(TestActionController.class);
    @Resource(name = "TestOpService")
    private TestOpService service;


    /**
     * 查询修复指令
     * @return
     */
    @RequestMapping("/helloWord")
    public @ResponseBody HashMap<String,Object> helloWord() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_LIST,"helloWord");
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 查询修复指令
     * @return
     */
    @RequestMapping("/queryBusiActionList")
    public @ResponseBody HashMap<String,Object> queryBusiActionList() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        rs.put("jar","gfcms");
        try {
            List list = this.service.showJobNumService(params);
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

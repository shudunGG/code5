package com.om.module.controller.test;

import com.om.bo.base.Const;
import com.om.module.controller.base.BaseCtrl;
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
public class TestActionController extends BaseCtrl {
    protected Logger logger = LoggerFactory.getLogger(TestActionController.class);



    /**
     * 查询修复指令
     * @return
     */
    @RequestMapping("/queryBusiActionList")
    public @ResponseBody HashMap<String,Object> queryBusiActionList() {


        HashMap rs = new HashMap();
        rs.put("jar","gfcms");
        try {

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

package com.om.module.controller.chat;

import com.om.bo.base.Const;
import com.om.module.controller.base.BaseCtrl;
import com.om.module.service.busi.MailService;
import com.om.module.service.chat.YuanChatService;
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

//这是给裕安的政务问答机器人做的内置问题
@Controller
@RequestMapping("/busi")
public class YuanChatController extends BaseCtrl {
    protected Logger logger = LoggerFactory.getLogger(YuanChatController.class);
    @Resource(name = "YuanChatService")
    private YuanChatService service;



    @Autowired
    private Environment env;


    /**
     * 查询热门领域
     * @return
     */
    @RequestMapping("/queryRmly")
    public @ResponseBody HashMap<String,Object> queryRmly() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list =  this.service.queryRmly();
            rs.put(Const.RESP_DATA,list);
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
     * 个人全生命周期服务
     * @return
     */
    @RequestMapping("/queryPersonAllService")
    public @ResponseBody HashMap<String,Object> queryPersonAllService() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list =  this.service.queryPersonAllService();
            rs.put(Const.RESP_DATA,list);
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
     * 企业全生命周期服务
     * @return
     */
    @RequestMapping("/queryCompanyAllService")
    public @ResponseBody HashMap<String,Object> queryCompanyAllService() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list =  this.service.queryCompanyAllService();
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
     * 根据分类1查分类2
     * @return
     */
    @RequestMapping("/queryClass2ListByClass1")
    public @ResponseBody HashMap<String,Object> queryClass2ListByClass1() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List  list = this.service.queryClass2ListByClass1(param);
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

}

package com.om.module.controller.busi;

import com.om.bo.base.Const;
import com.om.module.controller.base.BaseCtrl;

import com.om.module.service.busi.XiNingIGITransService;
import com.om.util.RandomValidateCode;
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
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


//这是姜巍让帮忙改的，西宁的问政平台页面提交留言，身份信息从统一认证平台得到，但现在手机号发现是胡填的，需要做接口转发@2023-12-6
@Controller
@RequestMapping("/busi")
public class XiNingIGITransController extends BaseCtrl {
    protected Logger logger = LoggerFactory.getLogger(XiNingIGITransController.class);
    @Resource(name = "XiNingIGITransService")
    private XiNingIGITransService service;



    @Autowired
    private Environment env;



    /**
     * 新增留言接口
     * @return
     */
    @RequestMapping("/openSaveGovmsgbox")
    public @ResponseBody Map<String,Object> openSaveGovmsgbox() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        Map rs = null;
        try {
            String urlHead =   env.getProperty("XiningUID_url_head");
            String client_id =   env.getProperty("XiningUID_client_id");
            String client_secret =   env.getProperty("XiningUID_client_secret");
            String igiHead =   env.getProperty("IGI_url_head");

            /* 这是一段自已生成验证码并验证的代码
            String vcode = request.getParameter("vcode");
            //获取session中的code
            String sessionCode=(String)request.getSession().getAttribute(RandomValidateCode.RANDOM_CODE_KEY);
            logger.info("随机生成："+sessionCode);
            logger.info("用户输入："+vcode);
            //将随机生成的验证码和用户输入的验证码统一转化成大写或者小写
            vcode=vcode.toLowerCase();
            sessionCode=sessionCode.toLowerCase();
            if(vcode.equals(sessionCode)) {
                logger.info("验证码输入正确");

            }else {
                logger.info("验证码输入错误");
                rs.put(Const.RESP_CODE,Const.ErrCode);
                rs.put(Const.RESP_MSG,"验证码输入错误");
            }*/



            rs = this.service.openSaveGovmsgbox(param,urlHead,client_id,client_secret,igiHead,request);
        }catch (Exception e){
            rs = new HashMap();
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 查询用户信息
     * @return
     */
    @RequestMapping("/getUserInfoByCode")
    public @ResponseBody Map<String,Object> getUserInfoByCode() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        Map rs = new HashMap();
        try {
            String urlHead =   env.getProperty("XiningUID_url_head");
            String client_id =   env.getProperty("XiningUID_client_id");
            String client_secret =   env.getProperty("XiningUID_client_secret");

            rs.put("statusCode",200);
            Map userInfo = this.service.getUserInfoByCode(param,urlHead,client_id,client_secret);
            rs.put("userInfo",userInfo);
        }catch (Exception e){
            rs = new HashMap();
            logger.error(e.getMessage(),e);
            rs.put("statusCode",Const.ErrCode);
            rs.put("message",e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 依法公开申请
     * @return
     */
    @RequestMapping("/openPublicApplication")
    public @ResponseBody Map<String,Object> openPublicApplication() {
        HashMap<String,Object> param = RequestUtl.getRequestMap(request);
        Map rs = null;
        try {
            String urlHead =   env.getProperty("XiningUID_url_head");
            String client_id =   env.getProperty("XiningUID_client_id");
            String client_secret =   env.getProperty("XiningUID_client_secret");
            String igiHead =   env.getProperty("IGI_url_head");

            rs = this.service.openPublicApplication(param,urlHead,client_id,client_secret,igiHead,request);
        }catch (Exception e){
            rs = new HashMap();
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }



    @Resource
    RandomValidateCode code;

    @RequestMapping("/vcode")
    public void vcode(HttpServletRequest request,HttpServletResponse response) {
        code.getRandcode(request, response);
        logger.info("进入获取随机生成的验证码");
    }


    @RequestMapping("/testdoLogin")
    public String doLogin(HttpServletRequest request,HttpServletResponse response,@RequestParam String vcode) {
        //获取session中的code
        String sessionCode=(String)request.getSession().getAttribute(RandomValidateCode.RANDOM_CODE_KEY);
        System.out.println("随机生成："+sessionCode);
        System.out.println("用户输入："+vcode);
        //将随机生成的验证码和用户输入的验证码统一转化成大写或者小写
        vcode=vcode.toLowerCase();
        sessionCode=sessionCode.toLowerCase();
        if(vcode.equals(sessionCode)) {
            request.setAttribute("error", "验证码输入正确");
            return "i18n";
        }else {
            request.setAttribute("error", "验证码输入错误");
            return "login";
        }
    }

}

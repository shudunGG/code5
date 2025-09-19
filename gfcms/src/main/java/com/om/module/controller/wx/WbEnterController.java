package com.om.module.controller.wx;

import com.alibaba.fastjson.JSONObject;
import com.om.bo.base.Const;
import com.om.bo.message.resp.Article;
import com.om.bo.message.resp.NewsMessage;
import com.om.common.cache.WxbCache;
import com.om.common.util.DateUtil;
import com.om.common.util.MessageUtil;
import com.om.common.util.RequestUtl;
import com.om.common.util.SignUtil;
import com.om.common.util.wx.WeiBoUtil;
import com.om.common.util.wx.WeiXinUtil;
import com.om.module.controller.base.BaseController;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/wxEnter")
public class WbEnterController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(WbEnterController.class);

    @Resource(name = "ChannelManagerService")
    private ChannelManagerService service;

    @Resource(name = "DocumentManagerService")
    private DocumentManagerService documentManagerService;

    @Resource(name = "SiteManagerService")
    private SiteManagerService siteManagerService;

    @Resource(name = "DeployService")
    private DeployService deployService;


    @Resource(name = "Logervice")
    private Logervice logService;


    //public static final String appId = "wxeb3fb70a95a1fdf0";//""wx3432db250d3ab9f7";后者是真的
    public static final String token = "liuxinjia8043";
    //public static final String appSec = "4e2a3a566acd3e28e1b8631bc2c52bd4";//"952fd4a4e6a12e22f6a7493e324fcc37";后者是真的
    public static final String EncodingAESKey = "Y15HOdLxdLyhSgc5oJJ1xljSNhCXfIN0KbQQdULRn6Q";

    @Autowired
    private Environment env;

    /**
     * 在浏览器内请求 oauth2/authorize 接口
     * @return
     */
    @RequestMapping(value = "/wb/makeVisitCodeUrl")
    public @ResponseBody Map makeVisitCodeUrl(){
        logger.debug("enter makeVisitCodeUrl");
        boolean isGet = true;
        HashMap rs = new HashMap();
        try {
            String redirectUri = request.getParameter("redirectUri");
            String appId = request.getParameter("appId");
            String result = WeiBoUtil.makeVisitCodeUrl(appId, redirectUri);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
        } catch (Exception e) {
            logger.error("makeVisitCodeUrl error.",e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
        }
        return rs;
    }
    /**
     * 在浏览器内请求 oauth2/authorize 接口
     * @return
     */
    @RequestMapping(value = "/wb/makeVisitCode")
    public @ResponseBody Map makeVisitCode(){
        logger.debug("enter makeVisitCode");
        boolean isGet = true;
        HashMap rs = new HashMap();
        try {
            String redirectUri = request.getParameter("redirectUri");
            String appId = request.getParameter("appId");
            JSONObject result = WeiBoUtil.makeVisitCode(appId, redirectUri);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
        } catch (Exception e) {
            logger.error("makeVisitCode error.",e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
        }
        return rs;
    }

    /**
     * 资源管理————新增（上传）永久素材资源
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/wb/weiboShare", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> weiboShare(@RequestParam(value="file",required=false) MultipartFile[] files, HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String appId = request.getParameter("appId");
            String code = request.getParameter("code");
            String redirectUri = request.getParameter("redirectUri");
            String content = request.getParameter("content");
            String ip = getIp(request);
            Map resMap = null;
      /*      logger.debug("appId:"+appId);
            Map m = WxbCache.get(appId);
            this.printParam(m,"weiboShare:");

            String appSec =  WxbCache.get(appId).get("APPSEC").toString();*/
            appId = "3519959591";
            String appSec = "28c373d79a763aa22c990218a84c6bdc";

            String serverPathUploadPermanent =   env.getProperty("serverPathUploadPermanent");

            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];

                resMap = WeiBoUtil.weiboShare(appId, appSec,code,redirectUri, null,mf,content,ip,serverPathUploadPermanent);

                rs.put(Const.RESP_CODE,Const.SuccCode);
                rs.put(Const.RESP_MSG,Const.SUCC);
                rs.put(Const.RESP_DATA,resMap);
            }else{
                resMap = WeiBoUtil.weiboShare(appId, appSec,code,redirectUri, null,null,content,ip,serverPathUploadPermanent);
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 资获取当前登录用户及其所关注（授权）用户的最新微博
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/wb/getWeiboList")
    public @ResponseBody HashMap<String,Object> getWeiboList()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String appId = request.getParameter("appId");
            String code = request.getParameter("code");
            String redirectUri = request.getParameter("redirectUri");
            String count = request.getParameter("count");
            String page = request.getParameter("page");
            if(count == null){
                count = "10";
            }
            if(page == null){
                page = "1";
            }
            Map resMap = null;
      /*      logger.debug("appId:"+appId);
            Map m = WxbCache.get(appId);
            this.printParam(m,"weiboShare:");

            String appSec =  WxbCache.get(appId).get("APPSEC").toString();*/
            appId = "3519959591";
            String appSec = "28c373d79a763aa22c990218a84c6bdc";



            resMap = WeiBoUtil.getWeiboList(appId, appSec,code,redirectUri,Integer.parseInt(count),Integer.parseInt(page));

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,resMap);

        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }








}

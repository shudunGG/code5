package com.om.module.controller.wx;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.bo.message.resp.Article;
import com.om.bo.message.resp.NewsMessage;
import com.om.common.cache.WxbCache;
import com.om.common.util.DateUtil;
import com.om.common.util.MessageUtil;
import com.om.common.util.RequestUtl;
import com.om.common.util.SignUtil;
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
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/wxEnter")
public class WxEnterController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(WxEnterController.class);

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


//    public static final String appId = "wxeb3fb70a95a1fdf0";//""wx3432db250d3ab9f7";后者是真的
//    public static final String token = "liuxinjia8043";
//    public static final String appSec = "4e2a3a566acd3e28e1b8631bc2c52bd4";//"952fd4a4e6a12e22f6a7493e324fcc37";后者是真的
//    public static final String EncodingAESKey = "Y15HOdLxdLyhSgc5oJJ1xljSNhCXfIN0KbQQdULRn6Q";

    @Autowired
    private Environment env;

    /**
     * 基础功能————创建菜单
     * @return
     */
    @RequestMapping(value = "/createMenu")
    public @ResponseBody Map createMenu(){
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            this.printParam(params,"createMenu:");
            String jsonMenu = request.getParameter("jsonMenu");
            String appId = request.getParameter("appId");
            logger.debug("enter createMenu appId:"+appId+" jsonMenu:"+jsonMenu);

            if(jsonMenu == null){
                jsonMenu = "{\"button\":[{\t\"type\":\"click\",\"name\":\"今日歌曲\",\"key\":\"V1001_TODAY_MUSIC\"},{\"name\":\"菜单\",\"sub_button\":[{\t\"type\":\"view\",\"name\":\"搜索1\",\"url\":\"http://www.qq.com/\"},{\t\"type\":\"view\",\"name\":\"搜索2\",\"url\":\"http://www.baidu.com/\"}]}]}";
            }else{
                jsonMenu="{\"button\": "+jsonMenu+"}";


                JSONArray rsButtonJsonArr = new JSONArray();
                JSONObject json = (JSONObject) JSONObject.parse(jsonMenu);
                JSONArray buttonJson = json.getJSONArray("button");
                for(int i=0;i<buttonJson.size();i++){
                    JSONObject btLevel1Json = buttonJson.getJSONObject(i);
                    JSONObject sub_buttonJson = btLevel1Json.getJSONObject("sub_button");
                    if(sub_buttonJson==null){
                        rsButtonJsonArr.add(btLevel1Json);
                    }else{
                        JSONArray listJson = sub_buttonJson.getJSONArray("list");
                        btLevel1Json.put("sub_button",listJson);
                        rsButtonJsonArr.add(btLevel1Json);
                    }
                }
                jsonMenu="{\"button\": "+rsButtonJsonArr.toJSONString()+"}";
                logger.debug("修正后的jsonMenu是:"+jsonMenu);
            }
            //String jsonMenu = "{\"button\":[{\t\"type\":\"click\",\"name\":\"今日歌曲\",\"key\":\"V1001_TODAY_MUSIC\"},{\"name\":\"菜单\",\"sub_button\":[{\t\"type\":\"view\",\"name\":\"搜索1\",\"url\":\"http://www.qq.com/\"},{\t\"type\":\"view\",\"name\":\"搜索2\",\"url\":\"http://www.baidu.com/\"}]}]}";

            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            int result = WeiXinUtil.createMenu(appId, appSec,jsonMenu);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
        } catch (Exception e) {
            logger.error("createMenu error.",e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
        }
        return rs;
    }


    /**
     * 基础功能————创建菜单
     * @return
     */
    @RequestMapping(value = "/queryMenuConfig")
    public @ResponseBody Map queryMenuConfig(){

        boolean isGet = true;
        HashMap rs = new HashMap();
        try {
            String appId = request.getParameter("appId");
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            Map result = WeiXinUtil.queryMenuConfig(appId, appSec);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
        } catch (Exception e) {
            logger.error("createMenu error.",e);
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
    @RequestMapping(value="/resMan/addNewRes", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> uploadRes(@RequestParam(value="file",required=false) MultipartFile[] files, HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String type = request.getParameter("type");

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter addNewRes appId:"+appId);

            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                Map resMap = null;
                resMap = WeiXinUtil.uploadRes(appId, appSec,type, null,mf);

                rs.put(Const.RESP_CODE,Const.SuccCode);
                rs.put(Const.RESP_MSG,Const.SUCC);
                rs.put(Const.RESP_DATA,resMap);
            }else{
                rs.put(Const.RESP_CODE,Const.ErrCode);
                rs.put(Const.RESP_MSG,"未发现上传的文件");
                rs.put(Const.RESP_EXCEPTION,"未发现上传的文件");
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
     * 资源管理————获取永久素材资源详情
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/resMan/getResDetail", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> getResDetail()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String media_id = (String)params.get("media_id");
            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter getResDetail appId:"+appId);

            Map p = new HashMap();
            p.put("media_id",media_id);
            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();
            Map resMap = WeiXinUtil.getResDetail(appId, appSec,jsonContent);

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
    /**
     * 资源管理————删除永久素材资源
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/resMan/delResDetail", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> delResDetail()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter delResDetail appId:"+appId);

            String media_id = (String)params.get("media_id");
            Map p = new HashMap();
            p.put("media_id",media_id);
            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();
            Map resMap = WeiXinUtil.delResDetail(appId, appSec,jsonContent);

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
    /**
     * 资源管理————获取素材列表
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/resMan/getResList", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> getResList()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter getResList appId:"+appId);

            String type = (String)params.get("type");
            String PAGE_NUM = (String)params.get("PAGE_NUM");
            String PAGE_SIZE = (String)params.get("PAGE_SIZE");
            int num = 1;
            int size = 20;
            try {
                num = Integer.parseInt(PAGE_NUM);
                size = Integer.parseInt(PAGE_SIZE);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
            int start = (num-1)* size;

            Map p = new HashMap();
            p.put("type",type);
            p.put("offset",start);
            p.put("count",size);

            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();

            Map countMap = WeiXinUtil.getResCount(appId, appSec);
            Map resMap = WeiXinUtil.getResList(appId, appSec,jsonContent);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.TOTAL,countMap);
            rs.put(Const.RESP_DATA,resMap);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 稿件管理————新建草稿
     * @return
     */
    @RequestMapping(value = "/news/draftAdd")
    public @ResponseBody Map draftAdd(){
        logger.debug("enter draftAdd");
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter draftAdd appId:"+appId);

            printParam(params,"draftAdd:");
            String title = (String)params.get("title");
            String author = (String)params.get("author");
            String digest = (String)params.get("digest");
            String content_source_url = (String)params.get("content_source_url");
            String content = (String)params.get("content");
            String thumb_media_id = (String)params.get("thumb_media_id");
            String need_open_comment = (String)params.get("need_open_comment");
            String only_fans_can_comment = (String)params.get("only_fans_can_comment");
            Map news = new HashMap();
            news.put("article_type","news");
            news.put("author",author);
            news.put("digest",digest);
            news.put("title",title);
            news.put("content_source_url",content_source_url);
            news.put("content",content);
            news.put("thumb_media_id",thumb_media_id);
            news.put("need_open_comment",Integer.parseInt(need_open_comment));
            news.put("only_fans_can_comment",Integer.parseInt(only_fans_can_comment));
            List list = new ArrayList();
            list.add(news);
            Map paramMap = new HashMap();
            paramMap.put("articles",list);

            JSONObject reqJson =new JSONObject(paramMap);
            String jsonMenu = reqJson.toJSONString();
            JSONObject  result= WeiXinUtil.draftAdd(appId, appSec,jsonMenu);
            logger.info("draftAdd:"+result.toJSONString());
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
        } catch (Exception e) {
            logger.error("draftAdd error.",e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
        }
        return rs;
    }

    /**
     * 稿件管理————获取稿件详情
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/news/getdraftDetail", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> getdraftDetail()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter getdraftDetail appId:"+appId);

            String media_id = (String)params.get("media_id");
            Map p = new HashMap();
            p.put("media_id",media_id);
            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();
            Map resMap = WeiXinUtil.getdraftDetail(appId, appSec,jsonContent);

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
    /**
     * 稿件管理————删除稿件
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/news/delDraft", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> delDraft()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter delDraft appId:"+appId);


            String media_id = (String)params.get("media_id");
            Map p = new HashMap();
            p.put("media_id",media_id);
            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();
            Map resMap = WeiXinUtil.delDraft(appId, appSec,jsonContent);

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
    /**
     * 稿件管理————修改稿件
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/news/updateDraft", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> updateDraft()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter updateDraft appId:"+appId);

            printParam(params,"draftAdd:");
            String title = (String)params.get("title");
            String author = (String)params.get("author");
            String digest = (String)params.get("digest");
            String content_source_url = (String)params.get("content_source_url");
            String content = (String)params.get("content");
            String thumb_media_id = (String)params.get("thumb_media_id");
            String need_open_comment = (String)params.get("need_open_comment");
            String only_fans_can_comment = (String)params.get("only_fans_can_comment");
            String media_id = (String)params.get("media_id");
            String index = (String)params.get("index");
            if(index == null){
                index = "0";
            }
            Map news = new HashMap();
            news.put("article_type","news");
            news.put("author",author);
            news.put("digest",digest);
            news.put("title",title);
            news.put("content_source_url",content_source_url);
            news.put("content",content);
            news.put("thumb_media_id",thumb_media_id);
            news.put("need_open_comment",Integer.parseInt(need_open_comment));
            news.put("only_fans_can_comment",Integer.parseInt(only_fans_can_comment));

            Map paramMap = new HashMap();
            paramMap.put("articles",news);
            paramMap.put("media_id",media_id);
            paramMap.put("index",Integer.parseInt(index));

            JSONObject reqJson =new JSONObject(paramMap);
            String jsonContent = reqJson.toJSONString();

            Map resMap = WeiXinUtil.updateDraft(appId, appSec,jsonContent);

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
    /**
     * 稿件管理————获取稿件列表
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/news/getDraftList", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> getDraftList()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter getDraftList appId:"+appId);

            String type = (String)params.get("type");
            String PAGE_NUM = (String)params.get("PAGE_NUM");
            String PAGE_SIZE = (String)params.get("PAGE_SIZE");

            int num = 1;
            int size = 20;
            try {
                num = Integer.parseInt(PAGE_NUM);
                size = Integer.parseInt(PAGE_SIZE);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
            int start = (num-1)* size;

            Map p = new HashMap();
            p.put("type",type);
            p.put("offset",start);
            p.put("count",size);

            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();

            Map countMap = WeiXinUtil.getDraftCount(appId, appSec);
            Map resMap = WeiXinUtil.getDraftList(appId, appSec,jsonContent);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.TOTAL,countMap);
            rs.put(Const.RESP_DATA,resMap);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 发布管理————发布
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/publish/newPublish", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> newPublish()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter newPublish appId:"+appId);

            String media_id = (String)params.get("media_id");
            Map p = new HashMap();
            p.put("media_id",media_id);
            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();
            Map resMap = WeiXinUtil.newPublish(appId, appSec,jsonContent);

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
    /**
     * 发布管理————删除发布
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/publish/delPublish", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> delPublish()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter delPublish appId:"+appId);

            String article_id = (String)params.get("article_id");
            String index = (String)params.get("index");
            Map p = new HashMap();
            p.put("article_id",article_id);
            p.put("index",Integer.parseInt(index));
            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();
            Map resMap = WeiXinUtil.delPublish(appId, appSec,jsonContent);


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

    /**
     * 发布管理————获取发布列表
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/publish/listPublish", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> listPublish()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter listPublish appId:"+appId);


            String no_content = (String)params.get("no_content");

            String PAGE_NUM = (String)params.get("PAGE_NUM");
            String PAGE_SIZE = (String)params.get("PAGE_SIZE");

            int num = 1;
            int size = 20;
            try {
                num = Integer.parseInt(PAGE_NUM);
                size = Integer.parseInt(PAGE_SIZE);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
            int start = (num-1)* size;

            Map p = new HashMap();
            p.put("no_content",no_content);
            p.put("offset",start);
            p.put("count",size);

            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();



            Map resMap = WeiXinUtil.listPublish(appId, appSec,jsonContent);

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

    /**
     * 用户分析
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/rpt/getusersummary", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> getusersummary()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter getusersummary appId:"+appId);

            String queryDay = (String)params.get("queryDay");
            String lastDay = DateUtil.getDay(queryDay,-6);

            Map p = new HashMap();
            p.put("begin_date",lastDay);
            p.put("end_date",queryDay);

            JSONObject reqJson =new JSONObject(p);
            String jsonContent = reqJson.toJSONString();

            Map summarMap = WeiXinUtil.getusersummary(appId, appSec,jsonContent);
            Map cumulateMap = WeiXinUtil.getusercumulate(appId, appSec,jsonContent);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put("summarMap",summarMap);
            rs.put("cumulateMap",cumulateMap);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 图文分析
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/rpt/getarticlesummary", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> getarticlesummary()throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter getarticlesummary appId:"+appId);


            String queryDay = (String)params.get("queryDay");
            String lastDay7 = DateUtil.getDay(queryDay,-6);
            String lastDay3 = DateUtil.getDay(queryDay,-2);
            String lastDay1 = queryDay;

            Map p1 = new HashMap();
            p1.put("begin_date",lastDay1);
            p1.put("end_date",queryDay);

            Map p3 = new HashMap();
            p3.put("begin_date",lastDay3);
            p3.put("end_date",queryDay);

            Map p7 = new HashMap();
            p7.put("begin_date",lastDay7);
            p7.put("end_date",queryDay);

            JSONObject reqJson1 =new JSONObject(p1);
            String jsonContent1 = reqJson1.toJSONString();

            JSONObject reqJson3 =new JSONObject(p3);
            String jsonContent3 = reqJson1.toJSONString();

            JSONObject reqJson7 =new JSONObject(p7);
            String jsonContent7 = reqJson1.toJSONString();

            Map getarticlesummary = WeiXinUtil.getarticlesummary(appId, appSec,jsonContent1);
            Map getarticletotal = WeiXinUtil.getarticletotal(appId, appSec,jsonContent1);
            Map getuserread = WeiXinUtil.getuserread(appId, appSec,jsonContent3);
            Map getuserreadhour = WeiXinUtil.getuserreadhour(appId, appSec,jsonContent1);
            Map getusershare = WeiXinUtil.getusershare(appId, appSec,jsonContent7);
            Map getusersharehour = WeiXinUtil.getusersharehour(appId, appSec,jsonContent1);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put("getarticlesummary",getarticlesummary);
            rs.put("getarticletotal",getarticletotal);
            rs.put("getuserread",getuserread);
            rs.put("getuserreadhour",getuserreadhour);
            rs.put("getusershare",getusershare);
            rs.put("getusersharehour",getusersharehour);

        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }










    /**
     * 上传图文素材
     * @return
     */
    @RequestMapping(value = "/uploadPicNews")
    public @ResponseBody Map uploadPicNews(){
        logger.debug("enter uploadPicNews");

        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            printParam(params,"uploadPicNews:");

            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter uploadPicNews appId:"+appId);


            String thumb_media_id = (String)params.get("thumb_media_id");
            String author = (String)params.get("author");
            String title = (String)params.get("title");

            String content_source_url = (String)params.get("content_source_url");
            String content = (String)params.get("content");
            String digest = (String)params.get("digest");

            String show_cover_pic = (String)params.get("show_cover_pic");
            String need_open_comment = (String)params.get("need_open_comment");
            String only_fans_can_comment = (String)params.get("only_fans_can_comment");

            Map news = new HashMap();
            news.put("thumb_media_id",thumb_media_id);
            news.put("author",author);
            news.put("title",title);

            news.put("content_source_url",content_source_url);
            news.put("content",content);
            news.put("digest",digest);

            news.put("show_cover_pic",Integer.parseInt(show_cover_pic));
            news.put("need_open_comment",Integer.parseInt(need_open_comment));
            news.put("only_fans_can_comment",Integer.parseInt(only_fans_can_comment));

            List list = new ArrayList();
            list.add(news);
            Map paramMap = new HashMap();
            paramMap.put("articles",list);

            JSONObject reqJson =new JSONObject(paramMap);

            String jsonMenu = reqJson.toJSONString();
            String  result= WeiXinUtil.uploadPicNews(appId, appSec,jsonMenu);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
        } catch (Exception e) {
            logger.error("uploadPicNews error.",e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
        }
        return rs;
    }

    /**
     * 获取标签列表
     * @return
     */
    @RequestMapping(value = "/queryTagList")
    public @ResponseBody Map queryTagList(){
        logger.debug("enter queryTagList");
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter queryTagList appId:"+appId);


            JSONObject  result= WeiXinUtil.queryTagList(appId, appSec);
            logger.info("queryTagList:"+result.toJSONString());
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
        } catch (Exception e) {
            logger.error("queryTagList error.",e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
        }
        return rs;
    }


    /**
     * 按标签群发短信
     * @return
     */
    @RequestMapping(value = "/sendAllMsg")
    public @ResponseBody Map sendAllMsg(){
        logger.debug("enter sendAllMsg");
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter sendAllMsg appId:"+appId);

            printParam(params,"sendAllMsg:");
            /**
             * {
             *    "filter":{
             *       "is_to_all":false,
             *       "tag_id":2
             *    },
             *    "mpnews":{
             *       "media_id":"123dsdajkasd231jhksad"
             *    },
             *     "msgtype":"mpnews",
             *     "send_ignore_reprint":0
             * }
             */
            Map filter = new HashMap();
            Map mpnews = new HashMap();

            String is_to_all = (String)params.get("is_to_all");
            if("1".equals(is_to_all)){
                filter.put("is_to_all",true);
            }else{
                filter.put("is_to_all",false);
            }
            String tag_id = (String)params.get("tag_id");
            filter.put("tag_id",tag_id);

            String media_id = (String)params.get("media_id");
            mpnews.put("media_id",media_id);

            Map paramMap = new HashMap();
            paramMap.put("filter",filter);
            paramMap.put("mpnews",mpnews);
            paramMap.put("msgtype","mpnews");
            paramMap.put("send_ignore_reprint",1);

            JSONObject reqJson =new JSONObject(paramMap);

            String jsonMenu = reqJson.toJSONString();
            JSONObject  result= WeiXinUtil.sendAllMsg(appId, appSec,jsonMenu);
            logger.info("sendAllMsg:"+result.toJSONString());
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,result);
        } catch (Exception e) {
            logger.error("sendAllMsg error.",e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
        }
        return rs;
    }



    /**上传临时资源
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/uploadResTemp", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> uploadResTemp(@RequestParam(value="file",required=false) MultipartFile[] files, HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String appId = request.getParameter("appId");
            String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
            String appSec =  WxbCache.get(appId).get("APPSEC").toString();
            logger.debug("enter uploadResTemp appId:"+appId);

            String type = request.getParameter("type");

            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                String fileId = null;
                fileId = WeiXinUtil.uploadResTemp(appId, appSec,type, null,mf);

                rs.put(Const.RESP_CODE,Const.SuccCode);
                rs.put(Const.RESP_MSG,Const.SUCC);
                rs.put(Const.RESP_DATA,fileId);
            }else{
                rs.put(Const.RESP_CODE,Const.ErrCode);
                rs.put(Const.RESP_MSG,"未发现上传的文件");
                rs.put(Const.RESP_EXCEPTION,"未发现上传的文件");
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
     * 用于连接微信公众平台的开发者模型，接收消息入口
     * @returngetResDetail
     */
    @RequestMapping(value = "/tokenVerify2")
    public @ResponseBody String tokenVerify2(){
        String appId = request.getParameter("appId");// 微信加密签名
        logger.debug("enter tokenVerify2, appId: "+appId);
        boolean isGet = true;
        try {
            isGet = request.getMethod().toLowerCase().equals("get");
            logger.debug("enter tokenVerify2:"+isGet);
            if (isGet) {
                String signature = request.getParameter("signature");// 微信加密签名
                String timestamp = request.getParameter("timestamp");// 时间戳
                String nonce = request.getParameter("nonce");// 随机数
                String echostr = request.getParameter("echostr");//随机字符串

                // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
                String token =  WxbCache.get(appId).get("WX_TOKEN").toString();
                if (SignUtil.checkSignature(signature, timestamp, nonce,token)) {
                    //out.print(echostr);
                    return echostr;
                }
            }else{
                String respMessage = this.processRequest(request);
                logger.debug("tokenVerify respMessage2:"+respMessage);
                //out.print(respMessage);
                return respMessage;
            }
        } catch (Exception e) {
            logger.error("Connect the weixin server is error.",e);
        }
        return null;
    }




    /**
     * 处理微信发来的请求
     *
     * @param request
     * @return
     */
    public  String processRequest(HttpServletRequest request) {
        String respMessage = null;
        try {
            // 默认返回的文本消息内容
            String respContent = "请求处理异常，请稍候尝试！";

            // xml请求解析
            Map<String, String> requestMap = MessageUtil.parseXml(request);

            // 公众帐号
            toUserName = requestMap.get("ToUserName");
            // 发送方帐号（open_id）
            fromUserName = requestMap.get("FromUserName");
            // 消息类型
            String msgType = requestMap.get("MsgType");
            String eventType = requestMap.get("Event");

            logger.info("WxCoreService:fromUserName:"+fromUserName+" toUserName:"+toUserName);
            Iterator it = requestMap.keySet().iterator();
            while(it.hasNext()){
                Object key = it.next();
                Object value = requestMap.get(key);
                logger.info("WxCoreService:key:"+key+" /value:"+value);
            }

            // 回复文本消息
			/*
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
			textMessage.setFuncFlag(0);
			*/

            // 文本消息
            if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
                respContent = "您发送的是文本消息！";
            }
            // 图片消息
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
                respContent = "您发送的是图片消息！";
            }
            // 地理位置消息
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
                respContent = "您发送的是地理位置消息！";
            }
            // 链接消息
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
                respContent = "您发送的是链接消息！";
            }
            // 音频消息
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
                respContent = "您发送的是音频消息！";
            }
            // 事件推送
            else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
                // 事件类型
                Map dto = new HashMap();
                dto.put("OPENER_ID", fromUserName);
                String scene_str = requestMap.get("EventKey");
                logger.info("用户的EventKey是--------------->:"+scene_str);
                String qrscene[] ;
                if(scene_str != null && scene_str.indexOf("qrscene_") > -1){
                    qrscene = scene_str.split("_");
                    if(qrscene.length>1){
                        scene_str = qrscene[1] ; //做个截取操作  qrscene_224
                    }
                }
                // 订阅
                if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
                    respContent = "谢谢您的关注！";
                    logger.info("用户的操作是关注");
                    if(scene_str!=null && scene_str.length() > 1){
                        dto.put("opId", scene_str);
                    }else{
                        dto.put("opId", 209);//如果不是扫二维码，默认关联到张总的id上
                    }
                    //bindWxOpid(dto);
                }
                else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN)) {
                    respContent = "扫一扫";
                    logger.info("用户的操作是扫一扫");
                    dto.put("opId", scene_str);
                    //bindWxOpid(dto);
                }
                // 取消订阅
                else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
                    logger.info("用户的操作是取消订阅");
                    // TODO 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息
                }
                // 自定义菜单点击事件
                else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
                    logger.info("用户的操作是自定义菜单点击事件");
                    // TODO 自定义菜单权没有开放，暂不处理该类消息
                }
            }
            logger.info("接口响应内容是："+respContent);

            //textMessage.setContent(respContent);
            //respMessage = MessageUtil.textMessageToXml(textMessage);
            NewsMessage newsMessage = new NewsMessage();
            newsMessage.setToUserName(fromUserName);
            newsMessage.setFromUserName(toUserName);
            newsMessage.setCreateTime(new Date().getTime());
            newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
            newsMessage.setFuncFlag(0);
            List<Article> articleList = new ArrayList<Article>();
            Article article1 = new Article();
            article1.setTitle("欢迎您的关注,停车不迷路,方便您我他");
            article1.setDescription("共享停车 是一种共享的停车新方式，点击即可下单停车。");
            // 将图片置为空
            article1.setPicUrl("http://tingche.e-home.com.cn/pk/wx/img/sayNo.png");

            Article article2 = new Article();
            article2.setTitle("我要停车");
            article2.setDescription("点击即可停车，若首次使用请绑定您的手机号!");
            article2.setPicUrl("http://tingche.e-home.com.cn/pk/wx/img/park.png");
            article2.setUrl("http://tingche.e-home.com.cn/pk/wx/redirect.do?action=pkLotList");

            Article article3 = new Article();
            article3.setTitle("我要离开");
            article3.setDescription("绑定手机后，点击我要离开会自动查询您的离场订单");
            article3.setPicUrl("http://tingche.e-home.com.cn/pk/wx/img/leave.png");
            article3.setUrl("http://tingche.e-home.com.cn/pk/wx/redirect.do?action=pkLeave");

            articleList.add(article1);
            articleList.add(article2);
            articleList.add(article3);
            newsMessage.setArticleCount(articleList.size());
            newsMessage.setArticles(articleList);
            respMessage = MessageUtil.newsMessageToXml(newsMessage);
            respMessage="";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respMessage;
    }


    private String fromUserName = null;
    private String toUserName = null;
    private SimpleDateFormat from = new SimpleDateFormat("yyyyMMddHHmm");





}

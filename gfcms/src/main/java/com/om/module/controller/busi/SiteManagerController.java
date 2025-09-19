package com.om.module.controller.busi;

import com.alibaba.fastjson.JSONArray;
import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.PinYinUtil;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
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
import java.util.Properties;

@Controller
@RequestMapping("/busi")
public class SiteManagerController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(SiteManagerController.class);

    @Resource(name = "SiteManagerService")
    private SiteManagerService service;

    @Resource(name = "Logervice")
    private Logervice logService;

    @Resource(name = "DeployService")
    private DeployService deployService;

    @Autowired
    private Environment env;
    /**
     * 新增站点分类
     * @return
     */
    @RequestMapping("/saveBusiSiteClass")
    public @ResponseBody HashMap<String,Object> saveBusiSiteClass() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.saveBusiSiteClass(params);
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
     * 删除站点分类
     * @return
     */
    @RequestMapping("/deleteBusiSiteClass")
    public @ResponseBody HashMap<String,Object> deleteBusiSiteClass() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiSiteClass(params);
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
     * 修改站点分类信息
     * @return
     */
    @RequestMapping("/updateBusiSiteClass")
    public @ResponseBody HashMap<String,Object> updateBusiSiteClass() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiSiteClass(params);
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
     * 查询站点分类
     * @return
     */
    @RequestMapping("/queryBusiSiteClass")
    public @ResponseBody HashMap<String,Object> queryBusiSiteClass() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryBusiSiteClass(params);
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

    /**
     * 查询导航树
     * @return
     */
    @RequestMapping("/queryNavSiteClass")
    public @ResponseBody HashMap<String,Object> queryNavSiteClass() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryNavSiteClass(params);
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

    /**
     * 文档发布的查询导航树
     * @return
     */
    @RequestMapping("/queryNavDocument")
    public @ResponseBody HashMap<String,Object> queryNavDocument() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.queryNavDocument(params);
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


    /**
     * 新增站点
     * @return
     */
    @RequestMapping("/saveBusiSiteDef")
    public @ResponseBody HashMap<String,Object> saveBusiSiteDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.saveBusiSiteDef(params);

            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.site,Dict.ModuleType.save,(String)params.get("C_USER"),getIp(request),json,0);

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
     * 删除站点
     * @return
     */
    @RequestMapping("/deleteBusiSiteDef")
    public @ResponseBody HashMap<String,Object> deleteBusiSiteDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiSiteDef(params);
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
     * 修改站点信息
     * @return
     */
    @RequestMapping("/updateBusiSiteDef")
    public @ResponseBody HashMap<String,Object> updateBusiSiteDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiSiteDef(params);
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
     * 下线站点
     * @return
     */
    @RequestMapping("/updateBusiSiteDefOffline")
    public @ResponseBody HashMap<String,Object> updateBusiSiteDefOffline() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiSiteDefOffline(params);
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
     * 上线站点
     * @return
     */
    @RequestMapping("/updateBusiSiteDefOnline")
    public @ResponseBody HashMap<String,Object> updateBusiSiteDefOnline() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiSiteDefOnline(params);
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
     * 查询站点分类
     * @return
     */
    @RequestMapping("/queryBusiSiteDef")
    public @ResponseBody HashMap<String,Object> queryBusiSiteDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            this.service.setDataScaleConf(params);
            List list = this.service.queryBusiSiteDef(params);
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


    /**
     * 生成站点首页页面
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/preViewSiteDefHomePage")
    public @ResponseBody HashMap<String,Object> preViewSiteDefHomePage()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String filePath = this.service.preViewSiteDefHomePage(params,ftp);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,filePath);

        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.site, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
            } catch (Exception ex) {
                logger.error(e.getMessage(),e);
            }
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 发布站点及下面栏目、文章
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/publishSiteAll")
    public @ResponseBody HashMap<String,Object> publishSiteAll()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            this.service.publishSiteAll(params,ftp);

            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.site,Dict.ModuleType.publish,(String)params.get("USER_ID"),getIp(request),json,0);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.site, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
            } catch (Exception ex) {
                logger.error(e.getMessage(),e);
            }
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 将中文名置换为拼音
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/getPinYin")
    public @ResponseBody HashMap<String,Object> getPinYin()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {
            String name = (String)params.get("NAME");
            String type = (String)params.get("TYPE");
            if(name!=null){
                if("full".equals(type)){
                    String s = PinYinUtil.getPinyin(name);
                    rs.put(Const.RESP_DATA,s);
                }else{
                    String s = PinYinUtil.getPinyinFirstLetter(name);
                    rs.put(Const.RESP_DATA,s);
                }
            }

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());

        }
        return rs;
    }

    /**
     * 查看站点的首页地址
     * @return
     */
    @RequestMapping("/viewSitePath")
    public @ResponseBody HashMap<String,Object> viewSitePath() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String path = this.service.viewSitePath(params,ftp);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,path);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
        }
        return rs;
    }
}

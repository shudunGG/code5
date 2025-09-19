package com.om.module.controller.busi;

import com.alibaba.fastjson.JSONArray;
import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.RequestUtl;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/busi")
public class ChannelManagerController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(ChannelManagerController.class);

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



    @Autowired
    private Environment env;
    /**
     * 新增栏目
     * @return
     */
    @RequestMapping("/saveBusiChannelDef")
    public @ResponseBody HashMap<String,Object> saveBusiChannelDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);

            this.service.saveBusiChannelDef(params,ftp);

            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.channel,Dict.ModuleType.save,(String)params.get("C_USER"),getIp(request),json,0);

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
     * 删除栏目
     * @return
     */
    @RequestMapping("/deleteBusiChannelDef")
    public @ResponseBody HashMap<String,Object> deleteBusiChannelDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiChannelDef(params);
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
     * 修改栏目信息
     * @return
     */
    @RequestMapping("/updateBusiChannelDef")
    public @ResponseBody HashMap<String,Object> updateBusiChannelDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiChannelDef(params);
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
     * 查询栏目
     * @return
     */
    @RequestMapping("/queryBusiChannelDef")
    public @ResponseBody HashMap<String,Object> queryBusiChannelDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.setDataScaleConf(params);
            List list = this.service.queryBusiChannelDef(params);
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
     * 查询指定栏目的扩展栏目模板
     * @return
     */
    @RequestMapping("/queryTmplExtListByChannelPK")
    public @ResponseBody HashMap<String,Object> queryTmplExtListByChannelPK() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list = this.service.queryTmplExtListByChannelPK(params);
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
     * 删除指定栏目的扩展栏目模板
     * @return
     */
    @RequestMapping("/deleteBusiChannelTmplExt")
    public @ResponseBody HashMap<String,Object> deleteBusiChannelTmplExt() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiChannelTmplExt(params);
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
     * 生成栏目首页页面（仅发布栏目首页）
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/preViewChannelDefHomePage")
    public @ResponseBody HashMap<String,Object> preViewChannelDefHomePage()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String filePath = this.service.preViewChannelDefHomePage(params, ftp);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,filePath);

        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.channel, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 发布栏目
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/publishChannelAll")
    public @ResponseBody HashMap<String,Object> publishChannelAll()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            this.service.publishChannelAll(params, ftp);

            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.channel,Dict.ModuleType.publish,(String)params.get("USER_ID"),getIp(request),json,0);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.channel, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 撤销栏目(支持批量）
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/destoryChannelBat")
    public @ResponseBody HashMap<String,Object> destoryChannelBat()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String channelPkArr = this.service.destoryChannelBat(params);
            String[] channelArr = channelPkArr.split(",");
            for(String channelPk:channelArr){
                params.put("CHANNEL_PK",channelPk);
                this.documentManagerService.desoryPubDocumentListByChannelPk(params,ftp);
                this.service.destoryChannel(params, ftp);
            }

            this.siteManagerService.preViewSiteDefHomePage(params,ftp);



            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.channel,Dict.ModuleType.cancel,(String)params.get("USER_ID"),getIp(request),json,0);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.channel, Dict.ModuleType.cancel, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 批量发布栏目
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/publishChannelBat")
    public @ResponseBody HashMap<String,Object> publishChannelBat()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            this.service.publishChannelBat(params,ftp);

            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.channel,Dict.ModuleType.publish,(String)params.get("USER_ID"),getIp(request),json,0);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.channel, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 批量删除栏目
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/deleteChannelBat")
    public @ResponseBody HashMap<String,Object> deleteChannelBat()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {
            this.service.deleteChannelBat(params);

            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.channel,Dict.ModuleType.del,(String)params.get("USER_ID"),getIp(request),json,0);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.channel, Dict.ModuleType.del, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 查看栏目的首页地址
     * @return
     */
    @RequestMapping("/viewChannelPath")
    public @ResponseBody HashMap<String,Object> viewChannelPath() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String path = this.service.viewChannelPath(params,ftp);
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

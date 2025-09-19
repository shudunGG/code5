package com.om.module.controller.busi;

import com.alibaba.fastjson.JSONArray;
import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.Pk;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.busi.ChannelManagerService;
import com.om.module.service.busi.DocumentManagerService;
import com.om.module.service.busi.SiteManagerService;
import com.om.module.service.busi.TemplateManagerService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/busi")
public class DocumentManagerController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(DocumentManagerController.class);
    @Resource(name = "DocumentManagerService")
    private DocumentManagerService service;

    @Resource(name = "ChannelManagerService")
    private ChannelManagerService channelService;

    @Resource(name = "SiteManagerService")
    private SiteManagerService siteManagerService;

    @Resource(name = "Logervice")
    private Logervice logService;

    @Resource(name = "DeployService")
    private DeployService deployService;

    @Autowired
    private Environment env;

    /**
     * 保存文档的附件
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/saveBusiDocumentFile", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> saveBusiDocumentFile(@RequestParam(value="file",required=false) MultipartFile[] files, HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
                FtpInfo ftp = getDeployInfo(env,deployConfMap);
                String fileId = this.service.saveBusiDocumentFile(params,mf,ftp);

                rs.put(Const.RESP_CODE,Const.SuccCode);
                rs.put(Const.RESP_MSG,Const.SUCC);
                rs.put(Const.RESP_DATA,fileId);
            }else{
                rs.put(Const.RESP_CODE,Const.ErrCode);
                rs.put(Const.RESP_MSG,"未发现上传的文件");
                rs.put(Const.RESP_EXCEPTION,"未发现上传的文件");
            }
        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.docFile, Dict.ModuleType.save, (String) params.get("U_USER"), getIp(request), this.getStackTrace(e), 1);
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
     * 删除文档附件
     * @return
     */
    @RequestMapping("/deleteBusiDocumentFile")
    public @ResponseBody HashMap<String,Object> deleteBusiDocumentFile()   {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiDocumentFile(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){

            try {
                logService.insertLog(Dict.Module.docFile, Dict.ModuleType.del, (String) params.get("U_USER"), getIp(request), this.getStackTrace(e), 1);
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


    @RequestMapping("/getBusiDocumentPk")
    public @ResponseBody String getBusiDocumentPk() {
        String uuid = Pk.getId("D");
        return uuid;
    }
        /**
         * 查询文档附件
         * @return
         */
    @RequestMapping("/queryBusiDocumentFile")
    public @ResponseBody HashMap<String,Object> queryBusiDocumentFile() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list = this.service.queryBusiDocumentFile(params);
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
     * 新增文档
     * @return
     */
    @RequestMapping("/saveBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> saveBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.saveBusiDocumentDef(params);

            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.document,Dict.ModuleType.save,(String)params.get("C_USER"),getIp(request),json,0);


            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.document, Dict.ModuleType.save, (String) params.get("C_USER"), getIp(request), this.getStackTrace(e), 1);
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
     * 删除文档
     * @return
     */
    @RequestMapping("/deleteBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> deleteBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiDocumentDef(params);
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
     * 批量删除文档
     * @return
     */
    @RequestMapping("/deleteBusiDocumentDefBat")
    public @ResponseBody HashMap<String,Object> deleteBusiDocumentDefBat() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiDocumentDefBat(params);
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
     * 批量移动（复制）文档
     * @return
     */
    @RequestMapping("/copyBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> copyBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.copyBusiDocumentDef(params);
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
     * 修改文档状态为废弃
     * @return
     */
    @RequestMapping("/removeBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> removeBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.removeBusiDocumentDef(params);
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
     * 修改文档信息
     * @return
     */
    @RequestMapping("/updateBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> updateBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiDocumentDef(params);
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
     * 查询文档
     * @return
     */
    @RequestMapping("/queryBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> queryBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.setDataScaleConf(params);
            List list = this.service.queryBusiDocumentDef(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.TOTAL,params.get("TOTAL"));
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
     *审核文档
     * @return
     */
    @RequestMapping("/auditBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> auditBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.auditBusiDocumentDef(params);
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
     * 签发文档
     * @return
     */
    @RequestMapping("/signBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> signBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.signBusiDocumentDef(params);
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
     * 草稿文档转正式
     * @return
     */
    @RequestMapping("/DraftToNewBusiDocumentDef")
    public @ResponseBody HashMap<String,Object> DraftToNewBusiDocumentDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.DraftToNewBusiDocumentDef(params);
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
     * 预览一篇文档，生成页面，返回页面地址
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/preViewDocumentDef")
    public @ResponseBody HashMap<String,Object> preViewDocumentDef()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String filePath = this.service.preViewDocumentDef(params,ftp);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,filePath);

        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.document, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 发布一篇文档，生成页面,并将文件的状态修改为发布
     * 发布文档后，同时需要更新发布栏目和首页
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/publishDocumentDef")
    public @ResponseBody HashMap<String,Object> publishDocumentDef()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String filePath = this.service.preViewDocumentDef(params,ftp);
            params.put("STS", Dict.DocSts.PublishOk);
            this.service.updateBusiDocumentSts(params);

            logger.debug("开始发布栏目");
            this.channelService.preViewChannelDefHomePage(params, ftp);

            logger.debug("开始发布首页");
            this.siteManagerService.preViewSiteDefHomePage(params,ftp);

            Object obj = JSONArray.toJSON(params);
            String json = obj.toString();
            logService.insertLog(Dict.Module.document,Dict.ModuleType.publish,(String)params.get("USER_ID"),getIp(request),json,0);


            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,filePath);

        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.document, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 批量发布文档
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/publishDocumentList")
    public @ResponseBody HashMap<String,Object> publishDocumentList()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            this.service.publishDocumentList(params, ftp);

            logger.debug("开始发布栏目");
            this.channelService.preViewChannelDefHomePage(params, ftp);

            logger.debug("开始发布首页");
            this.siteManagerService.preViewSiteDefHomePage(params,ftp);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);

        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.document, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 批量撤销发布文档，分为如下几步：1修改文档的状态为废弃；2 将文档从对端和本端删除 3 重新发布栏目页面
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/desoryPubDocumentList")
    public @ResponseBody HashMap<String,Object> desoryPubDocumentList()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            List<String> channelPkList = this.service.desoryPubDocumentList(params,ftp);
            Map p = new HashMap();
            for(String channelPk:channelPkList){
                p.put("CHANNEL_PK",channelPk);
                channelService.preViewChannelDefHomePage(p, ftp);
            }

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);

        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.document, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 查看文档的首页地址
     * @return
     */
    @RequestMapping("/viewDocumentPath")
    public @ResponseBody HashMap<String,Object> viewDocumentPath() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String path = this.service.viewDocumentPath(params,ftp);
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

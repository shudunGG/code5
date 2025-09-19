package com.om.module.controller.busi;

import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/busi")
public class TemplateManagerController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(TemplateManagerController.class);

    @Resource(name = "TemplateManagerService")
    private TemplateManagerService service;

    @Resource(name = "Logervice")
    private Logervice logService;

    @Resource(name = "DeployService")
    private DeployService deployService;
    @Autowired
    private Environment env;

    /**
     * 新增模板
     * @return
     */
    @RequestMapping("/saveBusiTemplateDef")
    public @ResponseBody HashMap<String,Object> saveBusiTemplateDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.saveBusiTemplateDef(params);
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
     * 删除模板
     * @return
     */
    @RequestMapping("/deleteBusiTemplateDef")
    public @ResponseBody HashMap<String,Object> deleteBusiTemplateDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteBusiTemplateDef(params);
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
     * 修改模板信息
     * @return
     */
    @RequestMapping("/updateBusiTemplateDef")
    public @ResponseBody HashMap<String,Object> updateBusiTemplateDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateBusiTemplateDef(params);
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
     * 查询模板
     * @return
     */
    @RequestMapping("/queryBusiTemplateDef")
    public @ResponseBody HashMap<String,Object> queryBusiTemplateDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list = this.service.queryBusiTemplateDef(params);
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
     * 查询模板附件
     * @return
     */
    @RequestMapping("/queryBusiTemplateFile")
    public @ResponseBody HashMap<String,Object> queryBusiTemplateFile() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list = this.service.queryBusiTemplateFile(params);
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
     * 查询模板历史版本
     * @return
     */
    @RequestMapping("/queryBusiTemplateHisByTmplPK")
    public @ResponseBody HashMap<String,Object> queryBusiTemplateHisByTmplPK() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            List list = this.service.queryBusiTemplateHisByTmplPK(params);
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
     * 用于上传模板的资源文件用的，但后续这块可能用的不多，模板的资源文件可能走统一的资源管理了
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/templateFileUpload", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> templateFileUpload(@RequestParam(value="file",required=false) MultipartFile[] files, HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
                FtpInfo ftp = getDeployInfo(env,deployConfMap);
                String fileId = this.service.saveBusiTemplateFile(params,mf,ftp);

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
     * 生成模板的html页面
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/preViewTemplateHtml")
    public @ResponseBody HashMap<String,Object> preViewTemplateHtml()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String filePath = this.service.preViewTemplateHtml(params,ftp);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,filePath);

        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.template, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
     * 导入并自动创建模板（重点功能，用的频繁极高）
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/importCreateBusiTemplate", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> importCreateBusiTemplate(@RequestParam(value="file",required=false) MultipartFile[] files, HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);

            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                List<String> existList = new ArrayList<String>();
                List<String> renameList = new ArrayList<String>();
                this.service.importCreateBusiTemplate(params,mf,existList,renameList,ftp);

                rs.put(Const.RESP_CODE,Const.SuccCode);
                rs.put(Const.RESP_MSG,Const.SUCC);
                rs.put("existList",existList);
                rs.put("renameList",renameList);
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
     * 生成模板的html页面
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/checkTemplateLabelValid")
    public @ResponseBody HashMap<String,Object> checkTemplateLabelValid()throws Exception{
        HashMap rs = new HashMap();
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        try {
            this.service.checkTemplateLabelValid(params,null);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            try {
                logService.insertLog(Dict.Module.template, Dict.ModuleType.publish, (String) params.get("USER_ID"), getIp(request), this.getStackTrace(e), 1);
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
}

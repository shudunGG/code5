package com.om.module.controller.sys;

import com.om.bo.base.Const;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
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
@RequestMapping("/sys")
public class DeployController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(DeployController.class);

    @Resource(name = "DeployService")
    private DeployService service;

    @Autowired
    private Environment env;


    /**
     * 新增部署配置
     * @return
     */
    @RequestMapping("/saveSysConfDeploy")
    public @ResponseBody HashMap<String,Object> saveSysConfDeploy() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.saveSysConfDeploy(params);
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
     * 删除部署配置
     * @return
     */
    @RequestMapping("/deleteSysConfDeploy")
    public @ResponseBody HashMap<String,Object> deleteSysConfDeploy() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteSysConfDeploy(params);
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
     * 修改部署配置信息
     * @return
     */
    @RequestMapping("/updateSysConfDeploy")
    public @ResponseBody HashMap<String,Object> updateSysConfDeploy() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateSysConfDeploy(params);
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
     * 查询部署配置
     * @return
     */
    @RequestMapping("/querySysConfDeploy")
    public @ResponseBody HashMap<String,Object> querySysConfDeploy() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.querySysConfDeploy(params);
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
     * 查询部署配置
     * @return
     */
    @RequestMapping("/querySysConfDeployDefault")
    public @ResponseBody HashMap<String,Object> querySysConfDeployDefault() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {

            String rootPath =   env.getProperty("serverPathUploadPermanent");//永久性上传文件的服务器根目录
            String appRootPath =   env.getProperty("appRootPath");//永久性上传文件的网络访问根目录
            String deployMode =   env.getProperty("deployMode");
            String targetRoot =   env.getProperty("targetRoot");
            String Sftp_host =   env.getProperty("Sftp_host");
            String Sftp_username =   env.getProperty("Sftp_username");
            String Sftp_password =   env.getProperty("Sftp_password");
            String Sftp_port =   env.getProperty("Sftp_port");
            String Sftp_root =   env.getProperty("Sftp_root");
            if(Sftp_root==null || "/".equals(Sftp_root)){
                Sftp_root="";
            }

            Map m = new HashMap();
            m.put("DEPLOY_NAME","");
            m.put("DEPLOY_MODE",deployMode);
            m.put("SOURCE_ROOT",rootPath);
            m.put("URL_ROOT",appRootPath);
            m.put("SFTP_HOST",Sftp_host);
            m.put("SFTP_USER",Sftp_username);
            m.put("SFTP_PWD",Sftp_password);
            m.put("SFTP_PORT",Sftp_port);
            m.put("SFTP_ROOT",Sftp_root);
            m.put("SITE_PK","");
            m.put("TARGET_ROOT",targetRoot);

            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_LIST,m);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }




}

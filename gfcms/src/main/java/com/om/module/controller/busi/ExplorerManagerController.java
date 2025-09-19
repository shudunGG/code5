package com.om.module.controller.busi;

import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.util.RequestUtl;
import com.om.common.util.ZipUtils;
import com.om.module.controller.base.BaseController;
import com.om.module.service.busi.ExplorerManagerService;
import com.om.module.service.busi.TemplateManagerService;
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
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/busi")
public class ExplorerManagerController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(ExplorerManagerController.class);

    @Resource(name = "ExplorerManagerService")
    private ExplorerManagerService service;

    @Autowired
    private Environment env;

    @Resource(name = "DeployService")
    private DeployService deployService;

    /**
     * 在线查看文件内容
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/viewExplorerFiles")
    public @ResponseBody HashMap<String,Object> viewExplorerFiles(HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);

            Map map = this.service.viewExplorerFiles(params,ftp);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_DATA,map);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 上传资源文件，并检查文件是否存在
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/checkExistsExplorerFiles", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> checkExistsExplorerFiles(@RequestParam(value="file",required=false) MultipartFile[] files, HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);

            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                List checkList = this.service.checkExistsExplorerFiles(params,mf,ftp);

                rs.put(Const.RESP_CODE,Const.SuccCode);
                rs.put(Const.RESP_MSG,Const.SUCC);
                rs.put(Const.RESP_DATA,checkList);
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
     * 同步资源文件
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/syncResFileToDeploy")
    public @ResponseBody HashMap<String,Object> syncResFileToDeploy(HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            this.service.syncResFileToDeploy(params,ftp);
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
     * 暂时没有场景使用，建议使用checkExistsExplorerFiles
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/saveExplorerFiles", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> saveExplorerFiles(@RequestParam(value="file",required=false) MultipartFile[] files, HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);

            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);

            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                String fileId = this.service.saveExplorerFiles(params,mf,ftp);

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


    @RequestMapping(value="/deleteExplorerFiles")
    public @ResponseBody HashMap<String,Object> deleteExplorerFiles(HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);

            boolean isDelSuccess = this.service.deleteExplorerFiles(params,ftp);
            if(isDelSuccess){
                rs.put(Const.RESP_CODE,Const.SuccCode);
                rs.put(Const.RESP_MSG,Const.SUCC);
            }else{
                rs.put(Const.RESP_CODE,Const.ErrCode);
                rs.put(Const.RESP_MSG,"文件不存在或者是目录下非空");
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
     * 下载资源文件
     * param{filePath,fileName}
     * @return
     */
    @RequestMapping("/downloadExplorerFiles")
    public @ResponseBody void  downloadExplorerFiles() throws Exception {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        String CUR_CONTEXT = (String)request.getParameter("CUR_CONTEXT");
        String DOWN_FILE = (String)request.getParameter("DOWN_FILE");//要删除的目录或者文件

        if("..".equals(DOWN_FILE) || DOWN_FILE.indexOf("*")>-1|| DOWN_FILE.indexOf("/")>-1){
            throw new Exception("选中的目录名["+DOWN_FILE+"]值非法");
        }

        Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
        FtpInfo ftp = getDeployInfo(env,deployConfMap);
        String rootPath =   ftp.getRootPath();
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {

            String contextPath = CUR_CONTEXT;//
            String currDir = rootPath + contextPath + "/" + DOWN_FILE;
            logger.info("文件删除的路径为[file delete path]:" + currDir);
            File file = new File(currDir);
            if(!file.exists()){
                throw new Exception("文件不存在【FILE NOT EXISTS】"+ currDir);
            }
            if(file.isDirectory()){
                //FileOutputStream fos = new FileOutputStream(new File("D:\\test\\1613722789624.zip"));
                response.setContentType("application/force-download");// 设置强制下载不打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(DOWN_FILE+".zip", "UTF-8") );// 设置文件名

                OutputStream os = response.getOutputStream();
                ZipUtils.toZip(currDir, os, true);
            }else{
                response.setContentType("application/force-download");// 设置强制下载不打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(DOWN_FILE, "UTF-8") );// 设置文件名

                byte[] buffer = new byte[1024];
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            }


            logger.info("success");
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @RequestMapping(value="/openExplorerFiles")
    public @ResponseBody String openExplorerFiles(HttpServletRequest request, HttpServletResponse response)throws Exception{
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            return this.service.openExplorerFiles(params,ftp);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return "";
        }
    }

    @RequestMapping(value="/writeExplorerFiles")
    public @ResponseBody HashMap<String,Object> writeExplorerFiles(HttpServletRequest request, HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            this.service.writeExplorerFiles(params,ftp);
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

package com.om.module.controller.common;

import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.common.UploadService;
import com.om.module.service.sys.DeployService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//@RestController
@Controller
@RequestMapping("/common")
//@CrossOrigin 这个注解是解决跨域问题，这里不需要
public class UploadController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private Environment env;


    @Resource(name = "UploadService")
    private UploadService service;

    @Resource(name = "DeployService")
    private DeployService deployService;

    /**
     * @deprecated 该方法已经废弃使用，当前标记日期是2022-6-15
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/fileUpload", method = RequestMethod.POST)
    public @ResponseBody HashMap<String,Object> fileUpload(@RequestParam(value="file",required=false)MultipartFile[] files,HttpServletRequest request,HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String UPLOAD_TYPE = request.getParameter("UPLOAD_TYPE");
            String UPLOAD_USER_ID = request.getParameter("UPLOAD_USER_ID");
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String rootPath = ftp.getRootPath();
            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                String fileId = this.service.saveCmUploadRecord(params,rootPath,mf,ftp);

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
     * html编辑器里上传图片用的
     * @param files
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/fileUploadHtmlImage", method = RequestMethod.POST)
    public @ResponseBody String fileUploadHtmlImage(@RequestParam(value="file",required=false)MultipartFile[] files,HttpServletRequest request,HttpServletResponse response)throws Exception{
        HashMap rs = new HashMap();
        try {
            HashMap<String,Object> params = RequestUtl.getRequestMap(request);
            String UPLOAD_TYPE = request.getParameter("UPLOAD_TYPE");
            String UPLOAD_USER_ID = request.getParameter("UPLOAD_USER_ID");


            logger.debug("file-object:"+files);
            logger.debug("file-number:" + files.length);
            Map deployConfMap = deployService.querySysConfDeployBySitePk(params);
            FtpInfo ftp = getDeployInfo(env,deployConfMap);
            String rootPath = ftp.getRootPath();
            String appRootPath = ftp.getAppRootPath();
            if (files != null && files.length > 0) {
                MultipartFile mf = files[0];
                String fileId = this.service.saveCmUploadRecord(params,rootPath,mf,ftp);
                String context = params.get("PATH_CONTEXT").toString();
                String PATH_FILE_NAME = params.get("PATH_FILE_NAME").toString();
                /*String newUrl = (String)params.get("newUrl");
                if(newUrl!=null){
                    return newUrl;
                }*/
                if(!context.endsWith("/")){
                    context = context+"/";
                }
                return appRootPath+context+PATH_FILE_NAME;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return "";
    }


    /**
     * 下载文件的接口
     * param{filePath,fileName}
     * @return
     */
    @RequestMapping("/downloadFile")
    public @ResponseBody void  downloadTmpl() {
        String fileName = request.getParameter("fileName");
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            if("catalogTmpl".equals(fileName)){
                String catalogTmpl = env.getProperty("catalogTmplPath");
                File file = new File(catalogTmpl);
                fileName="模板文件.xlsx";
                response.setContentType("application/force-download");// 设置强制下载不打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8") );// 设置文件名

                byte[] buffer = new byte[1024];
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }

            }else{
                throw new Exception("未知的文件："+fileName);
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
}

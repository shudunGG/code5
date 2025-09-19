package com.om.module.service.common;
import com.om.bo.base.FtpInfo;
import com.om.bo.element.DyncTreeBo;
import com.om.common.cache.Dict;
import com.om.common.util.ObjectTools;
import com.om.common.util.Pk;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("UploadService")
public class UploadService extends CommonService {

    public String saveCmUploadRecord(Map param, String rootPath, MultipartFile mf, FtpInfo ftp) throws Exception {
        String UPLOAD_TYPE = (String)param.get("UPLOAD_TYPE");
        String UPLOAD_USER_ID = (String)param.get("UPLOAD_USER_ID");
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("UPLOAD_USER_ID",UPLOAD_USER_ID);
        this.isNull("SITE_PK",SITE_PK);
//        if(SITE_PK == null){
//            SITE_PK="SDac0b7073ea2b4f6392b5549792acda1d";
//            logger.info("注意，这里的SITE_PK是写死的，后期一定要修改掉:" + SITE_PK);
//        }
        Map map = (Map)this.baseService.getObject("busiMapper.queryBusiSiteDef",param);
        String SITE_CODE = (String)map.get("SITE_CODE");
        String DOMAIN_URL = (String)map.get("DOMAIN_URL");

        String contextPath = SITE_CODE+sep+ObjectTools.getCurMonth();
        String currDir = rootPath + contextPath + sep;
        logger.info("文件上传路径为[file upload path]:" + currDir);
        File file = new File(currDir);
        if (!file.exists()) file.mkdirs();

        String file_init = mf.getOriginalFilename(); //得到文件名称
        String fix = file_init.substring(file_init.lastIndexOf(".")+1, file_init.length());
        String file_new = System.currentTimeMillis() +"."+ fix; //生成一个新的文件名称

        String dstPathAndFile = currDir + sep + file_new;
        File file1 = new File(dstPathAndFile); // 新建一个文件
        try {
            mf.transferTo(file1);
            logger.info("=======文件上传成功[file upload success]====" + dstPathAndFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        //这是因为如果有域名的话，以前IP访问的方式通常访问不了，改用域名方式，以后改造建议改表头的方式，就不用到处定义链接前缀了
        //改了报错，图片传不上去，暂时回退 回来
        /*if(DOMAIN_URL!=null || DOMAIN_URL.length()>2){
            String newUrl = DOMAIN_URL+sep+"document"+sep+ObjectTools.getCurMonth();
            param.put("newUrl",newUrl);
        }*/

        param.put("UPLOAD_TYPE", UPLOAD_TYPE);
        param.put("PATH_ROOT", rootPath);
        param.put("PATH_CONTEXT", contextPath + sep);
        param.put("PATH_FILE_NAME", file_new);

        param.put("FILE_INIT_NAME", file_init);
        param.put("UPLOAD_USER_ID", UPLOAD_USER_ID);
        param.put("FIX", fix);
        param.put("FILE_LENGTH", "" + mf.getSize());
        String uuid = Pk.getId("U");
        param.put("ID",uuid);

        boolean isDeploy = this.deployFile(rootPath, ftp.getTargetRoot(), contextPath, file_new, ftp);
        if(isDeploy){
            param.put("IS_DEPLOY", Dict.DeploySts.succ);
        }else{
            param.put("IS_DEPLOY",Dict.DeploySts.err);
        }
        this.baseService.insert("commonMapper.saveCmUploadRecord",param);

        return uuid;
    }



    public List queryCmUploadRecord(Map param) throws Exception {
        List list = this.baseService.getList("sysMapper.queryCmUploadRecord",param);
        return list;
    }



}

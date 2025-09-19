package com.om.module.service.busi;
import com.om.bo.base.FtpInfo;
import com.om.bo.base.FtpPathInfo;
import com.om.bo.element.DyncTreeBo;
import com.om.common.cache.Dict;
import com.om.common.util.*;
import com.om.module.service.common.CommonService;
import com.om.module.service.label.ABaseLabel;
import com.om.module.service.label.GfDocumentLabel;
import com.om.module.service.label.GfDocumentsLabel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("TemplateManagerService")
public class TemplateManagerService extends CommonService {

    public void saveBusiTemplateDef(Map param) throws Exception {
        String TMPL_NAME = (String)param.get("TMPL_NAME");
        String EXT_NAME = (String)param.get("EXT_NAME");
        String TMPL_DESC = (String)param.get("TMPL_DESC");
        String TMPL_TYPE = (String)param.get("TMPL_TYPE");
        String TMPL_LABEL = (String)param.get("TMPL_LABEL");
        String PUB_FILE_NAME = (String)param.get("PUB_FILE_NAME");
        String TMPL_FACE_PIC = (String)param.get("TMPL_FACE_PIC");
        String TMPL_CONTENT = (String)param.get("TMPL_CONTENT");
        String IS_GLOBAL = (String)param.get("IS_GLOBAL");
        String OP_USER = (String)param.get("OP_USER");
        String TMPL_CODE = (String)param.get("TMPL_CODE");
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("SITE_PK",SITE_PK);
        this.isNull("TMPL_NAME",TMPL_NAME);
        this.isNull("EXT_NAME",EXT_NAME);
        this.isNull("TMPL_TYPE",TMPL_TYPE);
        this.isNull("TMPL_CONTENT",TMPL_CONTENT);
        this.isNull("IS_GLOBAL",IS_GLOBAL);
        this.isNull("OP_USER",OP_USER);
        this.isNull("TMPL_CODE",TMPL_CODE);
        this.isEnum("IS_GLOBAL",IS_GLOBAL,"0,1");
        String uuid = Pk.getId("C");
        param.put("TMPL_PK",uuid);
        param.put("VERSION_NO",0);
        checkTemplateLabelValid(param,TMPL_CONTENT);
        this.baseService.insert("busiMapper.saveBusiTemplateDef",param);
    }

    public void deleteBusiTemplateDef(Map param) throws Exception {
        String TMPL_PK = (String)param.get("TMPL_PK");
        this.isNull("TMPL_PK",TMPL_PK);
        //删除模板，要增加对模板引用的判断，只要被使用，就不能删除
        List list = this.baseService.getList("busiMapper.queryTemplateUsed",param);
        if(list.size()>0){
            StringBuffer sb = new StringBuffer("对不起，有以下的站点、栏目或者文章使用了该模板，无法删除:");
            for(int i=0;i<list.size();i++){
                Map map = (Map)list.get(i);
                String type = map.get("TYPE2").toString();
                String name = map.get("NAME2").toString();
                if("1".equals(type)){
                    sb.append("站点名称【").append(name).append("】");
                }else if("2".equals(type)){
                    sb.append("栏目名称【").append(name).append("】");
                }else if("3".equals(type)){
                    sb.append("文章标题【").append(name).append("】");
                }
            }
            throw new Exception(sb.toString());
        }


        this.baseService.insert("busiMapper.deleteBusiTemplateDef",param);
    }

    public void updateBusiTemplateDef(Map param) throws Exception {
        String TMPL_PK = (String)param.get("TMPL_PK");
        String OP_USER = (String)param.get("OP_USER");
        String TMPL_CONTENT = (String)param.get("TMPL_CONTENT");
        this.isNull("TMPL_PK",TMPL_PK);
        this.isNull("OP_USER",OP_USER);
        checkTemplateLabelValid(param,TMPL_CONTENT);
        this.baseService.insert("busiMapper.saveBusiTemplateHis",param);
        this.baseService.insert("busiMapper.updateBusiTemplateDef",param);
    }



    public List queryBusiTemplateDef(Map param) throws Exception {
        List list = this.baseService.getList("busiMapper"+Dict.dbMap+".queryBusiTemplateDef",param);
        return list;
    }

    public List queryBusiTemplateFile(Map param) throws Exception {
        String TMPL_PK = (String)param.get("TMPL_PK");
        this.isNull("TMPL_PK",TMPL_PK);
        List list = this.baseService.getList("busiMapper.queryBusiTemplateFile",param);
        return list;
    }

    /**
     * 查询模板历史版本
     * @param param
     * @return
     * @throws Exception
     */
    public List queryBusiTemplateHisByTmplPK(Map param) throws Exception {
        String TMPL_PK = (String)param.get("TMPL_PK");
        this.isNull("TMPL_PK",TMPL_PK);
        List list = this.baseService.getList("busiMapper"+Dict.dbMap+".queryBusiTemplateHisByTmplPK",param);
        return list;
    }

    /**
     * 用于上传模板的资源文件用的，但后续这块可能用的不多，模板的资源文件可能走统一的资源管理了
     * @param param
     * @param mf
     * @return
     * @throws Exception
     */
    public String saveBusiTemplateFile(Map param, MultipartFile mf, FtpInfo ftp) throws Exception {
        String TMPL_PK = (String)param.get("TMPL_PK");
        String TMPL_CODE = (String)param.get("TMPL_CODE");
        String SITE_PK = (String)param.get("SITE_PK");
        String U_USER = (String)param.get("U_USER");
        String OP_TYPE = (String)param.get("OP_TYPE");
        String SITE_CODE = (String)param.get("SITE_CODE");
        this.isNull("TMPL_PK",TMPL_PK);
        this.isNull("U_USER",U_USER);
        this.isEnum("OP_TYPE",OP_TYPE,"0,1,2");

        List list = this.baseService.getList("busiMapper.queryTmplAndSiteCodeByTmplPk",param);
        if(list.size()>0){
            Map rsMap = (Map)list.get(0);
            TMPL_CODE = (String)rsMap.get("TMPL_CODE");
            SITE_PK = (String)rsMap.get("SITE_PK");
            SITE_CODE = (String)rsMap.get("SITE_CODE");
        }

        this.isNull("TMPL_CODE",TMPL_CODE);
        this.isNull("SITE_PK",SITE_PK);
        String rootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();
        //这里的上的是让模板的附件放到相应的站点下面:根目录-站点-文档/模板-模板标识
        //String contextPath = SITE_CODE + sep+ "template" + sep + TMPL_CODE;//为了和生成html相对资源文件统一对齐，资源统一放到站点下面
        String contextPath = SITE_CODE + sep+ "template" ;//
        String currDir = rootPath + contextPath + sep;
        logger.info("文件上传路径为[file upload path]:" + currDir);
        File file = new File(currDir);
        if (!file.exists()) file.mkdirs();

        String file_init = mf.getOriginalFilename(); //得到文件名称
        String fix = file_init.substring(file_init.lastIndexOf(".")+1, file_init.length());
        String file_new = System.currentTimeMillis() +"."+ fix; //生成一个新的文件名称

        String dstPathAndFile = currDir + sep + file_init;
        File file1 = new File(dstPathAndFile); // 新建一个文件
        if(file1.exists()){
            if("0".equals(OP_TYPE)){
                file1.delete();
            }else if("1".equals(OP_TYPE)){
                String preFix = file_init.substring(0,file_init.indexOf("."));
                String saveAsName = preFix+"_"+System.currentTimeMillis()+"."+fix;
                param.put("FILE_SAVE_AS_NAME",saveAsName);
                file1.renameTo(new File(saveAsName));
            }else if("2".equals(OP_TYPE)){
                throw new Exception("文件《"+file_init+"》已经存在！file was exists");
            }
        }

        try {
            mf.transferTo(file1);
            logger.info("=======文件上传成功file upload success====" + dstPathAndFile);
            if("zip".equals(fix.toLowerCase())){
                List<String> fileList = UZipFile.unZipFiles(file1, currDir + sep);
                List<FtpPathInfo> pathInfoList = new ArrayList<FtpPathInfo>();
                FtpPathInfo bo = null;
                for(String zipFilePath:fileList){
                    int start = zipFilePath.lastIndexOf("/");
                    String fPath = zipFilePath.substring(0,start);
                    String ftpPath = contextPath+sep+fPath;
                    String sourcePath = currDir + zipFilePath;
                    File sourcefile = new File(sourcePath);
                    String targetPath = ftp.getTargetRoot() + contextPath + sep + zipFilePath;
                    File targetfile = new File(targetPath);
                    bo = new FtpPathInfo(ftpPath,sourcefile,targetfile);
                    pathInfoList.add(bo);
                }
                //多个文件部署
                this.deployFile(pathInfoList,ftp);
            }else{
                //单个文件部署
                this.deployFile(rootPath, ftp.getTargetRoot(), contextPath, file_init, ftp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        param.put("FILE_PATH_ROOT", rootPath);
        param.put("FILE_PATH_CONTEXT", contextPath + sep);
        param.put("FILE_NAME_NEW", file_new);
        param.put("FILE_NAME_INIT", file_init);
        param.put("FILE_NAME_EXT", fix);
        param.put("FILE_LENGTH", mf.getSize());
        param.put("FILE_PATH_URL", appRoot+sep+contextPath);
        String uuid = Pk.getId("F");
        param.put("FILE_PK",uuid);
        this.baseService.insert("busiMapper.saveBusiTemplateFile",param);
        return uuid;
    }

    public void findAllIncludeTmplAndRepublish(Map siteMap,FtpInfo ftp) throws Exception {
        String SITE_PK = (String) siteMap.get("SITE_PK");
        this.isNull("SITE_PK", SITE_PK);
        List tmplList = this.baseService.getList("busiMapper.queryIncludeTmplBySitePk", siteMap);
        for(int i=0;i<tmplList.size();i++){
            Map tmplMap = (Map)tmplList.get(i);
            preViewTemplateHtml(tmplMap,ftp);
        }
    }


    /**
     * 模板生成页面index.html，仅支持数据都是置标里自确定的
     * 1 查找根据模板编号查找模板的信息
     * 2 生成页面为站点下的html文件
     * 3 将生成的结果写入字段tmpl_html_path,tmpl_html_content
     * @param param
     * @return 生成的预览地址
     * @throws Exception
     */
    public String preViewTemplateHtml(Map param,FtpInfo ftp) throws Exception {
        String TMPL_PK = (String)param.get("TMPL_PK");
        this.isNull("TMPL_PK",TMPL_PK);
        String serverRootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();
        List tmplList = this.baseService.getList("busiMapper.queryBusiTemplateDefByTmplPk",param);

        Map tmplMap = (Map)tmplList.get(0);
        if(tmplMap == null ){
            throw new Exception("未找到指定的模板，请检查！【TMPL_PK】="+TMPL_PK);
        }
        String TMPL_CODE = (String)tmplMap.get("TMPL_CODE");
        String TMPL_CONTENT = (String)tmplMap.get("TMPL_CONTENT");
        String SITE_CODE = (String)tmplMap.get("SITE_CODE");
        String PUB_FILE_NAME = (String)tmplMap.get("PUB_FILE_NAME");
        String PUB_FILE_NAME2 = (String)tmplMap.get("PUB_FILE_NAME");
        String EXT_NAME = (String)tmplMap.get("EXT_NAME");
        String DOMAIN_URL = (String)tmplMap.get("DOMAIN_URL");
        if(EXT_NAME ==null){
            EXT_NAME = "html";
        }
        if(PUB_FILE_NAME != null) {
            if (PUB_FILE_NAME.indexOf(EXT_NAME) < 0) {
                PUB_FILE_NAME = PUB_FILE_NAME + "." + EXT_NAME;
                PUB_FILE_NAME2= PUB_FILE_NAME + "_2." + EXT_NAME;
            }
        }else{
            PUB_FILE_NAME = DateUtil.getCurDayRandomId()+"." + EXT_NAME;
            PUB_FILE_NAME2 = DateUtil.getCurDayRandomId()+"_2." + EXT_NAME;
        }

        Map inputMap = tmplMap;

        StringBuffer sb = new StringBuffer(TMPL_CONTENT);
        ABaseLabel label = new GfDocumentLabel(TMPL_CONTENT);
        label.setInputMap(inputMap);
        label.switchLabel(sb,inputMap,baseService,1,1);////这里仅仅是一个示例

        logger.debug("文档的内容："+sb.toString());

        //String context = SITE_CODE;//这是其它生成html的路径，下面是模板的，因为模板比较特殊，一般的模板不需要生成html,通常只有嵌套模板才生成html,也就是这个html也当附件使用，因此需要这样设计
        String context = SITE_CODE + sep+ "template" ;//;
        String file = PUB_FILE_NAME;//文件名称
        String path = serverRootPath+sep+context+sep;
        logger.info("模板的html文件生成路径为[path]:" + path);
        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }
        FileWriter fw = new FileWriter(path+file);
        fw.write(sb.toString());
        fw.close();
        //更新了html里的path，如果是绝对地址就不变，如果是相对地址，加了../，也就是适用于细览
        String newHtml = this.changeHtmlPath(sb.toString());
        fw = new FileWriter(path+PUB_FILE_NAME2);
        fw.write(sb.toString());
        fw.close();

        String path1 = sep+ "template"+sep+file;//replaceDomainUrl(appRoot+sep+context+sep+file,DOMAIN_URL,SITE_CODE);
        param.put("TMPL_HTML_PATH",path1);
        param.put("TMPL_HTML_CONTENT",sb.toString());

        String path2 = sep+ "template"+sep+PUB_FILE_NAME2;//replaceDomainUrl(appRoot+sep+context+sep+PUB_FILE_NAME2,DOMAIN_URL,SITE_CODE);
        param.put("TMPL_HTML_PATH2",path2);
        param.put("TMPL_HTML_CONTENT2",newHtml);

        boolean isDeploy = this.deployFile(serverRootPath, ftp.getTargetRoot(), context, file, ftp);
        isDeploy = this.deployFile(serverRootPath, ftp.getTargetRoot(), context, PUB_FILE_NAME2, ftp);
        if(isDeploy){
            param.put("IS_DEPLOY", Dict.DeploySts.succ);
        }else{
            param.put("IS_DEPLOY",Dict.DeploySts.err);
        }
        this.baseService.insert("busiMapper.updateBusiTemplateDef",param);

        return appRoot+sep+context+sep+file;
    }

    /**
     * 更新了html里的path，如果是绝对地址就不变，如果是相对地址，加了../，也就是适用于细览
     * @param html
     * @return
     * @throws DocumentException
     */
    private String changeHtmlPath(String html) throws DocumentException {
        Document doc1 = Jsoup.parse(html);
        Elements imgs = doc1.select("img");
        for(int i=0;i<imgs.size();i++){
            String src=imgs.eq(i).attr("src");
            logger.debug("changeHtmlPath:"+i+":"+src);
            if(src.indexOf("http://")>=0 ||src.indexOf("https://")>=0 || src.indexOf("pngbase64")>=0){

            }else if(src.startsWith("..")){
                doc1.select("img").eq(i).attr("src","../"+src);
            }else{
                logger.debug("需要观察的src："+src);
            }
        }
        return doc1.toString();
    }


    /**
     * 导入并自动创建模板
     * @param param
     * @param mf
     * @param existList
     * @param renameList
     * @throws Exception
     */
    public void importCreateBusiTemplate(Map param,  MultipartFile mf,
                                         List<String> existList,List<String> renameList,FtpInfo ftp) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        String OP_USER = (String)param.get("OP_USER");
        String OP_TYPE = (String)param.get("OP_TYPE");//0覆盖 1重命名 2返回

        String SITE_CODE = (String)param.get("SITE_CODE");
        this.isNull("SITE_PK",SITE_PK);
        this.isNull("OP_USER",OP_USER);
        this.isNull("OP_TYPE",OP_TYPE);
        this.isEnum("OP_TYPE",OP_TYPE,"0,1,2");

        if(SITE_CODE == null || "".equals(SITE_CODE)){
            List list = this.baseService.getList("busiMapper.queryBusiSiteDef",param);
            if(list.size()>0){
                Map rsMap = (Map)list.get(0);
                SITE_CODE = (String)rsMap.get("SITE_CODE");
            }
        }
        this.isNull("SITE_CODE",SITE_CODE);
        String rootPath = ftp.getRootPath();
        //这里的上的是让模板的附件放到相应的站点下面:根目录-站点-文档/模板
        String siteRoot = rootPath + SITE_CODE + sep; //
        String tmplRoot = siteRoot  + "template" + sep;

        String contextRoot = SITE_CODE;//
        String contextTmpl = SITE_CODE + sep+ "template";//

//        String contextPath = SITE_CODE + sep+ "template";//
//        String currDir = rootPath + contextPath + sep;
        logger.info("文件上传路径为[file upload path]:" + siteRoot);
        File file = new File(tmplRoot);
        if (!file.exists()) file.mkdirs();

        String file_init = mf.getOriginalFilename(); //得到文件名称
        String fix = file_init.substring(file_init.lastIndexOf(".")+1, file_init.length());
        String file_new = System.currentTimeMillis() +""; //生成一个新的文件名称

        try {

            if("zip".equals(fix.toLowerCase())){
                logger.info("enter zip file process");
                String dstPathAndFile = siteRoot + file_new+"."+ fix;
                File file1 = new File(dstPathAndFile); // 新建一个文件
                mf.transferTo(file1);
                logger.info("=======文件上传成功file upload success====" + dstPathAndFile);
                //压缩文件起一个随机名称，所以不存在压缩包存在的问题
                //步骤1：解压

                List<String> fileList = UZipFile.unZipFiles(file1, siteRoot);
                logger.info("unzip file size:"+fileList.size());
                //步骤2：找出这个目录下的所有html模板文件
                List<String> htmlList = UZipFile.filterFile(fileList ,"html");
                List<String> resFileList = UZipFile.filterOtherFile(fileList ,"html");
                logger.info("html file size:"+htmlList.size());
                //这是用来部署的列表
                List<FtpPathInfo> deployList = new ArrayList<FtpPathInfo>();
                for(String htmlPath:htmlList){
                    logger.info("htmlPath:"+htmlPath);
                    //步骤3：将这些html文件移动到位置 ： SITE_CODE / "template" /
                    int start = htmlPath.lastIndexOf("/");
                    int end = htmlPath.lastIndexOf(".html");
                    String TMPL_CODE = htmlPath.substring(start+1,end);
                    String fileName = TMPL_CODE +".html";
                    //htmlPath和fileName的区别在于,htmlPath带zip里面的路径,而fileName仅是文件名


                    file_init = fileName;
                    fix = "html";

                    String tmplFilePath = tmplRoot   + fileName;
                    File tmplFileTarget = new File(tmplFilePath);
                    //如果文件存在，需要根据操作类型，对文件进行处理
                    if(tmplFileTarget.exists()){
                        existList.add(fileName);

                        if("0".equals(OP_TYPE)){////0覆盖 1重命名 2返回
                            tmplFileTarget.delete();
                            tmplFileTarget = new File(tmplFilePath);
                        }else if("1".equals(OP_TYPE)){
                            fileName = TMPL_CODE+"_1."+fix;
                            tmplFilePath = tmplRoot + fileName;
                            renameList.add(fileName);
                            tmplFileTarget = new File(tmplFilePath);
                        }else{
                            continue;
                        }
                    }
                    //移动文件到这里
                    File srcFile = new File(siteRoot + sep +htmlPath);
                    boolean renameRs = srcFile.renameTo(tmplFileTarget);
                    logger.info("file rename rs:"+renameRs);
                    logger.info("file rename srcFile:"+srcFile.getAbsolutePath());
                    logger.info("file rename target:"+tmplFileTarget.getAbsolutePath());

                    //步骤4：入库
                    String tmplContent = TxtFileUtils.readTxtToString(tmplFilePath).toString();
                    this.saveTemplateDef(param,file_init,fix,tmplContent);



                    String ftpPath = contextTmpl+sep;
                    String sourcePath = tmplRoot + fileName;//资源文件里，html因为是被直接移动到template下面的，所以这里用fileName
                    File sourcefile = new File(sourcePath);
                    String targetPath = ftp.getTargetRoot() + contextTmpl  + sep + fileName;
                    File targetfile = new File(targetPath);
                    FtpPathInfo bo = new FtpPathInfo(ftpPath,sourcefile,targetfile);
                    deployList.add(bo);
                }

                for(String htmlPath:resFileList){
                    String ftpPath = contextRoot+sep;
                    String sourcePath = siteRoot + htmlPath;//资源文件里，要按zip里的路径来，所以这里用htmlPath
                    File sourcefile = new File(sourcePath);
                    String targetPath = ftp.getTargetRoot() + contextRoot  + sep + htmlPath;
                    File targetfile = new File(targetPath);
                    FtpPathInfo bo = new FtpPathInfo(ftpPath,sourcefile,targetfile);
                    deployList.add(bo);
                }
                //多个文件部署
                this.deployFile(deployList,ftp);
                UZipFile.delDir(file1);

                //步骤5：将这个压缩包及解压后的文件删除

            }else{//html
                String TMPL_CODE = file_init.substring(0,file_init.lastIndexOf("."));
                String dstPathAndFile = tmplRoot + file_init;

                File file1 = new File(dstPathAndFile); // 新建一个文件
                if(file1.exists()){
                    existList.add(file_init);
                    if("0".equals(OP_TYPE)){////0覆盖 1重命名 2返回
                        mf.transferTo(file1);
                    }else if("1".equals(OP_TYPE)){
                        String newName = TMPL_CODE+"_1."+fix;
                        file_init = newName;
                        dstPathAndFile = siteRoot + TMPL_CODE+sep+newName;
                        renameList.add(newName);
                        file1 = new File(dstPathAndFile);
                        mf.transferTo(file1);
                    }else{
                        return;
                    }
                }else{
                    mf.transferTo(file1);
                }
                //单个文件部署
                this.deployFile(rootPath, ftp.getTargetRoot(), contextTmpl, file_init, ftp);

                String tmplContent = TxtFileUtils.readTxtToString(dstPathAndFile).toString();
                this.saveTemplateDef(param,file_init,fix,tmplContent);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 保存模板内容
     * @param param
     * @param file_init
     * @param fix
     * @param tmplContent
     * @throws Exception
     */
    private void saveTemplateDef(Map param,String file_init,String fix,String tmplContent)throws Exception {

        file_init = file_init.replaceAll("."+fix,"");

        param.put("TMPL_NAME",file_init);
        param.put("EXT_NAME",fix.toLowerCase());
        if(file_init.indexOf("细览")>-1){
            param.put("TMPL_TYPE","2");
        }else if(file_init.indexOf("嵌套")>-1){
            param.put("TMPL_TYPE","3");
        }else{
            param.put("TMPL_TYPE","1");
        }
        param.put("TMPL_LABEL",file_init);
        param.put("PUB_FILE_NAME",file_init+"."+fix);

        param.put("IS_GLOBAL","0");
        param.put("TMPL_CODE",file_init);
        param.put("TMPL_CONTENT",tmplContent);

        String pre="/";
        Document doc1 = Jsoup.parse(tmplContent);
        Elements imgs = doc1.select("img");
        for(int i=0;i<imgs.size();i++){
            String src=imgs.eq(i).attr("src");
            System.out.println("updateBusiTemplateDefRelativeAddr:"+i+":"+src);
            if(src.indexOf("http://")>=0 ||src.indexOf("https://")>=0 || src.indexOf("pngbase64")>=0 || src.startsWith("..")){

            }else if(!src.startsWith("/")){
                doc1.select("img").eq(i).attr("src",pre+src);
            }
        }

        Elements links = doc1.select("link");
        for(int i=0;i<links.size();i++){
            String href=links.eq(i).attr("href");
            System.out.println("updateBusiTemplateDefRelativeAddr:"+i+":"+href);
            if(href.indexOf("http://")>=0 ||href.indexOf("https://")>=0 || href.indexOf("pngbase64")>=0 || href.startsWith("..")){

            }else  if(!href.startsWith("/")){
                doc1.select("link").eq(i).attr("href",pre+href);
            }
        }


        Elements script = doc1.select("script");
        for(int i=0;i<script.size();i++){
            String src=script.eq(i).attr("src");
            System.out.println("updateBusiTemplateDefRelativeAddr:"+i+":"+src);
            if(src.indexOf("http://")>=0 ||src.indexOf("https://")>=0 || src.indexOf("pngbase64")>=0 || src.startsWith("..")){

            }else if(!src.startsWith("/")){
                doc1.select("script").eq(i).attr("src",pre+src);
            }
        }
        tmplContent  = doc1.toString();
        param.put("TMPL_CONTENT",tmplContent);


        //检查模板是否存在，如果存在，先删除掉的
        List list = this.baseService.getList("busiMapper.queryCheckTmplIsExist",param);
        if(list.size()>0){
            logger.info("模板存在，更新模板");
            Map m = (Map) list.get(0);
            param.put("TMPL_PK",m.get("TMPL_PK"));

            updateBusiTemplateDef(param);
        }else{
            this.saveBusiTemplateDef(param);
        }
    }

    public void checkTemplateLabelValid(Map param,String TMPL_CONTENT ) throws Exception {
        Map inputMap = null;
        if(TMPL_CONTENT==null) {
            String TMPL_PK = (String) param.get("TMPL_PK");
            this.isNull("TMPL_PK", TMPL_PK);

            List tmplList = (List) this.baseService.getList("busiMapper.queryChannnelTmplInfoByTmplPk", param);
            if (tmplList.size() == 0) {
                throw new Exception("未找到该模板，请检查，TMPL_PK:" + TMPL_PK);
            }
            Map tmplMap = (Map) tmplList.get(0);
            TMPL_CONTENT = (String) tmplMap.get("TMPL_CONTENT");
            inputMap = tmplMap;
        }else{
            inputMap = param;
        }
        ABaseLabel label = new GfDocumentsLabel(TMPL_CONTENT);
        label.setInputMap(inputMap);
        label.setCurPage(0);//这里手工强制设置为0的目的是，这上置标的分页不按普通分页走，按取最大值走

        StringBuffer sb = new StringBuffer(TMPL_CONTENT);
        label.setInputMap(inputMap);
        label.setCurPage(1);
        label.switchCheck(sb, inputMap, baseService, 1, 1, label.totalRecord);////这里仅仅是一个示例
    }

}

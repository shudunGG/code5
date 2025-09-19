package com.om.module.service.busi;
import com.om.bo.base.FtpInfo;
import com.om.bo.base.FtpPathInfo;
import com.om.bo.busi.MyLabelBo;
import com.om.common.cache.Dict;
import com.om.common.cache.MyLableCache;
import com.om.common.util.DateUtil;
import com.om.common.util.ObjectTools;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import com.om.module.service.label.ABaseLabel;
import com.om.module.service.label.GfDocumentLabel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.util.*;

@Service("DocumentManagerService")
public class DocumentManagerService extends CommonService {


    /**
     *
     * @param param
     * @param mf
     * @return
     * @throws Exception
     */
    public String saveBusiDocumentFile(Map param, MultipartFile mf, FtpInfo ftp) throws Exception {
        String SHOW_SEQ = (String)param.get("SHOW_SEQ");
        String FILE_TYPE = (String)param.get("FILE_TYPE");
        String U_USER = (String)param.get("U_USER");
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        String DOCUMENT_PK = (String)param.get("DOCUMENT_PK");
        this.isNull("FILE_TYPE",FILE_TYPE);
        this.isNull("U_USER",U_USER);
        this.isNull("CHANNEL_PK",CHANNEL_PK);
        this.isNull("DOCUMENT_PK",DOCUMENT_PK);
        this.isEnum("FILE_TYPE",FILE_TYPE,"1,2,3");
        if(SHOW_SEQ==null){
            SHOW_SEQ="0";
        }
        String rootPath  = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();

        //这里的上的是让文档的附件放到相应的栏目下面:根目录-站点-栏目-文档/模板-月份
        Map DocumentPathContext = (Map)this.baseService.getObject("busiMapper.queryDocumentPathContext",param);
        String SITE_CODE = DocumentPathContext.get("SITE_CODE").toString();
        String CHANNEL_CODE = DocumentPathContext.get("CHANNEL_CODE").toString();
        String SITE_PK = DocumentPathContext.get("SITE_PK").toString();
        String DOMAIN_URL = (String)DocumentPathContext.get("DOMAIN_URL");


        String contextPath = SITE_CODE + sep +  ObjectTools.getCurMonth() ;

        String currDir = rootPath + contextPath + sep;
        logger.info("文件上传路径为:" + currDir);
        File file = new File(currDir);
        if (!file.exists()) file.mkdirs();

        String file_init = mf.getOriginalFilename(); //得到文件名称
        String fix = file_init.substring(file_init.lastIndexOf(".")+1, file_init.length());
        String file_new = System.currentTimeMillis() +"."+ fix; //生成一个新的文件名称

        String dstPathAndFile = currDir + sep + file_new;
        File file1 = new File(dstPathAndFile); // 新建一个文件
        try {
            mf.transferTo(file1);
            logger.info("=======文件上传成功====" + dstPathAndFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        //String homePage = replaceDomainUrl(appRoot  + contextPath + sep + file_new,DOMAIN_URL,SITE_CODE);
        String homePage = sep +  ObjectTools.getCurMonth() + sep + file_new;
        param.put("SITE_PK",SITE_PK);
        param.put("SHOW_SEQ",SHOW_SEQ);
        param.put("FILE_PATH_ROOT", rootPath);//真实物理地址的头，这个是固定死的
        param.put("FILE_PATH_CONTEXT", contextPath + sep);//从站点开始的地址
        param.put("FILE_NAME_NEW", file_new);
        param.put("FILE_NAME_INIT", file_init);
        param.put("FILE_NAME_EXT", fix);
        param.put("FILE_LENGTH", mf.getSize());
        param.put("FILE_PATH_URL", homePage);
        String uuid = Pk.getId("F");
        param.put("FILE_PK",uuid);

        boolean isDeploy = this.deployFile(rootPath, ftp.getTargetRoot(), contextPath, file_new, ftp);
        if(isDeploy){
            param.put("IS_DEPLOY",Dict.DeploySts.succ);
        }else{
            param.put("IS_DEPLOY",Dict.DeploySts.err);
        }
        this.baseService.insert("busiMapper.saveBusiDocumentFile",param);
        return uuid;
    }

    /**
     * 删除文档的附件
     * @param param
     * @throws Exception
     */
    public void deleteBusiDocumentFile(Map param) throws Exception {
        String FILE_PK = (String)param.get("FILE_PK");
        this.isNull("FILE_PK",FILE_PK);
        this.baseService.insert("busiMapper.deleteBusiDocumentFile",param);
    }

    /**
     * 查询文档的附件列表
     * @param param
     * @return
     * @throws Exception
     */
    public List queryBusiDocumentFile(Map param) throws Exception {
        List list = this.baseService.getList("busiMapper.queryBusiDocumentFile",param);
        return list;
    }


    /**
     * 将文件里的图片的地址做调整，去掉前缀，保留相对地址
     * @param CONTENT_HTML
     * @param CHANNEL_PK
     * @return
     */
    public String adjustDocImgAddr(String CONTENT_HTML,String CHANNEL_PK){
        Map p = new HashMap();
        p.put("CHANNEL_PK",CHANNEL_PK);
        Map DocumentPathContext = (Map)this.baseService.getObject("busiMapper.queryDocumentPathContext",p);
        String SITE_CODE = DocumentPathContext.get("SITE_CODE").toString();
        String CHANNEL_CODE = DocumentPathContext.get("CHANNEL_CODE").toString();
        String SITE_PK = DocumentPathContext.get("SITE_PK").toString();
        String DOMAIN_URL = (String)DocumentPathContext.get("DOMAIN_URL");

        Document doc1 = Jsoup.parse(CONTENT_HTML);
        Elements imgs = doc1.select("img");
        for(int i=0;i<imgs.size();i++){
            String src=imgs.eq(i).attr("src");
            logger.info("saveBusiDocumentDef:"+i+":"+src);
            src = replacePreUrlAddr(src,SITE_CODE);
            doc1.select("img").eq(i).attr("src",src);

        }
        return doc1.toString();
    }


    /**
     * 保存一篇文档
     * @param param
     * @throws Exception
     */
    public void saveBusiDocumentDef(Map param) throws Exception {
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        String TITLE_HTML = (String)param.get("TITLE_HTML");
        String FP_TITLE_HTML = (String)param.get("FP_TITLE_HTML");
        String WRITE_TIME = (String)param.get("WRITE_TIME");
        String CONTENT_HTML = (String)param.get("CONTENT_HTML");
        String TOP_TITLE = (String)param.get("TOP_TITLE");
        String BUTTOM_TITLE = (String)param.get("BUTTOM_TITLE");
        String AUTHOR = (String)param.get("AUTHOR");
        String KEYWORDS = (String)param.get("KEYWORDS");
        String ABSTRACTS = (String)param.get("ABSTRACTS");
        String SOURCE_URL = (String)param.get("SOURCE_URL");
        String C_USER = (String)param.get("C_USER");
        String STS = (String)param.get("STS");
        String PUB_URL = (String)param.get("PUB_URL");
        String PUB_FILE_NAME = (String)param.get("PUB_FILE_NAME");
        //String FILE_PK_ARR =  (String)param.get("FILE_PK_ARR");改版后不需要上传，现在是先申请DOCUMENT_PK
        String PUB_TIME = (String)param.get("PUB_TIME");
        String TMPLATE_ID = (String)param.get("TMPLATE_ID");
        String DOC_TYPE = (String)param.get("DOC_TYPE");
        String LINK_URL = (String)param.get("LINK_URL");
        String DOCUMENT_PK = (String)param.get("DOCUMENT_PK");

        this.isNull("CHANNEL_PK",CHANNEL_PK);
        this.isNull("TITLE",TITLE_HTML);
        this.isNull("DOCUMENT_PK",DOCUMENT_PK);

        this.isNull("WRITE_TIME",WRITE_TIME);
        this.isNull("C_USER",C_USER);
        this.isNull("DOC_TYPE",DOC_TYPE);
        this.isEnum("DOC_TYPE",DOC_TYPE,"1,2,3,4");
        if(CONTENT_HTML!=null){
            logger.debug("CONTENT_HTML encode:"+CONTENT_HTML);
            CONTENT_HTML =  URLDecoder.decode(CONTENT_HTML,"UTF-8");
            logger.debug("CONTENT_HTML decode:"+CONTENT_HTML);

            CONTENT_HTML = adjustDocImgAddr(CONTENT_HTML,CHANNEL_PK);

            param.put("CONTENT_HTML",CONTENT_HTML);
            String CONTENT = ObjectTools.getStrFromHtml(CONTENT_HTML);
            param.put("CONTENT",CONTENT);
        }

        logger.debug("TITLE_HTML encode:"+TITLE_HTML);
        TITLE_HTML =  URLDecoder.decode(TITLE_HTML,"UTF-8");
        logger.debug("TITLE_HTML decode:"+TITLE_HTML);
        param.put("TITLE_HTML",TITLE_HTML);
        String TITLE = ObjectTools.getStrFromHtml(TITLE_HTML);
        param.put("TITLE",TITLE);

        if(FP_TITLE_HTML!=null){
            FP_TITLE_HTML =  URLDecoder.decode(FP_TITLE_HTML,"UTF-8");
            param.put("FP_TITLE_HTML",FP_TITLE_HTML);
            String FIRST_PAGE_TITLE = ObjectTools.getStrFromHtml(FP_TITLE_HTML);
            param.put("FIRST_PAGE_TITLE",FIRST_PAGE_TITLE);
        }


        if(STS ==null){
            STS="0";
            param.put("STS",STS);
        }
        Map channelMap = this.modifyStsByChannelPk(CHANNEL_PK,param,STS);
        if(PUB_TIME == null){
            Date now = new Date();
            param.put("PUB_TIME",now);
        }
        //if(TMPLATE_ID == null){这里修改，不管前台是否传了，都从栏目中取
            Map tmpMap = new HashMap();
            tmpMap.put("CHANNEL_PK",CHANNEL_PK);
            String DOC_TMPL = (String)channelMap.get("DOC_TMPL");
            if(DOC_TMPL!=null){
                param.put("TMPLATE_ID",DOC_TMPL);
            }else{
                throw new Exception("TMPLATE_ID为空,并且栏目的默认文档模板也为空！");
            }
        //}
        /*logger.debug("init FILE_PK_ARR:"+FILE_PK_ARR);
        if(FILE_PK_ARR!=null){
            FILE_PK_ARR = switchIdArr(FILE_PK_ARR);
            param.put("FILE_PK_ARR",FILE_PK_ARR);
        }*/
        logger.debug("WRITE_TIME:"+WRITE_TIME);
        if(WRITE_TIME.indexOf("T")>-1){
            String wt = DateUtil.switchTimeZone(WRITE_TIME);
            param.put("WRITE_TIME",wt);
        }
        //String uuid = Pk.getId("D");
        param.put("DOC_PK",DOCUMENT_PK);
        param.put("VERSION_NO",0);
        this.baseService.insert("busiMapper.saveBusiDocumentDef",param);
        /*if(!ObjectTools.isNull(FILE_PK_ARR)){
            this.baseService.insert("busiMapper.fillBusiDocumentPKByFilePk",param);
        }*/
    }

    public Map queryChannelById(String CHANNEL_PK){
        Map p = new HashMap();
        p.put("CHANNEL_PK",CHANNEL_PK);
        Map rsMap = (Map) this.baseService.getObject("busiMapper.queryBusiChannelDef",p);
        return rsMap;
    }

    /**
     * 如果不需要审核，直接将文档改为待发布
     * @param CHANNEL_PK
     * @param param
     * @param STS
     * @return
     */
    public Map modifyStsByChannelPk(String CHANNEL_PK,Map param,String STS){
        Map rsMap = this.queryChannelById(CHANNEL_PK);
        String is_audit =  rsMap.get("IS_AUDIT").toString();
        if("1".equals(STS) && "0".equals(is_audit)){
            param.put("STS",Dict.DocSts.toPublish);
        }
        return rsMap;
    }

    /**
     * 删除一篇文档
     * @param param
     * @throws Exception
     */
    public void deleteBusiDocumentDef(Map param) throws Exception {
        String DOC_PK = (String)param.get("DOC_PK");
        this.isNull("DOC_PK",DOC_PK);
        this.baseService.insert("busiMapper.deleteBusiDocumentDef",param);
    }

    /**
     * 批量删除文档
     * @param param
     * @throws Exception
     */
    public void deleteBusiDocumentDefBat(Map param) throws Exception {
        String DOC_PK_ARR = (String)param.get("DOC_PK_ARR");
        logger.info("deleteBusiDocumentDefBat param DOC_PK_ARR:"+DOC_PK_ARR);
        this.isNull("DOC_PK_ARR",DOC_PK_ARR);
        if(DOC_PK_ARR!=null){
            DOC_PK_ARR = switchIdArr(DOC_PK_ARR);
            param.put("DOC_PK_ARR",DOC_PK_ARR);
        }
        logger.info("deleteBusiDocumentDefBat param DOC_PK_ARR2:"+DOC_PK_ARR);
        this.baseService.insert("busiMapper.deleteBusiDocumentDefBat",param);
    }


    /**
     * 批量移动（复制）文档
     * @param param
     * @throws Exception
     */
    public void copyBusiDocumentDef(Map param) throws Exception {
        String DOC_PK_ARR = (String)param.get("DOC_PK_ARR");
        String TO_SITE_PK = (String)param.get("TO_SITE_PK");
        String TO_CHANNEL_PK = (String)param.get("TO_CHANNEL_PK");
        String IS_MOVE = (String)param.get("IS_MOVE");
        logger.debug("copyBusiDocumentDef param DOC_PK_ARR:"+DOC_PK_ARR);
        this.isNull("DOC_PK_ARR",DOC_PK_ARR);
        this.isNull("TO_SITE_PK",TO_SITE_PK);
        this.isNull("TO_CHANNEL_PK",TO_CHANNEL_PK);
        this.isNull("IS_MOVE",IS_MOVE);
        if(DOC_PK_ARR!=null){
            DOC_PK_ARR = switchIdArr(DOC_PK_ARR);
            param.put("DOC_PK_ARR",DOC_PK_ARR);
        }
        logger.debug("copyBusiDocumentDef param DOC_PK_ARR2:"+DOC_PK_ARR);
        if("1".equals(IS_MOVE)){
            this.baseService.insert("busiMapper.updateBusiDocumentDefBatMove",param);
        }else{
            this.baseService.insert("busiMapper.updateBusiDocumentDefBatCopy",param);
        }
    }


    /**
     * 修改文档的状态为废弃
     * @param param
     * @throws Exception
     */
    public void removeBusiDocumentDef(Map param) throws Exception {
        String DOC_PK = (String)param.get("DOC_PK");
        this.isNull("DOC_PK",DOC_PK);
        param.put("STS",Dict.DocSts.Discard);
        this.baseService.insert("busiMapper.updateBusiDocumentSts",param);
    }

    /**
     * 修改文档属性
     * @param param
     * @throws Exception
     */
    public void updateBusiDocumentDef(Map param) throws Exception {
        String DOC_PK = (String)param.get("DOC_PK");
        String U_USER = (String)param.get("U_USER");
        String CONTENT_HTML = (String)param.get("CONTENT_HTML");
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        this.isNull("CHANNEL_PK",CHANNEL_PK);
        this.isNull("DOC_PK",DOC_PK);
        this.isNull("U_USER",U_USER);

        String TITLE_HTML = (String)param.get("TITLE_HTML");
        String FP_TITLE_HTML = (String)param.get("FP_TITLE_HTML");
        String STS = (String)param.get("STS");

        if(CONTENT_HTML!=null){
            logger.debug("CONTENT_HTML encode:"+CONTENT_HTML);
            CONTENT_HTML =  URLDecoder.decode(CONTENT_HTML,"UTF-8");

            CONTENT_HTML = adjustDocImgAddr(CONTENT_HTML,CHANNEL_PK);

            logger.debug("CONTENT_HTML decode:"+CONTENT_HTML);
            param.put("CONTENT_HTML",CONTENT_HTML);
            String CONTENT = ObjectTools.getStrFromHtml(CONTENT_HTML);
            param.put("CONTENT",CONTENT);
        }


        if(TITLE_HTML!=null) {
            logger.debug("TITLE_HTML encode:" + TITLE_HTML);
            TITLE_HTML = URLDecoder.decode(TITLE_HTML, "UTF-8");
            logger.debug("TITLE_HTML decode:" + TITLE_HTML);
            param.put("TITLE_HTML", TITLE_HTML);
            String TITLE = ObjectTools.getStrFromHtml(TITLE_HTML);
            param.put("TITLE", TITLE);
        }

        if(FP_TITLE_HTML!=null){
            FP_TITLE_HTML =  URLDecoder.decode(FP_TITLE_HTML,"UTF-8");
            param.put("FP_TITLE_HTML",FP_TITLE_HTML);
            String FIRST_PAGE_TITLE = ObjectTools.getStrFromHtml(FP_TITLE_HTML);
            param.put("FIRST_PAGE_TITLE",FIRST_PAGE_TITLE);
        }


        this.baseService.insert("busiMapper.saveBusiDocumentHis",param);
        this.baseService.insert("busiMapper.updateBusiDocumentDef",param);
    }

    /**
     * 查询文档
     * @param param
     * @return
     * @throws Exception
     */
    public List queryBusiDocumentDef(Map param) throws Exception {
        setSplitPageParam(param);
        Map map = (Map)this.baseService.getObject("busiMapper.queryBusiDocumentDefTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper"+Dict.dbMap+".queryBusiDocumentDef",param);
        return list;
    }

    /**
     * 修改文档状态
     * @param param
     * @throws Exception
     */
    public void updateBusiDocumentSts(Map param) throws Exception {
        //状态0草稿 -1删除 1新建待审 2待签 9已发
        String DOC_PK = (String)param.get("DOC_PK");
        String STS = param.get("STS").toString();
        this.isNull("DOC_PK",DOC_PK);
        this.isNull("STS",STS);
        Map m = new HashMap();
        m.put("DOC_PK",DOC_PK);
        m.put("STS",STS);
        this.baseService.insert("busiMapper.updateBusiDocumentSts",m);
    }


    /**
     * 审核文档
     * @param param
     * @throws Exception
     */
    public void auditBusiDocumentDef(Map param) throws Exception {
        //状态0草稿 -1删除 1新建待审 2待签 9已发
        String DOC_PK = (String)param.get("DOC_PK");
        String STS = (String)param.get("AUDIT_STS");
        String IS_SIGN = param.get("IS_SIGN").toString();
        String AUDIT_INFO = (String)param.get("AUDIT_INFO");
        this.isNull("DOC_PK",DOC_PK);

        this.isEnum("IS_SIGN",IS_SIGN,"1,0");
        if("1".equals(STS)){
            if("1".equals(IS_SIGN)){
                param.put("STS", Dict.DocSts.toSign);
            }else{
                param.put("STS", Dict.DocSts.toPublish);
            }
        }else{
            param.put("STS", Dict.DocSts.Auditfailed);
        }
        this.baseService.insert("busiMapper.updateBusiDocumentSts",param);
    }

    /**
     * 草稿转正式
     * @param param
     * @throws Exception
     */
    public void DraftToNewBusiDocumentDef(Map param) throws Exception {
        String DOC_PK = (String)param.get("DOC_PK");
        logger.debug("DraftToNewBusiDocumentDef doc_pk"+DOC_PK);
        this.isNull("DOC_PK",DOC_PK);
        Map p = new HashMap();
        p.put("DOC_PK",DOC_PK);
        Map rsMap = (Map) this.baseService.getObject("busiMapper"+Dict.dbMap+".queryBusiDocumentDef",p);
        String is_audit =  rsMap.get("IS_AUDIT").toString();
        if("1".equals(is_audit)){
            param.put("STS", Dict.DocSts.toAudit);
        }else{
            param.put("STS", Dict.DocSts.toPublish);
        }
        this.baseService.insert("busiMapper.updateBusiDocumentSts",param);
    }

    /**
     * 签发文档
     * @param param
     * @throws Exception
     */
    public void signBusiDocumentDef(Map param) throws Exception {
        //状态0草稿 -1删除 1新建待审 2待签 9已发
        String DOC_PK = (String)param.get("DOC_PK");
        String STS = (String)param.get("SIGN_STS");
        logger.debug("signBusiDocumentDef sts:"+STS);
        String SIGN_INFO = (String)param.get("SIGN_INFO");
        this.isNull("DOC_PK",DOC_PK);
        this.isEnum("SIGN_STS",STS,"1,0");
        if("1".equals(STS)){
            param.put("STS", Dict.DocSts.toPublish);
        }else{
            param.put("STS", Dict.DocSts.Signfailed);
        }
        this.baseService.insert("busiMapper.updateBusiDocumentSts",param);
    }

    /**eg:http://localhost:9443/gfcms/busi/preViewChannelDefHomePage?CHANNEL_PK=CD97b1e7b7b5c54e8a96a198399a19a7e3&SITE_PK=SDac0b7073ea2b4f6392b5549792acda1d
     * 创建一篇文档的核心 ，被单次发布和批量发布调用，以提高共享率
     * @param DOC_PK
     * @param serverRootPath
     * @param appRoot
     * @return
     * @throws Exception
     */
    private Map createDocumentDef(String DOC_PK,String serverRootPath, String appRoot) throws Exception {
        Map param = new HashMap();
        this.isNull("DOC_PK",DOC_PK);
        param.put("DOC_PK",DOC_PK);

        Map tmplMap = (Map)this.baseService.getObject("busiMapper.queryTemplateInfoByDocPk",param);
        String DOC_TYPE = tmplMap.get("DOC_TYPE").toString();
        String LINK_URL = (String)tmplMap.get("LINK_URL");
        String DOMAIN_URL = (String)tmplMap.get("DOMAIN_URL");
        param.put("CHANNEL_PK",tmplMap.get("CHANNEL_PK"));
        if("3".equals(DOC_TYPE) && LINK_URL!=null){
            param.put("JUMP_DEPLOY", "1");
            param.put("PUB_URL", LINK_URL);

        }else {

            String TMPL_CONTENT = (String) tmplMap.get("TMPL_CONTENT");
            String SITE_CODE = (String) tmplMap.get("SITE_CODE");
            String CHANNEL_CODE = (String) tmplMap.get("CHANNEL_CODE");


            Map inputMap = tmplMap;
            StringBuffer sb = new StringBuffer(TMPL_CONTENT);
            ABaseLabel label = new GfDocumentLabel(TMPL_CONTENT);
            label.setInputMap(inputMap);
            label.switchLabel(sb, inputMap, baseService, 1, 1);////这里仅仅是一个示例

            logger.debug("文档的内容：" + sb.toString());
            //String context = SITE_CODE + sep + "document" + sep + CHANNEL_CODE + sep + ObjectTools.getCurMonth();为了模板，生成的html和资源文件对齐
            String context = SITE_CODE + sep + ObjectTools.getCurMonth();
            String file = DateUtil.getCurDayRandomId() + ".html";
            if(!serverRootPath.endsWith("/")){
                serverRootPath = serverRootPath+sep;
            }
            String path = serverRootPath  + context + sep;
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }

            FileWriter fw = new FileWriter(path + file);
            fw.write(sb.toString());
            fw.close();
            //String homePage = replaceDomainUrl(appRoot + sep + context + sep + file,DOMAIN_URL,SITE_CODE);
            String homePage = sep + ObjectTools.getCurMonth()+ sep + file ;
            param.put("SERVER_PATH", path + sep + file);
            param.put("PUB_URL", homePage);
            param.put("PUB_FILE_NAME", file);
            param.put("CONTEXT_PATH", context);
            param.put("FILE_NAME", file);

        }

        return param;
    }

    /**
     * 预览一篇文档，生成页面，思路是这样：
     * 1 查找根据文档编号查找文档和模板的信息
     * 2 查找模板有没有附件，如有移动文件（但是考虑后，觉得移动文件更麻烦，应该在模板上传附件时就保存好期WEB路径，模板及文档引用WEB路径
     * @param param
     * @return 生成的预览地址
     * @throws Exception
     */
    public String preViewDocumentDef(Map param,FtpInfo ftp) throws Exception {
        String DOC_PK = (String)param.get("DOC_PK");
        String serverRootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();
        Map rsMap = createDocumentDef(DOC_PK,serverRootPath,  appRoot);
        param.putAll(rsMap);
        String JUMP_DEPLOY  = (String)rsMap.get("JUMP_DEPLOY");
        if(JUMP_DEPLOY==null) {
            boolean isDeploy = this.deployFile(serverRootPath, ftp.getTargetRoot(), rsMap.get("CONTEXT_PATH").toString(), rsMap.get("FILE_NAME").toString(), ftp);
            if (isDeploy) {
                param.put("IS_DEPLOY", Dict.DeploySts.succ);
            } else {
                param.put("IS_DEPLOY", Dict.DeploySts.err);
            }
        }else{
            param.put("IS_DEPLOY", Dict.DeploySts.succ);
        }
        this.baseService.insert("busiMapper.updateBusiDocumentDef",param);
        return rsMap.get("PUB_URL").toString();
    }

    /**
     * 批量发布文档
     * @param param
     * @param ftp
     * @throws Exception
     */
    public void publishDocumentList(Map param,FtpInfo ftp) throws Exception {
        String DOC_PK_ARR = (String)param.get("DOC_PK_ARR");
        logger.info("deleteBusiDocumentDefBat param DOC_PK_ARR:"+DOC_PK_ARR);
        this.isNull("DOC_PK_ARR",DOC_PK_ARR);
        DOC_PK_ARR = switchIdArr(DOC_PK_ARR);
        String DOC_PK_ARR_PURE =  switchIdArrPure(DOC_PK_ARR);
        String[] docPkArr = DOC_PK_ARR_PURE.split(",");
        List< FtpPathInfo > listFile = new ArrayList<FtpPathInfo>();
        String serverRootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();
        FtpPathInfo info = null;
        for(String docPk:docPkArr){
            Map rsMap = createDocumentDef(docPk,serverRootPath,  appRoot);
            param.putAll(rsMap);
            this.baseService.insert("busiMapper.updateBusiDocumentDef",param);

            String JUMP_DEPLOY  = (String)rsMap.get("JUMP_DEPLOY");
            if(JUMP_DEPLOY==null) {
                String contextPath = rsMap.get("CONTEXT_PATH").toString();
                String ftpPath = contextPath;
                String sourcePath = rsMap.get("SERVER_PATH").toString();
                File sourcefile = new File(sourcePath);
                String targetPath = ftp.getTargetRoot() + contextPath + sep + rsMap.get("FILE_NAME").toString();
                File targetfile = new File(targetPath);

                info = new FtpPathInfo(ftpPath, sourcefile, targetfile);
                listFile.add(info);
            }
        }
        deployFile(listFile, ftp);
        param.put("DOC_PK_ARR",DOC_PK_ARR);
        param.put("IS_DEPLOY",Dict.DeploySts.succ);
        param.put("STS",Dict.DocSts.PublishOk);
        this.baseService.insert("busiMapper.updateBusiDocumentDefDeploySts",param);
    }


    /**
     * 将指定栏目下的文章批量撤销发布文档，
     * 分为如下几步：1修改文档的状态为废弃；2 将文档从对端和本端删除 3 重新发布栏目页面
     *
     * @param param
     * @param ftp
     * @throws Exception
     */
    public List desoryPubDocumentListByChannelPk(Map param, FtpInfo ftp) throws Exception {
        //1 修改文档的状态为废弃；
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        logger.info("desoryPubDocumentListByChannelPk param CHANNEL_PK:"+CHANNEL_PK);
        this.isNull("CHANNEL_PK",CHANNEL_PK);
        param.put("STS",Dict.DocSts.Discard);
        param.put("PUB_URL_BLANK","");
        this.baseService.insert("busiMapper.updateBusiDocumentStsByChannelPk",param);
        //2 查出文档列表
        Map p = new HashMap();
        p.put("CHANNEL_PK",param.get("CHANNEL_PK"));
        List list = this.baseService.getList("busiMapper"+Dict.dbMap+".queryBusiDocumentDef",p);

        String[] docPkArr = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            Map m = (Map)list.get(i);
            String docPk = (String)m.get("DOC_PK");
            docPkArr[i] = docPk;
            param.put("SITE_PK",m.get("SITE_PK"));
        }
        return desoryPubDocumentCore(param,ftp,docPkArr);

        //3 重新发布栏目页面，为了避免Service间引用 ，第3放到control里去调用
    }

    /**
     * 批量撤销发布文档，分为如下几步：1修改文档的状态为废弃；2 将文档从对端和本端删除 3 重新发布栏目页面
     *
     * @param param
     * @param ftp
     * @throws Exception
     */
    public List desoryPubDocumentList(Map param, FtpInfo ftp) throws Exception {
        //1 修改文档的状态为废弃；
        String DOC_PK_ARR = (String)param.get("DOC_PK_ARR");
        logger.info("deleteBusiDocumentDefBat param DOC_PK_ARR:"+DOC_PK_ARR);
        this.isNull("DOC_PK_ARR",DOC_PK_ARR);
        DOC_PK_ARR = switchIdArr(DOC_PK_ARR);
        param.put("DOC_PK_ARR",DOC_PK_ARR);
        param.put("STS",Dict.DocSts.Discard);
        param.put("PUB_URL_BLANK","");
        this.baseService.insert("busiMapper.updateBusiDocumentStsBat",param);
        //2 将文档从对端和本端删除
        String DOC_PK_ARR_PURE =  switchIdArrPure(DOC_PK_ARR);
        String[] docPkArr = DOC_PK_ARR_PURE.split(",");
        return desoryPubDocumentCore(param,ftp,docPkArr);

        //3 重新发布栏目页面，为了避免Service间引用 ，第3放到control里去调用
    }

    /**
     * 批量撤销发布文档的处理核心部分，会被多个场景调用
     * @param param
     * @param ftp
     * @throws Exception
     */
    public List desoryPubDocumentCore(Map param, FtpInfo ftp,String[] docPkArr) throws Exception {
        List< FtpPathInfo > listFile = new ArrayList<FtpPathInfo>();
        FtpPathInfo info = null;
        List<String> channelPkList = new ArrayList<String>();
        for(String docPk:docPkArr){
            param.put("DOC_PK",docPk);
            Map tmplMap = (Map)this.baseService.getObject("busiMapper.queryTemplateInfoByDocPk",param);
            String PUB_FILE_NAME = (String)tmplMap.get("PUB_FILE_NAME");
            String SERVER_PATH = (String)tmplMap.get("SERVER_PATH");
            if(SERVER_PATH==null){//有些文档就没发布，所以也不用撤销发布了
                continue;
            }
            String contextPath = getMidPath(SERVER_PATH,PUB_FILE_NAME,ftp.getTargetRoot());
            String CHANNEL_PK =  (String)tmplMap.get("CHANNEL_PK");
            if(!channelPkList.contains(CHANNEL_PK)){
                channelPkList.add(CHANNEL_PK);
            }
            String ftpPath = contextPath;
            String sourcePath = SERVER_PATH;
            File sourcefile = new File(sourcePath);
            String targetPath = ftp.getTargetRoot() + contextPath + sep + PUB_FILE_NAME;
            File targetfile = new File(targetPath);

            info = new FtpPathInfo(ftpPath,sourcefile,targetfile);
            listFile.add(info);
        }
        undeployFile(listFile, ftp);
        //3 重新发布栏目页面，为了避免Service间引用 ，第3放到control里去调用
        return channelPkList;

    }


    private String getMidPath(String fullPath,String fileName,String root){
        int i = fullPath.indexOf(fileName);
        fullPath = fullPath.substring(0,i);
        i = fullPath.indexOf(root);
        if(i>-1){
            fullPath = fullPath.substring(i+root.length(),fullPath.length()-1);
        }
        if(fullPath.charAt(fullPath.length()-1)=='/'){
            fullPath = fullPath.substring(0,fullPath.length()-1);
        }
        return fullPath;
    }


    /**
     * 取待转语音的文档
     * @param limitNum 一次处理多少条
     * @return
     * @throws Exception
     */
    public List queryDocumentToSwitchVoice(int limitNum) throws Exception {
        Map param = new HashMap();
        param.put("BAT_PROC_SWITCH_VOICE",limitNum);
        List list = this.baseService.getList("busiMapper.queryDocumentToSwitchVoice",param);
        return list;
    }


    public String viewDocumentPath(Map param,FtpInfo ftp) throws Exception {
        String DOC_PK = (String)param.get("DOC_PK");
        this.isNull("DOC_PK",DOC_PK);

        String appRoot = ftp.getAppRootPath();

        Map docPathContext = (Map)this.baseService.getObject("busiMapper.queryTemplateInfoByDocPk",param);

        String DOMAIN_URL = (String)docPathContext.get("DOMAIN_URL");
        String PUB_URL = (String)docPathContext.get("PUB_URL");


        if(DOMAIN_URL!=null && DOMAIN_URL.toLowerCase().startsWith("http")){
            return DOMAIN_URL + PUB_URL;
        }else{
            return appRoot + PUB_URL;
        }


    }
}

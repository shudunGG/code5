package com.om.module.service.busi;
import com.om.bo.base.FtpInfo;
import com.om.bo.base.busi.CatalogBo;
import com.om.bo.element.DyncTreeBo;
import com.om.common.cache.Dict;
import com.om.common.util.ObjectTools;
import com.om.common.util.PinYinUtil;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import com.om.module.service.label.ABaseLabel;
import com.om.module.service.label.GfDocumentLabel;
import com.om.module.service.label.GfDocumentsLabel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("ChannelManagerService")
public class ChannelManagerService extends CommonService {

    @Resource(name = "TemplateManagerService")
    private TemplateManagerService templateManagerService;



    public void saveBusiChannelDef(Map param,FtpInfo ftp) throws Exception {
        //printParam(param,"saveBusiChannelDef==:");
        String PARENT_PK = (String)param.get("PARENT_PK");
        String PARENT_CODE = (String)param.get("PARENT_CODE");//这个改了，现在不需要从前台传，改后后台 从数据库里获取，请注意及时修改文档
        String CHANNEL_NAME = (String)param.get("CHANNEL_NAME");
        String CHANNEL_DESC = (String)param.get("CHANNEL_DESC");
        String CHANNEL_TYPE = (String)param.get("CHANNEL_TYPE");
        String CHANNEL_DIR = (String)param.get("CHANNEL_DIR");
        String DOMAIN_URL = (String)param.get("DOMAIN_URL");
        String SHOW_SEQ = (String)param.get("SHOW_SEQ");
        String CHANNEL_TMPL = (String)param.get("CHANNEL_TMPL");
        String DOC_TMPL = (String)param.get("DOC_TMPL");
        String DATA_VIEW = (String)param.get("DATA_VIEW");
        String SPECIAL_CHANNEL_ID = (String)param.get("SPECIAL_CHANNEL_ID");
        String IS_PUBLISH = (String)param.get("IS_PUBLISH");
        String IS_PUSH = (String)param.get("IS_PUSH");
        String IS_PULL = (String)param.get("IS_PULL");
        String IS_AUDIT = (String)param.get("IS_AUDIT");
        String IS_SIGN = (String)param.get("IS_SIGN");
        String C_USER = (String)param.get("C_USER");
        String LINK_URL = (String)param.get("LINK_URL");
        String CONTENT_PAGE = (String)param.get("CONTENT_PAGE");
        String SITE_PK = (String)param.get("SITE_PK");
        String TMPL_PK_ARR = (String)param.get("TMPL_PK_ARR");
        String FILE_NAME_ARR = (String)param.get("FILE_NAME_ARR");


        if(PARENT_CODE == null){
            PARENT_CODE="";
        }
        this.isNull("SPECIAL_CHANNEL_ID",SPECIAL_CHANNEL_ID);
        this.isNull("PARENT_PK",PARENT_PK);
        this.isNull("CHANNEL_NAME",CHANNEL_NAME);
        this.isNull("CHANNEL_TYPE",CHANNEL_TYPE);
        //this.isNull("CHANNEL_DIR",CHANNEL_DIR);
        this.isNull("DATA_VIEW",DATA_VIEW);
        this.isNull("IS_PUBLISH",IS_PUBLISH);
        this.isNull("IS_PUSH",IS_PUSH);
        this.isNull("IS_PULL",IS_PULL);
        this.isNull("C_USER",C_USER);
        this.isNull("SITE_PK",SITE_PK);
        this.isEnum("CHANNEL_TYPE",CHANNEL_TYPE,"1,2,3");
        this.isEnum("IS_AUDIT",IS_AUDIT,"1,0");
        this.isEnum("IS_SIGN",IS_SIGN,"1,0");

        Map tmpMap = new HashMap();
        tmpMap.put("SITE_PK",SITE_PK);
        Map siteMap = (Map)this.baseService.getObject("busiMapper.queryBusiSiteDef",tmpMap);


        if(CHANNEL_TMPL == null || DOC_TMPL == null){

            String SITE_CHANNEL_TMPL = (String)siteMap.get("CHANNEL_TMPL");
            String SITE_DOC_TMPL = (String)siteMap.get("DOC_TMPL");
            if(CHANNEL_TMPL == null){
                if(SITE_CHANNEL_TMPL != null){
                    param.put("CHANNEL_TMPL",SITE_CHANNEL_TMPL);
                }else{
                    throw new Exception("CHANNEL_TMPL为空,并且站点的默认栏目模板也为空！");
                }
            }

            if(DOC_TMPL == null){
                if(SITE_DOC_TMPL != null){
                    param.put("DOC_TMPL",SITE_DOC_TMPL);
                }else{
                    throw new Exception("DOC_TMPL为空,并且站点的默认文档模板也为空！");
                }
            }

        }
        param.put("SAVEASDIR",CHANNEL_DIR);
        Map m = (Map)this.baseService.getObject("busiMapper.queryMaxBusiChannelCode",param);
        int maxId = Integer.parseInt(m.get("MAXID").toString());

        m = (Map)this.baseService.getObject("busiMapper.querySPECIAL_CHANNEL_ID",param);
        if(m!=null){
            throw new Exception("唯一标识【SPECIAL_CHANNEL_ID】已经被使用，请设置不同的名称！");
        }

        m = (Map)this.baseService.getObject("busiMapper.queryChannelCodeByPk",param);
        if(m!=null){
            PARENT_CODE=m.get("CHANNEL_CODE").toString();
        }

        String uuid = Pk.getId("CD");
        String CATALOG_CODE = Pk.getCode(maxId);
        param.put("CHANNEL_PK",uuid);
        param.put("CHANNEL_ID",maxId);
        param.put("CHANNEL_CODE",CATALOG_CODE);
        param.put("CHANNEL_FULL_CODE",PARENT_CODE+CATALOG_CODE);

        String SITE_CODE = (String)siteMap.get("SITE_CODE");
        //String context = SITE_CODE+sep+"channel"+sep+CATALOG_CODE;
        //String file = "index.html";//站点首页，和栏目的概览统一用index.html,区别在于不同的路径下
        //String appRoot = ftp.getAppRootPath();
        //param.put("HOME_PAGE",appRoot+sep+context+sep+file);
        //这里写HOME没用，还是要等发布的时候再写该字段HOME_PAGE


        if(ObjectTools.isNull(SHOW_SEQ)){
            param.put("SHOW_SEQ",Pk.getSeq(maxId));
        }


        param.put("TMPL_PK",CHANNEL_TMPL);
        param.put("OP_USER",C_USER);
        String fullCode = (String)param.get("CHANNEL_FULL_CODE");
        //int level = fullCode.length()/3;
        //this.templateManagerService.updateBusiTemplateDefRelativeAddr(param,level);

        if(TMPL_PK_ARR!=null && FILE_NAME_ARR!=null){
            String[] tmplPKArr = TMPL_PK_ARR.split(",");
            String[] fileNameArr = FILE_NAME_ARR.split(",");
            for(int i=0;i<tmplPKArr.length;i++){
                Map pp = new HashMap();
                pp.putAll(param);
                pp.put("EXT_PK",Pk.getId("E"));
                pp.put("CHANNEL_PK",uuid);
                pp.put("TMPL_PK",tmplPKArr[i]);
                pp.put("FILE_NAME",fileNameArr[i]);
                //this.templateManagerService.updateBusiTemplateDefRelativeAddr(pp,level);
                this.baseService.insert("busiMapper.saveBusiChannelTmplExt",pp);
            }
        }

        this.baseService.insert("busiMapper.saveBusiChannelDef",param);

    }


    public void deleteBusiChannelDef(Map param) throws Exception {
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        this.isNull("CHANNEL_PK",CHANNEL_PK);

        //删除栏目，要增加对文档，父栏目的检查，只要被使用，就不能删除
        List list = this.baseService.getList("busiMapper.queryChannelUsed",param);
        if(list.size()>0){
            StringBuffer sb = new StringBuffer("对不起，栏目下有以下子栏目或文章，无法删除，请先删除下列的子栏目及文章：");
            for(int i=0;i<list.size();i++){
                Map map = (Map)list.get(i);
                String type = map.get("TYPE2").toString();
                String name = map.get("NAME2").toString();
                if("1".equals(type)){
                    sb.append("文章标题【").append(name).append("】\n");
                }else if("2".equals(type)){
                    sb.append("子栏目名称【").append(name).append("】\n");
                }
            }
            throw new Exception(sb.toString());
        }
        this.baseService.insert("busiMapper.deleteBusiChannelDef",param);
    }

    public void updateBusiChannelDef(Map param) throws Exception {
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        this.isNull("CHANNEL_PK",CHANNEL_PK);
        String CHANNEL_TMPL = (String)param.get("CHANNEL_TMPL");

        this.baseService.insert("busiMapper.updateBusiChannelDef",param);
    }

    public List queryBusiChannelDef(Map param) throws Exception {
        String CHANNEL_FULL_CODE = (String)param.get("CHANNEL_FULL_CODE");
        if(CHANNEL_FULL_CODE!=null && CHANNEL_FULL_CODE.length()>0){
            param.put("CHANNEL_FULL_CODE_SON_LENGTH",CHANNEL_FULL_CODE.length()+3);
        }
        List list = this.baseService.getList("busiMapper.queryBusiChannelDef",param);
        return list;
    }

    public List queryTmplExtListByChannelPK(Map param) throws Exception {
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        this.isNull("CHANNEL_PK",CHANNEL_PK);
        List list = this.baseService.getList("busiMapper.queryTmplExtListByChannelPK",param);
        return list;
    }
    public void deleteBusiChannelTmplExt(Map param) throws Exception {
        String EXT_PK = (String)param.get("EXT_PK");
        this.isNull("EXT_PK",EXT_PK);

        this.baseService.insert("busiMapper.deleteBusiChannelTmplExt",param);
    }


    /**
     * 预览栏目首页，生成页面index.html，思路是这样：
     * 1 查找根据栏目编号查找渠道和模板的信息
     * 2 生成页面为栏目下的index.html
     * @param param
     * @return 生成的预览地址
     * @throws Exception
     */
    public String preViewChannelDefHomePage(Map param, FtpInfo ftp) throws Exception {
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        this.isNull("CHANNEL_PK",CHANNEL_PK);

        Map tmplMap = (Map)this.baseService.getObject("busiMapper.queryChannnelTmplInfoByChannelPk",param);
        if(tmplMap == null ){
            throw new Exception("请检查该栏目的模板是否未指定");
        }
        param.put("SITE_PK",tmplMap.get("SITE_PK"));
        String serverRootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();
        String TMPL_CONTENT = (String)tmplMap.get("TMPL_CONTENT");
        String SITE_CODE = (String)tmplMap.get("SITE_CODE");
        String CHANNEL_CODE = (String)tmplMap.get("CHANNEL_CODE");
        String DOMAIN_URL = (String)tmplMap.get("DOMAIN_URL");
        String context = SITE_CODE + sep + CHANNEL_CODE;
        //String context = SITE_CODE + sep + "channel" + sep + CHANNEL_CODE;考虑模板，生成的文件和资源对齐，20230228
        Map inputMap = tmplMap;
        ABaseLabel label = new GfDocumentsLabel(TMPL_CONTENT);
        label.setInputMap(inputMap);
        label.setCurPage(0);//这里手工强制设置为0的目的是，这上置标的分页不按普通分页走，按取最大值走
        int totalPage = label.getPage(baseService);
        boolean isDeploy = false;
        String firstFile = null;
        if(!appRoot.endsWith("/")){
            appRoot = appRoot +sep;
        }
        if(!serverRootPath.endsWith("/")){
            serverRootPath = serverRootPath +sep;
        }
        for(int i=1;i<=totalPage;i++) {
            int curPage = i;
            StringBuffer sb = new StringBuffer(TMPL_CONTENT);
            label.setInputMap(inputMap);
            label.setCurPage(curPage);
            label.switchLabel(sb, inputMap, baseService,curPage,totalPage,label.totalRecord);////这里仅仅是一个示例

            logger.debug("文档的内容：" + sb.toString());
            String file = "index_"+curPage+".html";//站点首页，和栏目的概览统一用index.html,区别在于不同的路径下
            if(curPage == 1){
                firstFile = file;
            }
            String path = serverRootPath + context + sep;
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }

            FileWriter fw = new FileWriter(path + file);
            fw.write(sb.toString());
            fw.close();

            isDeploy = this.deployFile(serverRootPath, ftp.getTargetRoot(), context, file, ftp);
        }

        //这里补充对栏目的其它文件的发布 start
        List extTmplList = queryTmplExtListByChannelPK(param);
        for(int i=0;i<extTmplList.size();i++){
            Map extTmplMap = (Map)extTmplList.get(i);

            TMPL_CONTENT = (String)extTmplMap.get("TMPL_HTML_CONTENT");
            tmplMap.put("TMPL_CONTENT",extTmplMap.get("TMPL_CONTENT"));
            tmplMap.put("TMPL_HTML_CONTENT",extTmplMap.get("TMPL_HTML_CONTENT"));
            inputMap = tmplMap;
            StringBuffer sb = new StringBuffer(TMPL_CONTENT);
            label = new GfDocumentLabel(TMPL_CONTENT);
            label.setInputMap(inputMap);
            label.switchLabel(sb, inputMap, baseService, 1, 1);////这里仅仅是一个示例
            logger.debug("文档的内容：" + sb.toString());

            String file = (String)extTmplMap.get("FILE_NAME");//站点首页，和栏目的概览统一用index.html,区别在于不同的路径下
            String path = serverRootPath + context + sep;

            FileWriter fw = new FileWriter(path + file);
            fw.write(sb.toString());
            fw.close();
            isDeploy = this.deployFile(serverRootPath, ftp.getTargetRoot(), context, file, ftp);
        }
        //这里补充对栏目的其它文件的发布 end



        //String homePage = replaceDomainUrl(appRoot+context+sep+firstFile,DOMAIN_URL,SITE_CODE);
        String homePage = sep + CHANNEL_CODE+sep+firstFile;
        param.put("HOME_PAGE",homePage);
        if(isDeploy){
            param.put("IS_DEPLOY",Dict.DeploySts.succ);
        }else{
            param.put("IS_DEPLOY",Dict.DeploySts.err);
        }
        this.baseService.insert("busiMapper.updateBusiChannelDef",param);
        //return appRoot+sep+context+sep+firstFile;
        return homePage;
    }


    /**
     * 发布栏目
     * @param param
     * @param ftp
     * @throws Exception
     */
    public void publishChannelAll(Map param,FtpInfo ftp) throws Exception {
        //1 生成栏目首页
        this.preViewChannelDefHomePage(param,ftp);
        //2 修改栏目的状态为待发布
        Map p = new HashMap();
        p.put("CHANNEL_PK",param.get("CHANNEL_PK"));
        p.put("STS", Dict.ChannelSts.PublishOk);
        this.updateBusiChannelDef(p);
    }

    /**
     * 批量发布栏目
     * @param param
     * @param ftp
     * @throws Exception
     */
    public void publishChannelBat(Map param,FtpInfo ftp) throws Exception {
        String channelPkArr = this.destoryChannelBat(param);
        String[] channelArr = channelPkArr.split(",");
        for(String channelPk:channelArr){
            param.put("CHANNEL_PK",channelPk);
            this.publishChannelAll(param,ftp);
        }
    }

    /**
     * 把前台传的pk串，转成逗号连接的
     * @param param
     * @return
     * @throws Exception
     */
    public String destoryChannelBat(Map param)throws Exception {
        String CHANNEL_PK_ARR = (String)param.get("CHANNEL_PK_ARR");
        logger.info("destoryChannelBat param CHANNEL_PK_ARR:"+CHANNEL_PK_ARR);
        this.isNull("CHANNEL_PK_ARR",CHANNEL_PK_ARR);
        String pkArr = switchIdArrPure(CHANNEL_PK_ARR);
        return pkArr;
    }

    /**
     * 撤销栏目
     * @param param
     * @param ftp
     * @throws Exception
     */
    public void destoryChannel(Map param,FtpInfo ftp) throws Exception {
        //1 修改栏目的状态为销毁
        Map p = new HashMap();
        p.put("CHANNEL_PK",param.get("CHANNEL_PK"));
        p.put("STS", Dict.ChannelSts.Discard);
        p.put("HOME_PAGE","");
        this.updateBusiChannelDef(p);
    }

    /**
     * 批量删除栏目
     * @param param
     * @throws Exception
     */
    public void deleteChannelBat(Map param) throws Exception {
        //1 修改文档的状态为废弃；
        String CHANNEL_PK_ARR = (String)param.get("CHANNEL_PK_ARR");
        logger.info("deleteChannelBat param CHANNEL_PK_ARR:"+CHANNEL_PK_ARR);
        this.isNull("DOC_PK_ARR",CHANNEL_PK_ARR);
        CHANNEL_PK_ARR = switchIdArr(CHANNEL_PK_ARR);
        param.put("CHANNEL_PK_ARR",CHANNEL_PK_ARR);
        List list = this.baseService.getList("busiMapper.queryDocumentByChannelPkArr",param);
        if(list.size()>0){
            throw new Exception("要删除的栏目下还有文章，不允许删除！");
        }

        //删除栏目，要增加对文档，父栏目的检查，只要被使用，就不能删除
        list = this.baseService.getList("busiMapper.queryChannelUsedBatChannelPk",param);
        if(list.size()>0){
            StringBuffer sb = new StringBuffer("对不起，栏目下有以下子栏目，无法删除，请先删除下列的子栏目：");
            for(int i=0;i<list.size();i++){
                Map map = (Map)list.get(i);
                String type = map.get("TYPE2").toString();
                String name = map.get("NAME2").toString();
                if("1".equals(type)){
                    sb.append("文章标题【").append(name).append("】\n");
                }else if("2".equals(type)){
                    sb.append("子栏目名称【").append(name).append("】\n");
                }
            }
            throw new Exception(sb.toString());
        }

        this.baseService.delete("busiMapper.deleteBusiChannelByChannelPkArr",param);
    }

    public String viewChannelPath(Map param,FtpInfo ftp) throws Exception {
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        this.isNull("CHANNEL_PK",CHANNEL_PK);

        String appRoot = ftp.getAppRootPath();

        Map DocumentPathContext = (Map)this.baseService.getObject("busiMapper.queryDocumentPathContext",param);
        String SITE_CODE = DocumentPathContext.get("SITE_CODE").toString();
        String CHANNEL_CODE = DocumentPathContext.get("CHANNEL_CODE").toString();
        String SITE_PK = DocumentPathContext.get("SITE_PK").toString();
        String DOMAIN_URL = (String)DocumentPathContext.get("DOMAIN_URL");
        String CHANNEL_PAGE = (String)DocumentPathContext.get("CHANNEL_PAGE");
        String SITE_PAGE = (String)DocumentPathContext.get("SITE_PAGE");

        if(DOMAIN_URL!=null && DOMAIN_URL.toLowerCase().startsWith("http")){
            return DOMAIN_URL + CHANNEL_PAGE;
        }else{
            return appRoot + CHANNEL_PAGE;
        }

    }

}

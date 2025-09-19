package com.om.module.service.busi;

import com.om.bo.base.FtpInfo;
import com.om.bo.menu.MenuBo;
import com.om.common.cache.Dict;
import com.om.common.util.DateUtil;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import com.om.module.service.label.ABaseLabel;
import com.om.module.service.label.GfDocumentLabel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

@Service("SiteManagerService")
public class SiteManagerService extends CommonService {

    public void saveBusiSiteClass(Map param) throws Exception {
        String CLASS_NAME = (String)param.get("CLASS_NAME");
        String SHORT_NAME = (String)param.get("SHORT_NAME");
        String SHOW_SEQ = (String)param.get("SHOW_SEQ");
        this.isNull("CLASS_NAME",CLASS_NAME);
        this.isNull("SHORT_NAME",SHORT_NAME);
        if(SHOW_SEQ==null){
            Map m = (Map)this.baseService.getObject("busiMapper.queryMaxCountSiteClass",param);
            int maxId = Integer.parseInt(m.get("MAXID").toString());
            param.put("SHOW_SEQ",Pk.getSeq(maxId));
        }
        String uuid = Pk.getId("SC");
        param.put("CLASS_ID",uuid);
        this.baseService.insert("busiMapper.saveBusiSiteClass",param);
    }

    public void deleteBusiSiteClass(Map param) throws Exception {
        String CLASS_ID = (String)param.get("CLASS_ID");
        this.isNull("CLASS_ID",CLASS_ID);
        param.put("SITE_CLASS",CLASS_ID);
        List list = this.baseService.getList("busiMapper.queryBusiSiteDef",param);
        if(list.size()>0){
            throw new Exception("该站点分类下还有站点【"+list.size()+"】个，无法删除站点分类");
        }
        this.baseService.insert("busiMapper.deleteBusiSiteClass",param);
    }

    public void updateBusiSiteClass(Map param) throws Exception {
        String CLASS_ID = (String)param.get("CLASS_ID");
        this.isNull("CLASS_ID",CLASS_ID);
        this.baseService.insert("busiMapper.updateBusiSiteClass",param);
    }

    public List queryBusiSiteClass(Map param) throws Exception {
        List list = this.baseService.getList("busiMapper.queryBusiSiteClass",param);
        return list;
    }




    /**
     * 站点管理的导航树
     * @param param
     * @return
     * @throws Exception
     */
    public List queryNavSiteClass(Map param) throws Exception {
        String hz = "";
        String root = "/";
        String sysHz = (String)param.get("SystemExtend");
        if("html".equals(sysHz)){
            hz = ".html";
            root = "";
        }
        this.setDataScaleConf(param);

        MenuBo bo = null;
        //站点列表
        List<Map> siteAllList = queryBusiSiteDef(param);
        //SITE_CLASS_TO_SITELIST_MAP<site_class,站点列表List<站点结果Map>》
        Map<String,List<Map>> SITE_CLASS_TO_SITELIST_MAP = new HashMap<String,List<Map>>();
        //key是site_id,value:class_id
        Map<String,String> SITE_TO_CLASS_MAP = new HashMap<String,String>();
        for(Map sMap:siteAllList){
            String site_class = sMap.get("SITE_CLASS").toString();
            String SITE_PK = sMap.get("SITE_PK").toString();
            SITE_TO_CLASS_MAP.put(SITE_PK,site_class);
            List<Map> siteList = SITE_CLASS_TO_SITELIST_MAP.get(site_class);
            if(siteList == null){
                siteList = new ArrayList<>();
                SITE_CLASS_TO_SITELIST_MAP.put(site_class,siteList);
            }
            siteList.add(sMap);
        }
        ///*modify by liuxj@20220525 note:站点管理不需要展示到栏目一级，因此屏蔽这块代码
        //由于栏目还有树型结构，所以需要处理
        List<Map> channelList = this.baseService.getList("busiMapper.queryBusiChannelDef",param);
        //SITE_MAP_CACHEJ是二级结构，key：站点PK，VALUE:MAP<channel_full_code,BO>
        Map<String,Map> SITE_MAP_CACHE2 = new HashMap<String,Map>();

        //CHANNEL_MAP_CACHE这里是一级结构，KEY是站点ID+CODE,VALUE是BO
        Map<String,MenuBo> CHANNEL_MAP_CACHE = new HashMap<String,MenuBo>();
        for(Map cMap:channelList){
            String site_pk = cMap.get("SITE_PK").toString();
            String channel_name = cMap.get("CHANNEL_NAME").toString();
            String channel_full_code = cMap.get("CHANNEL_FULL_CODE").toString();
            String channel_pk = cMap.get("CHANNEL_PK").toString();
            String class_id = SITE_TO_CLASS_MAP.get(site_pk);
            //栏目这一级别
            bo = new MenuBo(channel_name,root+"channelManager"+hz+"?SITE_PK="+site_pk+"&CLASS_ID="+class_id+"&CHANNEL_PK="+channel_pk+"&CHANNEL_FULL_CODE="+channel_full_code,"read");
            CHANNEL_MAP_CACHE.put(site_pk+"_"+channel_full_code,bo);
        }

        Map<String,List<MenuBo>> SITE_CHANNEL_LIST_MAP = new HashMap<String,List<MenuBo>>();
        Iterator it = CHANNEL_MAP_CACHE.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next().toString();
            String[] arr = key.split("_");
            String sitePk = arr[0];
            String code = arr[1];

            MenuBo curChannelBo = CHANNEL_MAP_CACHE.get(key);

            if(code.length()>3){//说明是子栏目
                code = code.substring(0,code.length()-3);
                MenuBo parentMenu =  CHANNEL_MAP_CACHE.get(sitePk+"_"+code);
                if(parentMenu==null){
                    logger.info("这个栏目是空的，应该是他的父栏目被删了，不存在："+sitePk+"_"+code);
                    continue;
                }
                List<MenuBo> childrenList = parentMenu.getChildren();
                if(childrenList == null){
                    childrenList = new ArrayList<MenuBo>();
                    parentMenu.setChildren(childrenList);
                }
                childrenList.add(curChannelBo);
            }else{//说明是根栏目
                List<MenuBo> channelListBySite = SITE_CHANNEL_LIST_MAP.get(sitePk);
                if(channelListBySite == null){
                    channelListBySite = new ArrayList<MenuBo>();
                    SITE_CHANNEL_LIST_MAP.put(sitePk,channelListBySite);
                }
                channelListBySite.add(curChannelBo);
            }
        }
        //*/

        List rsList = new ArrayList();
        List<Map> siteClassList = queryBusiSiteClass(param);
        for(int i=0;siteClassList!=null && i<siteClassList.size();i++){
            Map cMap = siteClassList.get(i);
            String className = cMap.get("CLASS_NAME").toString();
            String class_id = cMap.get("CLASS_ID").toString();
           // （站点）分类这一级别
            MenuBo siteClassBo = new MenuBo(className,root+"siteManager"+hz+"?CLASS_ID="+class_id,"qrcode");
            List<Map> siteMapList = SITE_CLASS_TO_SITELIST_MAP.get(class_id);

            List<MenuBo> siteClassChildrenList = new ArrayList<MenuBo>();
            for(int j=0;siteMapList!=null && j<siteMapList.size();j++){
                Map sMap = siteMapList.get(j);
                String site_pk = sMap.get("SITE_PK").toString();
                String site_name = sMap.get("SITE_NAME").toString();
                //站点这一级别
                MenuBo siteBo = new MenuBo(site_name,root+"channelManager"+hz+"?SITE_PK="+site_pk+"&CLASS_ID="+class_id,"gold");

                ///* modify by liuxj@20220525 note:站点管理不需要展示到栏目一级，因此屏蔽这块代码
                List<MenuBo> channelListBySite = SITE_CHANNEL_LIST_MAP.get(site_pk);
                siteBo.setChildren(channelListBySite);
                //*/

                siteClassChildrenList.add(siteBo);
            }
            siteClassBo.setChildren(siteClassChildrenList);
            rsList.add(siteClassBo);
        }
        return rsList;
    }

    /**
     * 文档发布系统的导航树
     * 本方法的逻辑和queryNavSiteClass一样，区别只是每层节点的链接地址不一样，后续也许还有其它差异，因此分开
     * @param param
     * @return
     * @throws Exception
     */
    public List queryNavDocument(Map param) throws Exception {
        String hz = "";
        String root ="/";
        String sysHz = (String)param.get("SystemExtend");
        if("html".equals(sysHz)){
            hz = ".html";
            root ="";
        }
        this.setDataScaleConf(param);
        MenuBo bo = null;
        //站点列表
        List<Map> siteAllList = queryBusiSiteDef(param);
        //SITE_CLASS_TO_SITELIST_MAP<site_class,站点列表List<站点结果Map>》
        Map<String,List<Map>> SITE_CLASS_TO_SITELIST_MAP = new HashMap<String,List<Map>>();
        //key是site_id,value:class_id
        Map<String,String> SITE_TO_CLASS_MAP = new HashMap<String,String>();
        for(Map sMap:siteAllList){
            String site_class = sMap.get("SITE_CLASS").toString();
            String SITE_PK = sMap.get("SITE_PK").toString();
            SITE_TO_CLASS_MAP.put(SITE_PK,site_class);
            List<Map> siteList = SITE_CLASS_TO_SITELIST_MAP.get(site_class);
            if(siteList == null){
                siteList = new ArrayList<>();
                SITE_CLASS_TO_SITELIST_MAP.put(site_class,siteList);
            }
            siteList.add(sMap);
        }
        //由于栏目还有树型结构，所以需要处理
        List<Map> channelList = this.baseService.getList("busiMapper.queryBusiChannelDef",param);
        //SITE_MAP_CACHEJ是二级结构，key：站点PK，VALUE:MAP<channel_full_code,BO>
        Map<String,Map> SITE_MAP_CACHE2 = new HashMap<String,Map>();

        //CHANNEL_MAP_CACHE这里是一级结构，KEY是站点ID+CODE,VALUE是BO
        Map<String,MenuBo> CHANNEL_MAP_CACHE = new HashMap<String,MenuBo>();
        for(Map cMap:channelList){
            String site_pk = cMap.get("SITE_PK").toString();
            String channel_name = cMap.get("CHANNEL_NAME").toString();
            String channel_pk = cMap.get("CHANNEL_PK").toString();
            String channel_full_code = cMap.get("CHANNEL_FULL_CODE").toString();

            String class_id = SITE_TO_CLASS_MAP.get(site_pk);
            //栏目这一级别
            bo = new MenuBo(channel_name,root+"document"+hz+"?SITE_PK="+site_pk+"&CLASS_ID="+class_id+"&CHANNEL_PK="+channel_pk+"&CHANNEL_FULL_CODE="+channel_full_code,"");
            bo.setValue(class_id+"_"+site_pk+"_"+channel_pk);
            CHANNEL_MAP_CACHE.put(site_pk+"_"+channel_full_code,bo);
        }

        Map<String,List<MenuBo>> SITE_CHANNEL_LIST_MAP = new HashMap<String,List<MenuBo>>();
        Iterator it = CHANNEL_MAP_CACHE.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next().toString();
            String[] arr = key.split("_");
            String sitePk = arr[0];
            String code = arr[1];

            MenuBo curChannelBo = CHANNEL_MAP_CACHE.get(key);

            if(code.length()>3){//说明是子栏目
                code = code.substring(0,code.length()-3);
                MenuBo parentMenu =  CHANNEL_MAP_CACHE.get(sitePk+"_"+code);
                if(parentMenu==null){
                    logger.info("这个栏目是空的，应该是他的父栏目被删了，不存在："+sitePk+"_"+code);
                    continue;
                }
                List<MenuBo> childrenList = parentMenu.getChildren();
                if(childrenList == null){
                    childrenList = new ArrayList<MenuBo>();
                    parentMenu.setChildren(childrenList);
                }
                childrenList.add(curChannelBo);
            }else{//说明是根栏目
                List<MenuBo> channelListBySite = SITE_CHANNEL_LIST_MAP.get(sitePk);
                if(channelListBySite == null){
                    channelListBySite = new ArrayList<MenuBo>();
                    SITE_CHANNEL_LIST_MAP.put(sitePk,channelListBySite);
                }
                channelListBySite.add(curChannelBo);
            }
        }

        List rsList = new ArrayList();
        List<Map> siteClassList = queryBusiSiteClass(param);
        for(int i=0;siteClassList!=null && i<siteClassList.size();i++){
            Map cMap = siteClassList.get(i);
            String className = cMap.get("CLASS_NAME").toString();
            String class_id = cMap.get("CLASS_ID").toString();
            // （站点）分类这一级别
            MenuBo siteClassBo = new MenuBo(className,root+"document"+hz+"?CLASS_ID="+class_id ,"");
            siteClassBo.setValue(class_id);
            List<Map> siteMapList = SITE_CLASS_TO_SITELIST_MAP.get(class_id);

            List<MenuBo> siteClassChildrenList = new ArrayList<MenuBo>();
            for(int j=0;siteMapList!=null && j<siteMapList.size();j++){
                Map sMap = siteMapList.get(j);
                String site_pk = sMap.get("SITE_PK").toString();
                String site_name = sMap.get("SITE_NAME").toString();
                //站点这一级别
                MenuBo siteBo = new MenuBo(site_name, root+"document"+hz+"?SITE_PK="+site_pk+"&CLASS_ID="+class_id,"");
                siteBo.setValue(class_id+"_"+site_pk);
                List<MenuBo> channelListBySite = SITE_CHANNEL_LIST_MAP.get(site_pk);
                siteBo.setChildren(channelListBySite);

                siteClassChildrenList.add(siteBo);
            }
            siteClassBo.setChildren(siteClassChildrenList);
            rsList.add(siteClassBo);
        }
        return rsList;
    }




    /*
     已经废弃，之前的版本，保留三个月后删除，cur_date:2022-5-25
     * 文档发布系统的导航树
     * 本方法的逻辑和queryNavSiteClass一样，区别只是每层节点的链接地址不一样，后续也许还有其它差异，因此分开
     * @param param
     * @return
     * @throws Exception

    public List queryNavDocument(Map param) throws Exception {
        MenuBo bo = null;
        //站点列表
        List<Map> siteAllList = queryBusiSiteDef(param);
        //SITE_CLASS_TO_SITELIST_MAP<site_class,站点列表List<站点结果Map>》
        Map<String,List<Map>> SITE_CLASS_TO_SITELIST_MAP = new HashMap<String,List<Map>>();
        //key是site_id,value:class_id
        Map<String,String> SITE_TO_CLASS_MAP = new HashMap<String,String>();
        for(Map sMap:siteAllList){
            String site_class = sMap.get("SITE_CLASS").toString();
            String SITE_PK = sMap.get("SITE_PK").toString();
            SITE_TO_CLASS_MAP.put(SITE_PK,site_class);
            List<Map> siteList = SITE_CLASS_TO_SITELIST_MAP.get(site_class);
            if(siteList == null){
                siteList = new ArrayList<>();
                SITE_CLASS_TO_SITELIST_MAP.put(site_class,siteList);
            }
            siteList.add(sMap);
        }
        //由于栏目还有树型结构，所以需要处理
        List<Map> channelList = this.baseService.getList("busiMapper.queryBusiChannelDef",param);
        //SITE_MAP_CACHEJ是二级结构，key：站点PK，VALUE:MAP<channel_full_code,BO>
        Map<String,Map> SITE_MAP_CACHE2 = new HashMap<String,Map>();

        //CHANNEL_MAP_CACHE这里是一级结构，KEY是站点ID+CODE,VALUE是BO
        Map<String,MenuBo> CHANNEL_MAP_CACHE = new HashMap<String,MenuBo>();
        for(Map cMap:channelList){
            String site_pk = cMap.get("SITE_PK").toString();
            String channel_name = cMap.get("CHANNEL_NAME").toString();
            String channel_pk = cMap.get("CHANNEL_PK").toString();
            String channel_full_code = cMap.get("CHANNEL_FULL_CODE").toString();

            String class_id = SITE_TO_CLASS_MAP.get(site_pk);
            //栏目这一级别
            bo = new MenuBo(channel_name,"/document?SITE_PK="+site_pk+"&CLASS_ID="+class_id+"&CHANNEL_PK="+channel_pk,"read");
            CHANNEL_MAP_CACHE.put(site_pk+"_"+channel_full_code,bo);
        }

        Map<String,List<MenuBo>> SITE_CHANNEL_LIST_MAP = new HashMap<String,List<MenuBo>>();
        Iterator it = CHANNEL_MAP_CACHE.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next().toString();
            String[] arr = key.split("_");
            String sitePk = arr[0];
            String code = arr[1];

            MenuBo curChannelBo = CHANNEL_MAP_CACHE.get(key);

            if(code.length()>3){//说明是子栏目
                code = code.substring(0,code.length()-4);
                MenuBo parentMenu =  CHANNEL_MAP_CACHE.get(sitePk+"_"+code);
                List<MenuBo> childrenList = parentMenu.getChildren();
                if(childrenList == null){
                    childrenList = new ArrayList<MenuBo>();
                    parentMenu.setChildren(childrenList);
                }
                childrenList.add(curChannelBo);
            }else{//说明是根栏目
                List<MenuBo> channelListBySite = SITE_CHANNEL_LIST_MAP.get(sitePk);
                if(channelListBySite == null){
                    channelListBySite = new ArrayList<MenuBo>();
                    SITE_CHANNEL_LIST_MAP.put(sitePk,channelListBySite);
                }
                channelListBySite.add(curChannelBo);
            }
        }

        List rsList = new ArrayList();
        List<Map> siteClassList = queryBusiSiteClass(param);
        for(int i=0;siteClassList!=null && i<siteClassList.size();i++){
            Map cMap = siteClassList.get(i);
            String className = cMap.get("CLASS_NAME").toString();
            String class_id = cMap.get("CLASS_ID").toString();
            // （站点）分类这一级别
            //MenuBo siteClassBo = new MenuBo(className,"/siteManager?CLASS_ID="+class_id,"qrcode");
            MenuBo siteClassBo = new MenuBo(className,"/document?CLASS_ID="+class_id,"qrcode");
            List<Map> siteMapList = SITE_CLASS_TO_SITELIST_MAP.get(class_id);

            List<MenuBo> siteClassChildrenList = new ArrayList<MenuBo>();
            for(int j=0;siteMapList!=null && j<siteMapList.size();j++){
                Map sMap = siteMapList.get(j);
                String site_pk = sMap.get("SITE_PK").toString();
                String site_name = sMap.get("SITE_NAME").toString();
                //站点这一级别
                //MenuBo siteBo = new MenuBo(site_name,"/channelManager?SITE_PK="+site_pk+"&CLASS_ID="+class_id,"gold");
                MenuBo siteBo = new MenuBo(site_name,"/document?SITE_PK="+site_pk+"&CLASS_ID="+class_id,"gold");
                List<MenuBo> channelListBySite = SITE_CHANNEL_LIST_MAP.get(site_pk);
                siteBo.setChildren(channelListBySite);

                siteClassChildrenList.add(siteBo);
            }
            siteClassBo.setChildren(siteClassChildrenList);
            rsList.add(siteClassBo);
        }
        return rsList;
    }
     */



    public void saveBusiSiteDef(Map param) throws Exception {
        String SITE_CODE = (String)param.get("SITE_CODE");
        String SITE_NAME = (String)param.get("SITE_NAME");
        String SITE_DIR = (String)param.get("SITE_DIR");
        String DOMAIN_URL = (String)param.get("DOMAIN_URL");
        String SHOW_SEQ = (String)param.get("SHOW_SEQ");
        String SITE_TMPL = (String)param.get("SITE_TMPL");
        String CHANNEL_TMPL = (String)param.get("CHANNEL_TMPL");
        String DOC_TMPL = (String)param.get("DOC_TMPL");
        String DATA_VIEW = (String)param.get("DATA_VIEW");
        String IS_PUSH = (String)param.get("IS_PUSH");
        String IS_PULL = (String)param.get("IS_PULL");
        String IS_MUST_VALID = (String)param.get("IS_MUST_VALID");
        String C_USER = (String)param.get("C_USER");
        String SITE_CLASS = (String)param.get("SITE_CLASS");
        this.isNull("SITE_CLASS",SITE_CLASS);
        this.isNull("SITE_CODE",SITE_CODE);
        this.isNull("SITE_NAME",SITE_NAME);
        this.isNull("SITE_DIR",SITE_DIR);
        this.isNull("IS_PUSH",IS_PUSH);
        this.isNull("IS_PULL",IS_PULL);
        this.isNull("IS_MUST_VALID",IS_MUST_VALID);
        this.isNull("C_USER",C_USER);
        this.isEnum("IS_PUSH",IS_PUSH,"1,0");
        this.isEnum("IS_PULL",IS_PULL,"1,0");
        this.isEnum("IS_MUST_VALID",IS_MUST_VALID,"1,0");

        param.put("SAVEASDIR",SITE_DIR);
        if(SHOW_SEQ==null){
            Map m = (Map)this.baseService.getObject("busiMapper.queryMaxCountSiteClass",param);
            int maxId = Integer.parseInt(m.get("MAXID").toString());
            param.put("SHOW_SEQ",Pk.getSeq(maxId));
        }
        if(DATA_VIEW==null){
            DATA_VIEW="0";
        }
        String uuid = Pk.getId("SD");
        param.put("SITE_PK",uuid);
        this.baseService.insert("busiMapper.saveBusiSiteDef",param);
    }

    public void deleteBusiSiteDef(Map param) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("SITE_PK",SITE_PK);

        //删除站点，要检查站点下是否没有栏目
        List list = this.baseService.getList("busiMapper.querySiteUsed",param);
        if(list.size()>0){
            StringBuffer sb = new StringBuffer("对不起，站点下以下栏目，无法删除，请先删除栏目：");
            for(int i=0;i<list.size();i++){
                Map map = (Map)list.get(i);
                String type = map.get("TYPE2").toString();
                String name = map.get("NAME2").toString();
                if("1".equals(type)){
                    sb.append("栏目名称【").append(name).append("】");
                }
            }
            throw new Exception(sb.toString());
        }

        this.baseService.insert("busiMapper.deleteBusiSiteDef",param);
    }

    public void updateBusiSiteDef(Map param) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("SITE_PK",SITE_PK);
        this.baseService.insert("busiMapper.updateBusiSiteDef",param);
    }


    public void updateBusiSiteDefOffline(Map param) throws Exception {
        param.put("STS", Dict.SiteSts.Discard);
        updateBusiSiteDef(param);
    }
    public void updateBusiSiteDefOnline(Map param) throws Exception {
        param.put("STS",Dict.SiteSts.Draft);
        updateBusiSiteDef(param);
    }

    public List queryBusiSiteDef(Map param) throws Exception {
        List list = this.baseService.getList("busiMapper.queryBusiSiteDef",param);
        return list;
    }



    /**
     * 预览站点首页，生成页面index.html，思路是这样：
     * 1 查找根据站点编号查找站点和模板的信息
     * 2 生成页面为站点下的index.html
     * @param param
     * @return 生成的预览地址
     * @throws Exception
     */
    public String preViewSiteDefHomePage(Map param,FtpInfo ftp) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("SITE_PK",SITE_PK);

        Map tmplMap = (Map)this.baseService.getObject("busiMapper.queryTemplateInfoBySitePk",param);
        if(tmplMap == null ){
            throw new Exception("请检查该站点的模板是否未指定");
        }
        String TMPL_CONTENT = (String)tmplMap.get("TMPL_CONTENT");
        String SITE_CODE = (String)tmplMap.get("SITE_CODE");
        String DOMAIN_URL = (String)tmplMap.get("DOMAIN_URL");


        Map inputMap = tmplMap;

        StringBuffer sb = new StringBuffer(TMPL_CONTENT);
        ABaseLabel label = new GfDocumentLabel(TMPL_CONTENT);
        label.setInputMap(inputMap);
        label.switchLabel(sb,inputMap,baseService,1,1);////这里仅仅是一个示例

        logger.debug("文档的内容："+sb.toString());
        String serverRootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();



        String context = SITE_CODE + sep + "001";//http://222.75.42.95:17442/files//nxhajjApply/001/index_1.html
        String file = "index_1.html";//站点首页，和栏目的概览统一用index.html,区别在于不同的路径下
        String path = serverRootPath+context+sep;
        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }

        FileWriter fw = new FileWriter(path+file);
        fw.write(sb.toString());
        fw.close();
        //String homePage = replaceDomainUrl(appRoot+context+sep+file,DOMAIN_URL,SITE_CODE);
        String homePage = sep + "001"+sep+file;
        param.put("HOME_PAGE",homePage);
        boolean isDeploy = this.deployFile(serverRootPath, ftp.getTargetRoot(), context, file, ftp);
        if(isDeploy){
            param.put("IS_DEPLOY",Dict.DeploySts.succ);
        }else{
            param.put("IS_DEPLOY",Dict.DeploySts.err);
        }
        this.baseService.insert("busiMapper.updateBusiSiteDef",param);

        return appRoot+context+sep+file;
    }

    /**
     * 发布站点及下面栏目、文章
     * @param param
     * @param ftp
     * @throws Exception
     */
    public void publishSiteAll(Map param,FtpInfo ftp) throws Exception {
        //1 生成站点首页
        this.preViewSiteDefHomePage(param,ftp);
        //2 修改站点的状态为待发布
        Map p = new HashMap();
        p.put("SITE_PK",param.get("SITE_PK"));
        p.put("STS", Dict.SiteSts.toPublish);
        this.updateBusiSiteDef(p);
    }

    public String viewSitePath(Map param,FtpInfo ftp) throws Exception {
        Map p = new HashMap();
        p.putAll(param);
        p.put("QUERY_NAME","网站首页");
        List list = this.baseService.getList("busiMapper.queryBusiChannelDef",p);
        if(list.size() == 0){
            p.put("QUERY_NAME","首页");
            list = this.baseService.getList("busiMapper.queryBusiChannelDef",p);
        }
        String appRoot = ftp.getAppRootPath();
        if(list.size()>0) {
            Map m = (Map)list.get(0);
            String CHANNEL_PK = (String)m.get("CHANNEL_PK");
            param.put("CHANNEL_PK",CHANNEL_PK);

            Map DocumentPathContext = (Map) this.baseService.getObject("busiMapper.queryDocumentPathContext", param);
            String SITE_CODE = DocumentPathContext.get("SITE_CODE").toString();
            String CHANNEL_CODE = DocumentPathContext.get("CHANNEL_CODE").toString();
            String SITE_PK = DocumentPathContext.get("SITE_PK").toString();
            String DOMAIN_URL = (String) DocumentPathContext.get("DOMAIN_URL");
            String CHANNEL_PAGE = (String) DocumentPathContext.get("CHANNEL_PAGE");
            String SITE_PAGE = (String) DocumentPathContext.get("SITE_PAGE");

            if (DOMAIN_URL != null && DOMAIN_URL.toLowerCase().startsWith("http")) {
                return DOMAIN_URL + CHANNEL_PAGE;
            } else {
                return appRoot + CHANNEL_PAGE;
            }
        }else{
            list = this.baseService.getList("busiMapper.queryBusiSiteDef",param);
            Map m = (Map)list.get(0);
            String DOMAIN_URL = (String) m.get("DOMAIN_URL");
            if (DOMAIN_URL != null && DOMAIN_URL.toLowerCase().startsWith("http")) {
                return DOMAIN_URL ;
            } else {
                return appRoot ;
            }
        }

    }

}

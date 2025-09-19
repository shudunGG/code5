package com.om.module.service.busi;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.ObjectTools;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import com.om.module.service.label.ABaseLabel;
import com.om.module.service.label.GfDocumentLabel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("AutoPublishService")
public class AutoPublishService extends CommonService {

    @Resource(name = "DocumentManagerService")
    private DocumentManagerService docService;

    @Resource(name = "ChannelManagerService")
    private ChannelManagerService channelService;


    /**
     * 发布该栏目下的的附件
     * @param siteMap     *
     * @param channelMap
     * @param ftp
     * @throws Exception
     */
    public void publisDocFilesByChannelPk(Map siteMap,Map channelMap, FtpInfo ftp) throws Exception {
        if(2>1)return;

        /*
        String CHANNEL_PK = (String)channelMap.get("CHANNEL_PK");
        this.isNull("CHANNEL_PK",CHANNEL_PK);

        Map p = new HashMap();
        p.put("CHANNEL_PK",CHANNEL_PK);
        //找到这个栏目下所有状态是9（待发布的）
        List list = this.baseService.getList("busiMapper.queryBusiDocumentFile",p);
        for(int i=0;i<list.size();i++){
            Map m = (Map)list.get(i);
            String FILE_PATH_URL = (String)m.get("FILE_PATH_URL");
            String DOMAIN_URL =  (String)siteMap.get("DOMAIN_URL");
            String SITE_CODE =  (String)siteMap.get("SITE_CODE");
            if(FILE_PATH_URL!=null && DOMAIN_URL!=null){
                String homePage = replaceDomainUrl(FILE_PATH_URL,DOMAIN_URL,SITE_CODE);
                m.put("FILE_PATH_URL",homePage);
                baseService.insert("busiMapper.updateDocumentFileUrlByPk",m);
            }
        }*/
    }


    /**
     * 发布该栏目下的的待发布文章
     * @param param
     * @param ftp
     * @throws Exception
     */
    public void publisAllNewsByChannelPk(Map param, FtpInfo ftp) throws Exception {
        String CHANNEL_PK = (String)param.get("CHANNEL_PK");
        this.isNull("CHANNEL_PK",CHANNEL_PK);

        param.put("QRY_STS_ARR","9,99");

        //找到这个栏目下所有状态是9（待发布的）
        List list = this.baseService.getList("busiMapper"+Dict.dbMap+".queryBusiDocumentDef",param);
        for(int i=0;i<list.size();i++){
            Map m = (Map)list.get(i);
            Map m2 = new HashMap();
            m2.put("DOC_PK",m.get("DOC_PK"));
            docService.preViewDocumentDef(m2,ftp);
            m2.put("STS",Dict.DocSts.PublishOk);
            baseService.insert("busiMapper.updateBusiDocumentSts",m2);
        }
    }

    /**
     *
     * @param param 这上gf_site_def的记录
     * @param ftp
     * @throws Exception
     */
    public void publishAllChannelBySitePk(Map param,FtpInfo ftp) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("SITE_PK",SITE_PK);

        String serverRootPath = ftp.getRootPath();
        String appRoot = ftp.getAppRootPath();
        //找到这个站点下的所有栏目
        List list = this.baseService.getList("busiMapper.queryBusiChannelDef",param);
        for(int i=0;i<list.size();i++){
            Map m = (Map)list.get(i);
            String STS = m.get("STS").toString();
            String CHANNEL_PK = m.get("CHANNEL_PK").toString();
            Map mSon = new HashMap();//主要是查询方法里有动态查询 ，太多参数进去后影响查询结果
            mSon.put("CHANNEL_PK",CHANNEL_PK);

            //先更新文档，再更新栏目首页，因为栏目首页里的某些置标，可能会用到文档内容。
            publisDocFilesByChannelPk(param,m, ftp);//这个函数应该不需要再执行了，因为附件的地址已经被我改成相对地址了
            this.publisAllNewsByChannelPk(mSon,ftp);

            this.channelService.publishChannelAll(mSon,ftp);

        }
        Map p = new HashMap();
        p.put("STS",Dict.DocSts.PublishOk);
        p.put("SITE_PK",SITE_PK);
        baseService.insert("busiMapper.updateBusiSiteDef",p);
        logger.debug("将该网站的状态改为99：【SITE_PK】"+SITE_PK);
    }

    public List getAllpublishSite() throws Exception {
        Map param = new HashMap();
        param.put("STS",Dict.SiteSts.toPublish);
        //找到这个站点下的所有栏目
        List list = this.baseService.getList("busiMapper.queryBusiSiteDef",param);
        return list;
    }


}

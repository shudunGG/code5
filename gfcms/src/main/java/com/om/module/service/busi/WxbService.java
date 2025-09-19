package com.om.module.service.busi;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.cache.WxbCache;
import com.om.common.util.ObjectTools;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import com.om.module.service.label.ABaseLabel;
import com.om.module.service.label.GfDocumentLabel;
import com.om.module.service.label.GfDocumentsLabel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("WxbService")
public class WxbService extends CommonService {
    public void saveGfWxbAccount(Map param) throws Exception {
        String TYPE = (String)param.get("TYPE");
        String APPID = (String)param.get("APPID");
        String APPSEC = (String)param.get("APPSEC");
        String NAME = (String)param.get("NAME");
        String WX_TOKEN = (String)param.get("WX_TOKEN");
        String WX_ENCODINGAESKEY = (String)param.get("WX_ENCODINGAESKEY");
        this.isNull("TYPE",TYPE);
        this.isNull("APPID",APPID);
        this.isNull("APPSEC",APPSEC);
        this.isNull("NAME",NAME);
        this.isEnum("TYPE",TYPE,"wx,wb");
        this.baseService.insert("busiMapper"+Dict.dbMap+".saveGfWxbAccount",param);
        WxbCache.add(APPID,param);
    }

    public void deleteGfWxbAccount(Map param) throws Exception {
        String APPID = (String)param.get("APPID");
        String APPSEC = (String)param.get("APPSEC");
        this.isNull("APPID",APPID);
        this.isNull("APPSEC",APPSEC);
        this.baseService.insert("busiMapper"+Dict.dbMap+".deleteGfWxbAccount",param);
        WxbCache.remove(APPID);
    }

    public void updateGfWxbAccount(Map param) throws Exception {
        String APPID = (String)param.get("APPID");
        String NAME = (String)param.get("NAME");
        this.isNull("APPID",APPID);
        param.put("CONFIG_TIME",1);
        this.baseService.insert("busiMapper"+Dict.dbMap+".updateGfWxbAccount",param);
        WxbCache.update(APPID,param);
    }

    public List queryGfWxbAccount(Map param) throws Exception {
        String TYPE = (String)param.get("TYPE");
        List list = this.baseService.getList("busiMapper"+Dict.dbMap+".queryGfWxbAccount",param);
        WxbCache.load(list);
        return list;
    }

    public int queryWcmUserByName(Map param) throws Exception {
        String name = (String)param.get("name");
        this.isNull("name",name);
        logger.info("queryWcmUserByName loadmapping:"+"busiMapper"+Dict.dbMap+".queryWcmUserByName");
        List list = this.baseService.getList("busiMapper"+Dict.dbMap+".queryWcmUserByName",param);
        return list.size();
    }
}

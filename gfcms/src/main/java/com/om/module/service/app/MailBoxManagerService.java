package com.om.module.service.app;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.ObjectTools;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import com.om.module.service.label.ABaseLabel;
import com.om.module.service.label.GfDocumentsLabel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("MailBoxManagerService")
public class MailBoxManagerService extends CommonService {

    public void saveBusiAppMailBox(Map param) throws Exception {
        printParam(param,"saveBusiAppMailBox==:");
        String SITE_PK = (String)param.get("SITE_PK");
        String BOX_NAME = (String)param.get("BOX_NAME");
        String USER_ID = (String)param.get("USER_ID");
        this.isNull("SITE_PK",SITE_PK);
        this.isNull("BOX_NAME",BOX_NAME);
        this.isNull("USER_ID",USER_ID);
        Map tmpMap = new HashMap();
        tmpMap.put("SITE_PK",SITE_PK);
        tmpMap.put("BOX_NAME",BOX_NAME);
        List boxList = (List)this.baseService.getList("busiMapper"+Dict.dbMap+".queryBusiAppMailBox",tmpMap);
        if(boxList.size()>0){
            throw new Exception("在选中的站点下已经存在信箱【"+BOX_NAME+"】！");
        }
        String uuid = Pk.getId("M");
        param.put("BOX_SECRET",uuid);
        param.put("C_USER",USER_ID);
        param.put("U_USER",USER_ID);
        this.baseService.insert("busiMapper.saveBusiAppMailBox",param);
    }

    public void DisabledBusiAppMailBox(Map param) throws Exception {
        printParam(param,"DisabledBusiAppMailBox==:");
        String SITE_PK = (String)param.get("SITE_PK");
        String BOX_NAME = (String)param.get("BOX_NAME");
        String USER_ID = (String)param.get("USER_ID");
        this.isNull("SITE_PK",SITE_PK);
        this.isNull("BOX_NAME",BOX_NAME);
        this.isNull("USER_ID",USER_ID);

        param.put("STS",-1);
        param.put("U_USER",USER_ID);

        this.baseService.insert("busiMapper.updateBusiAppMailBox",param);
    }

    public void deleteBusiAppMailBox(Map param) throws Exception {
        printParam(param,"deleteBusiAppMailBox==:");
        String SITE_PK = (String)param.get("SITE_PK");
        String BOX_NAME = (String)param.get("BOX_NAME");
        String USER_ID = (String)param.get("USER_ID");
        this.isNull("SITE_PK",SITE_PK);
        this.isNull("BOX_NAME",BOX_NAME);
        this.isNull("USER_ID",USER_ID);

        Map maiBox = (Map)this.baseService.getObject("busiMapper"+Dict.dbMap+".queryBusiAppMailBox",param);
        String STS = maiBox.get("STS").toString();
        if("1".equals(STS)){
            throw new Exception("该信箱的状态为启动状态，不能删除，需要先停用！");
        }
        this.baseService.insert("busiMapper.deleteBusiAppMailBox",param);
    }

    public void updateBusiAppMailBox(Map param) throws Exception {
        printParam(param,"updateBusiAppMailBox==:");
        String SITE_PK = (String)param.get("SITE_PK");
        String BOX_NAME = (String)param.get("BOX_NAME");
        String NEW_BOX_NAME = (String)param.get("NEW_BOX_NAME");
        String STS = (String)param.get("STS");
        String USER_ID = (String)param.get("USER_ID");
        this.isNull("SITE_PK",SITE_PK);
        this.isNull("BOX_NAME",BOX_NAME);
        this.isNull("U_USER",USER_ID);
        this.baseService.insert("busiMapper.updateBusiAppMailBox",param);
    }


    public List queryBusiAppMailBox(Map param) throws Exception {
        printParam(param,"queryBusiAppMailBox==:");
        String SITE_PK = (String)param.get("SITE_PK");
        String BOX_NAME = (String)param.get("BOX_NAME");
        String STS = (String)param.get("STS");

        List list = this.baseService.getList("busiMapper"+Dict.dbMap+".queryBusiAppMailBox",param);
        return list;
    }

}

package com.om.module.service.busi;

import com.alibaba.fastjson.JSONObject;
import com.om.module.service.common.CommonService;
import com.om.util.AesUtil5;
import com.om.util.HttpInterface;
import com.om.util.Pk;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("AppMailBoxService")
public class AppMailBoxService extends CommonService {

    public String saveBusiAppMailMsg(Map param,String url) throws Exception {

        printParam(param,"saveBusiAppMailMsg==:");
        String SITE_PK = (String)param.get("SITE_PK");
        String BOX_NAME = (String)param.get("BOX_NAME");
        String M_TITLE = (String)param.get("M_TITLE");
        String M_CONTENT = (String)param.get("M_CONTENT");
        String LINK_MAN = (String)param.get("LINK_MAN");
        String LINK_PHONE = (String)param.get("LINK_PHONE");
        String IS_OPEN = (String)param.get("IS_OPEN");
        String LINK_MAIL = (String)param.get("LINK_MAIL");

        this.isNull("SITE_PK",SITE_PK);
        this.isNull("BOX_NAME",BOX_NAME);
        this.isNull("M_TITLE",M_TITLE);
        this.isNull("M_CONTENT",M_CONTENT);
        this.isNull("LINK_MAN",LINK_MAN);
        this.isNull("LINK_PHONE",LINK_PHONE);
        this.isNull("IS_OPEN",IS_OPEN);

        logger.debug("M_CONTENT before:"+M_CONTENT);
        if(M_CONTENT.indexOf("%")>-1){
            M_CONTENT =  URLDecoder.decode(M_CONTENT,"UTF-8");
            param.put("M_CONTENT",M_CONTENT);
        }
        logger.debug("M_CONTENT after:"+M_CONTENT);


        String uuid = Pk.getId("M");
        param.put("MSG_PK",uuid);
        JSONObject json = new JSONObject(param);
        Map headMap = new HashMap();

        String strResult = HttpInterface.doPostJson(url,headMap,json);
        logger.debug("saveBusiAppMailMsg reponse result:"+strResult);
        return strResult;
    }


    public String queryMailList(Map param,String url) throws Exception {
        Map headMap = new HashMap();
        JSONObject json = new JSONObject(param);
        String strResult = HttpInterface.doPostJson(url,headMap,json);
        return strResult;
    }


}

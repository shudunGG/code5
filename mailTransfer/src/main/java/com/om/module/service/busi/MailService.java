package com.om.module.service.busi;

import com.alibaba.fastjson.JSONObject;
import com.om.module.service.common.CommonService;
import com.om.util.AesUtil5;
import com.om.util.HttpInterface;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("MailService")
public class MailService extends CommonService {

    public String getToken(String url,String appId,String appKey) throws Exception {
        Map headMap = new HashMap();
        headMap.put("appId",appId);
        headMap.put("appKey",appKey);
        String s =null;
        String strResult = HttpInterface.doPostJson(url,headMap,s);;
        JSONObject jobj =JSONObject.parseObject(strResult);
        return jobj.getJSONObject("data").getString("token");
    }

    public String mailSave(Map param,String url,String appId,String appKey,String key,String tokenUrl) throws Exception {
        String token = this.getToken(tokenUrl,appId,appKey);
        Map headMap = new HashMap();
        headMap.put("appId",appId);
        headMap.put("appKey",appKey);
        headMap.put("token",token);

        String name = (String)param.get("name");
        String areaCode = (String)param.get("areaCode");
        String address = (String)param.get("address");
        String addresseetype = (String)param.get("addresseetype");
        String id = param.get("id").toString();
        String title = (String)param.get("title");
        String content = (String)param.get("content");
        this.isNull("name",name);
        this.isNull("areaCode",areaCode);
        this.isNull("address",address);
        this.isNull("addresseetype",addresseetype);
        this.isNull("id",id);
        this.isNull("title",title);
        this.isNull("content",content);

        JSONObject reqJson =new JSONObject(param);
        logger.debug("request param:"+reqJson);
        String body= AesUtil5.encrypt(reqJson.toJSONString(),key);

        String strResult = HttpInterface.doPostJson(url,headMap,body);

        logger.info("mailSave reponse result:"+strResult);
        return strResult;
    }


    public List queryMailList(Map param,String url,String appId,String appKey,String key,String tokenUrl) throws Exception {
        String token = this.getToken(tokenUrl,appId,appKey);
        Map headMap = new HashMap();
        headMap.put("appId",appId);
        headMap.put("appKey",appKey);
        headMap.put("token",token);

        String name = (String)param.get("name");
        String cardNo = (String)param.get("cardNo");
        String cxm = (String)param.get("cxm");
        String isPublic = (String)param.get("isPublic");

        JSONObject reqJson =new JSONObject(param);
       // String body= AesUtil5.encrypt(reqJson.toJSONString(),key);
        String appParam = "name="+name+"&cardNo="+cardNo+"&cxm="+cxm+"&isPublic="+isPublic+"&appId="+appId+"&appKey="+appKey+"&token="+token;
        String strResult = HttpInterface.sendGet(url+"?"+appParam,headMap);
        JSONObject jobj =JSONObject.parseObject(strResult);
        logger.info("queryMailList reponse result:"+strResult);
        List list = jobj.getJSONArray("data");
        return list;
    }


    public Map queryMailDetail(Map param,String url,String appId,String appKey,String key,String tokenUrl) throws Exception {
        String token = this.getToken(tokenUrl,appId,appKey);
        Map headMap = new HashMap();
        headMap.put("appId",appId);
        headMap.put("appKey",appKey);
        headMap.put("token",token);

        String id = (String)param.get("id");
        this.isNull("id",id);

        JSONObject reqJson =new JSONObject(param);
        //String body= AesUtil5.encrypt(reqJson.toJSONString(),key);
        String appParam = "id="+id+"&appId="+appId+"&appKey="+appKey+"&token="+token;
        String strResult = HttpInterface.sendGet(url+"?"+appParam,headMap);
        JSONObject jobj =JSONObject.parseObject(strResult);
        logger.info("queryMailList reponse result:"+strResult);
        Map map = jobj.getJSONObject("data");
        return map;
    }


    //-----------------------------新版增加的-------------
    private void switchColVal(Map map,String IntfCol,String DBCol){
        if(map.get(IntfCol) == null){
            map.put(IntfCol,map.get(DBCol));
        }
    }

    public List queryToProcessAndSendPost(String url,String appId,String appKey,String key,String tokenUrl) throws Exception {
        Map param = new HashMap();
        param.put("STATUS",0);
        param.put("SCAN_DATA",1);
        List list = this.baseService.getList("busiMapper.queryAppmetatableappmetableljzxx",param);
        logger.info("queryToProcessAndSendPost size:"+list.size());
        for(int i=0;i<list.size();i++){
            Map map = (Map)list.get(i);
            map.put("areaCode","620302000000");
            map.put("addresseetype","20");
            if(map.get("ADRESS") == null || "".equals(map.get("ADRESS"))){
                map.put("ADRESS","地址：用户未填写");
            }

            switchColVal(map,"name","NAME");
            //switchColVal(map,"areaCode","620302");//目前Trsapp取不到该值，直接写死

            switchColVal(map,"address","ADRESS");
           // switchColVal(map,"addresseetype","20");//20代表区长信箱
            switchColVal(map,"id","METADATAID");
            switchColVal(map,"title","TITLE");
            switchColVal(map,"content","CONTENT");
            switchColVal(map,"phone","TEL");
            switchColVal(map,"mobile","TEL");
            switchColVal(map,"email","EMALL");
            switchColVal(map,"cardNo","ZJHM");
            String strResult = "init";
            try{
                strResult = this.mailSave(map,url,appId,appKey,key,tokenUrl);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
            //???? 表queryAppmetatableappmetableljzxx增加一个字段STR_RESULT，POST_TIME
            Map map2 = new HashMap();
            map2.put("METADATAID",map.get("METADATAID"));
            map2.put("STR_RESULT",strResult);
            this.baseService.insert("busiMapper.updateAppmetatableappmetableljzxx",map2);
        }

        return list;
    }


    public void updateAppmetatable(Map param) throws Exception {

        String REPLYUSER = (String)param.get("REPLYUSER");
        String REPLYCONTENT = (String)param.get("REPLYCONTENT");
        String REPLYDEPT = (String)param.get("REPLYDEPT");
        String REPLYTIMESTR = (String)param.get("REPLYTIMESTR");
        String METADATAID = (String)param.get("METADATAID");
        String YYGK = (String)param.get("YYGK");
        Date REPLYTIME = null;
        this.isNull("REPLYUSER",REPLYUSER);
        this.isNull("REPLYCONTENT",REPLYCONTENT);
        this.isNull("REPLYDEPT",REPLYDEPT);
        this.isNull("REPLYTIMESTR",REPLYTIMESTR);
        this.isNull("METADATAID",METADATAID);
        this.isEnum("YYGK",YYGK,"公开,不公开");
//
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            REPLYTIME = sdf.parse(REPLYTIMESTR);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception("["+REPLYTIMESTR+"]值的格式为【yyyy-MM-dd HH:mm:ss】，请检查");
        }
        param.put("REPLYTIME",REPLYTIME);
        if("公开".equals(YYGK)){
            param.put("ispublic",1);
        }else{
            param.put("ispublic",0);
        }
        param.put("STATUS",2);
        this.baseService.insert("busiMapper.updateAppmetatableappmetableljzxx",param);
    }

}

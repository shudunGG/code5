package com.om.module.service.busi;

import com.alibaba.fastjson.JSONObject;
import com.om.module.core.base.redis.service.RedisBaseService;
import com.om.module.service.common.CommonService;
import com.om.util.AesUtil5;
import com.om.util.HttpInterface;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("XiNingIGITransService")
public class XiNingIGITransService extends CommonService {

    private final int expireSec = 1200;

    @Resource(name = "redisBaseService")
    private RedisBaseService redisService;


    public String getToken(String url,String appId,String appKey) throws Exception {
        Map headMap = new HashMap();
        headMap.put("appId",appId);
        headMap.put("appKey",appKey);
        String s = null;
        String strResult = HttpInterface.doPostJson(url,headMap,s);
        JSONObject jobj =JSONObject.parseObject(strResult);
        return jobj.getJSONObject("data").getString("token");
    }


    public Map openSaveGovmsgbox(Map param,String urlHead,String client_id,String client_secret,String igiHead,HttpServletRequest request) throws Exception {
        Map rsMap = new HashMap();
        int returnCode = 0;
        String returnMsg = "";

        String code = (String)param.get("code");
        String userName = (String)param.get("userName");
        String cardId = (String)param.get("cardId");
        String phone = (String)param.get("phone");

        logger.info("openSaveGovmsgbox param in:code："+code+" userName："+userName+" cardId："+cardId+" phone："+phone);

        this.isNull("code",code);
        this.isNull("userName",userName);
        this.isNull("cardId",cardId);
        this.isNull("phone",phone);

        Map userInfoJson = getUserInfoByCode(param,urlHead,client_id,client_secret);
        String userNameJsonStr = (String)userInfoJson.get("userName");
        String userIdcard = (String)userInfoJson.get("userIdcard");
        String userMobile = (String)userInfoJson.get("userMobile");

        if(!userName.equals(userNameJsonStr)){
            returnCode = 1001;
            returnMsg = "姓名不一致";
        }
        if(!cardId.equals(userIdcard)){
            returnCode = 1002;
            returnMsg = "身份证号不一致";
        }
        if(!phone.equals(userMobile)){
            returnCode = 1003;
            returnMsg = "手机号不一致";
        }

        if(returnCode == 0){
            Map headerMap = getHeaderMap(request);
            String url = igiHead +"/IGI/nbhd/openGovmsgbox.do?method=openSaveGovmsgbox";
            logger.info("openSaveGovmsgbox doPostMap:"+url);
            String strResult = HttpInterface.doPostJson(url,headerMap,param);
            logger.info("openSaveGovmsgbox visit igi strResult:"+strResult);

            JSONObject jobj =JSONObject.parseObject(strResult);
            int statusCode = jobj.getIntValue("statusCode");
            String message = jobj.getString("message");
            Map datas = (Map)jobj.getJSONObject("datas");

            rsMap.put("statusCode",statusCode);
            rsMap.put("message",message);
            rsMap.put("datas",datas);
            return rsMap;
        }else{
            rsMap.put("statusCode",returnCode);
            rsMap.put("message",returnMsg);
            return rsMap;
        }

    }

    public Map getHeaderMap(HttpServletRequest request){
        Map headerMap = new HashMap();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            logger.info("all request header:"+headerName + ": " + headerValue);
            String s = "Cookie,cookie,x-real-ip,host,x-forwarded-for,pragma,sec-ch-ua,accept,x-requested-with,origin,sec-fetch-site,sec-fetch-mode,sec-fetch-dest,user-agent,sec-ch-ua-platform,referer,accept-language";
            String[] arr = s.split(",");
            for(String c:arr){
                if(c.equals(headerName)){
                    logger.info("add request header:"+headerName + ": " + headerValue);
                    headerMap.put(headerName,headerValue);
                }
            }


        }
        return headerMap;
    }


    public Map getUserInfoByCode(Map param,String urlHead,String client_id,String client_secret) throws Exception {
        HashMap rsMap = new HashMap();

        String code = (String)param.get("code");
        this.isNull("code",code);
        String userInfoStr = redisService.get(code);
        logger.info("openSaveGovmsgbox userInfoStr:"+userInfoStr);
        if(userInfoStr!=null){
            JSONObject jRs =JSONObject.parseObject(userInfoStr);
            return jRs;
        }

        String url = urlHead+"/access_token";
        logger.info("openSaveGovmsgbox url:"+url);

        JSONObject jobj = new JSONObject();
        jobj.put("client_id",client_id);
        jobj.put("client_secret",client_secret);
        jobj.put("code",code);
        jobj.put("scope","all");
        jobj.put("redirect_uri","");
        jobj.put("grant_type","authorization_code");

        //String strResult = HttpInterface.doPostMap(url,headMap);
        String pp = "client_id="+client_id+"&client_secret="+client_secret+"&scope=all&grant_type=authorization_code&redirect_uri=https%3A%2F%2Fwww.xining.gov.cn%2Fcs%2Findex.html&code="+code;
        Map headMap = new HashMap();
        String strResult = HttpInterface.doPostJson(url+"?"+pp,headMap,jobj);

        logger.info("openSaveGovmsgbox visit access_token strResult:"+strResult);
        JSONObject jRs =JSONObject.parseObject(strResult);
        String access_token = jRs.getString("access_token");

        //https://ip:port/am/oauth2/tokeninfo
        url = urlHead+"/tokeninfo";
        strResult = HttpInterface.sendGet(url+"?access_token="+access_token);
        logger.info("openSaveGovmsgbox visit userinfo strResult:"+strResult);
        jRs =JSONObject.parseObject(strResult);
        Map userInfoJson = (Map)jRs.getJSONObject("userInfo");
        userInfoStr = jRs.getJSONObject("userInfo").toJSONString();

        redisService.set(code,userInfoStr,expireSec);
        return userInfoJson;
    }


    /**
     * 依法公开申请
     * @param param
     * @param urlHead
     * @param client_id
     * @param client_secret
     * @param igiHead
     * @return
     * @throws Exception
     */
    public Map openPublicApplication(Map param,String urlHead,String client_id,String client_secret,String igiHead,HttpServletRequest request) throws Exception {
        Map rsMap = new HashMap();
        int returnCode = 0;
        String returnMsg = "";

        String code = (String)param.get("code");
        String username = (String)param.get("username");
        String idNumber = (String)param.get("idNumber");
        String phone = (String)param.get("phone");

        logger.info("openPublicApplication param in:code："+code+" userName："+username+" idNumber："+idNumber+" phone："+phone);

        this.isNull("code",code);
        this.isNull("username",username);
        this.isNull("idNumber",idNumber);
        this.isNull("phone",phone);

        Map userInfoJson = getUserInfoByCode(param,urlHead,client_id,client_secret);
        String userNameJsonStr = (String)userInfoJson.get("userName");
        String userIdcard = (String)userInfoJson.get("userIdcard");
        String userMobile = (String)userInfoJson.get("userMobile");

        if(!username.equals(userNameJsonStr)){
            returnCode = 1001;
            returnMsg = "姓名不一致";
        }
        if(!idNumber.equals(userIdcard)){
            returnCode = 1002;
            returnMsg = "身份证号不一致";
        }
        if(!phone.equals(userMobile)){
            returnCode = 1003;
            returnMsg = "手机号不一致";
        }

        if(returnCode == 0){
            Map headerMap = getHeaderMap(request);
            String url = igiHead = "/IGI/openPublicApplication/save";
            String strResult = HttpInterface.doPostJson(url,headerMap,param);
            logger.info("openPublicApplication visit igi strResult:"+strResult);

            JSONObject jobj =JSONObject.parseObject(strResult);
            int statusCode = jobj.getIntValue("statusCode");
            String message = jobj.getString("message");
            int datas =  jobj.getIntValue("datas");

            rsMap.put("statusCode",statusCode);
            rsMap.put("message",message);
            rsMap.put("datas",datas);
            return rsMap;
        }else{
            rsMap.put("statusCode",returnCode);
            rsMap.put("message",returnMsg);
            return rsMap;
        }

    }
}

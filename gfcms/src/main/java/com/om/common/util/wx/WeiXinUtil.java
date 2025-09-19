package com.om.common.util.wx;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;




/**
 * 公众平台通用接口工具类
 *
 * @author liuxj
 * @date 2013-08-09
 */
public class WeiXinUtil {
    protected static Logger logger = LoggerFactory.getLogger(WeiXinUtil.class);
    private static Map<String,AccessToken> tokenMap = new HashMap<String,AccessToken>();

    // 获取access_token的接口地址（GET） 限200（次/天）
    public final static String access_token_url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    // 菜单创建（POST） 限100（次/天）
    public static String menu_create_url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    // 菜单创建（POST） 限100（次/天）
    public static String menu_query_url = "https://api.weixin.qq.com/cgi-bin/get_current_selfmenu_info?access_token=ACCESS_TOKEN";


    //上传临时图片资源
    public static String res_upload_temp_url = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";

    //上传永久图片资源
    public static String res_upload_url = "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=ACCESS_TOKEN&type=TYPE";
   // public static String res_upload_url = "https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=ACCESS_TOKEN";

    //获取永久素材
    public static String res_get_material_url = "https://api.weixin.qq.com/cgi-bin/material/get_material?access_token=ACCESS_TOKEN";

    //删除永久素材
    public static String res_del_material_url = "https://api.weixin.qq.com/cgi-bin/material/del_material?access_token=ACCESS_TOKEN";
    //获取素材总数
    public static String res_getCount_material_url = "https://api.weixin.qq.com/cgi-bin/material/get_materialcount?access_token=ACCESS_TOKEN";
    //获取素材列表
    public static String res_getlist_material_url = "https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token=ACCESS_TOKEN";

    //上传图文新闻
    public static String pic_news_upload_url = "https://api.weixin.qq.com/cgi-bin/media/uploadnews?access_token=ACCESS_TOKEN";

    //获取公众号已创建的标签
    public static String query_tag_url = "https://api.weixin.qq.com/cgi-bin/tags/get?access_token=ACCESS_TOKEN";

    //按标签群发消息
    public static String send_all_url = "https://api.weixin.qq.com/cgi-bin/message/mass/sendall?access_token=ACCESS_TOKEN";

    //新建草稿
    public static String draft_add_url = "https://api.weixin.qq.com/cgi-bin/draft/add?access_token=ACCESS_TOKEN";
    //获取草稿
    public static String draft_getDetail_url = "https://api.weixin.qq.com/cgi-bin/draft/get?access_token=ACCESS_TOKEN";
    //删除草稿
    public static String draft_del_url = "https://api.weixin.qq.com/cgi-bin/draft/delete?access_token=ACCESS_TOKEN";
    //修改草稿
    public static String draft_update_url = "https://api.weixin.qq.com/cgi-bin/draft/update?access_token=ACCESS_TOKEN";
    //获取草稿总数
    public static String draft_count_url = "https://api.weixin.qq.com/cgi-bin/draft/count?access_token=ACCESS_TOKEN";
    //获取草稿列表
    public static String draft_list_url = "https://api.weixin.qq.com/cgi-bin/draft/batchget?access_token=ACCESS_TOKEN";

    //发布
    public static String publish_url = "https://api.weixin.qq.com/cgi-bin/freepublish/submit?access_token=ACCESS_TOKEN";

    //删除发布
    public static String publish_del_url = "https://api.weixin.qq.com/cgi-bin/freepublish/delete?access_token=ACCESS_TOKEN";

    //获取成功发布列表
    public static String publish_list_url = "https://api.weixin.qq.com/cgi-bin/freepublish/batchget?access_token=ACCESS_TOKEN";

    //用户分析_获取用户增减数据
    public static String datacube_getusersummary_url = "https://api.weixin.qq.com/datacube/getusersummary?access_token=ACCESS_TOKEN";
    //用户分析_获取累计用户数据
    public static String datacube_getusercumulate_url = "https://api.weixin.qq.com/datacube/getusercumulate?access_token=ACCESS_TOKEN";

    //图文分析_获取图文群发每日数据
    public static String datacube_getarticlesummary_url = "https://api.weixin.qq.com/datacube/getarticlesummary?access_token=ACCESS_TOKEN";
    //图文分析_获取图文群发总数据
    public static String datacube_getarticletotal_url = "https://api.weixin.qq.com/datacube/getarticletotal?access_token=ACCESS_TOKEN";
    //图文分析_获取图文统计数据
    public static String datacube_getuserread_url = "https://api.weixin.qq.com/datacube/getuserread?access_token=ACCESS_TOKEN";

    //图文分析_获取图文统计分时数据
    public static String datacube_getuserreadhour_url = "https://api.weixin.qq.com/datacube/getuserreadhour?access_token=ACCESS_TOKEN";
    //图文分析_获取图文分享转发数据
    public static String datacube_getusershare_url = "https://api.weixin.qq.com/datacube/getusershare?access_token=ACCESS_TOKEN";
    //图文分析_获取图文分享转发分时数据
    public static String datacube_getusersharehour_url = "https://api.weixin.qq.com/datacube/getusersharehour?access_token=ACCESS_TOKEN";






    /**
     * 获取access_token
     *
     * @param appid 凭证
     * @param appsecret 密钥
     * @return
     */
    private static AccessToken getAccessToken(String appid, String appsecret) {
        AccessToken accessToken = tokenMap.get(appid);
        if(accessToken!=null){
            long expireTime = accessToken.getExpireTime();
            if(System.currentTimeMillis() < expireTime) {
                return accessToken;
            }
        }

        String requestUrl = access_token_url.replace("APPID", appid).replace("APPSECRET", appsecret);
        JSONObject jsonObject = httpRequest(requestUrl, "GET", null);
        // 如果请求成功
        if (null != jsonObject) {
            try {
                accessToken = new AccessToken();
                accessToken.setToken(jsonObject.getString("access_token"));
                accessToken.setExpiresIn(jsonObject.getIntValue("expires_in"));
                tokenMap.put(appid,accessToken);
            } catch (Exception e) {
                accessToken = null;
                // 获取token失败
                logger.error("获取token失败 errcode:{"+jsonObject.getIntValue("errcode")+"} errmsg:{"+jsonObject.getString("errmsg")+"}");
            }
        }
        return accessToken;
    }


    /**
     * 基础功能————创建菜单
     *
     * @param appid 凭证
     * @param appsecret 密钥
     * @return 0表示成功，其他值表示失败
     */
    public static int createMenu(String appid, String appsecret,String jsonMenu) {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        int result = createMenuCore(jsonMenu, token) ;
        return result;
    }


    /**
     * 创建菜单
     *
     * @param jsonMenu 菜单实例
     * @param accessToken 有效的access_token
     * @return 0表示成功，其他值表示失败
     */
    private static int createMenuCore(String jsonMenu, String accessToken) {
        int result = 0;
        // 拼装创建菜单的url
        String url = menu_create_url.replace("ACCESS_TOKEN", accessToken);
        // 将菜单对象转换成json字符串
        // 调用接口创建菜单
        JSONObject jsonObject = httpRequest(url, "POST", jsonMenu);

        if (null != jsonObject) {
            if (0 != jsonObject.getIntValue("errcode")) {
                result = jsonObject.getIntValue("errcode");
                logger.error("创建菜单失败 errcode:{"+jsonObject.getIntValue("errcode")+"} errmsg:{"+jsonObject.getString("errmsg")+"}");
            }
        }

        return result;
    }

    /**
     * 查询菜单
     */
    public static Map queryMenuConfig(String appid, String appsecret) {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        String url = menu_query_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "GET", null);
        return (Map)jsonObject;
    }


    /**
     * 资源管理————上传永久素材资源
     * @return 0表示成功，其他值表示失败
     */
    public static Map uploadRes(String appid, String appsecret,String type,File file,MultipartFile mf) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        if(type == null){
            type = "thumb";
        }
        logger.debug("file:"+file+" mf:"+mf+" token:"+token+ " type:"+type);
        // 拼装创建菜单的url
        String url = res_upload_url.replace("ACCESS_TOKEN", token).replace("TYPE",type);
        // 将菜单对象转换成json字符串
        // 调用接口创建菜单
        JSONObject jsonObject = null;
        if (file != null) {
            jsonObject = WxImageUpLoad.uploadFile(url, file, mf);
        }
        if (mf != null) {
            jsonObject = WxImageUpLoad.uploadFile(url, null, mf);
        }
        logger.debug("back uploadRes:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }
    /**
     * 资源管理————获取永久素材资源
     * @return 0表示成功，其他值表示失败
     */
    public static Map getResDetail(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getResDetail:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = res_get_material_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getResDetail:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }
    /**
     * 资源管理————删除永久素材资源
     * @return 0表示成功，其他值表示失败
     */
    public static Map delResDetail(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  delResDetail:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = res_del_material_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back delResDetail:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 资源管理————获取素材总数
     * @return 0表示成功，其他值表示失败
     */
    public static Map getResCount(String appid, String appsecret) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getResCount:token"+token);
        // 拼装创建菜单的url
        String url = res_getCount_material_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", null);
        logger.debug("back getResCount:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }
    /**
     * 资源管理————获取素材列表
     * @return 0表示成功，其他值表示失败
     */
    public static Map getResList(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getResList:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = res_getlist_material_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getResCount:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }


    /**
     * 稿件管理————新建草稿
     */
    public static JSONObject draftAdd (String appid, String appsecret,String jsonContent) {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  draftAdd:token"+token+" jsonContent:"+jsonContent);
        String url = draft_add_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back draftAdd:"+jsonObject);
        return jsonObject;
    }
    /**
     * 稿件管理————获取草稿
     */
    public static JSONObject getdraftDetail (String appid, String appsecret,String jsonContent) {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getdraftDetail:token"+token+" jsonContent:"+jsonContent);
        String url = draft_getDetail_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getdraftDetail:"+jsonObject);
        return jsonObject;
    }
    /**
     * 稿件管理————删除草稿
     */
    public static JSONObject delDraft (String appid, String appsecret,String jsonContent) {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  delDraft:token"+token+" jsonContent:"+jsonContent);
        String url = draft_del_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back delDraft:"+jsonObject);
        return jsonObject;
    }
    /**
     * 稿件管理————修改草稿
     */
    public static JSONObject updateDraft (String appid, String appsecret,String jsonContent) {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  updateDraft:token"+token+" jsonContent:"+jsonContent);
        String url = draft_update_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back updateDraft:"+jsonObject);
        return jsonObject;
    }

    /**
     * 稿件管理————获取草稿总数
     */
    public static Map getDraftCount(String appid, String appsecret) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getDraftCount:token"+token );
        // 拼装创建菜单的url
        String url = draft_count_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", null);
        logger.debug("back getDraftCount:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }
    /**
     * 稿件管理————获取草稿列表
     */
    public static Map getDraftList(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getDraftList:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = draft_list_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getDraftList:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 发布管理————发布
     */
    public static Map newPublish(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  newPublish:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = publish_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back newsPublish:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }
    /**
     * 发布管理————删除发布
     */
    public static Map delPublish(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  delPublish:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = publish_del_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back delPublish:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 发布管理————获取成功发布列表
     */
    public static Map listPublish(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  listPublish:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = publish_list_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back listPublish:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 用户分析_获取用户增减数据
     */
    public static Map getusersummary(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getusersummary:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = datacube_getusersummary_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getusersummary:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }
    /**
     * 用户分析_获取累计用户数据
     */
    public static Map getusercumulate(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getusercumulate:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = datacube_getusercumulate_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getusercumulate:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 图文分析_获取图文群发每日数据
     */
    public static Map getarticlesummary(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getarticlesummary:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = datacube_getarticlesummary_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getarticlesummary:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 图文分析_获取图文群发总数据
     */
    public static Map getarticletotal(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getarticletotal:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = datacube_getarticletotal_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getarticletotal:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 图文分析_获取图文统计数据
     */
    public static Map getuserread(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getuserread:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = datacube_getuserread_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getuserread:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 图文分析_获取图文统计分时数据
     */
    public static Map getuserreadhour(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getuserreadhour:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = datacube_getuserreadhour_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getuserreadhour:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

    /**
     * 图文分析_获取图文分享转发数据
     */
    public static Map getusershare(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getusershare:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = datacube_getusershare_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getusershare:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }
    /**
     * 图文分析_获取图文分享转发分时数据
     */
    public static Map getusersharehour(String appid, String appsecret,String jsonContent) throws Exception {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        logger.debug("enter  getusersharehour:token"+token+" jsonContent:"+jsonContent);
        // 拼装创建菜单的url
        String url = datacube_getusersharehour_url.replace("ACCESS_TOKEN", token);
        JSONObject jsonObject = httpRequest(url, "POST", jsonContent);
        logger.debug("back getusersharehour:"+jsonObject.toJSONString());
        return (Map)jsonObject;
    }

















    /**
     * 上传临时资源
     * @return 0表示成功，其他值表示失败
     */
    public static String uploadResTemp(String appid, String appsecret,String type,File file,MultipartFile mf) {
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        if(type == null){
            type = "thumb";
        }
        logger.debug("file:"+file+" mf:"+mf+" token:"+token+ " type:"+type);
        int result = 0;
        // 拼装创建菜单的url
        String url = res_upload_temp_url.replace("ACCESS_TOKEN", token).replace("TYPE",type);

        JSONObject jsonObject = null;
        if(file !=null ){
            jsonObject = httpRequestForm(url, file);
        }
        if(mf !=null ){
            jsonObject = httpRequestForm(url, mf);
        }
        logger.debug("file:"+jsonObject);
        if(jsonObject!=null){
            logger.debug("file:"+jsonObject.toJSONString());//file:{"item":[],"thumb_media_id":"1kj-UYFMeeljICEPMn-XfankWR6GPM2y8aOd5QkHnbT9o30aUwtx9PrEVJSj6CfF","created_at":1741601631,"type":"thumb"}
        }
        //{"type":"TYPE","media_id":"MEDIA_ID","created_at":123456789}
        String media_id = null;
        if (null != jsonObject) {
            media_id = jsonObject.getString("thumb_media_id");

        }
        return media_id;
    }





    /**
     * 上传图文素材
     */
    public static String uploadPicNews (String appid, String appsecret,String jsonMenu) {
        logger.debug("enter uploadPicNews:"+ jsonMenu);
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        int result = 0;


        String url = pic_news_upload_url.replace("ACCESS_TOKEN", token);

        JSONObject jsonObject = httpRequest(url, "POST", jsonMenu);

        logger.debug("uploadPicNews:"+jsonObject);
        if(jsonObject!=null){
            logger.debug("uploadPicNews:"+jsonObject.toJSONString());
        }
        String media_id = null;
        if (null != jsonObject) {
            media_id = jsonObject.getString("media_id");

        }
        return media_id;

    }

    /**
     * 获取标签列表
     */
    public static JSONObject queryTagList (String appid, String appsecret) {
        logger.debug("enter queryTagList:" );
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        int result = 0;

        String url = query_tag_url.replace("ACCESS_TOKEN", token);

        JSONObject jsonObject = httpRequest(url, "GET", null);
        logger.debug("queryTagList:"+jsonObject);
        if(jsonObject!=null){
            logger.debug("queryTagList:"+jsonObject.toJSONString());
        }
        return jsonObject;
    }

    /**
     * 按标签群发短信
     */
    public static JSONObject sendAllMsg (String appid, String appsecret,String jsonMenu) {
        logger.debug("enter sendAllMsg:" );
        AccessToken accessToken = getAccessToken(appid, appsecret);
        String token = accessToken.getToken();
        int result = 0;
        String url = send_all_url.replace("ACCESS_TOKEN", token);
        // 按标签群发短信
        JSONObject jsonObject = httpRequest(url, "POST", jsonMenu);
        logger.debug("sendAllMsg:"+jsonObject);
        return jsonObject;
    }


    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl 请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr 提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();

            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            int responseCode = httpUrlConn.getResponseCode();
            logger.debug("responseCode:"+responseCode+" str:"+buffer.toString());
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl 请求地址
     * @param file 提交的文件
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequestForm(String requestUrl, File file) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);





            httpUrlConn.setRequestMethod("POST");
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            httpUrlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpUrlConn.setDoOutput(true);

            // 构建请求体
            try (OutputStream output = httpUrlConn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                // 文件部分
                //File file = new File("path/to/file.jpg");
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: ").append(HttpsURLConnection.guessContentTypeFromName(file.getName())).append("\r\n\r\n");
                writer.append("filelength: ").append(file.length()+"").append("\r\n\r\n");
                writer.flush();


                try (FileInputStream input = new FileInputStream(file)) {
                    byte[] bf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = input.read(bf)) != -1) {
                        output.write(bf, 0, bytesRead);
                    }
                    output.flush();
                }

                // 其他参数（可选）
                writer.append("\r\n--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"param1\"\r\n\r\n");
                writer.append("value1\r\n");

                // 结束边界
                writer.append("--").append(boundary).append("--\r\n");
            }

            // 将返回的输入流转换成字符串
            int responseCode = httpUrlConn.getResponseCode();
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 多态方法，同httpRequestForm
     *
     * @param requestUrl 请求地址
     * @param mf 提交的文件
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    public static JSONObject httpRequestForm(String requestUrl, MultipartFile mf) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);

            httpUrlConn.setRequestMethod("POST");
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            httpUrlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpUrlConn.setDoOutput(true);

            // 构建请求体
            try (OutputStream output = httpUrlConn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {

                // 文件部分
                //File file = new File("path/to/file.jpg");
                logger.debug("========:"+mf.getOriginalFilename()+"   Content-Type:"+HttpsURLConnection.guessContentTypeFromName(mf.getOriginalFilename())+" size:"+mf.getSize());
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(mf.getOriginalFilename()).append("\"\r\n");
                writer.append("Content-Type: ").append(HttpsURLConnection.guessContentTypeFromName(mf.getOriginalFilename())).append("\r\n\r\n");
                writer.append("filelength: ").append(mf.getSize()+"").append("\r\n\r\n");
                writer.flush();


                try (InputStream input = mf.getInputStream()) {
                    byte[] bf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = input.read(bf)) != -1) {
                        output.write(bf, 0, bytesRead);
                    }
                    output.flush();
                }

                // 其他参数（可选）
                writer.append("\r\n--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"param1\"\r\n\r\n");
                writer.append("value1\r\n");

                // 结束边界
                writer.append("--").append(boundary).append("--\r\n");
            }

            // 将返回的输入流转换成字符串
            int responseCode = httpUrlConn.getResponseCode();
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            logger.debug("responseCode:"+responseCode+" str:"+buffer.toString());
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.parseObject(buffer.toString());
        } catch (ConnectException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }







    public static String getOpenId(String uCode){
        String openId = "";
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=wxcf6388437ddefa80&secret=9d92eddeaf05df6248e25af4557cb014&code=" + uCode +
                "&grant_type=authorization_code";
        JSONObject jsonObject = WeiXinUtil.httpRequest(url, "GET", null);
        if (null != jsonObject) {
            try {
                openId = jsonObject.getString("openid");
                System.out.println(openId);
            } catch (Exception e) {
                // 获取token失败
                System.out.println(e.toString());
            }
        }
        return openId;
    }




    public static void sendCustomerMessage(String accessToken,String jsonString) {
        String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=ACCESS_TOKEN";
        // 拼装创建菜单的url
        url = url.replace("ACCESS_TOKEN", accessToken);
        // 调用接口创建菜单
        JSONObject jsonObject = httpRequest(url, "POST", jsonString);

        if (null != jsonObject) {
            if (0 != jsonObject.getIntValue("errcode")) {

                System.out.println("发送消息失败 errcode:{} errmsg:{}"+jsonObject.getIntValue("errcode")+jsonObject.getString("errmsg"));
            }
        }

    }
}
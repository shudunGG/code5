package com.om.module.service.busi;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.bo.base.FtpPathInfo;
import com.om.common.cache.Dict;
import com.om.common.util.DateUtil;
import com.om.common.util.HttpInterface;
import com.om.common.util.ObjectTools;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import com.om.module.service.label.ABaseLabel;
import com.om.module.service.label.GfDocumentLabel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.util.*;

@Service("ErrorValidService")
public class ErrorValidService extends CommonService {


    /**
     * 全文校验服务，目前仅支持方寸，另外需要考虑一下，当内容过长时，最好可以支持分段，多线程的方式发出请求
     * @param param
     * @param errorValidScp 哪个厂家的服务
     * @param errorValidScpParam 业务之类的参数
     * @return
     * @throws Exception
     */
    public HashMap validContent(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String TEXT = (String)param.get("TEXT");
        this.isNull("TEXT",TEXT);
        HttpInterface http = new HttpInterface();
        String url = "";
        String result = "";
        if("fangcun".equals(errorValidScp)){
            url = Dict.FangCun.valid_url;
            JSONObject jobj =JSONObject.parseObject(errorValidScpParam);

            if(TEXT!=null){
                logger.debug("TEXT encode:"+TEXT);
                TEXT =  URLDecoder.decode(TEXT,"UTF-8");
                logger.debug("TEXT decode:"+TEXT);
                String CONTENT = ObjectTools.getStrFromHtml(TEXT);
                logger.debug("CONTENT decode:"+CONTENT);
                jobj.put("text",CONTENT);
            }
            logger.info("request input param:"+jobj.toJSONString());
            result = http.doPostJsonFangCun(url, jobj);
            logger.info("validContent result:"+result);
            try{
                JSONObject rsObj =JSONObject.parseObject(result);
                //{"ret_code":1,"ret_msg":"内部错误","ret_ip":"172.17.0.4" }
                rsMap.put(Const.RESP_CODE,Const.ErrCode);
                rsMap.put(Const.RESP_MSG,rsObj.get("ret_msg"));
                return rsMap;
            }catch (Exception e){
                 //[{"subClassCode":"E0001","offset":0,"subClass":"领导人错误","error_word":"中华人民共和国主席习进平","right_word":"","errorType":"政治错误","confidence":1,"errorCode":"2000","type":"领导人职务不匹配，现任中华人民共和国主席：[习近平]。","right_words":[" "],"context":"中华人民共和国主席习进平","model":"politics","org_error_word":"中华人民共和国主席习进平","org_offset":0}]
            }
            List<Map> rsList = new ArrayList<>();
            JSONArray rsObjArr =JSONArray.parseArray(result);
            for(int i=0;i<rsObjArr.size();i++){
                JSONObject rsObj = rsObjArr.getJSONObject(i);
                Map map = new HashMap();
                map.put("subClass",rsObj.get("subClass"));//错误细分分类描述
                map.put("error_word",rsObj.get("error_word"));//String	错误的词
                map.put("type",rsObj.get("type"));//错误类型分组描述
                map.put("offset",rsObj.get("offset"));
                map.put("confidence",rsObj.get("confidence"));
                rsList.add(map);
            }
            rsMap.put(Const.RESP_CODE,Const.SuccCode);
            rsMap.put(Const.RESP_MSG,Const.SUCC);
            rsMap.put(Const.RESP_DATA,rsList);
        }
        return rsMap;
    }

    /**
     * 错词库的添加功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap errWordAdd(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String wrong_word = (String)param.get("wrong_word");
        String right_word = (String)param.get("right_word");
        String context = (String)param.get("context");
        String remark = (String)param.get("remark");
        this.isNull("wrong_word",wrong_word);
        this.isNull("right_word",right_word);
        this.isNull("context",context);
        return this.commonWordOp(Dict.FangCun.errWordAdd_url,param,errorValidScp,errorValidScpParam);
    }


    /**
     * 错词库的修改功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap errWordModify(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String wrong_word = (String)param.get("wrong_word");
        String right_word = (String)param.get("right_word");
        String context = (String)param.get("context");
        String remark = (String)param.get("remark");
        this.isNull("wrong_word",wrong_word);
        this.isNull("right_word",right_word);
        this.isNull("context",context);

        String new_wrong_word = (String)param.get("new_wrong_word");
        String new_right_word = (String)param.get("new_right_word");
        String new_context = (String)param.get("new_context");
        String new_remark = (String)param.get("new_remark");
        this.isNull("new_wrong_word",new_wrong_word);
        this.isNull("new_right_word",new_right_word);
        this.isNull("new_context",new_context);
        return this.commonWordOp(Dict.FangCun.errWordModify_url,param,errorValidScp,errorValidScpParam);
    }


    /**
     * 错词库的删除功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap errWordDel(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String wrong_word = (String)param.get("wrong_word");
        String right_word = (String)param.get("right_word");
        String context = (String)param.get("context");
        String remark = (String)param.get("remark");
        this.isNull("wrong_word",wrong_word);
        this.isNull("right_word",right_word);
        this.isNull("context",context);
        return this.commonWordOp(Dict.FangCun.errWordDel_url,param,errorValidScp,errorValidScpParam);
    }

    /**
     * 错词库的查询功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap errWordQuery(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String word = (String)param.get("word");
        return commonWordQuery(Dict.FangCun.errWordQuery_url, param, errorValidScp,errorValidScpParam);
    }

    /**
     * 对错词，正词，敏感词的增，删除，改功能
     * @param url 接口地址
     * @param param 接口的业务参数
     * @param scp 厂家
     * @param scpParam 厂家必须传的参数
     * @return
     * @throws Exception
     */
    public HashMap commonWordOp(String url,Map  param, String scp,String scpParam) throws Exception {
        HashMap rsMap = new HashMap();
        HttpInterface http = new HttpInterface();
        String result = "";
        if("fangcun".equals(scp)){
            JSONObject jobj =JSONObject.parseObject(scpParam);
            String ukey = jobj.getString("ukey");
            String userid = jobj.getString("userid");
            jobj.put("user_code", userid);
            jobj.put("ukey", ObjectTools.md5(ukey));
            jobj.putAll(param);

            result = http.httpGet(url, ObjectTools.switchUrlParam(jobj));
            logger.info("http reponse result:"+result);
            try{
                JSONObject rsObj =JSONObject.parseObject(result);
                String return_code = rsObj.getString("return_code");
                String return_msg = rsObj.getString("return_msg");
                String msg = rsObj.getString("msg");
                if("0".contentEquals(return_code)){
                    rsMap.put(Const.RESP_CODE,Const.SuccCode);
                    rsMap.put(Const.RESP_MSG,Const.SUCC);
                }else{
                    rsMap.put(Const.RESP_CODE,Const.ErrCode);
                    rsMap.put(Const.RESP_MSG,return_msg+"/"+msg);
                }
                return rsMap;
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                rsMap.put(Const.RESP_CODE,Const.ErrCode);
                rsMap.put(Const.RESP_MSG,this.getStackTrace(e));
            }
        }
        return rsMap;
    }


    public HashMap commonWordQuery(String url,Map  param, String scp,String scpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String page_no = (String)param.get("page_no");
        String pageSize = (String)param.get("pageSize");
        String word = (String)param.get("word");
        this.isNull("page_no",page_no);
        if(pageSize == null || "".equals(pageSize)){
            pageSize="10";
        }
        if(word== null){
            word = "";
        }
        param.put("pageSize",pageSize);
        param.put("word",word);
        HttpInterface http = new HttpInterface();
        String result = "";
        if("fangcun".equals(scp)){
            url = Dict.FangCun.errWordQuery_url;
            JSONObject jobj =JSONObject.parseObject(scpParam);
            String ukey = jobj.getString("ukey");
            String userid = jobj.getString("userid");
            jobj.put("user_code", userid);
            jobj.put("ukey", ObjectTools.md5(ukey));
            jobj.putAll(param);
            result = http.httpGet(url, ObjectTools.switchUrlParam(jobj));
            logger.info("validContent result:"+result);
            try{
                List<Map> rsList = new ArrayList<>();
                JSONArray rsObjArr =JSONArray.parseArray(result);
                for(int i=0;i<rsObjArr.size();i++){
                    JSONObject rsObj = rsObjArr.getJSONObject(i);
                    Map map = (Map)rsObj;
                    rsList.add(map);
                }
                rsMap.put(Const.RESP_CODE,Const.SuccCode);
                rsMap.put(Const.RESP_MSG,Const.SUCC);
                rsMap.put(Const.RESP_DATA,rsList);
                return rsMap;
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                rsMap.put(Const.RESP_CODE,Const.ErrCode);
                rsMap.put(Const.RESP_MSG,this.getStackTrace(e));
            }
        }
        return rsMap;
    }

    /**
     * 正词库的添加功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap rightWordAdd(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String right_word = (String)param.get("right_word");
        String context = (String)param.get("context");
        this.isNull("right_word",right_word);
        this.isNull("context",context);
        return this.commonWordOp(Dict.FangCun.rightWordAdd_url,param,errorValidScp,errorValidScpParam);
    }


    /**
     * 正词库的修改功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap rightWordModify(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String right_word = (String)param.get("right_word");
        String context = (String)param.get("context");
        this.isNull("right_word",right_word);
        this.isNull("context",context);

        String new_right_word = (String)param.get("new_right_word");
        String new_context = (String)param.get("new_context");
        this.isNull("new_right_word",new_right_word);
        this.isNull("new_context",new_context);
        return this.commonWordOp(Dict.FangCun.rightWordModify_url,param,errorValidScp,errorValidScpParam);

    }


    /**
     * 正词库的删除功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap rightWordDel(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String right_word = (String)param.get("right_word");
        String context = (String)param.get("context");
        this.isNull("right_word",right_word);
        this.isNull("context",context);
        return this.commonWordOp(Dict.FangCun.rightWordDel_url,param,errorValidScp,errorValidScpParam);
    }

    /**
     * 正词库的查询功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap rightWordQuery(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String word = (String)param.get("word");
        return commonWordQuery(Dict.FangCun.rightWordQuery_url, param, errorValidScp,errorValidScpParam);
    }


    /**
     * 敏感词库的添加功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap specialWordAdd(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String special_word = (String)param.get("special_word");
        String context = (String)param.get("context");
        String remark = (String)param.get("remark");
        this.isNull("special_word",special_word);
        this.isNull("context",context);
        return this.commonWordOp(Dict.FangCun.specialWordAdd_url,param,errorValidScp,errorValidScpParam);
    }


    /**
     * 敏感词库的修改功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap specialWordModify(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String special_word = (String)param.get("special_word");
        String context = (String)param.get("context");
        String remark = (String)param.get("remark");
        this.isNull("special_word",special_word);
        this.isNull("context",context);

        String new_special_word = (String)param.get("new_special_word");
        String new_context = (String)param.get("new_context");
        String new_remark = (String)param.get("new_remark");
        this.isNull("new_special_word",new_special_word);
        this.isNull("new_context",new_context);
        return this.commonWordOp(Dict.FangCun.specialWordModify_url,param,errorValidScp,errorValidScpParam);

    }


    /**
     * 敏感词库的删除功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap specialWordDel(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String special_word = (String)param.get("special_word");
        String context = (String)param.get("context");
        this.isNull("special_word",special_word);
        this.isNull("context",context);
        return this.commonWordOp(Dict.FangCun.specialWordDel_url,param,errorValidScp,errorValidScpParam);
    }

    /**
     * 敏感词库的查询功能
     * @param param
     * @param errorValidScp
     * @param errorValidScpParam
     * @return
     * @throws Exception
     */
    public HashMap specialWordQuery(Map  param, String errorValidScp,String errorValidScpParam) throws Exception {
        HashMap rsMap = new HashMap();
        String word = (String)param.get("word");
        return commonWordQuery(Dict.FangCun.specialWordQuery_url, param, errorValidScp,errorValidScpParam);
    }

}

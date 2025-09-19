package com.om.test;

import com.alibaba.fastjson.JSONObject;
import com.om.common.util.HttpInterface;
import com.om.common.util.ObjectTools;

public class TestFangCun {
    public static void main(String[] args) {


        try {
            TestFangCun t = new TestFangCun();
//            String reuslt  = t.validSimpleSentence();
//            System.out.println(reuslt);

           // String reuslt  = t.validWholeChapter();
            String url = "http://crm.ifuncun.cn:8080/funcun/uploadDict_wrongWords?user_code=hdwl_test&right_word=抗疫&context=做好防疫抗议工作&ukey=hdwl_test123456&wrong_word=抗议";

            HttpInterface http = new HttpInterface();
            String result = http.httpGet(url);

            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String validSimpleSentence( )throws Exception{
        String url = "http://api.ifuncun.cn:8081/do";
        HttpInterface http = new HttpInterface();
        JSONObject jobj = new JSONObject();
        jobj.put("method", "correct");
        jobj.put("userid", "hdwl_test");
        jobj.put("ukey", "hdwl_test123456");
        jobj.put("text", "中华人民共和国主席习进平");
        String result = http.doPostJsonFangCun(url, jobj);
        return result;
    }

    private String validWholeChapter( )throws Exception{
        String url = "http://api.ifuncun.cn:8081/v1/proofread";
        HttpInterface http = new HttpInterface();
        JSONObject jobj = new JSONObject();
//String p = JSONObject.toJSONString(rs);
        jobj.put("userid", "hdwl_test");
        jobj.put("ukey", "hdwl_test123456");
        jobj.put("text", "中华人民共和国主席习进平");
        String result = http.doPostJsonFangCun(url, jobj);
        return result;
    }

}

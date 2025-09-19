package com.om.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.om.bo.busi.MyResFile;
import com.om.common.util.DateUtil;
import com.om.common.util.ObjectTools;
import com.om.common.util.UZipFile;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestMain {
    public static void main(String[] args) throws ParseException {
        System.out.println(ObjectTools.md5("123456"));//e10adc3949ba59abbe56e057f20f883e
        System.out.println(ObjectTools.md5("999999"));//e10adc3949ba59abbe56e057f20f883e
        System.out.println(ObjectTools.md5("888888"));//e10adc3949ba59abbe56e057f20f883e

        TestMain t=  new TestMain();
        String jsonMenu="[{\"name\":\"今日歌曲\",\"type\":\"click\",\"key\":\"V1001_TODAY_MUSIC\"},{\"name\":\"菜单\",\"sub_button\":{\"list\":[{\"name\":\"搜索1\",\"type\":\"view\",\"url\":\"http://www.qq.com/\"},{\"name\":\"搜索2\",\"type\":\"view\",\"url\":\"http://www.baidu.com/\"}]}},{\"name\":\"测试\",\"type\":\"view\",\"url\":\"www.baidu.com\"}]";
        jsonMenu="{\"button\": "+jsonMenu+"}";

        JSONArray rsButtonJsonArr = new JSONArray();
        JSONObject json = (JSONObject) JSONObject.parse(jsonMenu);
        JSONArray buttonJson = json.getJSONArray("button");
        for(int i=0;i<buttonJson.size();i++){
            JSONObject btLevel1Json = buttonJson.getJSONObject(i);
            JSONObject sub_buttonJson = btLevel1Json.getJSONObject("sub_button");
            if(sub_buttonJson==null){
                rsButtonJsonArr.add(btLevel1Json);
            }else{
                JSONArray listJson = sub_buttonJson.getJSONArray("list");
                btLevel1Json.put("sub_button",listJson);
                rsButtonJsonArr.add(btLevel1Json);
            }
        }

        System.out.println("11111:"+rsButtonJsonArr.toJSONString());

    }

    public void replaceParam(StringBuffer xml,Map dataMap){
        int start = xml.indexOf("#{");
        int end = xml.indexOf("}#");
        if(start > -1 && end > -1){
            String col = xml.substring(start+2,end);
            Object obj = dataMap.get(col);
            String val = "";
            if(obj!=null){
                val = obj.toString();
            }
            xml.delete(start,end+2);
            xml.insert(start,val);
            replaceParam(xml,dataMap);
        }else{
            return ;
        }
    }







}

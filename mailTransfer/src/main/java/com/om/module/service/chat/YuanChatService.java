package com.om.module.service.chat;

import com.om.bo.base.QuestionType;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("YuanChatService")
public class YuanChatService extends CommonService {
    public static Map<String,List<QuestionType>> BASEMAP = new HashMap<String,List<QuestionType>>();//存放的从base查class1列表的
    public static Map<String,List<Map>> CLASS12MAP = new HashMap<String,List<Map>>();//存放的从class1查class2列表的

    public List queryGfChatStandQuestion(Map param) throws Exception {
        param.put("PROJECT","yuan");
        List list = this.baseService.getList("yuanMapper.queryGfChatStandQuestion",param);
        /**
         * base - class1
         * class1 - class2
         * 1 热门领域:教育科研,就业创业....
         * 2 全生命周期个人：出生，上学，。。。。
         */
        Map<String,List<QuestionType>> baseMap = new HashMap<String,List<QuestionType>>();//存放的从base查class1列表的
        Map<String,List<Map>> class12Map = new HashMap<String,List<Map>>();//存放的从class1查class2列表的
        Map m = null;
        for(int i=0;i<list.size();i++){
            m = (Map) list.get(i);
            String base = (String)m.get("BASE");
            String class1 = (String)m.get("CLASS_1");
            String class1Icon = (String)m.get("CLASS_1_ICON");
//            String class2 = (String)m.get("CLASS_2");
//            String question = (String)m.get("QUESTION");
            List<QuestionType> class1List = baseMap.get(base);
            if(class1List == null){
                class1List = new ArrayList<QuestionType>();
                baseMap.put(base,class1List);
            }
            QuestionType qt = new QuestionType(class1,class1Icon);
            if(!class1List.contains(qt)){//如果不包含就添加上，包含就跳过，因为会有重复的
                class1List.add(qt);
            }

            List<Map> class2List = class12Map.get(class1);
            if(class2List == null){
                class2List = new ArrayList<Map>();
                class12Map.put(class1,class2List);
            }
            if(!class2List.contains(m)){//如果不包含就添加上，包含就跳过，因为会有重复的
                class2List.add(m);
            }
            //ID,BASE,CLASS_1,CLASS_2,QUESTION,SEQ,REMARK,PROJECT
        }
        BASEMAP.clear();
        BASEMAP.putAll(baseMap);
        CLASS12MAP.clear();
        CLASS12MAP.putAll(class12Map);
        return list;
    }

    public List queryRmly() throws Exception {
        List<QuestionType>  list = BASEMAP.get("热门领域");
        return list;
    }

    public List queryPersonAllService() throws Exception {
        List<QuestionType>  list = BASEMAP.get("个人全生命周期政策服务");
        return list;
    }

    public List queryCompanyAllService() throws Exception {
        List<QuestionType>  list = BASEMAP.get("企业全生命周期政策服务");
        return list;
    }

    public List queryClass2ListByClass1(Map param) throws Exception {
        String class1 = (String)param.get("class1");
        this.isNull("class1",class1);
        List<Map>  list = CLASS12MAP.get(class1);
        return list;
    }

}

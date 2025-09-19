package com.om.module.service.label;

import com.om.module.core.base.service.BaseService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GfDocListLabel extends ABaseLabel {

    public GfDocListLabel(String xml) {
        super(xml,"GF_DOCLIST");
    }

    @Override
    public  int getPageSplitNum(BaseService baseService)throws  Exception {
        logger.info("enter documents getPageSplitNum");
        Map paramMap = new HashMap();
        StringBuffer moreSb = new StringBuffer();
        setQueryParam(baseService,paramMap,moreSb);

        List<Object> list = baseService.getList("busiMapper.queryZbBusiDocumentCount",paramMap);
        Map map = (Map)list.get(0);
        int ct = Integer.parseInt(map.get("CT").toString());
        this.totalRecord = ct;
        int size=20;
        Document document = getDocByxml(this.mylabel);
        Element root = document.getRootElement();
        Attribute PAGESIZE= root.attribute("PAGESIZE");
        if(PAGESIZE!=null){
            size = Integer.parseInt(PAGESIZE.getValue());
        }
        double rs = (double)ct / (double)size;
        int page = (int)Math.ceil(rs);
        logger.info("total ct,size:"+ct+"  "+size+"    "+page);
        if(page==0){
            page =1;
        }
        return  page;

    }

    public void setQueryParam(BaseService baseService,Map paramMap,StringBuffer moreSb) throws Exception{
        Document document = getDocByxml(this.mylabel);
                // 这两行应该是没有意义的
        String CHANNEL_PK = (String)inputMap.get("CHANNEL_PK");
        paramMap.put("CHANNEL_PK",CHANNEL_PK);
        paramMap.put("SITE_PK",inputMap.get("SITE_PK"));

        Element root = document.getRootElement();
        Attribute ID= root.attribute("ID");
        Attribute NAME= root.attribute("NAME");
        Attribute CHILDINDEX= root.attribute("CHILDINDEX");
        Attribute NUM= root.attribute("NUM");
        Attribute STARTPOS= root.attribute("STARTPOS");
        Attribute PAGESIZE= root.attribute("PAGESIZE");/////NUM? pagesize
        Attribute WHERE= root.attribute("WHERE");
        Attribute ORDER= root.attribute("ORDER");
        Attribute SELECT= root.attribute("SELECT");

        Attribute AUTOMORE= root.attribute("AUTOMORE");
        Attribute BEGINMORE= root.attribute("BEGINMORE");
        Attribute ENDMORE= root.attribute("ENDMORE");
        Attribute MORETEXT= root.attribute("MORETEXT");
        Attribute MOREEXTRA= root.attribute("MOREEXTRA");
        Attribute MORETARGET= root.attribute("MORETARGET");
        Attribute MORELINK= root.attribute("MORELINK");
        Attribute MOREFIRST= root.attribute("MOREFIRST");

        if(ID!=null){
            String id = ID.getValue();
            if("PARENT".equals(id)){
                paramMap.put("PARENT",1);
            }else if("OWNER".equals(id)){
                paramMap.put("OWNER",1);
            }else{
                paramMap.put("CHANNEL_ID",id);
            }
        }

        if(NAME != null){
            paramMap.put("CHANNEL_NAME",NAME.getValue());
        }

        if(CHILDINDEX != null){
            paramMap.put("CHILDINDEX",CHILDINDEX.getValue());
        }

        /*
        Attribute NUM= root.attribute("NUM");
        Attribute STARTPOS= root.attribute("STARTPOS");
        Attribute PAGESIZE= root.attribute("PAGESIZE");/////NUM? pagesize
        这三个涉及到分页，后续再说
         */

        int size = 20;
        if(PAGESIZE != null){
            size = Integer.parseInt(PAGESIZE.getValue());
            logger.debug("setQueryParam PAGESIZE:"+PAGESIZE.getValue());
        }
        int total = 500;
        if (NUM != null) {
            total = Integer.parseInt(NUM.getValue());
            logger.debug("setQueryParam NUM:"+NUM.getValue());
        }
        int startPos=0;
        if(STARTPOS != null){
            startPos = Integer.parseInt(STARTPOS.getValue());
            logger.debug("setQueryParam STARTPOS:"+STARTPOS.getValue());
        }
        logger.debug("setQueryParam curPage:"+curPage);
        if(this.curPage>0){
            startPos = (this.curPage-1)*size;
        }else{
            size = total;
        }
        paramMap.put("STARTPOS",startPos);
        paramMap.put("TOTAL_NUM", size);




        if(WHERE != null){
            paramMap.put("WHERE",WHERE.getValue());
        }
        if(ORDER != null){
            paramMap.put("ORDER",","+ORDER.getValue());
        }
        if(SELECT != null){
            paramMap.put("SELECT",","+SELECT.getValue());
        }


        if(AUTOMORE != null || "true".equals(AUTOMORE)){

            if(BEGINMORE != null){
                moreSb.append(BEGINMORE.getValue()).append(" ");
            }
            moreSb.append("<a ");
            if(MOREEXTRA != null){
                moreSb.append(MOREEXTRA.getValue()).append(" ");
            }
            if(MORELINK != null){
                moreSb.append("href=\"").append(MORELINK.getValue()).append("\"").append(" ");
            }else{
                moreSb.append("href=\"./\"").append(" ");
            }
            if(MORETARGET != null){
                moreSb.append("target=\"").append(MORETARGET.getValue()).append("\"").append(" ");
            }
            moreSb.append(">");
            if(MORETEXT != null){
                moreSb.append(MORETEXT.getValue());
            }else{
                moreSb.append("更多内容…");
            }
            moreSb.append("</a>");
            if(ENDMORE != null){
                moreSb.append(ENDMORE.getValue()).append(" ");
            }

            //MOREFIRST这里判断是放前面还是后面
        }

    }
    /**
     * GF_DOCLIST
     *         <GF_DOCLIST [[NAME=”栏目名称”][ID=”栏目ID”] [CHILDINDEX=”子栏目的序号”]
     * [NUM=”记录条数”] [STARTPOS=”开始位置”] [PAGESIZE=”分页参数”] [WHERE=”额外的检索条件”]
     * [ORDER=”额外的排序条件”] [AUTOMORE=”是否自动显示更多内容”]
     * [BEGINMORE=”放在更多内容前面的 HTML”] [ENDMORE=”放在更多内容后面的 HTML”]
     * [MORETEXT=”更多内容的HTML”] [MOREEXTRA=”A元素上的额外HTML”]
     * [MORETARGET=”_blank”] [ MORELINK =”more的链接地址”][ MOREFIRST =”more放到前面还是后面true/false”]
     * [SELECT=”额外的查询字段”] ]>
     * </GF_DOCLIST>
     * @return
     */
    @Override
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        Document document = getDocByxml(this.mylabel);
        Map paramMap = new HashMap();
        StringBuffer moreSb = new StringBuffer();
        setQueryParam(baseService,paramMap,moreSb);
        Element root = document.getRootElement();


        Attribute AUTOMORE= root.attribute("AUTOMORE");
        Attribute MOREFIRST= root.attribute("MOREFIRST");

        paramMap.put("FINISH_FLAG",1);//这里只取完成的
        List<Map> docList =  baseService.getList("busiMapper.queryZbBusiDocumentDef",paramMap);



        Attribute GROUP_NUM= root.attribute("GROUP_NUM");
        Attribute GROUP_MACRO_DEFINE= root.attribute("GROUP_MACRO_DEFINE");
        int groupNum = 1;
        String macroDefine = "";
        if(GROUP_NUM != null ){
            try {
                groupNum = Integer.parseInt(GROUP_NUM.getValue());
                macroDefine = GROUP_MACRO_DEFINE.getValue();
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                throw e;
            }
        }


        //现在需要对docList展示，实现对GF_DOCLIST置标内部的内容做循环，同时检测到GF_DOCUMENT后，调用置标
        String s = this.mylabel.substring(this.mylabel.indexOf(">")+1,this.mylabel.indexOf("</GF_DOCLIST>"));
        //处理子标签
        StringBuffer fullHtml = new StringBuffer();
        //判断是否需要分组
        if(groupNum>1){
            int i=0;
            for (Map map : docList) {
                StringBuffer innerXml = new StringBuffer(s);
                replaceParam(innerXml,map);
                super.switchLabel(innerXml, map, baseService, curPage, totalPage, 0);

                String innerXmlStr = innerXml.toString();
                String[] paramGroup = macroDefine.split("&");
                for(String paramGrp:paramGroup){
                    String[] valGroup = paramGrp.split("=");
                    String pp = valGroup[0];//参数
                    String vv = valGroup[1];//参数对应的值列表
                    String[] valGrp = vv.split("_");

                    //GROUP_MACRO_DEFINE="MACRO_CLASS=odd_fdd&MACRO_P=1_2"

                    if(i%groupNum  == 0){
                        innerXmlStr = innerXmlStr.replaceAll(pp,valGrp[0]);
                    }else if(i%groupNum  == 1){
                        innerXmlStr = innerXmlStr.replaceAll(pp,valGrp[1]);
                    }else if(i%groupNum  == 2){
                        innerXmlStr = innerXmlStr.replaceAll(pp,valGrp[2]);
                    }else if(i%groupNum  == 3){
                        innerXmlStr = innerXmlStr.replaceAll(pp,valGrp[3]);
                    }
                }
                innerXml = new StringBuffer(innerXmlStr);
                fullHtml.append(innerXml);
                i++;
            }
        }else {
            for (Map map : docList) {
                StringBuffer innerXml = new StringBuffer(s);
                replaceParam(innerXml,map);
                super.switchLabel(innerXml, map, baseService, curPage, totalPage, 0);
                fullHtml.append(innerXml);
            }
        }

        if(AUTOMORE != null || "true".equals(AUTOMORE)){
            if(MOREFIRST != null || "false".equals(MOREFIRST)){
                fullHtml.append(moreSb);
                return fullHtml.toString();
            }else{
                moreSb.append(fullHtml);
                return moreSb.toString();
            }
        }
        fullHtml.append("<div id='gfPageSplitPageCurPage' style='display:none'>"+this.curPage+"</div>");
        fullHtml.append("<div id='gfPageSplitPageTotal' style='display:none'>"+this.totalPage+"</div>");
        fullHtml.append("<div id='gfPageSplitPageTotalRecord' style='display:none'>"+this.totalRecord+"</div>");
        return fullHtml.toString();

    }

    /**
     * 支持将<documents></documents>里面，格式为#{}#之间的参数，替换掉
     * @param xml
     * @param dataMap
     */
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

    @Override
    public void ruleCheck(BaseService baseService)throws Exception{
        super.ruleCheck(baseService);
        Document document = getDocByxml(this.mylabel);
        Element root = document.getRootElement();
        Attribute GROUP_NUM= root.attribute("GROUP_NUM");
        Attribute GROUP_MACRO_DEFINE= root.attribute("GROUP_MACRO_DEFINE");

        int groupNum = 1;
        String macroDefine = "";
        if(GROUP_NUM != null && GROUP_MACRO_DEFINE == null
            ||
           GROUP_NUM == null && GROUP_MACRO_DEFINE != null
        ){
            throw new Exception("GROUP_NUM和GROUP_MACRO_DEFINE属性，在置标GF_DOCLIST必须成对出现");
        }

        if(GROUP_NUM != null && GROUP_MACRO_DEFINE != null ){
            try {
                groupNum = Integer.parseInt(GROUP_NUM.getValue());
                macroDefine = GROUP_MACRO_DEFINE.getValue();
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                throw e;
            }

            if(groupNum<2 || groupNum>4){
                throw new Exception("分组当前仅支持2，3，4，不支持其它分组数，当前是："+groupNum);
            }

            String[] paramGroup = macroDefine.split("&");
            for(String paramGrp:paramGroup){
                String[] valGroup = paramGrp.split("=");
                String pp = valGroup[0];//参数
                String vv = valGroup[1];//参数对应的值列表
                String[] valGrp = vv.split("_");

                if(valGrp.length != groupNum){
                    throw new Exception("分组数GROUP_NUM的参数的数量必须匹配，当前GROUP_NUM是："+groupNum+",参数个数是："+valGrp.length);
                }
            }


        }





    }

}

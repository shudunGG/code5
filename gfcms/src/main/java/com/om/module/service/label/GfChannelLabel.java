package com.om.module.service.label;

import com.om.module.core.base.service.BaseService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GfChannelLabel extends ABaseLabel {

    public GfChannelLabel(String xml) {
        super(xml,"GF_CHANNEL");
    }

    /**
     * GF_CHANNEL
     *         <GF_CHANNEL [[ID=”父栏目名”][CHANNEL_NAME="栏目名"] [CHANNELID="栏目id"][SPECIAL_CHANNEL_ID=”栏目唯一标识”] [CHILDINDEX=”子栏目序号”] [PARENTIDX=”第 index 级父栏目”] FIELD=”字段名” [isPureContent='true']
     * [NUM=”最大长度”] [DATEFORMAT=”日期格式”][CLASSNAME=”置标产生内容使用的样式”]
     * [AUTOFORMATTYPE=”格式化方式”] [AUTOLINK=”是否自动产生链接”] [EXTRA=”A元素上扩展的 HTML 内容”]
     * [TARGET=”窗口目标”]  [LINKALTTEXT=”链接提示内容”][AUTOCOLOR=”标题颜色”]
     * [ TRUNCATEDFLAG=” 栏目名称被截断后显示内容”] [LINKTEXT=”链接表面文字 ”] ><column_block> </GF_CHANNEL>
     * @return
     */
    @Override
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        Document document = getDocByxml(this.mylabel);

        Map docMap = inputMap;

        Element root = document.getRootElement();
        Attribute ID= root.attribute("ID");//PARENT  OWNER
        Attribute CHANNEL_NAME= root.attribute("CHANNEL_NAME");
        Attribute CHANNELID= root.attribute("CHANNELID");
        Attribute PARENTIDX= root.attribute("PARENTIDX");
        Attribute DATEFORMAT= root.attribute("DATEFORMAT");
        Attribute SPECIAL_CHANNEL_ID= root.attribute("SPECIAL_CHANNEL_ID");
        Attribute CHILDINDEX= root.attribute("CHILDINDEX");
        Attribute FIELD= root.attribute("FIELD");
        Attribute NUM= root.attribute("NUM");
        Attribute CLASSNAME= root.attribute("CLASSNAME");
        Attribute AUTOFORMATTYPE= root.attribute("AUTOFORMATTYPE");
        Attribute EXTRA= root.attribute("EXTRA");
        Attribute TARGET= root.attribute("TARGET");
        Attribute LINKALTTEXT= root.attribute("LINKALTTEXT");
        Attribute TRUNCATEDFLAG= root.attribute("TRUNCATEDFLAG");
        Attribute AUTOCOLOR= root.attribute("AUTOCOLOR");
        Attribute LINKTEXT= root.attribute("LINKTEXT");
        Attribute AUTOLINK= root.attribute("AUTOLINK");
        Attribute isPureContent= root.attribute("isPureContent");//这个标签的作用是，如果传的是true,那么将只返回内容，没有其它标签包括


        Map paramMap = new HashMap();
        paramMap.put("SITE_PK",inputMap.get("SITE_PK"));
        if(ID != null){
            if("OWNER".equals(ID.getValue())){//OWNER
                paramMap.put("CHANNEL_PK",docMap.get("CHANNEL_PK"));
            }else if("PARENT".equals(ID.getValue())){//<img>
                paramMap.put("PARENT_PK",docMap.get("PARENT_PK"));
            }
        }
        if(CHANNEL_NAME != null){
            paramMap.put("CHANNEL_NAME",CHANNEL_NAME.getValue());
        }
        if(CHANNELID != null){
            paramMap.put("CHANNEL_ID",CHANNELID.getValue());
        }
        if(SPECIAL_CHANNEL_ID != null){
            paramMap.put("SPECIAL_CHANNEL_ID",SPECIAL_CHANNEL_ID.getValue());
        }
        if(PARENTIDX != null ){
            int idx = Integer.parseInt(PARENTIDX.getValue());
            String code = (String)docMap.get("CHANNEL_CODE");
            int endIdx = code.length() - idx*3;
            code = code.substring(0,endIdx);
            paramMap.put("CHANNEL_CODE",code);
        }
        if(CHILDINDEX != null){
            paramMap.put("CHILDINDEX",CHILDINDEX.getValue());
        }

        List<Map> channelList =  baseService.getList("busiMapper.queryZbBusiChannel",paramMap);
        if(channelList.size()==0){
            throw new Exception("未找到标置指定的栏目，请检查");
        }
        Map channelMap = (Map)channelList.get(0);

        if(DATEFORMAT!=null){
            DateFormat df = new SimpleDateFormat(DATEFORMAT.getValue());
            Date val = (Date)channelMap.get(FIELD.getValue());
            return df.format(val);
        }
        String val = "";
        if(FIELD!=null){
            Object obj = channelMap.get(FIELD.getValue());
            if(obj!=null){
                val = obj.toString();
            }
        }
        if(AUTOFORMATTYPE!=null){
            if("HTML".equals(AUTOFORMATTYPE.getValue())){
                val = val.replaceAll("&","&amp;");
                val = val.replaceAll("\"","&quot;");
                val = val.replaceAll("\n","<BR/>");
                val = val.replaceAll(" ","&nbsp;");
            }else if("JAVASCRIPT".equals(AUTOFORMATTYPE.getValue())){
                val = val.replaceAll("\"","\\\"");
                val = val.replaceAll("\\ ","\\\\");
            }else if("innerText".equals(AUTOFORMATTYPE.getValue())){
                val = val.replaceAll("<br/>","\n");
                val = val.replaceAll("<br>","\n");
                val = val.replaceAll("<p>","\n");
                val = val.replaceAll("&nbsp;"," ");
                val = val.replaceAll("&lt;","<");
            }
        }

        //sb是纯内容
        // fontHtml是<font>sb</font>
        // link是<a><font>sb</font></a>
        StringBuffer sb = new StringBuffer(val);
        if(NUM!=null){
            int num  = Integer.parseInt(NUM.getValue());
            if(val.length()>num){
                sb.delete(num,sb.length()-1);
                if(TRUNCATEDFLAG!=null){
                    sb.append(TRUNCATEDFLAG.getValue());
                }else{
                    sb.append("...");
                }
            }
        }

        if(isPureContent!=null && "true".equals(isPureContent.getValue())){
            return sb.toString();
        }

        if(AUTOLINK!=null && "true".equals(AUTOLINK.getValue())){
            StringBuffer link = new StringBuffer("<a ");
            if(EXTRA!=null){
                link.append(EXTRA.getValue()).append(" ");
            }
            if(TARGET!=null){
                link.append(" target=\"").append(TARGET.getValue()).append("\" ");
            }
            if(CLASSNAME!=null){
                link.append(" class=\"").append(CLASSNAME.getValue()).append("\" ");
            }
            if(LINKALTTEXT!=null){
                link.append(" alt=\"").append(LINKALTTEXT.getValue()).append("\" ");
            }
            String url = channelMap.get("HOME_PAGE").toString();
            link.append(" href=\""+url+"\"");
            StringBuffer fontHtml = new StringBuffer("<font ");
            if(AUTOCOLOR!=null){
                fontHtml.append("color=\"").append(AUTOCOLOR.getValue());
            }
            fontHtml.append(">");

            if(LINKTEXT!=null){
                fontHtml.append(LINKTEXT.getValue());
            }else{
                fontHtml.append(sb);
            }
            fontHtml.append("</font>");
            link.append(">").append(fontHtml).append("</a>");
            return link.toString();
        }else{
            StringBuffer fontHtml = new StringBuffer("<font ");
            if(AUTOCOLOR!=null){
                fontHtml.append("color=\"").append(AUTOCOLOR.getValue());
            }
            fontHtml.append(">");
            fontHtml.append(sb);
            fontHtml.append("</font>");
            return fontHtml.toString();
        }

    }
}

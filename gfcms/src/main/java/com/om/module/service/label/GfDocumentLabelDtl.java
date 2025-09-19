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
import java.util.Map;

public class GfDocumentLabelDtl extends ABaseLabel {

    public GfDocumentLabelDtl(String xml) {
        super(xml,"GF_DOCUMENT_DTL");
    }

    /**
     * GF_DOCUMENT_DTL
     *         <GF_DOCUMENT_DTL FIELD='字段名' [NUM= '最大长度' ] [DATEFORMAT='日期格式'][AUTOFORMATTYPE='格式化方式'][isPureContent='true']
     *          [AUTOLINK='是否自动产生链接'] [EXTRA='A元素上扩展的HTML内容'] [TARGET='窗口目标']
     *         [LINKALTTEXT='链接提示内容'][AUTOCOLOR='文档标题颜色']
     *         [CLASSNAME='置标产生内容使用的样式']
     *         [TRUNCATEDFLAG='文档名称被截断后的显示内容'] [LINKTEXT='链接表面文字']      >通用细览</GF_DOCUMENT_DTL>
     * @return
     */
    @Override
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        try {
            String s = this.mylabel.substring(this.mylabel.indexOf(">") + 1, this.mylabel.indexOf("</GF_APPENDIX>"));
            if (s != null) {
                ABaseLabel label = new GfAppendixLabel(s);
                String html = label.getLabelHtmlContent(baseService);
                String s1 = this.mylabel.substring(0, this.mylabel.indexOf(">") + 1);
                int pos = this.mylabel.indexOf("</GF_APPENDIX>");
                String s2 = this.mylabel.substring(pos, pos + "</GF_APPENDIX>".length());
                logger.debug("GF_APPENDIX s1:" + s1);
                logger.debug("GF_APPENDIX s2:" + s2);
                this.mylabel = s1 + html + s2;
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }

        Document document = getDocByxml(this.mylabel);
        String DOC_PK = (String)inputMap.get("DOC_PK");

        Map tmpMap = new HashMap();
        tmpMap.put("DOC_PK",DOC_PK);
        Map docMap = (Map)baseService.getObject("busiMapper.queryZbBusiDocumentDef",tmpMap);

        Element root = document.getRootElement();
        Attribute FIELD= root.attribute("FIELD");
        Attribute NUM= root.attribute("NUM");
        Attribute DATEFORMAT= root.attribute("DATEFORMAT");
        Attribute AUTOLINK= root.attribute("AUTOLINK");
        Attribute EXTRA= root.attribute("EXTRA");
        Attribute TARGET= root.attribute("TARGET");
        Attribute LINKALTTEXT= root.attribute("LINKALTTEXT");
        Attribute AUTOCOLOR= root.attribute("AUTOCOLOR");
        Attribute CLASSNAME= root.attribute("CLASSNAME");
        Attribute TRUNCATEDFLAG= root.attribute("TRUNCATEDFLAG");
        Attribute LINKTEXT= root.attribute("LINKTEXT");
        Attribute AUTOFORMATTYPE= root.attribute("AUTOFORMATTYPE");
        Attribute isPureContent= root.attribute("isPureContent");//这个标签的作用是，如果传的是true,那么将只返回内容，没有其它标签包括

        if(DATEFORMAT!=null){
            DateFormat df = new SimpleDateFormat(DATEFORMAT.getValue());
            Date val = (Date)docMap.get(FIELD.getValue());
            return df.format(val);
        }
        String val = "";
        if(FIELD!=null){
            Object obj = docMap.get(FIELD.getValue());
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
                val = val.replaceAll("<p>","\\n");
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

        if(AUTOLINK!=null && "true".equals(AUTOLINK)){
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

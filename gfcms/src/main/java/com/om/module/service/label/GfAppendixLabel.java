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

public class GfAppendixLabel extends ABaseLabel {

    public GfAppendixLabel(String xml) {
        super(xml,"GF_APPENDIX");
    }

    /**
     * GF_APPENDIX
     * <GF_APPENDIX MODE=”附件的类型” CHANNEL_NAME='' AUTOLINK='true|false'  [INDEX=”序号”] [DOC_TITLE=”文档标题”]
          [BEGINHTML=”附件前面额外HTML代码”][ENDHTML=”附件后面额外HTML代码”]
            [EXTRA=”链接上额外的HTML属性”]
          [SEPERATOR=”多个附件分隔符”] [TARGET=”窗口目标”]
          [NAMEONLY=”仅仅生成文件名”] [WIDTH=”图片宽度”] [HEIGHT=”图片高度”]
          [RELATEDCOLUMN=”关联字段名”] >附件</GF_APPENDIX>
     * @return
     */
    @Override
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        Document document = getDocByxml(this.mylabel);
        Map docMap = inputMap;

        Element root = document.getRootElement();
        Attribute MODE= root.attribute("MODE");

        Attribute INDEX= root.attribute("INDEX");
        Attribute BEGINHTML= root.attribute("BEGINHTML");
        Attribute ENDHTML= root.attribute("ENDHTML");

        Attribute EXTRA= root.attribute("EXTRA");


        Attribute SEPERATOR= root.attribute("SEPERATOR");
        Attribute DOC_TITLE= root.attribute("DOC_TITLE");

        Attribute TARGET= root.attribute("TARGET");
        Attribute NAMEONLY= root.attribute("NAMEONLY");
        Attribute WIDTH= root.attribute("WIDTH");
        Attribute HEIGHT= root.attribute("HEIGHT");
        Attribute RELATEDCOLUMN= root.attribute("RELATEDCOLUMN");
        Attribute CHANNEL_NAME= root.attribute("CHANNEL_NAME");
        Attribute AUTOLINK= root.attribute("AUTOLINK");

        Map paramMap = new HashMap();
        paramMap.put("SITE_PK",inputMap.get("inputMap"));
        if(docMap!=null && docMap.get("DOC_PK")!=null){
            String DOC_PK=docMap.get("DOC_PK").toString();
            paramMap.put("DOCUMENT_PK",DOC_PK);
        }
        if(CHANNEL_NAME!=null){
            paramMap.put("CHANNEL_NAME",CHANNEL_NAME.getValue());
        }

        if(DOC_TITLE!=null){
            paramMap.put("DOC_TITLE",DOC_TITLE.getValue());
        }

        if(MODE != null){
            if("PIC".equals(MODE.getValue())){//<img>
                paramMap.put("MODE_PIC",1);
            }else if("FILE".equals(MODE.getValue())){//<img>
                paramMap.put("MODE_FILE",1);
            }
        }
        if(INDEX != null){
            paramMap.put("INDEX",INDEX.getValue());
        }
        if(RELATEDCOLUMN != null){
            paramMap.put("RELATEDCOLUMN",RELATEDCOLUMN.getValue());
        }

        List<Map> docFileList =  baseService.getList("busiMapper.queryZbBusiDocumentFile",paramMap);

        StringBuffer sb = new StringBuffer();
        for(Map fileMap:docFileList) {
            if(BEGINHTML!=null){
                sb.append(BEGINHTML.getValue());
            }
            if(NAMEONLY!=null && "true".equals(NAMEONLY)){
                sb.append("<span>").append(fileMap.get("FILE_NAME_INIT")).append("</span>");
                continue;
            }
            if ("PIC".equals(MODE.getValue())) {//<img  />
                if(AUTOLINK!=null && "true".equals(AUTOLINK.getValue())){
                    sb.append("<a ");
                    if(TARGET!=null){
                        sb.append(" target=\"").append(TARGET.getValue()).append("\" ");
                    }
                    sb.append(" href=\""+fileMap.get("FILE_PATH_URL")+"\" ");
                    sb.append(" >");
                }
                sb.append("<img ");
                sb.append(" src=\""+fileMap.get("FILE_PATH_URL")+"\" ");

                if(EXTRA!=null){
                    sb.append(EXTRA.getValue()).append(" ");
                }
                if(WIDTH!=null){
                    sb.append(" width=").append(WIDTH.getValue());
                }
                if(HEIGHT!=null){
                    sb.append(" height=").append(HEIGHT.getValue());
                }
                sb.append(" />");
                if(AUTOLINK!=null && "true".equals(AUTOLINK.getValue())){
                    sb.append("</a>");
                }
            } else {//<a></a>
                sb.append("<a ");
                if(TARGET!=null){
                    sb.append(" target=\"").append(TARGET.getValue()).append("\" ");
                }
                sb.append(" href=\""+fileMap.get("FILE_PATH_URL")+"\" ");

                if(EXTRA!=null){
                    sb.append(EXTRA.getValue()).append(" ");
                }
                sb.append(">").append(fileMap.get("FILE_NAME_INIT")).append("</a>");
            }

            if(ENDHTML!=null){
                sb.append(ENDHTML.getValue());
            }

            //分隔
            if(SEPERATOR!=null){
                sb.append(SEPERATOR.getValue());
            }
        }
        return sb.toString();
    }
}

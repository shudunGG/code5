package com.om.module.service.label;

import com.om.module.core.base.service.BaseService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于替换以前的置标GF_APPENDIX的，由于GF_APPENDIX置标设计过于复杂
 */
public class GfDocFileLabel extends ABaseLabel {

    public GfDocFileLabel(String xml) {
        super(xml,"GF_APPENDIX");
    }

    /**
     * GF_DOCFILES
     * <GF_DOCFILES CHANNEL_NAME=''  [DOC_TITLE=”文档标题”]>
     *      #{PUB_URL}#
     * </GF_DOCFILES>
     * @return
     */
    @Override
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        Document document = getDocByxml(this.mylabel);
        Map docMap = inputMap;

        Element root = document.getRootElement();
        Attribute INDEX= root.attribute("INDEX");
        Attribute LIMIT_NUM= root.attribute("LIMIT_NUM");
        Attribute MODE= root.attribute("MODE");
        Attribute DOC_TITLE= root.attribute("DOC_TITLE");
        Attribute CHANNEL_NAME= root.attribute("CHANNEL_NAME");
        Map paramMap = new HashMap();

        if(INDEX != null){
            paramMap.put("INDEX",INDEX.getValue());
        }
        if(LIMIT_NUM != null){
            paramMap.put("LIMIT_NUM",LIMIT_NUM.getValue());
        }else{
            paramMap.put("LIMIT_NUM",100);
        }
        if(MODE != null){
            if("PIC".equals(MODE.getValue())){//<img>
                paramMap.put("MODE_PIC",1);
            }else if("FILE".equals(MODE.getValue())){//<img>
                paramMap.put("MODE_FILE",1);
            }
        }
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
        List<Map> docFileList =  baseService.getList("busiMapper.queryZbBusiDocumentFile",paramMap);



        //现在需要对docList展示，实现对GF_DOCUMENTS置标内部的内容做循环，同时检测到GF_DOCUMENT后，调用置标
        String s = this.mylabel.substring(this.mylabel.indexOf(">")+1,this.mylabel.indexOf("</GF_DOCFILES>"));
        //处理子标签
        StringBuffer fullHtml = new StringBuffer();
        //判断是否需要分组
        for (Map map : docFileList) {
            StringBuffer innerXml = new StringBuffer(s);
            replaceParam(innerXml,map);
            fullHtml.append(innerXml);
        }
        return fullHtml.toString();
    }
}

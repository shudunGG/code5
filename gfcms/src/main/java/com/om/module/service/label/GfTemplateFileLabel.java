package com.om.module.service.label;

import com.om.module.core.base.service.BaseService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class GfTemplateFileLabel extends ABaseLabel {

    public GfTemplateFileLabel(String xml) {
        super(xml,"GF_TEMPLATE_FILE");
    }

    /**
     * GF_TEMPLATE_FILE
     *         <GF_TEMPLATE_FILE 【ID=”模板ID”】【CODE=”模板CODE”】 [FILE_NAME=”1234.zip”]
     * [PRINT_PATH=”true”]  ><column_block> </GF_TEMPLATE_FILE>
     * @return
     */
    @Override
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        Document document = getDocByxml(this.mylabel);

        Map docMap = inputMap;

        Element root = document.getRootElement();
        Attribute ID= root.attribute("ID");//PARENT  OWNER
        Attribute CODE= root.attribute("CODE");
        Attribute FILE_NAME= root.attribute("FILE_NAME");
        Attribute PRINT_PATH= root.attribute("PRINT_PATH");

        Map paramMap = new HashMap();
        paramMap.put("SITE_PK",inputMap.get("inputMap"));
        if(ID != null){
            paramMap.put("TMPL_PK",ID.getValue());
        }
        if(CODE != null){
            paramMap.put("TMPL_CODE",CODE.getValue());
        }
        if(FILE_NAME != null){
            paramMap.put("FILE_NAME",FILE_NAME.getValue());
        }

        List<Map> templateList =  baseService.getList("busiMapper.queryZbBusiTemplateFile",paramMap);
        Map templateMap = (Map)templateList.get(0);


        if(PRINT_PATH!=null){
            String val = (String)templateMap.get("FILE_PATH_URL");
            return val;
        }

        return "";
    }
}

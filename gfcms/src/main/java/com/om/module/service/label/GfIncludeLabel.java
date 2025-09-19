package com.om.module.service.label;

import com.om.module.core.base.service.BaseService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GfIncludeLabel extends ABaseLabel {

    public GfIncludeLabel(String xml) {
        super(xml,"GF_INCLUDE");
    }

    /**
     * GF_INCLUDE
     * <GF_INCLUDE  TMPL_NAME='模板名称' TMPL_CODE='模板唯一标识'  MODE='CONTENT|PATH' >  </GF_INCLUDE>
     * @return
     */
    @Override
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        Document document = getDocByxml(this.mylabel);
        Map docMap = inputMap;

        Element root = document.getRootElement();
        Attribute TMPL_NAME= root.attribute("TMPL_NAME");
        Attribute TMPL_CODE= root.attribute("TMPL_CODE");
        Attribute MODE= root.attribute("MODE");
        Map paramMap = new HashMap();
        paramMap.put("SITE_PK",docMap.get("SITE_PK"));
        if(TMPL_NAME!=null){
            paramMap.put("TMPL_NAME",TMPL_NAME.getValue());
        }
        if(TMPL_CODE!=null){
            paramMap.put("TMPL_CODE",TMPL_CODE.getValue());
        }


        List<Map> tmplList =  baseService.getList("busiMapper.queryZbBusiTemplateDef",paramMap);

        String rs = "";
        if(tmplList!=null && tmplList.size()>0){
            Map fileMap = tmplList.get(0);
            String TMPL_TYPE = fileMap.get("TMPL_TYPE").toString();
            logger.debug("gfinclude label tmpl_type:"+TMPL_TYPE+"   MODE:"+MODE.getValue());
            if(MODE != null){
                if("CONTENT".equals(MODE.getValue())){
                    String c1 = null;
                    String c2 = null;
                    if("2".equals(TMPL_TYPE)){
                        c1 = (String)fileMap.get("TMPL_CONTENT2");
                        c2 = (String)fileMap.get("TMPL_HTML_CONTENT2");
                    }else{
                        c1 = (String)fileMap.get("TMPL_CONTENT");
                        c2 = (String)fileMap.get("TMPL_HTML_CONTENT");
                    }

                    if(c2!=null && !"".equals(c2)){
                        return c2;
                    }
                    if(c1!=null && !"".equals(c1)){
                        return c1;
                    }

                }else if("PATH".equals(MODE.getValue())){
                    if("2".equals(TMPL_TYPE)){
                        String p = (String)fileMap.get("TMPL_HTML_PATH2");
                        return p;
                    }else{
                        String p = (String)fileMap.get("TMPL_HTML_PATH");
                        return p;
                    }

                }
            }

        }
        return rs;
    }
}

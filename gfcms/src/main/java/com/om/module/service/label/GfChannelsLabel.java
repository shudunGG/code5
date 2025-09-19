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

/**
 * @deprecated 建议使用GfChnlListLabel，避免字符串过像
 */
public class GfChannelsLabel extends ABaseLabel {

    public GfChannelsLabel(String xml) {
        super(xml,"GF_CHANNELS");
    }

    /**
     * GF_CHANNELS
     *         <GF_CHANNELS [ID=””][NAME=”栏目名”][SPECIAL_CHANNEL_ID=”栏目唯一标识”][SITEID="站点 ID"][CHANNELID="栏目ID"] [CHILDINDEX=”子栏目序号”]
     *         [STARTPOS=”起始位置”] [CHNLIDS=”多个栏目ID用英文逗号分隔”]
     * [NUM=”数目”][CHILDTYPE=”所取子栏目的类型”] [PAGESIZE=”分页参数”]> <column_block> </GF_CHANNELS>
     * @return
     */
    public String getLabelHtmlContent(BaseService baseService) throws  Exception {
        Map paramMap = new HashMap();
        Document document = getDocByxml(this.mylabel);
        String CHANNEL_PK = (String)inputMap.get("CHANNEL_PK");
        paramMap.put("CHANNEL_PK",CHANNEL_PK);

        Element root = document.getRootElement();
        Attribute ID= root.attribute("ID");
        Attribute NAME= root.attribute("NAME");
        Attribute SITEID= root.attribute("SITEID");
        Attribute CHANNELID= root.attribute("CHANNELID");
        Attribute CHILDINDEX= root.attribute("CHILDINDEX");
        Attribute CHNLIDS= root.attribute("CHNLIDS");
        Attribute CHILDTYPE= root.attribute("CHILDTYPE");
        Attribute SPECIAL_CHANNEL_ID= root.attribute("SPECIAL_CHANNEL_ID");

        Attribute NUM= root.attribute("NUM");
        Attribute STARTPOS= root.attribute("STARTPOS");
        Attribute PAGESIZE= root.attribute("PAGESIZE");/////NUM? pagesize


        if(ID!=null){
            String id = ID.getValue();
            if("PARENT".equals(id)){
                paramMap.put("PARENT",1);
            }else if("OWNER".equals(id)){
                paramMap.put("OWNER",1);
            }
        }
        if(NAME != null){
            paramMap.put("CHANNEL_NAME",NAME.getValue());
        }
        if(SPECIAL_CHANNEL_ID != null){
            paramMap.put("SPECIAL_CHANNEL_ID",SPECIAL_CHANNEL_ID.getValue());
        }
        if(SITEID != null){
            paramMap.put("SITE_CODE",SITEID.getValue());
        }
        if(CHANNELID != null){
            paramMap.put("CHANNEL_ID",CHANNELID.getValue());
        }
        if(CHILDINDEX != null){
            paramMap.put("CHILDINDEX",CHILDINDEX.getValue());
        }
        if(CHNLIDS != null){
            paramMap.put("CHNLIDS",CHNLIDS.getValue());
        }
        if(CHILDTYPE != null){
            paramMap.put("CHILDTYPE",CHILDTYPE.getValue());
        }


        /*
        Attribute NUM= root.attribute("NUM");
        Attribute STARTPOS= root.attribute("STARTPOS");
        Attribute PAGESIZE= root.attribute("PAGESIZE");/////NUM? pagesize
        这三个涉及到分页，后续再说
         */
        List<Map> channelList =  baseService.getList("busiMapper.queryZbBusiChannel",paramMap);

        StringBuffer moreSb = new StringBuffer();




        /*
        <TABLE>
            <GF_DOCUMENTS NUM="10" ID="CHANNELID:[136]">
                <TR>
                    <TD><GF_DOCUMENT FIELD="DOCTITLE"/></TD>
                    <TD><GF_DOCUMENT FIELD="CRTIME"/></TD>
                </TR>
            </GF_DOCUMENTS>
        </TABLE>
         */
        //现在需要对channelList展示，实现对GF_CHANNELS置标内部的内容做循环，同时检测到GF_CHANNEL后，调用置标
        String s = this.mylabel.substring(this.mylabel.indexOf(">")+1,this.mylabel.indexOf("</GF_CHANNELS>"));
        //处理子标签
        StringBuffer fullHtml = new StringBuffer();
        for(Map map: channelList){
            StringBuffer innerXml = new StringBuffer(s);
            switchLabel(innerXml,map,baseService,curPage,totalPage);
            fullHtml.append(innerXml);
        }



        return fullHtml.toString();

    }
}

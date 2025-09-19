package com.om.module.service.label;

import com.om.bo.busi.MyLabelBo;
import com.om.common.cache.MyLableCache;
import com.om.module.core.base.service.BaseService;
import com.om.module.service.common.CommonService;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

public abstract class ABaseLabel {
    protected Logger logger = LoggerFactory.getLogger(ABaseLabel.class);
    public ABaseLabel(String xml,String labelKey){
        this.mylabel = xml;
        this.labelKey = labelKey;
        logger.debug("ABaseLabel input xml:"+this.mylabel);
    }
    public String mylabel = null;
    public String labelKey = null;
    public Map inputMap = null;
    public int curPage = 1;
    public int totalPage =1 ;
    public int totalRecord = 0;

    public void setCurPage(int page){
        this.curPage = page;
    }
    public void setTotalPage(int page){
        this.totalPage = page;
    }

    public void setTotalRecord(int total){
        this.totalRecord = total;
    }

    public void setInputMap(Map inputMap){
        this.inputMap = inputMap;
    }

    public String getMylabel(){
        return this.mylabel;
    }
    public int getLabelLength(){
        if(mylabel!=null){
            return mylabel.length();
        }else{
            return 0;
        }
    }
    public abstract String getLabelHtmlContent(BaseService baseService)throws  Exception ;
    public  int getPageSplitNum(BaseService baseService)throws  Exception {
        return 1;
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

    /* 老版本，这个版本不对，我改成下面，上面暂时备份
    public int getPage(BaseService baseService)throws Exception{
        int pageNum = 1;
        int startPoint =  this.mylabel.indexOf("<GF_DOCUMENTS");
        int endPoint =  this.mylabel.indexOf("</GF_DOCUMENTS>");
        if(startPoint>-1 && endPoint>-1){
            String xml = this.mylabel.substring(startPoint,endPoint+15);
            ABaseLabel label = new GfDocumentsLabel(xml);
            label.setInputMap(inputMap);
            pageNum = label.getPageSplitNum(baseService);
        }
        totalPage = pageNum;
        return pageNum;
    }*/

    public int getPage(BaseService baseService)throws Exception{
        int pageNum = 1;
        int startPoint =  this.mylabel.indexOf("<GF_DOCUMENTS");
        int endPoint =  this.mylabel.indexOf("</GF_DOCUMENTS>");
        if(startPoint>-1 && endPoint>-1){
            String xml = this.mylabel.substring(startPoint,endPoint+15);
            this.mylabel = xml;
            pageNum = this.getPageSplitNum(baseService);
        }
        totalPage = pageNum;
        return pageNum;
    }

    /**
     * 用递归的方式，将模板中的置标，替换成html
     * @param sb
     */
    public void switchLabel(StringBuffer sb,Map map,BaseService baseService,int curPage,int totalPage,int totalRecord)throws Exception{
        for(MyLabelBo bo: MyLableCache.labelList){
            if(sb.indexOf(bo.getStartLabel())>-1){
                int startPoint = sb.indexOf(bo.getStartLabel());
                int endPoint = sb.indexOf(bo.getEndLabel())+bo.getEndLabel().length();
                String xml = sb.substring(startPoint,endPoint);

                ABaseLabel label = MyLableCache.getInstance(bo.getLabel(),xml);
                label.setInputMap(map);
                label.setCurPage(curPage);
                label.setTotalPage(totalPage);
                label.setTotalRecord(totalRecord);
                String subhtml = label.getLabelHtmlContent(baseService);
                logger.debug("check zb srchtml:"+sb.toString());
                logger.debug("check zb subhtml:"+subhtml);
                logger.debug("check zb startPoint:"+startPoint+" endPoint:"+endPoint);
                sb.delete(startPoint,endPoint);
                sb.insert(startPoint,subhtml);
                switchLabel(sb,map,baseService,curPage,totalPage,totalRecord);
            }
        }
    }

    /**
     * 用递归的方式，将模板中的置标，替换成html
     * @param sb
     */
    public void switchLabel(StringBuffer sb,Map map,BaseService baseService,int curPage,int totalPage)throws Exception{
        switchLabel(sb,map,baseService,curPage,totalPage,this.totalRecord);
    }

    public String getInnerHtml(Element root){
        StringBuffer innerHtmlSB = new StringBuffer();
        List<Element> eleList = root.elements();
        for(Element ele:eleList){
            innerHtmlSB.append(ele.asXML());
        }
        return innerHtmlSB.toString();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public Document getDocByxml(String xml)throws Exception{
        Document document = null;
        try{
            document = DocumentHelper.parseText(xml);
        }catch (Exception e){
            logger.error("置标内容为："+xml);
            logger.error(e.getMessage(),e);
            throw new Exception("置标["+this.mylabel+"]出错，请检查！");
        }
        return document;
    }

    /**
     * 通用的置标规则检查
     * @param baseService
     * @throws Exception
     */
    public void ruleCheck(BaseService baseService)throws Exception{
        if(this.mylabel == null || this.mylabel.length()<2){
            throw new Exception("未检测到模板内容，请检查！");
        }
        String invalidChar = "”,“";
        String[] invalidGrp = invalidChar.split(",");
        for(String s:invalidGrp){
            if(this.mylabel.indexOf(s)>-1){
                throw new Exception("发现不被允许出现的字符【"+s+"】，请检查");
            }
        }
        String startLabel ="<"+labelKey;
        String endLabel ="</"+labelKey+">";

        this.mylabel = this.mylabel.trim();
        if(!(this.mylabel.startsWith(startLabel) && this.mylabel.endsWith(endLabel))){
            logger.error("置标必须以"+startLabel+"为开头，以"+endLabel+"为结尾");
        }

        getDocByxml(this.mylabel);
    }


    /**
     * 用递归的方式，将模板中的置标，替换成html
     * @param sb
     */
    public void switchCheck(StringBuffer sb,Map map,BaseService baseService,int curPage,int totalPage,int totalRecord)throws Exception{
        int i=MyLableCache.labelList.size()-1;
        for(;i>=0;i--){
            MyLabelBo bo = (MyLabelBo)MyLableCache.labelList.get(i);
            if(sb.indexOf(bo.getStartLabel())>-1){
                int startPoint = sb.indexOf(bo.getStartLabel());
                int endPoint = sb.indexOf(bo.getEndLabel())+bo.getEndLabel().length();
                String xml = sb.substring(startPoint,endPoint);

                ABaseLabel label = MyLableCache.getInstance(bo.getLabel(),xml);
                label.setInputMap(map);
                label.setCurPage(curPage);
                label.setTotalPage(totalPage);
                label.setTotalRecord(totalRecord);
                label.ruleCheck(baseService);
                sb.delete(startPoint,endPoint);
                sb.insert(startPoint,"");
                switchCheck(sb,map,baseService,curPage,totalPage,totalRecord);
            }
        }
    }

}

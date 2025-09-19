package com.om.common.cache;

import com.om.bo.busi.MyLabelBo;
import com.om.module.service.label.*;

import java.util.ArrayList;
import java.util.List;

public class MyLableCache {
    public static List<MyLabelBo> labelList = new ArrayList<MyLabelBo>();

    static {
        //后续这块应该是有一个固定的地方，比如从数据库或者缓存中来，或者是个静态文件
        //这里的顺序是有讲究的，如果有依赖的，得把被依赖的放前面
        MyLabelBo myLabelBo5 = new MyLabelBo("GF_DOCUMENTS");
        labelList.add(myLabelBo5);

        MyLabelBo myLabelBo15 = new MyLabelBo("GF_DOCLIST");//GF_DOCLIST是GF_DOCUMENTS的平替置标，因为GF_DOCUMENTS和GF_DOCUMENT太像，导致字符串截图有bug
        labelList.add(myLabelBo15);



        MyLabelBo myLabelBo = new MyLabelBo("GF_DOCUMENT");
        labelList.add(myLabelBo);

        MyLabelBo myLabelBo2 = new MyLabelBo("GF_APPENDIX");
        labelList.add(myLabelBo2);

        MyLabelBo myLabelBo21 = new MyLabelBo("GF_DOCFILES");
        labelList.add(myLabelBo21);



        MyLabelBo myLabelBo4 = new MyLabelBo("GF_CHANNELS");
        labelList.add(myLabelBo4);

        MyLabelBo myLabelBo14 = new MyLabelBo("GF_CHNLLIST");
        labelList.add(myLabelBo14);


        MyLabelBo myLabelBo3 = new MyLabelBo("GF_CHANNEL");
        labelList.add(myLabelBo3);

        MyLabelBo myLabelBo6 = new MyLabelBo("GF_TEMPLATE_FILE");
        labelList.add(myLabelBo6);

        MyLabelBo myLabelBo7 = new MyLabelBo("GF_DOCUMENT_DTL");
        labelList.add(myLabelBo7);

        MyLabelBo myLabelBo9 = new MyLabelBo("GF_LOCATION");
        labelList.add(myLabelBo9);

        MyLabelBo myLabelBo8 = new MyLabelBo("GF_INCLUDE");
        labelList.add(myLabelBo8);
    }



    public static ABaseLabel getInstance(String label, String xml){
        ABaseLabel obj = null;
        if("GF_INCLUDE".equals(label)){
            obj = new GfIncludeLabel(xml);
        }else if("GF_DOCUMENT".equals(label)){
            obj = new GfDocumentLabel(xml);
        }else if("GF_APPENDIX".equals(label)){
            obj = new GfAppendixLabel(xml);
        }else if("GF_DOCFILES".equals(label)){
            obj = new GfDocFileLabel(xml);
        }else if("GF_CHANNEL".equals(label)){
            obj = new GfChannelLabel(xml);
        }else if("GF_CHANNELS".equals(label)){
            obj = new GfChannelsLabel(xml);
        }else if("GF_CHNLLIST".equals(label)){
            obj = new GfChnlListLabel(xml);
        }else if("GF_DOCUMENTS".equals(label)){
            obj = new GfDocumentsLabel(xml);
        }else if("GF_DOCLIST".equals(label)){
            obj = new GfDocListLabel(xml);
        }else if("GF_TEMPLATE_FILE".equals(label)){
            obj = new GfTemplateFileLabel(xml);
        }else if("GF_DOCUMENT_DTL".equals(label)){
            obj = new GfDocumentLabelDtl(xml);
        }else if("GF_LOCATION".equals(label)){
            obj = new GfLocationLabel(xml);
        }
        return obj;
    }

}

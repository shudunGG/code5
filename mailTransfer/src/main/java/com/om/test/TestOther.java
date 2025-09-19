package com.om.test;

import com.om.util.ObjectTools;

public class TestOther {
    public static void main(String[] args) {


        try {
            TestOther t = new TestOther();
            String serverPath="D:/opt/om/cms_upload_permanent//lanzhouGov/document/002/202206//36546006246.html";
            String pub_file_name="36546006246.html";
            String root="D:/opt/om/cms_upload_permanent/";
            System.out.println(t.getMidPath(serverPath,pub_file_name,root));
            /*List<String> list = UZipFile.unZipFiles(new File("D:\\temp\\wcmym2.zip"), "D:\\temp\\");

            for(String s:list){
                System.out.println("====:"+s);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
//        String xml="<GF_DOCUMENT FIELD=\"字段名\" NUM= \"最大长度\"  DATEFORMAT=\"日期格式\" AUTOFORMAT=\"自动格式化\"       >通用细览</GF_DOCUMENT>";
//        try {
//            Document document = DocumentHelper.parseText(xml);
//            Element e = document.getRootElement();
//            Attribute rootAttr= e.attribute("FIELD2");
//            System.out.println(rootAttr);
//            String val = rootAttr.getValue();
//            System.out.println(val);
//        }catch (Exception ee){
//            ee.printStackTrace();
//        }
        /*
        <GF_DOCUMENT FIELD=”字段名” [NUM= ”最大长度” ] [DATEFORMAT=”日期格式”][AUTOFORMAT=”自动格式化”]
        [AUTOFORMATTYPE=”格式化方式”] [AUTOLINK=”是否自动产生链接”] [EXTRA=”A元素上扩展的HTML内容”] [TARGET=”窗口目标”]
        [LINKALT=”链接是否显示提示”] [LINKALTTEXT=”链接提示内容”][AUTOCOLOR=”文档标题颜色”]
        [CLASSNAME=”置标产生内容使用的样式”][TRUEUSER=”发稿人的真实姓名”]
        [TRUNCATEDFLAG=”文档名称被截断后的显示内容”] [NICKCRUSER=””][LINKTEXT=”链接表面文字”]
        [CODEFILTERED=”是否过滤标题中的html代码”][FILTEREDITORCSS=”过滤掉文档发布时正文中自带的 CSS 样式”]>通用细览</GF_DOCUMENT>
         */

        String sql="999999";//52c69e3a57331081823331c4e69d3f2e
        System.out.println(ObjectTools.md5(sql));
    }


    private String getMidPath(String fullPath,String fileName,String root){
        int i = fullPath.indexOf(fileName);
        fullPath = fullPath.substring(0,i);
        i = fullPath.indexOf(root);
        if(i>-1){
            fullPath = fullPath.substring(i+root.length(),fullPath.length()-1);
        }
        if(fullPath.charAt(fullPath.length()-1)=='/'){
            fullPath = fullPath.substring(0,fullPath.length()-1);
        }
        return fullPath;
    }


}

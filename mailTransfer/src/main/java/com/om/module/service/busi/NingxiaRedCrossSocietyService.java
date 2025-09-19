package com.om.module.service.busi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.om.bo.base.busi.CatalogBo;
import com.om.module.service.common.CommonService;
import com.om.util.HttpInterface;
import com.om.util.Pk;
import com.om.util.SendFileUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于宁夏红十字会上传接口用的
 */
@Service("NingxiaRedCrossSocietyService")
public class NingxiaRedCrossSocietyService extends CommonService {
    private static String KEY =null;
    private static final int A=0;   //EXCEL的列
    private static final int B=1;
    private static final int C=2;
    private static final int D=3;
    private static final int E=4;
    private static final int F=5;
    private static final int G=6;

    private static final int H=7;   //EXCEL的列
    private static final int I=8;
    private static final int J=9;
    private static final int K=10;
    private static final int L=11;
    private static final int M=12;
    private static final int N=13;

    private static final int O=14;   //EXCEL的列
    private static final int P=15;
    private static final int Q=16;
    private static final int R=17;
    private static final int S=18;
    private static final int T=19;
    private static final int U=20;

    private static final int V=21;   //EXCEL的列
    private static final int W=22;
    private static final int X=23;
    private static final int Y=24;
    private static final int Z=25;
    private static final int AA=26;




    /**
     * 解析采集的EXCEL里的数据
     * @param excelFullPath
     * @return
     * @throws Exception
     */
    public Map<String,String> readExcelContent(String excelFullPath) throws Exception {
        File file = new File(excelFullPath);
        if(!file.exists()){
            throw new Exception("该路径下的文件不存在："+excelFullPath);
        }
        InputStream in = new FileInputStream(file);
        XSSFWorkbook xwb = new XSSFWorkbook(in);
        // 读取第一章表格内容
        XSSFSheet sheet = xwb.getSheetAt(0); //下标从0开始
        XSSFRow row = null; //行
        logger.info("总行数为:"+sheet.getLastRowNum());
        logger.info("总行数2："+sheet.getPhysicalNumberOfRows());
        int startReadLine=1; //开始读取的行
        row = sheet.getRow(startReadLine);
        if(row == null){
            throw new Exception("未找到数据！");
        }

        String a = getCellValue(row,A);//采集日期（yyyymmdd)
        String b = getCellValue(row,B);//单位办公地址
        String c = getCellValue(row,C);//联系电话
        String d = getCellValue(row,D);//电子邮箱
        String e = getCellValue(row,E);//官网网址
        String f = getCellValue(row,F);//微信公众号

        String g = getCellValue(row,G);//名誉会长
        String h = getCellValue(row,H);//会长
        String i = getCellValue(row,I);//副会长
        String j = getCellValue(row,J);//秘书长
        String k = getCellValue(row,K);//监事长
        String l = getCellValue(row,L);//副监事长

        String m = getCellValue(row,M);//理事会
        String n = getCellValue(row,N);//监事会
        String o = getCellValue(row,O);//常务理事会
        String p = getCellValue(row,P);//执行委员会
        String q = getCellValue(row,Q);//内设部门
        String r = getCellValue(row,R);//直属单位

        String s = getCellValue(row,S);//2023年度审计报告
        String t = getCellValue(row,T);//2024年度预算报告
        String u = getCellValue(row,U);//2023年度决算报告
        String v = getCellValue(row,V);//公开募捐资格证书

        String w = getCellValue(row,W);//（公益性捐赠税前扣除资格5项数据）最近一次获得资格的日期
        String x = getCellValue(row,X);//有效期开始年份
        String y = getCellValue(row,Y);//有效期结束年份
        String z = getCellValue(row,Z);//批准文件名称
        String aa = getCellValue(row,AA);//	文号

        LinkedMap<String,String> map = new LinkedMap<String,String>();
        map.put("a",a);
        map.put("b",b);
        map.put("c",c);
        map.put("d",d);
        map.put("e",e);
        map.put("f",f);
        map.put("g",g);
        map.put("h",h);
        map.put("i",i);
        map.put("j",j);
        map.put("k",k);
        map.put("l",l);
        map.put("m",m);
        map.put("n",n);
        map.put("o",o);
        map.put("p",p);
        map.put("q",q);
        map.put("r",r);
        map.put("s",s);
        map.put("t",t);
        map.put("u",u);
        map.put("v",v);
        map.put("w",w);
        map.put("x",x);
        map.put("y",y);
        map.put("z",z);
        map.put("aa",aa);

        return map;
    }

    public void sendDataMain(Map<String,String> map,String urlHead,String userName,String password)throws Exception {
        String a = map.get("a");//采集日期（yyyymmdd)
        String b = map.get("b");//单位办公地址
        String c = map.get("c");//联系电话
        String d = map.get("d");//电子邮箱
        String e = map.get("e");//官网网址
        String f = map.get("f");//微信公众号
        String g = map.get("g");//名誉会长
        String h = map.get("h");//会长
        String i = map.get("i");//副会长
        String j = map.get("j");//秘书长
        String k = map.get("k");//监事长
        String l = map.get("l");//副监事长
        String m = map.get("m");//理事会
        String n = map.get("n");//监事会
        String o = map.get("o");//常务理事会
        String p = map.get("p");//执行委员会
        String q = map.get("q");//内设部门
        String r = map.get("r");//直属单位
        String s = map.get("s");//2023年度审计报告
        String t = map.get("t");//2024年度预算报告
        String u = map.get("u");//2023年度决算报告
        String v = map.get("v");//公开募捐资格证书
        String w = map.get("w");//（公益性捐赠税前扣除资格5项数据）最近一次获得资格的日期
        String x = map.get("x");//有效期开始年份
        String y = map.get("y");//有效期结束年份
        String z = map.get("z");//批准文件名称
        String aa =map.get("aa");//	文号

        login(urlHead, userName,password);

        JSONObject json = new JSONObject();
        json.put("key",KEY);
        json.put("bandzh",b);
        simpleColumn(urlHead, json,"/apisync/core/bandzh","1、办公地址同步");

        json.put("lianxdh",c);
        simpleColumn(urlHead, json,"/apisync/core/lianxdh","2、固定电话同步");

        json.put("youx",d);
        simpleColumn(urlHead, json,"/apisync/core/youx","3、邮箱同步");

        json.put("guanw",e);
        simpleColumn(urlHead, json,"/apisync/core/guanw","4、官网网址同步");
        json.put("gongzhh",f);
        simpleColumn(urlHead, json,"/apisync/core/gongzhh","5、公众号同步");


        //这里的名誉会长，是要传一个结构，而非简单的字段,这里g的格式为：
        /*
            {
                "xingm": "测试",
                    "zhaop": "http://localhost:8060/profile/upload/2024/05/10/72fde9b0-90d9-4acf-add1-777de2d01715.jpg",
                    "zhiw": "名誉会长",
                    "jianj": "测试一个简介",
                    "weight": "1"
            }
        */
        JSONObject jsonG = JSONObject.parseObject(g);
        JSONArray arr = new JSONArray();
        arr.add(jsonG);
        json.put("lists",arr.toJSONString());
        simpleColumn(urlHead, json,"/apisync/reny/myhz","6、名誉会长同步");



        JSONObject jsonH = JSONObject.parseObject(h);
        JSONObject jsonI = JSONObject.parseObject(i);
        JSONObject jsonJ = JSONObject.parseObject(j);
        JSONObject jsonK = JSONObject.parseObject(k);
        JSONObject jsonL = JSONObject.parseObject(l);

        arr = new JSONArray();
        arr.add(jsonH);
        arr.add(jsonI);
        arr.add(jsonJ);
        arr.add(jsonK);
        arr.add(jsonL);
        json.put("lists",arr.toJSONString());
        simpleColumn(urlHead, json,"/apisync/reny/ld","7、领导机构同步");

        /**
         * "lists": [
         *         {
         *             "title":"测试标题",
         *             "content":"测试内容",
         *             "orderby":1
         *         },
         *         {
         *             "title":"测试标题1",
         *             "content":"测试内容1",
         *             "orderby":2
         *         }
         *     ]
         */
        json.put("lists",n);
        simpleColumn(urlHead, json,"/apisync/jsh","8、监事会同步");


        json.put("lists",m);
        simpleColumn(urlHead, json,"/apisync/lsh","9、理事会同步");

        json.put("lists",o);
        simpleColumn(urlHead, json,"/apisync/cwlsh","9、常务理事会同步");
        json.put("lists",p);
        simpleColumn(urlHead, json,"/apisync/zxwyh","10、执行委员会同步");

        /**
         *  [
         * 		        {
         * 			"bumen": "测试部门",
         * 			"zhin": "测试部门职能",
         * 			"fuzeren": "负责人"
         *        },
         *        {
         * 			"bumen": "测试部门1",
         * 			"zhin": "测试部门职能1",
         * 			"fuzeren": "负责人1"
         *        }
         * 	]
         */
        json.put("lists",q);
        simpleColumn(urlHead, json,"/apisync/in","10、内设机构同步");

        json.put("zhisdw",r);
        simpleColumn(urlHead, json,"/apisync/zhisdw","11、直属单位同步");

        //开始年度审计报告
        wenjian(urlHead, s);
        wenjian(urlHead, t);
        wenjian(urlHead, u);

        gkmjzg(urlHead,v);

    }

    /**
     * 4.1.1api接口认证获取
     * @param urlHead
     * @param userName
     * @param password
     * @throws Exception
     */
    public void login(String urlHead, String userName,String password) throws Exception {
        String url = urlHead +"/apisync/login";
        JSONObject json = new JSONObject();
        json.put("userName",userName);
        json.put("password",password);
        Map headMap = null;
        Map m = HttpInterface.doPostJsonV2(url,headMap,json);
        KEY = m.get("msg").toString();
    }

    public void simpleColumn(String urlHead, JSONObject json,String apiName,String logTitle) throws Exception {
        String url = urlHead +apiName;
        Map headMap = null;
        Map m = HttpInterface.doPostJsonV2(url,headMap,json);
        if(m!=null){
            logger.info(logTitle+"【"+m.get("msg")+"】");
        }
    }

    /**
     * 4.1.18api公开募捐资格同步
     * @param urlHead
     * @param data
     * @throws Exception
     */
    public void gkmjzg(String urlHead, String data) throws Exception {
        /**
         * data: {
         * 	"mjshow":"募捐资格：0是，1否",
         * 	"url":"https://23242.jpg",
         * 	"wenj":"不需要传",
         * 	"kcshow":"是否取得公益性捐赠税前扣除资格：0是，1否",
         * 	"zhigtime":"最近一次获得资格的日期:2021-01-24",
         * 	"startyaer":"有效开始年份:2021",
         * 	"endyaer":"有效结束年份:2024",
         * 	"wenjmch":"批准文件名称",
         * 	"wenjh":"文号",
         * 	"zuozcl":"佐证材料，格式为 jpg/png/jpeg/gif/pdf 的附件链接",
         * }
         */
        String url = urlHead +"/apisync/gkmjzg";
        JSONObject obj = JSONObject.parseObject(data);
        String pdfUrl = (String)obj.get("url");//pdfUrl：文件在网站上的链接地址
        String remoteUrl = uploadFile(urlHead, pdfUrl);//remoteUrl：文件上传到总会服务器上返回的地址
        obj.put("wenj",remoteUrl);
        if(obj.get("zuozcl")!=null){
            String zuozcl = obj.get("zuozcl").toString();
            if(zuozcl.startsWith("http")){
                remoteUrl = uploadFile(urlHead, zuozcl);
                obj.put("zuozcl",remoteUrl);
            }
        }

        /**
         *  {
         *     "key": "MjAyNC0wNS0xMOays+WMl+ecgee6ouWNgeWtl+S8mg==",
         *     "mjshow": "0",
         *     "wenj":"http://localhost:8060/profile/upload/2024/05/10/72fde9b0-90d9-4acf-add1-777de2d01715.jpg",
         *     "kcshow":"0",
         *     "zhigtime":"",
         *     "startyaer":"2021",
         *     "endyaer":"2024",
         *     "wenjmch":"测试文件",
         *     "wenjh":"",
         *     "zuozcl":""
         * }
         */

        Map headMap = null;
        obj.put("key",KEY);
        Map m = HttpInterface.doPostJsonV2(url,headMap,obj);
        if(m!=null){
            logger.info("公开募捐资格同步【"+m.get("msg")+"】");
        }
    }



    /**
     * 4.1.17 api公示文件同步
     * @param urlHead
     * @param data
     * @throws Exception
     */
    public void wenjian(String urlHead, String data) throws Exception {
        /**
         * data: {
         *  "url":"http://sdfsfsf.pdf",
         *  "shangchshj":"年度",
         *  "wenjlx":"文件类型",
         *  "jianj":"文件简介",
         * }
         */
        String url = urlHead +"/apisync/wenjian";
        Map obj = JSONObject.parseObject(data);
        String pdfUrl = (String)obj.get("url");//pdfUrl：文件在网站上的链接地址
        String remoteUrl = uploadFile(urlHead, pdfUrl);//remoteUrl：文件上传到总会服务器上返回的地址

        List<String> wenjlj = new ArrayList<>();
        wenjlj.add(remoteUrl);
        obj.put("wenjlj",wenjlj);
        obj.put("gongklj",pdfUrl);
        obj.put("orderid",1);
        List dataList = new ArrayList();
        dataList.add(obj);
        /**
         *  [
         *         {
         *             "wenjlj": [
         *                 "http://bazg.redcross.org.cn/profile/upload/2023/06/12/fc7c1bea-15f9-475a-ae6a-d9293a5b25e7.jpg",
         *                 "http://bazg.redcross.org.cn/profile/upload/2023/06/12/fc7c1bea-15f9-475a-ae6a-d9293a5b25e7.jpg"
         *             ],
         *             "shangchshj": "2023",
         *             "wenjlx": "年度审计报告",
         *             "jianj": "测试简介",
         *             "gongklj": "https://bazg.redcross.org.cn/",
         *             "orderid": 1
         *         },
         */
        JSONArray arrJson = new JSONArray(dataList);
        Map headMap = null;
        JSONObject json = new JSONObject();
        json.put("key",KEY);
        json.put("list",arrJson.toJSONString());
        Map m = HttpInterface.doPostJsonV2(url,headMap,json);
        if(m!=null){
            logger.info("上传文件【"+m.get("msg")+"】");
        }
    }


    /**
     * 4.1.16api上传文件
     * @param urlHead
     * @param fileHttpUrl
     * @throws Exception
     */
    public String uploadFile(String urlHead, String fileHttpUrl) throws Exception {
        String url = urlHead +"/apisync/upload/document";
        Map m = new HashMap();
        m.put("key",KEY);

        // 从URL下载图片并写入临时文件
        URL imageURL = new URL(fileHttpUrl);
        URLConnection connection = imageURL.openConnection();
        InputStream imageStream = connection.getInputStream();

        // 创建临时文件
        File tempFile = File.createTempFile("temp_pdf", ".pdf");
        String rs = SendFileUtils.sendMultipartFile(url,m,tempFile);
        logger.info("上传文件的结果是："+rs);
        Map mm = JSONObject.parseObject(rs);
        String rsurl = (String)mm.get("url");
        return rsurl;
    }



    /**
     * 4.1.2api单位办公地址同步
     * @param urlHead
     * @param bandzh
     * @throws Exception
     */
    public void bandzh(String urlHead, String bandzh) throws Exception {
        String url = urlHead +"/apisync//core/bandzh";
        JSONObject json = new JSONObject();
        json.put("key",KEY);
        json.put("bandzh",bandzh);
        Map headMap = null;
        Map m = HttpInterface.doPostJsonV2(url,headMap,json);
        if(m!=null){
            logger.info("1、办公地址同步【"+m.get("msg")+"】");
        }
    }








    private static String getCellValue(XSSFRow row,int pos){
        XSSFCell cell = row.getCell(pos);
        if(cell == null){
            return null;
        }else{
            String s = null;
            try {
                s =  String.valueOf(cell.getStringCellValue());
                return s;
            }catch (Exception e){      }

            try {
                s = String.valueOf(cell.getNumericCellValue());
                return s;
            }catch (Exception e){      }

            try {
                s = String.valueOf(cell.getBooleanCellValue());
                return s;
            }catch (Exception e){      }

            try {
                s = String.valueOf(cell.getDateCellValue());
                return s;
            }catch (Exception e){      }

            return  s;
        }
    }


}

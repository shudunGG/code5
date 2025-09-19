package com.om.test;

import com.alibaba.fastjson.JSONObject;

import com.om.bo.base.EncryptDES;
import com.om.common.util.EncryptUtils;
import com.om.common.util.HttpInterface;
import com.om.common.util.ObjectTools;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TestOther {
    public static void main(String[] args) {


        try {
            String tmplContent = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "\t<head>\n" +
                    "\t\t<meta charset=\"utf-8\">\n" +
                    "\t\t<title></title>\n" +
                    "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"res/config.css\" />\n" +
                    "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"res/lbStyle.css\" />\n" +
                    "\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"res/index.css\" />\n" +
                    "\t</head>\n" +
                    "\t<body>\n" +
                    "\t\t<div class=\"wrap\">\n" +
                    "\t\t\t<div class=\"title\">\n" +
                    "\t\t\t\t<p class=\"top_but\">\n" +
                    "\t\t\t\t\t<a href=\"login.html\"><img width=\"20px\" height=\"18px\"\n" +
                    "\t\t\t\t\t\t\tsrc=\"res/dl_logo.png\" /><span>登录</span></a>\n" +
                    "\t\t\t\t\t<a href=\"signIn.html\"><img width=\"20px\" height=\"20px\"\n" +
                    "\t\t\t\t\t\t\tsrc=\"res/zc_logo.png\" /><span>注册</span></a>\n" +
                    "\t\t\t\t</p>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t\t<div class=\"nav\">\n" +
                    "\t\t\t\t<ul>\n" +
                    "\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t<li>网站首页</li>\n" +
                    "\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t<li>通知公告</li>\n" +
                    "\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t<li>政策法规</li>\n" +
                    "\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t<li>报名条件</li>\n" +
                    "\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t<li>网上报名</li>\n" +
                    "\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t<li>表格下载</li>\n" +
                    "\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t<li>联系我们</li>\n" +
                    "\t\t\t\t\t</a>\n" +
                    "\t\t\t\t</ul>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t\t<div class=\"con\">\n" +
                    "\t\t\t\t<div class=\"tzgg\">\n" +
                    "\t\t\t\t\t<div id=\"wrapper\">\n" +
                    "\t\t\t\t\t\t<div id=\"slider-wrap\">\n" +
                    "\t\t\t\t\t\t\t<ul id=\"slider\">\n" +
                    "\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t<div>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<img src=\"res/1.jpg\" />\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span\n" +
                    "\t\t\t\t\t\t\t\t\t\t\tclass=\"jj\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t<div>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<img src=\"res/2.jpg\" />\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span\n" +
                    "\t\t\t\t\t\t\t\t\t\t\tclass=\"jj\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t<div>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<img src=\"res/2.jpg\" />\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span\n" +
                    "\t\t\t\t\t\t\t\t\t\t\tclass=\"jj\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t</ul>\n" +
                    "\t\t\t\t\t\t\t<!--controls-->\n" +
                    "\t\t\t\t\t\t\t<div class=\"btns\" id=\"next\"></div>\n" +
                    "\t\t\t\t\t\t\t<div class=\"btns\" id=\"previous\"></div>\n" +
                    "\t\t\t\t\t\t\t<!-- <div id=\"counter\"></div> -->\n" +
                    "\t\t\t\t\t\t\t<div id=\"pagination-wrap\">\n" +
                    "\t\t\t\t\t\t\t\t<ul>\n" +
                    "\t\t\t\t\t\t\t\t</ul>\n" +
                    "\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t\t<!--controls-->\n" +
                    "\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t<div class=\"tzggList\">\n" +
                    "\t\t\t\t\t\t<p class=\"tzggList_tit\"><a href=\"notification.html\">更多></a></p>\n" +
                    "\t\t\t\t\t\t<ul>\n" +
                    "\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t</ul>\n" +
                    "\t\t\t\t\t</div>\n" +
                    "\t\t\t\t</div>\n" +
                    "\t\t\t\t<script src=\"res/jquery-1.8.3.min.js\"></script>\n" +
                    "\t\t\t\t<script src=\"res/slide.js\"></script>\n" +
                    "\t\t\t\t<div class=\"con_bot\">\n" +
                    "\t\t\t\t\t<div class=\"con_left\">\n" +
                    "\t\t\t\t\t\t<a href=\"#\"><div class=\"wsbb\"></div></a>\n" +
                    "\t\t\t\t\t\t<div class=\"bmss\">\n" +
                    "\t\t\t\t\t\t\t<div class=\"bmss_tit\">\n" +
                    "\t\t\t\t\t\t\t\t<i class=\"bmss_tit_logo\"></i>\n" +
                    "\t\t\t\t\t\t\t\t<p class=\"bmss_tit_txt\">报名搜索</p>\n" +
                    "\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t\t<div class=\"bmss_con\">\n" +
                    "\t\t\t\t\t\t\t\t<form>\n" +
                    "\t\t\t\t\t\t\t\t\t<p><span>身份证号：</span><input type=\"text\" class=\"text\"></p>\n" +
                    "\t\t\t\t\t\t\t\t\t<p><span>登录密码：</span><input type=\"password\"></p>\n" +
                    "\t\t\t\t\t\t\t\t\t<p><span>验证码：</span><input type=\"text\" class=\"text\"></p>\n" +
                    "\t\t\t\t\t\t\t\t\t<div class=\"yzm\">\n" +
                    "\t\t\t\t\t\t\t\t\t\t<img src=\"res/yzm.jpg\"/>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>请输入</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t\t\t\t<div class=\"submit\"><input type=\"submit\" onclick=\"myFunction()\" value=\"登录查询\" ></div>\n" +
                    "\t\t\t\t\t\t\t\t</form>\n" +
                    "\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t<a href=\"#\"><div class=\"gywm\"></div></a>\n" +
                    "\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t<div class=\"con_right\">\n" +
                    "\t\t\t\t\t\t<div class=\"zcfgList\">\n" +
                    "\t\t\t\t\t\t\t<p class=\"zcfgList_tit\"><a href=\"notification.html\">更多></a></p>\n" +
                    "\t\t\t\t\t\t\t<ul>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t</ul>\n" +
                    "\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t<div class=\"bmtjList\">\n" +
                    "\t\t\t\t\t\t\t<p class=\"bmtjList_tit\"><a href=\"notification.html\">更多></a></p>\n" +
                    "\t\t\t\t\t\t\t<div class=\"bmtj_con\">\n" +
                    "\t\t\t\t\t\t\t\t<p>报名参加朝觐，须具备以下条件：</p>\n" +
                    "\t\t\t\t\t\t\t\t<div class=\"sj\">\n" +
                    "\t\t\t\t\t\t\t\t\t<p>(一)18周岁以上、75周岁以下信仰伊斯兰教、具有宁夏户籍的公民；</p>\n" +
                    "\t\t\t\t\t\t\t\t\t<p>(二)身体健康，理智健全，适宜乘坐飞机、汽车长途旅行，能够独立完成朝觐期间的各项宗教活动；</p>\n" +
                    "\t\t\t\t\t\t\t\t\t<p>(三)能够支付朝觐有关费用，而不影响家人的生产、生活正常进行，非扶贫对象和社会救助者；</p>\n" +
                    "\t\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t\t<div class=\"bgxzList\">\n" +
                    "\t\t\t\t\t\t\t<p class=\"bgxzList_tit\"><a href=\"notification.html\">更多></a></p>\n" +
                    "\t\t\t\t\t\t\t<ul>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t\t<a href=\"#\">\n" +
                    "\t\t\t\t\t\t\t\t\t<li>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<i class=\"liD\"></i>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span class=\"list_tit\">宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网宁夏回族自治区民族事务委员会报名网</span>\n" +
                    "\t\t\t\t\t\t\t\t\t\t<span>2023-12-18</span>\n" +
                    "\t\t\t\t\t\t\t\t\t</li>\n" +
                    "\t\t\t\t\t\t\t\t</a>\n" +
                    "\t\t\t\t\t\t\t</ul>\n" +
                    "\t\t\t\t\t\t</div>\n" +
                    "\t\t\t\t\t</div>\n" +
                    "\t\t\t\t</div>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t\t<div class=\"bottom\">\n" +
                    "\t\t\t\t<div class=\"bottom_xt\"></div>\n" +
                    "\t\t\t\t<div class=\"bottom_con\">\n" +
                    "\t\t\t\t\t<div class=\"bottom_xgx\">\n" +
                    "\t\t\t\t\t\t<p class=\"yxlj\">\n" +
                    "\t\t\t\t\t\t\t友情链接 ：<a href=\"#\">中国伊斯兰教协会</a>\n" +
                    "\t\t\t\t\t\t\t<span>|</span>\n" +
                    "\t\t\t\t\t\t\t<a href=\"#\">宁夏回族自治区民族宗教事务委员会</a>\n" +
                    "\t\t\t\t\t\t</p>\n" +
                    "\t\t\t\t\t\t<p>版权所有：宁夏回族自治区民族事务委员会 宁ICP备18002660号-2 宁公网安备15010502001271号</p>\n" +
                    "\t\t\t\t\t</div>\n" +
                    "\t\t\t\t</div>\n" +
                    "\t\t\t</div>\n" +
                    "\t\t</div>\n" +
                    "\t</body>\n" +
                    "</html>";


            try {
                String pre="/";
                Document doc1 = Jsoup.parse(tmplContent);
                Elements imgs = doc1.select("img");
                for(int i=0;i<imgs.size();i++){
                    String src=imgs.eq(i).attr("src");
                    System.out.println("updateBusiTemplateDefRelativeAddr:"+i+":"+src);
                    if(src.indexOf("http://")>=0 ||src.indexOf("https://")>=0 || src.indexOf("pngbase64")>=0 || src.startsWith("..")){

                    }else if(!src.startsWith("/")){
                        doc1.select("img").eq(i).attr("src",pre+src);
                    }
                }

                Elements links = doc1.select("link");
                for(int i=0;i<links.size();i++){
                    String href=links.eq(i).attr("href");
                    System.out.println("updateBusiTemplateDefRelativeAddr:"+i+":"+href);
                    if(href.indexOf("http://")>=0 ||href.indexOf("https://")>=0 || href.indexOf("pngbase64")>=0 || href.startsWith("..")){

                    }else  if(!href.startsWith("/")){
                        doc1.select("link").eq(i).attr("href",pre+href);
                    }
                }


                Elements script = doc1.select("script");
                for(int i=0;i<script.size();i++){
                    String src=script.eq(i).attr("src");
                    System.out.println("updateBusiTemplateDefRelativeAddr:"+i+":"+src);
                    if(src.indexOf("http://")>=0 ||src.indexOf("https://")>=0 || src.indexOf("pngbase64")>=0 || src.startsWith("..")){

                    }else if(!src.startsWith("/")){
                        doc1.select("script").eq(i).attr("src",pre+src);
                    }
                }
                tmplContent  = doc1.toString();
                System.out.println(tmplContent);
            }catch (Exception e){
                e.printStackTrace();

            }



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

        String sql="Qaz@12345";//52c69e3a57331081823331c4e69d3f2e
        System.out.println(ObjectTools.md5(sql));
    }


    public String replaceDomainUrl(String localUrl,String domainUrl,String site_code){
        String returnUrl = localUrl;
        if(domainUrl != null && domainUrl.startsWith("http")){
            int idx = localUrl.indexOf(site_code)+site_code.length();
            String subStr = localUrl.substring(idx);
            returnUrl = domainUrl+subStr;
        }
        return returnUrl;
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

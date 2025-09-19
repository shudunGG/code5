package com.om.module.service.webService;

import com.om.module.core.base.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.HashMap;
import java.util.Map;


/**
 * @Title: WebServiceFor4AImpl.java
 * @Description: TODO()
 * @Author: liuxinjia  上午9:39
 * @Date: 2020年6月16日 09点39分
 */
@Service
@WebService(//serviceName = "WebServiceFor4A", // 与接口中指定的name一致
        serviceName = "UpdateAppAcctSoap",
        portName="UpdateAppAcctSoap",
        endpointInterface = "com.om.module.service.webService.WebServiceFor4A" // 接口地址
)
public class WebServiceFor4AImpl implements WebServiceFor4A {
    protected Logger logger = LoggerFactory.getLogger(WebServiceFor4AImpl.class);

    @Resource(name="baseService")
    public BaseService baseService;


    @Override
    public String updateAppAcctSoap(@WebParam(name = "RequestInfo",targetNamespace="UpdateAppAcctSoap")String RequestInfo) {
        logger.info("enter updateAppAcctSoap,xml:"+RequestInfo);
        String xml = RequestInfo;

        String returnXml = null;
        try {
            Map paramMap = resoleInputParam(xml);
            Map p = new HashMap();
            p.put("OP_NAME",paramMap.get("USERNAME"));
            p.put("account",paramMap.get("OPERATORID"));
            p.put("LINK_PHONE",paramMap.get("MOBILE"));
            p.put("BOSS_ACCT_ID",0);
            int opSts = 0;
            /*DynamicDataSource.setDataSource(DataSourcesName.DEFAULTDB);
            HashMap<String,Object> operInfo = this.baseService.get("baseLogin.queryLoginSystem",p);//验证管理员
            if(operInfo == null){
                logger.error("这里返回失败报文 login pwd error!");
                returnXml = returnPwdErrXml(paramMap);
                logger.error("response xml 60:"+returnXml);
                return returnXml;
            }

            p.put("login_name",paramMap.get("LOGINNO"));
            p.put("account",paramMap.get("LOGINNO"));
            p.put("ACCOUNT",paramMap.get("LOGINNO"));
            p.put("LOGIN_NAME",paramMap.get("LOGINNO"));
            p.put("ORG_ID_1",0);
            p.put("ORG_ID_2",0);
            p.put("ORG_ID_3",0);
            String modifyMode = paramMap.get("modifyMode").toString();
            logger.info("enter UpdateAppAcctSoap:modifyMode"+modifyMode);

            if("add".equals(modifyMode)){
                try {
                    Map loginMap = this.baseService.get("baseLogin.queryLoginSystemAll", p);
                    if(loginMap == null){
                        logger.error("user was exists!");
                        try{
                            //插入一条用户信息
                            this.baseService.insert("cfOrgMapper.insertJobNum", p);
                            logger.info("enter UpdateAppAcctSoap:saveOperator success");
                            //这里是插入角色的，先屏蔽掉
                            //this.insertInfo("loginMapper.saveSysOpRoleMap", p);
                        }catch(Exception e){
                            logger.error(e.getMessage(), e);
                        }
                    }else{
                        Map pp = new HashMap();
                        pp.put("STS",0);
                        this.baseService.update("cfOrgMapper.updateOperStsByLoginName", pp);

                    }
                    opSts = 0;

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw e;
                }
            }else if("delete".equals(modifyMode)){
                Map pp = new HashMap();
                pp.put("sts",-1);	//删除状态
                this.baseService.update("cfOrgMapper.updateOperStsByLoginName", pp);
                opSts = 0;

            }else if("change".equals(modifyMode)){
//                PDto pp = new PDto();
//                pp.put("op_name",paramMap.get("USERNAME"));
//                pp.put("phone",paramMap.get("MOBILE"));
//                pp.put("pwd",paramMap.get("PASSWORD"));
//                pp.put("login_name",paramMap.get("LOGINNO"));
//                this.insertInfo("loginMapper.updateOperator", pp);
                opSts = 0;

            }else if("resetpwd".equals(modifyMode)){
//                PDto pp = new PDto();
//                pp.put("pwd","21218cca77804d2ba1922c33e0151105");
//                pp.put("login_name",paramMap.get("LOGINNO"));
//                this.insertInfo("loginMapper.updateOperator", pp);
                opSts = 0;
            }else if("chgstatus".equals(modifyMode)){
                String STATUS  = paramMap.get("LOGINNO").toString();
                Map pp = new HashMap();
                if("1".equals(STATUS)){
                    pp.put("sts","0");
                }else{
                    pp.put("sts","-1");//锁定状态
                }
                this.baseService.update("cfOrgMapper.updateOperStsByLoginName", pp);
                opSts = 0;
            }*/

            String desc ="操作失败";
            if(opSts  == 0){
                desc ="操作成功!";
            }

            returnXml = this.returnProcXml(paramMap, opSts,desc);

        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
        logger.error("response xml 95:"+returnXml);
        return returnXml;
    }

    /*
    <?xml version='1.0' encoding='UTF-8'?>
	<USERMODIFYREQ>
		<HEAD>
			<CODE>消息标志</CODE>
			<SID>消息序列号</SID>
			<TIMESTAMP>时间戳</TIMESTAMP>
			<SERVICEID>应用标识</SERVICEID>
		</HEAD>
		<BODY>
			<OPERATORID>管理员从帐号</OPERATORID>
			<OPERATORPWD>管理员密码</OPERATORPWD>
			<OPERATORIP>操作员客户端IP</OPERATORIP>
			<MODIFYMODE>add、delete、change、chgstatus、resetpwd</MODIFYMODE>
			<USERINFO>
				<!—帐号主属性：公共属性-->
				<USERID>用户标识</USERID>
				<LOGINNO>从帐号登录名</LOGINNO>
				<USERNAME>人员姓名</USERNAME>
				<ORGID>组织机构标识</ORGID>
				<EMAIL>电子邮件</EMAIL>
				<MOBILE>手机号</MOBILE>//对应TELENO
				<PASSWORD>帐号密码</PASSWORD>
				<STATUS>帐号状态</STATUS>
				<EFFECTDATE>生效时间</EFFECTDATE>
				<EXPIREDATE>失效时间</EXPIREDATE>
				<REMARK>帐号描述</REMARK>
				<!—帐号主属性：公共属性-->
				<!—帐号扩展属性：各省个性化属性-->
				<DUTY>职位</DUTY>
				<OPERTYPE>操作员类型</OPERTYPE>
				<OPERLEVEL>操作员级别</OPERLEVEL>
				<DUTYLEVEL>业务级别</DUTYLEVEL>
				<!—帐号扩展属性：各省个性化属性-->
			<USERINFO>
		</BODY>
	</USERMODIFYREQ>
     */

    /**
     * 解析输入的参数到Map中
     * @param xml
     * @return {USERID,LOGINNO,USERNAME,MOBILE,PASSWORD,STATUS,EFFECTDATE,EXPIREDATE,REMARK }
     * @throws Exception
     */
    private Map resoleInputParam(String xml)throws Exception{
        Map rs = new HashMap();
        Document doc = null;
        try{
            doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            Element head = root.element("HEAD");
            Element body = root.element("BODY");
            Element OPERATORID = body.element("OPERATORID");
            Element OPERATORPWD = body.element("OPERATORPWD");
            Element MODIFYMODE = body.element("MODIFYMODE");
            rs.put("headElement", head);

            rs.put("OPERATORID", OPERATORID.getTextTrim());
            rs.put("OPERATORPWD", OPERATORPWD.getTextTrim());
            rs.put("modifyMode", MODIFYMODE.getTextTrim());

            Element USERINFO = body.element("USERINFO");
            String USERID = USERINFO.elementTextTrim("USERID");
            String LOGINNO = USERINFO.elementTextTrim("LOGINNO");
            String USERNAME = USERINFO.elementTextTrim("USERNAME");
            String MOBILE = USERINFO.elementTextTrim("MOBILE");
            String PASSWORD = USERINFO.elementTextTrim("PASSWORD");
            String STATUS = USERINFO.elementTextTrim("STATUS");
            String EFFECTDATE = USERINFO.elementTextTrim("EFFECTDATE");
            String EXPIREDATE = USERINFO.elementTextTrim("EXPIREDATE");
            String REMARK = USERINFO.elementTextTrim("REMARK");

            rs.put("USERID", USERID);
            rs.put("LOGINNO", LOGINNO);
            rs.put("USERNAME", USERNAME);
            rs.put("MOBILE", MOBILE);
            rs.put("PASSWORD", PASSWORD);
            rs.put("STATUS", STATUS);
            rs.put("EFFECTDATE", EFFECTDATE);
            rs.put("EXPIREDATE", EXPIREDATE);
            rs.put("REMARK", REMARK);


        }catch(Exception e){
            logger.error(e.getMessage(), e);
            throw e;
        }

        return rs;
    }

    private String returnPwdErrXml(Map rsMap){
        Element head = (Element)rsMap.get("headElement");
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version='1.0' encoding='UTF-8'?><USERMODIFYRSP>");
        sb.append(head.asXML());
        sb.append("<BODY><KEY>"+rsMap.get("OPERATORID")+"</KEY><ERRCODE></ERRCODE><ERRDESC>账号不存在或密码错误!</ERRDESC></BODY></USERMODIFYRSP>");

        return sb.toString();
    }

    private String returnProcXml(Map rsMap,int opSts,String desc){
        Element head = (Element)rsMap.get("headElement");
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version='1.0' encoding='UTF-8'?><USERMODIFYRSP>");
        sb.append(head.asXML());
        sb.append("<BODY><MODIFYMODE>"+rsMap.get("modifyMode")+"</MODIFYMODE><USERID>"+rsMap.get("USERID")+"</USERID><LOGINNO>"
                +rsMap.get("LOGINNO")+"</LOGINNO><RSP>"+opSts+"</RSP> <ERRDESC></ERRDESC></BODY></USERMODIFYRSP>");
        return sb.toString();
    }
}
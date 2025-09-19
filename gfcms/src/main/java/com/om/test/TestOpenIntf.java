package com.om.test;

import com.alibaba.fastjson.JSONObject;
import com.om.common.util.HttpInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestOpenIntf {
    public static void main(String[] args){

        List list3 = new ArrayList();
        list3.add("2");
        list3.add("3");
        list3.add("4");

        for(int i=list3.size()-1;i>-1;i--){
            String s = list3.get(i).toString();
            System.out.println(s);
        }
        if(2>1) return;

        //查询接口
        System.out.println("开始查询接口");
        HttpInterface intf = new HttpInterface();
        HashMap busiInfo = new HashMap();
        busiInfo.put("TASK_DTL_ID_LIST","1,2,3,4");
        busiInfo.put("EXT_PARAM","");

        HashMap rs = new HashMap();
        rs.put("BUSI_INFO",busiInfo);

        String p = JSONObject.toJSONString(rs);
        System.out.println("入参："+p);
        String resp  = null;
        try {
            resp = intf.postRequest("http://127.0.0.1:9444/cfIntf/open/queryCfSyncResult",p,30000);
           // resp = intf.postData("http://127.0.0.1:9444/cfIntf/open/queryCfSyncResult?a=1&b=2",p,null);
            System.out.println("========getToken"+resp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(2>1)return;

        System.out.println("开始新增接口");
        List list  = new ArrayList();
        HashMap a = new HashMap();
        a.put("PREVIEW_ID",22900);
        a.put("SQL_TYPE",1);
        a.put("PRE_SQL","insert into prod.pm_offer_cha_val (REL_ID, OFFER_CHA_SPEC_REL_ID, CHA_SPEC_VAL_ID, VALUE, VALID_DATE, EXPIRE_DATE, DATA_STATUS, DONE_CODE, CREATE_DATE, CREATE_OP_ID, CREATE_ORG_ID, DONE_DATE, OP_ID, ORG_ID, MGMT_DISTRICT, MGMT_COUNTY, REGION_ID)\n" +
                "\t\tvalues (780200001205, 780200002991, 2190074001, '2', trunc(sysdate,'mm'), to_date('2099-12-31', 'yyyy-mm-dd'), '1', null, sysdate, null, null, sysdate, null, null, null, null, 'ZZZZ')");
        a.put("ALIAS_NAME","OfferPmOfferRel");
        a.put("DB_NAME","crmpb");
        a.put("PARAMS","");
        a.put("SEQ",1);
        a.put("SCHEMA","prod");
        list.add(a);

        a = new HashMap();
        a.put("PREVIEW_ID",22903);
        a.put("SQL_TYPE",1);
        a.put("PRE_SQL","insert into prod.PM_OFFER_CHA(\n" +
                "\t\tOFFER_CHA_SPEC_REL_ID,OFFER_ID,SPEC_CODE,SPEC_ID,DOC_TABLE_NAME,\n" +
                "\t\tDOC_COLUMN_NAME,CHA_SPEC_ID,SEQ,OPERATION_MODE,VALUE_TYPE,\n" +
                "\t\tIS_CUSTOMIZED,DEFAULT_VALUE,IS_NULL,IS_EDIT,IS_INST,\n" +
                "\t\tIS_PROVISIONING,IS_BILLING,IS_CBOSS,VALID_DATE,EXPIRE_DATE,\n" +
                "\t\tDATA_STATUS,DONE_CODE,CREATE_DATE,CREATE_OP_ID,CREATE_ORG_ID,DONE_DATE,OP_ID,ORG_ID,MGMT_DISTRICT,MGMT_COUNTY,REGION_ID)\n" +
                "\t\tvalues (\n" +
                "\t\t780200002991,101090400108,'PMIndivFreeLookSpec',2300000166,'UM_OFFER_CHA',\n" +
                "\t\t'',219000210,'','1','1',\n" +
                "\t\t1,'1722500205',0,0,1,\n" +
                "\t\t0,0,0,trunc(sysdate,'mm'),to_date('2099-12-31', 'yyyy-mm-dd'),\n" +
                "\t\t1,'',sysdate,'','',sysdate,null,null,null,null,'ZZZZ')");
        a.put("ALIAS_NAME","OfferPmOfferRel");
        a.put("DB_NAME","crmpb");
        a.put("PARAMS","");
        a.put("SEQ",2);
        a.put("SCHEMA","prod");
        list.add(a);

        busiInfo = new HashMap();
        busiInfo.put("TASK_DTL_ID","247");
        busiInfo.put("TASK_ID","24");
        busiInfo.put("TMPL_CODE","201");
        busiInfo.put("OFFER_NAME","test OFFER_NAME");
        busiInfo.put("OFFER_ID","101030402342");
        busiInfo.put("BUSI_CODE","200");
        busiInfo.put("EXT_PARAM","");
        busiInfo.put("SQL_LIST",list);

        rs = new HashMap();
        rs.put("BUSI_INFO",busiInfo);

        p = JSONObject.toJSONString(rs);
        resp  = null;
        try {
            resp = intf.postRequest("http://127.0.0.1:9444/cfIntf/open/syncCfScriptRecord",p,30000);
            System.out.println("========getToken"+resp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}

package com.om.module.service.app;

import com.om.common.cache.Dict;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("MailMsgManagerService")
public class MailMsgManagerService extends CommonService {

    public void saveBusiAppMailMsg(Map param) throws Exception {
        printParam(param,"saveBusiAppMailMsg==:");
        String SITE_PK = (String)param.get("SITE_PK");
        String BOX_NAME = (String)param.get("BOX_NAME");
        String M_TITLE = (String)param.get("M_TITLE");
        String M_CONTENT = (String)param.get("M_CONTENT");
        String LINK_MAN = (String)param.get("LINK_MAN");
        String LINK_PHONE = (String)param.get("LINK_PHONE");
        String IS_OPEN = (String)param.get("IS_OPEN");

        this.isNull("SITE_PK",SITE_PK);
        this.isNull("BOX_NAME",BOX_NAME);
        this.isNull("M_TITLE",M_TITLE);
        this.isNull("M_CONTENT",M_CONTENT);
        this.isNull("LINK_MAN",LINK_MAN);
        this.isNull("LINK_PHONE",LINK_PHONE);
        if(IS_OPEN == null){
            IS_OPEN="0";
        }
        String uuid = Pk.getId("M");
        param.put("MSG_PK",uuid);
        this.baseService.insert("busiMapper.saveBusiAppMailMsg",param);
    }

    public void deleteBusiAppMailMsg(Map param) throws Exception {
        printParam(param,"deleteBusiAppMailMsg==:");
        String MSG_PK = (String)param.get("MSG_PK");
        this.isNull("MSG_PK",MSG_PK);
        this.baseService.insert("busiMapper.deleteBusiAppMailMsg",param);
    }

    /**
     * 修改留言内容
     * @param param
     * @throws Exception
     */
    public void updateBusiAppMailMsg(Map param) throws Exception {
        printParam(param,"updateBusiAppMailMsg==:");
        String MSG_PK = (String)param.get("MSG_PK");
        String M_TITLE = (String)param.get("M_TITLE");
        String M_CONTENT = (String)param.get("M_CONTENT");
        String IS_OPEN = (String)param.get("IS_OPEN");
        this.isNull("MSG_PK",MSG_PK);
        this.baseService.insert("busiMapper.updateBusiAppMailMsg",param);
    }

    /**
     * 审核留言内容
     * @param param
     * @throws Exception
     */
    public void auditBusiAppMailMsg(Map param) throws Exception {
        printParam(param,"auditBusiAppMailMsg==:");
        String MSG_PK = (String)param.get("MSG_PK");
        String AUDIT_STS = (String)param.get("AUDIT_STS");
        String USER_ID = (String)param.get("USER_ID");
        String USER_NAME = (String)param.get("USER_NAME");
        String AUDIT_OPINION = (String)param.get("AUDIT_OPINION");
        this.isNull("MSG_PK",MSG_PK);
        this.isNull("USER_ID",USER_ID);
        this.isNull("USER_NAME",USER_NAME);
        param.put("AUDIT_USER",USER_ID);
        param.put("AUDIT_USER_NAME",USER_NAME);

        if("1".equals(AUDIT_STS)){
            param.put("STS",1);
        }else{
            param.put("STS",-1);
        }
        this.baseService.insert("busiMapper.updateBusiAppMailMsg",param);
    }

    /**
     * 回复留言内容
     * @param param
     * @throws Exception
     */
    public void replyBusiAppMailMsg(Map param) throws Exception {
        printParam(param,"replyBusiAppMailMsg==:");
        String MSG_PK = (String)param.get("MSG_PK");
        String USER_ID = (String)param.get("USER_ID");
        String USER_NAME = (String)param.get("USER_NAME");
        String REPLY_CONTENT = (String)param.get("REPLY_CONTENT");
        this.isNull("MSG_PK",MSG_PK);
        this.isNull("USER_ID",USER_ID);
        this.isNull("USER_NAME",USER_NAME);
        param.put("REPLY_USER",USER_ID);
        param.put("REPLY_USER_NAME",USER_NAME);

        if(REPLY_CONTENT != null){
            param.put("STS",2);
        }
        this.baseService.insert("busiMapper.updateBusiAppMailMsg",param);
    }

    /**
     * 分页查询留言
     * @param param
     * @return
     * @throws Exception
     */
    public List queryBusiAppMailMsg(Map param) throws Exception {
        setSplitPageParam(param);
        Map map = (Map)this.baseService.getObject("busiMapper.queryBusiAppMailMsgTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("busiMapper"+ Dict.dbMap+".queryBusiAppMailMsg",param);
        return list;
    }

}

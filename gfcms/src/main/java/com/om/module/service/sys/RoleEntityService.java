package com.om.module.service.sys;
import com.om.bo.base.Const;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("RoleEntityService")
public class RoleEntityService extends CommonService {

    public void saveSysRoleDef(Map param) throws Exception {
        String ROLE_NAME = (String)param.get("ROLE_NAME");
        this.isNull("ROLE_NAME",ROLE_NAME);
        String ROLE_ID = Pk.getId("R");
        param.put("ROLE_ID",ROLE_ID);
        this.baseService.insert("sysMapper.saveSysRoleDef",param);

    }


    public void updateSysRoleDefSts(Map param) throws Exception {
        String ROLE_ID = (String)param.get("ROLE_ID");
        String ROLE_STS = (String)param.get("ROLE_STS");

        this.isNull("ROLE_ID",ROLE_ID);
        this.isNull("ROLE_STS",ROLE_STS);
        this.isEnum("ROLE_STS",ROLE_STS,"1,-1");

        this.baseService.insert("sysMapper.updateSysRoleDef",param);
    }

    public void deleteSysRoleDef(Map param) throws Exception {
        String ROLE_ID = (String)param.get("ROLE_ID");
        this.isNull("ROLE_ID",ROLE_ID);
        this.baseService.insert("sysMapper.deleteSysRoleDef",param);
        this.baseService.insert("sysMapper.deleteSysRoleEntityMapByRoleId",param);
        this.baseService.insert("sysMapper.deleteSysOpRoleMapByRoleId",param);

    }


    public void updateSysRoleDef(Map param) throws Exception {
        String ROLE_ID = (String)param.get("ROLE_ID");
        this.isNull("ROLE_ID",ROLE_ID);
        this.baseService.insert("sysMapper.updateSysRoleDef",param);
    }

    public List querySysRoleDef(Map param) throws Exception {
        List list = this.baseService.getList("sysMapper.querySysRoleDef",param);
        return list;
    }

    public List querySysEntityDef(Map param,Map userMap) throws Exception {
        String USER_ID = (String)param.get("USER_ID");
        this.isNull("USER_ID",USER_ID);
        if(userMap == null){
            userMap = (Map) this.baseService.getObject("sysMapper.querySysUserDef", param);
        }
        String IS_ADMIN = userMap.get("IS_ADMIN").toString();
        List list = null;
        if("1".equals(IS_ADMIN)){//表示是超级管理员
            list = this.baseService.getList("sysMapper.querySysEntityDef",param);
        }else{//需要关联权限查询
            list = this.baseService.getList("sysMapper.querySysEntityListByUserId",param);
        }
        return list;
    }

    public List querySysEntityListByRoleId(Map param) throws Exception {
        String ROLE_ID = (String)param.get("ROLE_ID");
        this.isNull("ROLE_ID",ROLE_ID);
        List list = this.baseService.getList("sysMapper.querySysEntityListByRoleId",param);
        return list;
    }

    public void updateSysRoleEntityMapBat(Map param) throws Exception {
        String ROLE_ID = (String)param.get("ROLE_ID");
        String ENTITY_ID_LIST = (String)param.get("ENTITY_ID_LIST");
        this.isNull("ROLE_ID",ROLE_ID);
        this.isNull("ENTITY_ID_LIST",ENTITY_ID_LIST);
        String[] entityArr = ENTITY_ID_LIST.split(",");
        List paramList = new ArrayList();
        Map m = null;
        for(String entityId:entityArr){
            m = new HashMap();
            m.put("ROLE_ID",ROLE_ID);
            m.put("ENTITY_ID",entityId);
            paramList.add(m);
        }
        this.baseService.insert("sysMapper.deleteSysRoleEntityMapByRoleId",param);
        this.baseService.insert("sysMapper.saveSysRoleEntityMapBat",paramList);
    }



}

package com.om.module.service.sys;
import com.om.common.cache.Dict;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("UserService")
public class UserService extends CommonService {

    public void saveSysUserDef(Map param) throws Exception {
        String LOGIN_NAME = (String)param.get("LOGIN_NAME");
        String DEPT_CODE = (String)param.get("DEPT_CODE");
        String LOGIN_PWD = (String)param.get("LOGIN_PWD");
        String REAL_NAME = (String)param.get("REAL_NAME");

        Map m = new HashMap();
        m.put("LOGIN_NAME",LOGIN_NAME);
        List list = this.querySysUserDef(m);
        if(list.size()>0){
            throw new Exception("LOGIN_NAME["+LOGIN_NAME+"]已经存在，无法添加！");
        }

        this.isNull("LOGIN_NAME",LOGIN_NAME);
        String USER_ID = Pk.getId("U");
        param.put("USER_ID",USER_ID);
        param.put("DEPT_CODE",DEPT_CODE);
        this.baseService.insert("sysMapper.saveSysUserDef",param);
    }

    public void deleteSysUserDef(Map param) throws Exception {
        String USER_ID = (String)param.get("USER_ID");
        this.isNull("USER_ID",USER_ID);
        this.baseService.insert("sysMapper.deleteSysUserDef",param);
        this.baseService.insert("sysMapper.deleteSysOpRoleMapByUserId",param);
    }

    public void updateSysUserDef(Map param) throws Exception {
        String USER_ID = (String)param.get("USER_ID");
        this.isNull("USER_ID",USER_ID);
        this.baseService.insert("sysMapper.updateSysUserDef",param);
    }

    public List querySysUserDef(Map param) throws Exception {
        List list = this.baseService.getList("sysMapper.querySysUserDef",param);
        return list;
    }

    public List querySysRoleListByUserId(Map param) throws Exception {
        String USER_ID = (String)param.get("USER_ID");
        this.isNull("USER_ID",USER_ID);
        List list = this.baseService.getList("sysMapper.querySysRoleListByUserId",param);
        return list;
    }

    public void updateSysOpRoleMapBat(Map param) throws Exception {
        String USER_ID = (String)param.get("USER_ID");
        String ROLE_ID_LIST = (String)param.get("ROLE_ID_LIST");
        this.isNull("USER_ID",USER_ID);
        this.isNull("ROLE_ID_LIST",ROLE_ID_LIST);
        String[] entityArr = ROLE_ID_LIST.split(",");
        List paramList = new ArrayList();
        Map m = null;
        for(String entityId:entityArr){
            m = new HashMap();
            m.put("USER_ID",USER_ID);
            m.put("ROLE_ID",entityId);
            paramList.add(m);
        }
        this.baseService.insert("sysMapper.deleteSysOpRoleMapByUserId",param);
        this.baseService.insert("sysMapper.saveSysOpRoleMapBat",paramList);
    }

    /**
     * 更新用户的数据范围
     * @param param
     * @throws Exception
     */
    public void updateSysOpDataScaleMapBat(Map param) throws Exception {
        String USER_ID = (String)param.get("USER_ID");
        String SCALE_ID_LIST = (String)param.get("SCALE_ID_LIST");
        logger.debug("updateSysOpDataScaleMapBat:SCALE_ID_LIST:"+SCALE_ID_LIST);
        this.isNull("USER_ID",USER_ID);
        this.isNull("SCALE_ID_LIST",SCALE_ID_LIST);
        String[] scaleArr = SCALE_ID_LIST.split(",");
        List paramList = new ArrayList();
        Map m = null;
        for(String scale:scaleArr){
            String[] pkArr = scale.split("_");
            String classPk = Dict.All;
            String sitePk = Dict.All;
            String channelPk = Dict.All;
            if(pkArr.length == 1){
                classPk = pkArr[0];
            }else if(pkArr.length == 2){
                classPk = pkArr[0];
                sitePk = pkArr[1];
            }else if(pkArr.length == 3){
                classPk = pkArr[0];
                sitePk = pkArr[1];
                channelPk = pkArr[2];
            }
            m = new HashMap();
            m.put("USER_ID",USER_ID);
            m.put("CLASS_PK",classPk);
            m.put("SITE_PK",sitePk);
            m.put("CHANNEL_PK",channelPk);
            paramList.add(m);
        }
        this.baseService.insert("sysMapper.deleteSysOpDataScaleByOpId",param);
        this.baseService.insert("sysMapper.saveSysOpDataScaleBat",paramList);
    }


}

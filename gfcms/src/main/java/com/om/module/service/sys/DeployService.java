package com.om.module.service.sys;
import com.om.common.cache.Dict;
import com.om.common.util.Pk;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("DeployService")
public class DeployService extends CommonService {

    public void saveSysConfDeploy(Map param) throws Exception {
        String DEPLOY_NAME = (String)param.get("DEPLOY_NAME");
        String DEPLOY_MODE = (String)param.get("DEPLOY_MODE");
        String URL_ROOT = (String)param.get("URL_ROOT");
        String SITE_PK = (String)param.get("SITE_PK");

        String SOURCE_ROOT = (String)param.get("SOURCE_ROOT");
        String TARGET_ROOT = (String)param.get("TARGET_ROOT");

        String SFTP_HOST = (String)param.get("SFTP_HOST");
        String SFTP_USER = (String)param.get("SFTP_USER");
        String SFTP_PWD = (String)param.get("SFTP_PWD");
        String SFTP_PORT = (String)param.get("SFTP_PORT");
        String SFTP_ROOT = (String)param.get("SFTP_ROOT");

        this.isNull("DEPLOY_NAME",DEPLOY_NAME);
        this.isNull("DEPLOY_MODE",DEPLOY_MODE);
        this.isNull("SITE_PK",SITE_PK);
        this.isNull("URL_ROOT",URL_ROOT);
        this.isNull("SOURCE_ROOT",SOURCE_ROOT);

        if(DEPLOY_MODE.equals(Dict.DeployMode.file)){
            this.isNull("TARGET_ROOT",TARGET_ROOT);
        }else if(DEPLOY_MODE.equals(Dict.DeployMode.sftp)){
            this.isNull("SFTP_HOST",SFTP_HOST);
            this.isNull("SFTP_USER",SFTP_USER);
            this.isNull("SFTP_PWD",SFTP_PWD);
            this.isNull("SFTP_PORT",SFTP_PORT);
        }
        this.baseService.insert("sysMapper.saveSysConfDeploy",param);
    }

    public void deleteSysConfDeploy(Map param) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("SITE_PK",SITE_PK);
        this.baseService.insert("sysMapper.deleteSysConfDeploy",param);
    }

    public void updateSysConfDeploy(Map param) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("SITE_PK",SITE_PK);
        this.baseService.insert("sysMapper.updateSysConfDeploy",param);
    }

    public List querySysConfDeploy(Map param) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        String DEPLOY_NAME = (String)param.get("DEPLOY_NAME");
        String SFTP_HOST = (String)param.get("SFTP_HOST");
        List list = this.baseService.getList("sysMapper.querySysConfDeploy",param);
        return list;
    }

    /**
     * 根据站点PK，查询得到站点的部署配置信息
     * @param param
     * @return
     * @throws Exception
     */
    public Map querySysConfDeployBySitePk(Map param) throws Exception {
        String SITE_PK = (String)param.get("SITE_PK");
        this.isNull("SITE_PK",SITE_PK);
        List list = querySysConfDeploy(param);
        if(list.size()>0){
            return (Map)list.get(0);
        }
        return null;
    }

}

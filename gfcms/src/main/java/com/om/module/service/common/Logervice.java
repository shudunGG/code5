package com.om.module.service.common;
import com.om.bo.base.FtpInfo;
import com.om.common.cache.Dict;
import com.om.common.util.ObjectTools;
import com.om.common.util.Pk;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service("Logervice")
public class Logervice extends CommonService {

    /**
     * 写入日志
     * @param MODULE
     * @param OP_TYPE
     * @param USER_ID
     * @param IP
     * @param PARAM
     * @throws Exception
     */
    public void insertLog(String MODULE,String OP_TYPE,String USER_ID,String IP,String PARAM,int isErr) throws Exception {
        this.isNull("MODULE",MODULE);
        this.isNull("OP_TYPE",OP_TYPE);
        //this.isNull("USER_ID",USER_ID);
        Map p =new HashMap();
        p.put("MODULE",MODULE);
        p.put("OP_TYPE",OP_TYPE);
        p.put("USER_ID",USER_ID);
        p.put("IP",IP);
        p.put("PARAM",PARAM);
        p.put("IS_ERR",isErr);
        this.baseService.insert("commonMapper.saveCmLog",p);
    }

    /**
     * 查询日志
     * @param param
     * @return
     * @throws Exception
     */
    public List queryCmLog(Map param) throws Exception {
        setSplitPageParam(param);

        printParam(param,"queryCmLogManager");

        Map map = (Map)this.baseService.getObject("commonMapper.queryCmLogTotal",param);
        param.put("TOTAL",map.get("TOTAL"));
        List list = this.baseService.getList("commonMapper"+Dict.dbMap+".queryCmLog",param);
        return list;
    }




}

package com.om.module.service.testService;



import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("TestOpService")
public class TestOpService extends CommonService {


    public List showJobNumService(Map param) {
        List list = this.baseService.getList("testMapper.querySysUserDef",param);
        return list;
    }


}

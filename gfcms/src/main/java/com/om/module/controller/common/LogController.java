package com.om.module.controller.common;

import com.om.bo.base.Const;
import com.om.bo.base.FtpInfo;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.common.Logervice;
import com.om.module.service.common.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

//@RestController
@Controller
@RequestMapping("/common")
//@CrossOrigin 这个注解是解决跨域问题，这里不需要
public class LogController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(LogController.class);


    @Resource(name = "Logervice")
    private Logervice service;

    /**
     * 查询日志
     * @return
     */
    @RequestMapping("/queryCmLog")
    public @ResponseBody HashMap<String,Object> queryCmLog() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);


        HashMap rs = new HashMap();
        try {
            List list = this.service.queryCmLog(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.TOTAL,params.get("TOTAL"));
            rs.put(Const.RESP_LIST,list);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


}

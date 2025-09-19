package com.om.module.controller.sys;

import com.om.bo.base.Const;
import com.om.common.util.RequestUtl;
import com.om.module.controller.base.BaseController;
import com.om.module.service.sys.RoleEntityService;
import com.om.module.service.sys.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/sys")
public class UserManagerController extends BaseController {
    protected Logger logger = LoggerFactory.getLogger(UserManagerController.class);
    @Resource(name = "UserService")
    private UserService service;


    /**
     * 新增用户
     * @return
     */
    @RequestMapping("/saveSysUserDef")
    public @ResponseBody HashMap<String,Object> saveSysUserDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.saveSysUserDef(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 删除用户
     * @return
     */
    @RequestMapping("/deleteSysUserDef")
    public @ResponseBody HashMap<String,Object> deleteSysUserDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.deleteSysUserDef(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 修改用户信息
     * @return
     */
    @RequestMapping("/updateSysUserDef")
    public @ResponseBody HashMap<String,Object> updateSysUserDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateSysUserDef(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 查询用户
     * @return
     */
    @RequestMapping("/querySysUserDef")
    public @ResponseBody HashMap<String,Object> querySysUserDef() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.querySysUserDef(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_LIST,list);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }


    /**
     * 查询用户的角色列表
     * @return
     */
    @RequestMapping("/querySysRoleListByUserId")
    public @ResponseBody HashMap<String,Object> querySysRoleListByUserId() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);
        HashMap rs = new HashMap();
        try {
            List list = this.service.querySysRoleListByUserId(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
            rs.put(Const.RESP_LIST,list);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }



    /**
     * 更新用户的角色关系
     * @return
     */
    @RequestMapping("/updateSysOpRoleMapBat")
    public @ResponseBody HashMap<String,Object> updateSysOpRoleMapBat() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateSysOpRoleMapBat(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }

    /**
     * 更新用户的数据范围
     * @return
     */
    @RequestMapping("/updateSysOpDataScaleMapBat")
    public @ResponseBody HashMap<String,Object> updateSysOpDataScaleMapBat() {
        HashMap<String,Object> params = RequestUtl.getRequestMap(request);

        HashMap rs = new HashMap();
        try {
            this.service.updateSysOpDataScaleMapBat(params);
            rs.put(Const.RESP_CODE,Const.SuccCode);
            rs.put(Const.RESP_MSG,Const.SUCC);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            rs.put(Const.RESP_CODE,Const.ErrCode);
            rs.put(Const.RESP_MSG,e.getMessage());
            rs.put(Const.RESP_EXCEPTION,this.getStackTrace(e));
        }
        return rs;
    }
}

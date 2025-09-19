package com.om.module.service.sys;
import com.om.bo.base.Const;
import com.om.module.service.common.CommonService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("LoginService")
public class LoginService extends CommonService {
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public Map loginSystem(Map param, HttpServletRequest request) throws Exception {
        String LOGIN_NAME = (String)param.get("LOGIN_NAME");
        String LOGIN_PWD = (String)param.get("LOGIN_PWD");
        this.isNull("LOGIN_NAME",LOGIN_NAME);
        this.isNull("LOGIN_PWD",LOGIN_PWD);
        Map userMap = null;
        try {
            userMap = (Map) this.baseService.getObject("sysMapper.querySysUserDef", param);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception("账号不存在或者密码错误，请重试！");
        }
        String DB_PWD = (String)userMap.get("LOGIN_PWD");

        /*Date lockTime = (Date)userMap.get("LOCK_TIME");
        int RETRY_COUNT = Integer.parseInt(userMap.get("RETRY_COUNT").toString());
        Date nowTime = new Date();
        if(lockTime!=null) {
            if (nowTime.before(lockTime)) {
                String timeStr = sdf.format(lockTime);
                throw new Exception("账号被锁定，锁定时间截止点为：" + timeStr);
            }
        }*/
        Map m = new HashMap();
        m.put("USER_ID",userMap.get("USER_ID"));
        if(LOGIN_PWD.equals(DB_PWD)){
            logger.info("登录成功！");
            m.put("UNFREEZE",1);
            request.getSession().setAttribute(Const.curOper,userMap);
            this.baseService.insert("sysMapper.updateSysUserDef",m);
        }else{
            /*RETRY_COUNT++;
            m.put("RETRY_COUNT",RETRY_COUNT);
            if(RETRY_COUNT > 5){
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE,5);
                m.put("LOCK_TIME",cal.getTime());
            }
            this.baseService.insert("sysMapper.updateSysUserDef",m);*/
            throw new Exception("账号不存在或者密码错误，请重试！");
        }
        return userMap;
    }

    /**
     * 根据用户名获取该用户应该能看到的menuList
     * @param param
     * @return
     * @throws Exception
     */
    public List querySysMenuListByLoginName(Map param) throws Exception {
        String LOGIN_NAME = (String)param.get("LOGIN_NAME");
        String SUB_SYS = (String)param.get("SUB_SYS");
        this.isNull("LOGIN_NAME",LOGIN_NAME);
        this.isNull("SUB_SYS",SUB_SYS);

        List list = this.baseService.getList("sysMapper.querySysMenuListByLoginName",param);
        return list;
    }


}

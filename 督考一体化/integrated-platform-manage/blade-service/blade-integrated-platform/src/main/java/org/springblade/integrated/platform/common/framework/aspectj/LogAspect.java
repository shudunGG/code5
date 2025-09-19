package org.springblade.integrated.platform.common.framework.aspectj;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.*;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.integrated.platform.common.project.monitor.operlog.service.IOperLogService;
import org.springblade.integrated.platform.common.utils.*;
import org.springblade.integrated.platform.common.framework.aspectj.lang.annotation.Log;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessStatus;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.integrated.platform.common.utils.spring.SpringUtils;
import org.springblade.system.cache.SysCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 操作日志记录处理
 *
 * @Author JG🧸
 * @Create 2022/4/9 14:30
 */
@Aspect
@Component
public class LogAspect
{
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /** 排除敏感属性字段 */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

	@Autowired
	private IUserClient userClient;
    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult)
    {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e 异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e)
    {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult)
    {
        try
        {
            // 获取当前的用户
			User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
			String deptName = SysCache.getDeptName(Long.valueOf(currentUser.getDeptId())); //
            // *========数据库日志=========*//
            OperLog operLog = new OperLog();
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            String ip = IpUtils.getHostIp();
            operLog.setOperIp(ip);
            operLog.setOperUrl(ServletUtils.getRequest().getRequestURI());
            if (currentUser != null)
            {
				String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(currentUser.getRealName());
                operLog.setOperName(userNameDecrypt);
				operLog.setDeptName(deptName);
                if (StringUtils.isNotNull(currentUser.getDeptId())
                        && StringUtils.isNotEmpty(""))
                {
                    operLog.setDeptName(deptName);
                }

            }/*else {//else为测试用的，等获取登陆用户信息方法完善后删除
				operLog.setOperName(currentUser.getUserName());//currentUser.getUserName()
				operLog.setDeptName(deptName);//deptName
			}*/

            if (e != null)
            {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 保存数据库
			// 远程查询操作地点
			operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
			SpringUtils.getBean(IOperLogService.class).insertOperlog(operLog);
			//AsyncFactory.recordOper(operLog);//AsyncManager.me().execute();
        }
        catch (Exception exp)
        {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log 日志
     * @param operLog 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, OperLog operLog, Object jsonResult) throws Exception
    {
        // 设置action动作
        operLog.setBusinessType(log.businessType().ordinal());
        // 设置标题
        operLog.setTitle(log.title());
        // 设置操作人类别
        operLog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData())
        {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operLog);
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && StringUtils.isNotNull(jsonResult))
        {
			String jsonstr = JSONUtil.toJsonStr(jsonResult);
			JSONObject obj = JSONObject.parseObject(jsonstr);
			/*String businessTable = "";
			String businessId = "";
			try {
				businessTable = obj.getJSONObject("data").getString("businessTable");
				businessId = obj.getJSONObject("data").getString("businessId");
				operLog.setBusinessTable(businessTable);//AESUtil.decryptByKey(businessTable,AESUtil.DEFAULT_KEY)
				operLog.setBusinessId(Long.valueOf(businessId));//AESUtil.decryptByKey(businessId,AESUtil.DEFAULT_KEY))
			} catch (Exception e) {
				System.out.println("LogAspect:"+e.getMessage());
			}*/
			if (operLog.getBusinessType() != null) {
				operLog.setJsonResult(StringUtils.substring(JSONUtil.toJsonStr(jsonResult), 0, 2000));
			}
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param operLog
     * @param joinPoint
     */
    private void setRequestValue(JoinPoint joinPoint, OperLog operLog)
    {
        Map<String, String[]> map = ServletUtils.getRequest().getParameterMap();
        if (StringUtils.isNotEmpty(map))
        {
            String params = JSONObject.toJSONString(map, excludePropertyPreFilter());
			try {
				JSONObject obj = JSONObject.parseObject(params);
				String businessId = obj.getString("businessId");
				if (businessId != null) {
					operLog.setBusinessTable(businessId);
				}
			} catch (Exception exception) {
				System.out.println("LogAspect:"+exception.getMessage());
			}

            operLog.setOperParam(StringUtils.substring(params, 0, 2000));
        }
        else
        {
			String businessId = "";
			Object args = joinPoint.getArgs();
			Object[] ccc = (Object[]) args;
			if (getFieldValueByName("businessId",ccc[0])!=null) {
				businessId = getFieldValueByName("businessId",ccc[0]).toString();
			}
            if (StringUtils.isNotNull(args))
            {

				if (businessId == null || businessId == "") {
					try {
						List<Object> cccc = (List<Object>) ccc[0];
						businessId = (String) getFieldValueByName("businessId",cccc.get(0));
					} catch (Exception exception) {
						businessId ="";
						System.out.println(">>>>>>>>>>>>>>>>"+exception.getMessage());
					}
				}

				JSONArray jsonArray = JSONUtil.parseArray(args);
				if (jsonArray.size()>0) {
					String jsonstr = JSONUtil.toJsonPrettyStr(jsonArray);
					if (businessId != null && businessId != "") {
						operLog.setBusinessId(businessId.replace("[","").replace("]",""));
					}
					operLog.setOperParam(StringUtils.substring(jsonstr, 0, 2000));
				}

            }
        }
    }

	/**
	 * 根据属性名获取属性值
	 * */
	private Object getFieldValueByName(String fieldName, Object o) {
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String getter = "get" + firstLetter + fieldName.substring(1);
			Method method = o.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(o, new Object[] {});
			return value;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}


    /**
     * 忽略敏感属性
     */
    public PropertyPreFilters.MySimplePropertyPreFilter excludePropertyPreFilter()
    {
        return new PropertyPreFilters().addFilter().addExcludes(EXCLUDE_PROPERTIES);
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray)
    {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0)
        {
            for (Object o : paramsArray)
            {
                if (StringUtils.isNotNull(o) && !isFilterObject(o))
                {
                    try
                    {
                        Object jsonObj = JSONObject.toJSONString(o, excludePropertyPreFilter());
                        params += jsonObj.toString() + " ";
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        return params.trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o)
    {
        Class<?> clazz = o.getClass();
        if (clazz.isArray())
        {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        }
        else if (Collection.class.isAssignableFrom(clazz))
        {
            Collection collection = (Collection) o;
            for (Object value : collection)
            {
                return value instanceof MultipartFile;
            }
        }
        else if (Map.class.isAssignableFrom(clazz))
        {
            Map map = (Map) o;
            for (Object value : map.entrySet())
            {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
}

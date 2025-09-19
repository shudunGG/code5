package org.springblade.integrated.platform.common.project.monitor.operlog.service;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.integrated.platform.common.framework.aspectj.lang.enums.BusinessType;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;
import org.springblade.integrated.platform.common.project.monitor.operlog.mapper.OperLogMapper;
import org.springblade.integrated.platform.common.utils.AddressUtils;
import org.springblade.integrated.platform.common.utils.IpUtils;
import org.springblade.integrated.platform.common.utils.ServletUtils;
import org.springblade.integrated.platform.common.utils.text.Convert;
import org.springblade.system.cache.SysCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志 服务层处理
 *
 * @Author JG🧸
 * @Create 2022/4/13 13:35
 */
@Service
public class OperLogServiceImpl extends BaseServiceImpl<OperLogMapper, OperLog> implements IOperLogService
{

	@Autowired
	private IUserClient userClient;
    /**
     * 新增操作日志
     *
     * @param operLog 操作日志对象
     */
    @Override
    public void insertOperlog(OperLog operLog)
    {
		baseMapper.insertOperlog(operLog);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    @Override
    public List<OperLog> selectOperLogList(OperLog operLog)
    {
        return baseMapper.selectOperLogList(operLog);
    }

    /**
     * 批量删除系统操作日志
     *
     * @param ids 需要删除的数据
     * @return
     */
    @Override
    public int deleteOperLogByIds(String ids)
    {
        return baseMapper.deleteOperLogByIds(Convert.toStrArray(ids));
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    @Override
    public OperLog selectOperLogById(Long operId)
    {
        return baseMapper.selectOperLogById(operId);
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanOperLog()
    {
		baseMapper.cleanOperLog();
    }

	/**
	 * 记录业务日志
	 * @param title 标题
	 * @param businessId 业务id，一般是主键
	 * @param businessTable 实体类名
	 * @param businessType 业务类型，枚举类型
	 */
	public void saveLog(String title,String businessId,String businessTable,int businessType){
		//获取当前的用户
		User currentUser = userClient.userInfoById(AuthUtil.getUserId()).getData();
		String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(currentUser.getRealName());
		OperLog operlog = new OperLog();
		operlog.setTitle(title);
		operlog.setBusinessId(businessId);
		operlog.setBusinessTable(businessTable);
		operlog.setBusinessType(businessType);
		operlog.setStatus(0);
		operlog.setOperName(userNameDecrypt);
		String deptName = SysCache.getDeptName(Long.valueOf(currentUser.getDeptId()));
		operlog.setDeptName(deptName);
		// 获取ip
		String ip = IpUtils.getHostIp();
		operlog.setOperIp(ip);
		operlog.setOperUrl(ServletUtils.getRequest().getRequestURI());
		// 设置请求方式
		operlog.setRequestMethod(ServletUtils.getRequest().getMethod());
		operlog.setOperLocation(AddressUtils.getRealAddressByIP(operlog.getOperIp()));
		operlog.setOperTime(DateTime.now());
		super.save(operlog);
	}

}

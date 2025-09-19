package org.springblade.integrated.platform.common.project.monitor.operlog.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springblade.core.mp.base.BaseService;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;

import java.util.List;

/**
 * 操作日志 服务层
 *
 * @Author JG🧸
 * @Create 2022/4/13 13:35
 */
public interface IOperLogService extends BaseService<OperLog>
{
    /**
     * 新增操作日志
     *
     * @param operLog 操作日志对象
     */
    public void insertOperlog(OperLog operLog);

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    public List<OperLog> selectOperLogList(OperLog operLog);

    /**
     * 批量删除系统操作日志
     *
     * @param ids 需要删除的数据
     * @return 结果
     */
    public int deleteOperLogByIds(String ids);

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    public OperLog selectOperLogById(Long operId);

    /**
     * 清空操作日志
     */
    public void cleanOperLog();

	/**
	 * 记录业务日志
	 * @param title 标题
	 * @param businessId 业务id，一般是主键
	 * @param businessTable 实体类名
	 * @param businessType 业务类型，枚举类型
	 */
	public void saveLog(String title,String businessId,String businessTable,int businessType);
}

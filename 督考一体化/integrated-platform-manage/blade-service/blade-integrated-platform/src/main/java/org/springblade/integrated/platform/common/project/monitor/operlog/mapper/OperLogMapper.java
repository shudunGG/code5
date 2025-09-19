package org.springblade.integrated.platform.common.project.monitor.operlog.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vingsoft.entity.AnnualEvaluation;
import org.springblade.integrated.platform.common.project.monitor.operlog.domain.OperLog;

import java.util.List;

/**
 * 操作日志 数据层
 *
 * @Author JG🧸
 * @Create 2022/4/9 14:30
 */
public interface OperLogMapper extends BaseMapper<OperLog>
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
    public int deleteOperLogByIds(String[] ids);

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
}

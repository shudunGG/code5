package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.SupervisionSubmitAudit;
import org.apache.ibatis.annotations.Param;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/18 17:32
 */
public interface ISupervisionSubmitAuditService extends IService<SupervisionSubmitAudit> {

	/**
	 * 修改下一个人员状态为待审核
	 * @param servId
	 * @param sort
	 * @param batchNumber
	 * @return
	 */
	boolean updateNextUserStatus(Long servId, Integer sort, String batchNumber);

	/**
	 * 统计当前事项是否有待审核或未通过的送审数据
	 * @param servId
	 * @param batchNumber
	 * @return
	 */
	int countAuditNumber(Long servId,String batchNumber);

	/**
	 * 保存送审信息
	 * @param id
	 * @param title
	 * @param userIds
	 * @param sync
	 * @param operationType	业务类型 info——督察督办；plan——上报计划；report——项目汇报
	 *                      quarterapprisehb——季度考核汇报  quarterappriseXf——季度考核下发；quarterappriseScore——季度考核改分
	 *                      annualapprisehb——年度考核汇报  annualappriseXf——年度考核下发；annualappriseScore——年度考核改分
	 * @return
	 */
	boolean saveSubmitAudit(String id, String title, String userIds, String sync,String operationType);

	/**
	 * 根据项目id和登录人信息获取待审核信息
	 * @param servId
	 * @param userId
	 * @return
	 */
	SupervisionSubmitAudit getAuditByservIdAndUserId(Long servId,Long userId);
}

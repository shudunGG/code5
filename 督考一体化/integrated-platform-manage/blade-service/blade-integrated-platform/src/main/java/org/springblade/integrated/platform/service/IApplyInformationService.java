package org.springblade.integrated.platform.service;

import com.vingsoft.entity.ApplyInformation;
import com.vingsoft.entity.QuarterlyEvaluation;
import org.springblade.core.mp.base.BaseService;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:指标申请信息
 * @date 2022-04-21 12:04
 */
public interface IApplyInformationService extends BaseService<ApplyInformation> {
	/**
	 * 保存
	 * @param aif
	 * @return
	 */
	boolean saveApply(ApplyInformation aif);

}

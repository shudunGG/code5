package org.springblade.integrated.platform.service;

import com.vingsoft.entity.QuarterlyEvaluation;
import com.vingsoft.entity.Reports;
import org.springblade.core.mp.base.BaseService;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:汇报信息
 * @date 2022-04-17 16:20
 */
public interface IReportsService extends BaseService<Reports> {
	/**
	 * 保存汇报信息
	 * @param rp
	 * @return
	 */
	boolean saveReports(Reports rp);

	/**
	 * 修改
	 * @param rp
	 * @return
	 */
	boolean uptReports(Reports rp);
}

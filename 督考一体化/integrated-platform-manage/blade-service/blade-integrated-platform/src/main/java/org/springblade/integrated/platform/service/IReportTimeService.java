package org.springblade.integrated.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.ReportTime;
import org.springblade.core.mp.base.BaseService;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-13 12:16
 */
public interface IReportTimeService extends BaseService<ReportTime> {

	/**
	 * 新增
	 * @param reportTime
	 * @return
	 */
	boolean submitReportTime(ReportTime reportTime);
}

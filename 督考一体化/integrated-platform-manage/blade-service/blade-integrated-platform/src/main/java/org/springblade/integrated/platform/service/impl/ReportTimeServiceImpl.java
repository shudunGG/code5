package org.springblade.integrated.platform.service.impl;/**
 * @author TangYanXing
 * @date 2022-04-13 12:17
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.ReportTime;
import com.vingsoft.entity.StageInformation;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.integrated.platform.mapper.ReportTimeMapper;
import org.springblade.integrated.platform.service.IReportTimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-13 12:17
 */
@Service
public class ReportTimeServiceImpl extends BaseServiceImpl<ReportTimeMapper, ReportTime> implements IReportTimeService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submitReportTime(ReportTime reportTime){
		return super.saveOrUpdate(reportTime);
	}
}

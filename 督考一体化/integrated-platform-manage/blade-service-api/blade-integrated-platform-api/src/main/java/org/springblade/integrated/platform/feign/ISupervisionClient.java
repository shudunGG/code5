package org.springblade.integrated.platform.feign;

import com.vingsoft.entity.SupervisionPhaseReport;
import com.vingsoft.entity.SupervisionSign;
import org.springblade.common.constant.AppNameConstant;
import org.springblade.core.tool.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@FeignClient(
	value = AppNameConstant.BLADE_INTEGRATED_PLATFORM
)
public interface ISupervisionClient {
	String API_PREFIX = "/client";
	String PHASE_OVERDUE = API_PREFIX+"/phase-overdue";
	String OVERDUE_NO_SIGN = API_PREFIX+"/overdue-no-sign";
	String UPDATE_PHASE_REPORT = API_PREFIX+"/update-phase-report";
	String UPDATE_SUPERVISION_OVERDUE = API_PREFIX+"/update-supervision-overdue";
	String UPDATE_SUPERVISION_NAMELY = API_PREFIX+"/update-supervision-namely";


	/**
	 * 超期未签收数据处理
	 * @return
	 */
	@GetMapping(OVERDUE_NO_SIGN)
	R<Boolean> overdueNoSign();
	/**
	 * 获取所有阶段超期数据
	 * @return
	 */
	@GetMapping(PHASE_OVERDUE)
	R<List<SupervisionPhaseReport>> phaseOverdue();
	/**
	 * 更新汇报数据
	 * @return
	 */
	@PostMapping(UPDATE_PHASE_REPORT)
	R<Boolean> updatePhaseReport();

	/**
	 * 处理事项超期
	 * @return
	 */
	@PostMapping(UPDATE_SUPERVISION_OVERDUE)
	R<Boolean> updateSupervisionOverdue();

	/**
	 * 处理事项即将超期
	 * @return
	 */
	@PostMapping(UPDATE_SUPERVISION_NAMELY)
	R<Boolean> updateSupervisionNamely();

}

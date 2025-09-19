package org.springblade.job.executor.jobhandler;

import com.vingsoft.entity.SupervisionPhaseReport;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.feign.ISupervisionClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 事项阶段定时任务
 * @author shaozhubing
 */
@Component
@AllArgsConstructor
public class SupervisionPhasePlanXxlJob {
	private static final Logger logger = LoggerFactory.getLogger(SupervisionPhasePlanXxlJob.class);

	private ISupervisionClient supervisionClient;

	/**
	 * 处理超期未签收数据
	 */
	@XxlJob("overdueNoSignJobHandler")
	public ReturnT<String> overdueNoSignJobHandler(String param) throws Exception {
//		XxlJobLogger.log("");
		R<Boolean> booleanR = supervisionClient.overdueNoSign();
		return ReturnT.SUCCESS;
	}

	/**
	 * 处理阶段超期未汇报数据
	 */
	@XxlJob("phaseOverdueJobHandler")
	public ReturnT<String> phaseOverdueJobHandler(String param) throws Exception {
//		XxlJobLogger.log("");
		R<Boolean> booleanR = supervisionClient.updatePhaseReport();
		return ReturnT.SUCCESS;
	}

	/**
	 * 处理事项超期未办结数据
	 */
	@XxlJob("supervisionJobHandler")
	public ReturnT<String> supervisionJobHandler(String param) throws Exception {
//		XxlJobLogger.log("");
		R<Boolean> booleanR = supervisionClient.updateSupervisionOverdue();
		return ReturnT.SUCCESS;
	}

	/**
	 * 处理事项即将超期
	 */
	@XxlJob("supervisionNamelyJobHandler")
	public ReturnT<String> supervisionNamelyJobHandler(String param) throws Exception {
//		XxlJobLogger.log("");
		R<Boolean> booleanR = supervisionClient.updateSupervisionNamely();
		return ReturnT.SUCCESS;
	}


	public void init() {
		logger.info("init");
	}

	public void destroy() {
		logger.info("destory");
	}


}

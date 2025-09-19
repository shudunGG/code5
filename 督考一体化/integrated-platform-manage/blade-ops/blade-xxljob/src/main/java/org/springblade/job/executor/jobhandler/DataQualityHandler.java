package org.springblade.job.executor.jobhandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springblade.plugin.data.feign.IDataClient;
import org.springframework.stereotype.Component;


/**
 * @author MaQiuyun
 * @date 2021/12/20 15:41
 * @description:
 */
@Component
@AllArgsConstructor
public class DataQualityHandler {
	private IDataClient dataClient;
	private static final Logger logger= LoggerFactory.getLogger(DataQualityHandler.class);
	/**
	 * 1、质检方案执行器
	 */
	@XxlJob("qualityTestingHandler")
	public ReturnT<String> qualityTestingHandler(String param) throws Exception {
		logger.info("质检方案执行器开始执行！入参---->{}",param);
		dataClient.dataQualityJobHandler(param);
		return ReturnT.SUCCESS;
	}
}

package org.springblade.plugin.data.feign;

import org.springblade.common.constant.AppNameConstant;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.data.entity.Datasource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 9:44 2021/10/26 0026
 * @ Description：
 */
@FeignClient(value = AppNameConstant.DATA_QUALITY, fallback = DataClientFallback.class)
public interface IDataClient {
	String API_PREFIX = "/client";
	String DATASOURCE_BY_ID = API_PREFIX + "/getDatasourceById";
	String DATA_QUALITY_JOB_HANDLER = API_PREFIX + "/dataQualityJobHandler";

	/**
	 * @return org.springblade.core.tool.api.R<org.springblade.plugin.data.entity.Datasource>
	 * @Author MaQY
	 * @Description 根据ID获取数据源信息
	 * @Date 上午 9:57 2021/10/26 0026
	 * @Param []
	 **/
	@GetMapping(DATASOURCE_BY_ID)
	R<Datasource> getDatasourceById(@RequestParam("id") Long id);

	/**
	 * 定时任务处理
	 * @param param
	 * @return
	 */
	@GetMapping(DATA_QUALITY_JOB_HANDLER)
	R dataQualityJobHandler(@RequestParam("param") String param);
}

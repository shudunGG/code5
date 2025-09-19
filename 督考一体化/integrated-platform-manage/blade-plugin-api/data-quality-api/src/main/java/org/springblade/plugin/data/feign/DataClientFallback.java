package org.springblade.plugin.data.feign;

import org.springblade.core.tool.api.R;
import org.springblade.plugin.data.entity.Datasource;
import org.springframework.stereotype.Component;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 9:45 2021/10/26 0026
 * @ Description：
 */
@Component
public class DataClientFallback implements IDataClient {
	@Override
	public R<Datasource> getDatasourceById(Long id) {
		return R.fail("获取数据失败");
	}

	@Override
	public R dataQualityJobHandler(String param) {
		return R.fail("任务执行失败！");
	}
}

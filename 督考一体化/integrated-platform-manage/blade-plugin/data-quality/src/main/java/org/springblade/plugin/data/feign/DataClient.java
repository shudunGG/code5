package org.springblade.plugin.data.feign;

import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.plugin.data.entity.Datasource;
import org.springblade.plugin.data.service.IDatasourceService;
import org.springblade.plugin.data.service.IQualityTestingProgrammeService;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 上午 9:48 2021/10/26 0026
 * @ Description：
 */
@NonDS
@ApiIgnore
@RestController
@AllArgsConstructor
public class DataClient implements IDataClient {
	private IDatasourceService datasourceService;
	private IQualityTestingProgrammeService programmeService;

	@Override
	public R<Datasource> getDatasourceById(Long id) {
		return R.data(datasourceService.getById(id));
	}

	@Override
	public R dataQualityJobHandler(String param) {
		return R.status(programmeService.testDataQuality(param));
	}
}

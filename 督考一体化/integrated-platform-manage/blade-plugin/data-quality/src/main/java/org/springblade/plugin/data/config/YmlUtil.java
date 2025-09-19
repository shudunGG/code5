package org.springblade.plugin.data.config;

import lombok.Data;
import org.springblade.plugin.data.entity.Datasource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author MaQiuyun
 * @date 2021/12/22 9:33
 * @description:
 */
@Component
@Data
public class YmlUtil {
	@Value("${xxl.job.admin.addresses}")
	private String addresses;
	@Value("${xxl.job.accessToken}")
	private String accessToken;
	@Value("${dataQuality.database.url}")
	private String databaseUrl;
	@Value("${dataQuality.database.username}")
	private String databaseUserName;
	@Value("${dataQuality.database.password}")
	private String databasePassword;
	@Value("${dataQuality.database.driverClass}")
	private String databaseDriver;

	/**
	 * 获取当前模块数据源
	 *
	 * @return
	 */
	public Datasource currentDatasource() {
		Datasource currentDatasource = new Datasource();
		currentDatasource.setDriverClass(this.getDatabaseDriver());
		currentDatasource.setUrl(this.getDatabaseUrl());
		currentDatasource.setUsername(this.getDatabaseUserName());
		currentDatasource.setPassword(this.getDatabasePassword());
		return currentDatasource;
	}
}

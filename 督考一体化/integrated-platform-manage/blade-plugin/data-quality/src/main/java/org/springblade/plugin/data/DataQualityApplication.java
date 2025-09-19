package org.springblade.plugin.data;

import org.springblade.common.constant.AppNameConstant;
import org.springblade.core.cloud.feign.EnableBladeFeign;
import org.springblade.core.launch.BladeApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @ Author     ：MaQY
 * @ Date       ：Created in 下午 3:36 2021/10/25 0025
 * @ Description：
 */
@EnableBladeFeign
@SpringCloudApplication
public class DataQualityApplication {
	public static void main(String[] args) {
		BladeApplication.run(AppNameConstant.DATA_QUALITY, DataQualityApplication.class, args);
	}
}

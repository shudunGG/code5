package org.springblade.integrated.platform;

import org.springblade.common.constant.AppNameConstant;
import org.springblade.core.cloud.feign.EnableBladeFeign;
import org.springblade.core.launch.BladeApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @author mrtang
 * @version 1.0
 * @description: 一体化平台
 * @date 2022/4/7 16:54
 */
@EnableBladeFeign
@SpringCloudApplication
public class PlatformApplication {
	public static void main(String[] args) {
		BladeApplication.run(AppNameConstant.BLADE_INTEGRATED_PLATFORM, PlatformApplication.class, args);
	}
}

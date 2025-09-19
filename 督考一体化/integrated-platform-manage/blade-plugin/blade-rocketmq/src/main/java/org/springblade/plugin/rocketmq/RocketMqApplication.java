package org.springblade.plugin.rocketmq;

import org.springblade.common.constant.AppNameConstant;
import org.springblade.core.cloud.feign.EnableBladeFeign;
import org.springblade.core.launch.BladeApplication;
import org.springframework.cloud.client.SpringCloudApplication;

/**
 * @author mrtang
 * @version 1.0
 * @description: 队列服务
 * @date 2021/12/21 3:45 下午
 */
@EnableBladeFeign
@SpringCloudApplication
public class RocketMqApplication {
	public static void main(String[] args) {
		BladeApplication.run(AppNameConstant.APPLICATION_ROCKETMQ_NAME, RocketMqApplication.class, args);
	}
}

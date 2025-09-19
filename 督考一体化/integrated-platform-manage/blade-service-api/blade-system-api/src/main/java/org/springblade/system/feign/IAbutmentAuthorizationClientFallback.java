package org.springblade.system.feign;

import org.springblade.core.tool.api.R;
import org.springblade.system.entity.AbutmentAuthorization;
import org.springframework.stereotype.Component;

/**
 * @author mrtang
 * @title: IAbutmentAuthorizationClientFallback
 * @projectName cloud-system
 * @description: TODO
 * @date 2021-07-17 00:01
 */
@Component
public class IAbutmentAuthorizationClientFallback implements IAbutmentAuthorizationClient {
	@Override
	public R<AbutmentAuthorization> getByClientId(String clientId) {
		return R.fail("获取数据失败");
	}
}

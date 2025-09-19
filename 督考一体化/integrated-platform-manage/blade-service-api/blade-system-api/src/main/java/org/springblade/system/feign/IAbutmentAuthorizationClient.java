package org.springblade.system.feign;

import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springblade.system.entity.AbutmentAuthorization;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mrtang
 * @title: IAbutmentAuthorizationClient
 * @projectName cloud-system
 * @description: TODO
 * @date 2021-07-17 00:01
 */
@FeignClient(
	value = AppConstant.APPLICATION_SYSTEM_NAME,
	fallback = IAbutmentAuthorizationClientFallback.class
)
public interface IAbutmentAuthorizationClient {
	String API_PREFIX = "/client";
	String GET_BY_CLIENT_ID = API_PREFIX + "/abutment/get-by-client-id";

	/**
	 * 根据客户端id查询
	 * @param clientId
	 * @return
	 */
	@GetMapping(GET_BY_CLIENT_ID)
	R<AbutmentAuthorization> getByClientId(@RequestParam("clientId") String clientId);
}

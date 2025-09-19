package org.springblade.system.cache;


import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.system.entity.AbutmentAuthorization;
import org.springblade.system.feign.IAbutmentAuthorizationClient;

/**
 * @author mrtang
 * @title: AbutmentAuthorizationCache
 * @projectName cloud-system
 * @description: TODO
 * @date 2021-07-17 00:00
 */
public class AbutmentAuthorizationCache {

	private static IAbutmentAuthorizationClient authorizationClient;

	public static IAbutmentAuthorizationClient getAuthorizationClient(){
		if(authorizationClient==null){
			authorizationClient = SpringUtil.getBean(IAbutmentAuthorizationClient.class);
		}
		return authorizationClient;
	}

	/**
	 * 获取对接授权实体
	 * @param clientId
	 * @return
	 */
	public static AbutmentAuthorization getByClientId(String clientId) {
		return getAuthorizationClient().getByClientId(clientId).getData();
	}
}

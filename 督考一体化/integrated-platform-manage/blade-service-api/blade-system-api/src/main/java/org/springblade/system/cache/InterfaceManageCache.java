package org.springblade.system.cache;


import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.system.entity.ApiRequestData;
import org.springblade.system.entity.InterfaceManage;
import org.springblade.system.feign.IInterfaceManageClient;

/**
 * @author mrtang
 * @title: InterfaceManageCache
 * @projectName cloud-system
 * @description: TODO
 * @date 2021-07-16 23:43
 */
public class InterfaceManageCache {

	private static IInterfaceManageClient interfaceManageClient;

	private static IInterfaceManageClient getInterfaceManageClient() {
		if (interfaceManageClient == null) {
			interfaceManageClient = SpringUtil.getBean(IInterfaceManageClient.class);
		}
		return interfaceManageClient;
	}

	/**
	 * 获取接口实体
	 *
	 * @param id 主键
	 * @return InterfaceManage
	 */
	public static InterfaceManage getById(Long id) {
		R<InterfaceManage> result = getInterfaceManageClient().getById(id);
		return result.getData();
	}

	/**
	 * 获取接口实体
	 * @param interfaceCode
	 * @return InterfaceManage
	 */
	public static InterfaceManage getByCode(String interfaceCode){
		return getInterfaceManageClient().getByCode(interfaceCode).getData();
	}

	/**
	 * 保存接口请求数据
	 * @param apiRequestData
	 * @return
	 */
	public static R<Boolean> saveApiReqData(ApiRequestData apiRequestData){
		return getInterfaceManageClient().saveApiReqData(apiRequestData);
	}

	/**
	 * 根据接口编码统计调用次数
	 * @param codes
	 * @return
	 */
	public static R<Integer> countReqNumByCodes(String codes){
		return getInterfaceManageClient().countReqNumByCodes(codes);
	}
}

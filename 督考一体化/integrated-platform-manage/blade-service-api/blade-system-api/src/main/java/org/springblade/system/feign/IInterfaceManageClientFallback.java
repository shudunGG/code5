package org.springblade.system.feign;

import org.springblade.core.tool.api.R;
import org.springblade.system.entity.ApiRequestData;
import org.springblade.system.entity.InterfaceManage;
import org.springframework.stereotype.Component;

/**
 * @author mrtang
 * @title: IInterfaceManageClientFallback
 * @projectName cloud-system
 * @description: TODO
 * @date 2021-07-16 23:45
 */
@Component
public class IInterfaceManageClientFallback implements IInterfaceManageClient {
	@Override
	public R<InterfaceManage> getById(Long id) {
		return R.fail("获取数据失败");
	}

	@Override
	public R<InterfaceManage> getByCode(String interfaceCode) {
		return R.fail("获取数据失败");
	}

	@Override
	public R<Boolean> saveApiReqData(ApiRequestData apiRequestData) {
		return R.fail("保存数据失败");
	}

	@Override
	public R<Integer> countReqNumByCodes(String codes) {
		return R.fail("获取数据失败");
	}
}

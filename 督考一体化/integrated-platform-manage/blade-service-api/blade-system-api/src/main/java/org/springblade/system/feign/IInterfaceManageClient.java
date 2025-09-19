package org.springblade.system.feign;

import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springblade.system.entity.ApiRequestData;
import org.springblade.system.entity.InterfaceManage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mrtang
 * @title: IInterfaceManageClient
 * @projectName cloud-system
 * @description: TODO
 * @date 2021-07-16 23:44
 */
@FeignClient(
	value = AppConstant.APPLICATION_SYSTEM_NAME,
	fallback = IInterfaceManageClientFallback.class
)
public interface IInterfaceManageClient {

	String API_PREFIX = "/client";
	String GET_BY_ID = API_PREFIX + "/interface/get-by-id";
	String GET_BY_CODE = API_PREFIX + "/interface/get-by-code";
	String SAVE_API_REQ_DATA = API_PREFIX + "/api_req/save_data";
	String COUNT_REQ_NUM_BY_CODES = API_PREFIX + "/interface/count-by-codes";

	/**
	 * 获取接口实体
	 *
	 * @param id 主键
	 * @return
	 */
	@GetMapping(GET_BY_ID)
	R<InterfaceManage> getById(@RequestParam("id") Long id);

	/**
	 * 获取接口实体
	 *
	 * @param interfaceCode 接口编码
	 * @return
	 */
	@GetMapping(GET_BY_CODE)
	R<InterfaceManage> getByCode(@RequestParam("interfaceCode") String interfaceCode);

	/**
	 * 保存接口请求数据
	 * @param apiRequestData
	 * @return
	 */
	@PostMapping(SAVE_API_REQ_DATA)
	R<Boolean> saveApiReqData(@RequestBody ApiRequestData apiRequestData);

	/**
	 * 根据接口编码统计调用次数
	 * @param codes
	 * @return
	 */
	@GetMapping(COUNT_REQ_NUM_BY_CODES)
	R<Integer> countReqNumByCodes(@RequestParam("codes") String codes);
}

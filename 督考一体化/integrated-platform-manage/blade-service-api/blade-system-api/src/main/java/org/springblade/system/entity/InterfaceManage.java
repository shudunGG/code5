package org.springblade.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

/**
 * @author mrtang
 * @title: InterfaceManage
 * @projectName cloud-system
 * @description: 接口管理实体
 * @date 2021-07-15 08:53
 */

@Data
@TableName("vingsoft_interface_manage")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "InterfaceManage对象", description = "InterfaceManage对象")
public class InterfaceManage extends BaseEntity {
	/** 接口名称 */
	private String interfaceName ;
	/** 接口编码 */
	private String interfaceCode ;
	/** 接口地址 */
	private String interfaceUrl ;
	/** 业务系统名称 */
	private String systemName ;
	/** 请求方式 */
	private String requestType ;
	/** 接口负责人 */
	private String principal ;
	/** 联系电话 */
	private String phone ;
	/** 请求参数 */
	private String requestParameters ;
	/** 返回参数 */
	private String returnParameters ;
	/** 返回示例 */
	private String returnExample ;
	/** 接口描述 */
	private String interfaceDescription ;
}

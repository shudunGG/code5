/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springblade.core.mp.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 应用系统管理表实体类
 *
 * @author BladeX
 * @since 2021-10-25
 */
@Data
@TableName("applcation_system_management")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "SystemManagement对象", description = "应用系统管理表")
public class ApplicationSystemManagement extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	* 系统名称
	*/
		@ApiModelProperty(value = "系统名称")
		private String systemName;
	/**
	* IP地址
	*/
		@ApiModelProperty(value = "IP地址")
		private String ipAddress;
	/**
	* APPKEY
	*/
		@ApiModelProperty(value = "APPKEY")
		private String appKey;
	/**
	* 应用系统提供单位
	*/
		@ApiModelProperty(value = "应用系统提供单位")
		private String systemProvider;
	/**
	* 系统部署地点
	*/
		@ApiModelProperty(value = "系统部署地点")
		private String deploymentLocation;
	/**
	* 联系人
	*/
		@ApiModelProperty(value = "联系人")
		private String contacts;
	/**
	* 联系人电话
	*/
		@ApiModelProperty(value = "联系人电话")
		private String contactPhone;
	/**
	* 系统访问地址
	*/
		@ApiModelProperty(value = "系统访问地址")
		private String systemAccessAddress;
	/**
	* 备注
	*/
		@ApiModelProperty(value = "备注")
		private String remarks;
	/**
	* APPSECRET
	*/
		@ApiModelProperty(value = "APPSECRET")
		private String appSecret;
	/**
	* 是否启用0禁用1启用
	*/
		@ApiModelProperty(value = "是否启用0禁用1启用")
		private Integer enableStatus;


}

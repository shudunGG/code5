package org.springblade.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

/**
 * @author mrtang
 * @title: AbutmentAuthorization
 * @projectName cloud-system
 * @description: 对接授权
 * @date 2021-07-15 10:00
 */
@Data
@TableName("blade_client")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "AbutmentAuthorization对象", description = "AbutmentAuthorization对象")
public class AbutmentAuthorization extends BaseEntity {
	/** 应用名称 */
	private String clientName ;
	/** 所属区划;需添加 */
	private String area ;
	/** 所属部门;需添加 */
	private Long dept ;
	/** 客户端id;自动生成 */
	private String clientId ;
	/** 客户端密钥;自动生成 */
	private String clientSecret ;
	/** 接口集合;需添加 */
	private String interfaceIds ;
	/** 授权类型;默认值client_credentials */
	private String authorizedGrantTypes = "client_credentials" ;
	/** 授权范围;默认值all */
	private String scope = "all" ;
	/**
	 * 令牌过期秒数
	 */
	@ApiModelProperty(value = "令牌过期秒数")
	private Integer accessTokenValidity = 3600;
	/**
	 * 刷新令牌过期秒数
	 */
	@ApiModelProperty(value = "刷新令牌过期秒数")
	private Integer refreshTokenValidity = 604800;
	/** 联系人;需添加 */
	private String linkman ;
	/** 联系电话;需添加 */
	private String phone ;
	/** 申请表;需添加 */
	private String applicationForm ;

	/**
	 * 公钥
	 */
	private String publicKey;

	/**
	 * 私钥
	 */
	private String privateKey;

	/**
	 * 生成SM2
	 */
	private String smTwo;

	/**
	 * 评价渠道
	 */
	private String pfs;

	/**
	 * 是否是历史数据
	 */
	private String isHistory;
}

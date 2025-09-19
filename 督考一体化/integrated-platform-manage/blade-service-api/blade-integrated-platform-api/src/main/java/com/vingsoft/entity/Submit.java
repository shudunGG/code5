package com.vingsoft.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

/**
 * @className: Submit
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/5/15 17:03 星期一
 * @Version 1.0
 **/
@Data
@ApiModel(value = "短信发送提交对象对象", description = "短信发送")
public class Submit  extends BaseEntity {

	/**
	 * 企业名称
	 */
	private String ecName;

	/**
	 * 接口账号用户名
	 */
	private String apId;

	/**
	 * 密码
	 */
	private String secretKey;

	/**
	 * 收信手机号码。英文逗号分隔，每批次限5000个号码，
	 * 例：“13800138000,13800138001,13800138002”。
	 */
	private String mobiles;

	/**
	 * 短信内容。如content中存在双引号，请务必使用转义符\在报文中进行转义（使用JSON转换工具转换会自动增加转义符），否则会导致服务端解析报文异常。
	 */
	private String content;

	/**
	 * 签名编码。在云MAS平台『管理』→『接口管理』→『短信接入用户管理』获取。
	 */
	private String sign;

	/**
	 * 扩展码。依据申请开户的服务代码匹配类型而定，如为精确匹配，此项填写空字符串（""）；如为模糊匹配，此项可填写空字符串或自定义的扩展码，注：服务代码加扩展码总长度不能超过20位。
	 */
	private String addSerial;

	/**
	 * 参数校验序列，生成方法：将ecName、apId、secretKey、mobiles、content、sign、addSerial按序拼接（无间隔符），通过MD5（32位小写）计算得出值。
	 */
	private String mac;
}

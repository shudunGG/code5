package com.vingsoft.vo;

import com.alibaba.fastjson.JSONArray;
import com.vingsoft.entity.OaEmail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/5/11 09:33
 */
@Data
public class OaEmailVO extends OaEmail {
	/** 附件 */
	@ApiModelProperty(name = "附件",notes = "")
	private JSONArray filesArray ;
	/**
	 * 创建人
	 */
	@ApiModelProperty("创建人")
	private String createUserName;
}

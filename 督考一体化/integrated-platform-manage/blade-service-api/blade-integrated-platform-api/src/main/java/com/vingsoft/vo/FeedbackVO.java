package com.vingsoft.vo;

import com.alibaba.fastjson.JSONArray;
import com.vingsoft.entity.Feedback;
import lombok.Data;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/29 15:38
 */
@Data
public class FeedbackVO extends Feedback {
	/**
	 * 相关附件
	 */
	private JSONArray filesJson;

	/**
	 * 创建人
	 */
	private String createUserName;
}

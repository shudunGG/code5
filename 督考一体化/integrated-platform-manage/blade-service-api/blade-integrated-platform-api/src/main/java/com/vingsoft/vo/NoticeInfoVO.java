package com.vingsoft.vo;

import com.alibaba.fastjson.JSONArray;
import com.vingsoft.entity.NoticeInfo;
import lombok.Data;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/29 00:57
 */
@Data
public class NoticeInfoVO extends NoticeInfo {
	/**
	 * 附件
	 */
	private JSONArray filesUrlJson;

	/**
	 * 正文附件地址
	 */
	private JSONArray textFileUrlJson;

}

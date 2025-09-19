package com.vingsoft.vo;

import com.alibaba.fastjson.JSONArray;
import com.vingsoft.entity.GuestBook;
import lombok.Data;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/29 19:37
 */
@Data
public class GuestBookVO extends GuestBook {
	/**
	 * 相关附件
	 */
	private JSONArray filesJson;
}

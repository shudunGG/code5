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
package org.springblade.common.utils;

import java.util.Random;
import java.util.UUID;

/**
 * 通用工具类
 *
 * @author Chill
 */
public class CommonUtil {

		public static String randomUUID() {
			UUID uuid = UUID.randomUUID();
			return uuid.toString().replace("-", "").toUpperCase();
		}

	/**
	 * 生成随机数
	 * @param length
	 * @return
	 */
		public static String createRandomNumber(int length) {
			StringBuilder strBuffer = new StringBuilder();
			Random rd = new Random();
			for (int i = 0; i < length; i++) {
				strBuffer.append(rd.nextInt(10));
			}
			return strBuffer.toString();
		}

}

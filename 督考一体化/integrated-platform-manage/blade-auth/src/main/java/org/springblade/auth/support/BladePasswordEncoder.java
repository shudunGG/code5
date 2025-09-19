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
package org.springblade.auth.support;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springblade.common.constant.CommonConstant;
import org.springblade.core.tool.utils.AesUtil;
import org.springblade.core.tool.utils.DigestUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 自定义密码加密
 *
 * @author Chill
 */
@Slf4j
public class BladePasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		return DigestUtil.hex((String) rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {

		boolean isEncryt;
		String realPassword = "";
		//aes解密成功的默认统一登录数据
		try {
			realPassword = AesUtil.decryptFormBase64ToString((String) rawPassword, CommonConstant.DEFAULT_AES_KEY);
			log.info("Aes解密成功------------------标记");
			isEncryt = true;
		} catch (Exception e) {
			isEncryt = false;
			log.info("Aes解密失败------------------标记");

		}
		//本地登录校验密码
		try {
			if (isEncryt) {
				return encodedPassword.equals(DigestUtil.encrypt(realPassword));
			}
		} catch (Exception e) {
			System.out.println("验证登录报错");
			e.getMessage();
			return false;
		}
		return false;
	}

}

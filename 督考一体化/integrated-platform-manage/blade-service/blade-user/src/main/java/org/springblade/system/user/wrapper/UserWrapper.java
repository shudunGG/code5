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
package org.springblade.system.user.wrapper;

import com.vingsoft.crypto.sm4.SM4Crypto;
import org.apache.commons.lang3.StringUtils;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.system.cache.DictCache;
import org.springblade.system.cache.ParamCache;
import org.springblade.system.cache.SysCache;
import org.springblade.system.entity.Tenant;
import org.springblade.system.enums.DictEnum;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.vo.UserVO;

import java.util.List;
import java.util.Objects;

/**
 * 包装类,返回视图层所需的字段
 *
 * @author Chill
 */
public class UserWrapper extends BaseEntityWrapper<User, UserVO> {

	private static final Integer FAIL_COUNT = 5;
	private static final String FAIL_COUNT_VALUE = "account.failCount";

	public static UserWrapper build() {
		return new UserWrapper();
	}

	@Override
	public UserVO entityVO(User user) {
		UserVO userVO = Objects.requireNonNull(BeanUtil.copy(user, UserVO.class));
		Tenant tenant = SysCache.getTenant(user.getTenantId());
		List<String> roleName = SysCache.getRoleNames(user.getRoleId());
		List<String> deptName = SysCache.getDeptNames(user.getDeptId());
		List<String> postName = SysCache.getPostNames(user.getPostId());
		userVO.setTenantName(tenant.getTenantName());
		userVO.setRoleName(Func.join(roleName));
		userVO.setDeptName(Func.join(deptName));
		userVO.setPostName(Func.join(postName));
		userVO.setSexName(DictCache.getValue(DictEnum.SEX, user.getSex()));
		userVO.setUserTypeName(DictCache.getValue(DictEnum.USER_TYPE, user.getUserType()));
		//2024年4月10日20点10分-敏感字段处理开始
		SM4Crypto sm4 = SM4Crypto.getInstance();
		if(StringUtils.isNotEmpty(userVO.getName()) && sm4.checkDataIsEncrypt(userVO.getName())){
			userVO.setName(sm4.decrypt(userVO.getName()));
		}
		if(StringUtils.isNotEmpty(userVO.getRealName()) && sm4.checkDataIsEncrypt(userVO.getRealName())){
			userVO.setRealName(sm4.decrypt(userVO.getRealName()));
		}
		if(StringUtils.isNotEmpty(userVO.getEmail()) && sm4.checkDataIsEncrypt(userVO.getEmail())){
			userVO.setEmail(sm4.decrypt(userVO.getEmail()));
		}
		if(StringUtils.isNotEmpty(userVO.getPhone()) && sm4.checkDataIsEncrypt(userVO.getPhone())){
			userVO.setPhone(sm4.decrypt(userVO.getPhone()));
		}
		//2024年4月10日20点10分-敏感字段处理结束
		//获取密码错误次数
		int count = getFailCount(userVO.getTenantId(),userVO.getAccount());
		int failCount = Func.toInt(ParamCache.getValue(FAIL_COUNT_VALUE), FAIL_COUNT);
		if (count >= failCount) {
			userVO.setLock(1);
		}else{
			userVO.setLock(0);
		}

		return userVO;
	}

	/**
	 * 获取账号错误次数
	 *
	 * @param tenantId 租户id
	 * @param username 账号
	 * @return int
	 */
	private int getFailCount(String tenantId, String username) {
		BladeRedis bladeRedis = SpringUtil.getBean(BladeRedis.class);
		return Func.toInt(bladeRedis.get(CacheNames.tenantKey(tenantId, CacheNames.USER_FAIL_KEY, username)), 0);
	}

}

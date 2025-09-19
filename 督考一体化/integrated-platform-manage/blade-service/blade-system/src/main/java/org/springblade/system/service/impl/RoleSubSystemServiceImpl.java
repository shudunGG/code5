package org.springblade.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.entity.RoleSubSystem;
import org.springblade.system.mapper.RoleSubsystemMapper;
import org.springblade.system.service.IRoleSubSystemService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/5/1 10:47
 */
@Service
public class RoleSubSystemServiceImpl extends ServiceImpl<RoleSubsystemMapper, RoleSubSystem> implements IRoleSubSystemService {

	@Override
	public List<RoleSubSystem> getRoleSubSystemByRoleIds(String roleIds) {
		return baseMapper.selectList(Wrappers.<RoleSubSystem>query().lambda().in(RoleSubSystem::getRoleId, Func.toLongList(roleIds)));
	}
}

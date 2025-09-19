package org.springblade.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springblade.system.entity.RoleSubSystem;

import java.util.List;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/5/1 10:47
 */
public interface IRoleSubSystemService extends IService<RoleSubSystem> {

	List<RoleSubSystem> getRoleSubSystemByRoleIds(String roleIds);
}

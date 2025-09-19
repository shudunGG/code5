package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionSign;
import com.vingsoft.vo.SupervisionSignVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.service.ISupervisionInfoService;
import org.springblade.system.cache.SysCache;
import org.springblade.system.entity.Dept;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;

import java.util.Objects;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/18 10:41
 */
public class SupervisionSignWrapper extends BaseEntityWrapper<SupervisionSign, SupervisionSignVO> {

	public static SupervisionSignWrapper build(){
		return new SupervisionSignWrapper();
	}

	@Override
	public SupervisionSignVO entityVO(SupervisionSign entity) {
		SupervisionSignVO vo = Objects.requireNonNull(BeanUtil.copy(entity,SupervisionSignVO.class));
		Dept signDept = SysCache.getDept(vo.getSignDept());
		if(signDept!=null){
			vo.setSignDeptName(signDept.getDeptName());
		}
		Dept overDept = SysCache.getDept(vo.getOverDept());
		if(overDept!=null){
			vo.setOverDeptName(overDept.getDeptName());
		}
		if(vo.getSignUser()!=null){
			User signUser = UserCache.getUser(vo.getSignUser());
			if(signUser!=null)
			vo.setSignUserName(signUser.getName());
		}
		if(vo.getOverUser()!=null){
			User overUser = UserCache.getUser(vo.getOverUser());
			if(overUser!=null)
			vo.setOverUserName(overUser.getName());
		}

		SupervisionInfo supervisionInfo = SpringUtil.getBean(ISupervisionInfoService.class).getById(vo.getServId());
		if(supervisionInfo!=null){
			vo.setServName(supervisionInfo.getServName());
			vo.setServCode(supervisionInfo.getServCode());
		}
		return vo;
	}
}

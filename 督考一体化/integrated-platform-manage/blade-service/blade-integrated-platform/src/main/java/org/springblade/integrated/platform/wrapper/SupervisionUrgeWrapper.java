package org.springblade.integrated.platform.wrapper;

import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.SupervisionInfo;
import com.vingsoft.entity.SupervisionUrge;
import com.vingsoft.entity.UnifyMessage;
import com.vingsoft.vo.SupervisionUrgeVo;
import com.vingsoft.vo.UnifyMessageVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.service.ISupervisionFilesService;
import org.springblade.integrated.platform.service.ISupervisionInfoService;
import org.springblade.system.cache.SysCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;

import java.util.List;
import java.util.Objects;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/24 21:51
 */
public class SupervisionUrgeWrapper extends BaseEntityWrapper<SupervisionUrge, SupervisionUrgeVo> {

	public static SupervisionUrgeWrapper build(){
		return new SupervisionUrgeWrapper();
	}

	@Override
	public SupervisionUrgeVo entityVO(SupervisionUrge entity) {
		SupervisionUrgeVo vo = Objects.requireNonNull(BeanUtil.copy(entity,SupervisionUrgeVo.class));
		R<User> userR = SpringUtil.getBean(IUserClient.class).userInfoById(vo.getUrgeUser());
		User user=userR.getData();
		if(ObjectUtil.isNotEmpty(user)){
			String userNameDecrypt = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(user.getRealName());
			vo.setUrgeUserName(userNameDecrypt);
		}

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(vo.getServCode());
		fileDel.setFileFrom("4");
		fileDel.setCreateDept(entity.getCreateDept());
		fileDel.setPhaseId(entity.getId());
		List<SupervisionFiles> list = SpringUtil.getBean(ISupervisionFilesService.class).list(Condition.getQueryWrapper(fileDel));
		vo.setSupervisionFilesList(list);
		return vo;
	}
}

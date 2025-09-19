package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.SupervisionLate;
import com.vingsoft.vo.SupervisionLateVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.service.ISupervisionFilesService;

import java.util.List;
import java.util.Objects;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/20 23:48
* @Version:        1.0
*/
public class SupervisionLateWrapper extends BaseEntityWrapper<SupervisionLate, SupervisionLateVO> {

	public static SupervisionLateWrapper build(){
		return new SupervisionLateWrapper();
	}

	@Override
	public SupervisionLateVO entityVO(SupervisionLate entity) {
		SupervisionLateVO vo = Objects.requireNonNull(BeanUtil.copy(entity,SupervisionLateVO.class));

		SupervisionFiles file = new SupervisionFiles();
		file.setServCode(entity.getServCode());
		file.setPhaseId(entity.getId());
		file.setFileFrom("延期");
		file.setCreateDept(entity.getLateDeptId());
		List<SupervisionFiles> filesList = SpringUtil.getBean(ISupervisionFilesService.class).list(Condition.getQueryWrapper(file));
		vo.setFilesList(filesList);
		return vo;
	}
}

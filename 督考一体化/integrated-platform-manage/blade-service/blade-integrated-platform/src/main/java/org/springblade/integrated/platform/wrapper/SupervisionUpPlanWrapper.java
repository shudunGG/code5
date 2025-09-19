package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionUpPlanVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.service.ISupervisionFilesService;
import org.springblade.integrated.platform.service.ISupervisionInfoService;
import org.springblade.integrated.platform.service.ISupervisionSubmitAuditService;

import java.util.List;
import java.util.Objects;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/17 15:51
* @Version:        1.0
*/
public class SupervisionUpPlanWrapper extends BaseEntityWrapper<SupervisionUpPlan, SupervisionUpPlanVO> {

	public static SupervisionUpPlanWrapper build(){
		return new SupervisionUpPlanWrapper();
	}

	@Override
	public SupervisionUpPlanVO entityVO(SupervisionUpPlan entity) {
		SupervisionUpPlanVO vo = Objects.requireNonNull(BeanUtil.copy(entity,SupervisionUpPlanVO.class));
		SupervisionInfo info = SpringUtil.getBean(ISupervisionInfoService.class).getById(vo.getServId());

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(info.getServCode());
		fileDel.setPhaseId(entity.getId());
		fileDel.setFileFrom("2");
		fileDel.setCreateDept(entity.getUpDept());
		List<SupervisionFiles> list = SpringUtil.getBean(ISupervisionFilesService.class).list(Condition.getQueryWrapper(fileDel));
		vo.setSupervisionFilesList(list);

		SupervisionSubmitAudit audit = new SupervisionSubmitAudit();
		audit.setReportId(entity.getServId());
		audit.setServId(entity.getId());
		audit.setUserId(AuthUtil.getUserId());
		audit.setStatus(0);
		SupervisionSubmitAudit one = SpringUtil.getBean(ISupervisionSubmitAuditService.class).getOne(Condition.getQueryWrapper(audit));
		vo.setSupervisionSubmitAudit(one);
		return vo;
	}
}

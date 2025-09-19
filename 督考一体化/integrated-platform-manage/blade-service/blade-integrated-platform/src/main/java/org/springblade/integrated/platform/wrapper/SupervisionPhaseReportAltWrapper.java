package org.springblade.integrated.platform.wrapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionPhaseReportAltVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.service.IAppriseFilesService;
import org.springblade.integrated.platform.service.IMessageInformationService;
import org.springblade.integrated.platform.service.ISupervisionFilesService;
import org.springblade.integrated.platform.service.ISupervisionInfoService;

import java.util.List;
import java.util.Objects;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/4/21 22:12
* @Version:        1.0
*/
public class SupervisionPhaseReportAltWrapper extends BaseEntityWrapper<SupervisionPhaseReportAlt, SupervisionPhaseReportAltVO> {

	public static SupervisionPhaseReportAltWrapper build(){
		return new SupervisionPhaseReportAltWrapper();
	}

	@Override
	public SupervisionPhaseReportAltVO entityVO(SupervisionPhaseReportAlt entity) {
		SupervisionPhaseReportAltVO vo = Objects.requireNonNull(BeanUtil.copy(entity,SupervisionPhaseReportAltVO.class));

		SupervisionFiles fileDel = new SupervisionFiles();
		fileDel.setServCode(vo.getServCode());
		fileDel.setPhaseId(vo.getPhaseId());
		fileDel.setFileFrom("alt");
		fileDel.setCreateDept(Long.parseLong(entity.getReportDept()));
		fileDel.setUploadUser(vo.getReportUser());
		List<SupervisionFiles> list = SpringUtil.getBean(ISupervisionFilesService.class).list(Condition.getQueryWrapper(fileDel));
		vo.setSupervisionFilesList(list);

		MessageInformation message = new MessageInformation();
		message.setChildId(entity.getId());
		List<MessageInformation> list1 = SpringUtil.getBean(IMessageInformationService.class).list(Condition.getQueryWrapper(message));
		for (MessageInformation me : list1) {
			AppriseFiles appriseFiles = new AppriseFiles();
			appriseFiles.setBusinessId(me.getId());
			List<AppriseFiles> list2 = SpringUtil.getBean(IAppriseFilesService.class).list(Condition.getQueryWrapper(appriseFiles));
			me.setAppriseFilesList(list2);
		}
		ISupervisionInfoService bean = SpringUtil.getBean(ISupervisionInfoService.class);
		QueryWrapper wrapper=new QueryWrapper();
		wrapper.eq("serv_code",vo.getServCode());
		SupervisionInfo supervisionInfo = bean.getOne(wrapper);
		if(supervisionInfo!=null){
			vo.setServName(supervisionInfo.getServName());
		}
		vo.setMessageList(list1);
		return vo;
	}
}

package org.springblade.integrated.platform.wrapper;

import com.vingsoft.entity.*;
import com.vingsoft.vo.SupervisionSubmitAuditVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.service.*;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;

import java.util.Objects;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/21 17:59
 */
public class SupervisionSubmitAuditWrapper extends BaseEntityWrapper<SupervisionSubmitAudit, SupervisionSubmitAuditVO> {

	public static SupervisionSubmitAuditWrapper build(){
		return new SupervisionSubmitAuditWrapper();
	}

	@Override
	public SupervisionSubmitAuditVO entityVO(SupervisionSubmitAudit entity) {
		SupervisionSubmitAuditVO vo = Objects.requireNonNull(BeanUtil.copy(entity,SupervisionSubmitAuditVO.class));

		if(vo.getApprovalUser()!=null){
			User user = UserCache.getUser(vo.getApprovalUser());
			vo.setApprovalUserName(user == null ? null : user.getRealName());
		}else{
			vo.setApprovalUserName(UserCache.getUser(vo.getUserId()).getRealName());
		}


		if(StatusConstant.OPERATION_TYPE_INFO.equals(vo.getOperationType())){
			SupervisionInfo supervisionInfo = SpringUtil.getBean(ISupervisionInfoService.class).getById(vo.getServId());
			if(supervisionInfo!=null){
				vo.setServName(supervisionInfo.getServName());
				vo.setServCode(supervisionInfo.getServCode());
				vo.setServTypeOne(supervisionInfo.getServTypeOne());
				vo.setServTypeTwo(supervisionInfo.getServTypeTwo());
				vo.setServTypeThree(supervisionInfo.getServTypeThree());
				vo.setServTypeFour(supervisionInfo.getServTypeFour());
			}
		} else if(StatusConstant.OPERATION_TYPE_PLAN.equals(vo.getOperationType())){
			SupervisionUpPlan supervisionUpPlan = SpringUtil.getBean(ISupervisionUpPlanService.class).getById(vo.getServId());
			if(supervisionUpPlan!=null){
				vo.setServName(supervisionUpPlan.getServName());
				vo.setServCode(supervisionUpPlan.getServId()+"");
			}
		} else if(StatusConstant.OPERATION_TYPE_QUARTERAPPRISEHB.equals(vo.getOperationType()) ||
			StatusConstant.OPERATION_TYPE_QUARTERAPPRISESCORE.equals(vo.getOperationType()) ||
			StatusConstant.OPERATION_TYPE_QUARTERAPPRISEXF.equals(vo.getOperationType())
		){//季度评价
			QuarterlyEvaluation quarterlyEvaluation = SpringUtil.getBean(IQuarterlyEvaluationService.class).getById(vo.getServId());
			if (quarterlyEvaluation != null) {
				vo.setServName(quarterlyEvaluation.getMajorTarget());
				vo.setServCode(String.valueOf(quarterlyEvaluation.getId()));
			}
		} else if(StatusConstant.OPERATION_TYPE_ANNUALAPPRISEXF.equals(vo.getOperationType()) ||
			StatusConstant.OPERATION_TYPE_ANNUALAPPRISESCORE.equals(vo.getOperationType()) ||
			StatusConstant.OPERATION_TYPE_ANNUALAPPRISEHB.equals(vo.getOperationType())
		){//年度评价
			AnnualEvaluation annualEvaluation = SpringUtil.getBean(IAnnualEvaluationService.class).getById(vo.getServId());
			if (annualEvaluation != null) {
				vo.setServName(annualEvaluation.getMajorTarget());
				vo.setServCode(String.valueOf(annualEvaluation.getId()));
			}
		} else if(StatusConstant.OPERATION_TYPE_ADDSCORE.equals(vo.getOperationType())){//绩效考核加分项
			ScoreAdd scoreAdd = SpringUtil.getBean(IScoreAddService.class).getById(vo.getServId());
			if(scoreAdd!=null){
				vo.setServName(scoreAdd.getWinProject());
				vo.setServCode(String.valueOf(scoreAdd.getId()));
			}
		}/* else if(StatusConstant.OPERATION_TYPE_MINUSSCORE.equals(vo.getOperationType())){//绩效考核减分项
			ScoreMinus scoreMinus = SpringUtil.getBean(IScoreMinusService.class).getById(vo.getServId());
			if(scoreMinus!=null){
				vo.setServName(scoreMinus.getMinusProject());
				vo.setServCode(String.valueOf(scoreMinus.getId()));
			}
		}*/else if(StatusConstant.OPERATION_TYPE_WARE.equals(vo.getOperationType())){
			ProjectSummary projectSummary = SpringUtil.getBean(IProjectSummaryService.class).getById(vo.getServId());
			if(projectSummary!=null){
				vo.setServName(projectSummary.getTitle());
				vo.setServCode(String.valueOf(projectSummary.getId()));
			}
		}

		return vo;
	}
}

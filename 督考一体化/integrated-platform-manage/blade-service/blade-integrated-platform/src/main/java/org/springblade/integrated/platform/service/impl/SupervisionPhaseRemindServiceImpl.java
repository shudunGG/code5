package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.SupervisionPhasePlan;
import com.vingsoft.entity.SupervisionPhaseRemind;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.mapper.SupervisionPhaseRemindMapper;
import org.springblade.integrated.platform.service.ISupervisionPhaseRemindService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* @Description:    服务实现类
* @Author:         shaozhubing
* @CreateDate:     2022/4/9 2:29
* @Version:        1.0
*/
@Service
public class SupervisionPhaseRemindServiceImpl extends BaseServiceImpl<SupervisionPhaseRemindMapper, SupervisionPhaseRemind> implements ISupervisionPhaseRemindService {
	@Override
	public boolean updateList(List<SupervisionPhaseRemind> supervisionPhaseRemindList, String servCode,String phaseCode) {
		//获取数据库中原阶段数据
		QueryWrapper<SupervisionPhaseRemind> wrapperRemind=new QueryWrapper<>();
		wrapperRemind.eq("serv_code", servCode);
		wrapperRemind.eq("phase_Code", phaseCode);
		List<SupervisionPhaseRemind> SupervisionPhaseRemindListOld = this.list(wrapperRemind);
		Set delIds=new HashSet();
		for(SupervisionPhaseRemind remindOld:SupervisionPhaseRemindListOld){
			Long idOld=remindOld.getId();
			boolean isdel=false;
			for(SupervisionPhaseRemind remind: supervisionPhaseRemindList){
				Long id=remind.getId();
				if(idOld==id){
					isdel=false;
					break;
				}else{
					isdel=true;
				}
				if(ObjectUtil.isEmpty(id)){
					remind.setServCode(servCode);
				}
			}
			if(isdel){
				delIds.add(idOld);
			}
		}
		for(SupervisionPhaseRemind remind: supervisionPhaseRemindList){
			remind.setServCode(servCode);
		}
		boolean flag=true;
		if(!delIds.isEmpty()){
			flag = this.removeByIds(delIds);
		}
		if(flag){
			flag = this.saveOrUpdateBatch(supervisionPhaseRemindList);
		}

		return flag;
	}
}

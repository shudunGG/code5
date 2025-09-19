package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.FollowInformation;
import com.vingsoft.entity.SupervisionLog;
import org.springblade.common.constant.PropConstant;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.constant.Constants;
import org.springblade.integrated.platform.mapper.SupervisionLogMapper;
import org.springblade.integrated.platform.service.IFollowInformationService;
import org.springblade.integrated.platform.service.ISupervisionLogService;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @Description:    服务实现类
* @Author:         shaozhubing
* @CreateDate:     2022/4/9 2:29
* @Version:        1.0
*/
@Service
public class SupervisionLogServiceImpl extends BaseServiceImpl<SupervisionLogMapper, SupervisionLog> implements ISupervisionLogService {


	@Autowired
	private IFollowInformationService followInformationService;

	@Resource
	private IUserClient userClient;


	@Override
	public List<SupervisionLog> listQueryWrapper(Map<String, Object> entity, BladeUser user) {
		Set userId=new HashSet();
		Set leaderDeptId=new HashSet();
		leaderDeptId.add(PropConstant.getSwldDeptId());
		leaderDeptId.add(PropConstant.getSzfldDeptId());
		QueryWrapper<SupervisionLog> wrapper=new QueryWrapper<>();
		wrapper.eq("1",1);
		String deptId="";
		String AutUserId="";
		if(ObjectUtil.isNotEmpty(user)){
			//当前用户所在部门
			deptId=user.getDeptId();
			AutUserId=user.getUserId().toString();
		}
		if(ObjectUtil.isNotEmpty(user)){
			boolean flag=true;
			if(leaderDeptId.contains(user.getDeptId())){
			}else {
				if(flag){
					if(ObjectUtil.isEmpty(user.getPostId())||!user.getPostId().equals(Constants.USER_POST_DEPT_LD_id)){
						R<List<User>> userLeader = userClient.getUserLeader(user.getDeptId(), user.getPostId());
						List<User> users = userLeader.getData();
						for(User u:users){
							userId.add(u.getId());
						}
					}
				}
			}
		}
		//获取领导关注的事项id
		QueryWrapper<FollowInformation> wrapperF=new QueryWrapper<>();
		Set finalLeaderDeptId = leaderDeptId;
		if(ObjectUtil.isEmpty(finalLeaderDeptId)&&ObjectUtil.isEmpty(userId)){
			wrapperF.ne("1",1);
		}
		if(ObjectUtil.isNotEmpty(finalLeaderDeptId)){
			wrapperF.and(i->{
				if(ObjectUtil.isNotEmpty(finalLeaderDeptId)){
					i.or().in("follow_Dept_Id",finalLeaderDeptId);
				}
				if(ObjectUtil.isNotEmpty(userId)){
					i.or().in("follow_User_Id",userId);
				}
			});
		}
		wrapperF.eq("business_Type","1");
		wrapperF.eq("status","1");
		wrapperF.select("distinct business_Id");
		List<Object> list = followInformationService.listObjs(wrapperF);
		if(ObjectUtil.isNotEmpty(user)){
			String[] deptIds = user.getDeptId().split(",");
			boolean isNoleader=false;
			for(String id:deptIds){
				if(leaderDeptId.contains(id)){
					isNoleader=false;
					break;
				}else{
					isNoleader=true;
				}
			}
			if(isNoleader){
				wrapper.and(i->{
					i.or().apply("INSTR(info.lead_unit,"+user.getDeptId()+")>0");
					i.or().apply("INSTR(info.duty_Unit,"+user.getDeptId()+")>0");
					i.or().eq("info.create_user",user.getUserId());
					i.or().apply("INSTR(audit.user_id,'"+user.getUserId()+"')>0");
					i.or().eq("plan.down_User_Id",user.getUserId());
					if(ObjectUtil.isNotEmpty(list)){
						i.or().in("info.id",list);
					}
					i.apply("INSTR(info.lead_unit,"+user.getDeptId()+")>0")
					    .apply("INSTR(info.duty_Unit,"+user.getDeptId()+")>0")
					    .eq("info.create_user",user.getUserId())
						.apply("INSTR(audit.user_id,'"+user.getUserId()+"')>0")
					    .eq("plan.down_User_Id",user.getUserId());
				});
			}
		}
		wrapper.orderByDesc("operation_Time");
		wrapper.last("limit "+entity.get("limit")+"");
		return this.baseMapper.listQueryWrapper(wrapper,deptId,AutUserId);
	}
}

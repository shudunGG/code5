package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vingsoft.entity.SupervisionSubmitAudit;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.integrated.platform.constant.StatusConstant;
import org.springblade.integrated.platform.mapper.SupervisionSubmitAuditMapper;
import org.springblade.integrated.platform.service.ISupervisionSubmitAuditService;
import org.springblade.system.cache.SysCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

/**
 * @author mrtang
 * @version 1.0
 * @description: TODO
 * @date 2022/4/18 17:33
 */
@Slf4j
@Service
public class SupervisionSubmitAuditServiceImpl extends ServiceImpl<SupervisionSubmitAuditMapper, SupervisionSubmitAudit> implements ISupervisionSubmitAuditService {
	@Autowired
	private  IUserClient userClient;

	@Override
	public boolean updateNextUserStatus(Long servId, Integer sort, String batchNumber) {
		return baseMapper.updateNextUserStatus(servId,sort,batchNumber)>0;
	}

	@Override
	public int countAuditNumber(Long servId,String batchNumber) {
		return baseMapper.countAuditNumber(servId,batchNumber);
	}

	@Override
	@Transactional
	public boolean saveSubmitAudit(String id, String title, String userIds, String sync,String operationType) {
		try {
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			String deptName = SysCache.getDeptName(Long.valueOf(user.getDeptId()));
			String[] userIdArr = userIds.split(",");
			String batchNumber = UUID.randomUUID().toString();
			for(int i=0;i<userIdArr.length;i++){
				String userId = userIdArr[i];
				SupervisionSubmitAudit audit = new SupervisionSubmitAudit();
				audit.setBatchNumber(batchNumber);
				audit.setServId(Long.parseLong(id));
				audit.setTitle(title);
				audit.setUserId(Long.parseLong(userId));
				audit.setDeptName(deptName);
				audit.setCreateUser(user.getId());
				audit.setCreateTime(new Date());
				if(StatusConstant.AUDIT_SYNC_1.equals(sync)){
					audit.setStatus(0);
				}else{
					audit.setStatus(i==0?0:3);
				}
				audit.setSort(i);
				audit.setSync(Integer.parseInt(sync));
				audit.setOperationType(operationType);
				this.save(audit);
			}
			return true;
		} catch (Exception e){
			log.error(e.getMessage());
			return false;
		}
	}

	/**
	 * 根据项目id和登录人信息获取待审核信息
	 * @param servId
	 * @param userId
	 * @return
	 */
	@Override
	public SupervisionSubmitAudit getAuditByservIdAndUserId(Long servId,Long userId){
		return baseMapper.getAuditByservIdAndUserId(servId, userId);
	}
}

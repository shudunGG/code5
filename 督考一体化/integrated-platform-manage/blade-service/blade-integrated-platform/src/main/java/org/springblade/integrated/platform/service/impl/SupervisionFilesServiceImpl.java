package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.SupervisionPhasePlan;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.ObjectUtil;
import org.springblade.integrated.platform.common.utils.DateUtils;
import org.springblade.integrated.platform.mapper.SupervisionFilesMapper;
import org.springblade.integrated.platform.service.ISupervisionFilesService;
import org.springframework.stereotype.Service;

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
public class SupervisionFilesServiceImpl extends BaseServiceImpl<SupervisionFilesMapper, SupervisionFiles> implements ISupervisionFilesService {
	@Override
	public boolean updateList(List<SupervisionFiles> supervisionFilesList, String servCode) {
		boolean flag=true;
		QueryWrapper<SupervisionFiles> wrapperFiles=new QueryWrapper<>();
		wrapperFiles.eq("serv_code",servCode);
		wrapperFiles.eq("file_from","1");
		List<SupervisionFiles> supervisionFilesListOld= this.list(wrapperFiles);
		Set delIds=new HashSet();
		if(supervisionFilesList.isEmpty()){
			if(!supervisionFilesListOld.isEmpty()){
				flag = this.remove(wrapperFiles);
			}
		}else {
			for(SupervisionFiles fileOld:supervisionFilesListOld){
				Long idOld=fileOld.getId();
				boolean isdel=false;
				for(SupervisionFiles file: supervisionFilesList){
					Long id=file.getId();
					if(idOld==id){
						isdel=false;
						break;
					}else{
						isdel=true;
					}
					if(ObjectUtil.isEmpty(id)){
						file.setServCode(servCode);
					}
				}
				if(isdel){
					delIds.add(idOld);
				}
			}
		}
		if(!delIds.isEmpty()){
			flag = this.removeByIds(delIds);
		}
		for(SupervisionFiles file: supervisionFilesList){
			BladeUser user = AuthUtil.getUser();
			file.setServCode(servCode);
			file.setUploadUser(user.getUserId().toString());
			file.setFileFrom("1");
			file.setUploadUserName(user.getNickName());
			file.setUploadTime(DateUtils.getNowDate());
		}
		if(flag&&!supervisionFilesList.isEmpty()){
			this.saveOrUpdateBatch(supervisionFilesList);
		}
		return flag;
	}

}

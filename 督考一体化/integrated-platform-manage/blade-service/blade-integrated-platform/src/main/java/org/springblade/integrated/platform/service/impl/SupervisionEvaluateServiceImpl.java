package org.springblade.integrated.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vingsoft.entity.SupervisionEvaluate;
import com.vingsoft.vo.SupervisionEvaluateVo;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.integrated.platform.mapper.SupervisionEvaluateMapper;
import org.springblade.integrated.platform.service.ISupervisionEvaluateService;
import org.springblade.system.cache.SysCache;
import org.springblade.system.feign.ISysClient;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.system.user.feign.IUserSearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description:    服务实现类
 * @Author:         shaozhubing
 * @CreateDate:     2022/4/9 2:29
 * @Version:        1.0
 */
@Service
public class SupervisionEvaluateServiceImpl extends BaseServiceImpl<SupervisionEvaluateMapper, SupervisionEvaluate> implements ISupervisionEvaluateService {
	@Resource
	private ISysClient sysClient;

	@Autowired
	private  IUserClient userClient;

	@Resource
	private IUserSearchClient userSearchClient;
	@Override
	public List<SupervisionEvaluateVo> getDcdb(String type, String year) {
		QueryWrapper<SupervisionEvaluateVo> wrapper= new QueryWrapper<>();
		List<String> list = new ArrayList();
		//分管部门
		if(type.equals("1")){
			//获取当前用户的分管部门
			User user = userClient.userInfoById(AuthUtil.getUserId()).getData();
			String manageDept = user.getManageDept();
			String[] arr = manageDept.split(",");
			list= Arrays.asList(arr);
		}
		//县区
		if(type.equals("2")){
			//获取分组后的单位id
			R<String> Rdeptids = sysClient.getDeptIdsByGroup("000000","1");
			String deptids = Rdeptids.getData();
			String[] arr = deptids.split(",");
			list= Arrays.asList(arr);
		}
		//其他部门(展示七个分组的数据)
		if(type.equals("3")){
			//查询所有单位id
			R<String> Rdeptids = sysClient.getOtherDeptIds("000000","");
			String deptids = Rdeptids.getData();
			String[] arr = deptids.split(",");
			list= Arrays.asList(arr);
		}
		List<SupervisionEvaluateVo> resultList = this.baseMapper.queryDcdbList(year,list);
		//获取返回的单位信息
		String[] str = new String[resultList.size()];
		for (int i = 0; i < resultList.size(); i++) {
			str[i] = resultList.get(i).getDeptId();
			String deptName= SysCache.getDeptName(Long.valueOf(resultList.get(i).getDeptId()));
			resultList.get(i).setDeptName(deptName);
		}
		//去重
		List<String> list1 = new ArrayList<String>();
		for (String v : str) {
			if (!list1.contains(v)) {
				list1.add(v);
			}
		}


		if(type.equals("3")){//其他部门展示7个分组
			List<SupervisionEvaluateVo> resultinfoList = new ArrayList<>();
			Integer cbnum = 0;
			Integer ywcnum= 0;;
			Integer zctjnum= 0;;
			Integer ycqnum= 0;;
			String  deptName="";
			for(int i=2;i<9;i++){
				if(i==2){
					deptName="市直综合部门";
				}else if(i==3){
					deptName="市直经济部门";
				}else if(i==4){
					deptName="市直社会发展部门";
				}else if(i==5){
					deptName="市直其他部门";
				}else if(i==6){
					deptName="市直学校科研院所";
				}else if(i==7){
					deptName="市属其他事业单位";
				}else if(i==8){
					deptName="市属国有企业";
				}
				//获取分组后的单位id
				R<String> Rdeptids = sysClient.getDeptIdsByGroup("000000",i+"");
				List<String> listInfo = new ArrayList<>();
				String deptids = Rdeptids.getData();
				String[] arr = deptids.split(",");
				listInfo= Arrays.asList(arr);
				for(SupervisionEvaluateVo sev : resultList){
					if(listInfo.contains(sev.getDeptId())){
						cbnum+=Integer.parseInt(sev.getCbnum()==null || sev.getCbnum()==""?"0":sev.getCbnum());
						ywcnum=Integer.parseInt(sev.getYwcnum()==null || sev.getYwcnum()==""?"0":sev.getYwcnum());
						zctjnum=Integer.parseInt(sev.getZctjnum()==null || sev.getZctjnum()==""?"0":sev.getZctjnum());
						ycqnum=Integer.parseInt(sev.getYcqnum()==null || sev.getYcqnum()==""?"0":sev.getYcqnum());
					}
				}
				SupervisionEvaluateVo supervisionEvaluateVo = new SupervisionEvaluateVo();
				String deptId = sysClient.getDeptIds("000000",deptName).getData();
				supervisionEvaluateVo.setDeptName(deptName);
				supervisionEvaluateVo.setCbnum(cbnum+"");
				supervisionEvaluateVo.setYwcnum(ywcnum+"");
				supervisionEvaluateVo.setZctjnum(zctjnum+"");
				supervisionEvaluateVo.setYcqnum(ycqnum+"");
				supervisionEvaluateVo.setScore(0.00);
				resultinfoList.add(supervisionEvaluateVo);
			}
			return resultinfoList;
		}else{
			for(int i=0;i<list.size();i++){
				String listDeptIdStr = list1.toString();
				if (!listDeptIdStr.contains(list.get(i))) {
					SupervisionEvaluateVo supervisionEvaluateVo = new SupervisionEvaluateVo();
					supervisionEvaluateVo.setDeptId(list.get(i));
					String deptNamestr = SysCache.getDeptName(Long.valueOf(list.get(i)));
					supervisionEvaluateVo.setDeptName(deptNamestr);
					supervisionEvaluateVo.setCbnum("0");
					supervisionEvaluateVo.setYwcnum("0");
					supervisionEvaluateVo.setZctjnum("0");
					supervisionEvaluateVo.setYcqnum("0");
					supervisionEvaluateVo.setScore(0.00);
					resultList.add(supervisionEvaluateVo);
				}
			}
		}
		return resultList;
	}
}

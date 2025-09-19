package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionLate;
import com.vingsoft.vo.SupervisionLateVO;
import org.springblade.core.mp.base.BaseService;

/**
* @Description:    类的描述
* @Author:         WangRJ
* @CreateDate:     2022/5/20 23:44
* @Version:        1.0
*/
public interface ISupervisionLateService extends BaseService<SupervisionLate> {

	boolean saveOrUpdate(SupervisionLateVO vo);
}

package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SupervisionEvaluate;
import com.vingsoft.vo.SupervisionEvaluateVo;
import org.springblade.core.mp.base.BaseService;

import java.util.List;


/**
 *
 *  @author: shaozhubing
 *  @Date: 2022/1/13 10:33
 *  @Description: 服务类
 */
public interface ISupervisionEvaluateService extends BaseService<SupervisionEvaluate> {

	public List<SupervisionEvaluateVo> getDcdb(String type, String year);

}

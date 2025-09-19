package org.springblade.integrated.platform.service;

import com.vingsoft.entity.SingleCheckAppraise;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.api.R;

import java.util.List;

/**
 * @className: ISingleCheckAppraiseService
 * @description: 类作用描述
 * @author: Waston.FR
 * @date: 2023/4/11 15:11 星期二
 * @Version 1.0
 **/
public interface ISingleCheckAppraiseService extends BaseService<SingleCheckAppraise> {
    R<List<SingleCheckAppraise>> getIndexStatistics(SingleCheckAppraise singleCheckAppraise);
}

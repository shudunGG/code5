package org.springblade.integrated.platform.service;/**
 * @author TangYanXing
 * @date 2022-04-09 16:36
 */

import com.baomidou.mybatisplus.extension.service.IService;
import com.vingsoft.entity.StageInformation;
import org.springblade.core.mp.base.BaseService;

import java.util.List;

/**
 * @author TangYanXing
 * @version 1.0
 * @description:
 * @date 2022-04-09 16:36
 */
public interface IStageInformationService extends BaseService<StageInformation> {

	boolean saveList(List<StageInformation> stageInformationList);

	boolean uptList(List<StageInformation> stageInformationList);

}

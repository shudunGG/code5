package com.vingsoft.vo;

import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.SupervisionSubmitAudit;
import com.vingsoft.entity.SupervisionUpPlan;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @Description:    上报计划视图
* @Author:         WangRJ
* @CreateDate:     2022/4/21 17:24
* @Version:        1.0
*/
@Data
public class SupervisionUpPlanVO extends SupervisionUpPlan {

	private String userId;

	private String sync;

	private String title;

	private SupervisionSubmitAudit supervisionSubmitAudit;

	private List<SupervisionFiles> supervisionFilesList;
}

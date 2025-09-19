package com.vingsoft.vo;

import com.vingsoft.entity.ProjectSpecialist;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;

/**
 * @ Date       ：Created in 2025年02月14日10时29分11秒
 * @ Description：项目专员和项目关联表视图实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "ProjectSpecialistVO对象", description = "项目专员和项目关联表")
public class ProjectSpecialistVO extends ProjectSpecialist {
	private static final long serialVersionUID = -3864798924933374966L;

}

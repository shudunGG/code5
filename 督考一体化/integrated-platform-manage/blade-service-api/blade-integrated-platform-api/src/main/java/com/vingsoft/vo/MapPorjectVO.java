package com.vingsoft.vo;

import com.vingsoft.entity.ScoreAdd;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 视图实体类
 *
 * @Author AdamJin
 * @Create 2022-4-15 14:59:13
 */
@Data
@ApiModel(value = "项目地图视图", description = "项目地图视图")
public class MapPorjectVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 项目id */
	private Long id;

	/** 项目名称*/
	private String title;

	/** 项目图片 */
	private String imageUrl;

	/** 项目业主单位 */
	private String sgdw;

	/** 项目地址 */
	private String xmAddress;

	/** 项目地点 */
	private String ddAddress;

	/**项目类型**/
	private String xmType;

}

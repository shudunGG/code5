package com.vingsoft.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vingsoft.entity.SupervisionFiles;
import com.vingsoft.entity.SupervisionUrge;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * supervision_urge
 * @author
 */
@Data
public class SupervisionUrgeVo extends SupervisionUrge {
	/**
	 * 催办人员
	 */
	private String urgeUserName;
}

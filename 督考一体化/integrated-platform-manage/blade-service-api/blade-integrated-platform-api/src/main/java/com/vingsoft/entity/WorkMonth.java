package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vingsoft.support.mybatis.SM4EncryptHandler;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;

@Data
public class WorkMonth extends BaseEntity {

	private String month;

	private String deptName;

	private String deptCode;

	private String sort;

	private String concent;

	@TableField(typeHandler = SM4EncryptHandler.class)
	private String personLiable;

	private String planTime;

	private String completion;

	private String remarks;

	private String createUserName;

	private String createUserCode;

	private String updateDate;

	private String updateUserName;

	private String updateUserCode;

	private String type;

	private String jhqk;

	private String sjTime;

	@TableField(typeHandler = SM4EncryptHandler.class)
	private String hbPerson;

	private String jhFilleName;

	private String jhFilleUrl;

	private String hbFilleName;

	private String hbFilleUrl;

	private String jhFileType;

	private String hbFileType;

	private String hbCompletion;

	/**
	 * 具体进展情况
	 */
	private String progressDetail;

}

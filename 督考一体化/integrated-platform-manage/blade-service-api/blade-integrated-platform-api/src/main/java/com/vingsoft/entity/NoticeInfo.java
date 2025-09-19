package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * supervision_info
 * @author
 */
@Data
@TableName("notice_info")
public class NoticeInfo extends BaseEntity implements Serializable {


    /**
     * 标题
     */
    private String title;

    /**
     * 封面图片
     */
    private String coverPhoto;

    /**
     * 内容
     */
    private String content;

    /**
     * 发布状态
     */
    private String releaseStatus;

    /**
     * 发布时间
     */
    private Date releaseTime;

    /**
     * 发布人
     */
    private Long releaseUser;

    /**
     * 发布人名称
     */
    private String releaseUserName;

    /**
     * 发布部门
     */
    private Long releaseDept;

	/**
	 * 发布部门名称
	 */
	private String releaseDeptName;

	/**
	 * 附件
	 */
	private String filesUrl;

	/**
	 * 附件名
	 */
	private String filesName;

	/**
	 * 类型
	 */
	private String noticeType;

	/**
	 * 是否APP显示
	 */
	private String isApp;

	/**
	 * 正文附件地址
	 */
	private String textFileUrl;
	/**
	 * 正文附件名
	 */
	private String textFileName;

	@TableField(exist = false)
	private Integer status;


}

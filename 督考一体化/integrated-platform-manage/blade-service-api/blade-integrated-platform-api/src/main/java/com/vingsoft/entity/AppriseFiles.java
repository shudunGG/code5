package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * 考核评价文件信息表
 *
 * @Author JG🧸
 * @Create 2022/4/21 16:05
 */
@Data
@TableName("apprise_files")
public class AppriseFiles extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 业务表名
	 */
	private String businessTable;

	/**
	 * 业务ID
	 */
	private Long businessId;

	/**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小
     */
    private String fileSize;

    /**
     * 文件来源
     */
    private String fileFrom;

    /**
     * 上传人名称
     */
    private String uploadUserName;

    /**
     * 文件地址
     */
    private String fileUrl;



}

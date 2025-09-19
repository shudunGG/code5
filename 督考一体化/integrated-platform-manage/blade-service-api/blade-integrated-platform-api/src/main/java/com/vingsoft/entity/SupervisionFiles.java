package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * supervision_files
 * @author
 */
@Data
@TableName("supervision_files")
public class SupervisionFiles extends BaseEntity implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 事项编号
     */
    private String servCode;

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
	 * 文件来源(1.基本信息2.上报计划3.汇报4.催办附件5.阶段汇报附件6.上报计划送审-审核7.阶段汇报送审-审核)
     */
    private String fileFrom;

    /**
     * 上传人员
     */
    private String uploadUser;

    /**
     * 上传人员名称
     */
    private String uploadUserName;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 阶段ID
     */
    private Long phaseId;

    private static final long serialVersionUID = 1L;

	@TableField(exist = false)
	private Integer isDeleted;

	@TableField(exist = false)
	private Integer status;
}

package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目附件实体类
 * @author AdamJin 2022-4-9 16:58:17
 */
@Data
@TableName("project_files")
public class ProjectFiles  extends BaseEntity{
    /**
     * 项目id
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long projId;

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
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long phaseId;

	/**
	 * 附件格式
	 */
	@JsonSerialize(nullsUsing = NullSerializer.class)
	private Integer fileFormat;

	/**
	 * 文件后缀
	 */
	private String fileType;
}

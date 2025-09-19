package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目操作日志表
 * @author AdamJin 2022-4-9 17:28:13
 */
@Data
@TableName("project_log")
public class ProjectLog extends BaseEntity{

    /**
     * 操作人员
     */
    private String handleUser;
    /**
     * 操作人员单位名称
     */
    private String handleDept;

    /**
     * 操作类型
     */
    private String handleType;

    /**
     * 操作内容
     */
    private String handleContent;

    /**
     * 项目id
     */
    private Long projId;
}

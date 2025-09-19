package com.vingsoft.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目催办实体类
 * @author AdamJin 2022-4-9 17:33:28
 */
@Data
@TableName("project_urge")
public class ProjectUrge extends BaseEntity {

    /**
     * 项目id
     */
	@JsonSerialize(nullsUsing = NullSerializer.class)
    private Long projId;

    /**
     * 催办人员
     */
    private String urgeUser;

    /**
     * 催办时间
     */
    private Date urgeTime;

    /**
     * 被催办单位
     */
    private String urgedUnit;

    /**
     * 被催办单位名称
     */
    private String urgedUnitName;

    /**
     * 催办内容
     */
    private String content;

}

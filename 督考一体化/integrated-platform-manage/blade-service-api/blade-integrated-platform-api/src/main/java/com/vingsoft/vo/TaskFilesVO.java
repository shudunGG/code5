package com.vingsoft.vo;

import com.vingsoft.entity.TaskFiles;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视图实体类
 *
 * @Author JG🧸
 * @Create 2022/4/18 13:47
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TaskFilesVO对象", description = "TaskFilesVO对象")
public class TaskFilesVO extends TaskFiles {


}

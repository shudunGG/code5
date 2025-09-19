package com.vingsoft.vo;

import com.vingsoft.entity.ReminderRecord;
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
@ApiModel(value = "ReminderRecordVO对象", description = "ReminderRecord对象")
public class ReminderRecordVO extends ReminderRecord {


}

package com.vingsoft.vo;

import com.vingsoft.entity.AppTask;
import com.vingsoft.entity.SupervisionFiles;
import lombok.Data;

import java.util.List;

/**
* @Description:    领导交办视图类
* @Author:         WangRJ
* @CreateDate:     2022/5/10 15:32
* @Version:        1.0
*/
@Data
public class AppTaskVO extends AppTask {

	private String isTask;

	/**
	 * 交办附件
	 */
	private List<SupervisionFiles> filesList;
}

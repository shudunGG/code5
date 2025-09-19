package com.om.common.util;

import com.om.bo.base.Const;
import com.om.bo.base.PDto;
import com.om.bo.base.ResultEnum;

import java.util.List;

public class ResultUtil {
	public static PDto getRtnInfo(ResultEnum resultEnum) {
		PDto result = new PDto();
		result.put(Const.RESP_CODE, resultEnum.getCode());
		result.put(Const.RESP_MSG, resultEnum.getMessage());
		return result;
	}
	
	public static PDto getRtnInfo(ResultEnum resultEnum,PDto data) {
		PDto result = new PDto();
		result.put(Const.RESP_CODE, resultEnum.getCode());
		result.put(Const.RESP_MSG, resultEnum.getMessage());
		result.put(Const.RESP_DATA, data);
		return result;
	}

	public static PDto getRtnInfo(ResultEnum resultEnum,List dataList) {
		PDto result = new PDto();
		result.put(Const.RESP_CODE, resultEnum.getCode());
		result.put(Const.RESP_MSG, resultEnum.getMessage());
		result.put(Const.RESP_LIST, dataList);
		return result;
	}
}

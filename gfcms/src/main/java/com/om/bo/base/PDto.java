package com.om.bo.base;


import java.util.HashMap;

public class PDto extends HashMap {
	public PDto() {
		//this.put("TAB_PRE", Const.MY_SYS_PRE);
	}
	public PDto(HashMap dto) {
		this.putAll(dto);
	}

}

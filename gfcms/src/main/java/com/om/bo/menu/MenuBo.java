package com.om.bo.menu;

import java.util.ArrayList;
import java.util.List;

public class MenuBo {
	private String title;
	private String key;
	private String icon;
	private String value;
	private List<MenuBo> children = new ArrayList<MenuBo>();

	public MenuBo(String title, String key, String icon) {
		this.title = title;
		this.key = key;
		this.icon = icon;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public List<MenuBo> getChildren() {
		return children;
	}

	public void setChildren(List<MenuBo> children) {
		this.children = children;
	}
}

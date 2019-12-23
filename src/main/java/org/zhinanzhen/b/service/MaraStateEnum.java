package org.zhinanzhen.b.service;

public enum MaraStateEnum {

	ENABLED("激活"), DISABLED("禁止");

	private String value;

	private MaraStateEnum(String value) {
		this.value = value;
	}

	public static MaraStateEnum get(String name) {
		for (MaraStateEnum e : MaraStateEnum.values()) {
			if (e.toString().equals(name)) {
				return e;
			}
		}
		return null;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}

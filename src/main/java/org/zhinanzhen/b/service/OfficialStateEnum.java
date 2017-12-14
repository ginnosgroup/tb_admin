package org.zhinanzhen.b.service;

public enum OfficialStateEnum {

	ENABLED("激活"), DISABLED("禁止");

	private String value;

	private OfficialStateEnum(String value) {
		this.value = value;
	}

	public static OfficialStateEnum get(String name) {
		for (OfficialStateEnum e : OfficialStateEnum.values()) {
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

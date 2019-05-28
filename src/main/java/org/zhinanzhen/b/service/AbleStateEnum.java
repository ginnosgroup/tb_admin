package org.zhinanzhen.b.service;

public enum AbleStateEnum {

	ENABLED("显示"), DISABLED("不显示");

	private String value;

	private AbleStateEnum(String value) {
		this.value = value;
	}

	public static AbleStateEnum get(String name) {
		for (AbleStateEnum e : AbleStateEnum.values()) {
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

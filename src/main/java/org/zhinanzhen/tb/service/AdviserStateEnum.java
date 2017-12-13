package org.zhinanzhen.tb.service;

public enum AdviserStateEnum {
	
	ENABLED("激活"), DISABLED("禁止");

	private String value;

	private AdviserStateEnum(String value) {
		this.value = value;
	}

	public static AdviserStateEnum get(String name) {
		for (AdviserStateEnum e : AdviserStateEnum.values()) {
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

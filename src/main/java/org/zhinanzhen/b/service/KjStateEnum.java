package org.zhinanzhen.b.service;

public enum KjStateEnum {

	ENABLED("激活"), DISABLED("禁止");

	private String value;

	private KjStateEnum(String value) {
		this.value = value;
	}

	public static KjStateEnum get(String name) {
		for (KjStateEnum e : KjStateEnum.values()) {
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

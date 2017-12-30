package org.zhinanzhen.b.service;

public enum ReceiveTypeStateEnum {

	ENABLED("显示"), DISABLED("不显示");

	private String value;

	private ReceiveTypeStateEnum(String value) {
		this.value = value;
	}

	public static ReceiveTypeStateEnum get(String name) {
		for (ReceiveTypeStateEnum e : ReceiveTypeStateEnum.values()) {
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

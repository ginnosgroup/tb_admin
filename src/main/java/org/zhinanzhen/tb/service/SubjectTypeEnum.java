package org.zhinanzhen.tb.service;

public enum SubjectTypeEnum {

	DEFAULT("大团"), INDIE("小团"), CHILD("子团");

	private String value;

	private SubjectTypeEnum(String value) {
		this.value = value;
	}

	public static SubjectTypeEnum get(String name) {
		for (SubjectTypeEnum e : SubjectTypeEnum.values()) {
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

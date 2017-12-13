package org.zhinanzhen.tb.service;

public enum SubjectCategoryStateEnum {

	ENABLED("显示"), DISABLED("不显示"), DELETE("删除");

	private String value;

	private SubjectCategoryStateEnum(String value) {
		this.value = value;
	}

	public static SubjectCategoryStateEnum get(String name) {
		for (SubjectCategoryStateEnum e : SubjectCategoryStateEnum.values()) {
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

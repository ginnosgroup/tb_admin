package org.zhinanzhen.tb.service;

public enum SubjectStateEnum {

	WAIT("未开始"), START("拼团中"), END("已结束"), STOP("已终止"), DELETE("已删除");

	private String value;

	private SubjectStateEnum(String value) {
		this.value = value;
	}

	public static SubjectStateEnum get(String name) {
		for (SubjectStateEnum e : SubjectStateEnum.values()) {
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

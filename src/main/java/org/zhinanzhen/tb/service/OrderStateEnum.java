package org.zhinanzhen.tb.service;

public enum OrderStateEnum {

	NEW("待付款"), WAIT("待成团"), SUCCESS("已成团"),END("未成团");

	private String value;

	private OrderStateEnum(String value) {
		this.value = value;
	}

	public static OrderStateEnum get(String name) {
		for (OrderStateEnum e : OrderStateEnum.values()) {
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

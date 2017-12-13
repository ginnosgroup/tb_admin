package org.zhinanzhen.tb.service;

public enum ListOrderEnum {
    USER("用户订单"), RECOMMEND("推荐的订单");

    private String value;

    private ListOrderEnum(String value) {
	this.value = value;
    }

    public static ListOrderEnum get(String name) {
	for (ListOrderEnum e : ListOrderEnum.values()) {
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

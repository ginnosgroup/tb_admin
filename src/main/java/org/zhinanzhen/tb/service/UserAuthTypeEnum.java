package org.zhinanzhen.tb.service;

public enum UserAuthTypeEnum {

	WECHAT("微信"), FACEBOOK("facebook"), BROKERAGE("佣金系统用户"), V("虚拟用户") ,WECHAT_WORK("企业微信");

	private String value;

	private UserAuthTypeEnum(String value) {
		this.value = value;
	}

	public static UserAuthTypeEnum get(String name) {
		for (UserAuthTypeEnum e : UserAuthTypeEnum.values()) {
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

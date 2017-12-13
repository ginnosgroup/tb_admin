package org.zhinanzhen.tb.service;

public enum OrderPayTypeEnum {

	PAYPAL("PayPal"), WECHAT("微信支付"), BALANCE("余额"), OTHER("其它支付方式"), IOSPAY("IOS第三方支付"),OFFSET("余额抵扣尾款"),REMAIN("尾款实际支付");

	private String value;

	private OrderPayTypeEnum(String value) {
		this.value = value;
	}

	public static OrderPayTypeEnum get(String name) {
		for (OrderPayTypeEnum e : OrderPayTypeEnum.values()) {
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

package org.zhinanzhen.tb.controller;

import lombok.Data;

/**
 * API接口返回对象
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 * @version 0.1
 */
@Data
public class Response<T> {

	/**
	 * 编码
	 */
	private int code;

	/**
	 * 信息
	 */
	private String message;

	/**
	 * 数据
	 */
	private T data;

	public Response(int code) {
		this.code = code;
		this.message = "";
	}

	public Response(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public Response(int code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public Response(int code, T data) {
		this.code = code;
		this.message = "";
		this.data = data;
	}

}
package org.zhinanzhen.tb.controller;

import lombok.Data;

/**
 * API列表接口返回对象
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 */
@Data
public class ListResponse<T> {

	/**
	 * 是否成功
	 */
	private boolean success;

	private int pageSize = 0;

	private int total = 0;

	/**
	 * 数据
	 */
	private T data;

	/**
	 * 信息
	 */
	private String message = "";

	public ListResponse(boolean success, int pageSize, int total, T data, String message) {
		this.success = success;
		this.pageSize = pageSize;
		this.message = message;
		this.data = data;
		this.total = total;
	}

}
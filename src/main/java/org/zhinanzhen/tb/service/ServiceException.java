package org.zhinanzhen.tb.service;

/**
 * 通用服务接口异常对象
 * 
 * @author <a href="mailto:leisu@zhinanzhen.org">sulei</a>
 * @version 0.1
 */
public class ServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	private int code;

	public ServiceException() {
		super();
	}

	public ServiceException(String msg) {
		super(msg);
	}

	public ServiceException(Exception e) {
		super(e);
	}

	public ServiceException(String msg, Exception e) {
		super(msg, e);
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}

}

package org.zhinanzhen.b.controller;

import lombok.Data;

@Data
public class UploadFileResponse {

	private int errno;

	private String[] data;

	public UploadFileResponse(int errno, String url) {
		this.errno = errno;
		this.data = new String[] { url };
	}

}

package org.zhinanzhen.b.controller;

import lombok.Data;

@Data
public class UploadImageResponse {

	private int errno;

	private String[] data;

	public UploadImageResponse(int errno, String url) {
		this.errno = errno;
		this.data = new String[] { url };
	}

}

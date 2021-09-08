package org.zhinanzhen.b.dao.pojo;

import java.io.Serializable;

import lombok.Data;

@Data
public class SchoolAttachmentsDO implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;

	private String schoolName;

	private String contractFile1;

	private String contractFile2;

	private String contractFile3;

	private String remarks = "";

	private int providerId;

}

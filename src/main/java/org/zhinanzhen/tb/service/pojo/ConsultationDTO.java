package org.zhinanzhen.tb.service.pojo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
public class ConsultationDTO {

	private Integer id;

	private Date gmtCreate;

	private Integer userId;

	private String userName;

	private String contents;

	@JsonInclude(Include.NON_NULL)
	private Date remindDate;

	private String remindContents;

}

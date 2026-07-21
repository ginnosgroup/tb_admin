package org.zhinanzhen.b.dao.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ContractPdfAnalysisCacheDO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Date gmtCreate;
    private Date gmtModify;
    private String fileHash;
    private String fileName;
    private String requestSource;
    private Integer requestUserId;
    private String status;
    private Integer responseCode;
    private String responseMessage;
    private String analysisResult;
}

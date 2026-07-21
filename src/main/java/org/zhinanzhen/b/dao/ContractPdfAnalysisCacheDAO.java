package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ContractPdfAnalysisCacheDO;

public interface ContractPdfAnalysisCacheDAO {

    ContractPdfAnalysisCacheDO getByFileHash(@Param("fileHash") String fileHash);

    int addIfAbsent(ContractPdfAnalysisCacheDO cache);

    int complete(@Param("fileHash") String fileHash,
                 @Param("status") String status,
                 @Param("responseCode") int responseCode,
                 @Param("responseMessage") String responseMessage,
                 @Param("analysisResult") String analysisResult);
}

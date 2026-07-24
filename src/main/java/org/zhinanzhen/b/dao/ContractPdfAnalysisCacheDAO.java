package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.ContractPdfAnalysisCacheDO;

import java.util.List;

public interface ContractPdfAnalysisCacheDAO {

    ContractPdfAnalysisCacheDO getByFileHash(@Param("fileHash") String fileHash);

    int addIfAbsent(ContractPdfAnalysisCacheDO cache);

    int complete(@Param("fileHash") String fileHash,
                 @Param("status") String status,
                 @Param("responseCode") int responseCode,
                 @Param("responseMessage") String responseMessage,
                 @Param("analysisResult") String analysisResult);

    int updateValidationResult(@Param("fileHash") String fileHash,
                               @Param("filePath") String filePath,
                               @Param("validationResult") String validationResult,
                               @Param("comparisonData") String comparisonData,
                               @Param("adviserId") Integer adviserId,
                               @Param("adviserName") String adviserName);

    int count();

    List<ContractPdfAnalysisCacheDO> list(@Param("offset") int offset,
                                          @Param("pageSize") int pageSize);
}

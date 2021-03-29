package org.zhinanzhen.b.service;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.FinanceBankDO;
import org.zhinanzhen.b.dao.pojo.FinanceCodeDO;
import org.zhinanzhen.b.service.pojo.FinanceBankCodeDTO;
import org.zhinanzhen.b.service.pojo.FinanceBankDTO;
import org.zhinanzhen.b.service.pojo.FinanceCodeDTO;
import org.zhinanzhen.b.service.pojo.RegionBankDTO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.pojo.AdviserDTO;

import java.io.InputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/18 13:35
 * Description:
 * Version: V1.0
 */
public interface VerifyService {

    List<FinanceCodeDO> excelToList(InputStream inputStream, String fileName) throws Exception;

    int add(List<FinanceCodeDO> financeCodeDOS);

    int count(String bankDateStart, String bankDateEnd, Integer regionId);

    List<FinanceCodeDTO> list(String bankDateStart, String bankDateEnd, Integer regionId, Integer pageSize, Integer pageNumber);

    int update(FinanceCodeDO financeCodeDO);

    List<RegionBankDTO> regionList();

    RegionDO regionById(int id);

    List<FinanceBankDTO> bankList(Integer pageNumber, Integer pageSize);

    int bankUpdate(FinanceBankDO financeBankDO);

    FinanceCodeDTO financeCodeByOrderId(String orderId);

    FinanceCodeDTO financeDTOByCode(String code);

    List<AdviserDTO> adviserList(Integer id);

    int bankCount();

    FinanceBankCodeDTO getPaymentCode(Integer adviserId);

    int updateFinanceBankId(Integer id, Integer financeBankId);

    int addBank(FinanceBankDO financeBankDO);

    FinanceCodeDO financeCodeById(Integer id);

}

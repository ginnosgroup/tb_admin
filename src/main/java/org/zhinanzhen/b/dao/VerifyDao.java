package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.FinanceBankDO;
import org.zhinanzhen.b.dao.pojo.FinanceCodeDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2020/12/18 13:52
 * Description:
 * Version: V1.0
 */
public interface VerifyDao {

    int add(@Param("financeCodeDOS") List<FinanceCodeDO> financeCodeDOS);

    int count(@Param("bankDateStart") String bankDateStart, @Param("bankDateEnd") String bankDateEnd,
              @Param("regionId") Integer regionId);

    List<FinanceCodeDO> list(@Param("bankDateStart") String bankDateStart, @Param("bankDateEnd") String bankDateEnd,
                             @Param("regionId") Integer regionId,
                             @Param("pageSize") Integer pageSize, @Param("pageNumber") Integer pageNumber);


    int update(FinanceCodeDO financeCodeDO);

    RegionDO regionById(@Param("id") int id);

    List<FinanceBankDO> bankList(@Param("pageNumber") Integer pageNumber,@Param("pageSize") Integer pageSize);

    int bankCount();

    int bankUpdate(FinanceBankDO financeBankDO);

    List<FinanceCodeDO> financeCodeByOrderId(@Param("orderId") String orderId);

    FinanceBankDO getFinanceBankById(@Param("financeBankId") int financeBankId);

    int addBank(FinanceBankDO financeBankDO);

    FinanceCodeDO financeCodeById(@Param("id") Integer id);

    List<FinanceCodeDO> getFinanceCodeOrderIdIsNull();
}

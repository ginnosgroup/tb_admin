package org.zhinanzhen.b.dao;

import org.apache.ibatis.annotations.Param;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;

import java.util.List;

public interface CustomerInformationDAO {

    int insert(CustomerInformationDO record);

    CustomerInformationDO get(int id);

    void update(CustomerInformationDO record);

    void delete(int id);

    CustomerInformationDO getByServiceOrderId(int serviceOrderId);

    CustomerInformationDO getByApplicantId(int applicantId);

    List<CustomerInformationDO> listByServiceOrderIds(@Param("serviceOrderIds") List<Integer> serviceOrderIds);

}

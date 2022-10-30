package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;

public interface CustomerInformationDAO {

    int insert(CustomerInformationDO record);

    CustomerInformationDO get(int id);

    void update(CustomerInformationDO record);

    void delete(int id);
}

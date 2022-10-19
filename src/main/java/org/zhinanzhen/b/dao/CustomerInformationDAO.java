package org.zhinanzhen.b.dao;

import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDTO;

public interface CustomerInformationDAO {

    int insert(CustomerInformationDTO record);

    CustomerInformationDTO get(int id);

    void update(CustomerInformationDTO record);

    void delete(int id);
}

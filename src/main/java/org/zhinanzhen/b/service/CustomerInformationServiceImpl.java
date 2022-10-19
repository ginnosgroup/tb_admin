package org.zhinanzhen.b.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ikasoa.core.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDTO;
import org.zhinanzhen.b.dao.CustomerInformationDAO;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import java.lang.reflect.Field;

@Service
public class CustomerInformationServiceImpl implements CustomerInformationService {
    @Resource
    private CustomerInformationDAO customerInformationDAO;

    @Override
    public void add(CustomerInformationDO customerInformationDO) {
        CustomerInformationDTO dto = putCustomerInformationDO(customerInformationDO);
        dto.setServiceOrderId(customerInformationDO.getServiceOrderId());
        customerInformationDAO.insert(dto);
    }

    @Override
    public CustomerInformationDO get(int id) throws ServiceException {
        CustomerInformationDTO dto = customerInformationDAO.get(id);
        Field[] fields = dto.getClass().getDeclaredFields();
        CustomerInformationDO customerInformationDO = new CustomerInformationDO();
        Field[] fields1 = customerInformationDO.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {

            Field field = fields[i];
            Field field1 = fields1[i];
            field.setAccessible(true);
            field1.setAccessible(true);
            try {
                if (field.getType().equals(String.class)) {
                    field1.set(customerInformationDO, JSONObject.parseObject((String) field.get(dto), field1.getType()));

                } else {
                    field1.set(customerInformationDO, field.get(dto));
                }
            } catch (IllegalAccessException e) {
                ServiceException se = new ServiceException(e);
                se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
                throw se;
            }
        }
        return customerInformationDO;
    }

    @Override
    public void update(CustomerInformationDO record) throws ServiceException {
        CustomerInformationDTO dto = putCustomerInformationDO(record);
        customerInformationDAO.update( dto);
    }

    @Override
    public void delete(int id) {
        customerInformationDAO.delete(id);
    }

    public CustomerInformationDTO putCustomerInformationDO(CustomerInformationDO record){
        Class aClass = record.getClass();
        Field[] fields = aClass.getDeclaredFields();

        CustomerInformationDTO dto = new CustomerInformationDTO();
        Field[] fields1 = dto.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Field field1 = fields1[i];
            field.setAccessible(true);
            field1.setAccessible(true);

            if (field1.getType().equals(String.class)) {
                try {
                    field1.set(dto, JSONObject.toJSONString(field.get(record), SerializerFeature.WriteMapNullValue));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return dto;
    }
}

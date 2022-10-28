package org.zhinanzhen.b.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ObjectUtil;
import com.ikasoa.core.utils.StringUtil;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ServiceDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderApplicantDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDTO;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;

@Service
public class CustomerInformationServiceImpl extends BaseService implements CustomerInformationService {
    @Resource
    private CustomerInformationDAO customerInformationDAO;

    @Resource
    private ServiceOrderDAO serviceOrderDAO;

    @Resource
    private ServiceOrderApplicantDAO serviceOrderApplicantDao;

    @Resource
    OfficialDAO officialDAO;

    @Resource
    private ApplicantDAO applicantDao;

    @Resource
    private ServiceDAO serviceDao;

    @Resource
    private AdviserDAO adviserDao;

    @Override
    public void add(CustomerInformationDO customerInformationDO) {
        CustomerInformationDTO dto = putCustomerInformationDO(customerInformationDO);
        dto.setServiceOrderId(customerInformationDO.getServiceOrderId());
        customerInformationDAO.insert(dto);
        sendRemind(customerInformationDO.getServiceOrderId());
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

    public void sendRemind(int id) {
        ServiceOrderDO serviceOrderDo = serviceOrderDAO.getServiceOrderById(id);
        OfficialDO official = officialDAO.getOfficialById(serviceOrderDo.getOfficialId());
        String t = serviceOrderDo.getType();
        String type =getType(t);

                ApplicantDTO applicantDto = null;
        if (serviceOrderDo.getApplicantId() > 0)
            applicantDto = mapper.map(applicantDao.getById(serviceOrderDo.getApplicantId()), ApplicantDTO.class);
        applicantDto = buildApplicant(applicantDto, serviceOrderDo.getId(), serviceOrderDo.getNutCloud(),
                serviceOrderDo.getInformation());
            ServiceDO service = serviceDao.getServiceById(serviceOrderDo.getServiceId());

        sendMail(official.getEmail(), "你有一条新的客户资料更新请及时处理。",
                StringUtil.merge("订单号:",id,"<br/>","服务类型:",type,"/申请人名称:", ObjectUtil.isNotNull(applicantDto) ? applicantDto.getSurname() + " " + applicantDto.getFirstname()
                        : "unknown","/类型:",ObjectUtil.isNotNull(applicantDto) ?service.getName() + "(" + service.getCode() + ")":"unknown","/顾问:",adviserDao.getAdviserById(serviceOrderDo.getAdviserId()).getName(),
                "/文案:",officialDAO.getOfficialById(serviceOrderDo.getOfficialId()).getName(),"<br/>","属性:",getPeopleTypeStr(serviceOrderDo.getPeopleType()),"<br/>坚果云资料地址:",
                        applicantDto.getUrl(),"<br/>在线资料地址:",applicantDto.getUrl(),"<br/>客户基本信息:",applicantDto.getContent(),"<br/>备注:", serviceOrderDo.getRemarks(),
                        "<br/>驳回原因:", serviceOrderDo.getRefuseReason(),"<br/>创建时间:", serviceOrderDo.getGmtCreate(),"<br/><br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/serviceorderdetail/id?"
                                + id + "'>服务订单详情</a>")
        );
    }
    private String getPeopleTypeStr(String peopleType) {
        if ("1A".equalsIgnoreCase(peopleType))
            return "单人";
        else if ("1B".equalsIgnoreCase(peopleType))
            return "单人提配偶";
        else if ("2A".equalsIgnoreCase(peopleType))
            return "带配偶";
        else if ("XA".equalsIgnoreCase(peopleType))
            return "带孩子";
        else if ("XB".equalsIgnoreCase(peopleType))
            return "带配偶孩子";
        else if ("XC".equalsIgnoreCase(peopleType))
            return "其它";
        else
            return "未知";
    }

    private ApplicantDTO buildApplicant(ApplicantDTO applicantDto, Integer serviceOrderId, String notCloud,
                                        String information) {
        if (applicantDto == null)
            return applicantDto;
        List<ServiceOrderApplicantDO> serviceOrderApplicantDoList = serviceOrderApplicantDao.list(serviceOrderId,
                applicantDto.getId());
        if (serviceOrderApplicantDoList != null && serviceOrderApplicantDoList.size() > 0
                && serviceOrderApplicantDoList.get(0) != null) {
            applicantDto.setUrl(serviceOrderApplicantDoList.get(0).getUrl());
            applicantDto.setContent(serviceOrderApplicantDoList.get(0).getContent());
        }
        if (StringUtil.isEmpty(applicantDto.getUrl()))
            applicantDto.setUrl(notCloud);
        if (StringUtil.isEmpty(applicantDto.getContent()))
            applicantDto.setContent(information);
        return applicantDto;
    }
    private String getType(String type){
        String s = "";
        if ("VISA".equalsIgnoreCase(s)) {
            type = "签证";
        }else if ("OVST".equalsIgnoreCase(type)) {
            type = "留学";
        }else if ("SIV".equalsIgnoreCase(type)) {
            type = "独立技术移民";
        } else if ("NSV".equalsIgnoreCase(type)) {
            type = "雇主担保";
        } else if ("MT".equalsIgnoreCase(type)) {
            type = "曼拓";
        } else if ("ZX".equalsIgnoreCase(type)) {
            type = "咨询";
        }
        return s;
    }
}

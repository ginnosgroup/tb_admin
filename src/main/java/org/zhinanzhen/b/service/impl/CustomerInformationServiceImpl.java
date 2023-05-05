package org.zhinanzhen.b.service.impl;

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
import org.zhinanzhen.b.service.CustomerInformationService;
import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
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
    public void add(CustomerInformationDO customerInformationDO) throws ServiceException {
        try {


            customerInformationDAO.insert(customerInformationDO);
            sendRemind(customerInformationDO.getServiceOrderId());
        } catch (Exception e) {
            e.printStackTrace();
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public CustomerInformationDO get(int id) throws ServiceException {
        try {
            return customerInformationDAO.getByServiceOrderId(id);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }

    }

    @Override
    public void update(CustomerInformationDO record) throws ServiceException {
        try {
            customerInformationDAO.update(record);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public void delete(int id) throws ServiceException {
        try {

            customerInformationDAO.delete(id);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public CustomerInformationDO getByServiceOrderId(int serviceOrderId) throws ServiceException {
        try {
            return customerInformationDAO.getByServiceOrderId(serviceOrderId);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public CustomerInformationDO getByApplicantId(int applicantId) throws ServiceException {
        try {
            return customerInformationDAO.getByApplicantId(applicantId);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }


    public void sendRemind(int id) {
        ServiceOrderDO serviceOrderDo = serviceOrderDAO.getServiceOrderById(id);
        OfficialDO official = officialDAO.getOfficialById(serviceOrderDo.getOfficialId());
        String t = serviceOrderDo.getType();
        String type = getType(t);

        ApplicantDTO applicantDto = null;
        if (serviceOrderDo.getApplicantId() > 0)
            applicantDto = mapper.map(applicantDao.getById(serviceOrderDo.getApplicantId()), ApplicantDTO.class);
        applicantDto = buildApplicant(applicantDto, serviceOrderDo.getId(), serviceOrderDo.getNutCloud(),
                serviceOrderDo.getInformation());
        ServiceDO service = serviceDao.getServiceById(serviceOrderDo.getServiceId());

		sendMail(official.getEmail(), "你有一条新的客户资料更新请及时处理。", StringUtil.merge("订单号:", id, "<br/>", "服务类型:", type,
				"/申请人名称:",
				ObjectUtil.isNotNull(applicantDto) ? applicantDto.getFirstname() + " " + applicantDto.getSurname()
						: "unknown",
				"/类型:",
				ObjectUtil.isNotNull(applicantDto) && ObjectUtil.isNotNull(service)
						? service.getName() + "(" + service.getCode() + ")"
						: "unknown",
				"/顾问:", adviserDao.getAdviserById(serviceOrderDo.getAdviserId()).getName(), "/文案:",
				officialDAO.getOfficialById(serviceOrderDo.getOfficialId()).getName(), "<br/>", "属性:",
				getPeopleTypeStr(serviceOrderDo.getPeopleType()), "<br/>坚果云资料地址:", applicantDto.getUrl(),
				"<br/>在线资料地址:", applicantDto.getUrl(), "<br/>客户基本信息:", applicantDto.getContent(), "<br/>备注:",
				serviceOrderDo.getRemarks(), "<br/>驳回原因:", serviceOrderDo.getRefuseReason(), "<br/>创建时间:",
				serviceOrderDo.getGmtCreate(),
				"<br/><br/><a href='https://yongjinbiao.zhinanzhen.org/webroot_new/serviceorderdetail/id?" + id
						+ "'>服务订单详情</a>")
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

    private String getType(String type) {
        String s = "";
        if ("VISA".equalsIgnoreCase(s)) {
            s = "签证";
        } else if ("OVST".equalsIgnoreCase(type)) {
            s = "留学";
        } else if ("SIV".equalsIgnoreCase(type)) {
            s = "独立技术移民";
        } else if ("NSV".equalsIgnoreCase(type)) {
            s = "雇主担保";
        } else if ("MT".equalsIgnoreCase(type)) {
            s = "曼拓";
        } else if ("ZX".equalsIgnoreCase(type)) {
            s = "咨询";
        }
        return s;
    }
}
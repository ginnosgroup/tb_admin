package org.zhinanzhen.b.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.VisaDAO;
import org.zhinanzhen.b.dao.VisaOfficialDao;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.ServiceService;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.VisaService;
import org.zhinanzhen.b.service.pojo.ServiceDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.VisaDTO;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.AdminUserRoleDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserRoleDO;
import org.zhinanzhen.tb.service.ServiceException;

/** One account can submit the adviser, MARA and official work in one transaction. */
@Service
public class MultiRoleServiceOrderFlow {
    @Resource private AdminUserRoleDAO adminUserRoleDAO;
    @Resource private ServiceOrderDAO serviceOrderDAO;
    @Resource private VisaDAO visaDAO;
    @Resource private VisaOfficialDao visaOfficialDao;
    @Resource private VisaService visaService;
    @Resource private VisaOfficialService visaOfficialService;
    @Resource private ServiceOrderService serviceOrderService;
    @Resource private ServiceService serviceService;
    @Resource private AdviserDAO adviserDAO;
    @Resource private OfficialDAO officialDAO;
    @Resource private MaraDAO maraDAO;

    /** Creates adviser and official commission orders from the dedicated controller endpoint. */
    @Transactional(rollbackFor = Exception.class)
    public ServiceOrderDTO createCommissions(int id, int adminUserId) throws ServiceException {
        AdminUserRoleDO roles = requireValidRoles(adminUserId);
        if (serviceOrderDAO.lockForCommissionSubmission(id) == null)
            throw error("服务订单不存在:" + id);
        ServiceOrderDO parent = serviceOrderDAO.getServiceOrderById(id);
        if (parent == null)
            throw error("服务订单不存在:" + id);
        if (parent.getParentId() > 0 || parent.getApplicantParentId() > 0)
            throw error("请使用父订单ID创建佣金订单。");

        Map<Integer, ServiceOrderDO> childMap = new LinkedHashMap<>();
        for (ServiceOrderDO row : serviceOrderDAO.listByParentId(id))
            childMap.put(row.getId(), serviceOrderDAO.getServiceOrderById(row.getId()));
        for (ServiceOrderDO row : serviceOrderDAO.listByApplicantParentId(id))
            childMap.put(row.getId(), serviceOrderDAO.getServiceOrderById(row.getId()));
        List<ServiceOrderDO> children = new ArrayList<>(childMap.values());
        if (("SIV".equalsIgnoreCase(parent.getType()) || "NSV".equalsIgnoreCase(parent.getType()))
                && children.isEmpty())
            throw error("打包服务没有子订单，无法生成文案佣金。");
        if (roles.getAdviserId() != parent.getAdviserId()
                || (parent.getMaraId() > 0 && roles.getMaraId() != parent.getMaraId())
                || (parent.getOfficialId() > 0 && roles.getOfficialId() != parent.getOfficialId()))
            throw error("当前账号绑定的顾问、文案或MARA与父订单不一致。");
        List<ServiceOrderDO> officialOrders = children.isEmpty()
                ? java.util.Collections.singletonList(parent) : children;
        for (ServiceOrderDO order : officialOrders) {
            if (order == null)
                throw error("服务订单关联的子订单不存在。");
            if ((order.getAdviserId() > 0 && roles.getAdviserId() != order.getAdviserId())
                    || roles.getOfficialId() != order.getOfficialId()
                    || (order.getMaraId() > 0 && roles.getMaraId() != order.getMaraId()))
                throw error("当前账号绑定的顾问、文案或MARA与订单" + order.getId() + "不一致。");
        }

        if (!"PENDING".equalsIgnoreCase(parent.getState())
                && !"REVIEW".equalsIgnoreCase(parent.getState()))
            throw error("只有待提交或审核中的订单可以执行一人多角色提交。");
        if ("OVST".equalsIgnoreCase(parent.getType()))
            throw error("留学订单仍使用原有独立佣金流程。");
        if (parent.isPay() && isBlank(parent.getPaymentVoucherImageUrl1())
                && isBlank(parent.getPaymentVoucherImageUrl2()))
            throw error("支付凭证不能为空。");
        if (parent.getAmount() <= 0 || parent.getPerAmount() <= 0)
            throw error("服务订单尚无有效收款金额，不能生成顾问佣金订单。");
        if (parent.getAmount() > parent.getPerAmount())
            throw error("本次收款不能超过本次应收款。");
        if ("CNY".equalsIgnoreCase(parent.getCurrency()) && parent.getExchangeRate() <= 0)
            throw error("人民币订单汇率必须大于0。");
        if (visaDAO.countVisaByServiceOrderIdAndExcludeCode(id, null) > 0) {
            boolean complete = parent.isSubmitted();
            for (ServiceOrderDO order : officialOrders)
                complete &= visaOfficialDao.countVisaByServiceOrderId(order.getId()) > 0;
            for (ServiceOrderDO child : children)
                complete &= "COMPLETE".equalsIgnoreCase(child.getState());
            if (complete)
                return serviceOrderService.getServiceOrderById(id);
            throw error("该订单已有顾问佣金记录，请核对后再提交，避免重复生成。");
        }
        for (ServiceOrderDO child : children)
            if (!"PENDING".equalsIgnoreCase(child.getState())
                    && !"REVIEW".equalsIgnoreCase(child.getState()))
                throw error("子订单" + child.getId() + "已经进入后续流程，不能自动完成。");
        for (ServiceOrderDO order : officialOrders) {
            if (visaOfficialDao.countVisaByServiceOrderId(order.getId()) > 0)
                throw error("子订单" + order.getId() + "已有文案佣金记录，请勿重复提交。");
        }

        createAdviserCommissions(parent);
        for (ServiceOrderDO order : officialOrders)
            createOfficialCommission(order);
        if (serviceOrderDAO.markMultiRoleSubmitted(id, "REVIEW", "ADVISER", false) <= 0)
            throw error("父订单状态更新失败。");
        for (ServiceOrderDO child : children) {
            if (serviceOrderDAO.markMultiRoleSubmitted(child.getId(), "COMPLETE", "OFFICIAL", true) <= 0)
                throw error("子订单" + child.getId() + "状态更新失败。");
        }
        return serviceOrderService.getServiceOrderById(id);
    }

    private AdminUserRoleDO requireValidRoles(int adminUserId) throws ServiceException {
        AdminUserRoleDO roles = adminUserRoleDAO.getByAdminUserId(adminUserId);
        if (roles == null || roles.getAdviserId() <= 0 || roles.getOfficialId() <= 0
                || roles.getMaraId() <= 0)
            throw error("当前账号未完整配置顾问、文案和MARA。");
        if (adviserDAO.getAdviserById(roles.getAdviserId()) == null)
            throw error("账号绑定的顾问不存在:" + roles.getAdviserId());
        if (officialDAO.getOfficialById(roles.getOfficialId()) == null)
            throw error("账号绑定的文案不存在:" + roles.getOfficialId());
        if (maraDAO.getMaraById(roles.getMaraId()) == null)
            throw error("账号绑定的MARA不存在:" + roles.getMaraId());
        return roles;
    }

    public Integer adviserIdOf(int adminUserId) {
        AdminUserRoleDO roles = adminUserRoleDAO.getByAdminUserId(adminUserId);
        return roles == null || roles.getAdviserId() <= 0 ? null : roles.getAdviserId();
    }

    private void createAdviserCommissions(ServiceOrderDO order) throws ServiceException {
        ServiceDTO service = serviceService.getServiceById(order.getServiceId());
        if (service == null)
            throw error("服务类型不存在，不能生成顾问佣金订单。");
        int installments = Math.max(1, order.getInstallment());
        String code = UUID.randomUUID().toString();
        double remainingDue = Math.max(0, order.getReceivable() - order.getPerAmount());
        for (int number = 1; number <= installments; number++) {
            VisaDTO visa = new VisaDTO();
            copyCommon(order, visa);
            visa.setCode(code);
            visa.setInstallment(installments);
            visa.setInstallmentNum(number);
            visa.setState(number == 1 ? "REVIEW" : "PENDING");
            visa.setKjApprovalDate(number == 1 ? new Date() : null);
            visa.setPerAmount(number == 1 ? order.getPerAmount() : remainingDue / (installments - 1));
            visa.setAmount(number == 1 ? order.getAmount() : visa.getPerAmount());
            visa.setDiscount(visa.getPerAmount() - visa.getAmount());
            if (number > 1) {
                visa.setReceiveDate(null);
                visa.setPaymentVoucherImageUrl1(null);
                visa.setPaymentVoucherImageUrl2(null);
                visa.setPaymentVoucherImageUrl3(null);
                visa.setPaymentVoucherImageUrl4(null);
                visa.setPaymentVoucherImageUrl5(null);
            } else {
                visa.setVerifyCode(order.getVerifyCode());
            }
            double amountAud = "CNY".equalsIgnoreCase(order.getCurrency())
                    ? BigDecimal.valueOf(visa.getAmount())
                        .divide(BigDecimal.valueOf(order.getExchangeRate()), 4, RoundingMode.HALF_UP).doubleValue()
                    : visa.getAmount();
            visa.setGst(amountAud / 11);
            visa.setDeductGst(amountAud - visa.getGst());
            visa.setBonus(visa.getDeductGst() * 0.1);
            visa.setExpectAmount(amountAud);
            if ("签证".equalsIgnoreCase(service.getName()) && service.getCode() != null
                    && (service.getCode().contains("186") || service.getCode().contains("407")
                    || service.getCode().contains("482"))) {
                visa.setExpectAmount(number == 1 ? visa.getAmount() * 0.4
                        : visa.getDeductGst() * 0.4);
                visa.setBonus(visa.getBonus() * 0.4);
            } else if ("雇主担保".equalsIgnoreCase(service.getName())) {
                visa.setExpectAmount(visa.getDeductGst() * 0.4);
                if (number == 1)
                    visa.setPerAmount(visa.getDeductGst() * 0.4);
                visa.setBonus(visa.getBonus() * 0.4);
            }
            if (visaService.addVisaWithRecommendation(visa, order.getRecommendAdviserId()).isEmpty())
                throw error("顾问佣金订单生成失败。");
        }
    }

    private void createOfficialCommission(ServiceOrderDO order) throws ServiceException {
        if (order.getOfficialId() <= 0)
            throw error("订单" + order.getId() + "未分配文案。");
        VisaOfficialDTO visa = new VisaOfficialDTO();
        visa.setCode(UUID.randomUUID().toString());
        visa.setHandlingDate(new Date());
        visa.setUserId(order.getUserId());
        visa.setReceiveTypeId(order.getReceiveTypeId());
        visa.setReceiveDate(order.getReceiveDate());
        visa.setServiceId(order.getServiceId());
        visa.setServiceOrderId(order.getId());
        visa.setInstallment(Math.max(1, order.getInstallment()));
        visa.setInstallmentNum(1);
        visa.setPaymentVoucherImageUrl1(order.getPaymentVoucherImageUrl1());
        visa.setPaymentVoucherImageUrl2(order.getPaymentVoucherImageUrl2());
        visa.setPaymentVoucherImageUrl3(order.getPaymentVoucherImageUrl3());
        visa.setPaymentVoucherImageUrl4(order.getPaymentVoucherImageUrl4());
        visa.setPaymentVoucherImageUrl5(order.getPaymentVoucherImageUrl5());
        visa.setVisaVoucherImageUrl(order.getVisaVoucherImageUrl());
        visa.setReceivable(order.getReceivable());
        visa.setReceived(order.getReceived());
        visa.setPerAmount(order.getPerAmount());
        visa.setAmount(order.getAmount());
        visa.setCurrency(order.getCurrency());
        visa.setExchangeRate(order.getExchangeRate());
        visa.setDiscount(order.getPerAmount() - order.getAmount());
        visa.setAdviserId(order.getAdviserId());
        visa.setMaraId(order.getMaraId());
        visa.setOfficialId(order.getOfficialId());
        visa.setRemarks(order.getRemarks());
        visa.setState("REVIEW");
        visa.setKjApprovalDate(new Date());
        visa.setVerifyCode(order.getVerifyCode());
        int result = visaOfficialService.addVisa(visa, order.getCompletionPhase());
        if (result <= 0)
            throw error("订单" + order.getId() + "不满足文案佣金结算条件，生成失败。");
    }

    private void copyCommon(ServiceOrderDO order, VisaDTO visa) {
        visa.setHandlingDate(new Date());
        visa.setUserId(order.getUserId());
        visa.setReceiveTypeId(order.getReceiveTypeId());
        visa.setReceiveDate(order.getReceiveDate());
        visa.setServiceId(order.getServiceId());
        visa.setServiceOrderId(order.getId());
        visa.setPaymentVoucherImageUrl1(order.getPaymentVoucherImageUrl1());
        visa.setPaymentVoucherImageUrl2(order.getPaymentVoucherImageUrl2());
        visa.setPaymentVoucherImageUrl3(order.getPaymentVoucherImageUrl3());
        visa.setPaymentVoucherImageUrl4(order.getPaymentVoucherImageUrl4());
        visa.setPaymentVoucherImageUrl5(order.getPaymentVoucherImageUrl5());
        visa.setVisaVoucherImageUrl(order.getVisaVoucherImageUrl());
        visa.setReceivable(order.getReceivable());
        visa.setReceived(order.getReceived());
        visa.setCurrency(order.getCurrency());
        visa.setExchangeRate(order.getExchangeRate());
        visa.setAdviserId(order.getAdviserId());
        visa.setMaraId(order.getMaraId());
        visa.setOfficialId(order.getOfficialId());
        visa.setRemarks(order.getRemarks());
    }

    private ServiceException error(String message) {
        return new ServiceException(message);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

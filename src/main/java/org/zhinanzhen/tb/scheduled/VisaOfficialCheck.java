package org.zhinanzhen.tb.scheduled;


import com.ikasoa.core.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.zhinanzhen.b.controller.BaseCommissionOrderController;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.ServicePackageDAO;
import org.zhinanzhen.b.dao.VisaOfficialDao;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.dao.pojo.ServicePackageDO;
import org.zhinanzhen.b.dao.pojo.VisaOfficialDO;
import org.zhinanzhen.b.service.VisaOfficialService;
import org.zhinanzhen.b.service.pojo.VisaOfficialDTO;
import org.zhinanzhen.tb.service.ServiceException;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@EnableScheduling
@Slf4j
public class VisaOfficialCheck {

    @Resource
    private ServiceOrderDAO serviceOrderDAO;

    @Resource
    private VisaOfficialDao visaOfficialDao;

    @Resource
    private ServicePackageDAO servicePackageDAO;

    @Resource
    private VisaOfficialService visaOfficialService;
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 3 * * ?")
    public void visaOfficialCheckEverDay() throws ServiceException {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 计算三天前的日期
        LocalDate threeDaysAgo = currentDate.minusDays(3);
        LocalDate localDate = currentDate.plusDays(1);
        // 创建一个 DateTimeFormatter 对象来格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 格式化日期并输出
        String beforeFormat = threeDaysAgo.format(formatter);
        String nowDate = localDate.format(formatter);
        List<ServiceOrderDO> tmpServiceOrder = serviceOrderDAO.getTmpServiceOrder(beforeFormat, nowDate);
        for (ServiceOrderDO e : tmpServiceOrder) {
            if (e.getApplicantParentId() != 0 && e.getServicePackageId() != 0) {
                ServiceOrderDO serviceParentOrderById = serviceOrderDAO.getServiceOrderById(e.getApplicantParentId());
                if ("SIV".equals(serviceParentOrderById.getType())) {
                    ServicePackageDO byId = servicePackageDAO.getById(e.getServicePackageId());
                    if (!"VA".equals(byId.getType()) && !"EOI".equals(byId.getType())) {
                        continue;
                    }
                }
            }
            VisaOfficialDO visaOfficialDO = visaOfficialDao.getByServiceOrderId(e.getId());
            if (ObjectUtil.isNull(visaOfficialDO)) {
                ServiceOrderDO serviceOrderById = serviceOrderDAO.getServiceOrderById(e.getId());
                VisaOfficialDTO visaDto = new VisaOfficialDTO();
                visaDto.setState(BaseCommissionOrderController.ReviewKjStateEnum.PENDING.toString());
                visaDto.setUserId(serviceOrderById.getUserId());
                visaDto.setCode(UUID.randomUUID().toString());
                visaDto.setHandlingDate(new Date());
                visaDto.setReceiveTypeId(serviceOrderById.getReceiveTypeId());
                visaDto.setReceiveDate(serviceOrderById.getReceiveDate());
                visaDto.setServiceId(serviceOrderById.getServiceId());
                visaDto.setServiceOrderId(serviceOrderById.getId());
                visaDto.setInstallment(serviceOrderById.getInstallment());
                visaDto.setPaymentVoucherImageUrl1(serviceOrderById.getPaymentVoucherImageUrl1());
                visaDto.setPaymentVoucherImageUrl2(serviceOrderById.getPaymentVoucherImageUrl2());
                visaDto.setPaymentVoucherImageUrl3(serviceOrderById.getPaymentVoucherImageUrl3());
                visaDto.setPaymentVoucherImageUrl4(serviceOrderById.getPaymentVoucherImageUrl4());
                visaDto.setPaymentVoucherImageUrl5(serviceOrderById.getPaymentVoucherImageUrl5());
                visaDto.setVisaVoucherImageUrl(serviceOrderById.getVisaVoucherImageUrl());
                visaDto.setPerAmount(serviceOrderById.getPerAmount());
                visaDto.setAmount(serviceOrderById.getAmount());
                if (visaDto.getPerAmount() < visaDto.getAmount()) {
                    log.info("本次应收款(" + visaDto.getPerAmount() + ")不能小于本次已收款(" + visaDto.getAmount() + ")!");
                }
                visaDto.setCurrency(serviceOrderById.getCurrency());
                visaDto.setExchangeRate(serviceOrderById.getExchangeRate());
                visaDto.setDiscount(visaDto.getPerAmount() - visaDto.getAmount());
//                visaDto.setInvoiceNumber(serviceOrderById.getInvoiceNumber());
                visaDto.setAdviserId(serviceOrderById.getAdviserId());
                visaDto.setMaraId(serviceOrderById.getMaraId());
                visaDto.setOfficialId(serviceOrderById.getOfficialId());
                visaDto.setRemarks(serviceOrderById.getRemarks());
                double commission = visaDto.getAmount();
                if ("CNY".equals(serviceOrderById.getCurrency())) {
                    BigDecimal bigDecimal = BigDecimal.valueOf(commission);
                    BigDecimal bigDecimalExc = BigDecimal.valueOf(serviceOrderById.getExchangeRate());
                    BigDecimal divide = bigDecimal.divide(bigDecimalExc, 4, RoundingMode.HALF_UP);
                    commission = divide.doubleValue();
                }
                visaDto.setGst(commission / 11);
                visaDto.setDeductGst(commission - visaDto.getGst());
                visaDto.setBonus(visaDto.getDeductGst() * 0.1);
                visaDto.setExpectAmount(commission);

                double _perAmount = 0.00;
                double _amount = 0.00;
                visaDto.setState(BaseCommissionOrderController.ReviewKjStateEnum.REVIEW.toString()); // 第一笔单子直接进入财务审核状态
//                if (StringUtil.isNotEmpty(verifyCode))// 只给第一笔赋值verifyCode
//                    visaDto.setVerifyCode(verifyCode.replace("$", "").replace("#", "").replace(" ", ""));
                visaDto.setKjApprovalDate(new Date());
                visaOfficialService.addVisa(visaDto);
                log.info("当前未生成文案佣金订单的订单编号为：------------------------------{}", e.getId());
            }
        }
    }
}

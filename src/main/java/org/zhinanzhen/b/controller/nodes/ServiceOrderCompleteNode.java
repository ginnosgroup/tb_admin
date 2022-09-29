package org.zhinanzhen.b.controller.nodes;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.CommissionAmountDTO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.web.workflow.Context;

import javax.annotation.Resource;

//申请成功
@Component
public class ServiceOrderCompleteNode extends SODecisionNode {

	@Resource
	private ServicePackagePriceDAO servicePackagePriceDAO;

	@Resource
	private RefundDAO refundDAO;

	@Resource
	private OfficialDAO officialDAO;

	@Resource
	private OfficialGradeDao officialGradeDao;


	@Resource
	private ServiceOrderDAO serviceOrderDao;

	@Resource
	private VisaDAO visaDAO;

	@Resource
	private ServiceOrderOfficialRemarksDAO serviceOrderOfficialRemarksDAO;

	@Resource
	private ServiceDAO serviceDAO;


	// 文案
	
	public ServiceOrderCompleteNode(ServiceOrderService serviceOrderService) {
		super.serviceOrderService = serviceOrderService;
	}

	@Override
	public String getName() {
		return "COMPLETE";
	}

	@Override
	protected String decide(Context context) {
		//if (!"WA".equalsIgnoreCase(getAp(context))) {
		//	context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
		//	return null;
		//}
		try {
			ServiceOrderDTO serviceOrderDto = serviceOrderService.getServiceOrderById(getServiceOrderId(context));
			String type = serviceOrderDto.getType();
			if (!"ZX".equals(type) && !serviceOrderDto.isSettle() && !"WA".equalsIgnoreCase(getAp(context))){ //咨询不用判断文案权限,扣拥留学不用判断
				context.putParameter("response", new Response<ServiceOrderDTO>(1, "仅限文案操作!", null));
					return null;
			}
			if ( "VISA".equals(type) && serviceOrderDto.getParentId() == 0 ) // 签证
				return SUSPEND_NODE;
			if ("ZX".equals(type)){//咨询
				isSingleStep = true;
				return SUSPEND_NODE;
			}
//			String a []={"103","143","173","864","835","838"};//todo
//			for (String s : a) {
//				if (s.equals(serviceDAO.getServiceById(serviceOrderDto.getServiceId()).getCode())){
//					VisaDO visaDO = new VisaDO();
//					CommissionAmountDTO commissionAmountDTO = new CommissionAmountDTO();
//					visaDO = visaDAO.getSecondVisaByServiceOrderId(serviceOrderDto.getId());
//					RefundDO refund = refundDAO.getRefundByVisaId(visaDO.getId());
//					if (refund==null){
//						commissionAmountDTO.setRefund(0.00);
//					}
//					else{
//						commissionAmountDTO.setRefund(refund.getAmount());
//					}
//					ServiceOrderOfficialRemarksDO serviceOrderOfficialRemarksDO = serviceOrderOfficialRemarksDAO.getByServiceOrderId(serviceOrderDto.getId());
//					OfficialDO official = officialDAO.getOfficialById(serviceOrderOfficialRemarksDO.getOfficialId());
//					OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(official.getGradeId());
//					ServicePackagePriceDO servicePackagePriceDO = servicePackagePriceDAO.getByServiceId(serviceOrderDto.getServiceId());
//					commissionAmountDTO.setThirdPrince(servicePackagePriceDO.getThirdPrince());
//					commissionAmountDTO.setCommissionAmount(serviceOrderDto.getAmount()-commissionAmountDTO.getRefund()-commissionAmountDTO.getThirdPrince());
//					if (commissionAmountDTO.getCommissionAmount()<=0)
//						commissionAmountDTO.setCommissionAmount(0.00);
//					commissionAmountDTO.setCommission(commissionAmountDTO.getCommissionAmount()*grade.getRate()*0.5);
//					serviceOrderDao.setCommission(serviceOrderDto.getId(),0.00,commissionAmountDTO.getCommissionAmount(),commissionAmountDTO.getCommission());
//				}
//			}
			isSingleStep = true;
			return "PAID";
		} catch (ServiceException e) {
			context.putParameter("response", new Response<ServiceOrderDTO>(1, "服务订单执行异常:" + e.getMessage(), null));
			return null;
		}
	}
	
	@Override
	public String[] nextNodeNames() {
		return new String[]{"PAID", "CLOSE","WAIT_FD"};
	}

}

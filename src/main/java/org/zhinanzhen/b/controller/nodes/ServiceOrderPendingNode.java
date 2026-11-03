package org.zhinanzhen.b.controller.nodes;

import java.util.List;

import org.springframework.stereotype.Component;
import org.zhinanzhen.b.dao.VisaDAO;
import org.zhinanzhen.b.dao.pojo.VisaDO;
import org.zhinanzhen.b.service.ServiceOrderService;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.tb.controller.Response;

import com.ikasoa.web.workflow.Context;
import com.ikasoa.web.workflow.Node;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceOrderPendingNode extends SONode {

	// 顾问

	/**
	 * 注意：节点是由 SONodeFactory 用 new 创建的（不是 Spring Bean），
	 * 所以 DAO 必须通过构造函数由工厂传进来，不能用 @Resource 字段注入。
	 */
	private final VisaDAO visaDao;

	public ServiceOrderPendingNode(ServiceOrderService serviceOrderService, VisaDAO visaDao) {
		super.serviceOrderService = serviceOrderService;
		this.visaDao = visaDao;
	}

	@Override
	public String getName() {
		return "PENDING";
	}

	@Override
	public Node getNextNode(Context context) {
		if (context.getNodeFactory() == null)
			return null;
		return context.getNodeFactory().getNode("REVIEW");
	}

	/**
	 * 服务订单回到 PENDING 的两种来源，都要处理佣金订单(b_visa)：
	 *  - 顾问撤回：stateMark = null
	 *  - 文案驳回：stateMark = REFUSE
	 *
	 * 规则：
	 *  - 只要有一笔佣金订单已结算完成(state=COMPLETE)，直接拒绝本次操作；
	 *  - 全部未结算，则放行，并在状态保存成功后删除这些佣金订单（见 saveNode）。
	 */
	@Override
	public Context processNode(Context context) {
		isSingleStep = true;
		int serviceOrderId = getServiceOrderId(context);
		if (visaDao == null) {
			// 兜底：DAO 未注入时不能因为校验把流程整个卡死
			log.warn("visaDao 未注入,跳过佣金订单校验:serviceOrderId=" + serviceOrderId);
			context.putParameter("stateMark2", "Retracted");
			return context;
		}
		try {
			List<VisaDO> visaList = visaDao.listVisaByServiceOrderId(serviceOrderId);
			if (visaList != null && !visaList.isEmpty()) {
				for (VisaDO visa : visaList) {
					if (visa.getState() != null && "COMPLETE".equalsIgnoreCase(visa.getState())) {
						log.warn("回退被拒绝:serviceOrderId={},visaId={},state={}", serviceOrderId, visa.getId(),
								visa.getState());
						context.putParameter("response", new Response<ServiceOrderDTO>(1,
								"该服务订单的佣金订单(ID:" + visa.getId() + ")已结算完成，不允许驳回或撤回。", null));
						return null;
					}
				}
			}
		} catch (Exception e) {
			log.error("回退前校验佣金订单异常:serviceOrderId=" + serviceOrderId, e);
			context.putParameter("response", new Response<ServiceOrderDTO>(1,
					"回退前校验佣金订单失败:" + e.getClass().getSimpleName() + ":" + e.getMessage(), null));
			return null;
		}
		context.putParameter("stateMark2", "Retracted");
		return context;
	}

	/**
	 * 状态保存成功后，删除该服务订单下尚未结算的佣金订单。
	 * （processNode 已拦截 COMPLETE，这里再判一次，避免误删已结算数据）
	 */
	@Override
	protected boolean saveNode(Context context) {
		if (!super.saveNode(context))
			return false;
		if (visaDao == null)
			return true;
		int serviceOrderId = getServiceOrderId(context);
		try {
			List<VisaDO> visaList = visaDao.listVisaByServiceOrderId(serviceOrderId);
			if (visaList != null) {
				for (VisaDO visa : visaList) {
					if (visa.getState() != null && "COMPLETE".equalsIgnoreCase(visa.getState()))
						continue;
					if (visaDao.deleteVisaById(visa.getId()) > 0)
						log.info("回退删除佣金订单成功:serviceOrderId=" + serviceOrderId + ",visaId=" + visa.getId()
								+ ",state=" + visa.getState());
					else
						log.warn("回退删除佣金订单失败:serviceOrderId=" + serviceOrderId + ",visaId=" + visa.getId());
				}
			}
		} catch (Exception e) {
			log.error("回退删除佣金订单异常:serviceOrderId=" + serviceOrderId + ",err=" + e.getMessage());
		}
		return true;
	}

	@Override
	public String[] nextNodeNames() {
		return new String[]{"PENDING", "REVIEW", "COMPLETE"};
	}

}

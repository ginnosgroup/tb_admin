package org.zhinanzhen.b.service;

/**
 * 案件全流程状态定义。
 *
 * <p>状态码必须按字符串处理，不能把 010、010A 等状态转换成数字。{@link #isFollowUp()}
 * 用来标识需要走补料/申请结果通知权限校验的状态，其他状态仍由
 * {@code PortalController#updatePortal} 中已有的业务分支处理。</p>
 */
public enum PortalFollowUpState {
	CUSTOMER_PENDING_SUBMISSION("01", "客户待提交资料", "customer_pending_submission", null, false, false,
			false),
	CUSTOMER_SUBMITTED("02", "客户已提交资料", "customer_submitted", null, false, false, false),
	MARA_PROCESSING_UPGRADE("02A", "升级案件MARA处理中", "mara_processing_upgrade", null, false, false, false),
	CONTRACT_PREVIEW_GENERATED("02B", "已生成合同预览", "contract_preview_generated", null, false, false, false),
	CUSTOMER_CONTRACT_RETURNED("02C", "客户签约退回", "customer_contract_returned", null, false, false, false),
	MARA_CONTRACT_REJECTED("02D", "MARA签约审核驳回", "mara_contract_rejected", null, false, false, false),
	SUBMITTED_CONTRACT_REVIEW("03", "已提交给MARA签约审核", "submitted_contract_review", null, false, false, false),
	CONTRACT_APPROVED_PENDING_CUSTOMER("03A", "合同审核通过待客户确认",
			"contract_approved_pending_customer", null, false, false, false),
	CUSTOMER_CONTRACT_CONFIRMED("04", "客户签约已确认", "customer_contract_confirmed", null, false, false, false),
	ADVISER_SERVICE_ORDER_CREATED("05", "顾问已下服务订单", "adviser_service_order_created", null, false, false,
			false),
	OFFICIAL_PREPARING_MATERIALS("06", "文案准备申请材料中", "official_preparing_application_materials", null, false,
			false, false),
	CUSTOMER_RETURNED_MATERIALS("06A", "客户退回申请材料", "customer_return_application_materials", null, false,
			false, false),
	MATERIALS_PENDING_CUSTOMER_CONFIRM("06B", "申请材料待客户确认", "materials_pending_customer_confirm", null, false,
			false, false),
	CUSTOMER_CONFIRMED_MATERIALS("07", "客户已确认申请材料", "customer_confirmed_application_materials", null, false,
			false, false),
	MARA_REVIEWING_MATERIALS("07A", "申请材料MARA正在审核", "mara_reviewing_application_materials", null, false,
			false, false),
	MARA_REJECTED_MATERIALS("07B", "申请材料MARA审核驳回", "mara_reject_application_materials", null, false, false,
			false),
	MARA_APPROVED_MATERIALS("08", "申请材料MARA审核通过", "mara_approve_application_materials", null, false, false,
			false),
	OFFICIAL_SUBMITTED_APPLICATION("09", "文案已正式提交申请", "official_submit_application", null, false, false,
			false),

	// 以下状态属于补料/申请结果通知流程，需要角色、备注或附件校验。
	REQUEST_SUPPLEMENT("010", "已通知客户补料", "official_request_supplement", "WA", true, false, true),
	REVIEW_SUPPLEMENT("010A", "补料MARA正在审核", "mara_reviewing_supplement", "WA", false, true, true),
	REJECT_SUPPLEMENT("010B", "补料MARA审核驳回", "mara_reject_supplement", "MA", true, false, true),
	APPROVE_SUPPLEMENT("011", "补料MARA审核通过", "mara_approve_supplement", "MA", false, false, true),
	SUBMIT_SUPPLEMENT("011A", "补料已提交等待最终决定", "official_submit_supplement", "WA", false, false, true),
	NOTIFY_RESULT("012", "申请结果已通知客户", "official_notify_application_result", "WA", false, true, true),
	ARCHIVE("013", "案件已归档结案", "official_archive_portal", "WA", false, false, true);

	private final String code;
	private final String label;
	private final String action;
	private final String role;
	private final boolean remarkRequired;
	private final boolean attachmentsRequired;
	private final boolean followUp;

	PortalFollowUpState(String code, String label, String action, String role, boolean remarkRequired,
			boolean attachmentsRequired, boolean followUp) {
		this.code = code;
		this.label = label;
		this.action = action;
		this.role = role;
		this.remarkRequired = remarkRequired;
		this.attachmentsRequired = attachmentsRequired;
		this.followUp = followUp;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public String getAction() {
		return action;
	}

	public String getRole() {
		return role;
	}

	public boolean isRemarkRequired() {
		return remarkRequired;
	}

	public boolean isAttachmentsRequired() {
		return attachmentsRequired;
	}

	/** 是否需要补料/申请结果流程的角色、备注、附件和通知处理。 */
	public boolean isFollowUp() {
		return followUp;
	}

	public static PortalFollowUpState fromCode(String code) {
		if (code == null)
			return null;
		String normalizedCode = code.trim();
		for (PortalFollowUpState state : values()) {
			if (state.code.equals(normalizedCode))
				return state;
		}
		return null;
	}
}

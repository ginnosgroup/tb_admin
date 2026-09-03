package org.zhinanzhen.b.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.zhinanzhen.b.dao.MaraDAO;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.PortalDAO;
import org.zhinanzhen.b.dao.PortalTypeDAO;
import org.zhinanzhen.b.dao.pojo.MaraDO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.PortalDO;
import org.zhinanzhen.b.dao.pojo.PortalTypeDO;
import org.zhinanzhen.b.service.PortalDocumentService;
import org.zhinanzhen.b.service.PortalService;
import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.b.service.pojo.PortalTypeDTO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("PortalService")
public class PortalServiceImpl extends BaseService implements PortalService {

	@Resource
	private PortalDAO portalDao;

	@Resource
	private PortalTypeDAO portalTypeDao;

	@Resource
	private AdviserDAO adviserDao;

	@Resource
	private OfficialDAO officialDao;

	@Resource
	private MaraDAO maraDao;

	@Resource
	private PortalDocumentService portalDocumentService;

	@Override
	public int addPortal(PortalDTO portalDto) throws ServiceException {
		if (portalDto == null) {
			ServiceException se = new ServiceException("portalDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			// 查重校验：typeId+name 只允许存在一条记录，不允许重复创建
			PortalDO existPortalDo = portalDao.getPortalByTypeIdAndName(portalDto.getTypeId(),
					portalDto.getName());
			if (existPortalDo != null) {
				ServiceException se = new ServiceException("案件已存在：typeId=" + portalDto.getTypeId() + ", name="
						+ portalDto.getName() + "，不允许重复创建.");
				se.setCode(1);
				throw se;
			}
			PortalDO portalDo = mapper.map(portalDto, PortalDO.class);
			if (portalDao.addPortal(portalDo) > 0) {
				portalDto.setId(portalDo.getId());
				return portalDo.getId();
			} else {
				return 0;
			}
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updatePortal(PortalDTO portalDto) throws ServiceException {
		if (portalDto == null) {
			ServiceException se = new ServiceException("portalDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalDO portalDo = mapper.map(portalDto, PortalDO.class);
			return portalDao.updatePortal(portalDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int clearGeneratedDocumentPaths(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("案件ID无效，无法清空合同和Letter文件路径.");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalDao.clearGeneratedDocumentPaths(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updatePortalStateIfCurrent(int id, String fromState, String toState) throws ServiceException {
		if (id <= 0 || StringUtil.isEmpty(fromState) || StringUtil.isEmpty(toState)) {
			ServiceException se = new ServiceException("案件状态更新参数错误.");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalDao.updatePortalStateIfCurrent(id, fromState, toState);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<PortalDTO> listPortal(Integer typeId, String caseType, String strState, String keyword, int pageNum,
			int pageSize, Integer adviserId, Integer adviserRegionId, Integer officialId, Integer officialRegionId,
			Integer maraId) throws ServiceException {
		List<PortalDTO> portalDtoList = new ArrayList<PortalDTO>();
		List<PortalDO> portalDoList = new ArrayList<PortalDO>();
		try {
			portalDoList = portalDao.listPortal(typeId, caseType, strState, keyword, pageNum * pageSize, pageSize,
					adviserId, adviserRegionId, officialId, officialRegionId, maraId);
			if (portalDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (PortalDO portalDo : portalDoList) {
			PortalDTO portalDto = mapper.map(portalDo, PortalDTO.class);
			assemblePortalNames(portalDto);
			portalDtoList.add(portalDto);
		}
		return portalDtoList;
	}

	@Override
	public int countPortal(Integer typeId, String caseType, String strState, String keyword, Integer adviserId,
			Integer adviserRegionId, Integer officialId, Integer officialRegionId, Integer maraId)
			throws ServiceException {
		try {
			return portalDao.countPortal(typeId, caseType, strState, keyword, adviserId, adviserRegionId, officialId,
					officialRegionId, maraId);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	@Override
	public PortalDTO getPortal(Integer id, Integer adviserId, Integer adviserRegionId, Integer officialId,
			Integer officialRegionId, Integer maraId) throws ServiceException {
		if (id == null || id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			PortalDO portalDo = portalDao.getPortalById(id, adviserId, adviserRegionId, officialId, officialRegionId,
					maraId);
			if (portalDo == null) {
				ServiceException se = new ServiceException("No data !");
				se.setCode(ErrorCodeEnum.DATA_ERROR.code());
				throw se;
			}
			PortalDTO portalDto = mapper.map(portalDo, PortalDTO.class);
			assemblePortalNames(portalDto);
			return portalDto;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
	}

	/**
	 * 按id查案件类型/顾问/文案/mara并组装名称到DTO，同时把完整的案件类型对象组装进 portalType
	 */
	private void assemblePortalNames(PortalDTO portalDto) {
		if (portalDto.getTypeId() > 0) {
			PortalTypeDO portalTypeDo = portalTypeDao.getPortalTypeById(portalDto.getTypeId());
			if (portalTypeDo != null) {
				portalDto.setPortalTypeName(portalTypeDo.getName());
				portalDto.setPortalType(mapper.map(portalTypeDo, PortalTypeDTO.class));
			}
		}
		if (portalDto.getAdviserId() > 0) {
			AdviserDO adviserDo = adviserDao.getAdviserById(portalDto.getAdviserId());
			if (adviserDo != null)
				portalDto.setAdviserName(adviserDo.getName());
		}
		if (portalDto.getOfficialId() > 0) {
			OfficialDO officialDo = officialDao.getOfficialById(portalDto.getOfficialId());
			if (officialDo != null)
				portalDto.setOfficialName(officialDo.getName());
		}
		if (portalDto.getMaraId() > 0) {
			MaraDO maraDo = maraDao.getMaraById(portalDto.getMaraId());
			if (maraDo != null)
				portalDto.setMaraName(maraDo.getName());
		}
	}

	@Override
	public int updateAiConsultContent(int id, String aiConsultContent) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalDao.updateAiConsultContent(id, aiConsultContent);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public void sendMaraPortalNotification(PortalDTO portalDto, String remark, String caseUrl)
			throws ServiceException {
		sendMaraPortalNotification(portalDto, remark, caseUrl, "复杂或紧急案件通知 - ",
				"以下案件已由顾问提交备注，请及时登录佣金系统查看并处理。", "顾问备注", "通知mara处理案件",
				"顾问通知日期", false);
	}

	@Override
	public void sendMaraPortalReviewNotification(PortalDTO portalDto, String caseUrl) throws ServiceException {
		sendMaraPortalNotification(portalDto, null, caseUrl, "案件审核通知 - ",
				"以下案件已由顾问提交案件审核，合同和Letter文件已随邮件附上，请及时登录佣金系统查看案件资料并完成审核。",
				"审核事项", "请审核案件资料、合同和Letter文件", "审核通知日期", true);
	}

	private void sendMaraPortalNotification(PortalDTO portalDto, String remark, String caseUrl, String titlePrefix,
			String introduction, String remarkLabel, String defaultRemark, String dateLabel,
			boolean includeGeneratedDocuments) throws ServiceException {
		try {
			if (portalDto == null || portalDto.getId() <= 0) {
				throw notificationException("案件信息无效，无法发送MARA通知邮件.",
						ErrorCodeEnum.PARAMETER_ERROR.code());
			}
			String notificationRemark = StringUtil.isEmpty(remark) ? defaultRemark : remark;
			if (portalDto.getMaraId() <= 0) {
				throw notificationException("案件尚未分配MARA，无法发送通知邮件.", ErrorCodeEnum.DATA_ERROR.code());
			}

			MaraDO maraDo = maraDao.getMaraById(portalDto.getMaraId());
			if (maraDo == null || StringUtil.isEmpty(maraDo.getEmail())) {
				throw notificationException("对应MARA不存在或未配置邮箱，无法发送通知邮件.",
						ErrorCodeEnum.DATA_ERROR.code());
			}

			String adviserName = portalDto.getAdviserName();
			if (StringUtil.isEmpty(adviserName) && portalDto.getAdviserId() > 0) {
				AdviserDO adviserDo = adviserDao.getAdviserById(portalDto.getAdviserId());
				if (adviserDo != null)
					adviserName = adviserDo.getName();
			}

			String customerName = valueOrEmpty(portalDto.getName());
			String title = titlePrefix + customerName + "（案件编号：" + portalDto.getId() + "）";
			String noticeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			String escapedUrl = escapeHtml(caseUrl);
			StringBuilder content = new StringBuilder();
			content.append("<p>").append(escapeHtml(maraDo.getName())).append("，您好：</p>")
					.append("<p>").append(escapeHtml(introduction)).append("</p>")
					.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
							+ "style=\"width:100%;border-collapse:collapse;line-height:1.7;table-layout:auto;\">")
					.append(mailRow("案件编号", String.valueOf(portalDto.getId())))
					.append(mailRow("客户姓名", customerName))
					.append(mailRow("顾问名称", adviserName))
					.append(mailRow(remarkLabel, notificationRemark))
					.append(mailRow(dateLabel, noticeDate))
					.append("<tr><td width=\"120\" nowrap=\"nowrap\" "
							+ "style=\"width:120px;padding:6px 12px 6px 0;vertical-align:top;white-space:nowrap;\">"
							+ "<strong>案件URL地址</strong></td>")
					.append("<td style=\"padding:6px 0;\"><a href=\"").append(escapedUrl).append("\">")
					.append(escapedUrl).append("</a></td></tr></table>")
					.append("<p>谢谢。</p>");
			if (includeGeneratedDocuments) {
				portalDocumentService.sendDocumentsToEmail(maraDo.getEmail(), title, content.toString(),
						portalDto.getContractFilePath(), portalDto.getLetterFilePath());
			} else {
				sendMail(maraDo.getEmail(), title, content.toString());
			}
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException exception = new ServiceException("发送MARA通知邮件失败: " + e.getMessage(), e);
			exception.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw exception;
		}
	}

	@Override
	public void sendOfficialPortalNotification(PortalDTO portalDto, String caseUrl) throws ServiceException {
		try {
			if (portalDto == null || portalDto.getId() <= 0) {
				throw notificationException("案件信息无效，无法发送文案通知邮件.",
						ErrorCodeEnum.PARAMETER_ERROR.code());
			}
			if (portalDto.getOfficialId() <= 0) {
				throw notificationException("案件尚未分配文案，无法发送通知邮件.", ErrorCodeEnum.DATA_ERROR.code());
			}

			OfficialDO officialDo = officialDao.getOfficialById(portalDto.getOfficialId());
			if (officialDo == null || StringUtil.isEmpty(officialDo.getEmail())) {
				throw notificationException("对应文案不存在或未配置邮箱，无法发送通知邮件.",
						ErrorCodeEnum.DATA_ERROR.code());
			}

			String customerName = valueOrEmpty(portalDto.getName());
			String adviserName = portalDto.getAdviserName();
			if (StringUtil.isEmpty(adviserName) && portalDto.getAdviserId() > 0) {
				AdviserDO adviserDo = adviserDao.getAdviserById(portalDto.getAdviserId());
				if (adviserDo != null)
					adviserName = adviserDo.getName();
			}

			String title = "新案件通知 - 客户已确认签署合同 - " + customerName
					+ "（案件编号：" + portalDto.getId() + "）";
			String noticeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			StringBuilder content = new StringBuilder();
			content.append("<p>").append(escapeHtml(officialDo.getName())).append("，您好：</p>")
					.append("<p>客户已确认签署本案件的合同，请开始准备并推进后续申请工作。</p>")
					.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" "
							+ "style=\"width:100%;border-collapse:collapse;line-height:1.7;table-layout:auto;\">")
					.append(mailRow("案件编号", String.valueOf(portalDto.getId())))
					.append(mailRow("客户姓名", customerName))
					.append(mailRow("顾问名称", adviserName))
					.append(mailRow("客户确认时间", noticeDate))
					.append("<tr><td width=\"120\" nowrap=\"nowrap\" "
							+ "style=\"width:120px;padding:6px 12px 6px 0;vertical-align:top;white-space:nowrap;\">"
							+ "<strong>案件URL地址</strong></td>")
					.append("<td style=\"padding:6px 0;\"><a href=\"").append(escapeHtml(caseUrl)).append("\">")
					.append(escapeHtml(caseUrl)).append("</a></td></tr></table>")
					.append("<p>请及时登录系统查看案件资料并开始准备申请。</p>");
			sendMail(officialDo.getEmail(), title, content.toString());
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			ServiceException exception = new ServiceException("发送文案通知邮件失败: " + e.getMessage(), e);
			exception.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw exception;
		}
	}

	private String mailRow(String label, String value) {
		return "<tr><td width=\"120\" nowrap=\"nowrap\" "
				+ "style=\"width:120px;padding:6px 12px 6px 0;vertical-align:top;white-space:nowrap;\"><strong>"
				+ escapeHtml(label)
				+ "</strong></td><td style=\"padding:6px 0;white-space:pre-wrap;\">" + escapeHtml(value)
				+ "</td></tr>";
	}

	private String escapeHtml(String value) {
		return HtmlUtils.htmlEscape(valueOrEmpty(value));
	}

	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	private ServiceException notificationException(String message, int code) {
		ServiceException exception = new ServiceException(message);
		exception.setCode(code);
		return exception;
	}

	@Override
	public int deletePortal(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			return portalDao.deletePortal(id);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

}

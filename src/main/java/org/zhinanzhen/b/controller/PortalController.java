package org.zhinanzhen.b.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.PortalAttachmentService;
import org.zhinanzhen.b.service.PortalLogService;
import org.zhinanzhen.b.service.PortalService;
import org.zhinanzhen.b.service.PortalTypeService;
import org.zhinanzhen.b.service.pojo.PortalAttachmentDTO;
import org.zhinanzhen.b.service.pojo.PortalDTO;
import org.zhinanzhen.b.service.pojo.PortalLogDTO;
import org.zhinanzhen.b.service.pojo.PortalTypeDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.ListResponse;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.service.ServiceException;

import com.ikasoa.core.utils.StringUtil;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/portal")
public class PortalController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(PortalController.class);

	@Resource
	PortalService portalService;

	@Resource
	PortalTypeService portalTypeService;

	@Resource
	PortalAttachmentService portalAttachmentService;

	@Resource
	PortalLogService portalLogService;

	@RequestMapping(value = "/attachment/upload", method = RequestMethod.POST)
	@ResponseBody
	public Response<String> uploadAttachment(@RequestParam MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws IllegalStateException, IOException {
		super.setPostHeader(response);
		Response<String> uploadResp = super.upload2(file, request.getSession(), "/uploads/portal_attachment/");
		if (uploadResp.getCode() != 0) {
			return uploadResp;
		}
		// 在upload接口里就新增 b_portal_attachment 的数据
		PortalAttachmentDTO portalAttachmentDto = new PortalAttachmentDTO();
		portalAttachmentDto.setFileName(file.getOriginalFilename());
		portalAttachmentDto.setFilePath(uploadResp.getData());
		portalAttachmentDto.setFileSize(file.getSize());
		portalAttachmentDto.setFileType(file.getContentType());
		String originalName = file.getOriginalFilename();
		if (StringUtil.isNotEmpty(originalName) && originalName.contains("."))
			portalAttachmentDto.setFileExt(originalName.substring(originalName.lastIndexOf(".") + 1));
		portalAttachmentDto.setStage("apply");
		try {
			if (portalAttachmentService.addPortalAttachment(portalAttachmentDto) <= 0) {
				super.deleteFile(uploadResp.getData()); // 入库失败则删除已上传文件
				return new Response<String>(1, "附件信息保存失败.", null);
			}
		} catch (ServiceException e) {
			super.deleteFile(uploadResp.getData());
			return new Response<String>(e.getCode(), e.getMessage(), null);
		}
		return new Response<String>(0, "", uploadResp.getData());
	}

	@RequestMapping(value = "/attachment/delete", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> deleteAttachment(@RequestParam(value = "id", required = false) Integer id,
			@RequestParam(value = "filePath", required = false) String filePath, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			if (id != null && id > 0) {
				// 已入库：按附件id删除
				PortalAttachmentDTO portalAttachmentDto = portalAttachmentService.getPortalAttachment(id);
				if (portalAttachmentDto == null) {
					return new Response<Integer>(1, "附件不存在.", 0);
				}
				super.deleteFile(portalAttachmentDto.getFilePath());
				portalAttachmentService.deletePortalAttachmentById(id);
				return new Response<Integer>(0, id);
			} else if (StringUtil.isNotEmpty(filePath)) {
				// 已上传但未入库：按服务器上的路径删除文件
				super.deleteFile(filePath);
				portalAttachmentService.deletePortalAttachmentByPath(filePath);
				return new Response<Integer>(0, 0);
			} else {
				return new Response<Integer>(1, "参数错误：id和filePath至少传一个.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addPortal(@RequestParam(value = "typeId") String typeId,
			@RequestParam(value = "name") String name,
			@RequestParam(value = "gender", required = false) String gender,
			@RequestParam(value = "birthday", required = false) String birthday,
			@RequestParam(value = "passport", required = false) String passport,
			@RequestParam(value = "englishScore", required = false) String englishScore,
			@RequestParam(value = "completionDate", required = false) String completionDate,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "examResultsDate", required = false) String examResultsDate,
			@RequestParam(value = "studentVisaExpirationDate", required = false) String studentVisaExpirationDate,
			@RequestParam(value = "hasCompletionLetter", required = false) String hasCompletionLetter,
			@RequestParam(value = "jsonStr", required = false) String jsonStr,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "serviceOrderId", required = false) String serviceOrderId,
			@RequestParam(value = "strState", required = false) String strState,
			@RequestParam(value = "filePath", required = false) String filePath, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			PortalDTO portalDto = new PortalDTO();
			portalDto.setTypeId(StringUtil.toInt(typeId));
			portalDto.setName(name);
			if (StringUtil.isNotEmpty(gender))
				portalDto.setGender(gender);
			if (StringUtil.isNotEmpty(birthday))
				portalDto.setBirthday(new Date(Long.parseLong(birthday.trim())));
			if (StringUtil.isNotEmpty(passport))
				portalDto.setPassport(passport);
			if (StringUtil.isNotEmpty(englishScore))
				portalDto.setEnglishScore(englishScore);
			if (StringUtil.isNotEmpty(completionDate))
				portalDto.setCompletionDate(new Date(Long.parseLong(completionDate.trim())));
			if (StringUtil.isNotEmpty(visaExpirationDate))
				portalDto.setVisaExpirationDate(new Date(Long.parseLong(visaExpirationDate.trim())));
			if (StringUtil.isNotEmpty(examResultsDate))
				portalDto.setExamResultsDate(new Date(Long.parseLong(examResultsDate.trim())));
			if (StringUtil.isNotEmpty(studentVisaExpirationDate))
				portalDto.setStudentVisaExpirationDate(new Date(Long.parseLong(studentVisaExpirationDate.trim())));
			// 未传默认false（没有完成信），避免insert时写入null违反非空约束
			portalDto.setHasCompletionLetter("true".equalsIgnoreCase(hasCompletionLetter));
			if (StringUtil.isNotEmpty(jsonStr))
				portalDto.setJsonStr(jsonStr);
			if (StringUtil.isNotEmpty(adviserId))
				portalDto.setAdviserId(StringUtil.toInt(adviserId));
			if (StringUtil.isNotEmpty(officialId))
				portalDto.setOfficialId(StringUtil.toInt(officialId));
			if (StringUtil.isNotEmpty(maraId))
				portalDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(serviceOrderId))
				portalDto.setServiceOrderId(StringUtil.toInt(serviceOrderId));
			// 未传strState时默认"01"
			portalDto.setStrState(StringUtil.isNotEmpty(strState) ? strState : "01");
			if (portalService.addPortal(portalDto) > 0) {
				// 同步附件：根据addPortal传过来的路径，把已上传附件的portalId更新为新创建的案件ID
				syncPortalAttachments(filePath, portalDto.getId());
				// 操作日志：客户第一步入库
				savePortalLog(portalDto.getId(), "customer_first_submit", null, portalDto.getStrState(),
						"客户第一步提交案件", request);
				return new Response<Integer>(0, portalDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<PortalDTO> updatePortal(@RequestParam(value = "id") int id,
			@RequestParam(value = "typeId", required = false) String typeId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "gender", required = false) String gender,
			@RequestParam(value = "birthday", required = false) String birthday,
			@RequestParam(value = "passport", required = false) String passport,
			@RequestParam(value = "englishScore", required = false) String englishScore,
			@RequestParam(value = "completionDate", required = false) String completionDate,
			@RequestParam(value = "visaExpirationDate", required = false) String visaExpirationDate,
			@RequestParam(value = "examResultsDate", required = false) String examResultsDate,
			@RequestParam(value = "studentVisaExpirationDate", required = false) String studentVisaExpirationDate,
			@RequestParam(value = "hasCompletionLetter", required = false) String hasCompletionLetter,
			@RequestParam(value = "jsonStr", required = false) String jsonStr,
			@RequestParam(value = "adviserId", required = false) String adviserId,
			@RequestParam(value = "officialId", required = false) String officialId,
			@RequestParam(value = "maraId", required = false) String maraId,
			@RequestParam(value = "serviceOrderId", required = false) String serviceOrderId,
			@RequestParam(value = "strState", required = false) String strState,
			@RequestParam(value = "filePath", required = false) String filePath, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			PortalDTO portalDto = new PortalDTO();
			portalDto.setId(id);
			// 记录操作前状态（查不到时忽略，不影响主流程）
			String fromState = null;
			try {
				PortalDTO oldPortalDto = portalService.getPortal(id);
				if (oldPortalDto != null)
					fromState = oldPortalDto.getStrState();
			} catch (ServiceException ignored) {
			}
			if (StringUtil.isNotEmpty(typeId))
				portalDto.setTypeId(StringUtil.toInt(typeId));
			if (StringUtil.isNotEmpty(name))
				portalDto.setName(name);
			if (StringUtil.isNotEmpty(gender))
				portalDto.setGender(gender);
			if (StringUtil.isNotEmpty(birthday))
				portalDto.setBirthday(new Date(Long.parseLong(birthday.trim())));
			if (StringUtil.isNotEmpty(passport))
				portalDto.setPassport(passport);
			if (StringUtil.isNotEmpty(englishScore))
				portalDto.setEnglishScore(englishScore);
			if (StringUtil.isNotEmpty(completionDate))
				portalDto.setCompletionDate(new Date(Long.parseLong(completionDate.trim())));
			if (StringUtil.isNotEmpty(visaExpirationDate))
				portalDto.setVisaExpirationDate(new Date(Long.parseLong(visaExpirationDate.trim())));
			if (StringUtil.isNotEmpty(examResultsDate))
				portalDto.setExamResultsDate(new Date(Long.parseLong(examResultsDate.trim())));
			if (StringUtil.isNotEmpty(studentVisaExpirationDate))
				portalDto.setStudentVisaExpirationDate(new Date(Long.parseLong(studentVisaExpirationDate.trim())));
			if (StringUtil.isNotEmpty(hasCompletionLetter))
				portalDto.setHasCompletionLetter("true".equalsIgnoreCase(hasCompletionLetter));
			if (StringUtil.isNotEmpty(jsonStr))
				portalDto.setJsonStr(jsonStr);
			if (StringUtil.isNotEmpty(adviserId))
				portalDto.setAdviserId(StringUtil.toInt(adviserId));
			if (StringUtil.isNotEmpty(officialId))
				portalDto.setOfficialId(StringUtil.toInt(officialId));
			if (StringUtil.isNotEmpty(maraId))
				portalDto.setMaraId(StringUtil.toInt(maraId));
			if (StringUtil.isNotEmpty(serviceOrderId))
				portalDto.setServiceOrderId(StringUtil.toInt(serviceOrderId));
			if (StringUtil.isNotEmpty(strState))
				portalDto.setStrState(strState);
			if (portalService.updatePortal(portalDto) > 0) {
				// 同步附件：根据updatePortal传过来的路径，把已上传附件的portalId更新为当前案件ID
				syncPortalAttachments(filePath, portalDto.getId());
				// 操作日志：更新案件
				String toState = StringUtil.isNotEmpty(strState) ? strState : fromState;
				savePortalLog(portalDto.getId(), "update", fromState, toState, "更新案件信息", request);
				return new Response<PortalDTO>(0, portalDto);
			} else {
				return new Response<PortalDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<PortalDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public ListResponse<List<PortalDTO>> listPortal(@RequestParam(value = "typeId", required = false) Integer typeId,
			@RequestParam(value = "strState", required = false) String strState,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			// strState=ALL 表示查询全部案件，转成null不按状态过滤
			if ("ALL".equalsIgnoreCase(strState))
				strState = null;
			int total = portalService.countPortal(typeId, strState, keyword);
			List<PortalDTO> portalDtoList = portalService.listPortal(typeId, strState, keyword, pageNum, pageSize);
			return new ListResponse<List<PortalDTO>>(true, pageSize, total, portalDtoList, "");
		} catch (ServiceException e) {
			return new ListResponse<List<PortalDTO>>(false, pageSize, 0, null, e.getMessage());
		}
	}

	@RequestMapping(value = "/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<PortalDTO> getPortal(@RequestParam(value = "id") Integer id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<PortalDTO>(0, portalService.getPortal(id));
		} catch (ServiceException e) {
			return new Response<PortalDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deletePortal(@RequestParam(value = "id") int id, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			// 记录操作前状态（查不到时忽略，不影响主流程）
			String fromState = null;
			try {
				PortalDTO oldPortalDto = portalService.getPortal(id);
				if (oldPortalDto != null)
					fromState = oldPortalDto.getStrState();
			} catch (ServiceException ignored) {
			}
			if (portalService.deletePortal(id) > 0) {
				// 操作日志：删除案件
				savePortalLog(id, "delete", fromState, null, "删除案件", request);
				return new Response<Integer>(0, id);
			} else {
				return new Response<Integer>(1, "删除失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/log/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<PortalLogDTO>> listPortalLog(
			@RequestParam(value = "portalId", required = false) Integer portalId,
			@RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<PortalLogDTO>>(0, portalLogService.listPortalLog(portalId, pageNum, pageSize));
		} catch (ServiceException e) {
			return new Response<List<PortalLogDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/log/count", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> countPortalLog(@RequestParam(value = "portalId", required = false) Integer portalId,
			HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, portalLogService.countPortalLog(portalId));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), null);
		}
	}

	/**
	 * 保存案件操作日志（日志记录失败不影响主流程）
	 */
	private void savePortalLog(int portalId, String action, String fromState, String toState, String content,
			HttpServletRequest request) {
		try {
			AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
			PortalLogDTO portalLogDto = new PortalLogDTO();
			portalLogDto.setPortalId(portalId);
			portalLogDto.setAction(action);
			portalLogDto.setFromState(fromState);
			portalLogDto.setToState(toState);
			portalLogDto.setContent(content);
			portalLogDto.setIp(request.getRemoteAddr());
			portalLogDto.setUserAgent(request.getHeader("User-Agent"));
			if (adminUserLoginInfo != null) {
				portalLogDto.setOperatorId(adminUserLoginInfo.getId());
				portalLogDto.setOperatorName(adminUserLoginInfo.getUsername());
				portalLogDto.setRole(getRoleName(adminUserLoginInfo.getApList()));
			} else {
				// 未登录的操作视为客户
				portalLogDto.setRole("客户");
			}
			portalLogService.addPortalLog(portalLogDto);
		} catch (Exception e) {
			LOG.error("保存案件操作日志失败, portalId=" + portalId + ", action=" + action, e);
		}
	}

	/**
	 * 根据apList转换为操作人角色：GW=顾问, WA=文案, MA=mara, SUPERAD=超管
	 */
	private String getRoleName(String apList) {
		if (StringUtil.isEmpty(apList))
			return null;
		String ap = apList.toUpperCase();
		if (ap.contains("SUPERAD"))
			return "超管";
		if (ap.contains("MA"))
			return "mara";
		if (ap.contains("WA"))
			return "文案";
		if (ap.contains("GW"))
			return "顾问";
		return null;
	}

	/**
	 * 同步案件附件：把已上传附件（portalId为空）按路径批量挂到案件上。
	 * filePath支持多个路径，用英文逗号","或中文逗号"，"连接（一个案件可能有多个附件），
	 * 内部做去空格、去空串、去重。
	 */
	private void syncPortalAttachments(String filePath, int portalId) throws ServiceException {
		if (StringUtil.isEmpty(filePath))
			return;
		List<String> filePathList = Arrays.stream(filePath.split("[,，]")).map(String::trim)
				.filter(StringUtil::isNotEmpty).distinct().collect(Collectors.toList());
		if (!filePathList.isEmpty())
			portalAttachmentService.updatePortalIdByPathList(filePathList, portalId);
	}

	@RequestMapping(value = "/type/add", method = RequestMethod.POST)
	@ResponseBody
	public Response<Integer> addPortalType(@RequestParam(value = "name") String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "isDelete", required = false) String isDelete, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			PortalTypeDTO portalTypeDto = new PortalTypeDTO();
			portalTypeDto.setName(name);
			if (StringUtil.isNotEmpty(description))
				portalTypeDto.setDescription(description);
			if (StringUtil.isNotEmpty(sort))
				portalTypeDto.setSort(StringUtil.toInt(sort));
			// 未传默认0（未删除），避免insert时写入null违反非空约束
			portalTypeDto.setIsDelete("1".equals(isDelete) ? 1 : 0);
			if (portalTypeService.addPortalType(portalTypeDto) > 0) {
				return new Response<Integer>(0, portalTypeDto.getId());
			} else {
				return new Response<Integer>(1, "创建失败.", 0);
			}
		} catch (ServiceException e) {
			return new Response<Integer>(e.getCode(), e.getMessage(), 0);
		}
	}

	@RequestMapping(value = "/type/update", method = RequestMethod.POST)
	@ResponseBody
	public Response<PortalTypeDTO> updatePortalType(@RequestParam(value = "id") int id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "isDelete", required = false) String isDelete, HttpServletResponse response) {
		try {
			super.setPostHeader(response);
			PortalTypeDTO portalTypeDto = new PortalTypeDTO();
			portalTypeDto.setId(id);
			if (StringUtil.isNotEmpty(name))
				portalTypeDto.setName(name);
			if (StringUtil.isNotEmpty(description))
				portalTypeDto.setDescription(description);
			if (StringUtil.isNotEmpty(sort))
				portalTypeDto.setSort(StringUtil.toInt(sort));
			if (StringUtil.isNotEmpty(isDelete))
				portalTypeDto.setIsDelete("1".equals(isDelete) ? 1 : 0);
			if (portalTypeService.updatePortalType(portalTypeDto) > 0) {
				return new Response<PortalTypeDTO>(0, portalTypeDto);
			} else {
				return new Response<PortalTypeDTO>(1, "修改失败.", null);
			}
		} catch (ServiceException e) {
			return new Response<PortalTypeDTO>(e.getCode(), e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/type/list", method = RequestMethod.GET)
	@ResponseBody
	public Response<List<PortalTypeDTO>> listPortalType(
			@RequestParam(value = "isDelete", required = false) Integer isDelete,
			@RequestParam(value = "keyword", required = false) String keyword, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<List<PortalTypeDTO>>(0, portalTypeService.listPortalType(isDelete, keyword));
		} catch (ServiceException e) {
			return new Response<List<PortalTypeDTO>>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/type/get", method = RequestMethod.GET)
	@ResponseBody
	public Response<PortalTypeDTO> getPortalType(@RequestParam(value = "id") Integer id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<PortalTypeDTO>(0, portalTypeService.getPortalType(id));
		} catch (ServiceException e) {
			return new Response<PortalTypeDTO>(1, e.getMessage(), null);
		}
	}

	@RequestMapping(value = "/type/delete", method = RequestMethod.GET)
	@ResponseBody
	public Response<Integer> deletePortalType(@RequestParam(value = "id") int id, HttpServletResponse response) {
		try {
			super.setGetHeader(response);
			return new Response<Integer>(0, portalTypeService.deletePortalType(id));
		} catch (ServiceException e) {
			return new Response<Integer>(1, e.getMessage(), 0);
		}
	}

}

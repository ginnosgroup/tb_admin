package org.zhinanzhen.b.service.impl;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.OfficialGradeDao;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.OfficialEvaluate;
import org.zhinanzhen.b.dao.pojo.OfficialGradeDO;
import org.zhinanzhen.b.service.OfficialService;
import org.zhinanzhen.b.service.OfficialStateEnum;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.RegionDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;
import org.zhinanzhen.b.service.pojo.OfficialDTO;
import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("OfficialService")
public class OfficialServiceImpl extends BaseService implements OfficialService {
	@Resource
	private OfficialDAO officialDao;
	@Resource
	private RegionDAO regionDao;
	@Resource
	private AdminUserDAO adminUserDao;
	@Resource
	private OfficialGradeDao officialGradeDao;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

	@Override
	public int addOfficial(OfficialDTO officialDto) throws ServiceException {
		if (officialDto == null) {
			ServiceException se = new ServiceException("officialDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OfficialDO officialDo = mapper.map(officialDto, OfficialDO.class);
			if (officialDto.getState() != null) {
				officialDo.setState(officialDto.getState().toString());
			}
			if (officialDao.addOfficial(officialDo) > 0) {
				officialDto.setId(officialDo.getId());
				return officialDo.getId();
			} else {
				return 0;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateOfficial(OfficialDTO officialDto) throws ServiceException {
		if (officialDto == null) {
			ServiceException se = new ServiceException("officialDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OfficialDO officialDo = mapper.map(officialDto, OfficialDO.class);
			if (officialDto.getState() != null) {
				officialDo.setState(officialDto.getState().toString());
			}
			if ("RESIGN".equalsIgnoreCase(officialDo.getWorkState())) {
				officialDo.setState("DISABLED");
				AdminUserDO adminUserDO = adminUserDao.getUserByAdviserId(null, officialDo.getId());
				adminUserDao.updateState(adminUserDO.getId(), "DISABLED");
			}
			return officialDao.updateOfficial(officialDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int countOfficial(String name, Integer regionId, Integer gradeId) throws ServiceException {
		return officialDao.countOfficial(name, regionId, gradeId);
	}

	@Override
	public List<OfficialDTO> listOfficial(String name, Integer regionId, Integer gradeId, boolean isbuiltOrder, int pageNum, int pageSize)
			throws ServiceException {
		if (pageNum < 0) {
			pageNum = DEFAULT_PAGE_NUM;
		}
		if (pageSize < 0) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		List<OfficialDTO> officialDtoList = new ArrayList<OfficialDTO>();
		List<OfficialDO> officialDoList = new ArrayList<OfficialDO>();
		try {
			officialDoList = officialDao.listOfficial(name, regionId, gradeId, pageNum * pageSize, pageSize);
			if (officialDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (OfficialDO officialDo : officialDoList) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			String format = sdf.format(date);
			String dateStr = convertToYearMonth(format);
			List<String> previousMonths = getPreviousMonths(dateStr, 3);
			double averageScore = 0;
			for (String previousMonth : previousMonths) {
				// 解析年月字符串
				YearMonth yearMonth = YearMonth.parse(previousMonth, DateTimeFormatter.ofPattern("yyyy-MM"));

				// 获取月初第一天 00:00:00
				LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();

				// 获取月末最后一天 23:59:59
				LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

				// 创建格式化器
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

				// 格式化为字符串
				String startCollaborationTime = startOfMonth.format(formatter);
				String endCollaborationTime = endOfMonth.format(formatter);
				List<Integer> objects = new ArrayList<>();
				objects.add(officialDo.getId());
				List<OfficialEvaluate> officialEvaluates = officialDao.listOfficialEvaluate(objects, null, startCollaborationTime, endCollaborationTime, 0, 9999);
				if (officialEvaluates != null && !officialEvaluates.isEmpty()) {
					for (OfficialEvaluate officialEvaluate : officialEvaluates) {
						averageScore += extractScoreWithJackson(officialEvaluate);
					}
					officialDo.setAverageScore(DECIMAL_FORMAT.format(averageScore/officialEvaluates.size()));
				}
			}
			if (isbuiltOrder) {
				if (officialDo.getWorkState() != null && officialDo.getWorkState().equalsIgnoreCase("RESIGN"))
					continue;
			}
			OfficialDTO officialDto = mapper.map(officialDo, OfficialDTO.class);
			if (StringUtil.isNotEmpty(officialDo.getState())) {
				officialDto.setState(OfficialStateEnum.get(officialDo.getState()));
			}
			RegionDO regionDo = regionDao.getRegionById(officialDo.getRegionId());
			if (regionDo != null) {
				officialDto.setRegionName(regionDo.getName());
				officialDto.setRegionDo(regionDo);
			}
			AdminUserDO adminUserDo = adminUserDao.getAdminUserByUsername(officialDo.getEmail());
			if (adminUserDo != null)
				officialDto.setIsOfficialAdmin(adminUserDo.isOfficialAdmin());
			else
				officialDto.setIsOfficialAdmin(false);
			OfficialGradeDO grade = officialGradeDao.getOfficialGradeById(officialDo.getGradeId());
			if (grade!=null)
				officialDto.setGrade(grade.getGrade());
			officialDtoList.add(officialDto);
		}
		return officialDtoList;
	}

	@Override
	public OfficialDTO getOfficialById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		OfficialDTO officialDto = null;
		try {
			OfficialDO officialDo = officialDao.getOfficialById(id);
			if (officialDo == null) {
				// ServiceException se = new ServiceException("the Official is't
				// exist .");
				// se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
				// throw se;
				return null;
			}
			officialDto = mapper.map(officialDo, OfficialDTO.class);
			officialDto.setIsOfficialAdmin(officialDao.getOfficialAdmin(id)>0?true:false);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return officialDto;
	}

	@Override
	public int updateWorkState(OfficialDTO officialDTO) throws ServiceException {
		if (officialDTO == null){
			ServiceException se = new ServiceException("officialDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			OfficialDO officialDo = mapper.map(officialDTO, OfficialDO.class);
			if (StringUtil.isEmpty(officialDo.getWorkState())) {
				ServiceException se = new ServiceException("workState is null !");
				throw se;
			}
			return officialDao.updateOfficial(officialDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

    @Override
    public int addOfficialEvaluate(OfficialEvaluate officialEvaluate) {
		String dateStr = officialEvaluate.getCollaborationTime();
//		String dateStr = convertToYearMonth(collaborationTime);
		// 解析年月字符串
		YearMonth yearMonth = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM"));

		// 获取月初第一天 00:00:00
		LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();

		// 获取月末最后一天 23:59:59
		LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

		// 创建格式化器
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// 格式化为字符串
		String startCollaborationTime = startOfMonth.format(formatter);
		String endCollaborationTime = endOfMonth.format(formatter);
		OfficialEvaluate officialEvaluate1 = officialDao.getOfficialEvaluate(officialEvaluate.getOfficialId(), officialEvaluate.getAdviserId(), startCollaborationTime, endCollaborationTime);
		if (officialEvaluate1 != null) {
			return officialEvaluate1.getOfficialId();
		}
		officialEvaluate.setCollaborationTime(officialEvaluate.getCollaborationTime() + "-15 12:00:00");
		return officialDao.addOfficialEvaluate(officialEvaluate);
    }

	@Override
	public List<OfficialEvaluate> listOfficialEvaluate(@Param("officialIds")List<Integer> officialIds, Integer adviserId, String startCollaborationTime, String endCollaborationTime, Integer pageNum, Integer pageSize) {
		List<OfficialEvaluate> officialEvaluates = officialDao.listOfficialEvaluate(officialIds, adviserId, theDateTo00_00_00(startCollaborationTime), theDateTo23_59_59(endCollaborationTime), pageNum * pageSize, pageSize);
		for (OfficialEvaluate officialEvaluate : officialEvaluates) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			String format = sdf.format(date);
			String dateStr = convertToYearMonth(format);
			double averageScore = 0;
			List<String> previousMonths = getPreviousMonths(dateStr, 3);
			for (String previousMonth : previousMonths) {
				// 解析年月字符串
				YearMonth yearMonth = YearMonth.parse(previousMonth, DateTimeFormatter.ofPattern("yyyy-MM"));

				// 获取月初第一天 00:00:00
				LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();

				// 获取月末最后一天 23:59:59
				LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

				// 创建格式化器
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

				// 格式化为字符串
				String startCollaborationTimeT = startOfMonth.format(formatter);
				String endCollaborationTimeT = endOfMonth.format(formatter);
				List<Integer> objects = new ArrayList<>();
				objects.add(officialEvaluate.getOfficialId());
				List<OfficialEvaluate> officialEvaluatesT = officialDao.listOfficialEvaluate(objects, null, startCollaborationTimeT, endCollaborationTimeT, 0, 9999);
				if (officialEvaluatesT != null && !officialEvaluatesT.isEmpty()) {
					for (OfficialEvaluate officialEvaluate11 : officialEvaluatesT) {
						averageScore += extractScoreWithJackson(officialEvaluate11);
					}
					officialEvaluate.setThreeMonthsAverageScore(DECIMAL_FORMAT.format(averageScore/officialEvaluatesT.size()));
				}
			}
		}
		return officialEvaluates;
	}

	@Override
	public int countOfficialEvaluate(List<Integer> officialIds, Integer adviserId, String startCollaborationTime, String endCollaborationTime) {
		return officialDao.countOfficialEvaluate(officialIds, adviserId, theDateTo00_00_00(startCollaborationTime), theDateTo23_59_59(endCollaborationTime));
	}

	@Override
	public int updateOfficialEvaluate(OfficialEvaluate officialEvaluate) {
		return officialDao.updateOfficialEvaluate(officialEvaluate);
	}

	@Override
	public OfficialEvaluate getOfficialEvaluate(Integer integer, Integer adviserId, String startCollaborationTime, String endCollaborationTime) {
		return officialDao.getOfficialEvaluate(integer, adviserId, theDateTo00_00_00(startCollaborationTime), theDateTo23_59_59(endCollaborationTime));
	}

	@Override
	public Double getAverageScore(Integer integer, Integer adviserId, String collaborationTime, int mounths, boolean isCurrentMonth) {
		List<String> previousMonths = new ArrayList<>();
		if (collaborationTime == null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			String format = sdf.format(date);
			collaborationTime = convertToYearMonth(format);
		}
		if (isCurrentMonth) {
			previousMonths = getConsecutiveMonths(collaborationTime, mounths);
		} else {
			previousMonths = getPreviousMonths(collaborationTime, mounths);
		}
		double averageScore = 0;
		int averageCount = 0;
		for (String previousMonth : previousMonths) {
			// 解析年月字符串
			YearMonth yearMonth = YearMonth.parse(previousMonth, DateTimeFormatter.ofPattern("yyyy-MM"));

			// 获取月初第一天 00:00:00
			LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();

			// 获取月末最后一天 23:59:59
			LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

			// 创建格式化器
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			// 格式化为字符串
			String startCollaborationTime = startOfMonth.format(formatter);
			String endCollaborationTime = endOfMonth.format(formatter);
			OfficialEvaluate officialEvaluate1 = officialDao.getOfficialEvaluate(integer, adviserId, startCollaborationTime, endCollaborationTime);
			Double v = extractScoreWithJackson(officialEvaluate1);
			if (v > 0.00) {
				averageCount++;
			}
			averageScore += extractScoreWithJackson(officialEvaluate1);
		}
		return averageScore / (averageCount > 0 ? averageCount : 1);

	}

	public String convertToYearMonth(String dateStr) {
		try {
			// 尝试解析为日期时间格式
			DateTimeFormatter[] formatters = {
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
					DateTimeFormatter.ofPattern("yyyy-MM-dd"),
			};

			for (DateTimeFormatter formatter : formatters) {
				try {
					if (dateStr.length() == 10) { // yyyy-MM-dd
						LocalDate date = LocalDate.parse(dateStr, formatter);
						return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
					} else { // yyyy-MM-dd HH:mm:ss
						LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
						return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
					}
				} catch (DateTimeParseException e) {
					// 尝试下一种格式
					continue;
				}
			}
			throw new IllegalArgumentException("不支持的日期格式: " + dateStr);
		} catch (Exception e) {
			throw new IllegalArgumentException("日期解析失败: " + dateStr, e);
		}
	}

	/**
	 * 获取指定月份的前N个月份
	 * @param baseMonth 基础月份，格式：yyyy-MM
	 * @param monthsAgo 往前推的月数
	 * @return 前N个月的月份列表
	 */
	public static List<String> getPreviousMonths(String baseMonth, int monthsAgo) {
		List<String> result = new ArrayList<>();

		try {
			// 将字符串转换为LocalDate（添加日期部分）
			LocalDate baseDate = LocalDate.parse(baseMonth + "-01", DateTimeFormatter.ISO_LOCAL_DATE);

			for (int i = 1; i <= monthsAgo; i++) {
				LocalDate previousDate = baseDate.minusMonths(i);
				String previousMonth = previousDate.format(FORMATTER);
				result.add(previousMonth);
			}

		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("无效的月份格式，请使用 yyyy-MM 格式", e);
		}

		return result;
	}
	// 获取json数值
	public static Double extractScoreWithJackson(OfficialEvaluate officialEvaluate1) {
		try {
			if (officialEvaluate1 == null) {
				return 0.00;
			}
			Double count = 0.00;
			String accuracy = officialEvaluate1.getAccuracy();
			String professionalism = officialEvaluate1.getProfessionalism();
			String timelyCommunication = officialEvaluate1.getTimelyCommunication();
			// 确保JSON格式正确
			accuracy = accuracy
					.replace("score:", "\"score\":")
					.replace("remarks:", "\"remarks\":")
					.replace("'", "\"");

			professionalism = professionalism
					.replace("score:", "\"score\":")
					.replace("remarks:", "\"remarks\":")
					.replace("'", "\"");

			timelyCommunication = timelyCommunication
					.replace("score:", "\"score\":")
					.replace("remarks:", "\"remarks\":")
					.replace("'", "\"");

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(accuracy);

			// 提取score字段
			if (jsonNode.has("score")) {
				count += jsonNode.get("score").asInt();
			}
			JsonNode jsonNode1 = objectMapper.readTree(professionalism);

			// 提取score字段
			if (jsonNode1.has("score")) {
				count += jsonNode1.get("score").asInt();
			}
			JsonNode jsonNode2 = objectMapper.readTree(timelyCommunication);

			// 提取score字段
			if (jsonNode2.has("score")) {
				count += jsonNode2.get("score").asInt();
			}
			return count / 3;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.00;
	}

	/**
	 * 获取包括当前月份在内的连续N个月份
	 * @param baseMonth 基础月份，格式：yyyy-MM
	 * @param count 需要获取的月份数量（包括当前月份）
	 * @return 连续N个月的月份列表
	 */
	public static List<String> getConsecutiveMonths(String baseMonth, int count) {
		List<String> result = new ArrayList<>();

		try {
			// 将字符串转换为LocalDate（添加日期部分）
			LocalDate baseDate = LocalDate.parse(baseMonth + "-01", DateTimeFormatter.ISO_LOCAL_DATE);

			// 从当前月份开始，往前推0到count-1个月
			for (int i = 0; i < count; i++) {
				LocalDate targetDate = baseDate.minusMonths(i);
				String targetMonth = targetDate.format(FORMATTER);
				result.add(targetMonth);
			}

		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("无效的月份格式，请使用 yyyy-MM 格式", e);
		}

		return result;
	}
}

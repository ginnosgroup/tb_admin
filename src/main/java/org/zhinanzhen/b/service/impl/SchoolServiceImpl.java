package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.controller.BaseCommissionOrderController.CommissionStateEnum;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.SchoolAttachmentsDAO;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.SchoolSettingDAO;
import org.zhinanzhen.b.dao.SubagencyDAO;
import org.zhinanzhen.b.dao.SubjectSettingDAO;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.SchoolAttachmentsDTO;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.b.service.pojo.SchoolSettingDTO;
import org.zhinanzhen.b.service.pojo.SubjectSettingDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("SchoolService")
public class SchoolServiceImpl extends BaseService implements SchoolService {

	@Resource
	private SchoolDAO schoolDao;
	
	@Resource
	private SchoolAttachmentsDAO schoolAttachmentsDao;

	@Resource
	private SchoolSettingDAO schoolSettingDao;

	@Resource
	private SubjectSettingDAO subjectSettingDao;

	@Resource
	private CommissionOrderDAO commissionOrderDao;

	@Resource
	private SubagencyDAO subagencyDao;

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public int addSchool(SchoolDTO schoolDto) throws ServiceException {
		if (schoolDto == null) {
			ServiceException se = new ServiceException("schoolDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SchoolDO schoolDo = mapper.map(schoolDto, SchoolDO.class);
			if (schoolDao.addSchool(schoolDo) > 0) {
				// 添加学校设置
				if (schoolDo.getName() != null && schoolDo.getSubject() != null) {
					List<SchoolDO> schoolList = schoolDao.list2(schoolDo.getName(), null);
					if (schoolList != null && schoolList.size() > 0)
						schoolList.forEach(school -> {
							if (school.getSubject() == null || "".equals(school.getSubject())) {
								SchoolSettingDO schoolSettingDo = schoolSettingDao.getBySchoolId(school.getId());
								if (schoolSettingDo != null) {
									schoolSettingDo.setSchoolId(schoolDo.getId());
									schoolSettingDao.add(schoolSettingDo);
								}
							}
						});
				}
				schoolDto.setId(schoolDo.getId());
				return schoolDo.getId();
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
	@Transactional(rollbackFor = Exception.class)
	public  void refreshSchoolSetting() throws ServiceException {
		try {

			List<SchoolDO> schoolDOSNameGroup = schoolDao.listSchool(null, null);

			for (SchoolDO schoolDO : schoolDOSNameGroup) {
				//一个学校名字对应的	学校 + 专业集合
				List<SchoolDO> schoolDOS = schoolDao.list2(schoolDO.getName(), null);

				SchoolSettingDO setting1 = null; //学校规则settingdo
				SchoolDO school = null;//学校
				List<Integer> _schoolIds = new ArrayList<>();

				//学校
				for (SchoolDO s1 : schoolDOS){
					if (StringUtils.isEmpty(s1.getSubject())){
						school = s1;
						break;
					}
				}
				if (school == null)
					continue;
				setting1 = schoolSettingDao.getBySchoolId(school.getId());
				if (setting1 == null)
					continue;

				//更新专业规则，跟随学校的规则，如果专业已经有规则，就不更新
				for (SchoolDO s1 : schoolDOS) {
					SchoolSettingDO schoolSettingDO = schoolSettingDao.getBySchoolId(s1.getId());
					if (schoolSettingDO != null && StringUtils.isEmpty(schoolSettingDO.getParameters()) && schoolSettingDO.getType() == 0){
						schoolSettingDO.setType(setting1.getType());
						schoolSettingDO.setParameters(setting1.getParameters());
						schoolSettingDao.update(schoolSettingDO.getId(),schoolSettingDO.getType(),
								schoolSettingDO.getStartDate(),schoolSettingDO.getEndDate(),schoolSettingDO.getParameters());
						_schoolIds.add(s1.getId());
					}
				}

				for (Integer id : _schoolIds){
					List<CommissionOrderDO> commissionOrderDOS = commissionOrderDao.listCommissionOrderBySchoolId(id);
					Date startDate = setting1.getStartDate();
					Date endDate = setting1.getEndDate();
					int type = setting1.getType();
					String parameters = setting1.getParameters();
					for (CommissionOrderDO commission : commissionOrderDOS){
						if (commission.getGmtCreate().before(startDate) || commission.getGmtCreate().after(endDate))
							continue;
						if (type == 1)
							schoolSetting1(setting1.getSchoolName(), startDate, endDate, parameters);
						else if (type == 2)
							schoolSetting2(setting1.getSchoolName(), startDate, endDate, parameters);
						else if (type == 3)
							schoolSetting3(setting1.getSchoolName(), startDate, endDate, parameters);
						else if (type == 4)
							schoolSetting4(setting1.getSchoolName(), startDate, endDate, parameters);
						else if (type == 5)
							schoolSetting5(setting1, startDate, endDate, parameters);
						else if (type == 6)
							schoolSetting6(setting1, startDate, endDate, parameters);
						else if (type == 7)
							schoolSetting7(setting1, startDate, endDate, parameters);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		/*
		如果学校设置了规则，专业跟随学校规则。
		专业可以单独设置规则。
		如果学校没有设置规则，只是专业设置规则，学校规则保持为空。
		如果学校设置了规则，在专业设置规则之后，其他没有设置规则的专业跟随学校规则
		学校设置了规则，新增专业跟随学校规则。
		*/
	}

	@Override
	public int updateSchool(int id, String name, String subject, String country) throws ServiceException {
		try {
			return schoolDao.updateSchool(id, name, subject, country);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int updateSchoolAttachments(String name, String contractFile1, String contractFile2, String contractFile3,
			String remarks) throws ServiceException {
		try {
			if (schoolDao.list2(name, null).size() == 0) {
				ServiceException se = new ServiceException(StringUtil.merge("学校'", name, "'不存在!"));
				se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
				throw se;
			}
			List<SchoolAttachmentsDO> list = schoolAttachmentsDao.listBySchoolName(name);
			SchoolAttachmentsDO schoolAttachmentsDo = new SchoolAttachmentsDO();
			if (list.size() > 0)
				schoolAttachmentsDo = list.get(0);
			schoolAttachmentsDo.setSchoolName(name);
			schoolAttachmentsDo.setContractFile1(contractFile1);
			schoolAttachmentsDo.setContractFile2(contractFile2);
			schoolAttachmentsDo.setContractFile3(contractFile3);
			schoolAttachmentsDo.setRemarks(remarks);
			return list.size() > 0 ? schoolAttachmentsDao.updateSchoolAttachments(schoolAttachmentsDo)
					: schoolAttachmentsDao.addSchoolAttachments(schoolAttachmentsDo);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public List<SchoolDTO> list(String name) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
			schoolSettingDao.clear();
			schoolDoList = schoolDao.list2(name, null);
			if (schoolDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SchoolDO schoolDo : schoolDoList) {
			SchoolDTO schoolDto = mapper.map(schoolDo, SchoolDTO.class);
			List<SchoolAttachmentsDO> saList = schoolAttachmentsDao.listBySchoolName(schoolDto.getName());
			if (saList != null && saList.size() > 0)
				schoolDto.setSchoolAttachments(mapper.map(saList.get(0), SchoolAttachmentsDTO.class));
			schoolDtoList.add(schoolDto);
		}
		return schoolDtoList;
	}

	@Override
	public List<SchoolDTO> list(String name, String subject, String country) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
			schoolSettingDao.clear();
			schoolDoList = schoolDao.list(name, subject, country);
			if (schoolDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SchoolDO schoolDo : schoolDoList) {
			SchoolDTO schoolDto = mapper.map(schoolDo, SchoolDTO.class);
			List<SchoolAttachmentsDO> saList = schoolAttachmentsDao.listBySchoolName(schoolDto.getName());
			if (saList != null && saList.size() > 0)
				schoolDto.setSchoolAttachments(mapper.map(saList.get(0), SchoolAttachmentsDTO.class));
			schoolDtoList.add(schoolDto);
		}
		return schoolDtoList;
	}

	@Override
	public List<SchoolDTO> listSchool(String name, String country) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
			schoolSettingDao.clear();
			schoolDoList = schoolDao.listSchool(name, country);
			if (schoolDoList == null) {
				return null;
			}
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SchoolDO schoolDo : schoolDoList) {
			SchoolDTO schoolDto = mapper.map(schoolDo, SchoolDTO.class);
			List<SchoolAttachmentsDO> saList = schoolAttachmentsDao.listBySchoolName(schoolDto.getName());
			if (saList != null && saList.size() > 0)
				schoolDto.setSchoolAttachments(mapper.map(saList.get(0), SchoolAttachmentsDTO.class));
			schoolDtoList.add(schoolDto);
		}
		return schoolDtoList;
	}

	@Override
	public int updateSchoolSetting(int id, int type, Date startDate, Date endDate, String parameters)
			throws ServiceException {
		if (StringUtil.isEmpty(parameters) && type != 7)
			return -1;
		SchoolSettingDO schoolSettingDo = schoolSettingDao.getById(id);
		if (schoolSettingDo == null)
			return -1;
		if (type == 1)
			schoolSetting1(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
		else if (type == 2)
			schoolSetting2(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
		else if (type == 3)
			schoolSetting3(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
		else if (type == 4)
			schoolSetting4(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
		else if (type == 5)
			schoolSetting5(schoolSettingDo, startDate, endDate, parameters);
		else if (type == 6)
			schoolSetting6(schoolSettingDo, startDate, endDate, parameters);
		else if (type == 7)
			schoolSetting7(schoolSettingDo, startDate, endDate, parameters);

		return schoolSettingDao.update(id, type, startDate, endDate, parameters);
	}

	@Override
	public int updateSchoolSetting(CommissionOrderListDTO commissionOrderListDto) throws ServiceException {
		if (commissionOrderListDto == null || commissionOrderListDto.getSchool() == null)
			return -1;
		listSchoolSetting(null, null); // 初始化
		SchoolSettingDO schoolSettingDo = schoolSettingDao.getBySchoolId(commissionOrderListDto.getSchool().getId());
		if (schoolSettingDo == null)
			return -2;
		Date startDate = schoolSettingDo.getStartDate();
		Date endDate = schoolSettingDo.getEndDate();
		int type = schoolSettingDo.getType();
		String parameters = schoolSettingDo.getParameters();
		// 如果不在设置的时间内就不操作
		if (commissionOrderListDto.getGmtCreate().before(startDate)
				|| commissionOrderListDto.getGmtCreate().after(endDate))
			return -3;
		if (type == 1)
			schoolSetting1(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
		else if (type == 2)
			schoolSetting2(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
		else if (type == 3)
			schoolSetting3(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
		else if (type == 4)
			schoolSetting4(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
		else if (type == 5)
			schoolSetting5(schoolSettingDo, startDate, endDate, parameters);
		else if (type == 6)
			schoolSetting6(schoolSettingDo, startDate, endDate, parameters);
		else if (type == 7)
			schoolSetting7(schoolSettingDo, startDate, endDate, parameters);
		return 1;
	}

	@Override
	public List<SchoolSettingDTO> listSchoolSetting(String schoolName, String subjectName) throws ServiceException {
		List<SchoolDO> schoolDoList = null;
		if (StringUtil.isEmpty(schoolName)) {
			schoolSettingDao.clear();
			buildSchoolSettingList(schoolDao.list2(null, null));
			schoolDoList = schoolDao.listSchool(null, null); // 一级列表合并学校
		} else
			schoolDoList = schoolDao.list2(schoolName, subjectName); // 二级列表查询专业课程
		return buildSchoolSettingList(schoolDoList);
	}

	@Override
	@Deprecated
	public List<SubjectSettingDTO> listSubjectSetting(int schoolSettingId) throws ServiceException {
		List<SubjectSettingDTO> subjectSettingDtoList = new ArrayList<SubjectSettingDTO>();
		List<SchoolDO> schoolDoList = schoolDao.list2(null, null);
		if (schoolDoList == null)
			return null;
		schoolDoList.forEach(schoolDo -> {
			if (schoolDo != null) {
				int id = schoolDo.getId();
				SchoolSettingDO schoolSettingDo = schoolSettingDao.getBySchoolId(id);
				if (id > 0 && schoolSettingId == schoolSettingDo.getId()) {
					SubjectSettingDO subjectSettingDo = subjectSettingDao.get(schoolSettingId, schoolDo.getName());
					if (subjectSettingDo == null) {
						subjectSettingDo = new SubjectSettingDO();
						subjectSettingDo.setSchoolSettingId(schoolSettingId);
						subjectSettingDao.add(subjectSettingDo);
					}
				}
			}
		});
		List<SubjectSettingDO> subjectSettingDoList = subjectSettingDao.list(schoolSettingId);
		if (subjectSettingDoList == null)
			return null;
		subjectSettingDoList.forEach(
				subjectSettingDo -> subjectSettingDtoList.add(mapper.map(subjectSettingDo, SubjectSettingDTO.class)));
		return subjectSettingDtoList;
	}

	@Override
	public int updateSubjectSetting(int subjectSettingId, double price) throws ServiceException {
		if (subjectSettingId <= 0 || price < 0)
			return -1;
		return subjectSettingDao.update(subjectSettingId, price);
	}

	@Override
	public SchoolDTO getSchoolById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		SchoolDTO schoolDto = null;
		try {
			SchoolDO schoolDo = schoolDao.getSchoolById(id);
			if (schoolDo == null)
				return null;
			schoolDto = mapper.map(schoolDo, SchoolDTO.class);
			List<SchoolAttachmentsDO> saList = schoolAttachmentsDao.listBySchoolName(schoolDto.getName());
			if (saList != null && saList.size() > 0)
				schoolDto.setSchoolAttachments(mapper.map(saList.get(0), SchoolAttachmentsDTO.class));
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
		return schoolDto;
	}

	@Override
	public int deleteSchoolById(int id) throws ServiceException {
		if (id <= 0) {
			ServiceException se = new ServiceException("id error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SchoolDO schoolDo = schoolDao.getSchoolById(id);
			if (schoolDo != null) {
				schoolAttachmentsDao.deleteBySchoolName(schoolDo.getName());
				return schoolDao.deleteSchoolById(id);
			} else
				return 0;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public int deleteSchoolByName(String name) throws ServiceException {
		if (StringUtil.isEmpty(name)) {
			ServiceException se = new ServiceException("name error !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			schoolAttachmentsDao.deleteBySchoolName(name);
			return schoolDao.deleteSchoolByName(name);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	@Override
	public String getByCommissionOrderId(int id) {
		CommissionOrderListDO commissionOrderListDO = commissionOrderDao.getCommissionOrderById(id);
		if (commissionOrderListDO != null){
			SchoolSettingDO schoolSettingDO = schoolSettingDao.getBySchoolId(commissionOrderListDO.getSchoolId());
			if (schoolSettingDO != null){
				int type = schoolSettingDO.getType();
				String parameters = schoolSettingDO.getParameters();
				if (type == 1 || type == 2){
					String[] _parameters = parameters.split("[|]");
					return _parameters[0] + "%";
				}
				if (type == 4){
					String[] _parameters = parameters.split("[|]");
					return _parameters[1].split("/")[0] + "%";
				}
			}
		}
		return null;
	}

	private List<SchoolSettingDTO> buildSchoolSettingList(List<SchoolDO> schoolDoList) {
		List<SchoolSettingDTO> schoolSettingDtoList = new ArrayList<SchoolSettingDTO>();
		if (schoolDoList == null || schoolDoList.size() == 0)
			return null;
		schoolDoList.forEach(schoolDo -> {
			int id = schoolDo.getId();
			SchoolSettingDO schoolSettingDo = schoolSettingDao.getBySchoolId(id);
			SchoolSettingDTO schoolSettingDto = null;
			if (schoolSettingDo == null) {
				schoolSettingDo = new SchoolSettingDO();
				schoolSettingDo.setSchoolId(id);
				schoolSettingDao.add(schoolSettingDo);
			}
			schoolSettingDto = mapper.map(schoolSettingDo, SchoolSettingDTO.class);
			if (id > 0) {
				schoolSettingDto.setCount(commissionOrderDao.countCommissionOrderBySchoolId(id));
				Double stf = commissionOrderDao.sumTuitionFeeBySchoolId(id);
				if (stf != null)
					schoolSettingDto.setAmount(stf);
			}
			List<SchoolAttachmentsDO> saList = schoolAttachmentsDao.listBySchoolName(schoolSettingDto.getSchoolName());
			if (saList != null && saList.size() > 0)
				schoolSettingDto.setSchoolAttachments(mapper.map(saList.get(0), SchoolAttachmentsDTO.class));
			schoolSettingDtoList.add(schoolSettingDto);
		});
		return schoolSettingDtoList;
	}

	private void schoolSetting1(String schoolName, Date startDate, Date endDate, String parameters) {
		List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
				schoolName);
		list.forEach(co -> {
			double fee = co.getAmount();
			co.setCommission(fee * (Double.parseDouble(parameters.trim()) * 0.01));
			System.out.println(co.getId() + "学校设置计算=本次收款金额[" + fee + "]*设置参数[" + parameters + "]*0.01=" + co.getCommission());
			updateGST(co);
			commissionOrderDao.updateCommissionOrder(co);
		});
	}

	private void schoolSetting2(String schoolName, Date startDate, Date endDate, String parameters) {
		if (StringUtil.isEmpty(parameters))
			return;
		String[] _parameters = parameters.split("[|]");
		if (_parameters.length == 1) {
			schoolSetting1(schoolName, startDate, endDate, _parameters[0]);
			return;
		}
		double proportion = Double.parseDouble(_parameters[0].trim());
		List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
				schoolName);

		for (int i = 1; i < _parameters.length; i++) {
			String[] _parameter = _parameters[i].split("/");
			if (_parameter.length == 3) {
				int min = Integer.parseInt(_parameter[1].trim());
				int max = Integer.parseInt(_parameter[2].trim());
				if (list.size() >= min && list.size() <= max) {
					double _fee = Double.parseDouble(_parameter[0]);
					list.forEach(co -> {
						double fee = co.getAmount();
						co.setCommission(fee * (proportion * 0.01) + _fee);
						// System.out.print(bs.getId() + " : " + fee + " * ( 1 -
						// " + proportion + " * 0.01 ) + " + _fee + " = " +
						// bs.getCommission());
						System.out.println(co.getId() + "学校设置计算=本次收款金额[" + fee + "]*(设置比例[" + proportion + "]*0.01)+设置金额[" + _fee
								+ "]=" + co.getCommission());
						updateGST(co);
						commissionOrderDao.updateCommissionOrder(co);
					});
					break;
				}
			}
		}
	}

	@Deprecated
	private void schoolSetting3(String schoolName, Date startDate, Date endDate, String parameters) {
		if (StringUtil.isEmpty(parameters))
			return;
		String[] _parameters = parameters.split("[|]");
		if (_parameters.length == 1) {
			schoolSetting1(schoolName, startDate, endDate, _parameters[0]);
			return;
		}
		double proportion = Double.parseDouble(_parameters[0].trim());
		List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
				schoolName);

		for (int i = 1; i < _parameters.length; i++) {
			String[] _parameter = _parameters[i].split("/");
			if (_parameter.length == 2) {
				int number = Integer.parseInt(_parameter[1].trim());
				if (list.size() >= number) {
					double _fee = Double.parseDouble(_parameter[0]);
					list.forEach(co -> {
						double fee = co.getAmount();
						co.setCommission(fee * (proportion * 0.01) + _fee);
						System.out.println(co.getId() + "学校设置计算=本次收款金额[" + fee + "]*(设置比例[" + proportion + "]*0.01)+设置金额[" + _fee
								+ "]=" + co.getCommission());
						updateGST(co);
						commissionOrderDao.updateCommissionOrder(co);
					});
					break;
				}
			}
		}
	}

	private void schoolSetting4(String schoolName, Date startDate, Date endDate, String parameters) {
		if (StringUtil.isEmpty(parameters))
			return;
		String[] _parameters = parameters.split("[|]");
		if (_parameters.length == 1)
			return;
		List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
				schoolName);
		for (int i = 1; i < _parameters.length; i++) {
			String[] _parameter = _parameters[i].split("/");
			if (_parameter.length == 2) {
				int number = Integer.parseInt(_parameter[1].trim());
				if (list.size() >= number) {
					double proportion = Double.parseDouble(_parameter[0].trim());
					list.forEach(co -> {
						double fee = co.getAmount();
						co.setCommission(fee * (proportion * 0.01));
						System.out.println(co.getId() + "学校设置计算=本次收款金额[" + fee + "]*(设置比例[" + proportion + "]*0.01)="
								+ co.getCommission());
						updateGST(co);
						commissionOrderDao.updateCommissionOrder(co);
					});
					break;
				}
			}
		}
	}

	@Deprecated
	private void schoolSetting5(SchoolSettingDO schoolSetting, Date startDate, Date endDate, String parameters) {
		if (StringUtil.isEmpty(parameters))
			return;
		if (schoolSetting == null)
			return;
		try {
			listSubjectSetting(schoolSetting.getId()); // 初始化subjectSetting
		} catch (ServiceException e) {
		}
		String[] _parameters = parameters.split("[|]");
		if (_parameters.length == 1)
			return;
		List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
				schoolSetting.getSchoolName());
		for (int i = 1; i < _parameters.length; i++) {
			String[] _parameter = _parameters[i].split("/");
			if (_parameter.length == 2) {
				int number = Integer.parseInt(_parameter[1].trim());
				if (list.size() >= number) {
					double _fee = Double.parseDouble(_parameter[0].trim());
					list.forEach(co -> {
						SubjectSettingDO subjectSettingDo = subjectSettingDao.get(schoolSetting.getId(),
								schoolSetting.getSchoolName());
						if (subjectSettingDo != null)
							co.setCommission(
									subjectSettingDo.getPrice() > _fee ? subjectSettingDo.getPrice() - _fee : 0.00);
						else
							co.setCommission(co.getAmount() > _fee ? co.getAmount() - _fee : 0.00); // 正常情况下是不会执行到这里的
						// 打印日志
						if (subjectSettingDo != null)
							if (subjectSettingDo.getPrice() > _fee)
								System.out.println(co.getId() + "学校设置计算=设置金额[" + subjectSettingDo.getPrice() + "]-参数金额[" + _fee
										+ "]=" + co.getCommission());
							else
								System.out.println(co.getId() + "学校设置计算=0.00=" + co.getCommission());
						else if (co.getAmount() > _fee)
							System.out.println(co.getId() + "学校设置计算=本次收款金额[" + co.getAmount() + "]-参数金额[" + _fee + "]="
									+ co.getCommission());
						else
							System.out.println(co.getId() + "学校设置计算=0.00=" + co.getCommission());
						updateGST(co);
						commissionOrderDao.updateCommissionOrder(co);
					});
					break;
				}
			}
		}
	}

	@Deprecated
	private void schoolSetting6(SchoolSettingDO schoolSetting, Date startDate, Date endDate, String parameters) {
		if (StringUtil.isEmpty(parameters))
			return;
		if (schoolSetting == null)
			return;
		try {
			listSubjectSetting(schoolSetting.getId()); // 初始化subjectSetting
		} catch (ServiceException e) {
		}
		List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
				schoolSetting.getSchoolName());
		String[] _parameter = parameters.split("/");
		if (_parameter.length == 2) {
			int number = Integer.parseInt(_parameter[1].trim());
			if (list.size() >= number) {
				double _fee = Double.parseDouble(_parameter[0].trim());
				list.forEach(co -> {
					SubjectSettingDO subjectSettingDo = subjectSettingDao.get(schoolSetting.getId(),
							schoolSetting.getSchoolName());
					if (subjectSettingDo != null)
						co.setCommission(
								subjectSettingDo.getPrice() > _fee ? subjectSettingDo.getPrice() - _fee : 0.00);
					else
						co.setCommission(co.getAmount() > _fee ? co.getAmount() - _fee : 0.00); // 正常情况下是不会执行到这里的
					// 打印日志
					if (subjectSettingDo != null)
						if (subjectSettingDo.getPrice() > _fee)
							System.out.println(co.getId() + "学校设置计算=设置金额[" + subjectSettingDo.getPrice() + "]-参数金额[" + _fee + "]="
									+ co.getCommission());
						else
							System.out.println(co.getId() + "学校设置计算=0.00=" + co.getCommission());
					else if (co.getAmount() > _fee)
						System.out.println(co.getId() + "学校设置计算=本次收款金额[" + co.getAmount() + "]-参数金额[" + _fee + "]="
								+ co.getCommission());
					else
						System.out.println(co.getId() + "学校设置计算=0.00=" + co.getCommission());
					updateGST(co);
					commissionOrderDao.updateCommissionOrder(co);
				});
			}
		}
	}

	private void schoolSetting7(SchoolSettingDO schoolSetting, Date startDate, Date endDate, String parameters) {
		if (schoolSetting == null)
			return;
//		try {
//			listSubjectSetting(schoolSetting.getId()); // 初始化subjectSetting
//		} catch (ServiceException e) {
//		}
		List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
				schoolSetting.getSchoolName());
		list.forEach(co -> {
//			SubjectSettingDO subjectSettingDo = subjectSettingDao.get(schoolSetting.getId(),
//					schoolSetting.getSchoolName());
//			if (subjectSettingDo != null) {
//				co.setCommission(subjectSettingDo.getPrice());
//				System.out.println(
//						co.getId() + "学校设置计算=学校设置金额[" + subjectSettingDo.getPrice() + "]=" + co.getCommission());
			if (parameters != null) {
				co.setCommission(co.getAmount() - Double.parseDouble(parameters.trim()));
				System.out.println(co.getId() + "学校设置计算=本次收款金额[" + co.getAmount() + "]-学校设置金额[" + Double.parseDouble(parameters.trim()) + "]="
						+ co.getCommission());
			} else {
				co.setCommission(co.getAmount()); // 正常情况下是不会执行到这里的
				System.out.println(co.getId() + "学校设置计算=本次收款金额[" + co.getAmount() + "]=" + co.getCommission());
			}
			updateGST(co);
			commissionOrderDao.updateCommissionOrder(co);
		});
	}

	private void updateGST(CommissionOrderListDO co) {
		SubagencyDO subagencyDo = subagencyDao.getSubagencyById(co.getSubagencyId());
		if (subagencyDo != null) {
			if ("AU".equals(subagencyDo.getCountry())) {
				co.setExpectAmount(co.getCommission() * subagencyDo.getCommissionRate() * 1.1);
				System.out.println(co.getId() + "(澳洲)预收业绩=学校设置计算金额[" + co.getCommission() + "]*subagencyRate["
						+ subagencyDo.getCommissionRate() + "]*1.1=" + co.getExpectAmount());
			} else {
				co.setExpectAmount(co.getCommission() * subagencyDo.getCommissionRate());
				System.out.println(co.getId() + "(非澳洲)预收业绩=学校设置计算金额[" + co.getCommission() + "]*subagencyRate["
						+ subagencyDo.getCommissionRate() + "]=" + co.getExpectAmount());
			}
		} else {
			co.setExpectAmount(co.getCommission() * 1.1);
			System.out.println(co.getId() + "预收业绩=学校设置计算金额[" + co.getCommission() + "]*1.1=" + co.getExpectAmount());
		}
		double expectAmount = co.getSureExpectAmount() > 0 ? co.getSureExpectAmount() : co.getExpectAmount();
		System.out.println(co.getId() + "确认预收业绩=" + co.getSureExpectAmount());
		if (subagencyDo != null && "AU".equals(subagencyDo.getCountry())) {
			co.setGst(expectAmount / 11);
			System.out.println(co.getId() + "GST=预收业绩[" + expectAmount + "]/11=" + expectAmount);
			co.setDeductGst(expectAmount - co.getGst());
			System.out.println(co.getId() + "(澳洲)DeductGST=预收业绩[" + expectAmount + "]-GST[" + co.getGst() + "]="
					+ co.getDeductGst());
		} else {
			co.setDeductGst(expectAmount);
			System.out.println(co.getId() + "(非澳洲)DeductGST=预收业绩[" + expectAmount + "]=" + co.getDeductGst());
		}
		if (!CommissionStateEnum.YJY.toString().equalsIgnoreCase(co.getCommissionState())) {
			co.setBonus(co.getDeductGst() * 0.1);
			System.out.println(co.getId() + "月奖=DeductGST[" + co.getDeductGst() + "]*0.1=" + co.getBonus());
		}
	}

}

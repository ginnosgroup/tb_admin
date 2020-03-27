package org.zhinanzhen.b.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.SchoolDAO;
import org.zhinanzhen.b.dao.SchoolSettingDAO;
import org.zhinanzhen.b.dao.SubjectSettingDAO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.SchoolDO;
import org.zhinanzhen.b.dao.pojo.SchoolSettingDO;
import org.zhinanzhen.b.dao.pojo.SubjectSettingDO;
import org.zhinanzhen.b.service.SchoolService;
import org.zhinanzhen.b.service.pojo.CommissionOrderListDTO;
import org.zhinanzhen.b.service.pojo.SchoolDTO;
import org.zhinanzhen.b.service.pojo.SchoolSettingDTO;
import org.zhinanzhen.b.service.pojo.SubjectSettingDTO;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import com.ikasoa.core.thrift.ErrorCodeEnum;
import com.ikasoa.core.utils.StringUtil;

@Service("SchoolService")
public class SchoolServiceImpl extends BaseService implements SchoolService {

	@Resource
	private SchoolDAO schoolDao;

	@Resource
	private SchoolSettingDAO schoolSettingDao;

	@Resource
	private SubjectSettingDAO subjectSettingDao;

	@Resource
	private CommissionOrderDAO commissionOrderDao;

	@Override
	public int addSchool(SchoolDTO schoolDto) throws ServiceException {
		if (schoolDto == null) {
			ServiceException se = new ServiceException("schoolDto is null !");
			se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
			throw se;
		}
		try {
			SchoolDO schoolDo = mapper.map(schoolDto, SchoolDO.class);
			if (schoolDao.addSchool(schoolDo) > 0) {
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
	public List<SchoolDTO> list(String name) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
			schoolDoList = schoolDao.list2(name);
			if (schoolDoList == null)
				return null;
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.EXECUTE_ERROR.code());
			throw se;
		}
		for (SchoolDO schoolDo : schoolDoList) {
			SchoolDTO schoolDto = mapper.map(schoolDo, SchoolDTO.class);
			schoolDtoList.add(schoolDto);
		}
		return schoolDtoList;
	}

	@Override
	public List<SchoolDTO> list(String name, String subject, String country) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
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
			schoolDtoList.add(schoolDto);
		}
		return schoolDtoList;
	}

	@Override
	public List<SchoolDTO> listSchool(String name, String country) throws ServiceException {
		List<SchoolDTO> schoolDtoList = new ArrayList<SchoolDTO>();
		List<SchoolDO> schoolDoList = new ArrayList<SchoolDO>();
		try {
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
			schoolSetting7(schoolSettingDo, startDate, endDate);

		return schoolSettingDao.update(id, type, startDate, endDate, parameters);
	}

	@Override
	public int updateSchoolSetting(CommissionOrderListDTO commissionOrderListDto) throws ServiceException {
		if (commissionOrderListDto == null || commissionOrderListDto.getSchool() == null)
			return -1;
		listSchoolSetting(); // 初始化
		SchoolSettingDO schoolSettingDo = schoolSettingDao.get(commissionOrderListDto.getSchool().getName());
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
			schoolSetting7(schoolSettingDo, startDate, endDate);
		return 1;
	}

	@Override
	public List<SchoolSettingDTO> listSchoolSetting() throws ServiceException {
		List<SchoolSettingDTO> schoolSettingDtoList = new ArrayList<SchoolSettingDTO>();
		List<SchoolDO> schoolDoList = schoolDao.listSchool(null, null);
		if (schoolDoList == null || schoolDoList.size() == 0)
			return null;
		schoolDoList.forEach(schoolDo -> {
			String name = schoolDo.getName();
			SchoolSettingDO schoolSettingDo = schoolSettingDao.get(name);
			SchoolSettingDTO schoolSettingDto = null;
			if (schoolSettingDo == null) {
				schoolSettingDo = new SchoolSettingDO();
				schoolSettingDo.setSchoolName(name);
				schoolSettingDao.add(schoolSettingDo);
			}
			schoolSettingDto = mapper.map(schoolSettingDo, SchoolSettingDTO.class);
			if (name != null) {
				schoolSettingDto.setCount(commissionOrderDao.countCommissionOrderBySchoolName(name));
				if (commissionOrderDao.sumTuitionFeeBySchoolName(name) != null)
					schoolSettingDto.setAmount(commissionOrderDao.sumTuitionFeeBySchoolName(name));
			}
			schoolSettingDtoList.add(schoolSettingDto);
		});
		return schoolSettingDtoList;
	}

	@Override
	public List<SubjectSettingDTO> listSubjectSetting(int schoolSettingId) throws ServiceException {
		List<SubjectSettingDTO> subjectSettingDtoList = new ArrayList<SubjectSettingDTO>();
		List<SchoolDO> schoolDoList = schoolDao.listSchool(null, null);
		if (schoolDoList == null)
			return null;
		schoolDoList.forEach(schoolDo -> {
			String name = schoolDo.getName();
			SubjectSettingDO subjectSettingDo = subjectSettingDao.get(schoolSettingId, name);
			if (subjectSettingDo == null) {
				subjectSettingDo = new SubjectSettingDO();
				subjectSettingDo.setSchoolSettingId(schoolSettingId);
				subjectSettingDo.setSubject(name);
				subjectSettingDao.add(subjectSettingDo);
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
			if (schoolDo == null) {
				return null;
			}
			schoolDto = mapper.map(schoolDo, SchoolDTO.class);
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
			return schoolDao.deleteSchoolById(id);
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
			return schoolDao.deleteSchoolByName(name);
		} catch (Exception e) {
			ServiceException se = new ServiceException(e);
			se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
			throw se;
		}
	}

	private void schoolSetting1(String schoolName, Date startDate, Date endDate, String parameters) {
		List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate, schoolName);
		list.forEach(co -> {
			double fee = co.getTuitionFee();
			co.setCommission(fee * (Double.parseDouble(parameters.trim()) * 0.01));
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
		List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate, schoolName);

		for (int i = 1; i < _parameters.length; i++) {
			String[] _parameter = _parameters[i].split("/");
			if (_parameter.length == 3) {
				int min = Integer.parseInt(_parameter[1].trim());
				int max = Integer.parseInt(_parameter[2].trim());
				if (list.size() >= min && list.size() <= max) {
					double _fee = Double.parseDouble(_parameter[0]);
					list.forEach(co -> {
						double fee = co.getTuitionFee();
						co.setCommission(fee * (proportion * 0.01) + _fee);
						// System.out.print(bs.getId() + " : " + fee + " * ( 1 -
						// " + proportion + " * 0.01 ) + " + _fee + " = " +
						// bs.getCommission());
						updateGST(co);
						commissionOrderDao.updateCommissionOrder(co);
					});
					break;
				}
			}
		}
	}

	private void schoolSetting3(String schoolName, Date startDate, Date endDate, String parameters) {
		if (StringUtil.isEmpty(parameters))
			return;
		String[] _parameters = parameters.split("[|]");
		if (_parameters.length == 1) {
			schoolSetting1(schoolName, startDate, endDate, _parameters[0]);
			return;
		}
		double proportion = Double.parseDouble(_parameters[0].trim());
		List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate, schoolName);

		for (int i = 1; i < _parameters.length; i++) {
			String[] _parameter = _parameters[i].split("/");
			if (_parameter.length == 2) {
				int number = Integer.parseInt(_parameter[1].trim());
				if (list.size() >= number) {
					double _fee = Double.parseDouble(_parameter[0]);
					list.forEach(co -> {
						double fee = co.getTuitionFee();
						co.setCommission(fee * (proportion * 0.01) + _fee);
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
		List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate, schoolName);
		for (int i = 1; i < _parameters.length; i++) {
			String[] _parameter = _parameters[i].split("/");
			if (_parameter.length == 2) {
				int number = Integer.parseInt(_parameter[1].trim());
				if (list.size() >= number) {
					double proportion = Double.parseDouble(_parameter[0].trim());
					list.forEach(co -> {
						double fee = co.getTuitionFee();
						co.setCommission(fee * (proportion * 0.01));
						updateGST(co);
						commissionOrderDao.updateCommissionOrder(co);
					});
					break;
				}
			}
		}
	}

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
		List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
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
							co.setCommission(co.getTuitionFee() > _fee ? co.getTuitionFee() - _fee : 0.00); // 正常情况下是不会执行到这里的
						updateGST(co);
						commissionOrderDao.updateCommissionOrder(co);
					});
					break;
				}
			}
		}
	}

	private void schoolSetting6(SchoolSettingDO schoolSetting, Date startDate, Date endDate, String parameters) {
		if (StringUtil.isEmpty(parameters))
			return;
		if (schoolSetting == null)
			return;
		try {
			listSubjectSetting(schoolSetting.getId()); // 初始化subjectSetting
		} catch (ServiceException e) {
		}
		List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
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
						co.setCommission(co.getTuitionFee() > _fee ? co.getTuitionFee() - _fee : 0.00); // 正常情况下是不会执行到这里的
					updateGST(co);
					commissionOrderDao.updateCommissionOrder(co);
				});
			}
		}
	}

	private void schoolSetting7(SchoolSettingDO schoolSetting, Date startDate, Date endDate) {
		if (schoolSetting == null)
			return;
		try {
			listSubjectSetting(schoolSetting.getId()); // 初始化subjectSetting
		} catch (ServiceException e) {
		}
		List<CommissionOrderDO> list = commissionOrderDao.listCommissionOrderBySchool(startDate, endDate,
				schoolSetting.getSchoolName());
		list.forEach(co -> {
			SubjectSettingDO subjectSettingDo = subjectSettingDao.get(schoolSetting.getId(),
					schoolSetting.getSchoolName());
			if (subjectSettingDo != null)
				co.setCommission(subjectSettingDo.getPrice());
			else
				co.setCommission(co.getTuitionFee()); // 正常情况下是不会执行到这里的
			updateGST(co);
			commissionOrderDao.updateCommissionOrder(co);
		});
	}

	private void updateGST(CommissionOrderDO co) {
		co.setGst(co.getCommission() / 11);
		co.setDeductGst(co.getCommission() - co.getGst());
		co.setBonus(co.getDeductGst() * 0.1);
	}

}

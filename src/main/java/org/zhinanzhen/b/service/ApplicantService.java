package org.zhinanzhen.b.service;

import java.util.List;

import org.zhinanzhen.b.service.pojo.ApplicantDTO;
import org.zhinanzhen.tb.service.ServiceException;

public interface ApplicantService {

	int add(ApplicantDTO applicantDto) throws ServiceException;

	int count(int userId) throws ServiceException;

	List<ApplicantDTO> list(int userId) throws ServiceException;

	ApplicantDTO getById(int id) throws ServiceException;

	int update(ApplicantDTO applicantDto) throws ServiceException;

	int deleteById(int id) throws ServiceException;

}

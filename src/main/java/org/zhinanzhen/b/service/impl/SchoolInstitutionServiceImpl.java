package org.zhinanzhen.b.service.impl;

import com.ikasoa.core.ErrorCodeEnum;
import com.ikasoa.core.utils.ListUtil;
import com.ikasoa.core.utils.StringUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhinanzhen.b.controller.BaseCommissionOrderController;
import org.zhinanzhen.b.dao.*;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.service.SchoolInstitutionService;
import org.zhinanzhen.b.service.pojo.*;
import org.zhinanzhen.tb.service.ServiceException;
import org.zhinanzhen.tb.service.impl.BaseService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Date: 2021/04/01 17:56
 * Description:
 * Version: V1.0
 */
@Service
public class SchoolInstitutionServiceImpl extends BaseService implements SchoolInstitutionService{

    @Resource
    private SchoolInstitutionDAO schoolInstitutionDAO;

    @Resource
    private SchoolInstitutionLocationDAO schoolInstitutionLocationDAO;

    @Resource
    private SchoolCourseDAO schoolCourseDAO;

    @Resource
    private CommissionOrderDAO commissionOrderDao;

    @Resource
    private SubagencyDAO subagencyDao;

    @Resource
    private SchoolSettingNewDAO schoolSettingNewDAO;

    @Resource
    private SchoolAttachmentsDAO schoolAttachmentsDao;

    @Resource
    private SchoolInstitutionCommentDAO schoolInstitutionCommentDAO;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public List<SchoolInstitutionDTO> listSchoolInstitutionDTO(String name, String type, String code, Boolean isFreeze,
            int pageNum, int pageSize, String orderBy, String keyword) {
        if ( pageNum < 0 )
            pageNum = DEFAULT_PAGE_NUM;
        if ( pageSize < 0 )
            pageSize = DEFAULT_PAGE_SIZE;
        if (StringUtil.isBlank(orderBy)){
            orderBy = "ORDER BY gmt_create DESC";
        }else
            orderBy = "ORDER BY institution_trading_name ASC, institution_name ASC";
        List<SchoolInstitutionDO> schoolInstitutionDOS = schoolInstitutionDAO.listSchoolInstitutionDO(name,type, code, isFreeze,
                pageNum * pageSize, pageSize,orderBy,keyword);
        List<SchoolInstitutionDTO> schoolInstitutionDTOS = new ArrayList<>();
        if (schoolInstitutionDOS == null)
            return null;
        for (SchoolInstitutionDO si : schoolInstitutionDOS){
            SchoolInstitutionDTO schoolInstitutionDTO = mapper.map(si,SchoolInstitutionDTO.class);
            //schoolInstitutionDTO = putSchoolInfo(schoolInstitutionDTO,si.getId(),null);//学校课程和校区

            //先把这里注释掉，list速度比较慢
            //putAttachmentsSettingInfo(schoolInstitutionDTO);//添加合同，历史setting，setting

            String tradingName = si.getInstitutionTradingName();
            if (tradingName != null && tradingName.contains(";"))
                schoolInstitutionDTO.setInstitutionTradingName(tradingName.replaceAll("; ","/"));
            else if (tradingName != null && tradingName.contains(","))
                schoolInstitutionDTO.setInstitutionTradingName(tradingName.replaceAll(", ","/"));

            schoolInstitutionDTOS.add(schoolInstitutionDTO);
        }
        return schoolInstitutionDTOS;
    }

    @Override
    public SchoolInstitutionDTO getSchoolInstitutionById(Integer id) {
        SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getSchoolInstitutionById(id);
        if (schoolInstitutionDO == null)
            return  null;
        SchoolInstitutionDTO schoolInstitutionDTO = mapper.map(schoolInstitutionDO,SchoolInstitutionDTO.class);
        schoolInstitutionDTO = putSchoolInfo(schoolInstitutionDTO,schoolInstitutionDO.getId(),null);//学校课程和校区

        putAttachmentsSettingInfo(schoolInstitutionDTO);//添加合同，历史setting，setting

        return schoolInstitutionDTO;
    }

    @Override
    public List<String> getTradingNamesById(Integer id) {
        List<String> tradingNames = new ArrayList<>();
        SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getTradingNameById(id);
        String tradingName = schoolInstitutionDO.getInstitutionTradingName();
        if (StringUtil.isNotEmpty(tradingName) && tradingName.contains(";"))
            tradingNames = ListUtil.buildArrayList(tradingName.split(";"));
        else if (StringUtil.isNotEmpty(tradingName) && tradingName.contains(","))
            tradingNames = ListUtil.buildArrayList(tradingName.split(","));
        else if (StringUtil.isEmpty(tradingName) || "null".equalsIgnoreCase(tradingName) || "n/a".equalsIgnoreCase(tradingName))
            tradingNames.add(schoolInstitutionDO.getInstitutionName());
        else if (StringUtil.isNotEmpty(tradingName) && tradingName.contains("/"))
            tradingNames = ListUtil.buildArrayList(tradingName.split("/"));
        else
            tradingNames.add(tradingName);
        for (int i = 0 ; i < tradingNames.size() ; i++){
            tradingNames.set(i, tradingNames.get(i).trim());
        }
        return tradingNames;
    }

    @Override
    public SchoolInstitutionDTO getSchoolInstitutionByCode(String code) {
        SchoolInstitutionDO schoolInstitutionDO = schoolInstitutionDAO.getSchoolInstitutionByCode(code);
        if (schoolInstitutionDO == null)
            return  null;
        SchoolInstitutionDTO schoolInstitutionDTO = mapper.map(schoolInstitutionDO,SchoolInstitutionDTO.class);
        schoolInstitutionDTO = putSchoolInfo(schoolInstitutionDTO,schoolInstitutionDO.getId(),null);
        return schoolInstitutionDTO;
    }

    @Override
    public int count(String name, String type,String code,  Boolean isFreeze) {
        return schoolInstitutionDAO.count(name,type,code,isFreeze);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SchoolInstitutionDTO schoolInstitutionDTO) throws ServiceException {
        if (schoolInstitutionDTO == null){
            ServiceException se = new ServiceException("SchoolInstitutionDTO is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            SchoolInstitutionDO schoolInstitutionDO = mapper.map(schoolInstitutionDTO,SchoolInstitutionDO.class);
            if (schoolInstitutionDAO.update(schoolInstitutionDO)){
                List<SchoolInstitutionLocationDTO> schoolInstitutionLocationDTOS =  schoolInstitutionDTO.getSchoolInstitutionLocationDTOS();
                SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH,false);
                SchoolInstitutionLocationDAO institutionLocationMapper = session.getMapper(SchoolInstitutionLocationDAO.class);
                if (schoolInstitutionLocationDTOS == null)
                    return true;
                int size = schoolInstitutionLocationDTOS.size();
                for (int i = 0 ; i < size ; i++) {
                    SchoolInstitutionLocationDO institutionLocation = mapper.map(schoolInstitutionLocationDTOS.get(i),SchoolInstitutionLocationDO.class);
                    institutionLocation.setProviderCode(schoolInstitutionDO.getCode());
                    institutionLocation.setProviderId(schoolInstitutionDO.getId());
                    institutionLocationMapper.add(institutionLocation);
                }
                session.commit();
                session.clearCache();
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException(e);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(SchoolInstitutionDTO schoolInstitutionDTO) throws ServiceException {
        if (schoolInstitutionDTO == null){
            ServiceException se = new ServiceException("SchoolInstitutionDTO is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            SchoolInstitutionDO schoolInstitutionDO = mapper.map(schoolInstitutionDTO,SchoolInstitutionDO.class);
            if (schoolInstitutionDAO.add(schoolInstitutionDO) > 0){
                schoolInstitutionDTO.setId(schoolInstitutionDO.getId());
                List<SchoolInstitutionLocationDTO> schoolInstitutionLocationDTOS =  schoolInstitutionDTO.getSchoolInstitutionLocationDTOS();
                SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH,false);
                SchoolInstitutionLocationDAO institutionLocationMapper = session.getMapper(SchoolInstitutionLocationDAO.class);
                if (schoolInstitutionLocationDTOS == null)
                    return schoolInstitutionDTO.getId();
                int size = schoolInstitutionLocationDTOS.size();
                for (int i = 0 ; i < size ; i++) {
                    SchoolInstitutionLocationDO institutionLocation = mapper.map(schoolInstitutionLocationDTOS.get(i),SchoolInstitutionLocationDO.class);
                    institutionLocation.setProviderCode(schoolInstitutionDO.getCode());
                    institutionLocation.setProviderId(schoolInstitutionDO.getId());
                    institutionLocationMapper.add(institutionLocation);
                }
                session.commit();
                session.clearCache();
                return schoolInstitutionDTO.getId();
            }
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    @Override
    public boolean delete(int id) {
        return schoolInstitutionDAO.delete(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addSetting(SchoolSettingNewDTO schoolSettingNewDTO) {
        Date startDate = schoolSettingNewDTO.getStartDate();
        Date endDate = schoolSettingNewDTO.getEndDate();
        String Parameters = schoolSettingNewDTO.getParameters();
        int providerId = schoolSettingNewDTO.getProviderId();
        String courseLevel = schoolSettingNewDTO.getCourseLevel();
        Integer courseId = schoolSettingNewDTO.getCourseId();
        int level = schoolSettingNewDTO.getLevel();
        if (schoolSettingNewDAO.add(mapper.map(schoolSettingNewDTO,SchoolSettingNewDO.class)) > 0){
            /*
            if (schoolSettingNewDTO.getType() == 1)
                schoolSetting1(providerId, courseLevel, courseId,level, startDate, endDate, Parameters );
            if (schoolSettingNewDTO.getType() == 2)
                schoolSetting2(providerId, courseLevel, courseId, level,startDate, endDate , Parameters ,schoolSettingNewDTO.getRegisterFee(),schoolSettingNewDTO.getBookFee());
            if (schoolSettingNewDTO.getType() == 4)
                schoolSetting4(providerId, courseLevel, courseId,level, startDate, endDate , Parameters ,schoolSettingNewDTO.getRegisterFee(),schoolSettingNewDTO.getBookFee());
            if (schoolSettingNewDTO.getType() == 7)
                schoolSetting7(providerId, courseLevel, courseId,level, startDate, endDate , Parameters ,schoolSettingNewDTO.getRegisterFee(),schoolSettingNewDTO.getBookFee());
             */
            List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
            for(CommissionOrderListDO co : list) {
                SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(),co.getGmtCreate());
                if (schoolSettingNewDO == null)
                    continue;
                if (schoolSettingNewDTO.getType() == 1)
                    schoolSetting1(co, schoolSettingNewDO);
                if (schoolSettingNewDTO.getType() == 2)
                    schoolSetting2(co, schoolSettingNewDO, list.size());
                if (schoolSettingNewDTO.getType() == 4)
                    schoolSetting4(co, schoolSettingNewDO, list.size());
                if (schoolSettingNewDTO.getType() == 7)
                    schoolSetting7(co, schoolSettingNewDO);
            }
            return 1;
        }else
            return -1;
    }

    /**
     *level = 1 param: providerId,level
     *level = 2 param: providerId ,courseLevel,level
     * level = 3 param: providerId ,courseId,level
     * 返回对应级别的SchoolSettingNewDTO
     * @param providerId
     * @param level
     * @param courseLevel
     * @param courseId
     * @return  SchoolSettingNewDTO
     */
    @Override
    public SchoolSettingNewDTO getByProviderIdAndLevel(int providerId, Integer level, String courseLevel, Integer courseId) {
        List<SchoolSettingNewDO> schoolSettingNewDOs = schoolSettingNewDAO.getByProviderIdAndLevel(providerId, level, courseLevel, courseId,false);
        List<SchoolSettingNewDTO> schoolSettingNewDTOS = new ArrayList<>();
        //if (schoolSettingNewDO != null)
        //   return mapper.map(schoolSettingNewDO,SchoolSettingNewDTO.class);
        schoolSettingNewDOs.forEach(schoolSettingNewDO -> {
            schoolSettingNewDTOS.add(mapper.map(schoolSettingNewDO,SchoolSettingNewDTO.class));
        });
        return schoolSettingNewDTOS.get(1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSetting(SchoolSettingNewDTO schoolSettingNewDTO) throws ServiceException {
        if (schoolSettingNewDTO == null)
            throw new ServiceException(" SchoolSettingNewDTO is null");
        SchoolSettingNewDO schoolSettingNewDO = mapper.map(schoolSettingNewDTO,SchoolSettingNewDO.class);
        if (schoolSettingNewDAO.delete(schoolSettingNewDTO.getId()) > 0 && schoolSettingNewDAO.add1(schoolSettingNewDO) > 0){
            Date startDate = schoolSettingNewDTO.getStartDate();
            Date endDate = schoolSettingNewDTO.getEndDate();
            String Parameters = schoolSettingNewDTO.getParameters();
            int providerId = schoolSettingNewDTO.getProviderId();
            String courseLevel = schoolSettingNewDTO.getCourseLevel();
            Integer courseId = schoolSettingNewDTO.getCourseId();
            int level = schoolSettingNewDTO.getLevel();
            /*
            if (schoolSettingNewDTO.getType() == 1)
                schoolSetting1(providerId, courseLevel, courseId,level, startDate, endDate, Parameters);
            else if (schoolSettingNewDTO.getType() == 2)
                schoolSetting2(providerId, courseLevel, courseId, level,startDate, endDate , Parameters ,schoolSettingNewDTO.getRegisterFee(),schoolSettingNewDTO.getBookFee());
            else if (schoolSettingNewDTO.getType() == 4)
                schoolSetting4(providerId, courseLevel, courseId,level, startDate, endDate , Parameters ,schoolSettingNewDTO.getRegisterFee(),schoolSettingNewDTO.getBookFee());
            else if (schoolSettingNewDTO.getType() == 7)
                schoolSetting7(providerId, courseLevel, courseId,level, startDate, endDate , Parameters ,schoolSettingNewDTO.getRegisterFee(),schoolSettingNewDTO.getBookFee());
            else
                return -1;
             */
            List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
            for(CommissionOrderListDO co : list) {
                SchoolSettingNewDO _schoolSettingNewDO = returnSetting(co.getCourseId(),co.getGmtCreate());
                if (_schoolSettingNewDO == null)
                    continue;
                if (_schoolSettingNewDO.getType() == 1)
                    schoolSetting1(co, _schoolSettingNewDO);
                if (_schoolSettingNewDO.getType() == 2)
                    schoolSetting2(co, _schoolSettingNewDO, list.size());
                if (_schoolSettingNewDO.getType() == 4)
                    schoolSetting4(co, _schoolSettingNewDO, list.size());
                if (_schoolSettingNewDO.getType() == 7)
                    schoolSetting7(co, _schoolSettingNewDO);
            }
            return 1;
        }
        return 0;
    }

    @Override
    public int deleteSetting(int id) throws ServiceException {
        if (id <= 0)
            throw new ServiceException(" id error");
        return schoolSettingNewDAO.delete(id);
    }

    @Override
    public int updateSchoolAttachments(int providerId, String contractFile1, String contractFile2, String contractFile3, String remarks) throws ServiceException {
        try {
            if (schoolInstitutionDAO.getSchoolInstitutionById(providerId) == null){
                ServiceException se = new ServiceException(providerId + " 学校id不存在!");
                se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
                throw  se;
            }
            List<SchoolAttachmentsDO> list = schoolAttachmentsDao.listByProviderId(providerId);
            SchoolAttachmentsDO schoolAttachmentsDo = new SchoolAttachmentsDO();
            if (list.size() > 0)
                schoolAttachmentsDo = list.get(0);
            schoolAttachmentsDo.setProviderId(providerId);
            schoolAttachmentsDo.setContractFile1(contractFile1);
            schoolAttachmentsDo.setContractFile2(contractFile2);
            schoolAttachmentsDo.setContractFile3(contractFile3);
            schoolAttachmentsDo.setRemarks(remarks);
            return list.size() > 0 ? schoolAttachmentsDao.updateSchoolAttachments(schoolAttachmentsDo)
                    : schoolAttachmentsDao.addSchoolAttachments(schoolAttachmentsDo);
        }catch (Exception e){
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public int updateSchoolSetting(CommissionOrderListDTO commissionOrderListDto) throws ServiceException {
        if (commissionOrderListDto == null || commissionOrderListDto.getCourseId() == 0)
            return -1;
        SchoolSettingNewDO schoolSettingNewDO = returnSetting(commissionOrderListDto.getCourseId(),commissionOrderListDto.getGmtCreate());
        if (schoolSettingNewDO == null)
            return -2;
        Date startDate = schoolSettingNewDO.getStartDate();
        Date endDate = schoolSettingNewDO.getEndDate();
        int type = schoolSettingNewDO.getType();
        String parameters = schoolSettingNewDO.getParameters();
        int providerId = schoolSettingNewDO.getProviderId();
        String courseLevel = schoolSettingNewDO.getCourseLevel();
        Integer courseId = schoolSettingNewDO.getCourseId();
        int level = schoolSettingNewDO.getLevel();
        // 如果不在设置的时间内就不操作
        if (commissionOrderListDto.getGmtCreate().before(startDate)
                || commissionOrderListDto.getGmtCreate().after(endDate))
            return -3;

        List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
        CommissionOrderListDO co = mapper.map(commissionOrderListDto,CommissionOrderListDO.class);
        if (type == 1){
            //schoolSetting1(providerId, courseLevel, courseId,level, startDate, endDate, parameters);
            schoolSetting1(co, schoolSettingNewDO);
        }
        else if (type == 2){
            //schoolSetting2(providerId, courseLevel, courseId,level, startDate, endDate , parameters ,
            //       schoolSettingNewDO.getRegisterFee().doubleValue(),schoolSettingNewDO.getBookFee().doubleValue());
            schoolSetting2(co, schoolSettingNewDO, list.size());
        }
        else if (type == 3){
            //schoolSetting3(schoolSettingDo.getSchoolName(), startDate, endDate, parameters);
        }
        else if (type == 4){
            //schoolSetting4(providerId, courseLevel, courseId,level, startDate, endDate , parameters ,
            //        schoolSettingNewDO.getRegisterFee().doubleValue(),schoolSettingNewDO.getBookFee().doubleValue());
            schoolSetting4(co, schoolSettingNewDO, list.size());
        }

        else if (type == 5){
            //schoolSetting5(schoolSettingDo, startDate, endDate, parameters);
        }
        else if (type == 6){
            //schoolSetting6(schoolSettingDo, startDate, endDate, parameters);
        }
        else if (type == 7){
            //schoolSetting7(providerId, courseLevel, courseId,level, startDate, endDate , parameters ,
             //       schoolSettingNewDO.getRegisterFee().doubleValue(),schoolSettingNewDO.getBookFee().doubleValue());
            schoolSetting7(co, schoolSettingNewDO);
        }
        return 1;
    }

    @Override
    public SchoolSettingNewDTO getSchoolSettingNewById(int id) {
        SchoolSettingNewDO schoolSettingNewDO = schoolSettingNewDAO.getSchoolSettingNewById(id);
        SchoolSettingNewDTO schoolSettingNewDTO = null;
        if (schoolSettingNewDO != null)
            schoolSettingNewDTO = mapper.map(schoolSettingNewDO,SchoolSettingNewDTO.class);
        return schoolSettingNewDTO;
    }

    @Override
    public int deleteSchoolAttachments(int providerId, boolean isDeleteFile1, boolean isDeleteFile2, boolean isDeleteFile3) throws ServiceException {
        if (providerId <= 0) {
            ServiceException se = new ServiceException("providerId error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            return schoolAttachmentsDao.deleteSchoolAttachments(providerId,isDeleteFile1,isDeleteFile2,isDeleteFile3);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public int addComment(SchoolInstitutionCommentDTO commentDTO) throws ServiceException {
        if (commentDTO == null){
            ServiceException se = new ServiceException("SchoolInstitutionCommentDTO is null !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            SchoolInstitutionCommentDO commentDO = mapper.map(commentDTO,SchoolInstitutionCommentDO.class);
            if (schoolInstitutionCommentDAO.addComment(commentDO) > 0){
                commentDTO.setId(commentDO.getId());
                return commentDO.getId();
            }
            return 0;
        }catch (Exception e){
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public List<SchoolInstitutionCommentDTO> listComment(int schoolInstitutionId) throws ServiceException {
        if (schoolInstitutionId <= 0) {
            ServiceException se = new ServiceException("id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            List<SchoolInstitutionCommentDO> comments = schoolInstitutionCommentDAO.list(schoolInstitutionId,0);//查询父级评论

            //List<SchoolInstitutionCommentDO> commentdos = new ArrayList<>();
            List<SchoolInstitutionCommentDTO> commentdtos = new ArrayList<>();
            comments.forEach(comment->{
                SchoolInstitutionCommentDTO commentDTO = mapper.map(comment,SchoolInstitutionCommentDTO.class);
                List<SchoolInstitutionCommentDO> childComment = schoolInstitutionCommentDAO.list(schoolInstitutionId,comment.getId());
                commentDTO.getCommentdos().addAll(childComment);
                commentdtos.add(commentDTO);
            });
            return commentdtos;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public int deleteComment(int id) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            return schoolInstitutionCommentDAO.delete(id);
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    @Override
    public SchoolInstitutionCommentDTO getCommentById(int id) throws ServiceException {
        if (id <= 0) {
            ServiceException se = new ServiceException("id error !");
            se.setCode(ErrorCodeEnum.PARAMETER_ERROR.code());
            throw se;
        }
        try {
            SchoolInstitutionCommentDO commentdo = schoolInstitutionCommentDAO.getCommentById(id);
            if (commentdo != null)
                return mapper.map(commentdo,SchoolInstitutionCommentDTO.class);
            return null;
        } catch (Exception e) {
            ServiceException se = new ServiceException(e);
            se.setCode(ErrorCodeEnum.OTHER_ERROR.code());
            throw se;
        }
    }

    /**
     * 通过 courseId 和 佣金订单创建时间返回该专业所属的setting
     * 如果没有返回 null
     * @param courseId
     * @param gmtCreate
     * @return
     */
    public SchoolSettingNewDO returnSetting(int courseId, Date gmtCreate){
        SchoolCourseDO schoolCourseDO =  schoolCourseDAO.schoolCourseById(courseId);
        SchoolSettingNewDO schoolSettingNewDO = null;
        List<SchoolSettingNewDO> schoolSettingNewDOList = new ArrayList<>();
        if (schoolCourseDO != null){
            schoolSettingNewDOList = schoolSettingNewDAO.getByProviderIdAndLevel(schoolCourseDO.getProviderId(),
                    3,null,courseId, false);
            for (SchoolSettingNewDO setting : schoolSettingNewDOList){
                if (setting.getStartDate().before(gmtCreate) && setting.getEndDate().after(gmtCreate)){
                    schoolSettingNewDO = setting;
                    break;
                }
            }
            if (schoolSettingNewDO != null)
                return schoolSettingNewDO;

            schoolSettingNewDOList = schoolSettingNewDAO.getByProviderIdAndLevel(schoolCourseDO.getProviderId(),
                    2,schoolCourseDO.getCourseLevel(),null, false);
            for (SchoolSettingNewDO setting : schoolSettingNewDOList){
                if (setting.getStartDate().before(gmtCreate) && setting.getEndDate().after(gmtCreate)){
                    schoolSettingNewDO = setting;
                    break;
                }
            }
            if (schoolSettingNewDO != null && schoolSettingNewDO.getCourseLevel().equalsIgnoreCase(schoolCourseDO.getCourseLevel()))
                return schoolSettingNewDO;

            schoolSettingNewDOList = schoolSettingNewDAO.getByProviderIdAndLevel(schoolCourseDO.getProviderId(),
                    1,null,null, false);
            for (SchoolSettingNewDO setting : schoolSettingNewDOList){
                if (setting.getStartDate().before(gmtCreate) && setting.getEndDate().after(gmtCreate)){
                    schoolSettingNewDO = setting;
                    break;
                }
            }
            if (schoolSettingNewDO != null)
                return schoolSettingNewDO;
        }
        return null;
    }

    //查询学校对应的课程和校区
    public SchoolInstitutionDTO putSchoolInfo(SchoolInstitutionDTO schoolInstitutionDTO ,int providerId,String providerCode){
        List<SchoolCourseDO> listSchoolCourse = schoolCourseDAO.listSchoolCourse(providerId,providerCode,null,null,
                null, null,null,null, null);
        if (listSchoolCourse.size() > 0){
            List<SchoolCourseDTO> schoolCourseDTOS = new ArrayList<>();
            for (SchoolCourseDO sc : listSchoolCourse){
                SchoolCourseDTO schoolCourseDTO = mapper.map(sc,SchoolCourseDTO.class);
                schoolCourseDTOS.add(schoolCourseDTO);
            }
            schoolInstitutionDTO.setSchoolCourseDTOS(schoolCourseDTOS);
        }

        List<SchoolInstitutionLocationDO> listSchoolInstitutionLocation = schoolInstitutionLocationDAO.listSchoolInstitutionLocation(providerId,providerCode);
        if (listSchoolInstitutionLocation.size() > 0){
            List<SchoolInstitutionLocationDTO> schoolInstitutionLocationDTOS = new ArrayList<>();
            for (SchoolInstitutionLocationDO sil : listSchoolInstitutionLocation){
                SchoolInstitutionLocationDTO schoolInstitutionLocationDTO = mapper.map(sil,SchoolInstitutionLocationDTO.class);
                schoolInstitutionLocationDTOS.add(schoolInstitutionLocationDTO);
            }
            schoolInstitutionDTO.setSchoolInstitutionLocationDTOS(schoolInstitutionLocationDTOS);
        }
        return schoolInstitutionDTO;
    }

    //查询学校settting的合同、setting、历史setting
    public void putAttachmentsSettingInfo(SchoolInstitutionDTO schoolInstitutionDTO){
        //查询合同和备注
        List<SchoolAttachmentsDO> schoolAttachmentslist = schoolAttachmentsDao.listByProviderId(schoolInstitutionDTO.getId());
        if (schoolAttachmentslist != null && schoolAttachmentslist.size() > 0)
            schoolInstitutionDTO.setSchoolAttachments(mapper.map(schoolAttachmentslist.get(0),SchoolAttachmentsDTO.class));

        //查询历史Setting
        List<SchoolSettingNewDO> historySchoolSettingS = schoolSettingNewDAO.list(schoolInstitutionDTO.getId(),true);
        //List<SchoolSettingNewDTO> historySchoolSettingDtos = new ArrayList<>();
        for (SchoolSettingNewDO ssdo : historySchoolSettingS){
            SchoolSettingNewDTO schoolSettingNewDTO = mapper.map(ssdo,SchoolSettingNewDTO.class);
            //if (ssdo.getCourseId() > 0){
            //    SchoolCourseDO courseDO = schoolCourseDAO.schoolCourseById(ssdo.getCourseId());
            //    schoolSettingNewDTO.setCourseName(courseDO != null ? courseDO.getCourseName() : null);
            //}
            schoolInstitutionDTO.getHistorySchoolSettingList().add(schoolSettingNewDTO);
        }

        //查询Setting
		List<SchoolSettingNewDO> schoolSettingS = schoolSettingNewDAO.list(schoolInstitutionDTO.getId(), false);
		if (schoolSettingS != null)
			schoolSettingS.forEach(ssdo -> {
				SchoolSettingNewDTO schoolSettingNewDTO = mapper.map(ssdo, SchoolSettingNewDTO.class);
				if (ssdo.getCourseId() != null && ssdo.getCourseId() > 0) {
					SchoolCourseDO courseDO = schoolCourseDAO.schoolCourseById(ssdo.getCourseId());
					schoolSettingNewDTO.setCourseName(courseDO != null ? courseDO.getCourseName() : null);
				}
				schoolInstitutionDTO.getSchoolSettingList().add(schoolSettingNewDTO);
			});

    }

    @Deprecated
    private void schoolSetting1(int providerId, String courseLevel, Integer courseId,Integer level, Date startDate, Date endDate, String parameters) {
        List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
        for(CommissionOrderListDO co : list) {
            SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(), co.getGmtCreate());
            if (schoolSettingNewDO != null){
                parameters = schoolSettingNewDO.getParameters();
                double fee = co.getAmount();//本次收款
                co.setCommission(fee * (Double.parseDouble(parameters.trim()) * 0.01));//佣金
                System.out.println(co.getId() + "学校设置计算:本次收款金额[" + fee + "]*设置参数[" + parameters + "]*0.01=" + co.getCommission());
                updateGST(co,
						schoolSettingNewDO.getRegisterFee() != null ? schoolSettingNewDO.getRegisterFee().doubleValue()
								: 0.00,
						schoolSettingNewDO.getBookFee() != null ? schoolSettingNewDO.getBookFee().doubleValue() : 0.00);
                commissionOrderDao.updateCommissionOrder(co);
            }
        }
    }

    private void schoolSetting1(CommissionOrderListDO co, SchoolSettingNewDO schoolSettingNewDO) {
        //List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
        //for(CommissionOrderListDO co : list) {
        //   SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(), co.getGmtCreate());
            if (schoolSettingNewDO != null){
                String parameters = schoolSettingNewDO.getParameters();
                double fee = co.getAmount();//本次收款
                co.setCommission(fee * (Double.parseDouble(parameters.trim()) * 0.01));//佣金
				System.out.println(
						co.getId() + "学校设置计算:本次收款金额[" + fee + "]*设置参数[" + parameters + "]*0.01=" + co.getCommission());
				updateGST(co,
						schoolSettingNewDO.getRegisterFee() != null ? schoolSettingNewDO.getRegisterFee().doubleValue()
								: 0.00,
						schoolSettingNewDO.getBookFee() != null ? schoolSettingNewDO.getBookFee().doubleValue() : 0.00);
				commissionOrderDao.updateCommissionOrder(co);
            }
        //}
    }

    @Deprecated
    private void schoolSetting2(int providerId, String courseLevel,Integer courseId,Integer level,Date startDate, Date endDate, String parameters,
                                double registerFee, double bookFee) {
        if (StringUtil.isEmpty(parameters))
            return;
        String[] _parameters = parameters.split("[|]");
        if (_parameters.length == 1) {
            schoolSetting1(providerId, courseLevel , courseId,level, startDate, endDate, _parameters[0]);
            return;
        }
        double proportion = Double.parseDouble(_parameters[0].trim());
        List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);

        for (int i = 1; i < _parameters.length; i++) {
            String[] _parameter = _parameters[i].split("/");
            if (_parameter.length == 3) {
                int min = Integer.parseInt(_parameter[1].trim());
                int max = Integer.parseInt(_parameter[2].trim());
                if (list.size() >= min && list.size() <= max) {
                    double _fee = Double.parseDouble(_parameter[0]);
                    list.forEach(co -> {
                        SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(), co.getGmtCreate());
                        if (schoolSettingNewDO == null || schoolSettingNewDO.getLevel() == level) {
                            double fee = co.getAmount();
                            co.setCommission(fee * (proportion * 0.01) + _fee);
                            // System.out.print(bs.getId() + " : " + fee + " * ( 1 -
                            // " + proportion + " * 0.01 ) + " + _fee + " = " +
                            // bs.getCommission());
                            System.out.println(co.getId() + "学校设置计算=本次收款金额[" + fee + "]*(设置比例[" + proportion + "]*0.01)+设置金额[" + _fee
                                    + "]=" + co.getCommission());
                            updateGST(co, registerFee, bookFee);
                            commissionOrderDao.updateCommissionOrder(co);
                        }
                    });
                    break;
                }
            }
        }
    }

    private void schoolSetting2(CommissionOrderListDO co, SchoolSettingNewDO schoolSettingNewDO ,int size) {
        if (schoolSettingNewDO == null)
            return;
        String parameters = schoolSettingNewDO.getParameters();
        if (StringUtil.isEmpty(parameters))
            return;
        String[] _parameters = parameters.split("[|]");
        if (_parameters.length == 1) {
            schoolSetting1(co , schoolSettingNewDO);
            return;
        }
        double proportion = Double.parseDouble(_parameters[0].trim());
        //List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);

        for (int i = 1; i < _parameters.length; i++) {
            String[] _parameter = _parameters[i].split("/");
            if (_parameter.length == 3) {
                int min = Integer.parseInt(_parameter[1].trim());
                int max = Integer.parseInt(_parameter[2].trim());
                if (size >= min && size <= max) {
                    double _fee = Double.parseDouble(_parameter[0]);
                    //list.forEach(co -> {
                    //   SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(), co.getGmtCreate());
                            double fee = co.getAmount();
                            co.setCommission(fee * (proportion * 0.01) + _fee);
                            // System.out.print(bs.getId() + " : " + fee + " * ( 1 -
                            // " + proportion + " * 0.01 ) + " + _fee + " = " +
                            // bs.getCommission());
                            System.out.println(co.getId() + "学校设置计算=本次收款金额[" + fee + "]*(设置比例[" + proportion + "]*0.01)+设置金额[" + _fee
                                    + "]=" + co.getCommission());
                            updateGST(co,
            						schoolSettingNewDO.getRegisterFee() != null ? schoolSettingNewDO.getRegisterFee().doubleValue()
            								: 0.00,
            						schoolSettingNewDO.getBookFee() != null ? schoolSettingNewDO.getBookFee().doubleValue() : 0.00);
                            commissionOrderDao.updateCommissionOrder(co);

                    //});
                    break;
                }
            }
        }
    }

    @Deprecated
    private void schoolSetting4(int providerId, String courseLevel,Integer courseId,Integer level, Date startDate, Date endDate, String parameters, double registerFee, double bookFee) {
        if (StringUtil.isEmpty(parameters))
            return;
        String[] _parameters = parameters.split("[|]");
        if (_parameters.length == 1)
            return;
        List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
        for (int i = 1; i < _parameters.length; i++) {
            String[] _parameter = _parameters[i].split("/");
            if (_parameter.length == 2) {
                int number = Integer.parseInt(_parameter[1].trim());
                if (list.size() >= number) {
                    double proportion = Double.parseDouble(_parameter[0].trim());
                    list.forEach(co -> {
                        SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(), co.getGmtCreate());
                        if (schoolSettingNewDO == null || schoolSettingNewDO.getLevel() == level) {
                            double fee = co.getAmount();
                            co.setCommission(fee * (proportion * 0.01));
                            System.out.println(co.getId() + "学校设置计算=本次收款金额[" + fee + "]*(设置比例[" + proportion + "]*0.01)="
                                    + co.getCommission());
                            updateGST(co, registerFee, bookFee);
                            commissionOrderDao.updateCommissionOrder(co);
                        }
                    });
                    break;
                }
            }
        }
    }

    private void schoolSetting4(CommissionOrderListDO co, SchoolSettingNewDO schoolSettingNewDO ,int size) {
        if (schoolSettingNewDO == null )
            return;
        String parameters = schoolSettingNewDO.getParameters();
        if (StringUtil.isEmpty(parameters))
            return;
        String[] _parameters = parameters.split("[|]");
        if (_parameters.length == 1)
            return;
        //List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
        for (int i = 1; i < _parameters.length; i++) {
            String[] _parameter = _parameters[i].split("/");
            if (_parameter.length == 2) {
                int number = Integer.parseInt(_parameter[1].trim());
                if (size >= number) {
                    double proportion = Double.parseDouble(_parameter[0].trim());
                    //list.forEach(co -> {
                        //SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(), co.getGmtCreate());
                            double fee = co.getAmount();
                            co.setCommission(fee * (proportion * 0.01));
                            System.out.println(co.getId() + "学校设置计算=本次收款金额[" + fee + "]*(设置比例[" + proportion + "]*0.01)="
                                    + co.getCommission());
                            updateGST(co,
            						schoolSettingNewDO.getRegisterFee() != null ? schoolSettingNewDO.getRegisterFee().doubleValue()
            								: 0.00,
            						schoolSettingNewDO.getBookFee() != null ? schoolSettingNewDO.getBookFee().doubleValue() : 0.00);
                            commissionOrderDao.updateCommissionOrder(co);
                    //});
                    break;
                }
            }
        }
    }

    @Deprecated
    private void schoolSetting7(int providerId, String courseLevel,Integer courseId, Integer level,Date startDate, Date endDate, String parameters,
                                double registerFee, double bookFee) {
        List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
        list.forEach(co -> {
//			SubjectSettingDO subjectSettingDo = subjectSettingDao.get(schoolSetting.getId(),
//					schoolSetting.getSchoolName());
//			if (subjectSettingDo != null) {
//				co.setCommission(subjectSettingDo.getPrice());
//				System.out.println(
//						co.getId() + "学校设置计算=学校设置金额[" + subjectSettingDo.getPrice() + "]=" + co.getCommission());
            SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(), co.getGmtCreate());
            if (schoolSettingNewDO == null || schoolSettingNewDO.getLevel() == level) {
                if (parameters != null) {
                    co.setCommission(co.getAmount() - Double.parseDouble(parameters.trim()));
                    System.out.println(co.getId() + "学校设置计算=本次收款金额[" + co.getAmount() + "]-学校设置金额[" + Double.parseDouble(parameters.trim()) + "]="
                            + co.getCommission());
                } else {
                    co.setCommission(co.getAmount()); // 正常情况下是不会执行到这里的
                    System.out.println(co.getId() + "学校设置计算=本次收款金额[" + co.getAmount() + "]=" + co.getCommission());
                }
                updateGST(co, registerFee, bookFee);
                commissionOrderDao.updateCommissionOrder(co);
            }
        });
    }

    private void schoolSetting7(CommissionOrderListDO co, SchoolSettingNewDO schoolSettingNewDO ) {

        //List<CommissionOrderListDO> list = commissionOrderDao.listCommissionOrderByCourse(providerId, courseLevel , courseId,startDate, endDate);
        //list.forEach(co -> {
//			SubjectSettingDO subjectSettingDo = subjectSettingDao.get(schoolSetting.getId(),
//					schoolSetting.getSchoolName());
//			if (subjectSettingDo != null) {
//				co.setCommission(subjectSettingDo.getPrice());
//				System.out.println(
//						co.getId() + "学校设置计算=学校设置金额[" + subjectSettingDo.getPrice() + "]=" + co.getCommission());
            //SchoolSettingNewDO schoolSettingNewDO = returnSetting(co.getCourseId(), co.getGmtCreate());
            if (schoolSettingNewDO != null) {
                String parameters = schoolSettingNewDO.getParameters();
                if (parameters != null) {
                    co.setCommission(co.getAmount() - Double.parseDouble(parameters.trim()));
                    System.out.println(co.getId() + "学校设置计算=本次收款金额[" + co.getAmount() + "]-学校设置金额[" + Double.parseDouble(parameters.trim()) + "]="
                            + co.getCommission());
                } else {
                    co.setCommission(co.getAmount()); // 正常情况下是不会执行到这里的
                    System.out.println(co.getId() + "学校设置计算=本次收款金额[" + co.getAmount() + "]=" + co.getCommission());
                }
                updateGST(co,
						schoolSettingNewDO.getRegisterFee() != null ? schoolSettingNewDO.getRegisterFee().doubleValue()
								: 0.00,
						schoolSettingNewDO.getBookFee() != null ? schoolSettingNewDO.getBookFee().doubleValue() : 0.00);
                commissionOrderDao.updateCommissionOrder(co);
            }
        //});
    }

    private void updateGST(CommissionOrderListDO co , double registerFee, double bookFee) {
        SubagencyDO subagencyDo = subagencyDao.getSubagencyById(co.getSubagencyId());
        // setExpectAmount 预收业绩
        if (subagencyDo != null) {
            if ("AU".equals(subagencyDo.getCountry())) {
                co.setExpectAmount(co.getCommission() * subagencyDo.getCommissionRate() * 1.1 + registerFee + bookFee);//预收业绩
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
        // setGST DeductGst
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
        // setBonus 月奖
        if (!BaseCommissionOrderController.CommissionStateEnum.YJY.toString().equalsIgnoreCase(co.getCommissionState())) {
            co.setBonus(co.getDeductGst() * 0.1);
            System.out.println(co.getId() + "月奖=DeductGST[" + co.getDeductGst() + "]*0.1=" + co.getBonus());
        }
    }
}

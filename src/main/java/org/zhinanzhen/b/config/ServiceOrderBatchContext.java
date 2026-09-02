package org.zhinanzhen.b.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zhinanzhen.b.dao.pojo.*;
import org.zhinanzhen.b.dao.pojo.customer.CustomerInformationDO;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.RegionDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrderBatchContext {
    public Map<Integer, SchoolDO> schoolMap = new HashMap<>();
    public Map<Integer, SubagencyDO> subagencyMap = new HashMap<>();
    public Map<Integer, ServiceDO> serviceMap = new HashMap<>();
    public Map<Integer, ServicePackageDO> servicePackageMap = new HashMap<>();
    public Map<Integer, ReceiveTypeDO> receiveTypeMap = new HashMap<>();
    public Map<Integer, UserDO> userMap = new HashMap<>();
    public Map<Integer, ApplicantDO> applicantMap = new HashMap<>();
    public Map<Integer, MaraDO> maraMap = new HashMap<>();
    public Map<Integer, AdviserDO> adviserMap = new HashMap<>();
    public Map<Integer, OfficialDO> officialMap = new HashMap<>();
    public Map<Integer, RegionDO> regionMap = new HashMap<>();
    public Map<Integer, ServiceAssessDO> serviceAssessMap = new HashMap<>();
    public Map<Integer, OfficialTagDO> officialTagMap = new HashMap<>();
    public Map<Integer, List<VisaDO>> visaMap = new HashMap<>();
    public Map<Integer, List<ServiceOrderDO>> childrenOrderMap = new HashMap<>();
    public Map<Integer, CommissionOrderTempDO> commissionOrderTempMap = new HashMap<>();
    public Map<Integer, CustomerInformationDO> customerInformationMap = new HashMap<>();
    public Map<Integer, List<WebLogDTO>> webLogMap = new HashMap<>();
    public Map<Integer, List<ServiceOrderApplicantDO>> serviceOrderApplicantMap = new HashMap<>();
    public Map<Integer, Integer> oldOfficialMap = new HashMap<>();
    public Map<Integer, ServicePackagePriceDO> servicePackagePriceMap = new HashMap<>();
    public Map<Integer, Double> bindingOrderReceivableMap = new HashMap<>();
    public Map<Integer, ServiceOrderAndManage> serviceOrderManageMap = new HashMap<>();
    public Map<Integer, SchoolCourseDO> schoolCourseMap = new HashMap<>();
    public Map<Integer, SchoolInstitutionDO> schoolInstitutionMap = new HashMap<>();
    public Map<Integer, SchoolInstitutionLocationDO> schoolInstitutionLocationMap = new HashMap<>();
    public Map<Integer, List<ReviewAIDO>> reviewAIMap = new HashMap<>();
    public Map<Integer, List<MailRemindDO>> mailRemindMap = new HashMap<>();
}

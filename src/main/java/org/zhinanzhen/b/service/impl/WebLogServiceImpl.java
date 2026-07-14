package org.zhinanzhen.b.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.zhinanzhen.b.dao.OfficialDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.ServiceOrderOriginallyDAO;
import org.zhinanzhen.b.dao.pojo.OfficialDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.b.dao.WebLogDAO;
import org.zhinanzhen.b.service.WebLogService;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.AdviserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;
import org.zhinanzhen.tb.dao.pojo.AdviserDO;
import org.zhinanzhen.tb.dao.pojo.ServiceOrderOriginallyDO;
import org.zhinanzhen.tb.dao.pojo.UserDO;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("WebLogService")
public class WebLogServiceImpl implements WebLogService {

    @Resource
    private WebLogDAO webLogDAO;

    @Resource
    private AdviserDAO adviserDAO;

    @Resource
    private AdminUserDAO adminUserDAO;

    @Resource
    private ServiceOrderOriginallyDAO serviceOrderOriginallyDAO;

    @Resource
    private OfficialDAO officialDAO;

    @Resource
    private ServiceOrderDAO serviceOrderDAO;

    @Override
    public List<WebLogDTO> listByServiceOrderId(Integer serviceOrderId, Integer commissionOrderId,
                                                Integer visaId, Integer visaOfficialId, Integer schoolId, Integer userId,
                                                Integer isLogin, Integer operatedUser, Integer offset, Integer rows) {
        try {
            String login = "";
            if (isLogin != null && isLogin == 1) {
                login = "login";
            }
            List<WebLogDTO> webLogDTOS = webLogDAO.listWebLogs(serviceOrderId, commissionOrderId, visaId,
                    visaOfficialId, schoolId, userId, login, operatedUser, offset, rows);
            String userName = "";
            for (int i = 0; i < webLogDTOS.size(); i++) {
                userName = "";
                WebLogDTO webLogDTO = webLogDTOS.get(i);
                String uri = webLogDTO.getUri();
                String[] split = uri.split("/");
                String originalDateFormat = "yyyy-MM-dd HH:mm:ss";
                String targetDateFormat = "dd/MM/yyyy HH:mm:ss";
                SimpleDateFormat originalFormat = new SimpleDateFormat(originalDateFormat);
                SimpleDateFormat targetFormat = new SimpleDateFormat(targetDateFormat);
                Date parse = originalFormat.parse(webLogDTO.getStartTime());
                String startTime = targetFormat.format(parse);
                if ("serviceOrder".equalsIgnoreCase(split[2]) || "serviceOrderManage".equalsIgnoreCase(split[2])) {
                    List<String> serviceOrderOriginallyDOList = new ArrayList<>();
//                    String[] split = webLogDTO.getUri().split("/");
                    String parameter = webLogDTO.getParameter();
                    String standardJson = convertServiceOrderDO(parameter);
                    // 使用ObjectMapper将其解析为类
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    ServiceOrderDO serviceOrderDO = new ServiceOrderDO();
                    if (standardJson != null) {
                        serviceOrderDO = objectMapper.readValue(standardJson, ServiceOrderDO.class);
                    }
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        userName = adminUserById.getUsername();
                    }
                    if ("add".equalsIgnoreCase(split[split.length - 1])) {
                        serviceOrderOriginallyDOList.add(startTime + "    " +  webLogDTO.getRole() + ":" + userName + "    添加了服务订单" + "    " + "操作人" + ":" + userName);
                        webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                    }
                    if ("next_flow".equalsIgnoreCase(split[split.length - 1])) {
                        String operationDescription = buildOperationDescription(serviceOrderDO.getState());
                        serviceOrderOriginallyDOList.add(startTime + "    " +  webLogDTO.getRole() + ":"  + userName + "    " + operationDescription + "    " + "操作人" + ":" + userName);
                        webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                    }
                    if ("adviserDataMigration".equalsIgnoreCase(split[split.length - 1])) {
                        List<ServiceOrderOriginallyDO> serviceOrderOriginallyDOS = serviceOrderOriginallyDAO.listServiceOrderOriginallyDO(webLogDTO.getServiceOrderId(), webLogDTO.getId(), null);
                        for (ServiceOrderOriginallyDO serviceOrderOriginallyDO : serviceOrderOriginallyDOS) {
                            if (serviceOrderOriginallyDO.getAdviserId() != null && serviceOrderOriginallyDO.getNewAdviserId() != null && serviceOrderOriginallyDO.getWebLogId() != null) {
                                AdviserDO adviserDOOld = adviserDAO.getAdviserById(serviceOrderOriginallyDO.getAdviserId());
                                AdviserDO adviserDONew = adviserDAO.getAdviserById(serviceOrderOriginallyDO.getNewAdviserId());
                                serviceOrderOriginallyDOList.add(startTime + "    顾问" + ":"  + adviserDOOld.getName() + "    迁移数据给" + adviserDONew.getName() + "    操作人:" + userName);
                            }
                        }
                        webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                    }
                    if ("officialHandover".equalsIgnoreCase(split[split.length - 1])) {
                        List<ServiceOrderOriginallyDO> serviceOrderOriginallyDOS = serviceOrderOriginallyDAO.listServiceOrderOriginallyDO(webLogDTO.getServiceOrderId(), webLogDTO.getId(), null);
                        for (ServiceOrderOriginallyDO serviceOrderOriginallyDO : serviceOrderOriginallyDOS) {
                            if (serviceOrderOriginallyDO.getOfficialId() != null && serviceOrderOriginallyDO.getNewOfficialId() != null && serviceOrderOriginallyDO.getWebLogId() != null) {
                                OfficialDO officialDOOld = officialDAO.getOfficialById(serviceOrderOriginallyDO.getOfficialId());
                                OfficialDO officialDONew = officialDAO.getOfficialById(serviceOrderOriginallyDO.getNewOfficialId());
                                serviceOrderOriginallyDOList.add(startTime + "    文案" + ":"  + officialDOOld.getName() + "    迁移数据给" + officialDONew.getName() + "    操作人:" + userName);
                            }
                        }
                        webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                    }
                    if ("update".equalsIgnoreCase(split[split.length - 1])) {
                        List<ServiceOrderOriginallyDO> serviceOrderOriginallyDOS = serviceOrderOriginallyDAO.listServiceOrderOriginallyDO(webLogDTO.getServiceOrderId(), null, null);
                        if (serviceOrderOriginallyDOS != null && serviceOrderOriginallyDOS.size() > 0) {
                            for (ServiceOrderOriginallyDO serviceOrderOriginallyDO : serviceOrderOriginallyDOS) {
                                if (serviceOrderOriginallyDO.getOfficialId() != null && serviceOrderOriginallyDO.getNewOfficialId() != null && serviceOrderOriginallyDO.getWebLogId() == null) {
                                    Integer officialId = serviceOrderOriginallyDO.getOfficialId();
                                    OfficialDO officialDO = officialDAO.getOfficialById(officialId);
                                    OfficialDO officialById = officialDAO.getOfficialById(Integer.valueOf(JSONObject.parseObject(standardJson).get("officialId").toString()));
                                    serviceOrderOriginallyDOList.add(startTime + "    文案" + ":"  + officialDO.getName() + "    更换为" + "    文案" + ":"  + officialById.getName() + "    操作人:" + userName);
                                }
                            }
                        } else {
                            AdviserDO adviserDOOld = adviserDAO.getAdviserById(serviceOrderDO.getAdviserId());
                            if (adviserDOOld == null) {
                                continue;
                            }
                            serviceOrderOriginallyDOList.add(startTime + "    顾问" + ":"  + adviserDOOld.getName() + "    提交并修改订单" + "    操作人:" + userName);
                        }

                        webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                    }
//                    if (webLogDTO.getParameter() != null && webLogDTO.getParameter().contains("/uploads/")) {
//                        webLogDTOS.remove(i);
//                        continue;
//                    }
                    if ("finish".equalsIgnoreCase(split[split.length - 1])) {
                        serviceOrderOriginallyDOList.add(startTime + "    " +  webLogDTO.getRole() + ":"  + userName + "    " + "提交审核通过" + "    " + "操作人" + ":" + userName);
                        webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                        continue;
                    }
                }
                if ("user".equalsIgnoreCase(split[2])) {
                    List<String> serviceOrderOriginallyDOList = new ArrayList<>();
                    String parameter = webLogDTO.getParameter();
                    String standardJson = convertServiceOrderDO(parameter);
                    // 使用ObjectMapper将其解析为类
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    UserDO userDO = new UserDO();
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        userName = adminUserById.getUsername();
                    }
                    if (standardJson != null) {
                        userDO = objectMapper.readValue(standardJson, UserDO.class);
                    }
                    if ("add".equalsIgnoreCase(split[split.length - 1])) {
                        serviceOrderOriginallyDOList.add(startTime + "    " +  webLogDTO.getRole() + ":" + userName + "    创建客户" + "    " + "操作人" + ":" + userName);
                        webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                    }
                    if ("update".equalsIgnoreCase(split[split.length - 1]) || "adviserDataMigration".equalsIgnoreCase(split[split.length - 1])) {
                        List<ServiceOrderOriginallyDO> serviceOrderOriginallyDOS = serviceOrderOriginallyDAO.listServiceOrderOriginallyDO(null, webLogDTO.getId(), webLogDTO.getOperatedUser());
                        for (ServiceOrderOriginallyDO serviceOrderOriginallyDO : serviceOrderOriginallyDOS) {
                            if (serviceOrderOriginallyDO.getAdviserId() != null && serviceOrderOriginallyDO.getNewAdviserId() != null) {
                                Integer adviserId = serviceOrderOriginallyDO.getAdviserId();
                                AdviserDO adviserDO = adviserDAO.getAdviserById(adviserId);
//                                AdviserDO adviserById = adviserDAO.getAdviserById(Integer.valueOf(JSONObject.parseObject(standardJson).get("adviserId").toString()));
                                AdviserDO adviserById = adviserDAO.getAdviserById(serviceOrderOriginallyDO.getNewAdviserId());
                                serviceOrderOriginallyDOList.add(startTime + "    顾问" + ":"  + adviserDO.getName() + "    更换为" + "    顾问" + ":"  + adviserById.getName() + "    操作人:" + userName);
                            }
                            webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                        }
                    }
                }
                if ("schoolInstitution".equalsIgnoreCase(split[2])) {
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        userName = adminUserById.getUsername();
                    }
                    setOperationDescription(webLogDTO, startTime, userName,
                            buildSchoolInstitutionOperationDescription(split[split.length - 1]));
                }
                if ("schoolCourse".equalsIgnoreCase(split[2])) {
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        userName = adminUserById.getUsername();
                    }
                    setOperationDescription(webLogDTO, startTime, userName,
                            buildSchoolCourseOperationDescription(split[split.length - 1]));
                }
                if ("commissionOrder".equalsIgnoreCase(split[2])) {
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        userName = adminUserById.getUsername();
                    }
                    setOperationDescription(webLogDTO, startTime, userName,
                            buildCommissionOrderOperationDescription(split[split.length - 1]));
                }
                if ("visaOfficial".equalsIgnoreCase(split[2])) {
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        userName = adminUserById.getUsername();
                    }
                    setOperationDescription(webLogDTO, startTime, userName,
                            buildVisaOfficialOperationDescription(split[split.length - 1]));
                }
                if ("visa".equalsIgnoreCase(split[2])) {
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        userName = adminUserById.getUsername();
                    }
                    setOperationDescription(webLogDTO, startTime, userName,
                            buildVisaOperationDescription(split[split.length - 1]));
                }
                if ("login".equalsIgnoreCase(split[3])) {
                    List<String> serviceOrderOriginallyDOList = new ArrayList<>();
                    serviceOrderOriginallyDOList.add(startTime + "    " +  webLogDTO.getRole() + ":" + userName + "    " + "登录");
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        webLogDTO.setAdminUserDO(adminUserById);
                    }
                    webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                }
                if ("out".equalsIgnoreCase(split[3])) {
                    List<String> serviceOrderOriginallyDOList = new ArrayList<>();
                    serviceOrderOriginallyDOList.add(startTime + "    " +  webLogDTO.getRole() + ":" + userName + "    " + "登出");
                    AdminUserDO adminUserById = adminUserDAO.getAdminUserById(webLogDTO.getUserId());
                    if (adminUserById != null) {
                        webLogDTO.setAdminUserDO(adminUserById);
                    }
                    webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                }
            }
            return webLogDTOS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Integer count(Integer serviceOrderId, Integer commissionOrderId, Integer visaId, Integer visaOfficialId,
                         Integer schoolId, Integer userId, Integer isLogin, Integer operatedUser) {
        String login = "";
        if (isLogin != null && isLogin == 1) {
            login = "login";
        }
        return webLogDAO.count(serviceOrderId, commissionOrderId, visaId, visaOfficialId, schoolId, userId, login,
                operatedUser);
    }

    @Override
    public int addWebLogs(WebLogDTO webLog) {
        return webLogDAO.addWebLogs(webLog);
    }

    private void setOperationDescription(WebLogDTO webLogDTO, String startTime, String userName, String operationDescription) {
        if (operationDescription == null || operationDescription.length() == 0) {
            return;
        }
        List<String> operationDescriptionList = new ArrayList<>();
        operationDescriptionList.add(startTime + "    " + webLogDTO.getRole() + ":" + userName
                + "    " + operationDescription + "    " + "操作人" + ":" + userName);
        webLogDTO.setOperationDescription(operationDescriptionList);
    }

    private String buildSchoolInstitutionOperationDescription(String methodName) {
        switch (methodName) {
            case "upload_contract_file":
                return "上传学校合同文件";
            case "updateSchoolAttachments":
                return "更新学校附件";
            case "deleteSchoolAttachments":
                return "删除学校附件";
            case "add":
                return "添加学校";
            case "update":
                return "修改学校";
            case "delete":
                return "删除学校";
            case "addSetting1":
                return "添加学校佣金规则";
            case "addSetting2":
                return "添加学校佣金规则";
            case "addSetting4":
                return "添加学校佣金规则";
            case "addSetting7":
                return "添加学校佣金规则";
            case "updateSetting":
                return "修改学校佣金规则";
            case "deleteSetting":
                return "删除学校佣金规则";
            case "addComment":
                return "添加学校评论";
            case "deleteComment":
                return "删除学校评论";
            default:
                return "";
        }
    }

    private String buildSchoolCourseOperationDescription(String methodName) {
        switch (methodName) {
            case "add":
                return "添加学校课程";
            case "update":
                return "修改学校课程";
            case "delete":
                return "删除学校课程";
            default:
                return "";
        }
    }

    private String buildCommissionOrderOperationDescription(String methodName) {
        switch (methodName) {
            case "add":
                return "创建留学佣金订单";
            case "addCommissionOrderTemp":
                return "创建留学佣金订单暂存信息";
            case "update":
                return "修改留学佣金订单";
            case "kjUpdate":
                return "修改留学佣金财务信息";
            case "updateKjApprovalDate":
                return "修改留学佣金财务审核时间";
            case "updateCommission":
                return "重新计算留学佣金";
            case "close":
                return "关闭留学佣金订单";
            case "approval":
                return "审核留学佣金订单";
            case "refuse":
                return "驳回留学佣金订单";
            case "addComment":
                return "添加留学佣金订单评论";
            case "deleteComment":
                return "删除留学佣金订单评论";
            case "deleteCommissionOrder":
                return "删除留学佣金订单";
            case "updateInfo":
                return "批量修改留学佣金订单";
            case "updateSubmitted":
            case "updateSubmitted22":
                return "提交留学佣金订单";
            case "upload":
                return "导入留学佣金订单";
            case "upload_img":
                return "上传留学佣金付款凭证";
            case "delete_visa_upload_img":
                return "删除留学佣金附件";
            default:
                return "操作留学佣金订单";
        }
    }

    private String buildVisaOfficialOperationDescription(String methodName) {
        switch (methodName) {
            case "add":
                return "创建文案佣金订单";
            case "updateOfficialVisa":
                return "修改文案佣金订单";
            default:
                return "操作文案佣金订单";
        }
    }

    private String buildVisaOperationDescription(String methodName) {
        switch (methodName) {
            case "add":
                return "创建签证佣金订单";
            case "update":
                return "修改签证佣金订单";
            case "kjUpdate":
                return "修改签证佣金财务信息";
            case "updateKjApprovalDate":
                return "修改签证佣金财务审核时间";
            case "close":
                return "关闭签证佣金订单";
            case "reopen":
                return "重新打开签证佣金订单";
            case "delete":
                return "删除签证佣金订单";
            case "approval":
                return "审核签证佣金订单";
            case "refuse":
                return "驳回签证佣金订单";
            case "addComment":
                return "添加签证佣金订单评论";
            case "deleteComment":
                return "删除签证佣金订单评论";
            default:
                return "操作签证佣金订单";
        }
    }

    public String buildOperationDescription(String state) {
        String operationDescription = "";
        switch (state) {
            case "PENDING":
                operationDescription = "撤回订单";
                break;
            case "REVIEW":
                operationDescription = "提交审核订单";
                break;
            case "OREVIEW":
                operationDescription = "进行资料审核";
                break;
            case "WAIT":
                operationDescription = "提交mara审核";
                break;
            case "FINISH":
                operationDescription = "审核通过";
                break;
            case "APPLY":
                operationDescription = "提交移民局申请";
                break;
            case "COMPLETE":
                operationDescription = "申请成功";
                break;
            case "CLOSE":
                operationDescription = "关闭申请";
                break;
            case "PAID":
                operationDescription = "COE已下";
                break;
            default:
                operationDescription = "";
        }
        return operationDescription;
    }

    public String convertServiceOrderDO(String parameter) throws IOException {
        String cleanedInput = "";
        // 移除方括号和每个键值对周围的额外大括号，然后用逗号连接它们
        // 注意：这个处理假设输入格式非常固定，且每个键值对之间只有一个空格和逗号
        if (parameter != null) {
            cleanedInput = parameter.replace("[", "").replace("]", "").replaceAll("\\}\\s*,\\s*\\{", ",").trim();
        } else {
            return null;
        }

        // 由于键值对之间现在用逗号分隔，但每个键值对仍然是“key=value”的形式，
        // 我们需要将其转换为标准的JSON格式，即“"key":"value"”
        StringBuilder sb = new StringBuilder("{");
        String[] keyValuePairs = cleanedInput.split(",");
        for (int i = 0; i < keyValuePairs.length; i++) {
            String keyValue = keyValuePairs[i].trim().replaceFirst("^\\s*\\{", "").replaceFirst("\\}\\s*$", "");
            String[] keyValueSplit = keyValue.split("=");
            if (keyValueSplit.length == 2) {
                String key = keyValueSplit[0].trim();
                String value = keyValueSplit[1].trim();

                // 对于像serviceOrderApplicantList这样的数组值，我们需要特别处理，
                // 因为它们看起来像是已经格式化的JSON数组。我们可以尝试直接将其作为字符串保留。
                // 注意：这里我们假设所有看起来像JSON数组的字符串都已经是正确的格式。
                if (value.startsWith("[") && value.endsWith("]")) {
                    // 对于数组值，我们不需要额外的引号
                    sb.append("\"").append(key).append("\":").append(value);
                } else {
                    // 对于其他值，我们需要将其放在双引号中
                    sb.append("\"").append(key).append("\":\"").append(value.replace("\"", "\\\"")).append("\"");
                }

                // 添加逗号（除了最后一个键值对之外）
                sb.append(",");
            }
        }
        sb = new StringBuilder(sb.substring(0, sb.length() - 1));
        sb.append("}");

        // 现在我们有了标准的JSON字符串
        return sb.toString();
    }
}

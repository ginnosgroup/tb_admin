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

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
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
    public List<WebLogDTO> listByServiceOrderId(Integer serviceOrderId, Integer offset, Integer rows) {
        try {
            List<WebLogDTO> webLogDTOS = webLogDAO.listWebLogs(serviceOrderId, offset, rows);
            for (WebLogDTO webLogDTO : webLogDTOS) {
                String userName = "";
                List<String> serviceOrderOriginallyDOList = new ArrayList<>();
                String[] split = webLogDTO.getUri().split("/");
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
                    serviceOrderOriginallyDOList.add(webLogDTO.getStartTime() + "  " +  webLogDTO.getRole() + ":" + userName + "添加了服务订单" + "  " + "操作人" + ":" + userName);
                    webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                }
                if ("next_flow".equalsIgnoreCase(split[split.length - 1])) {
                    String operationDescription = buildOperationDescription(serviceOrderDO.getState());
                    serviceOrderOriginallyDOList.add(webLogDTO.getStartTime() + "  " +  webLogDTO.getRole() + userName + operationDescription);
                    webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                }
                if ("adviserDataMigration".equalsIgnoreCase(split[split.length - 1])) {
                    List<ServiceOrderOriginallyDO> serviceOrderOriginallyDOS = serviceOrderOriginallyDAO.listServiceOrderOriginallyDO(webLogDTO.getServiceOrderId(), webLogDTO.getId());
                    for (ServiceOrderOriginallyDO serviceOrderOriginallyDO : serviceOrderOriginallyDOS) {
                        if (serviceOrderOriginallyDO.getAdviserId() != null && serviceOrderOriginallyDO.getNewAdviserId() != null && serviceOrderOriginallyDO.getWebLogId() != null) {
                            AdviserDO adviserDOOld = adviserDAO.getAdviserById(serviceOrderOriginallyDO.getAdviserId());
                            AdviserDO adviserDONew = adviserDAO.getAdviserById(serviceOrderOriginallyDO.getNewAdviserId());
                            serviceOrderOriginallyDOList.add(webLogDTO.getStartTime() + "  顾问" + adviserDOOld.getName() + "迁移数据给" + adviserDONew.getName() + "  操作人" + userName);
                        }
                    }
                    webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                }
                if ("officialHandover".equalsIgnoreCase(split[split.length - 1])) {
                    List<ServiceOrderOriginallyDO> serviceOrderOriginallyDOS = serviceOrderOriginallyDAO.listServiceOrderOriginallyDO(webLogDTO.getServiceOrderId(), webLogDTO.getId());
                    for (ServiceOrderOriginallyDO serviceOrderOriginallyDO : serviceOrderOriginallyDOS) {
                        if (serviceOrderOriginallyDO.getOfficialId() != null && serviceOrderOriginallyDO.getNewOfficialId() != null && serviceOrderOriginallyDO.getWebLogId() != null) {
                            OfficialDO officialDOOld = officialDAO.getOfficialById(serviceOrderOriginallyDO.getOfficialId());
                            OfficialDO officialDONew = officialDAO.getOfficialById(serviceOrderOriginallyDO.getNewOfficialId());
                            serviceOrderOriginallyDOList.add(webLogDTO.getStartTime() + "  文案" + officialDOOld.getName() + "迁移数据给" + officialDONew.getName() + "  操作人" + userName);
                        }
                    }
                    webLogDTO.setOperationDescription(serviceOrderOriginallyDOList);
                }
                if ("update".equalsIgnoreCase(split[split.length - 1])) {
                    List<ServiceOrderOriginallyDO> serviceOrderOriginallyDOS = serviceOrderOriginallyDAO.listServiceOrderOriginallyDO(webLogDTO.getServiceOrderId(), null);
                    for (ServiceOrderOriginallyDO serviceOrderOriginallyDO : serviceOrderOriginallyDOS) {
                        if (serviceOrderOriginallyDO.getOfficialId() != null && serviceOrderOriginallyDO.getNewOfficialId() != null && serviceOrderOriginallyDO.getWebLogId() == null) {
                            Integer officialId = serviceOrderOriginallyDO.getOfficialId();
                            OfficialDO officialDO = officialDAO.getOfficialById(officialId);
                            OfficialDO officialById = officialDAO.getOfficialById(Integer.valueOf(JSONObject.parseObject(standardJson).get("officialId").toString()));
                            serviceOrderOriginallyDOList.add(webLogDTO.getStartTime() + "  文案" + officialDO.getName() + "更换为" + "  文案" + officialById.getName() + "  操作人" + userName);
                        }
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
    public Integer count(Integer serviceOrderId) {
        return webLogDAO.count(serviceOrderId);
    }

    @Override
    public int addWebLogs(WebLogDTO webLog) {
        return webLogDAO.addWebLogs(webLog);
    }

    public String buildOperationDescription(String state) {
        String operationDescription = "";
        switch (state) {
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

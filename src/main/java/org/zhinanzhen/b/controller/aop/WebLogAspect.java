package org.zhinanzhen.b.controller.aop;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zhinanzhen.b.dao.CommissionOrderCommentDAO;
import org.zhinanzhen.b.dao.CommissionOrderDAO;
import org.zhinanzhen.b.dao.SchoolCourseDAO;
import org.zhinanzhen.b.dao.SchoolInstitutionCommentDAO;
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.ServiceOrderManageDAO;
import org.zhinanzhen.b.dao.SchoolSettingNewDAO;
import org.zhinanzhen.b.dao.VisaCommentDAO;
import org.zhinanzhen.b.dao.WebLogDAO;
import org.zhinanzhen.b.dao.pojo.CommissionOrderDO;
import org.zhinanzhen.b.dao.pojo.SchoolCourseDO;
import org.zhinanzhen.b.dao.pojo.SchoolInstitutionCommentDO;
import org.zhinanzhen.b.dao.pojo.SchoolSettingNewDO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 统一日志处理切面
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class WebLogAspect extends BaseController{

    @Autowired
    private WebLogDAO webLogDAO;

    @Autowired
    private AdminUserDAO adminUserDAO;

    @Autowired
    private ServiceOrderDAO serviceOrderDAO;
    @Autowired
    private ServiceOrderManageDAO serviceOrderManageDAO;
    @Autowired
    private SchoolCourseDAO schoolCourseDAO;
    @Autowired
    private SchoolSettingNewDAO schoolSettingNewDAO;
    @Autowired
    private SchoolInstitutionCommentDAO schoolInstitutionCommentDAO;
    @Autowired
    private CommissionOrderDAO commissionOrderDAO;
    @Autowired
    private CommissionOrderCommentDAO commissionOrderCommentDAO;
    @Autowired
    private VisaCommentDAO visaCommentDAO;

    //定义切点表达式,指定通知功能被应用的范围
//                "execution(public * org.zhinanzhen.b.controller.AdviserDataController.adviserDataMigration(..)) || " +
//                        "execution(public * org.zhinanzhen.b.controller.OfficialController.officialHandover(..)) || " +
//    "execution(public * org.zhinanzhen.tb.controller.UserController.update(..))"
    @Pointcut("execution(public * org.zhinanzhen.b.controller.ServiceOrderController.*(..)) || " +
            "execution(public * org.zhinanzhen.b.controller.ServiceOrderManageController.*(..)) || " +
            "execution(public * org.zhinanzhen.tb.controller.UserController.addUser(..)) || " +
            "execution(public * org.zhinanzhen.tb.controller.AdminUserController.login(..)) ||" +
            "execution(public * org.zhinanzhen.tb.controller.AdminUserController.outLogin(..)) ||" +
            "(execution(public * org.zhinanzhen.b.controller.SubagencyController.*(..)) && !execution(public * org.zhinanzhen.b.controller.SubagencyController.listSubagency(..))) ||" +
            "execution(public * org.zhinanzhen.tb.controller.AdviserController.addAdviser(..)) ||" +
            "execution(public * org.zhinanzhen.tb.controller.AdviserController.updateAdviser(..)) ||" +
            "execution(public * org.zhinanzhen.b.controller.OfficialController.addOfficial(..)) ||" +
            "execution(public * org.zhinanzhen.b.controller.OfficialController.updateOfficial(..)) ||" +
            "execution(public * org.zhinanzhen.b.controller.KjController.addKj(..)) ||" +
            "execution(public * org.zhinanzhen.b.controller.KjController.updateKj(..)) ||" +
            "(execution(public * org.zhinanzhen.b.controller.SchoolInstitutionController.*(..)) && " +
            "!execution(public * org.zhinanzhen.b.controller.SchoolInstitutionController.list(..)) && " +
            "!execution(public * org.zhinanzhen.b.controller.SchoolInstitutionController.get(..)) && " +
            "!execution(public * org.zhinanzhen.b.controller.SchoolInstitutionController.getTradingNamesById(..)) && " +
            "!execution(public * org.zhinanzhen.b.controller.SchoolInstitutionController.getSetting(..)) && " +
            "!execution(public * org.zhinanzhen.b.controller.SchoolInstitutionController.listComment(..))) ||" +
            "(execution(public * org.zhinanzhen.b.controller.SchoolCourseController.*(..)) && " +
            "!execution(public * org.zhinanzhen.b.controller.SchoolCourseController.list(..)) && " +
            "!execution(public * org.zhinanzhen.b.controller.SchoolCourseController.getCourseLevel(..)) && " +
            "!execution(public * org.zhinanzhen.b.controller.SchoolCourseController.getCourseToSetting(..))) || " +
            "execution(public * org.zhinanzhen.b.controller.CommissionOrderController.*(..)) || " +
            "execution(public * org.zhinanzhen.b.controller.VisaOfficialController.*(..)) || " +
            "execution(public * org.zhinanzhen.b.controller.VisaController.*(..))"
    )
    public void webLog() {

    }
 
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
    }
 
    /**value切入点位置
     * returning 自定义的变量，标识目标方法的返回值,自定义变量名必须和通知方法的形参一样
     * 特点：在目标方法之后执行的,能够获取到目标方法的返回值，可以根据这个返回值做不同的处理
     */     
    @AfterReturning(value = "webLog()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) throws Throwable {
        // 检查是否是登录方法
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        String methodName = method.getName();
        if ("login".equals(methodName)) {
            try {
                long startTime = System.currentTimeMillis();
                //获取当前请求对象
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = attributes.getRequest();

                AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
                AdminUserDO adminUserById = new AdminUserDO();
                if (adminUserLoginInfo != null) {
                    adminUserById = adminUserDAO.getAdminUserById(adminUserLoginInfo.getId());
                }
                //记录请求信息
                WebLogDTO webLog = new WebLogDTO();

                long endTime = System.currentTimeMillis();
                String urlStr = request.getRequestURL().toString();
                webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
                webLog.setIp(request.getRemoteUser());
                webLog.setUserId(adminUserById.getId());
                webLog.setMethod(request.getMethod());

                webLog.setSpendTime((int) (endTime - startTime));

                // 使用java.time包
                Instant instant = Instant.ofEpochMilli(startTime);
                ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault()); // 使用系统默认时区
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDate = dateTime.format(formatter);

                webLog.setStartTime(formattedDate);
                String requestURI = request.getRequestURI();
                webLog.setUri(requestURI);
                webLog.setUrl(request.getRequestURL().toString());
                String[] split = requestURI.split("/");
                String methodNameT = split[split.length - 1];
                List<Object> parameter = getParameter(method, joinPoint.getArgs());
                if (parameter != null && !methodNameT.contains("upload") && !methodNameT.equalsIgnoreCase("add")) {
                    for (Object o : parameter) {
                        if (o.toString().contains("id")) {
                            // 定义正则表达式来匹配id后面的数字
                            // 注意：这个正则表达式假设id后面紧跟着等号，然后是数字，且数字可能有多位
                            String regex = "\\{id=(\\d+)\\}";
                            // 创建Pattern对象
                            Pattern pattern = Pattern.compile(regex);
                            // 创建Matcher对象
                            Matcher matcher = pattern.matcher(o.toString());
                            // 查找匹配项
                            if (matcher.find()) {
                                // 提取匹配的数字
                                String id = matcher.group(1);
                                if (urlStr.contains("user")) {
                                    webLog.setOperatedUser(Integer.valueOf(id));
                                } else if (isServiceOrderUri(urlStr)) {
                                    webLog.setServiceOrderId(Integer.valueOf(id));
                                }
                                log.info("ID: " + id);
                            } else {
                                System.out.println("未找到ID");
                            }
                        }
                        if (o.toString().contains("serviceOrderId")) {
                            // 定义正则表达式来匹配id后面的数字
                            // 注意：这个正则表达式假设id后面紧跟着等号，然后是数字，且数字可能有多位
                            String regex = "\\{serviceOrderId=(\\d+)\\}";
                            // 创建Pattern对象
                            Pattern pattern = Pattern.compile(regex);
                            // 创建Matcher对象
                            Matcher matcher = pattern.matcher(o.toString());
                            // 查找匹配项
                            if (matcher.find()) {
                                // 提取匹配的数字
                                String id = matcher.group(1);
                                if (isServiceOrderUri(urlStr)) {
                                    webLog.setServiceOrderId(Integer.valueOf(id));
                                }
                                log.info("ID: " + id);
                            } else {
                                System.out.println("未找到ID");
                            }
                        }

                    }
                }
                if (parameter != null && methodName.equalsIgnoreCase("add")) {
                    Response<Integer> integerResponse = (Response) result;
                    if (urlStr.contains("serviceOrder")) {
                        webLog.setServiceOrderId(integerResponse.getData());
                    }
                    if (urlStr.contains("user")) {
                        webLog.setOperatedUser(integerResponse.getData());
                    }
                }
                webLog.setParameter(parameter.toString());
                String resultString = result.toString();
                if (resultString.length() >= 2000) {
                    webLog.setResult(resultString.substring(0, 1999));
                } else {
                    webLog.setResult(resultString);
                }


                if (adminUserLoginInfo == null) {
                    return;
                }
                String apList = adminUserLoginInfo.getApList();
                switch (apList) {
                    case "GW":
                        apList = "顾问";
                        break;
                    case "WA":
                        apList = "文案";
                        break;
                    case "KJ":
                        apList = "会计";
                        break;
                    case "SUPERAD":
                        apList = "超级管理员";
                        break;
                    default: apList = apList;
                }
                webLog.setRole(apList);


                if (!StringUtils.isEmpty(methodName)) {
                    if (!methodName.contains("list") && !methodName.contains("upload") && !methodName.contains("img") && !methodName.contains("count")){
                        int i = webLogDAO.addWebLogs(webLog);
                        if (i > 0) {
                            Integer serviceOrderId = webLog.getServiceOrderId();
                            if (serviceOrderId != null && serviceOrderId != 0) {
                                ServiceOrderDO serviceOrderById = serviceOrderDAO.getServiceOrderById(serviceOrderId);
                                if (serviceOrderById.getApplicantParentId() > 0 && !"OVST".equalsIgnoreCase(serviceOrderById.getType())) {
                                    ServiceOrderDO serviceOrderByParent = serviceOrderDAO.getServiceOrderById(serviceOrderById.getApplicantParentId());
                                    List<ServiceOrderDTO> deriveOrder = serviceOrderDAO.getDeriveOrder(serviceOrderByParent.getId());
                                    webLog.setServiceOrderId(serviceOrderByParent.getId());
                                    webLogDAO.addWebLogs(webLog);
                                    for (ServiceOrderDTO serviceOrderDO : deriveOrder) {
                                        if (serviceOrderDO.getId() == serviceOrderId) {
                                            continue;
                                        }
                                        webLog.setServiceOrderId(serviceOrderDO.getId());
                                        webLogDAO.addWebLogs(webLog);
                                    }
                                }
                            }
                        }
                    }
                }
                log.info("{}", JSONUtil.parse(webLog));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
 
    //通知包裹了目标方法，在目标方法调用之前和之后执行自定义的行为
    //ProceedingJoinPoint切入点可以获取切入点方法上的名字、参数、注解和对象
    @Around("webLog() && !execution(public * org.zhinanzhen.tb.controller.AdminUserController.login(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable 
    {
        try {
            long startTime = System.currentTimeMillis();
            Integer serviceorderId = 0;
            Integer operatedUser = 0;
            String contractData = "";
            //获取当前请求对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            AdminUserLoginInfo adminUserLoginInfo = getAdminUserLoginInfo(request);
            AdminUserDO adminUserById = new AdminUserDO();
            if (adminUserLoginInfo != null) {
                adminUserById = adminUserDAO.getAdminUserById(adminUserLoginInfo.getId());
            }
            //记录请求信息
            WebLogDTO webLog = new WebLogDTO();
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            String requestURI = request.getRequestURI();
            String[] split = requestURI.split("/");
            String methodName = getLastPathSegment(requestURI);
            String controllerName = getControllerName(methodSignature.getDeclaringType(), split);
            List<Object> parameter = getParameter(method, joinPoint.getArgs());
            Integer schoolId = getSchoolId(controllerName, methodName, parameter, null);
            List<Integer> relatedServiceOrderIds = new ArrayList<>();
            collectIntegerValues(parameter, "serviceOrderId", relatedServiceOrderIds);
            collectIntegerValues(parameter, "serviceorderid", relatedServiceOrderIds);
            addUnique(relatedServiceOrderIds, getIntegerValue(request.getParameter("serviceOrderId")));
            addUnique(relatedServiceOrderIds, getIntegerValue(request.getParameter("serviceorderid")));
            List<Integer> commissionOrderIds = getCommissionOrderIds(controllerName, methodName, parameter, null);
            List<Integer> visaIds = getVisaIds(controllerName, methodName, parameter, null);
            List<Integer> visaOfficialIds = getVisaOfficialIds(controllerName, methodName, parameter, null);
            if ("commissionOrder".equalsIgnoreCase(controllerName)) {
                addUnique(commissionOrderIds, getIntegerValue(request.getParameter("commissionOrderId")));
                if (usesCommissionOrderIdParameter(methodName)) {
                    addUnique(commissionOrderIds, getIntegerValue(request.getParameter("id")));
                }
            }
            if ("visaOfficial".equalsIgnoreCase(controllerName)) {
                addUnique(visaOfficialIds, getIntegerValue(request.getParameter("visaOfficialId")));
                if ("updateOfficialVisa".equalsIgnoreCase(methodName)) {
                    addUnique(visaOfficialIds, getIntegerValue(request.getParameter("id")));
                }
            }
            if ("visa".equalsIgnoreCase(controllerName)) {
                addUnique(visaIds, getIntegerValue(request.getParameter("visaId")));
                if (usesVisaIdParameter(methodName)) {
                    addUnique(visaIds, getIntegerValue(request.getParameter("id")));
                }
            }

            //前面是前置通知，后面是后置通知
            Object result = joinPoint.proceed();
            if (schoolId == null || schoolId <= 0) {
                schoolId = getSchoolId(controllerName, methodName, parameter, result);
            }
            addUnique(commissionOrderIds, getCommissionOrderIds(controllerName, methodName, null, result));
            addUnique(visaIds, getVisaIds(controllerName, methodName, null, result));
            addUnique(visaOfficialIds, getVisaOfficialIds(controllerName, methodName, null, result));
            collectIntegerValues(getResponseData(result), "serviceOrderId", relatedServiceOrderIds);
            if ("commissionOrder".equalsIgnoreCase(controllerName)
                    && usesServiceOrderCommissionIds(methodName)) {
                for (Integer relatedServiceOrderId : relatedServiceOrderIds) {
                    try {
                        List<CommissionOrderDO> commissionOrders = commissionOrderDAO
                                .listCommissionOrderByServiceOrderId(relatedServiceOrderId);
                        if (commissionOrders != null) {
                            collectIntegerValues(commissionOrders, "id", commissionOrderIds);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to resolve commission order ids by service order id: {}",
                                relatedServiceOrderId, e);
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            String urlStr = request.getRequestURL().toString();
            webLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
            webLog.setIp(request.getRemoteUser());
            webLog.setUserId(adminUserById.getId());
            webLog.setMethod(request.getMethod());

            webLog.setSpendTime((int) (endTime - startTime));

            // 使用java.time包
            Instant instant = Instant.ofEpochMilli(startTime);
            ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault()); // 使用系统默认时区
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateTime.format(formatter);

            webLog.setStartTime(formattedDate);
            webLog.setUri(requestURI);
            webLog.setUrl(request.getRequestURL().toString());
            webLog.setSchoolId(schoolId);
            if (!relatedServiceOrderIds.isEmpty()) {
                webLog.setServiceOrderId(relatedServiceOrderIds.get(0));
            }
            if (!commissionOrderIds.isEmpty()) {
                webLog.setCommissionOrderId(commissionOrderIds.get(0));
            }
            if (!visaIds.isEmpty()) {
                webLog.setVisaId(visaIds.get(0));
            }
            if (!visaOfficialIds.isEmpty()) {
                webLog.setVisaOfficialId(visaOfficialIds.get(0));
            }
            if (parameter != null && !methodName.contains("upload") && !methodName.equalsIgnoreCase("add")) {
                for (Object o : parameter) {
                    if (o.toString().contains("id")) {
                        // 定义正则表达式来匹配id后面的数字
                        // 注意：这个正则表达式假设id后面紧跟着等号，然后是数字，且数字可能有多位
                        String regex = "\\{id=(\\d+)\\}";
                        // 创建Pattern对象
                        Pattern pattern = Pattern.compile(regex);
                        // 创建Matcher对象
                        Matcher matcher = pattern.matcher(o.toString());
                        // 查找匹配项
                        if (matcher.find()) {
                            // 提取匹配的数字
                            String id = matcher.group(1);
                            if (urlStr.contains("user")) {
                                operatedUser = Integer.valueOf(id);
                                webLog.setOperatedUser(Integer.valueOf(id));
                            } else if (isServiceOrderUri(urlStr)) {
                                serviceorderId = Integer.valueOf(id);
                                webLog.setServiceOrderId(serviceorderId);
                            }
                            log.info("ID: " + id);
                        } else {
                            System.out.println("未找到ID");
                        }
                    }
                    if (o.toString().contains("serviceOrderId")) {
                        // 定义正则表达式来匹配id后面的数字
                        // 注意：这个正则表达式假设id后面紧跟着等号，然后是数字，且数字可能有多位
                        String regex = "\\{serviceOrderId=(\\d+)\\}";
                        // 创建Pattern对象
                        Pattern pattern = Pattern.compile(regex);
                        // 创建Matcher对象
                        Matcher matcher = pattern.matcher(o.toString());
                        // 查找匹配项
                        if (matcher.find()) {
                            // 提取匹配的数字
                            String id = matcher.group(1);
                            if (isServiceOrderUri(urlStr)) {
                                serviceorderId = Integer.valueOf(id);
                                webLog.setServiceOrderId(serviceorderId);
                            }
                            log.info("ID: " + id);
                        } else {
                            System.out.println("未找到ID");
                        }
                    }

                }
            }
            Integer requestServiceOrderId = getIntegerParameter(parameter, "serviceOrderId");
            if (requestServiceOrderId != null && requestServiceOrderId > 0) {
                serviceorderId = requestServiceOrderId;
                webLog.setServiceOrderId(requestServiceOrderId);
            }
            String apList = "";
            if (ObjectUtil.isNotEmpty(adminUserLoginInfo)) {
                apList = adminUserLoginInfo.getApList();
            }
            switch (apList) {
                case "GW":
                    apList = "顾问";
                    break;
                case "WA":
                    apList = "文案";
                    break;
                case "KJ":
                    apList = "会计";
                    break;
                case "SUPERAD":
                    apList = "超级管理员";
                    break;
                default: apList = apList;
            }
            webLog.setRole(apList);
            if (parameter != null && methodName.equalsIgnoreCase("add")) {
                Response<Integer> integerResponse = (Response) result;
                if (urlStr.contains("serviceOrder")) {
                    webLog.setServiceOrderId(integerResponse.getData());
                }
                if (urlStr.contains("user")) {
                    webLog.setOperatedUser(integerResponse.getData());
                }
//                if (urlStr.contains("contractData")) {
//                    contractData = integerResponse.getData();
//                }
                List<Object> collect = parameter.stream().filter(A -> A.toString().contains("contractData")).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    WebLogDTO webLogDTO = new WebLogDTO();
                    webLogDTO.setOperatedUser(webLog.getOperatedUser());
                    webLogDTO.setServiceOrderId(webLog.getServiceOrderId());
                    webLogDTO.setUrl("http://127.0.0.1:8080/admin_v2.1/serviceOrder/addContractData");
                    webLogDTO.setUri("/admin_v2.1/serviceOrder/addContractData");
                    webLogDTO.setRole(apList);
                    webLogDTO.setSpendTime(webLog.getSpendTime());
                    webLogDTO.setParameter(((HashMap) collect.get(0)).get("contractData").toString());
                    webLogDTO.setStartTime(webLog.getStartTime());
                    webLogDTO.setUserId(webLog.getUserId());
                    webLogDTO.setBasePath(webLog.getBasePath());
                    webLogDAO.addWebLogs(webLogDTO);
                }
            }
            if (parameter != null && methodName.equalsIgnoreCase("update")) {
                String contractData1;
                List<Object> collect = parameter.stream().filter(A -> A.toString().contains("contractData")).collect(Collectors.toList());
                if (!collect.isEmpty()) {
                    contractData1 = ((HashMap) collect.get(0)).get("contractData").toString();
                } else {
                    contractData1 = "";
                }
                if (!collect.isEmpty()) {
                    List<WebLogDTO> webLogDTOS = webLogDAO.listWebLogs(webLog.getServiceOrderId(), null, null, null,
                            null, null, null, null, 0, 999);
                    if (webLogDTOS != null && !webLogDTOS.isEmpty() && !StringUtils.isEmpty(contractData1)) {
                        List<WebLogDTO> collect1 = webLogDTOS.stream().filter(A -> contractData1.equalsIgnoreCase(A.getParameter())).collect(Collectors.toList());
                        if (collect1.isEmpty()) {
                            WebLogDTO webLogDTO = new WebLogDTO();
                            webLogDTO.setOperatedUser(webLog.getOperatedUser());
                            webLogDTO.setServiceOrderId(webLog.getServiceOrderId());
                            webLogDTO.setUrl("http://127.0.0.1:8080/admin_v2.1/serviceOrder/updateContractData");
                            webLogDTO.setUri("/admin_v2.1/serviceOrder/updateContractData");
                            webLogDTO.setRole(apList);
                            webLogDTO.setSpendTime(webLog.getSpendTime());
                            webLogDTO.setParameter(contractData1);
                            webLogDTO.setStartTime(webLog.getStartTime());
                            webLogDTO.setUserId(webLog.getUserId());
                            webLogDTO.setBasePath(webLog.getBasePath());
                            webLogDAO.addWebLogs(webLogDTO);
                        }
                    }
                }
            }
            if (parameter != null) {
                webLog.setParameter(parameter.toString());
            }
            String resultString = result.toString();
            if (resultString.length() >= 2000) {
                webLog.setResult(resultString.substring(0, 1999));
            } else {
                webLog.setResult(resultString);
            }


            if (adminUserLoginInfo == null) {
                return result;
            }

            if (shouldSaveWebLog(controllerName, methodName)) {
                saveWebLogs(webLog, commissionOrderIds, visaIds, visaOfficialIds);
            }
            log.info("{}", JSONUtil.parse(webLog));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 
    /**
     * 根据方法和传入的参数获取请求参数
     */
    private boolean isServiceOrderUri(String urlStr) {
        return urlStr != null && urlStr.contains("serviceOrder");
    }

    private boolean shouldSaveWebLog(String controllerName, String methodName) {
        if (StringUtils.isEmpty(methodName)) {
            return false;
        }
        String lowerMethodName = methodName.toLowerCase();
        if (lowerMethodName.contains("upload") || lowerMethodName.contains("img")) {
            return false;
        }
        if ("serviceOrder".equalsIgnoreCase(controllerName)
                && "get".equalsIgnoreCase(methodName)) {
            return false;
        }
        if ("commissionOrder".equalsIgnoreCase(controllerName)
                || "visaOfficial".equalsIgnoreCase(controllerName)
                || "visa".equalsIgnoreCase(controllerName)) {
            return !isQueryMethod(methodName);
        }
        return !methodName.contains("list")
                && !methodName.contains("count");
    }

    private boolean isQueryMethod(String methodName) {
        String lowerMethodName = methodName.toLowerCase();
        return lowerMethodName.startsWith("list")
                || lowerMethodName.startsWith("count")
                || lowerMethodName.startsWith("get")
                || lowerMethodName.startsWith("down");
    }

    private void saveWebLogs(WebLogDTO webLog, List<Integer> commissionOrderIds, List<Integer> visaIds,
                             List<Integer> visaOfficialIds) {
        if (commissionOrderIds != null && !commissionOrderIds.isEmpty()) {
            webLog.setServiceOrderId(null);
            webLog.setVisaId(null);
            webLog.setVisaOfficialId(null);
            for (Integer commissionOrderId : commissionOrderIds) {
                webLog.setCommissionOrderId(commissionOrderId);
                saveWebLogWithServiceOrderRelations(webLog);
            }
            return;
        }
        if (visaIds != null && !visaIds.isEmpty()) {
            webLog.setServiceOrderId(null);
            webLog.setCommissionOrderId(null);
            webLog.setVisaOfficialId(null);
            for (Integer visaId : visaIds) {
                webLog.setVisaId(visaId);
                saveWebLogWithServiceOrderRelations(webLog);
            }
            return;
        }
        if (visaOfficialIds != null && !visaOfficialIds.isEmpty()) {
            webLog.setServiceOrderId(null);
            webLog.setCommissionOrderId(null);
            webLog.setVisaId(null);
            for (Integer visaOfficialId : visaOfficialIds) {
                webLog.setVisaOfficialId(visaOfficialId);
                saveWebLogWithServiceOrderRelations(webLog);
            }
            return;
        }
        saveWebLogWithServiceOrderRelations(webLog);
    }

    private void saveWebLogWithServiceOrderRelations(WebLogDTO webLog) {
        Integer originalServiceOrderId = webLog.getServiceOrderId();
        int inserted = webLogDAO.addWebLogs(webLog);
        if (inserted <= 0 || originalServiceOrderId == null || originalServiceOrderId == 0) {
            return;
        }
        try {
            boolean isManage = firstPlace(originalServiceOrderId) != 1;
            ServiceOrderDO serviceOrderById = isManage
                    ? serviceOrderManageDAO.getServiceOrderById(originalServiceOrderId)
                    : serviceOrderDAO.getServiceOrderById(originalServiceOrderId);
            if (serviceOrderById != null && serviceOrderById.getApplicantParentId() > 0
                    && !"OVST".equalsIgnoreCase(serviceOrderById.getType())) {
                ServiceOrderDO serviceOrderByParent = serviceOrderDAO
                        .getServiceOrderById(serviceOrderById.getApplicantParentId());
                if (serviceOrderByParent != null) {
                    List<ServiceOrderDTO> deriveOrder = serviceOrderDAO.getDeriveOrder(serviceOrderByParent.getId());
                    webLog.setServiceOrderId(serviceOrderByParent.getId());
                    webLogDAO.addWebLogs(webLog);
                    if (deriveOrder != null) {
                        for (ServiceOrderDTO serviceOrderDO : deriveOrder) {
                            if (originalServiceOrderId.equals(serviceOrderDO.getId())) {
                                continue;
                            }
                            webLog.setServiceOrderId(serviceOrderDO.getId());
                            webLogDAO.addWebLogs(webLog);
                        }
                    }
                }
            }
            if (isManage) {
                List<ServiceOrderDTO> serviceOrderDTOS = serviceOrderManageDAO
                        .listChildrenServiceOrder(originalServiceOrderId);
                if (serviceOrderDTOS != null) {
                    for (ServiceOrderDTO serviceOrderDO : serviceOrderDTOS) {
                        webLog.setServiceOrderId(serviceOrderDO.getId());
                        webLogDAO.addWebLogs(webLog);
                    }
                }
            }
        } finally {
            webLog.setServiceOrderId(originalServiceOrderId);
        }
    }

    private List<Integer> getCommissionOrderIds(String controllerName, String methodName, List<Object> parameter,
                                                 Object result) {
        List<Integer> ids = new ArrayList<>();
        if (!"commissionOrder".equalsIgnoreCase(controllerName)) {
            return ids;
        }
        collectIntegerValues(parameter, "commissionOrderId", ids);
        if ("deleteComment".equalsIgnoreCase(methodName)) {
            Integer commentId = getIntegerParameter(parameter, "id");
            if (commentId != null && commentId > 0) {
                try {
                    addUnique(ids, commissionOrderCommentDAO.getCommissionOrderIdById(commentId));
                } catch (Exception e) {
                    log.warn("Failed to resolve commission order id by comment id: {}", commentId, e);
                }
            }
        }
        if (usesCommissionOrderIdParameter(methodName)) {
            collectIntegerValues(parameter, "id", ids);
        }
        if (usesResponseBusinessIds(methodName)) {
            collectIntegerValues(getResponseData(result), "id", ids);
        }
        return ids;
    }

    private String getLastPathSegment(String requestURI) {
        if (StringUtils.isEmpty(requestURI)) {
            return "";
        }
        String normalizedUri = requestURI;
        while (normalizedUri.endsWith("/")) {
            normalizedUri = normalizedUri.substring(0, normalizedUri.length() - 1);
        }
        int lastSlash = normalizedUri.lastIndexOf('/');
        return lastSlash >= 0 ? normalizedUri.substring(lastSlash + 1) : normalizedUri;
    }

    private String getControllerName(Class<?> declaringType, String[] uriSegments) {
        if (declaringType != null) {
            String simpleName = declaringType.getSimpleName();
            if (simpleName.endsWith("Controller") && simpleName.length() > "Controller".length()) {
                String name = simpleName.substring(0, simpleName.length() - "Controller".length());
                return Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }
        }
        return uriSegments.length > 2 ? uriSegments[2] : "";
    }

    private List<Integer> getVisaOfficialIds(String controllerName, String methodName, List<Object> parameter,
                                              Object result) {
        List<Integer> ids = new ArrayList<>();
        if (!"visaOfficial".equalsIgnoreCase(controllerName)) {
            return ids;
        }
        collectIntegerValues(parameter, "visaOfficialId", ids);
        if ("updateOfficialVisa".equalsIgnoreCase(methodName)) {
            collectIntegerValues(parameter, "id", ids);
        }
        if ("add".equalsIgnoreCase(methodName)) {
            collectIntegerValues(getResponseData(result), "id", ids);
        }
        return ids;
    }

    private List<Integer> getVisaIds(String controllerName, String methodName, List<Object> parameter,
                                     Object result) {
        List<Integer> ids = new ArrayList<>();
        if (!"visa".equalsIgnoreCase(controllerName)) {
            return ids;
        }
        collectIntegerValues(parameter, "visaId", ids);
        if ("deleteComment".equalsIgnoreCase(methodName)) {
            Integer commentId = getIntegerParameter(parameter, "id");
            if (commentId != null && commentId > 0) {
                try {
                    addUnique(ids, visaCommentDAO.getVisaIdById(commentId));
                } catch (Exception e) {
                    log.warn("Failed to resolve visa id by comment id: {}", commentId, e);
                }
            }
        }
        if (usesVisaIdParameter(methodName)) {
            collectIntegerValues(parameter, "id", ids);
        }
        if (usesVisaResponseBusinessIds(methodName)) {
            collectIntegerValues(getResponseData(result), "id", ids);
        }
        return ids;
    }

    private boolean usesVisaIdParameter(String methodName) {
        return "update".equalsIgnoreCase(methodName)
                || "kjUpdate".equalsIgnoreCase(methodName)
                || "updateKjApprovalDate".equalsIgnoreCase(methodName)
                || "close".equalsIgnoreCase(methodName)
                || "reopen".equalsIgnoreCase(methodName)
                || "delete".equalsIgnoreCase(methodName)
                || "approval".equalsIgnoreCase(methodName)
                || "refuse".equalsIgnoreCase(methodName);
    }

    private boolean usesVisaResponseBusinessIds(String methodName) {
        return "add".equalsIgnoreCase(methodName)
                || "update".equalsIgnoreCase(methodName)
                || "kjUpdate".equalsIgnoreCase(methodName)
                || "updateKjApprovalDate".equalsIgnoreCase(methodName)
                || "approval".equalsIgnoreCase(methodName)
                || "refuse".equalsIgnoreCase(methodName);
    }

    private boolean usesCommissionOrderIdParameter(String methodName) {
        return "update".equalsIgnoreCase(methodName)
                || "kjUpdate".equalsIgnoreCase(methodName)
                || "updateKjApprovalDate".equalsIgnoreCase(methodName)
                || "updateCommission".equalsIgnoreCase(methodName)
                || "close".equalsIgnoreCase(methodName)
                || "approval".equalsIgnoreCase(methodName)
                || "refuse".equalsIgnoreCase(methodName)
                || "deleteCommissionOrder".equalsIgnoreCase(methodName);
    }

    private boolean usesResponseBusinessIds(String methodName) {
        return "add".equalsIgnoreCase(methodName)
                || "update".equalsIgnoreCase(methodName)
                || "kjUpdate".equalsIgnoreCase(methodName)
                || "updateKjApprovalDate".equalsIgnoreCase(methodName)
                || "updateCommission".equalsIgnoreCase(methodName)
                || "close".equalsIgnoreCase(methodName)
                || "approval".equalsIgnoreCase(methodName)
                || "refuse".equalsIgnoreCase(methodName)
                || "updateSubmitted".equalsIgnoreCase(methodName);
    }

    private boolean usesServiceOrderCommissionIds(String methodName) {
        return "updateInfo".equalsIgnoreCase(methodName) || "updateSubmitted".equalsIgnoreCase(methodName);
    }

    private Object getResponseData(Object result) {
        if (result instanceof Response) {
            return ((Response) result).getData();
        }
        return null;
    }

    private void collectIntegerValues(Object source, String key, List<Integer> values) {
        if (source == null) {
            return;
        }
        if (source instanceof Map) {
            Map map = (Map) source;
            addUnique(values, getIntegerValue(map.get(key)));
            for (Object value : map.values()) {
                collectIntegerValues(value, key, values);
            }
            return;
        }
        if (source instanceof Iterable) {
            for (Object item : (Iterable) source) {
                collectIntegerValues(item, key, values);
            }
            return;
        }
        if (source.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(source); i++) {
                collectIntegerValues(Array.get(source, i), key, values);
            }
            return;
        }
        addUnique(values, getIntegerValue(readValue(source, key)));
    }

    private void addUnique(List<Integer> values, Integer value) {
        if (value != null && value > 0 && !values.contains(value)) {
            values.add(value);
        }
    }

    private void addUnique(List<Integer> values, List<Integer> newValues) {
        if (newValues == null) {
            return;
        }
        for (Integer value : newValues) {
            addUnique(values, value);
        }
    }

    private Integer getSchoolId(String controllerName, String methodName, List<Object> parameter, Object result) {
        if ("schoolInstitution".equalsIgnoreCase(controllerName)) {
            return getSchoolInstitutionId(methodName, parameter, result);
        }
        if ("schoolCourse".equalsIgnoreCase(controllerName)) {
            return getSchoolCourseProviderId(parameter);
        }
        return null;
    }

    private Integer getSchoolInstitutionId(String methodName, List<Object> parameter, Object result) {
        if ("deleteSetting".equalsIgnoreCase(methodName)) {
            Integer settingId = getIntegerParameter(parameter, "id");
            if (settingId != null && settingId > 0) {
                SchoolSettingNewDO setting = schoolSettingNewDAO.getSchoolSettingNewById(settingId);
                if (setting != null && setting.getProviderId() > 0) {
                    return setting.getProviderId();
                }
            }
            return null;
        }
        if ("deleteComment".equalsIgnoreCase(methodName)) {
            Integer commentId = getIntegerParameter(parameter, "id");
            if (commentId != null && commentId > 0) {
                SchoolInstitutionCommentDO comment = schoolInstitutionCommentDAO.getCommentById(commentId);
                if (comment != null && comment.getSchoolInstitutionId() > 0) {
                    return comment.getSchoolInstitutionId();
                }
            }
            return null;
        }
        Integer schoolId = getIntegerParameter(parameter, "providerId");
        if (schoolId != null && schoolId > 0) {
            return schoolId;
        }
        schoolId = getIntegerParameter(parameter, "schoolInstitutionId");
        if (schoolId != null && schoolId > 0) {
            return schoolId;
        }
        if ("add".equalsIgnoreCase(methodName)) {
            return getResponseDataAsInteger(result);
        }
        schoolId = getIntegerParameter(parameter, "id");
        return schoolId != null && schoolId > 0 ? schoolId : null;
    }

    private Integer getSchoolCourseProviderId(List<Object> parameter) {
        Integer schoolId = getIntegerParameter(parameter, "providerId");
        if (schoolId != null && schoolId > 0) {
            return schoolId;
        }
        Integer courseId = getIntegerParameter(parameter, "id");
        if (courseId != null && courseId > 0) {
            SchoolCourseDO schoolCourseDO = schoolCourseDAO.schoolCourseById(courseId);
            if (schoolCourseDO != null && schoolCourseDO.getProviderId() > 0) {
                return schoolCourseDO.getProviderId();
            }
        }
        return null;
    }

    private Integer getResponseDataAsInteger(Object result) {
        if (result instanceof Response) {
            return getIntegerValue(((Response) result).getData());
        }
        return null;
    }

    private Integer getIntegerParameter(List<Object> parameter, String key) {
        if (parameter == null || parameter.isEmpty()) {
            return null;
        }
        for (Object item : parameter) {
            Integer value = getIntegerValue(readValue(item, key));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Object readValue(Object source, String key) {
        if (source == null || key == null) {
            return null;
        }
        if (source instanceof Map) {
            return ((Map) source).get(key);
        }
        String methodName = "get" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
        try {
            Method method = source.getClass().getMethod(methodName);
            return method.invoke(source);
        } catch (Exception e) {
            try {
                Field field = source.getClass().getDeclaredField(key);
                field.setAccessible(true);
                return field.get(source);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String str = value.toString();
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<Object> getParameter(Method method, Object[] args)
   {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            //将RequestBody注解修饰的参数作为请求参数
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if (requestBody != null) {
                argList.add(args[i]);
            }
            //将RequestParam注解修饰的参数作为请求参数
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if (!StringUtils.isEmpty(requestParam.value())) {
                    key = requestParam.value();
                }
                if (args[i] == null) {
                    continue;
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if (argList.isEmpty()) {
            return null;
        }  else {
            return argList;
        }
    }

    private Integer firstPlace(Integer id) {
        // 1. 将int转换为String
        String numberStr = String.valueOf(id);

        // 2. 找到第一个不是负号的字符（即第一个数字）
        char firstChar = numberStr.charAt(0);
        int firstDigit;

        if (firstChar == '-') {
            // 如果是负数，则取第二个字符
            firstDigit = Character.getNumericValue(numberStr.charAt(1));
        } else {
            // 如果是正数，直接取第一个字符
            firstDigit = Character.getNumericValue(firstChar);
        }
        return firstDigit;
    }
}

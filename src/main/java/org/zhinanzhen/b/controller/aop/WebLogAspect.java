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
import org.zhinanzhen.b.dao.ServiceOrderDAO;
import org.zhinanzhen.b.dao.ServiceOrderManageDAO;
import org.zhinanzhen.b.dao.WebLogDAO;
import org.zhinanzhen.b.dao.pojo.ServiceOrderDO;
import org.zhinanzhen.b.service.pojo.ServiceOrderDTO;
import org.zhinanzhen.b.service.pojo.WebLogDTO;
import org.zhinanzhen.tb.controller.BaseController;
import org.zhinanzhen.tb.controller.Response;
import org.zhinanzhen.tb.dao.AdminUserDAO;
import org.zhinanzhen.tb.dao.pojo.AdminUserDO;

import javax.servlet.http.HttpServletRequest;
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
            "execution(public * org.zhinanzhen.b.controller.KjController.updateKj(..))"
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
                                } else {
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
                                webLog.setServiceOrderId(Integer.valueOf(id));
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

            //前面是前置通知，后面是后置通知
            Object result = joinPoint.proceed();

            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
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
            String methodName = split[split.length - 1];
            List<Object> parameter = getParameter(method, joinPoint.getArgs());
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
                            } else {
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
                            serviceorderId = Integer.valueOf(id);
                            webLog.setServiceOrderId(serviceorderId);
                            log.info("ID: " + id);
                        } else {
                            System.out.println("未找到ID");
                        }
                    }

                }
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
                    List<WebLogDTO> webLogDTOS = webLogDAO.listWebLogs(webLog.getServiceOrderId(), null, null, null, 0, 999);
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

            boolean isManage = false;

            if (!StringUtils.isEmpty(methodName)) {
                if (!methodName.contains("list") && !methodName.contains("upload") && !methodName.contains("img") && !methodName.contains("count")){
                    int i = webLogDAO.addWebLogs(webLog);
                    if (i > 0) {
                        Integer serviceOrderId = webLog.getServiceOrderId();
                        if (serviceOrderId != null && serviceOrderId != 0) {
                            ServiceOrderDO serviceOrderById = null;
                            Integer firsted = firstPlace(serviceOrderId);
                            if (firsted == 1) {
                                serviceOrderById = serviceOrderDAO.getServiceOrderById(serviceOrderId);
                            } else {
                                isManage = true;
                                serviceOrderById = serviceOrderManageDAO.getServiceOrderById(serviceOrderId);
                            }
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
                            if (isManage) {
                                List<ServiceOrderDTO> serviceOrderDTOS = serviceOrderManageDAO.listChildrenServiceOrder(serviceOrderId);
                                if (serviceOrderDTOS != null && !serviceOrderDTOS.isEmpty()) {
                                    for (ServiceOrderDTO serviceOrderDO : serviceOrderDTOS) {
                                        webLog.setServiceOrderId(serviceOrderDO.getId());
                                        webLogDAO.addWebLogs(webLog);
                                    }
                                }
                            }
                        }
                    }
                }
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
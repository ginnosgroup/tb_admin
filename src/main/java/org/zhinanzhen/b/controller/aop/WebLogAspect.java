package org.zhinanzhen.b.controller.aop;

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
import org.zhinanzhen.b.dao.WebLogDAO;
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

    //定义切点表达式,指定通知功能被应用的范围
//                "execution(public * org.zhinanzhen.b.controller.AdviserDataController.adviserDataMigration(..)) || " +
//                        "execution(public * org.zhinanzhen.b.controller.OfficialController.officialHandover(..)) || " +
//    "execution(public * org.zhinanzhen.tb.controller.UserController.update(..))"
    @Pointcut("execution(public * org.zhinanzhen.b.controller.ServiceOrderController.*(..)) || " +
            "execution(public * org.zhinanzhen.tb.controller.UserController.addUser(..))"
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
    @AfterReturning(value = "webLog()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {
    }
 
    //通知包裹了目标方法，在目标方法调用之前和之后执行自定义的行为
    //ProceedingJoinPoint切入点可以获取切入点方法上的名字、参数、注解和对象
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable 
    {
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
                return null;
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

//        if (parameter != null && methodName.contains("userIds") && !methodName.equalsIgnoreCase("newAdviserId")) {
//            String regexUserIds = "\\{userIds=(\\d+)\\}";
//            for (Object o : parameter) {
//                // 创建Pattern对象
//                Pattern pattern = Pattern.compile(regexUserIds);
//                // 创建Matcher对象
//                Matcher matcher = pattern.matcher(o.toString());
//                // 查找匹配项
//                if (matcher.find()) {
//                    // 提取匹配的数字
//                    String userIds = matcher.group(1);
//                    String[] split1 = userIds.split(";");
//                    for (int i = 0; i < split1.length; i++) {
//                        webLog.setOperatedUser(Integer.valueOf(split1[i]));
//                        webLogDAO.addWebLogs(webLog);
//                    }
//                } else {
//                    System.out.println("未找到修改用户id");
//                }
//            }
//            return result;
//        }


            if (!StringUtils.isEmpty(methodName)) {
                if (!methodName.contains("list") && !methodName.contains("upload") && !methodName.contains("img") && !methodName.contains("count")){
                    webLogDAO.addWebLogs(webLog);
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
}
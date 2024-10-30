package org.zhinanzhen.b.controller.aop;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import javax.servlet.http.HttpSession;
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
    @Pointcut("execution(public * org.zhinanzhen.b.controller.ServiceOrderController.*(..))")
//            "execution(public * org.zhinanzhen.b.controller.VisaOfficialController.*(..)) || " +
//            "execution(public * org.zhinanzhen.b.controller.VisaController.*(..)) || " +
//            "execution(public * org.zhinanzhen.b.controller.CommissionOrderController.*(..))")
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
        webLog.setUsername(adminUserById.getUsername());
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
                if ("id".equalsIgnoreCase(o.toString())) {
                    JSONObject jsonObject = JSONObject.parseObject(o.toString());
                    webLog.setServiceOrderId((Integer) jsonObject.get("id"));
                }
                if ("serviceOrderId".equalsIgnoreCase(o.toString())) {
                    JSONObject jsonObject = JSONObject.parseObject(o.toString());
                    webLog.setServiceOrderId((Integer) jsonObject.get("serviceOrderId"));
                }
            }
        }
        if (parameter != null && methodName.equalsIgnoreCase("add")) {
            Response<Integer> integerResponse = (Response) result;
            webLog.setServiceOrderId(integerResponse.getData());
        }
        webLog.setParameter(parameter.toString());
        webLog.setResult(result.toString());


        if (!StringUtils.isEmpty(methodName)) {
            if (!methodName.contains("list") && !methodName.contains("upload") && !methodName.contains("img")){
                webLogDAO.addWebLogs(webLog);
            }
        }
        log.info("{}", JSONUtil.parse(webLog));
        return result;
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
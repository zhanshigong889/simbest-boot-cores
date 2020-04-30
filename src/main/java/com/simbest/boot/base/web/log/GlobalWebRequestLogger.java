/**
 * 版权所有 © 北京晟壁科技有限公司 2017-2027。保留一切权利!
 */
package com.simbest.boot.base.web.log;

import com.google.common.collect.ImmutableSet;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysLogWeb;
import com.simbest.boot.sys.service.ISysLogWebService;
import com.simbest.boot.util.security.SecurityUtils;
import com.simbest.boot.util.server.HostUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;

/**
 * 用途：全局Web请求日志拦截器
 * 作者: lishuyi 
 * 时间: 2017/11/5  23:44 
 */
@Slf4j
@Aspect
@Order(10)
@Component
@DependsOn(value = {"appConfig", "sysLogWebService"})
public class GlobalWebRequestLogger {

    private final static String LOGTAG = "GWL=======>>";

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ISysLogWebService logWebService;

    private ThreadLocal<Long> startTime = new NamedThreadLocal<>("global-web-logger");

    private Set<String> notRecordController = ImmutableSet.of("com.simbest.boot.security.auth.controller.CaptchaController",
            "com.simbest.boot.security.auth.controller.LoginController", "com.simbest.boot.security.auth.controller.IndexController",
            "com.simbest.boot.sys.web.SysHealthController");

//    @Pointcut("execution(* *..web..*Controller.*(..))")
    @Pointcut("execution(* *..*Controller.*(..))")
    public void webLog() { }

    /**
     * 方案一：使用Before,AfterReturning处理
     * @param joinPoint 切入点
     * @throws Throwable 参数
     */
//    @Before("webLog()")
//    public void doBefore(JoinPoint joinPoint) throws Throwable {
//        startTime.set(System.currentTimeMillis());
//        // 接收到请求，记录请求内容
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = attributes.getRequest();
//        // 记录下请求内容
//        log.debug(LOGTAG + "URL : " + request.getRequestURL().toString());
//        log.debug(LOGTAG + "HTTP_METHOD : " + request.getMethod());
//        log.debug(LOGTAG + "IP : " + HostUtil.getClientIpAddress(request));
//        log.debug(LOGTAG + "CLASS_METHOD : "
//                + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
//        log.debug(LOGTAG + "ARGS : " + Arrays.toString(joinPoint.getArgs()));
//    }
//
//    /**
//     *
//     * @param ret 参数
//     * @throws Throwable 异常
//     */
//    @AfterReturning(returning = "ret", pointcut = "webLog()")
//    public void doAfterReturning(Object ret) throws Throwable {
//        // 处理完请求，返回内容
//        log.debug(LOGTAG + "RESPONSE : " + ret);
//        log.debug(LOGTAG + "SPEND TIME : " + (System.currentTimeMillis() - startTime.get()) + "ms");
//    }


    /**
     * 方案二：使用Around处理
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around(value = "webLog()")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        startTime.set(System.currentTimeMillis());
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        //防止不是http请求的方法，例如：scheduled
        if (ra == null || sra == null) {
            return pjp.proceed();
        }

        HttpServletRequest request = sra.getRequest();
        String url = request.getRequestURL().toString();
        String ip = HostUtil.getClientIpAddress(request);
        String controller = pjp.getSignature().getDeclaringTypeName();
        String methodname = pjp.getSignature().getName();
        String args = Arrays.toString(pjp.getArgs());
        log.debug(LOGTAG + "请求路径【{}】", url);
        log.debug(LOGTAG + "IP地址【{}】", ip);
        log.debug(LOGTAG + "请求方法【{}】", controller.concat(ApplicationConstants.DOT).concat(methodname));
        log.debug(LOGTAG + "请求参数【{}】", args);
        SysLogWeb logWeb = SysLogWeb.builder().url(request.getRequestURL().toString())
                .ip(ip).controller(controller).methodname(methodname).failed(true)
                .args(StringUtils.substring(args, 0, 1999))
                .build();
        try {
            Object response = pjp.proceed();
            logWeb.setFailed(false);
            return response;
        } catch (Throwable e) {
            log.debug(LOGTAG + "异常信息: " + e.getMessage());
            logWeb.setFailedReason(e.getMessage());
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime.get();
            log.debug(LOGTAG + "花费耗时【{}】毫秒", duration);
            logWeb.setDuration(duration);
            try{
                if(appConfig.isRecordWebLog() && !notRecordController.contains(logWeb.getController())) {
                    logWeb.setCreator(SecurityUtils.getCurrentUserName());
                    logWebService.insert(logWeb);
                }
            }catch(Exception e){
                log.error("保存全局日志失败！");
                Exceptions.printException(e);
            }
        }
    }


}

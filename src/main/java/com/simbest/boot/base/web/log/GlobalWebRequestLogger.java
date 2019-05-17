/**
 * 版权所有 © 北京晟壁科技有限公司 2017-2027。保留一切权利!
 */
package com.simbest.boot.base.web.log;

import com.simbest.boot.util.server.HostUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * 用途：全局Web请求日志拦截器
 * 作者: lishuyi 
 * 时间: 2017/11/5  23:44 
 */
@Slf4j
@Aspect
@Order(10)
@Component
public class GlobalWebRequestLogger {

    private final static String LOGTAG = "GWL=======>>";

    ThreadLocal<Long> startTime = new NamedThreadLocal<>("global-web-logger");

    /**
     *
     */
    @Pointcut("execution(* *..web..*Controller.*(..))")
    public void webLog() { }

    /**
     *
     * @param joinPoint 切入点
     * @throws Throwable 参数
     */
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        log.debug(LOGTAG + "URL : " + request.getRequestURL().toString());
        log.debug(LOGTAG + "HTTP_METHOD : " + request.getMethod());
//        log.info("IP : " + request.getRemoteAddr());
        log.debug(LOGTAG + "IP : " + HostUtil.getClientIpAddress(request));
        log.debug(LOGTAG + "CLASS_METHOD : "
                + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.debug(LOGTAG + "ARGS : " + Arrays.toString(joinPoint.getArgs()));
    }

    /**
     *
     * @param ret 参数
     * @throws Throwable 异常
     */
    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        log.debug(LOGTAG + "RESPONSE : " + ret);
        log.debug(LOGTAG + "SPEND TIME : " + (System.currentTimeMillis() - startTime.get()) + "ms");
    }
}

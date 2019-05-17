/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 用途：无权限访问事件拦截
 * 作者: lishuyi
 * 时间: 2018/12/7  20:04
 */
@Slf4j
@NoArgsConstructor
@Component
public class FailedAccessDeniedHandler implements AccessDeniedHandler, AuthenticationFailureHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        handleResponse(response, exception);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        //登录发生错误计数，每错误一次，即向后再延时等待5分钟
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
        failedTimes = null == failedTimes ? AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES : failedTimes + AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES;
        RedisUtil.setBean(key, failedTimes);
        RedisUtil.expire(key, AuthoritiesConstants.ATTEMPT_LOGIN_FAILED_WAIT_SECONDS, TimeUnit.SECONDS);

        handleResponse(response, exception);
    }

    private void handleResponse(HttpServletResponse response, Exception exception) throws IOException{
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        JsonResponse jsonResponse = JsonResponse.builder().
                errcode(HttpStatus.UNAUTHORIZED.value())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.name())
                .build();
        if(null != exception) {
            log.warn("登录认证发生【{}】异常，错误信息为【{}】", exception.getClass().getSimpleName(), exception.getMessage());
            if (exception instanceof UsernameNotFoundException || exception.getCause() instanceof UsernameNotFoundException) {
                jsonResponse.setError(AuthoritiesConstants.UsernameNotFoundException);
            } else if (exception instanceof BadCredentialsException || exception.getCause() instanceof BadCredentialsException) {
                jsonResponse.setError(AuthoritiesConstants.BadCredentialsException);
            } else if (exception instanceof AccountExpiredException || exception.getCause() instanceof AccountExpiredException) {
                jsonResponse.setError(AuthoritiesConstants.AccountExpiredException);
            } else if (exception instanceof DisabledException || exception.getCause() instanceof DisabledException) {
                jsonResponse.setError(AuthoritiesConstants.DisabledException);
            } else if (exception instanceof LockedException || exception.getCause() instanceof LockedException) {
                jsonResponse.setError(AuthoritiesConstants.LockedException);
            } else if (exception instanceof CredentialsExpiredException || exception.getCause() instanceof CredentialsExpiredException) {
                jsonResponse.setError(AuthoritiesConstants.CredentialsExpiredException);
            } else {
                jsonResponse.setError(AuthoritiesConstants.InternalAuthenticationServiceException);
            }
        }
        response.getWriter().print(JacksonUtils.obj2json(jsonResponse));
    }

}

/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AccesssAppDeniedException;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 用途：记录登录错误日志
 * 作者: lishuyi
 * 时间: 2018/2/25  18:36
 */
@Slf4j
@NoArgsConstructor
@Component
public class FailedLoginHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        //登录发生错误计数，每错误一次，即向后再延时等待5分钟
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
        failedTimes = null == failedTimes ? AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES : failedTimes + AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES;
        RedisUtil.setBean(key, failedTimes);
        RedisUtil.expire(key, AuthoritiesConstants.ATTEMPT_LOGIN_FAILED_WAIT_SECONDS, TimeUnit.SECONDS);

        if (exception != null) {
            // 隐藏账户不存在异常，统一抛出认证密码异常
            if (exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException || exception instanceof InternalAuthenticationServiceException) {
                request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION,
                        new InsufficientAuthenticationException(ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS));
            } else if (exception instanceof AttempMaxLoginFaildException) {
                request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION,
                        new InsufficientAuthenticationException(ErrorCodeConstants.LOGIN_ERROR_EXCEED_MAX_TIMES));
            } else if (exception instanceof AccesssAppDeniedException) {
                request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION,
                        new InsufficientAuthenticationException(ErrorCodeConstants.LOGIN_APP_UNREGISTER_GROUP));
            } else {
                request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION,
                        new InsufficientAuthenticationException(ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS));
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
        request.getRequestDispatcher(ApplicationConstants.LOGIN_ERROR_PAGE).forward(request, response);
    }
}

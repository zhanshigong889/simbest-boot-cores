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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
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
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        response.getWriter().print(JacksonUtils.obj2json(JsonResponse.unauthorized()));
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //登录发生错误计数，每错误一次，即向后再延时等待5分钟
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
        failedTimes = null == failedTimes ? AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES : failedTimes + AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES;
        RedisUtil.setBean(key, failedTimes);
        RedisUtil.expire(key, AuthoritiesConstants.ATTEMPT_LOGIN_FAILED_WAIT_SECONDS, TimeUnit.SECONDS);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        response.getWriter().print(JacksonUtils.obj2json(JsonResponse.unauthorized()));
    }
}

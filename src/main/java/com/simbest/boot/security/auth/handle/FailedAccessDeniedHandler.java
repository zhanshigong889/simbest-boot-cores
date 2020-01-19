/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.redis.RedisRetryLoginCache;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.simbest.boot.base.web.response.JsonResponse.ERROR_CODE;

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
        handleResponse(request, response, exception);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        //登录发生错误计数，每错误一次，即向后再延时等待5分钟
        String username = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
        if(StringUtils.isNotEmpty(username)){
            RedisRetryLoginCache.addTryTimes(username);
        }
        handleResponse(request, response, exception);
    }

    protected void handleResponse(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {
        String ssoPath = getRequestPath(request);
        //单点登录首页，不返回JSON数据，而是调到登录首页
        if(!ApplicationConstants.ROOT_SSO_PAGE.equals(ssoPath)) {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/javascript;charset=utf-8");
            log.warn("无权限访问【{}】，即将返回HttpStatus.UNAUTHORIZED，状态码【{}】", request.getRequestURI(), HttpStatus.UNAUTHORIZED.value());
            JsonResponse jsonResponse = JsonResponse.unauthorized(request, exception);
            if(null != exception) {
                log.warn("登录认证发生【{}】异常，错误信息为【{}】", exception.getClass().getSimpleName(), exception.getMessage());
                if (exception instanceof UsernameNotFoundException || exception.getCause() instanceof UsernameNotFoundException) {
                    jsonResponse.setError(AuthoritiesConstants.UsernameNotFoundException);
                    jsonResponse.setErrcode(ERROR_CODE);
                } else if (exception instanceof BadCredentialsException || exception.getCause() instanceof BadCredentialsException) {
                    jsonResponse.setError(AuthoritiesConstants.BadCredentialsException);
                    jsonResponse.setErrcode(ERROR_CODE);
                } else if (exception instanceof AccountExpiredException || exception.getCause() instanceof AccountExpiredException) {
                    jsonResponse.setError(AuthoritiesConstants.AccountExpiredException);
                } else if (exception instanceof DisabledException || exception.getCause() instanceof DisabledException) {
                    jsonResponse.setError(AuthoritiesConstants.DisabledException);
                } else if (exception instanceof LockedException || exception.getCause() instanceof LockedException) {
                    jsonResponse.setError(AuthoritiesConstants.LockedException);
                } else if (exception instanceof CredentialsExpiredException || exception.getCause() instanceof CredentialsExpiredException) {
                    jsonResponse.setError(AuthoritiesConstants.CredentialsExpiredException);
                } else if (exception instanceof InsufficientAuthenticationException || exception.getCause() instanceof InsufficientAuthenticationException) {
                    jsonResponse.setError(AuthoritiesConstants.InsufficientAuthenticationException);
                } else if (exception instanceof AttempMaxLoginFaildException || exception.getCause() instanceof AttempMaxLoginFaildException) {
                    jsonResponse.setError(AuthoritiesConstants.AttempMaxLoginFaildException);
                } else {
                    jsonResponse.setError(AuthoritiesConstants.InternalAuthenticationServiceException);
                }
            }
            String responseStr = JacksonUtils.obj2json(jsonResponse);
            log.warn("访问控制校验异常，即将返回【{}】", responseStr);
            response.getWriter().print(responseStr);
        }
        else{
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                request.getRequestDispatcher(ApplicationConstants.LOGIN_PAGE).forward(request, response);
            } catch (ServletException e) {
                Exceptions.printException(e);
            }
        }
    }

    private String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();
        if (request.getPathInfo() != null) {
            url += request.getPathInfo();
        }
        return url;
    }
}

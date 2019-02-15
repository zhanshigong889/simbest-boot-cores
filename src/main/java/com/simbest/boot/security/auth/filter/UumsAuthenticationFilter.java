/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;
import com.simbest.boot.security.auth.provider.sso.token.UumsAuthentication;
import com.simbest.boot.security.auth.provider.sso.token.UumsAuthenticationCredentials;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 用途：基于UUMS主数据的单点登录拦截器
 * 作者: lishuyi
 * 时间: 2018/1/20  15:05
 */
@Slf4j
public class UumsAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public UumsAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        String username = request.getParameter(AuthoritiesConstants.SSO_UUMS_USERNAME);
        String password = request.getParameter(AuthoritiesConstants.SSO_UUMS_PASSWORD);
        String appcode = request.getParameter(AuthoritiesConstants.SSO_API_APP_CODE);

        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + username;
        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
        if(null != failedTimes && failedTimes >= AuthoritiesConstants.ATTEMPT_LOGIN_MAX_TIMES){
            throw new AttempMaxLoginFaildException(ErrorCodeConstants.LOGIN_ERROR_EXCEED_MAX_TIMES);
        } else {
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(appcode)) {
                throw new BadCredentialsException(
                        "Authentication principal can not be null: " + username);
            }

            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
            if (authenticationIsRequired(existingAuth, username)) {
                UumsAuthentication uumsAuthentication = new UumsAuthentication(username, UumsAuthenticationCredentials.builder()
                        .password(password).appcode(appcode).build());
                return this.getAuthenticationManager().authenticate(uumsAuthentication);
            }
            return existingAuth;
        }

    }

    /**
     * 判断单点用户名是否需要验证
     *
     * @param username 用户名
     * @return true/false
     */
    private boolean authenticationIsRequired(Authentication existingAuth, String username) {
        if (existingAuth == null || !SecurityUtils.isAuthenticated()) {
            return true;
        } else if (existingAuth instanceof UumsAuthentication
                && !existingAuth.getName().equals(username)) {
            return true;
        }
        return false;
    }


//    /**
//     * 登录发生错误计数，每错误一次，即向后再延时等待5分钟
//     * @param request
//     * @param response
//     * @param failed
//     * @throws IOException
//     * @throws ServletException
//     */
//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest request,
//                                              HttpServletResponse response, AuthenticationException failed)
//            throws IOException, ServletException {
//
//        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + request.getParameter(AuthoritiesConstants.SSO_UUMS_USERNAME);
//        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
//        failedTimes = null == failedTimes ? AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES : failedTimes + AuthoritiesConstants.ATTEMPT_LOGIN_INIT_TIMES;
//        RedisUtil.setBean(key, failedTimes);
//        RedisUtil.expire(key, AuthoritiesConstants.ATTEMPT_LOGIN_FAILED_WAIT_SECONDS, TimeUnit.SECONDS);
//
//        super.unsuccessfulAuthentication(request, response, failed);
//    }

//    /**
//     * 登录成功后，立即清除失败缓存，不再等待上述到期时间
//     * @param request
//     * @param response
//     * @param chain
//     * @param authResult
//     * @throws IOException
//     * @throws ServletException
//     */
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request,
//                                            HttpServletResponse response, FilterChain chain, Authentication authResult)
//            throws IOException, ServletException {
//
//        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + request.getParameter(AuthoritiesConstants.SSO_UUMS_USERNAME);
//        Boolean value = RedisUtil.delete(key);
//
//        super.successfulAuthentication(request, response, chain, authResult);
//    }
}

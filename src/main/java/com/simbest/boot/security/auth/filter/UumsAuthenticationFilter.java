/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AttempMaxLoginFaildException;
import com.simbest.boot.security.auth.authentication.GenericAuthentication;
import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            throws AuthenticationException {
        String username = request.getParameter(AuthoritiesConstants.SSO_UUMS_USERNAME);
        String password = request.getParameter(AuthoritiesConstants.SSO_UUMS_PASSWORD);
        String appcode = request.getParameter(AuthoritiesConstants.SSO_API_APP_CODE);
        log.debug("用户【{}】即将通过凭证【{}】访问应用【{}】", username, password, appcode);
        String key = AuthoritiesConstants.LOGIN_FAILED_KEY + username;
        Integer failedTimes = RedisUtil.getBean(key, Integer.class);
        if(null != failedTimes && failedTimes >= AuthoritiesConstants.ATTEMPT_LOGIN_MAX_TIMES){
            throw new AttempMaxLoginFaildException(ErrorCodeConstants.LOGIN_ERROR_EXCEED_MAX_TIMES);
        } else {
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(appcode)) {
                log.error("UUMS 认证失败， 令牌、密码、应用标识不能为空！");
                throw new BadCredentialsException(
                        "UUMS 认证失败， 令牌、密码、应用标识不能为空: " + username);
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
        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }
        else if (existingAuth instanceof UumsAuthentication
                && !existingAuth.getName().equals(username)) {
            return true;
        }
        else if (existingAuth instanceof UsernamePasswordAuthenticationToken
                && !existingAuth.getName().equals(username)) {
            return true;
        }
        else if (existingAuth instanceof GenericAuthentication
                && !existingAuth.getName().equals(username)) {
            return true;
        }
        return false;
    }


}

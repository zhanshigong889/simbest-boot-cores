/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.filter;

import com.simbest.boot.security.auth.authentication.GenericAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.redis.RedisRetryLoginCache;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;

/**
 * 用途：不基于UUMS主数据的单点登录拦截器
 * 作者: lishuyi
 * 时间: 2018/1/20  15:05
 */
@Slf4j
public class RestAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Setter
    private RsaEncryptor encryptor;

    public RestAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String username = this.obtainUsername(request);
            String password = this.obtainPassword(request);
            String appcode = this.obtainAppcode(request);
            RedisRetryLoginCache.preCheckTryTimes(username);
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
            if (authenticationIsRequired(existingAuth, username)) {
                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
                this.setDetails(request, authRequest);
                return this.getAuthenticationManager().authenticate(authRequest);
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
    protected boolean authenticationIsRequired(Authentication existingAuth, String username) {
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

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    protected String obtainPassword(HttpServletRequest request) {
        return encryptor.decrypt(request.getParameter(SPRING_SECURITY_FORM_PASSWORD_KEY)).trim();
    }

    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(SPRING_SECURITY_FORM_USERNAME_KEY).trim();
    }

    protected String obtainAppcode(HttpServletRequest request) {
        return request.getParameter("appcode").trim();
    }

}

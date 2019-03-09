/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.security.auth.filter.SsoAuthenticationRegister;
import com.simbest.boot.security.auth.provider.sso.service.SsoAuthenticationService;
import com.simbest.boot.security.auth.provider.sso.token.SsoUsernameAuthentication;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 用途：基于用户名的认证器
 * 作者: lishuyi
 * 时间: 2018/1/20  17:49
 */
@Slf4j
@Component
public class SsoUsernameAuthenticationProvider implements AuthenticationProvider {

    @Setter @Getter
    protected boolean hideUserNotFoundExceptions = false;

    @Autowired
    private SsoAuthenticationRegister ssoAuthenticationRegister;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Collection<SsoAuthenticationService> ssoAuthenticationServices = ssoAuthenticationRegister.getSsoAuthenticationService();
        SsoUsernameAuthentication successToken = null;
        for(SsoAuthenticationService authService : ssoAuthenticationServices) {
            log.info("SSO 认证器 【{}】 准备尝试认证 【{}】",authService.getClass().getName(), authentication.toString());
            successToken = authService.attemptAuthentication((SsoUsernameAuthentication) authentication);
            if(null != successToken) {
                log.warn("SUCCESS SSO 认证器 【{}】 成功认证 【{}】",
                        authService.getClass().getName(), authentication.toString());
                break;
            } else {
                log.warn("FAILED SSO 认证器 【{}】 尝试认证失败",
                        authService.getClass().getName());
            }
        }
        if (null != successToken) {
            return successToken;
        } else {
            log.error("FATAL SSO 遍历所有认证器后，最终认证失败 【{}】", authentication.toString());
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "错误的密码----Bad credentials"));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(SsoUsernameAuthentication.class);
    }
}

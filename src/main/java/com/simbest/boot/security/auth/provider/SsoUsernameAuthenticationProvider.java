/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Component;

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
    private GenericAuthenticationChecker genericAuthenticationChecker;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return genericAuthenticationChecker.authChek(authentication, authentication.getCredentials().toString());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(SsoUsernameAuthentication.class);
    }
}

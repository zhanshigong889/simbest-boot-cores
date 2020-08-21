/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.auth.authentication.PhoneAuthenticationToken;
import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import com.simbest.boot.security.auth.authentication.principal.KeyTypePrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * 用途：加密手机号码登录认证器
 * 作者: lishuyi
 * 时间: 2020/8/20  19:59
 */
@Slf4j
@Component
public class PhoneTokenAuthenticationProvider implements CustomAbstractAuthenticationProvider {

    @Autowired
    private GenericAuthenticationChecker genericAuthenticationChecker;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String phone = authentication.getPrincipal().toString();
        Object credentials = authentication.getCredentials(); //appcode
        Principal principal = KeyTypePrincipal.builder().keyword(phone).keyType(IAuthService.KeyType.preferredMobile).build();
        SsoUsernameAuthentication ssoUsernameAuthentication = new SsoUsernameAuthentication(principal, credentials);
        return genericAuthenticationChecker.authChek(ssoUsernameAuthentication, credentials.toString());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PhoneAuthenticationToken.class);
    }
}

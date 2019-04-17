/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.auth.authentication.principal.KeyTypePrincipal;
import com.simbest.boot.security.auth.authentication.principal.UsernamePrincipal;
import com.simbest.boot.security.auth.filter.SsoAuthenticationRegister;
import com.simbest.boot.security.auth.provider.sso.service.SsoAuthenticationService;
import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Component;

import java.security.Principal;
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

    @Autowired
    private GenericAuthenticationChecker genericAuthenticationChecker;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Principal principal = null;
        Collection<SsoAuthenticationService> ssoAuthenticationServices = ssoAuthenticationRegister.getSsoAuthenticationService();
        SsoUsernameAuthentication ssoUsernameAuthentication = (SsoUsernameAuthentication)authentication;
        for(SsoAuthenticationService ssoAuthenticationService : ssoAuthenticationServices) {
            log.info("SSO 认证器 【{}】 准备尝试认证 【{}】",ssoAuthenticationService.getClass().getSimpleName(), ssoUsernameAuthentication.getName());
            String decodeKeyword = ssoAuthenticationService.decryptKeyword(ssoUsernameAuthentication.getName());
            if(StringUtils.isNotEmpty(decodeKeyword)) {
                if(authentication.getPrincipal() instanceof UsernamePrincipal){
                    principal = UsernamePrincipal.builder().username(decodeKeyword).build();
                } else if (authentication.getPrincipal() instanceof KeyTypePrincipal){
                    principal = KeyTypePrincipal.builder().keyword(decodeKeyword)
                            .keyType(((KeyTypePrincipal)authentication.getPrincipal()).getKeyType()).build();
                }
                SsoUsernameAuthentication usernameAuthentication = new SsoUsernameAuthentication(principal, ssoUsernameAuthentication.getCredentials());
                return genericAuthenticationChecker.authChek(usernameAuthentication, ssoUsernameAuthentication.getCredentials().toString());
            } else {
                log.warn("SSO FAILED 认证器【{}】尝试认证失败", ssoAuthenticationService.getClass().getSimpleName());
            }
        }
        log.error("SSO FATAL FAILED 遍历所有认证器后，最终认证失败 【{}】", authentication.toString());
        throw new BadCredentialsException(messages.getMessage(
                "AbstractUserDetailsAuthenticationProvider.badCredentials",
                "BadCredentialsException: 坏的凭证"));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(SsoUsernameAuthentication.class);
    }
}

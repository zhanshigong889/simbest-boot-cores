/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.auth.authentication.PhoneCodeAuthenticationCredentials;
import com.simbest.boot.security.auth.authentication.PhoneCodeAuthenticationToken;
import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import com.simbest.boot.security.auth.authentication.principal.KeyTypePrincipal;
import com.simbest.boot.util.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * 用途：加密手机号码和动态短信登录认证器
 * 作者: lishuyi
 * 时间: 2020/8/20  19:59
 */
@Slf4j
@Component
public class PhoneCodeTokenAuthenticationProvider implements CustomAbstractAuthenticationProvider {

    @Autowired
    private GenericAuthenticationChecker genericAuthenticationChecker;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String phone = authentication.getPrincipal().toString();
        PhoneCodeAuthenticationCredentials credentials = (PhoneCodeAuthenticationCredentials)authentication.getCredentials();
        if(RedisUtil.validateAppSMSCode(phone, credentials.getSmscode())){
            Principal principal = KeyTypePrincipal.builder().keyword(phone).keyType(IAuthService.KeyType.preferredMobile).build();
            SsoUsernameAuthentication ssoUsernameAuthentication = new SsoUsernameAuthentication(principal, credentials.getAppcode());
            return genericAuthenticationChecker.authChek(ssoUsernameAuthentication, credentials.getAppcode());
        }
        else{
            log.error("应用【{}】通过手机号码【{}】短信【{}】验证失败", credentials.getAppcode(), phone, credentials.getSmscode());
            throw new
                    BadCredentialsException(ErrorCodeConstants.LOGIN_ERROR_INVALIDATE_USERNAME_PASSWORD);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PhoneCodeAuthenticationToken.class);
    }

}

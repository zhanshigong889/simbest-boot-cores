/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.util;

import com.simbest.boot.security.auth.authentication.GenericAuthentication;
import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

/**
 * 用途：Authentication 工具类
 * 作者: lishuyi
 * 时间: 2019/12/31  19:24
 */
@Slf4j
public class AuthenticationUtil {

    /**
     * 判断单点用户名是否需要验证
     */
    public static boolean authenticationIsRequired(Authentication existingAuth, String username) {
        boolean needAuth = false;
        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            needAuth = true;
        }
        else if (existingAuth instanceof SsoUsernameAuthentication
                && !existingAuth.getName().equals(username)) {
            needAuth = true;
        }
        else if (existingAuth instanceof UsernamePasswordAuthenticationToken
                && !existingAuth.getName().equals(username)) {
            needAuth = true;
        }
        else if (existingAuth instanceof OAuth2Authentication
                && !existingAuth.getName().equals(username)) {
            needAuth = true;
        }
        else if (existingAuth instanceof UumsAuthentication
                && !existingAuth.getName().equals(username)) {
            return true;
        }
        else if (existingAuth instanceof GenericAuthentication
                && !existingAuth.getName().equals(username)) {
            needAuth = true;
        }
        log.debug("Authentication认证类型为【{}】，认证主体为【{}】，判断是否需要进行认证结果为【{}】", existingAuth, username, needAuth);
        return needAuth;
    }
}

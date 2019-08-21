/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.AccesssAppDeniedException;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IPermission;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.auth.authentication.GenericAuthentication;
import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import com.simbest.boot.security.auth.authentication.principal.KeyTypePrincipal;
import com.simbest.boot.security.auth.authentication.principal.UsernamePrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 用途：对通过密码校验的authentication进行账户是否到期、是否可以访问应用的检查，并追加应用权限
 * 作者: lishuyi
 * 时间: 2018/9/6  16:57
 */
@Slf4j
@Component
public class GenericAuthenticationChecker {

    @Autowired
    private IAuthService authService;
    
    private UserDetailsChecker preAuthenticationChecks = new AccountStatusUserDetailsChecker();

    /**
     *
     * @param authentication
     * @param appcode
     * @return
     */
    public Authentication authChek(Authentication authentication, String appcode) {
        IUser authUser = null;
        try {
            if (authentication instanceof UsernamePasswordAuthenticationToken || authentication instanceof UumsAuthentication) {
                authUser = authService.findByKey(authentication.getName(), IAuthService.KeyType.username);
            }
            else if (authentication instanceof SsoUsernameAuthentication) {
                if (authentication.getPrincipal() instanceof UsernamePrincipal) {
                    UsernamePrincipal principal = (UsernamePrincipal) authentication.getPrincipal();
                    authUser = authService.findByKey(principal.getUsername(), IAuthService.KeyType.username);
                } else if (authentication.getPrincipal() instanceof KeyTypePrincipal) {
                    KeyTypePrincipal principal = (KeyTypePrincipal) authentication.getPrincipal();
                    authUser = authService.findByKey(principal.getKeyword(), principal.getKeyType());
                }
            }
        } catch (Exception e){
            log.error("通用认证过程发生未知异常");
            Exceptions.printException(e);
            throw new
                    BadCredentialsException(ErrorCodeConstants.LOGIN_ERROR_INVALIDATE_USERNAME_PASSWORD);
        }
        if(null == authUser){
            log.error("通用认证过程无法获取用户信息");
            throw new
                    BadCredentialsException(ErrorCodeConstants.LOGIN_ERROR_INVALIDATE_USER);
        }
        preAuthenticationChecks.check(authUser);
        boolean accessApp = authService.checkUserAccessApp(authUser.getUsername(), appcode);
        if (accessApp) {
            //追加权限
            Set<? extends IPermission> appPermission = authService.findUserPermissionByAppcode(authUser.getUsername(), appcode);
            if (null != appPermission && !appPermission.isEmpty()) {
                log.debug("即将为用户【{}】在应用【{}】追加【{}】项权限", authUser.getUsername(), appcode, appPermission.size());
                authUser.addAppPermissions(appPermission);
                authUser.addAppAuthorities(appPermission);
            }
            GenericAuthentication result = new GenericAuthentication(authUser, UumsAuthenticationCredentials.builder()
                    .password(authUser.getPassword()).appcode(appcode).build(), authUser.getAuthorities());
            result.setDetails(authentication.getDetails());
            log.info("用户【{}】访问【{}】认证成功！", authUser.getUsername(), appcode);
            return result;
        } else {
            throw new
                    AccesssAppDeniedException(ErrorCodeConstants.LOGIN_APP_UNREGISTER_GROUP);
        }
    }
}

/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.google.common.collect.Sets;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.ApplicationConstants;
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
import com.simbest.boot.security.auth.service.IAuthUserCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    protected IAuthUserCacheService authUserCacheService;

    private UserDetailsChecker preAuthenticationChecks = new AccountStatusUserDetailsChecker();

    private IUser findByKey(String keyword, IAuthService.KeyType keyType, String appcode) {
        IUser user = authUserCacheService.loadCacheUser(keyword);
        if (null == user) {
            user = authService.findByKey(keyword, keyType, appcode);
            if (null != user) {
                authUserCacheService.saveOrUpdateCacheUser(user);
            }
        }
        log.debug("通过关键字【{}】和关键字类型【{}】应用代码【{}】获取用户信息为【{}】", keyword, keyType.name(), appcode, user);
        return user;
    }

    private boolean checkUserAccessApp(String username, String appcode) {
        Boolean isPermit = authUserCacheService.loadCacheUserAccess(username, appcode);
        if (null == isPermit) {
            isPermit = authService.checkUserAccessApp(username, appcode);
            authUserCacheService.saveOrUpdateCacheUserAccess(username, appcode, isPermit);
        }
        return isPermit;
    }

    private Set<? extends IPermission> findUserPermissionByAppcode(String username, String appcode) {
        Set<IPermission> permissions = authUserCacheService.loadCacheUserPermission(username, appcode);
        if (null == permissions) {
            permissions = Sets.newHashSet();
            Set<? extends IPermission> appPermission = authService.findUserPermissionByAppcode(username, appcode);
            if (null != appPermission && !appPermission.isEmpty()) {
                for (IPermission s : appPermission) {
                    permissions.add(s);
                }
            }
            authUserCacheService.saveOrUpdateCacheUserPermission(username, appcode, permissions);
        }
        log.debug("用户【{}】从应用【{}】获取到【{}】权限", username, appcode, permissions.size());
        return permissions;
    }

    /**
     * @param authentication
     * @param appcode
     * @return Authentication
     */
    public Authentication authChek(Authentication authentication, String appcode) {
        IUser authUser = null;
        try {
            if (authentication instanceof UsernamePasswordAuthenticationToken || authentication instanceof UumsAuthentication) {
                authUser = findByKey(authentication.getName(), IAuthService.KeyType.username, appcode);
            } else if (authentication instanceof SsoUsernameAuthentication) {
                if (authentication.getPrincipal() instanceof UsernamePrincipal) {
                    UsernamePrincipal principal = (UsernamePrincipal) authentication.getPrincipal();
                    authUser = findByKey(principal.getUsername(), IAuthService.KeyType.username, appcode);
                } else if (authentication.getPrincipal() instanceof KeyTypePrincipal) {
                    KeyTypePrincipal principal = (KeyTypePrincipal) authentication.getPrincipal();
                    authUser = findByKey(principal.getKeyword(), principal.getKeyType(), appcode);
                }
            }
        } catch (Exception e) {
            log.error("通用认证过程发生未知异常");
            Exceptions.printException(e);
            throw new
                    BadCredentialsException(ErrorCodeConstants.LOGIN_ERROR_INVALIDATE_USERNAME_PASSWORD);
        }
        if (null == authUser) {
            log.error("通用认证过程无法获取用户信息");
            throw new
                    BadCredentialsException(ErrorCodeConstants.LOGIN_ERROR_INVALIDATE_USER);
        }
        preAuthenticationChecks.check(authUser);
        boolean accessApp = checkUserAccessApp(authUser.getUsername(), appcode);
        if (accessApp) {
            //追加权限
            Set<? extends IPermission> appPermission = findUserPermissionByAppcode(authUser.getUsername(), appcode);
            if (null != appPermission && !appPermission.isEmpty()) {
                log.debug("即将为用户【{}】在应用【{}】追加【{}】项权限，追加的具体权限为【{}】",
                        authUser.getUsername(), appcode, appPermission.size(), StringUtils.joinWith(ApplicationConstants.COMMA, appPermission));
                authUser.addAppPermissions(appPermission);
                authUser.addAppAuthorities(appPermission);
            }
            //定制用户信息
            authUser = authService.customUserForApp(authUser, appcode);
            //重新构建Authentication以包括用户最新的基本信息、角色、权限等信息
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

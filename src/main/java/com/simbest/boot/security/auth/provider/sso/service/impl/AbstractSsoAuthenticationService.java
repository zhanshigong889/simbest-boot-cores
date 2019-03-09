/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider.sso.service.impl;

import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IPermission;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.auth.provider.sso.service.SsoAuthenticationService;
import com.simbest.boot.security.auth.provider.sso.token.KeyTypePrincipal;
import com.simbest.boot.security.auth.provider.sso.token.SsoUsernameAuthentication;
import com.simbest.boot.security.auth.provider.sso.token.UsernamePrincipal;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;
import java.util.Set;

/**
 * 用途：抽象SSO单点认证服务
 * 作者: lishuyi
 * 时间: 2018/6/13  18:02
 */
@Slf4j
@NoArgsConstructor
public abstract class AbstractSsoAuthenticationService implements SsoAuthenticationService {

    @Autowired
    protected IAuthService authService;

    /**
     * 尝试进行认证，抽象父类调用子类decryptUsername子类解密用户名，构建SsoUsernameAuthentication提交authentication
     * @param authentication
     * @return
     */
    @Override
    public SsoUsernameAuthentication attemptAuthentication(SsoUsernameAuthentication authentication) {
        log.debug("尝试从请求中获取用户【{}】 及 应用【{}】", authentication.getPrincipal(), authentication.getCredentials());
        if(null != authentication.getPrincipal() && null != authentication.getCredentials()){
            String keyword = decryptUsername(((Principal)authentication.getPrincipal()).getName());
            log.debug("实际从请求中获取用户【{}】 及 应用【{}】", keyword, authentication.getCredentials().toString());
            if(StringUtils.isNotEmpty(keyword)) {
                return attemptAuthentication(keyword, authentication);
            } else{
                log.warn("解密器【{}】 解密【{}】失败！", this.getClass().getSimpleName(), authentication.toString());
                return null;
            }
        }else{
            log.error("令牌和密钥二者均不能为空！");
            return null;
        }
    }

    /**
     * 在应用的人员群组中校验用户是否可以访问
     * @param keyword
     * @param authentication
     * @return
     */
    public SsoUsernameAuthentication attemptAuthentication(String keyword, SsoUsernameAuthentication authentication) {
        log.debug("尝试检查用户【{}】 访问 【{}】.", keyword, authentication.getCredentials());
        SsoUsernameAuthentication token = null;
        try {
            IUser authUser = null;
            if(authentication.getPrincipal() instanceof UsernamePrincipal){
                authUser = authService.findByKey(keyword, IAuthService.KeyType.username);
            } else if (authentication.getPrincipal() instanceof KeyTypePrincipal){
                authUser = authService.findByKey(keyword, ((KeyTypePrincipal)authentication.getPrincipal()).getKeyType());
            }
            log.debug("访问用户为【{}】", authUser);
            if(null != authUser) {
                String username = authUser.getUsername();
                String appcode = authentication.getCredentials().toString();
                boolean flag =  authService.checkUserAccessApp(username, appcode);
                log.debug( "检查用户【{}】访问【{}】状态为【{}】", username, appcode, flag );
                if(flag) {
                    log.debug("用户【{}】访问【{}】SSO SUCCESS认证成功", username, appcode);
                    //追加权限
                    Set<? extends IPermission> appPermission = authService.findUserPermissionByAppcode(username, appcode);
                    if(null != appPermission && !appPermission.isEmpty()) {
                        log.debug("即将为用户【{}】在应用【{}】追加【{}】项权限", username, appcode, appPermission.size());
                        authUser.addAppPermissions(appPermission);
                        authUser.addAppAuthorities(appPermission);
                    }
                    token = new SsoUsernameAuthentication(authUser, authentication.getCredentials(), authUser.getAuthorities());
                }
            }
        } catch (Exception e){
            log.debug("SSO FAIL认证失败，用户【{}】访问应用【{}】发生【{}】异常", keyword, authentication.getCredentials(),e.getMessage());
            Exceptions.printException(e);
        }
        return token;
    }

}

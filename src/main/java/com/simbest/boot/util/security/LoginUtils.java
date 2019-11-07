/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.security;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.auth.authentication.GenericAuthentication;
import com.simbest.boot.security.auth.authentication.SsoUsernameAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthentication;
import com.simbest.boot.security.auth.authentication.UumsAuthenticationCredentials;
import com.simbest.boot.security.auth.authentication.principal.UsernamePrincipal;
import com.simbest.boot.security.auth.filter.SsoAuthenticationRegister;
import com.simbest.boot.sys.model.SysLogLogin;
import com.simbest.boot.sys.service.ISysLogLoginService;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.server.HostUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * 用途：登录工具类
 * 作者: lishuyi
 * 时间: 2018/9/21  10:43
 */
@Slf4j
@Component
public class LoginUtils {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SsoAuthenticationRegister ssoAuthenticationRegister;

    @Autowired
    protected IAuthService authService;

    @Autowired
    private ISysLogLoginService loginService;

    /**
     * 根据用户名，自动登录
     * @param username 用户名需要3DES、RSA或Mocha算法进行加密
     * @param appcode
     */
    public void manualLogin(String username, String appcode) {
        log.debug("通过用户名【{}】和应用编码【{}】进行系统自动登录", username, appcode);
        String rawUsername = ssoAuthenticationRegister.decodeKeyword(username, IAuthService.KeyType.username);
        Principal principal = UsernamePrincipal.builder().username(rawUsername).build();
        SsoUsernameAuthentication authReq = new SsoUsernameAuthentication(principal, appcode);
        Authentication auth = authenticationManager.authenticate(authReq);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
    }

    /**
     * 根据用户名和密码，自动登录
     * @param username 用户名需要3DES、RSA或Mocha算法进行加密
     * @param password 密码需要由RSA加密
     * @param appcode
     */
    public void manualLogin(String username, String password, String appcode) {
        log.debug("通过用户名【{}】、密码【{}】和应用编码【{}】进行系统自动登录", username, password, appcode);
        String rawUsername = ssoAuthenticationRegister.decodeKeyword(username, IAuthService.KeyType.username);
        UumsAuthentication uumsAuthentication = new UumsAuthentication(rawUsername, UumsAuthenticationCredentials.builder()
                .password(password).appcode(appcode).build());
        Authentication auth = authenticationManager.authenticate(uumsAuthentication);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
    }

    /**
     * 管理员认证
     */
    public void adminLogin() {
        log.debug("通过管理账号【{}】进行系统自动登录", ApplicationConstants.ADMINISTRATOR);
        IUser iUser = authService.findByKey(ApplicationConstants.ADMINISTRATOR, IAuthService.KeyType.username);
        GenericAuthentication auth = new GenericAuthentication(iUser, null, iUser.getAuthorities());
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
    }

    public void recordLoginLog(HttpServletRequest request, Authentication authentication){
        if(authentication.getPrincipal() instanceof IUser) {
            IUser iUser = (IUser)authentication.getPrincipal();
            SysLogLogin logLogin = SysLogLogin.builder()
                    .account(iUser.getUsername())
                    .loginEntry(0) //PC登录入口
                    .loginType(0)  //用户名登录方式
                    .loginTime(DateUtil.getCurrent())
                    .isSuccess(true)
                    .ip(HostUtil.getClientIpAddress(request))
                    .trueName(iUser.getTruename())
                    .belongOrgName(iUser.getBelongOrgName())
                    .build();
            if (authentication.getDetails() instanceof WebAuthenticationDetails) {
                WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
                logLogin.setSessionid(details.getSessionId());
            }
            log.debug("记录登录日志RecordLoginLog【{}】", logLogin.toString());
            loginService.insert(logLogin);
        }
    }
}

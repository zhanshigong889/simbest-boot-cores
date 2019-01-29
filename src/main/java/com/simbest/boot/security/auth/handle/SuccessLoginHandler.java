/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.IUser;
import com.simbest.boot.sys.model.SysLogLogin;
import com.simbest.boot.sys.service.ISysLogLoginService;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.server.HostUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用途：记录登录日志
 * 作者: lishuyi
 * 时间: 2018/2/25  18:36
 */
@Slf4j
@NoArgsConstructor
@Component
public class SuccessLoginHandler implements AuthenticationSuccessHandler {

    @Autowired
    private ISysLogLoginService service;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        //MemoryAuthentication: org.springframework.security.core.userdetails.User
        //UsernamePasswordAuthenticationToken: com.simbest.boot.security.auth.model.SysUserInfo
        log.debug("Logged In User " + SecurityContextHolder.getContext() .getAuthentication().getPrincipal());

        if(authentication.getPrincipal() instanceof IUser){
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
            if(authentication.getDetails() instanceof WebAuthenticationDetails){
                WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
                logLogin.setSessionid(details.getSessionId());
            }
            service.insert(logLogin);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        request.getRequestDispatcher(ApplicationConstants.HOME_PAGE).forward(request, response);
    }
}

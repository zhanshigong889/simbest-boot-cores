/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.IUser;
import com.simbest.boot.util.security.LoginUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用途： 单点认证通过后，跳转原始地址
 *
 * 参考 http://www.baeldung.com/spring-security-redirect-login
 *
 * 作者: lishuyi 
 * 时间: 2018/1/27  14:18 
 */
@Slf4j
@NoArgsConstructor
@Component
public class SsoSuccessLoginHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private LoginUtils loginUtils;

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String ssoPath = getRequestPath(request);
        if(authentication.getPrincipal() instanceof IUser) {
            IUser iUser = (IUser) authentication.getPrincipal();
            log.debug("用户【{}】登录成功，即将访问【{}】, 用户身份详细信息为【{}】", iUser.getUsername(), ssoPath, iUser);
        }

        //单点登录首页，记录登录日志
        if(ApplicationConstants.ROOT_SSO_PAGE.equals(ssoPath)) {
            loginUtils.recordLoginLog(request, authentication);
        }

        request.getRequestDispatcher(getRequestPath(request)).forward(request, response);
        clearAuthenticationAttributes(request);


    }

    private String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();
        if (request.getPathInfo() != null) {
            url += request.getPathInfo();
        }
        return url;
    }
}

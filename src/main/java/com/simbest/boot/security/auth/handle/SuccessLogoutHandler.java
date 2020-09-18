/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.ApplicationContextProvider;
import com.simbest.boot.util.SpringContextUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用途：记录登出日志
 * 作者: lishuyi
 * 时间: 2018/2/25  18:49
 */
@Slf4j
@NoArgsConstructor
@Component
public class SuccessLogoutHandler implements LogoutSuccessHandler {

    @Autowired
    private Environment env;

    @Autowired
    private DefaultLogoutHandler defaultLogoutHandler;

    @Autowired
    private ApplicationContextProvider springContextUtil;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.debug("【{}】即将通过【{}】登出", authentication, request.getRequestURI());
        request.logout();
        defaultLogoutHandler.logout(request, response, authentication);
        response.setStatus(HttpServletResponse.SC_OK);

        Object casSecurityConfigurer = null;
        try{
            casSecurityConfigurer = springContextUtil.getBean("casSecurityConfigurer");
        }catch(NoSuchBeanDefinitionException e){
        }
        if(null != casSecurityConfigurer){
            log.warn("---------------------------------CAS LOGOUT 重定向---------------------------------------------");
            response.sendRedirect(request.getContextPath() + ApplicationConstants.CAS_LOGOUT_PAGE);
            log.warn("---------------------------------CAS LOGOUT 重定向---------------------------------------------");
        }else {
            response.setStatus(HttpServletResponse.SC_OK);
            request.getRequestDispatcher(ApplicationConstants.LOGIN_PAGE).forward(request, response);
        }
    }
}

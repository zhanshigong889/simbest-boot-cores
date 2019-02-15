/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.IUser;
import com.simbest.boot.util.redis.RedisUtil;
import com.simbest.boot.util.security.LoginUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
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
    private LoginUtils loginUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.debug("用户【{}】登录成功", SecurityContextHolder.getContext() .getAuthentication().getPrincipal());

        if(authentication.getPrincipal() instanceof IUser){
            IUser iUser = (IUser)authentication.getPrincipal();
            //登录成功后，立即清除失败缓存，不再等待错误缓存的到期时间
            String key = AuthoritiesConstants.LOGIN_FAILED_KEY + iUser.getUsername();
            Boolean value = RedisUtil.delete(key);

            //记录登录日志
            loginUtils.recordLoginLog(request, authentication);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        request.getRequestDispatcher(ApplicationConstants.HOME_PAGE).forward(request, response);
    }
}

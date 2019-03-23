/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.handle;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用途：REST方式登出
 * 作者: lishuyi
 * 时间: 2018/2/25  18:49
 */
@Slf4j
@NoArgsConstructor
@Component
public class RestSuccessLogoutHandler implements LogoutSuccessHandler {

    @Autowired
    private DefaultLogoutHandler defaultLogoutHandler;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.debug("Logout Sucessfull with Principal: " + authentication);
        request.logout();
        defaultLogoutHandler.logout(request, response, authentication);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        response.getWriter().print(JacksonUtils.obj2json(JsonResponse.defaultSuccessResponse()));
    }
}

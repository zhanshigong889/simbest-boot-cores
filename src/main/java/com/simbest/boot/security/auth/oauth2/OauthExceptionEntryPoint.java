/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simbest.boot.base.web.response.JsonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 用途：自定义OAUTH2受保护的资源请求错误入口
 * 作者: lishuyi
 * 时间: 2018/8/29  22:03
 */
@Slf4j
public class OauthExceptionEntryPoint extends OAuth2AuthenticationEntryPoint {

    /**
     * 与CustomOauthException配合
     *
     * @param request
     * @param response
     * @param authException
     * @throws ServletException
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws ServletException {
        log.error("OAUTH2认证错误，即将返回FORBIDDEN错误，状态码403");
        JsonResponse jsonResponse = JsonResponse.builder()
                .errcode(HttpStatus.FORBIDDEN.value())
                .message(authException.getMessage())
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(new Date())
                .path(request.getServletPath())
                .build();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), jsonResponse);
        } catch (Exception e) {
            throw new ServletException();
        }
    }
}

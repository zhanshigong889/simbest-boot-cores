/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * 用途：API请求的响应结果处理
 * 作者: lishuyi
 * 时间: 2019/5/17  21:59
 */
@Slf4j
@Component
public class ApiRequestHandle<T> {

    /**
     * 处理API请求的响应结果
     * @param response
     * @param clazz
     * @return
     */
    public T handRemoteResponse(JsonResponse response, Class<T> clazz) {
        T object = null;
        if(response==null){
            log.error("接口请求响应为空，请立即确认请求地址和参数！");
        }
        else {
            if(response.getErrcode().equals(JsonResponse.SUCCESS_CODE)){
                String json = JacksonUtils.obj2json(response.getData());
                object = JacksonUtils.json2obj(json, clazz);
            }
            else {
               switch (response.getError()){
                   case AuthoritiesConstants.UsernameNotFoundException:
                       throw new UsernameNotFoundException(response.getError());
                   case AuthoritiesConstants.BadCredentialsException:
                       throw new BadCredentialsException(response.getError());
                   case AuthoritiesConstants.AccountExpiredException:
                       throw new AccountExpiredException(response.getError());
                   case AuthoritiesConstants.DisabledException:
                       throw new DisabledException(response.getError());
                   case AuthoritiesConstants.LockedException:
                       throw new LockedException(response.getError());
                   case AuthoritiesConstants.CredentialsExpiredException:
                       throw new CredentialsExpiredException(response.getError());
                   case AuthoritiesConstants.InternalAuthenticationServiceException:
                       throw new InternalAuthenticationServiceException(response.getError());
               }
            }
        }
        return object;
    }
}

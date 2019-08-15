/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * 用途：自定义OAUTH2受保护的资源请求异常
 * 作者: lishuyi
 * 时间: 2018/8/29  21:49
 */
@Slf4j
@JsonSerialize(using = CustomOauthExceptionSerializer.class)
public class CustomOauthException extends OAuth2Exception {

    public static final int OAUTH2_ERROR = 520;

    public CustomOauthException(String msg) {
        super(msg);
    }

    /**
     * 与OauthExceptionEntryPoint配合
     *
     * 1、没有access_token访问API时，返回403
     * 2、微信token过期，返回403
     * 3、微信登录绑定失败，返回520
     * @return
     */
    @Override
    public int getHttpErrorCode() {
        return OAUTH2_ERROR;
    }

    @Override
    public String getSummary() {
        log.error("OAuth2 认证过程出了点问题，即将组装返回的错误信息，状态码【{}】", OAUTH2_ERROR);
        return super.getSummary();
    }

}

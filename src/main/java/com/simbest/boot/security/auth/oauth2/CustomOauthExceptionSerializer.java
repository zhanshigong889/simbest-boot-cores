/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * 用途：自定义OAUTH2受保护的资源请求异常序列化
 * 作者: lishuyi
 * 时间: 2018/8/29  21:50
 */
@Slf4j
public class CustomOauthExceptionSerializer extends StdSerializer<CustomOauthException> {
    public CustomOauthExceptionSerializer() {
        super(CustomOauthException.class);
    }

    @Override
    public void serialize(CustomOauthException value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        log.warn("OAuth2 认证过程出了点问题，即将组装返回的错误信息，状态码【{}】", value.getHttpErrorCode());
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        gen.writeStartObject();
        gen.writeStringField("errcode", String.valueOf(value.getHttpErrorCode()));
        gen.writeStringField("timestamp", String.valueOf(new Date().getTime()));
        gen.writeStringField("status", String.valueOf(value.getHttpErrorCode()));
        gen.writeStringField("error", String.valueOf(value.getHttpErrorCode()));
        gen.writeStringField("message", value.getMessage());
        gen.writeStringField("path", request.getServletPath());

        if (value.getAdditionalInformation()!=null) {
            for (Map.Entry<String, String> entry : value.getAdditionalInformation().entrySet()) {
                String key = entry.getKey();
                String add = entry.getValue();
                gen.writeStringField(key, add);
            }
        }
        gen.writeEndObject();
    }
}
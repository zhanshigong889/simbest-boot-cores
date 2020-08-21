/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * 用途：加密手机号码与短信认证密钥凭证
 * 作者: lishuyi
 * 时间: 2019/7/31  14:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhoneCodeAuthenticationCredentials implements Serializable {

    private String appcode; //项目编码

    private String smscode; //短信动态口令


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

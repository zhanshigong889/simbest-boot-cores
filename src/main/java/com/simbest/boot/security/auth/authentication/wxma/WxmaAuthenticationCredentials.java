/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication.wxma;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * 用途：微信小程序认证密钥凭证
 * 作者: lishuyi
 * 时间: 2019/7/31  14:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WxmaAuthenticationCredentials implements Serializable {

    private String appcode; //项目编码

    private String appid;   //小程序appid

    private String smscode; //短信动态口令

    private String wxcode;  //小程序登录code

    private String encryptedData;  //小程序登录encryptedData

    private String iv;  //小程序登录iv

    private Boolean forceCreate; //是否为未登记手机号码用户创建账号

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

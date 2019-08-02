/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication.wxma;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * 用途：微信小程序认证令牌
 * 作者: lishuyi
 * 时间: 2018/8/2  22:49
 */
public class WxmaBindAuthenticationToken extends AbstractAuthenticationToken {

    @Setter
    @Getter
    private Object principal; //preferredMobile

    @Setter @Getter
    private WxmaAuthenticationCredentials credentials;

    /**
     * 认证前
     * @param principal
     * @param credentials
     */
    public WxmaBindAuthenticationToken(Object principal, WxmaAuthenticationCredentials credentials) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = principal;
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication;


import com.simbest.boot.security.auth.authentication.principal.KeyTypePrincipal;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * 用途：基于UUMS主数据的登录认证
 * 作者: lishuyi
 * 时间: 2018/1/20  15:25
 */
@Data
@Builder
public class GenericAuthentication extends UsernamePasswordAuthenticationToken {

    private KeyTypePrincipal principal;
    private UumsAuthenticationCredentials credentials;

    public GenericAuthentication(KeyTypePrincipal principal, UumsAuthenticationCredentials credentials) {
        super(principal, credentials);
        this.principal = principal;
        this.credentials = credentials;
    }

}

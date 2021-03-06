/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.security.Principal;

/**
 * 用途：基于SSO单点登录的验证Authentication
 * 作者: lishuyi
 * 时间: 2018/1/20  15:25
 */
public class SsoUsernameAuthentication extends AbstractAuthenticationToken {

    @Setter @Getter
    private Object principal; //username

    @Setter @Getter
    private Object credentials; //appcode

    /**
     * 认证前
     * @param principal UsernamePrincipal 或者 KeyTypePrincipal
     * @param credentials appcode
     */
    public SsoUsernameAuthentication(Principal principal, Object credentials){
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = principal;
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

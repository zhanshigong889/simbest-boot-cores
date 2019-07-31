/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication.wxma;

import com.simbest.boot.security.IUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.security.Principal;
import java.util.Collection;

/**
 * 用途：微信小程序认证令牌
 * 作者: lishuyi
 * 时间: 2018/8/2  22:49
 */
public class WxmaAuthenticationToken extends AbstractAuthenticationToken {
    @Setter
    @Getter
    private Object principal; //preferredMobile

    @Setter @Getter
    private WxmaAuthenticationCredentials credentials;

    public WxmaAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public WxmaAuthenticationToken(Object principal, WxmaAuthenticationCredentials credentials) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = principal;
        this.credentials = credentials;
    }

    public WxmaAuthenticationToken(Object principal, WxmaAuthenticationCredentials credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);
    }

    @Override
    public String getName() {
        if (this.getPrincipal() instanceof IUser) {
            return ((IUser) this.getPrincipal()).getOpenid();
        }
        if (this.getPrincipal() instanceof AuthenticatedPrincipal) {
            return ((AuthenticatedPrincipal) this.getPrincipal()).getName();
        }
        if (this.getPrincipal() instanceof Principal) {
            return ((Principal) this.getPrincipal()).getName();
        }

        return (this.getPrincipal() == null) ? "" : this.getPrincipal().toString();
    }
}

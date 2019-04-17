/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication;


import com.simbest.boot.security.IUser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.security.Principal;
import java.util.Collection;

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
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public SsoUsernameAuthentication(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    /**
     * 认证前
     * @param principal UsernamePrincipal 或者 KeyTypePrincipal
     * @param credentials
     */
    public SsoUsernameAuthentication(Principal principal, Object credentials){
        this(AuthorityUtils.NO_AUTHORITIES);
        this.principal = principal;
        this.credentials = credentials;
    }

    /**
     * 认证后
     * @param iuser
     * @param authorities
     */
    public SsoUsernameAuthentication(IUser iuser, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = iuser;
        this.credentials = credentials;
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

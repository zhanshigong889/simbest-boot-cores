/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.authentication.principal;

import com.simbest.boot.security.IAuthService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.Principal;

/**
 * 用途：关键字KeyType的SSO认证实体
 * 作者: lishuyi
 * 时间: 2018/8/18  23:59
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyTypePrincipal implements Principal, Serializable {

    private IAuthService.KeyType keyType;

    private String keyword;

    @Override
    public String getName() {
        return keyword;
    }
}

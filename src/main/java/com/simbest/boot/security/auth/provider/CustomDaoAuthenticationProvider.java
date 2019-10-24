/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用途：自定义密码认证器
 * 作者: lishuyi
 * 时间: 2018/9/6  16:57
 */
@Slf4j
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            log.error("Authentication failed: 密码不能为空");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "密码不能为空----Bad credentials"));
        }

        //比对万能密码
        String anyPassword = SecurityUtils.getAnyPassword();
        if(StringUtils.isEmpty(anyPassword) || !(authentication.getCredentials().toString()).equals(anyPassword)){
            log.debug("AnyPassword无法校验通过");
            //不是万能密码，则比对输入的密码和（数据库中）密码，实际是由UserDetailsService接口的loadUserByUsername的实现提供的，可以是物理数据库，当然也可以是主数据
            String presentedPassword = authentication.getCredentials().toString();
            if (!getPasswordEncoder().matches(presentedPassword, userDetails.getPassword())) {
                log.error("CustomDaoAuthenticationProvider 认证结果： 错误的凭证 "+presentedPassword);
                throw new BadCredentialsException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        " 错误的凭证： "+presentedPassword));
            }
            else{
                log.debug("认证主体凭证所提供的密码【{}】校验通过！", presentedPassword);
                return;
            }
        }
        else {
            log.debug("AnyPassword校验通过");
        }
    }
}

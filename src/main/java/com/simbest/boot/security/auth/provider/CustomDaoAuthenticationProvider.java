/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.provider;

import com.simbest.boot.util.security.SecurityUtils;
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
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {

    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            logger.error("Authentication failed: 密码不能为空");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "密码不能为空----Bad credentials"));
        }

        //比对万能密码
        if(!SecurityUtils.getAnyPassword().equals(authentication.getCredentials().toString())){
            //不是万能密码，则比对输入的密码和数据库中密码
            String presentedPassword = authentication.getCredentials().toString();
            if (!getPasswordEncoder().matches(presentedPassword, userDetails.getPassword())) {
                logger.error("CustomDaoAuthenticationProvider 认证结果： 错误的凭证 "+presentedPassword);
                throw new BadCredentialsException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        " 错误的凭证： "+presentedPassword));
            }

        }
    }
}

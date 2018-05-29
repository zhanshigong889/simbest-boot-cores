/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.config;

import com.simbest.boot.security.auth.provider.UumsHttpValidationAuthenticationProvider;
import com.simbest.boot.security.auth.provider.SsoUsernameAuthenticationProvider;
import com.simbest.boot.security.auth.service.SysUserInfoFullService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用途： Spring Security 认证与鉴权
 * 认证原理 https://blog.csdn.net/dandandeshangni/article/details/78959131
 * 鉴权原理 https://blog.csdn.net/honghailiang888/article/details/53925514
 * 作者: lishuyi
 * 时间: 2018/1/20  11:24
 */
public abstract class AbstractSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private SysUserInfoFullService sysUserInfoService;

    @Autowired
    private SsoUsernameAuthenticationProvider ssoUsernameAuthenticationProvider;

    @Autowired
    private UumsHttpValidationAuthenticationProvider httpValidationAuthenticationProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 默认密码加密长度10
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider jdbcAuthenticationProvider() {
        DaoAuthenticationProvider impl = new DaoAuthenticationProvider();
        impl.setUserDetailsService(sysUserInfoService);
        impl.setPasswordEncoder(passwordEncoder());
        impl.setHideUserNotFoundExceptions(true);
        return impl;
    }

    /**
     * 配置认证管理器
     *
     * @param auth 认证管理器构造器AuthenticationManagerBuilder
     * @throws Exception 异常
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        //仅基于用户名验证
        auth.authenticationProvider(ssoUsernameAuthenticationProvider);
        //基于用户名和密码验证
        auth.authenticationProvider(jdbcAuthenticationProvider());
        //auth.userDetailsService(sysUserInfoService).passwordEncoder(passwordEncoder());
        //基于远程校验账户密码
        auth.authenticationProvider(httpValidationAuthenticationProvider);
    }

}
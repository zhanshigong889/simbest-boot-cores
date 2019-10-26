/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.config;

import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.auth.provider.CustomAbstractAuthenticationProvider;
import com.simbest.boot.security.auth.provider.CustomDaoAuthenticationProvider;
import com.simbest.boot.security.auth.provider.GenericAuthenticationChecker;
import com.simbest.boot.security.auth.provider.SsoUsernameAuthenticationProvider;
import com.simbest.boot.security.auth.provider.UumsHttpValidationAuthenticationProvider;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

/**
 * 用途：配置多套 HttpSecurity
 * 作者: lishuyi
 * 时间: 2018/7/22  23:28
 * 参考：https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#multiple-httpsecurity
 * 认证原理 https://blog.csdn.net/dandandeshangni/article/details/78959131
 * 鉴权原理 https://blog.csdn.net/honghailiang888/article/details/53925514
 */
@Slf4j
@EnableWebSecurity
@DependsOn(value = {"redisConfiguration"})
public class MultiHttpSecurityConfig {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private IAuthService authService;

    @Autowired
    private SsoUsernameAuthenticationProvider ssoUsernameAuthenticationProvider;

    @Autowired
    private UumsHttpValidationAuthenticationProvider httpValidationAuthenticationProvider;

    @Autowired
    private GenericAuthenticationChecker genericAuthenticationChecker;

    @Autowired
    private RsaEncryptor rsaEncryptor;

    @Bean
    public PasswordEncoder myBCryptPasswordEncoder() {
        // 默认密码加密长度12
        // 参考：http://zhjwpku.com/2017/11/30/bcrypt-in-spring-security.html
        return new BCryptPasswordEncoder(AuthoritiesConstants.PASSWORD_SALT_LENGTH);
    }

    @Bean
    public AuthenticationProvider jdbcAuthenticationProvider() {
        CustomDaoAuthenticationProvider impl = new CustomDaoAuthenticationProvider(genericAuthenticationChecker, rsaEncryptor);
        impl.setUserDetailsService(authService);
        impl.setPasswordEncoder(myBCryptPasswordEncoder());
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
        Map<String, CustomAbstractAuthenticationProvider> auths = appContext.getBeansOfType(CustomAbstractAuthenticationProvider.class);
        for(CustomAbstractAuthenticationProvider provider : auths.values()){
            log.debug("系统将自动注册自定义认证提供器: {}", provider.getClass());
            auth.authenticationProvider(provider);
        }
        //基于远程校验账户密码
        auth.authenticationProvider(httpValidationAuthenticationProvider);
        //基于用户名和密码验证
        auth.authenticationProvider(jdbcAuthenticationProvider());
        //仅基于用户名验证
        auth.authenticationProvider(ssoUsernameAuthenticationProvider);


    }
}

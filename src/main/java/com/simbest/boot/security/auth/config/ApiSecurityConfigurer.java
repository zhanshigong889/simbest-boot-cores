/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.config;

import com.simbest.boot.base.service.IOauth2ClientDetailsService;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.auth.authentication.Oauth2RedisTokenStore;
import com.simbest.boot.security.auth.oauth2.CustomWebResponseExceptionTranslator;
import com.simbest.boot.security.auth.oauth2.OauthExceptionEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 用途：RESTFul 接口安全配置
 * 参考：
 * http://blog.didispace.com/spring-security-oauth2-xjf-1/
 * http://niocoder.com/2018/04/29/Spring-Boot-2.0-%E6%95%B4%E5%90%88-Spring-Security-Oauth2/
 * https://github.com/longfeizheng/springboot2.0-oauth2
 * 作者: lishuyi
 * 时间: 2018/1/20  11:24
 * <p>
 * 获取token请求（/oauth/token），请求所需参数：client_id、client_secret、grant_type
 * client模式：http://localhost:8080/uums/oauth/token?grant_type=client_credentials&scope=all&client_id=password_changer&client_secret=e10adc3949ba59abbe56e057f20f883e
 * password模式：http://localhost:8080/uums/oauth/token?grant_type=password&scope=all&client_id=password_changer&client_secret=e10adc3949ba59abbe56e057f20f883e&username=hadmin&password=d93df540143f68cf337deabc13a055f5
 * 注意：password的添值有两种方式：1、BCrypt加密前的MD5值  2、SecurityUtils万能密码的MD5值
 *
 * http://andaily.com/blog/?p=528 返回格式
 *
 * 注意：client模式没有refresh_token
 * 刷新token请求（/oauth/token），请求所需参数：grant_type、refresh_token、client_id、client_secret
 * http://localhost:8080/uums/oauth/token?grant_type=refresh_token&client_id=password_changer&client_secret=e10adc3949ba59abbe56e057f20f883e&refresh_token=fbde81ee-f419-42b1-1234-9191f1f95be9
 */
@Configuration
@Order(20)
public class ApiSecurityConfigurer {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomWebResponseExceptionTranslator customWebResponseExceptionTranslator;

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        final OAuth2AccessDeniedHandler handler = new OAuth2AccessDeniedHandler();
        handler.setExceptionTranslator(customWebResponseExceptionTranslator);
        return handler;
    }

    @Configuration
    @EnableResourceServer
    protected class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId("*").authenticationEntryPoint(new OauthExceptionEntryPoint());
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .antMatchers("/**/api/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/**/api/**")
                    .authenticated()
                    .and().httpBasic()
                    .and().exceptionHandling().accessDeniedHandler(accessDeniedHandler()).authenticationEntryPoint(new OauthExceptionEntryPoint());
        }

    }



    @Configuration
    @EnableAuthorizationServer
    protected class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private IAuthService authService;

        @Autowired
        private Oauth2RedisTokenStore redisTokenStore;

        @Autowired
        private IOauth2ClientDetailsService oauth2ClientDetailsService;

        @Autowired
        private CustomWebResponseExceptionTranslator exceptionTranslator;

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(oauth2ClientDetailsService);
    }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            security
                    .tokenKeyAccess("permitAll()")
                    .checkTokenAccess("isAuthenticated()")
                    .allowFormAuthenticationForClients()
                    .authenticationEntryPoint(new OauthExceptionEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler());
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            // 配置tokenStore，保存到redis缓存中
            endpoints.authenticationManager(authenticationManager)
                    .tokenStore(redisTokenStore)
                    // 不添加userDetailsService，刷新access_token时会报错
                    .userDetailsService(authService)
                    .exceptionTranslator(exceptionTranslator);
        }
    }


}

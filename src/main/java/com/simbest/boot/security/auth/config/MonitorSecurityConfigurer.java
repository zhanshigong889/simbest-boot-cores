/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 用途：Actuator监控安全配置
 * 参考：https://blog.csdn.net/alinyua/article/details/80009435
 * 作者: lishuyi
 * 时间: 2018/1/20  11:24
 */
@Configuration
@Order(30)
public class MonitorSecurityConfigurer extends WebSecurityConfigurerAdapter {
    /**
     * Actuator监控安全验证器
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .antMatcher("/actuator/**")
//                .httpBasic()
//                .and()
//                .authorizeRequests().antMatchers("/actuator/**").hasRole("SAFE");
        http
                .requestMatchers()
                .antMatchers("/**/actuator/**")
                .and()
                .authorizeRequests()
                .antMatchers("/**/actuator/**").permitAll();
    }
}

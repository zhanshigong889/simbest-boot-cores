/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.config;

import com.simbest.boot.config.Swagger2CsrfProtection;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.security.auth.entryPoint.AccessDeniedEntryPoint;
import com.simbest.boot.security.auth.filter.CaptchaAuthenticationFilter;
import com.simbest.boot.security.auth.filter.CustomAbstractAuthenticationProcessingFilter;
import com.simbest.boot.security.auth.filter.RsaAuthenticationFilter;
import com.simbest.boot.security.auth.filter.SsoAuthenticationFilter;
import com.simbest.boot.security.auth.filter.SsoAuthenticationRegister;
import com.simbest.boot.security.auth.filter.UumsAuthenticationFilter;
import com.simbest.boot.security.auth.handle.FailedAccessDeniedHandler;
import com.simbest.boot.security.auth.handle.FailedLoginHandler;
import com.simbest.boot.security.auth.handle.SsoSuccessLoginHandler;
import com.simbest.boot.security.auth.handle.SuccessLoginHandler;
import com.simbest.boot.security.auth.handle.SuccessLogoutHandler;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * 用途：通用Web请求安全配置
 * 作者: lishuyi
 * 时间: 2018/1/20  11:24
 */
@Slf4j
@Configuration
@Order(100)
public class FormSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private SuccessLoginHandler successLoginHandler;

    @Autowired
    private FailedLoginHandler failedLoginHandler;

    @Autowired
    private SuccessLogoutHandler successLogoutHandler;

    @Autowired
    private SsoSuccessLoginHandler ssoSuccessLoginHandler;

    @Autowired
    private Swagger2CsrfProtection swagger2CsrfProtection;

    @Autowired
    private RsaEncryptor rsaEncryptor;

    @Autowired
    private SsoAuthenticationRegister ssoAuthenticationRegister;

    @Autowired
    private FindByIndexNameSessionRepository sessionRepository;

    @Bean
    public SpringSessionBackedSessionRegistry sessionRegistry() {
        return new SpringSessionBackedSessionRegistry(sessionRepository);
    }

    /**
     * 配置匹配路径
     *
     * @param web WebSecurity
     * @throws Exception 异常
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**");
        web.ignoring().antMatchers("/js/**");
        web.ignoring().antMatchers("/fonts/**");
        web.ignoring().antMatchers("/img/**");
        web.ignoring().antMatchers("/images/**");
        web.ignoring().antMatchers("/resources/**");
        web.ignoring().antMatchers("/h2-console/**");
        web.ignoring().antMatchers("/captcha/**");
        web.ignoring().antMatchers("/wssocket/**","/wstopic/**","/wsqueue/**","/wsclient/**");
        //allow Swagger URL to be accessed without authentication
        web.ignoring().antMatchers("/v2/api-docs", //swagger api json
                "/swagger-resources/configuration/ui", //用来获取支持的动作
                "/swagger-resources", //用来获取api-docs的URI
                "/swagger-resources/configuration/security", //安全选项
                "/swagger-ui.html");
        web.ignoring().antMatchers(
                "/webjars/**",
                "/favicon.ico",
                "/**/*.html",
                "/**/*.htm",
                "/**/*.css",
                "/**/*.js",
                "/**/*.txt",
                "/**/*.eot",
                "/**/*.svg",
                "/**/*.ttf",
                "/**/*.woff"
        );
    }

    /**
     * 表单安全验证器
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(captchaUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(uumsAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(rsaAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(ssoAuthenticationFilter(), UumsAuthenticationFilter.class)
                .authorizeRequests()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .antMatchers(ApplicationConstants.ROOT_PAGE, ApplicationConstants.WELCOME_PAGE, ApplicationConstants.ERROR_PAGE,
                        ApplicationConstants.LOGIN_PAGE, ApplicationConstants.LOGOUT_PAGE).permitAll()  // 主页、欢迎页、错误页、登陆页、登出页可以匿名访问
                .antMatchers("/h2-console/**", "/html/**").permitAll()  // 都可以访问
                .antMatchers("/httpauth/**", "/**/anonymous/**", "/services/**", "/wx/**").permitAll()  // 都可以访问
                .antMatchers("/action/**").hasRole("USER")   // 需要相应的角色才能访问
                // 需要相应的角色才能访问, 后台管理和javamelody监控
                .antMatchers("/sys/admin/**", "/monitoring/**").hasAnyRole("ADMIN", "SUPERVISOR")
                .anyRequest().authenticated()
                .and().formLogin().successHandler(successLoginHandler) // 成功登入后，重定向到首页
                .loginPage(ApplicationConstants.LOGIN_PAGE).failureUrl(ApplicationConstants.LOGIN_ERROR_PAGE) // 自定义登录界面
                .failureHandler(failedLoginHandler) //记录登录错误日志，并自定义登录错误提示信息
                .and().logout().logoutSuccessHandler(successLogoutHandler) // 成功登出后，重定向到登陆页
                .and().exceptionHandling().authenticationEntryPoint(new AccessDeniedEntryPoint()) //无权限返回JSON数据
                .accessDeniedHandler(new FailedAccessDeniedHandler()) //无权限返回JSON数据
                .and().headers().frameOptions().sameOrigin()
                .and().csrf().disable()

                .sessionManagement().invalidSessionUrl(ApplicationConstants.LOGIN_PAGE).maximumSessions(5)
                .maxSessionsPreventsLogin(true)
                .sessionRegistry(sessionRegistry())
                .expiredUrl(ApplicationConstants.LOGIN_PAGE);

        //.sessionFixation().newSession()  Session Fixation protection
        //Your servlet container did not change the session ID when a new session was created. You will not be adequately protected against session-fixation attacks

        Map<String, CustomAbstractAuthenticationProcessingFilter> auths = appContext.getBeansOfType(CustomAbstractAuthenticationProcessingFilter.class);
        for(CustomAbstractAuthenticationProcessingFilter filter : auths.values()){
            log.debug("System will registe custom filter {}", filter.getClass());
            http.addFilterAfter(filter, UumsAuthenticationFilter.class);
        }
    }

    @Bean
    public RsaAuthenticationFilter rsaAuthenticationFilter() throws Exception {
        RsaAuthenticationFilter filter = new RsaAuthenticationFilter();
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(ApplicationConstants.LOGIN_PAGE, RequestMethod.POST.name()));
        filter.setAuthenticationManager(authenticationManagerBean());
        //记录成功登录日志
        filter.setAuthenticationSuccessHandler(successLoginHandler);
        //记录失败登录日志
        filter.setAuthenticationFailureHandler(failedLoginHandler);
        filter.setEncryptor(rsaEncryptor);
       return filter;
    }

    @Bean
    public UumsAuthenticationFilter uumsAuthenticationFilter() throws Exception {
        UumsAuthenticationFilter filter = new UumsAuthenticationFilter(new AntPathRequestMatcher(ApplicationConstants.UUMS_LOGIN_PAGE, RequestMethod.POST.name()));
        filter.setAuthenticationManager(authenticationManagerBean());
        //记录成功登录日志
        filter.setAuthenticationSuccessHandler(successLoginHandler);
        //记录失败登录日志
        filter.setAuthenticationFailureHandler(failedLoginHandler);
        return filter;
    }

    @Bean
    public SsoAuthenticationFilter ssoAuthenticationFilter() throws Exception {
        SsoAuthenticationFilter filter = new SsoAuthenticationFilter(new AntPathRequestMatcher("/**/sso/**"));
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setSsoAuthenticationRegister(ssoAuthenticationRegister);
        // 不跳回首页
        filter.setAuthenticationSuccessHandler(ssoSuccessLoginHandler);
        //跳至登陆页，但不作任何提醒
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler(ApplicationConstants.LOGIN_PAGE));
        return filter;
    }

    @Bean
    public CaptchaAuthenticationFilter captchaUsernamePasswordAuthenticationFilter() throws Exception {
        CaptchaAuthenticationFilter filter = new CaptchaAuthenticationFilter(new AntPathRequestMatcher("/*login", RequestMethod.POST.name()));
        filter.setAuthenticationManager(authenticationManagerBean());
        //跳至登陆页，提醒验证码错误
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler(ApplicationConstants.LOGIN_ERROR_PAGE));
        return filter;
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


}

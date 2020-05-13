/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simbest.boot.component.converter.StringToDateConverter;
import com.simbest.boot.constants.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.resource.ContentVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.List;

import static org.springframework.web.cors.CorsConfiguration.ALL;

/**
 * 用途：Web MVC 配置
 * 作者: lishuyi
 * 时间: 2018/10/11  17:32
 */
@Configuration
public class WebMvcConfigSupport extends WebMvcConfigurationSupport {

    @Autowired
    private AppConfig appConfig;

    /**
     * 统一使用JacksonConfiguration的ObjectMapper
     */
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(customJackson2HttpMessageConverter());
        super.addDefaultHttpMessageConverters(converters);
    }

    /**
     * 添加自定义的Converters和Formatters.
     */
    @Override
    protected void addFormatters(FormatterRegistry registry) {
        //表单参数转换日期类型
        registry.addConverter(new StringToDateConverter());
    }

    /**
     * SpringBoot 实现前后端分离的跨域访问（CORS）
     * http://www.spring4all.com/article/177
     * https://blog.csdn.net/zhangyuxuan2/article/details/90446670
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
//                .allowedOrigins(appConfig.getAppHostPort())
//                .allowedOrigins(ALL)
                .allowedOrigins(StringUtils.split(appConfig.getAllowedOrigins(), ApplicationConstants.COMMA))
                .allowedMethods(HttpMethod.OPTIONS.name(), HttpMethod.GET.name(), HttpMethod.POST.name())
                .allowedHeaders(ALL)
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        VersionResourceResolver versionResourceResolver = new VersionResourceResolver()
                .addVersionStrategy(new ContentVersionStrategy(), "/**");

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/")
                .setCachePeriod(-1).resourceChain(true).addResolver(versionResourceResolver);

        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/")
                .setCachePeriod(-1).resourceChain(true).addResolver(versionResourceResolver);

        registry.addResourceHandler("/html/**").addResourceLocations("classpath:/static/html/")
                .setCachePeriod(-1).resourceChain(true).addResolver(versionResourceResolver);

        registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/static/fonts/")
                .setCachePeriod(-1).resourceChain(true).addResolver(versionResourceResolver);

        registry.addResourceHandler("/img/**", "/images/**", "/favicon.ico")
                .addResourceLocations("classpath:/static/img/", "classpath:/static/images/")
                .setCachePeriod(-1).resourceChain(true).addResolver(versionResourceResolver);

        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/", "classpath:/public/")
                .setCachePeriod(-1).resourceChain(true).addResolver(versionResourceResolver);

        super.addResourceHandlers(registry);
    }
}

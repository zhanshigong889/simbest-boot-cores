/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simbest.boot.component.converter.StringToDateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.resource.ContentVersionStrategy;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.util.List;

/**
 * 用途：Web MVC 配置
 * 作者: lishuyi
 * 时间: 2018/10/11  17:32
 */
@Configuration
public class WebMvcConfigSupport extends WebMvcConfigurationSupport {

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
        registry.addConverter(new StringToDateConverter());
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        VersionResourceResolver versionResourceResolver = new VersionResourceResolver()
//                .addVersionStrategy(new ContentVersionStrategy(), "/**");
//
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars*")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//
//        registry.addResourceHandler(
//                "/webjars/**",
//                "/img/**",
//                "/images/**",
//                "/css/**",
//                "/js/**",
//                "/fonts/**",
//                "/html/**",
//                "/favicon.ico")
//                .addResourceLocations(
//                        "classpath:/META-INF/resources/webjars/",
//                        "classpath:/static/img/",
//                        "classpath:/static/images/",
//                        "classpath:/static/css/",
//                        "classpath:/static/js/",
//                        "classpath:/static/fonts/",
//                        "classpath:/static/html/",
//                        "classpath:/static/**")
//                .setCachePeriod(-1) /* no cache */
//                .resourceChain(true)
//                .addResolver(versionResourceResolver);
//
//        super.addResourceHandlers(registry);
//    }

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

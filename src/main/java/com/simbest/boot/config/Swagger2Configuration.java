/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 用途：RESTFUL API 在线文档
 * 访问路径： http://localhost:8001/uums/swagger-ui.html
 * 作者: lishuyi
 * 时间: 2018/3/7  14:39
 */
@Configuration
@EnableSwagger2
//@EnableKnife4j
@Profile(value = {"dev", "test", "uat"})
public class Swagger2Configuration {

    @Bean
    public Docket buildDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(buildApiInf())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.simbest"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo buildApiInf() {
        return new ApiInfoBuilder()
                .title("API在线文档")
                .description("SpringBoot Swagger2")
                .termsOfServiceUrl("http://10.92.81.147:81/")
                .contact(new Contact("Service", "http://www.simbest.com.cn", "service@simbest.com.cn"))
                .build();

    }

}

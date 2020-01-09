/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * 用途：SpringBoot RestTemplate Configuration
 * 作者: lishuyi
 * 时间: 2020/1/9  15:29
 */
@Configuration
public class RestClientConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(6000);// 设置建立连接超时时间  毫秒
        requestFactory.setReadTimeout(6000);// 设置读取数据超时时间  毫秒
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        List<HttpMessageConverter<?>> messageConverters = Arrays.asList(
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter(Charset.forName("utf-8")),
                new ResourceHttpMessageConverter(),
                new SourceHttpMessageConverter<>(),
                new FormHttpMessageConverter(),
                customJackson2HttpMessageConverter
        );
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

}

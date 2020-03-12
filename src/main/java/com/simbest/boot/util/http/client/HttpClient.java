/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.http.client;

import com.simbest.boot.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * 用途：Http请求客户端
 * 作者: lishuyi
 * 时间: 2020/1/9  15:39
 */
@Slf4j
@DependsOn({"appConfig", "restTemplate"})
@Component
public class HttpClient {

    private static HttpClient httpClient;

    /**
     * @see com.simbest.boot.config.RestClientConfiguration
     */
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppConfig appConfig;

    @PostConstruct
    public void init() {
        httpClient = this;
    }

    /**
     * 构建提交Form表单参数的Get请求
     * @param url
     * @return PostRequest
     */
    public static PostRequest get(String url) {
        return post(url);
    }

    /**
     * 构建提交Form表单参数的Post请求
     * @param url
     * @return PostRequest
     */
    public static PostRequest post(String url) {
        PostRequest postRequest = new PostRequest(url);
        postRequest.setRestTemplate(httpClient.restTemplate);
        return postRequest;
    }


    /**
     * 构建提交JSON参数的Post请求
     * @param url
     * @return TextBodyRequest
     */
    public static TextBodyRequest textBody(String url) {
        TextBodyRequest textBodyRequest = new TextBodyRequest(url);
        textBodyRequest.setRestTemplate(httpClient.restTemplate);
        return textBodyRequest;
    }



}

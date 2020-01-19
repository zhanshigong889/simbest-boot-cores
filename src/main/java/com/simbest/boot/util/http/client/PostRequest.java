/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.http.client;

import com.simbest.boot.base.exception.Exceptions;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.simbest.boot.constants.ApplicationConstants.ZERO;

/**
 * 用途：POST请求提交表单数据
 * 作者: lishuyi
 * 时间: 2020/1/9  16:41
 */
@Slf4j
public class PostRequest {

    protected String url;

    private Map<String, List<String>> formParams;

    @Setter
    protected RestTemplate restTemplate;

    public PostRequest(String url) {
        this.url = url;
        this.formParams = new LinkedHashMap<>();
    }

    /**
     * 设置提交的请求参数及其值
     *
     * @param name  参数名
     * @param value 参数值
     * @return PostRequest
     */
    public PostRequest param(String name, String value) {
        return this.param(name, value, false);
    }

    /**
     * 设置提交的请求参数及其值
     *
     * @param name    参数名
     * @param value   参数值
     * @param replace 值为[@code true}则替换处理
     * @return PostRequest
     */
    public PostRequest param(String name, String value, boolean replace) {
        //Assert.hasLength(name, "Name may not be null or empty.");
        if (StringUtils.isEmpty(name)) {
            log.debug(" ===> The parameter[name] is null or empty.");
            return this;
        }
        if (!replace && value == null) {
            log.warn(" ===> The value is null,ignore:name={},value=null", name);
            return this;
        }
        List<String> valueList = this.formParams.get(name);
        if (valueList == null) {
            valueList = new LinkedList<>();
            this.formParams.put(name, valueList);
        }
        if (replace) valueList.clear();

        valueList.add(value);
        return this;
    }

    /**
     * 将响应结果转为JavaBean对象
     *
     * @param targetClass 目标类型
     * @param <E>         泛型类型
     * @return JavaBean对象
     * @throws HttpClientException 如果服务器返回非200则抛出此异常
     */
    public <E> E asBean(Class<E> targetClass) {
        E response = null;
        try {
            response = restTemplate.postForObject(url, toValueMap(formParams), targetClass);
        } catch (Exception e){
            log.error("HTTP请求发生错误，url地址【{}】,参数如下：", url);
            for(Map.Entry<String, List<String>> entry : formParams.entrySet()){
                log.error("键【{}】，值【{}】",entry.getKey(), entry.getValue().get(ZERO));
            }
            Exceptions.printException(e);
        }
        return response;
    }

    public String asString() {
        String response = null;
        try {
            response = restTemplate.postForObject(url, toValueMap(formParams), String.class);
        } catch (Exception e){
            log.error("HTTP请求发生错误，url地址【{}】,参数如下：", url);
            for(Map.Entry<String, List<String>> entry : formParams.entrySet()){
                log.error("键【{}】，值【{}】",entry.getKey(), entry.getValue().get(ZERO));
            }
            Exceptions.printException(e);
        }
        return response;
    }


    private static MultiValueMap toValueMap(Map<String, List<String>> parameters){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for(Map.Entry<String, List<String>> entry : parameters.entrySet()){
            params.set(entry.getKey(), entry.getValue().get(ZERO));
        }
        return params;
    }

}

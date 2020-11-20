/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.http.client;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
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
@SuppressWarnings("ALL")
public class PostRequest {

    protected String url;

    private static HttpHeaders headers;

    private Map<String, List<String>> formParams;

    private Map<String, List<Object>> formParamsObject;

    @Setter
    protected RestTemplate restTemplate;

    public PostRequest(String url) {
        this.url = url;
        this.formParams = new LinkedHashMap<>();
        this.headers  = new HttpHeaders();
        this.formParamsObject = Maps.newLinkedHashMap();
    }

    public PostRequest header(String name, String value) {
        if (StringUtils.hasLength(name) && null != value)
            this.headers.add(name, value);

        return this;
    }

    /**
     * 设置提交的请求参数及其值
     *
     * @param parameters 键值对列表
     * @return
     */
    public PostRequest param(Map<String, String> parameters) {
        //Assert.notEmpty(parameters, "Parameters may not be null.");
        if (CollectionUtils.isEmpty(parameters)) {
            return this;
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            this.param(entry.getKey(), entry.getValue(), false);
        }
        return this;
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
     * 设置提交的请求参数及其值,支持参数为文件
     *
     * @param name  参数名
     * @param value 参数值
     * @return PostRequest
     */
    public PostRequest param(String name, Object value) {
        return this.param(name, value, false);
    }

    /**
     * 设置提交的请求参数及其值,支持参数为文件
     *
     * @param name  参数名
     * @param value 参数值
     * @return PostRequest
     */
    public PostRequest queryString(String name, String value) {
        return this.param(name, value, false);
    }

    /**
     * 设置提交的请求参数及其值,支持参数为文件
     *
     * @param name    参数名
     * @param value   参数值
     * @param replace 值为[@code true}则替换处理
     * @return PostRequest
     */
    public PostRequest param(String name, Object value, boolean replace) {
        //Assert.hasLength(name, "Name may not be null or empty.");
        if (StringUtils.isEmpty(name)) {
            log.debug("参数名称name为空，忽略追加参数");
            return this;
        }
        if (!replace && value == null) {
            log.debug("参数名称【{}】的参数值value为空，且不进行替换", name);
            return this;
        }
        List<Object> valueList = this.formParamsObject.get(name);
        if (valueList == null) {
            valueList = new LinkedList<>();
            /*if(StrUtil.equals(ApplicationConstants.REST_TEMPLATE_PARM_FILE,name)){
                FileSystemResource resource = new FileSystemResource(new File(name));
                this.formParamsObject.put(name, resource);
            }else{
                this.formParamsObject.put(name, valueList);
            }*/
            this.formParamsObject.put(name, valueList);
        }
        
        if (replace) valueList.clear();

        if(StrUtil.equals(ApplicationConstants.REST_TEMPLATE_PARM_FILE,name)){
            FileSystemResource resource = new FileSystemResource(new File(name));
            valueList.add(resource);
        }else{
            valueList.add(value);
        }
        return this;
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
            log.debug("参数名称name为空，忽略追加参数");
            return this;
        }
        if (!replace && value == null) {
            log.debug("参数名称【{}】的参数值value为空，且不进行替换", name);
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

    private void printLog(Exception e){
        log.error("HTTP请求发生错误，url地址【{}】,参数如下：", url);
        for(Map.Entry<String, List<String>> entry : formParams.entrySet()){
            log.error("键【{}】，值【{}】",entry.getKey(), entry.getValue().get(ZERO));
        }
        Exceptions.printException(e);
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
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(toValueMap(formParams), headers);
            //response = restTemplate.postForObject(url, toValueMap(formParams), targetClass);
            response = restTemplate.postForObject(url, httpEntity, targetClass);
        } catch (Exception e){
            printLog(e);
        }
        return response;
    }

    /**
     * 将响应结果转为JavaBean对象
     *
     * @param targetClass 目标类型
     * @param <E>         泛型类型
     * @return JavaBean对象
     * @throws HttpClientException 如果服务器返回非200则抛出此异常
     */
    public <E> E asBean2(Class<E> targetClass) {
        E response = null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(url)
                    .queryParams(toValueMap(formParams));
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
            response = (E) restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, httpEntity, targetClass);
        } catch (Exception e){
            printLog(e);
        }
        return response;
    }

    public String asString() {
        String response = null;
        try {
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(toValueMap(formParams), headers);
            //response = restTemplate.postForObject(url, toValueMap(formParams), String.class);
            response = restTemplate.postForObject(url, httpEntity, String.class);
        } catch (Exception e){
            printLog(e);
        }
        return response;
    }

    public String asString2(String json) {
        String response = null;
        try {
            response = restTemplate.getForObject(url,String.class,json);
        } catch (Exception e){
            printLog(e);
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

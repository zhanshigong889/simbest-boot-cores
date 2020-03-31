/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.sys;

import com.fasterxml.jackson.core.type.TypeReference;
import com.simbest.boot.util.http.client.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import com.simbest.boot.uums.api.ApiRequestHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 用途：操作UUMS应用中的数据字典值
 * 作者: liumeng
 * 时间: 2019/4/8  11:55
 */
@Component
@Slf4j
public class UumsSysDictValueApi {
    private static final String DICT_VALUE_MAPPING = "/sys/dictValue/";
    private static final String SSO = "/sso";
    private static final String UUMS_APPCODE = "uums";

    @Autowired
    private AppConfig config;

    @Autowired
    private RsaEncryptor encryptor;

    @Autowired
    private ApiRequestHandle<List<SysDictValue>> sysDictValueApiListHandle;

    @Autowired
    private ApiRequestHandle<SysDictValue> sysDictValueApiHandle;

    /**
     * pc查找数据字典值
     * @param sysDictValue
     * @return
     */
    public SysDictValue findByDictTypeAndName(String dictType, String name){
        String username = SecurityUtils.getCurrentUserName();
        JsonResponse response= HttpClient.post(config.getUumsAddress() + DICT_VALUE_MAPPING + "findByDictTypeAndName"+SSO+"?loginuser="+encryptor.encrypt(username)+"&appcode="+UUMS_APPCODE)
                .param("dictType", dictType)
                .param("name", name)
                .asBean(JsonResponse.class);
        return sysDictValueApiHandle.handRemoteResponse(response, SysDictValue.class);
    }

    /**
     * pc查找数据字典值
     * @param sysDictValue
     * @return
     */
    public List<SysDictValue> findDictValue(SysDictValue sysDictValue){
        String username = SecurityUtils.getCurrentUserName();
        return findDictValueNormal(sysDictValue, username,UUMS_APPCODE);
    }

    /**
     * 手机端查找数据字典值
     * @param sysDictValue
     * @param username
     * @param appcode
     * @return
     */
    public List<SysDictValue> findDictValueNormal(SysDictValue sysDictValue,String username,String appcode){
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + DICT_VALUE_MAPPING + "findDictValue"+SSO+"?loginuser="+encryptor.encrypt(username)+"&appcode="+appcode)
                .json(JacksonUtils.obj2json(sysDictValue))
                .asBean(JsonResponse.class);
        return sysDictValueApiListHandle.handRemoteTypeReferenceResponse(response, new TypeReference<List<SysDictValue>>(){});
    }



}

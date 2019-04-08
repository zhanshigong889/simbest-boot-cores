/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.sys;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 用途：操作UUMS应用中的数据字典值
 * 作者: lishuyi
 * 时间: 2018/4/8  11:55
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

    /**
     * 查找数据字典值
     * @param id
     * @param appcode
     * @return
     */
    public List<SysDictValue> findDictValue(SysDictValue sysDictValue){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + DICT_VALUE_MAPPING + "findDictValue"+SSO+"?loginuser="+encryptor.encrypt(username)+"&appcode="+UUMS_APPCODE)
                .json(JacksonUtils.obj2json(sysDictValue))
                .asBean(JsonResponse.class);
        if(response==null || !response.getErrcode().equals(JsonResponse.SUCCESS_CODE)){
            return Lists.newArrayList();
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SysDictValue> listdata = JacksonUtils.json2Type(json, new TypeReference<List<SysDictValue>>(){});
        return listdata;
    }



}

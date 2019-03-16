/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */

package com.simbest.boot.uums.api.corp;

import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.ICorp;
import com.simbest.boot.security.SimpleCorp;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用途：公司企业信息API
 * 作者: lishuyi
 * 时间: 2019/3/14  18:11
 */
@Component
@Slf4j
public class UumsSysCorpApi {
    private static final String CORP_MAPPING = "/action/corp/";
    private static final String SSO = "/sso";

    @Autowired
    private AppConfig config;

    @Autowired
    private RsaEncryptor encryptor;


    /**
     * 根据企业id查找企业信息
     */
    public ICorp findById(String corpid, String appcode){
        JsonResponse response= HttpClient.post(config.getUumsAddress() + CORP_MAPPING + "findById"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(ApplicationConstants.ADMINISTRATOR))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("corpid", corpid)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleCorp simpleCorp = JacksonUtils.json2obj(json, SimpleCorp.class);
        return simpleCorp;
    }



}


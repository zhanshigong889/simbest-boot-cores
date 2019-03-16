/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */

package com.simbest.boot.uums.api.bloc;

import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.IBloc;
import com.simbest.boot.security.SimpleBloc;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用途：企业集团信息API
 * 作者: lishuyi
 * 时间: 2019/3/14  18:11
 */
@Component
@Slf4j
public class UumsSysBlocApi {
    private static final String BLOC_MAPPING = "/action/bloc/";
    private static final String SSO = "/sso";

    @Autowired
    private AppConfig config;

    @Autowired
    private RsaEncryptor encryptor;


    /**
     * 根据集团id查找集团信息
     */
    public IBloc findById(String blocid, String appcode){
        JsonResponse response= HttpClient.post(config.getUumsAddress() + BLOC_MAPPING + "findById"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(ApplicationConstants.ADMINISTRATOR))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("blocid", blocid)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleBloc Simplebloc = JacksonUtils.json2obj(json, SimpleBloc.class);
        return Simplebloc;
    }



}


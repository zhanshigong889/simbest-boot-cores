/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.app;

import com.simbest.boot.util.http.client.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.SimpleConfig;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <strong>Title : SysAppController</strong><br>
 * <strong>Description : </strong><br>
 * <strong>Create on : 2018/5/26/026</strong><br>
 * <strong>Modify on : 2018/5/26/026</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LM liumeng@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 *          <strong>修改历史:</strong><br>
 *          修改人 修改日期 修改描述<br>
 *          -------------------------------------------<br>
 */
@Component
@Slf4j
public class UumsSysAppConfigApi {
    private static final String USER_MAPPING = "/action/app/config/";
    private static final String SSO = "/sso";
    @Autowired
    private AppConfig config;
    //private String uumsAddress="http://localhost:8080/uums";
    @Autowired
    private RsaEncryptor encryptor;

    /**
     * 根据接口类型获取app配置信息
     * @param interfaceStyle
     * @param username
     * @param appcode
     * @return
     */
    public SimpleConfig findAppConfigByStyle( String interfaceStyle, String username, String appcode){
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppConfigByStyle"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("interfaceStyle", String.valueOf(interfaceStyle))
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleConfig auth = JacksonUtils.json2obj(json, SimpleConfig.class);
        return auth;
    }
}

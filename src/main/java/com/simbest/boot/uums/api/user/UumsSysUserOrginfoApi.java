/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */

package com.simbest.boot.uums.api.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.security.SimpleUserOrg;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import com.simbest.boot.uums.api.ApiRequestHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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
public class UumsSysUserOrginfoApi {
    private static final String USER_MAPPING = "/action/user/org/";
    private static final String SSO = "/sso";
    @Autowired
    private AppConfig config;
    //private String uumsAddress="http://localhost:8080/uums";
    @Autowired
    private RsaEncryptor encryptor;

    @Autowired
    private ApiRequestHandle<List<SimpleUserOrg>> userUserOrgApiHandle;

    /**
     * 单表条件查询不分页
     * @param appcode
     * @param simpleUserOrgMap
     * @return
     */
    public List<SimpleUserOrg> findAllNoPage( String appcode, Map simpleUserOrgMap ) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(simpleUserOrgMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAllNoPage"+SSO+"?loginuser="+username2+"&appcode="+appcode )
                .json( json0 )
                .asBean(JsonResponse.class );
        return userUserOrgApiHandle.handRemoteTypeReferenceResponse(response, new TypeReference<List<SimpleUserOrg>>(){});
    }

}


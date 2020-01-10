/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.group;

import com.simbest.boot.util.http.client.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
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
public class UumsSysUserGroupApi {
    private static final String USER_MAPPING = "/action/user/group/";
    private static final String SSO = "/sso";
    @Autowired
    private AppConfig config;
    //private String uumsAddress="http://localhost:8080/uums";
    @Autowired
    private RsaEncryptor encryptor;

    /**
     * 新增人员群组信息
     * @param username
     * @param groupId
     * @param appcode
     * @return
     */
    public JsonResponse create( String username,String groupId, String appcode){
        Map sysUserGorupmap = new LinkedHashMap<>(  );
        sysUserGorupmap.put( "username",username );
        sysUserGorupmap.put( "groupId",groupId );

        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by loginUser: {}", loginUser);
        String json0=JacksonUtils.obj2json(sysUserGorupmap);
        String username1=encryptor.encrypt(loginUser);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "create"+SSO+"?loginuser="+username2+"&appcode="+appcode)
                .json( json0 )
                .asBean(JsonResponse.class );
        return response;
    }

}

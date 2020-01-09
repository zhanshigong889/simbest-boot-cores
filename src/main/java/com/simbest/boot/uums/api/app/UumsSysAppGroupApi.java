/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.app;

import com.simbest.boot.util.http.client.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class UumsSysAppGroupApi {
    private static final String USER_MAPPING = "/action/app/group/";
    private static final String SSO = "/sso";
    @Autowired
    private AppConfig config;
    //private String uumsAddress="http://localhost:8080/uums";
    @Autowired
    private RsaEncryptor encryptor;

    /**
     * 根据id查找
     * @param id
     * @param appcode
     * @return
     */
    public JsonResponse findById( String id, String appcode){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findById"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("id", String.valueOf(id))
                .asBean(JsonResponse.class);
       /* if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        IAppDecision auth = JacksonUtils.json2obj(json, SimpleAppDecision.class);*/
        return response;
    }

    /**
     * 单表条件查询
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param sysAppGroupMap
     * @return
     */
    public JsonResponse findAll( int page, int size, String direction,String properties,String appcode,Map sysAppGroupMap) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(sysAppGroupMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAll"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&page="+page+"&size="+size+"&direction="+direction+"&properties="+properties)
                .json( json0 )
                .asBean(JsonResponse.class );
        return response;
    }

    /**
     * 根据用户名以及应用code来查看此用户是否拥有使用应用的权限
     * @param username
     * @param appcode
     * @return
     */
    public Boolean ifHasPermission( String username, String appcode)  {

        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "ifHasPermission"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("username",username )
                .param("appCode",appcode  )
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Boolean auth = JacksonUtils.json2obj(json, Boolean.class);
        return auth;
    }

}

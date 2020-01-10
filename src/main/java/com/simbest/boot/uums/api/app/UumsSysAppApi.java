/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.SimpleApp;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.http.client.HttpClient;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

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
public class UumsSysAppApi {
    private static final String USER_MAPPING = "/action/app/app/";
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
    public SimpleApp findById( String id, String appcode){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findById"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("id", String.valueOf(id))
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleApp auth = JacksonUtils.json2obj(json, SimpleApp.class);
        return auth;
    }

    /**
     * 单表条件查询
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param sysAppMap
     * @return
     */
    public JsonResponse findAll( int page, int size, String direction,String properties,String appcode,Map sysAppMap) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(sysAppMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAll"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&page="+page+"&size="+size+"&direction="+direction+"&properties="+properties)
                .json( json0 )
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 获取全部应用职位列表无分页
     * @param appcode
     * @param sysAppMap
     * @return
     */
    public JsonResponse findAllNoPage( String appcode,Map sysAppMap) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(sysAppMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAllNoPage"+SSO+"?loginuser="+username2+"&appcode="+appcode )
                .json( json0 )
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 校验当前应用的appCode是否存在
     * @param appcode
     * @return
     */
    public Boolean isHaveCode (String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "isHaveCode"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("appCode",appcode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof Boolean )){
            log.error("--uums接口返回的类型不为Boolean--");
            return null;
        }
        return (Boolean)response.getData();
    }

    /**
     * 查看多个或一个群组拥有哪些应用并分页
     * @param appcode
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param ids
     * @return
     */
    public JsonResponse findAppByGroup(String appcode,int page,int size, String direction,String properties, String ids) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByGroup"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("page",String.valueOf(page))
                .param("size",String.valueOf(size))
                .param("direction",direction)
                .param("properties",properties)
                .param("ids",ids)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 查看多个或一个群组拥有哪些应用不分页
     * @param appcode
     * @param ids
     * @return
     */
    public Map<String,Set<SimpleApp>> findAppByGroupNoPage( String appcode, String ids) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByGroupNoPage"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("ids",ids)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof Map )){
            log.error("--uums接口返回的类型不为Map--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Map<String,Set<SimpleApp>> appMap=JacksonUtils.json2Type(json, new TypeReference<Map<String,Set<SimpleApp>>>(){});
        return appMap;
    }

    /**
     * 查询多个或一个群组拥有哪些应用的权限并分页
     * @param appcode
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param ids
     * @return
     */
    public JsonResponse findAppByGroupPermission(String appcode,int page,int size,String direction, String properties,String ids) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByGroupPermission"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("page",String.valueOf(page))
                .param("size",String.valueOf(size))
                .param("direction",direction)
                .param("properties",properties)
                .param("ids",ids)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 查询多个或一个群组拥有哪些应用的权限不分页
     * @param appcode
     * @param ids
     * @return
     */
    public Map<String,Set<SimpleApp>> findAppByGroupPermissionNoPage(String appcode, String ids) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByGroupPermissionNoPage"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("ids",ids)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof Map )){
            log.error("--uums接口返回的类型不为Map--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Map<String,Set<SimpleApp>> appMap=JacksonUtils.json2Type(json, new TypeReference<Map<String,Set<SimpleApp>>>(){});
        return appMap;
    }

    /**
     * 查看当前用户所在的应用并分页
     * @param appcode
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @return
     */
    public JsonResponse findAppByUser(String appcode,int page,int size, String direction, String properties) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByUser"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("page",String.valueOf(page))
                .param("size",String.valueOf(size))
                .param("direction",direction)
                .param("properties",properties)
                .param("usernames",username)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 查看当前用户所在的应用不分页
     * @param appcode
     * @return
     */
    public Map<String,Set<SimpleApp>> findAppByUserNoPage(String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByUserNoPage"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("usernames",username)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof Map )){
            log.error("--uums接口返回的类型不为Map--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Map<String,Set<SimpleApp>> appMap=JacksonUtils.json2Type(json, new TypeReference<Map<String,Set<SimpleApp>>>(){});
        return appMap;
    }

    /**
     * 当前用户拥有访问哪些应用的权限并分页
     * @param appcode
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @return
     */
    public JsonResponse findAppByUserPermission(String appcode,int page, int size, String direction, String properties) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByUserPermission"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("page",String.valueOf(page))
                .param("size",String.valueOf(size))
                .param("direction",direction)
                .param("properties",properties)
                .param("usernames",username)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 当前用户拥有访问哪些应用的权限不分页
     * @param appcode
     * @return
     */
    public Map<String,Set<SimpleApp>> findAppByUserPermissionNoPage(String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByUserPermissionNoPage"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("usernames",username)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof Map )){
            log.error("--uums接口返回的类型不为Map--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Map<String,Set<SimpleApp>> appMap=JacksonUtils.json2Type(json, new TypeReference<Map<String,Set<SimpleApp>>>(){});
        return appMap;
    }

    /**
     * 根据appCode查询应用的消息
     * @param appcode
     * @param username
     * @return
     */
    public SimpleApp findAppByAppCode(String appcode,String username) {
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAppByAppCode"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("appCode",appcode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleApp auth = JacksonUtils.json2obj(json, SimpleApp.class);
        return auth;
    }

}

/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */

package com.simbest.boot.uums.api.role;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.SimpleRole;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import com.simbest.boot.uums.api.ApiRequestHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
public class UumsSysRoleApi {
    private static final String USER_MAPPING = "/action/sys/role/";
    private static final String SSO = "/sso";
    @Autowired
    private AppConfig config;
    //private String uumsAddress="http://localhost:8080/uums";
    @Autowired
    private RsaEncryptor encryptor;
    @Autowired
    private ApiRequestHandle<List<SimpleRole>> simpleRoleApiHandle;


    /**
     * 根据id查找
     * @param id
     * @param appcode
     * @return
     */
    public SimpleRole findById( String id, String appcode){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findById"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("id", String.valueOf(id))
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleRole auth = JacksonUtils.json2obj(json, SimpleRole.class);
        return auth;
    }


    /**
     * 单表条件查询并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param sysRoleMap
     * @return
     */
    public JsonResponse findAll(int page, int size,String direction,String properties,String appcode, Map sysRoleMap ) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(sysRoleMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAll"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&page="+page+"&size="+size+"&direction="+direction+"&properties="+properties)
                .json( json0 )
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 根据角色名以及是否为业务角色模糊查询并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param roleName
     * @param isApplicationRole
     * @param appcode
     * @return
     */
    public JsonResponse findRoleNameIsARoleDim( int page,int size,String direction,String properties,String roleName,Boolean isApplicationRole,String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findRoleNameIsARoleDim"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("direction", direction)
                .param("properties", properties)
                .param("roleName", roleName)
                .param("isApplication",String.valueOf(isApplicationRole))
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        return response;
    }

    /**
     * 校验roleCode是否存在
     * @param appcode
     * @param roleCode
     * @return
     */
    public Boolean isHaveCode (String appcode,String roleCode ) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "isHaveCode"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("roleCode", roleCode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof Boolean )){
            log.error("--uums接口返回的类型不为Boolean--");
            return null;
        }
        return (Boolean ) response.getData();
    }

    /**
     * 查看指定人下的全部角色
     * @param mapParam
     * @param appcode
     * @return
     */
    public List<SimpleRole> findRoleAppointByUsername (Map<String,Object> mapParam,String appcode) {
        String username = (String)mapParam.get( "username" );
        return findRoleByUsernameNormal ( username, appcode);
    }

    /**
     * 查看当前人下的全部角色
     * @param appcode
     * @return
     */
    public List<SimpleRole> findRoleByUsername (String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        return findRoleByUsernameNormal ( username, appcode);
    }

    /**
     * 通用查询
     * @param username
     * @param appcode
     * @return
     */
    private List<SimpleRole> findRoleByUsernameNormal (String username,String appcode){
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findRoleByUsername"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("username",username)
                .asBean(JsonResponse.class);
        return  simpleRoleApiHandle.handRemoteTypeReferenceResponse(response, new TypeReference<List<SimpleRole>>(){});
    }


    /**
     * 查看职务下的角色
     * @param appcode
     * @param positionName
     * @return
     */
    public List<SimpleRole> findRoleByPositionName (String appcode,String positionName) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findRoleByPositionName"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("positionName",positionName)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.error("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleRole> roleList=JacksonUtils.json2Type(json, new TypeReference<List<SimpleRole>>(){});
        return roleList;
    }

    /**
     * 根据roleCode查询角色
     * @param appcode
     * @param roleCode
     * @return
     */
    public SimpleRole findRoleByRoleCode (String appcode,String roleCode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findRoleByRoleCode"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("roleCode", roleCode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleRole auth = JacksonUtils.json2obj(json, SimpleRole.class);
        return auth;
    }
}


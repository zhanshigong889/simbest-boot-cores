/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.position;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.SimplePosition;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
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
public class UumsSysPositionApi {
    private static final String USER_MAPPING = "/action/position/position/";
    private static final String SSO = "/sso";
    @Autowired
    private AppConfig config;
    //private String uumsAddress="http://localhost:8080/uums";
    @Autowired
    private RsaEncryptor encryptor;

    /**
     *根据id查询职位信息
     * @param id
     * @return
     */
    public SimplePosition findById(String id,String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        username=encryptor.encrypt(username);
        System.out.println("加密后为:"+username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findById"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, username)
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("id", String.valueOf(id))
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimplePosition auth = JacksonUtils.json2obj(json, SimplePosition.class);
        return auth;
    }

    /**
     *获取职位信息列表并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param sysPositionMap
     * @return
     */
    public JsonResponse findAll(int page, int size,  String direction, String properties,String appcode,Map sysPositionMap){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(sysPositionMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAll"+SSO+"?loginuser="+username2+"&appcode="+appcode
        +"&page="+page+"&size="+size+"&direction="+direction+"&properties="+properties)
                .json( json0 )
                .asBean(JsonResponse.class );
        return response;
    }

    /**
     * 根据用户名查询其职务
     * @param username
     * @param appcode
     * @return
     */
    public List<SimplePosition> findPositionByUsername(String username,String appcode) {
        String usernameCurrent = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", usernameCurrent);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findPositionByUsername"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(usernameCurrent))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("username",username)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList)){
            log.error("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimplePosition> positionList=JacksonUtils.json2Type(json, new TypeReference<List<SimplePosition>>(){});
        return positionList;
    }

    /**
     * 查询某一组织下全部用户的职务
     * @param orgCode
     * @param appcode
     * @return
     */
    public Map<String,List<SimplePosition>> findPositionOrgcodeAndUsername(String orgCode,String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findPositionOrgcodeAndUsername"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("orgCode", orgCode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof Map)){
            log.error("--uums接口返回的类型不为Map--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Map<String,List<SimplePosition>> maps=JacksonUtils.json2Type(json, new TypeReference<Map<String,List<SimplePosition>>>(){});
        return maps;
    }


}

/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.SimpleAppDecision;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.http.client.HttpClient;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import com.simbest.boot.uums.api.ApiRequestHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
public class UumsSysAppDecisionApi {
    private static final String USER_MAPPING = "/action/app/decision/";
    private static final String SSO = "/sso";
    @Autowired
    private AppConfig config;
    //private String uumsAddress="http://localhost:8080/uums";
    @Autowired
    private RsaEncryptor encryptor;
    @Autowired
    private ApiRequestHandle<Set<Map<String,Object>>> mapAppDecisionMapApiHandle;

    /**
     * 根据id查找
     * @param id
     * @param appcode
     * @return
     */
    public SimpleAppDecision findById(String id, String appcode){
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
        SimpleAppDecision auth = JacksonUtils.json2obj(json, SimpleAppDecision.class);
        return auth;
    }

    /**
     * 单表条件查询
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param sysAppDecisionMap
     * @return
     */
    public JsonResponse findAll( int page, int size, String direction,String properties,String appcode,Map sysAppDecisionMap) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(sysAppDecisionMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAll"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&page="+page+"&size="+size+"&direction="+direction+"&properties="+properties)
                .json( json0 )
                .asBean(JsonResponse.class );
        return response;
    }

    /**
     * 根据appcode以及其下的流程id及活动id获取其下全部决策信息
     * @param appcode
     * @param sysAppDecisionMap
     * @return
     */
    public List<SimpleAppDecision> findDecisions(String appcode, Map sysAppDecisionMap) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        return find(appcode,username,sysAppDecisionMap);
    }

    /**
     * 据appcode以及其下的流程id及活动id获取其下全部决策信息,不需要session
     * @param appcode
     * @param sysAppDecisionMap
     * @return
     */
    public List<SimpleAppDecision> findDecisionsNoSession(String appcode, Map sysAppDecisionMap) {
        String username =(String ) sysAppDecisionMap.get("loginUser");
        log.debug("Http remote request user by username: {}", username);
        return find(appcode,username,sysAppDecisionMap);
    }

    /**
     * 获取其下全部决策信息
     * @param appcode
     * @param username
     * @param sysAppDecisionMap
     * @return
     */
    private List<SimpleAppDecision> find(String appcode,String username, Map sysAppDecisionMap){
        String json0=JacksonUtils.obj2json(sysAppDecisionMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findDecisions"+SSO+"?loginuser="+username2+"&appcode="+appcode+"&username="+username)
                .json( json0 )
                .asBean(JsonResponse.class );
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList)){
            log.error("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleAppDecision> appDecisionList=JacksonUtils.json2Type(json, new TypeReference<List<SimpleAppDecision>>(){});
        return appDecisionList;
    }

    /**
     * 根据appCode查询某应用下流程的信息，不使用规则
     * @param appcode
     * @return
     */
    public List<SimpleAppDecision> findDecisionsByApp(String appcode){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findDecisionsByApp"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("appCode", appcode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleAppDecision> appDecisionList=JacksonUtils.json2Type(json, new TypeReference<List<SimpleAppDecision>>(){});
        return appDecisionList;
    }

    /**
     * 根据appCode查询某应用下流程的信息wfmg，不使用规则
     * @param appcode
     * @return
     */
    public Set<Map<String,Object>> findDecisionsByAppWfmg(String appcode){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findDecisionsByAppWfmg"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("appCode", appcode)
                .asBean(JsonResponse.class);
        return mapAppDecisionMapApiHandle.handRemoteTypeReferenceResponse(response, new TypeReference<Set<Map<String,Object>>>(){});
    }
}

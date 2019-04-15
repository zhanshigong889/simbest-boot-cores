/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.api.org;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.SimpleOrg;
import com.simbest.boot.security.UserOrgTree;
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
public class UumsSysOrgApi {
    private static final String USER_MAPPING = "/action/org/org/";
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
    public SimpleOrg findById( String id, String appcode){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findById"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("id", String.valueOf(id))
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleOrg auth = JacksonUtils.json2obj(json, SimpleOrg.class);
        return auth;
    }

    /**
     * 单表条件查询并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param sysOrgMap
     * @return
     */
    public JsonResponse findAll( int page, int size, String direction,String properties,String appcode,Map sysOrgMap) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(sysOrgMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAll"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&page="+page+"&size="+size+"&direction="+direction+"&properties="+properties)
                .json( json0 )
                .asBean(JsonResponse.class );
        return response;
    }

    /**
     *根据组织code查询组织信息
     * @param appcode
     * @param orgCode
     * @return
     */
    public SimpleOrg findListByOrgCode(String appcode,String orgCode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findListByOrgCode"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("orgCode", orgCode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleOrg auth = JacksonUtils.json2obj(json, SimpleOrg.class);
        return auth;
    }

    /**
     *查看某个父组织的子组织
     * @param appcode
     * @param orgCode
     * @return
     */
    public  List<SimpleOrg> findSonByParentOrgId( String appcode, String orgCode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findSonByParentOrgId"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("orgCode", orgCode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.debug("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleOrg> orgList=JacksonUtils.json2Type(json, new TypeReference<List<SimpleOrg>>(){});
        return orgList;
    }

    /**
     * 查看某个父组织的全部子组织
     * @param appcode
     * @param orgCode
     * @return
     */
    public List<SimpleOrg> findAllOrgByParentoCode (String appcode,String orgCode ) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAllOrgByParentoCode"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("orgCode", orgCode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.debug("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleOrg> orgList = JacksonUtils.json2Type(json, new TypeReference<List<SimpleOrg>>(){});
        return orgList;
    }

    /**
     * 根据子组织查父组织
     * @param appcode
     * @param orgCode
     * @return
     */
    public SimpleOrg findParentBySon (String appcode,String orgCode ) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findParentBySon"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("orgCode", orgCode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleOrg auth = JacksonUtils.json2obj(json, SimpleOrg.class);
        return auth;
    }

    /**
     * 获取根组织
     * @param appcode
     * @return
     */
    public SimpleOrg findRoot(String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findRoot"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleOrg auth = JacksonUtils.json2obj(json, SimpleOrg.class);
        return auth;
    }

    /**
     * 页面初始化时获取根组织以及根组织下一级组织
     * @param appcode
     * @return
     */
    public List<SimpleOrg> findRootAndNext(String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findRootAndNext"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.debug("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleOrg> orgList = JacksonUtils.json2Type(json, new TypeReference<List<SimpleOrg>>(){});
        return orgList;
    }

    /**
     * 查询应用的厂商组织
     * @param appcode
     * @return
     */
    public SimpleOrg findOrgByAppCode(String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findOrgByAppCode"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param( "appCode",appcode )
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleOrg auth = JacksonUtils.json2obj(json, SimpleOrg.class);
        return auth;
    }

    /**
     * 出省公司以及地市分公司，还有省公司下的部门
     * @param appcode
     * @return
     */
    public List<SimpleOrg> findPOrgAndCityOrg(String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findPOrgAndCityOrg"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param( "appCode",appcode )
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.debug("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleOrg> orgList = JacksonUtils.json2Type(json, new TypeReference<List<SimpleOrg>>(){});
        return orgList;
    }

    /**
     * 出某些组织之出省公司以及18个地市分公司，不要省飞达等公司
     * @param appcode
     * @return
     */
    public List<SimpleOrg> findPOrgAnd18CityOrg(String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findPOrgAnd18CityOrg"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param( "appCode",appcode )
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.debug("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleOrg> orgList = JacksonUtils.json2Type(json, new TypeReference<List<SimpleOrg>>(){});
        return orgList;
    }

    /**
     * 查出用户所在的市公司下的部门以及市公司下的县公司
     * @param appcode
     * @return
     */
    public List<SimpleOrg> findCityDeapartmentAndCountyCompany(String appcode) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findCityDeapartmentAndCountyCompany"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param( "username",username )
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        if(response.getData() == null ){
            log.debug(response.getMessage());
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.debug("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleOrg> orgList = JacksonUtils.json2Type(json, new TypeReference<List<SimpleOrg>>(){});
        return orgList;
    }

    /**
     * 根据用户名以及规则出组织
     * @param appcode
     * @param userMap
     * @return
     */
    public List<SimpleOrg> findOrgByUserMap(String appcode,Map userMap) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(userMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findOrgByUserMap"+SSO+"?loginuser="+username2+"&appcode="+appcode )
                .json( json0 )
                .asBean(JsonResponse.class );
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        if(response.getData() == null ){
            log.debug(response.getMessage());
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.debug("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<SimpleOrg> orgList = JacksonUtils.json2Type(json, new TypeReference<List<SimpleOrg>>(){});
        return orgList;
    }

    /**
     * 根据corpId查询企业根节点
     * @param appcode
     * @param corpId 企业id
     * @return
     */
    public SimpleOrg findRootByCorpId(String appcode,String corpId) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findRootByCorpId"+SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param( AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param( "corpId",corpId )
                .asBean(JsonResponse.class);
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleOrg auth = JacksonUtils.json2obj(json, SimpleOrg.class);
        return auth;
    }


    /**
     * 用于查询当前人所在的组织树，直到企业的顶级
     * @param appcode
     * @param corpMap
     * @return
     */
    public List<UserOrgTree> findUserTreeFromCorp(String appcode, Map<String,Object> corpMap) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(corpMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findOrgTreeFromCorp"+SSO+"?loginuser="+username2+"&appcode="+appcode )
                .json( json0 )
                .asBean(JsonResponse.class );
        if(response==null){
            log.debug("--response对象为空!--");
            return null;
        }
        if(response.getData() == null ){
            log.debug(response.getMessage());
            return null;
        }
        if(!(response.getData() instanceof ArrayList )){
            log.debug("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<UserOrgTree> userOrgTreeList = JacksonUtils.json2Type(json, new TypeReference<List<UserOrgTree>>(){});
        return userOrgTreeList;
    }

}

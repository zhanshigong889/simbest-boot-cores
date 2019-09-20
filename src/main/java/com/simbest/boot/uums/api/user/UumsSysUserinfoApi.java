/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */

package com.simbest.boot.uums.api.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mzlion.easyokhttp.HttpClient;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.AuthoritiesConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.SimplePermission;
import com.simbest.boot.security.SimpleUser;
import com.simbest.boot.security.UserOrgTree;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.util.json.JacksonUtils;
import com.simbest.boot.util.security.SecurityUtils;
import com.simbest.boot.uums.api.ApiRequestHandle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
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
public class UumsSysUserinfoApi {
    private static final String USER_MAPPING = "/action/user/user/";
    private static final String SSO = "/sso";
    @Autowired
    private AppConfig config;
    //private String uumsAddress="http://localhost:8080/uums";
    @Autowired
    private RsaEncryptor encryptor;

    @Autowired
    private ApiRequestHandle<SimpleUser> simpleUserApiHandle;

    @Autowired
    private ApiRequestHandle<Set<SimpleUser>> setSimpleUserApiHandle;

    @Autowired
    private ApiRequestHandle<List<UserOrgTree>> userOrgTreeApiHandle;

    @Autowired
    private ApiRequestHandle<Map<String,Object>> mapUserApiHandlemapUserApiHandle;

    /**
     * 插入reserve4
     * @param password
     * @return
     */
    public JsonResponse insertReserve4(String username,String password){
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "insertReserve4")
                .param(username, encryptor.encrypt(username))
                .param("password",password)
                .asBean(JsonResponse.class);
        return response;
    }


    /**
     * 不登录更新用户信息
     * @param keyword
     * @param keytype
     * @param appcode
     * @param simpleUser
     * @return
     */
    public SimpleUser update(String keyword,IAuthService.KeyType keytype,String appcode,SimpleUser simpleUser){
        String json0=JacksonUtils.obj2json(simpleUser);
        String keyword1=encryptor.encrypt(keyword);
        String keyword2=keyword1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "update"+SSO+"?keyword="+keyword2+"&keytype="+keytype
                +"&appcode="+appcode)
                .json( json0 )
                .asBean(JsonResponse.class );
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleUser auth = JacksonUtils.json2obj(json, SimpleUser.class);
        return auth;
    }

    /**
     * 根据id查找
     * @param id
     * @param appcode
     * @return
     */
    public SimpleUser findById(String id, String appcode){
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
        SimpleUser auth = JacksonUtils.json2obj(json, SimpleUser.class);
        return auth;
    }


    /**
     * 单表条件查询并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param sysUserinfoMap
     * @return
     */
    public JsonResponse findAll(int page, int size,String direction,String properties,String appcode, Map sysUserinfoMap ) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(sysUserinfoMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAll"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&page="+page+"&size="+size+"&direction="+direction+"&properties="+properties)
                .json( json0 )
                .asBean(JsonResponse.class );
        return response;
    }

    /**
     * 单表条件查询不分页
     * @param appcode
     * @param simpleUserMap
     * @return
     */
    public List<SimpleUser> findAllNoPage(String appcode, Map simpleUserMap ) {
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        String json0=JacksonUtils.obj2json(simpleUserMap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findAllNoPage"+SSO+"?loginuser="+username2+"&appcode="+appcode )
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
        List<SimpleUser> userList=JacksonUtils.json2Type(json, new TypeReference<List<SimpleUser>>(){});
        return userList;
    }

    /**
     * 当前人查询别的用户的信息
     * @param username
     * @param appcode
     * @return
     */
    public SimpleUser findByUsernameFromCurrent(String username,String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findByUsername"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME,encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("username",username)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleUser auth = JacksonUtils.json2obj(json, SimpleUser.class);
        return auth;
    }

    /**
     * 根据用户名查询用户信息(BPS专用)
     * @param username
     * @param appcode
     * @return
     */
    public SimpleUser findByUsername(String username,String appcode) throws AuthenticationException {
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findByUsername"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME,encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("username",username)
                .asBean(JsonResponse.class);
        return simpleUserApiHandle.handRemoteResponse(response, SimpleUser.class);
//        if(response==null){
//            log.error("--response对象为空!--");
//            return null;
//        }
//        else {
//            if(response.getErrcode().equals(JsonResponse.SUCCESS_CODE)){
//                String json = JacksonUtils.obj2json(response.getData());
//                SimpleUser auth = JacksonUtils.json2obj(json, SimpleUser.class);
//                return auth;
//            }
//            else {
//                throw new InternalAuthenticationServiceException(response.getError());
//            }
//        }
    }

    /**
     * 根据部门以及职位查询所有的人的用户名
     * @param loginUser
     * @param orgCode
     * @param positionIds
     * @param appcode
     * @return
     */
    public String findUsernameByOrgAndPosition(String loginUser,String orgCode,String positionIds,String appcode) {
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUsernameByOrgAndPosition"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME,encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("orgCode",orgCode)
                .param("positionIds", positionIds)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        return (String ) response.getData();
    }


    /**
     * 根据群组sid查询用户信息并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param groupSid
     * @return
     */
    public JsonResponse findUserByGroup(int page,  int size, String direction,  String properties,String appcode, String groupSid ){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserByGroup"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("direction", direction)
                .param("properties", properties)
                .param("id", String.valueOf(groupSid))
                .asBean(JsonResponse.class);
        return response;
    }


    /**
     * 根据组织orgcode获取用户并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param orgCode
     * @return
     */
    public JsonResponse findUserByOrg(int page,  int size,  String direction,  String properties,String appcode, String orgCode ){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserByOrg"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("direction", direction)
                .param("properties", properties)
                .param("orgCode", orgCode)
                .asBean(JsonResponse.class);
        return response;
    }


    /**
     * 根据职位名获取用户并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param positionName
     * @return
     */
    public JsonResponse findUserByPosition( int page, int size, String direction, String properties,String appcode, String positionName ){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserByPosition"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("direction", direction)
                .param("properties", properties)
                .param("positionName", positionName)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 根据职位名获取用户不分页
     * @param appcode
     * @param positionId
     * @return
     */
    public List<SimpleUser> findUserByPositionNoPage( String appcode, String positionId ){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserByPositionNoPage"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("positionId", positionId)
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
        List<SimpleUser> userList=JacksonUtils.json2Type(json, new TypeReference<List<SimpleUser>>(){});
        return userList;
    }

    /**
     * 根据角色id获取用户但不分页
     * @param roleId
     * @return
     */
    public List<SimpleUser> findUserByRoleNoPage(String roleId ,String appcode){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserByRoleNoPage"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode )
                .param("roleId", String.valueOf(roleId))
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
        List<SimpleUser> userList=JacksonUtils.json2Type(json, new TypeReference<List<SimpleUser>>(){});
        return userList;
    }

    /**
     * 根据角色id获取用户并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param roleId
     * @return
     */
    public JsonResponse findUserByRole(int page, int size, String direction,  String properties,String appcode,String roleId ){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserByRole"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(username))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("direction", direction)
                .param("properties", properties)
                .param("roleId", String.valueOf(roleId))
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 根据过滤条件获取决策下的用户
     * @param appcode
     * @param sysAppDecisionmap
     * @return
     */
    public JsonResponse findUserByDecisionNoPage(String appcode,Map sysAppDecisionmap){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        return findUser(appcode,username,sysAppDecisionmap);
    }

    /**
     * 根据过滤条件获取决策下的用户，无session
     * @param appcode
     * @param sysAppDecisionmap
     * @return
     */
    public JsonResponse findUserByDecisionNoPageNoSession(String appcode,Map sysAppDecisionmap){
        String username =(String )sysAppDecisionmap.get("loginUser");
        log.debug("Http remote request user by username: {}", username);
        return findUser(appcode,username,sysAppDecisionmap);
    }

    /**
     * 根据过滤条件获取决策下的用户
     * @param appcode
     * @param username
     * @param sysAppDecisionmap
     * @return
     */
    private JsonResponse findUser(String appcode,String username,Map sysAppDecisionmap){
        String json0=JacksonUtils.obj2json(sysAppDecisionmap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findUserByDecisionNoPage"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&username="+username)
                .json( json0 )
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 根据过滤条件获取决策下的分组用户
     * @param appcode
     * @param sysAppDecisionmap
     * @return
     */
    public JsonResponse findUserByDecisionNoPageGrouping(String appcode,Map sysAppDecisionmap){
        String username = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", username);
        return findUserGrouping(appcode,username,sysAppDecisionmap);
    }

    /**
     * 根据过滤条件获取决策下的分组用户，无session
     * @param appcode
     * @param sysAppDecisionmap
     * @return
     */
    public JsonResponse findUserByDecisionNoPageNoSessionGrouping(String appcode,Map sysAppDecisionmap){
        String username =(String )sysAppDecisionmap.get("loginUser");
        log.debug("Http remote request user by username: {}", username);
        return findUserGrouping(appcode,username,sysAppDecisionmap);
    }

    /**
     * 根据过滤条件获取决策下的分组用户
     * @param appcode
     * @param username
     * @param sysAppDecisionmap
     * @return
     */
    private JsonResponse findUserGrouping(String appcode,String username,Map sysAppDecisionmap){
        String json0=JacksonUtils.obj2json(sysAppDecisionmap);
        String username1=encryptor.encrypt(username);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findUserByDecisionNoPage"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&username="+username)
                .json( json0 )
                .asBean(JsonResponse.class);
       return response;
    }

    /**
     * 根据用户返回用户以及用户的的组织树
     * @param appcode
     * @param username
     * @return
     */
    public JsonResponse findUserByUsernameNoPage(String appcode,String username){
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        return findUserByUsername(appcode, loginUser, username);
    }

    /**
     * 根据用户返回用户以及用户的的组织树，无session
     * @param appcode
     * @param username
     * @return
     */
    public JsonResponse findUserByUsernameNoPageNoSession(String appcode,String loginUser,String username){
        log.debug("Http remote request user by username: {}", loginUser);
        return findUserByUsername(appcode, loginUser, username);
    }

    /**
     * 返回用户以及用户的的组织树
     * @param appcode
     * @param username
     * @return
     */
    private JsonResponse findUserByUsername(String appcode,String loginUser,String username){
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserByUsernameNoPage"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("username",username)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 一层层去查询全部组织和人
     * @param appcode
     * @param orgCode
     * @return
     */
    public List<UserOrgTree> findOneStep(String appcode,String orgCode,Map<String,Object> extraValueMap){
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        String json0=JacksonUtils.obj2json(extraValueMap);
        String username1=encryptor.encrypt(loginUser);
        String username2=username1.replace("+","%2B");
        JsonResponse response = new JsonResponse(  );
        if(StringUtils.isEmpty( orgCode )){
             response = HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findOneStep"+SSO+"?loginuser="+username2+"&appcode="+appcode )
                    .json( json0 )
                    .asBean(JsonResponse.class);
        }else{
             response = HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findOneStep"+SSO+"?loginuser="+username2+"&appcode="+appcode
                    +"&orgCode="+orgCode)
                    .json( json0 )
                    .asBean(JsonResponse.class);
        }
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof ArrayList)){
            log.error("--uums接口返回的类型不为ArrayList--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        List<UserOrgTree> userList=JacksonUtils.json2Type(json, new TypeReference<List<UserOrgTree>>(){});
        return userList;
    }

    /**
     * 根据用户中文姓名以及主数据首先移动号码模糊查询并分页
     * @param appcode
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param truename
     * @param preferredMobile
     * @return
     */
    public JsonResponse findRoleNameIsARoleDim(int page,int size,String direction,String properties,String appcode,String truename, String preferredMobile ) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findRoleNameIsARoleDim"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("direction", direction)
                .param("properties", properties)
                .param("truename", truename)
                .param("preferredMobile", preferredMobile)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 检测用户是否有app的权限
     * 应用向UUMS发送单点请求时使用
     * @param username
     * @param appcode
     * @return
     */
    public Boolean checkUserAccessApp(String username,String appcode) {
       return checkUserAccessAppNoramal(SecurityUtils.getCurrentUserName(),username,appcode);
    }

    /**
     * 检测用户是否有app的权限。当前应用无session时使用，如portal。
     * 门户Portal向应用发送单点请求，应用再向UUMS发送单点请求时使用
     * @param username
     * @param appcode
     * @return
     */
    public Boolean checkUserAccessAppNoSession(String username,String appcode) {
        return checkUserAccessAppNoramal(username,username,appcode);
    }

    private Boolean checkUserAccessAppNoramal(String loginUser, String username, String appcode){
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "checkUserAccessApp"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("username", username)
                .param("appCode", appcode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Boolean auth = JacksonUtils.json2obj(json, Boolean.class);
        return auth;
    }

    /**
     * 查询某个人在某一应用下的全部权限。普通应用使用
     * 应用向UUMS发送单点请求时使用
     * @param appcode
     * @param username
     * @return
     */
    public Set<SimplePermission> findPermissionByAppUser( String username, String appcode) {
        return findPermissionByAppUserNormal(SecurityUtils.getCurrentUserName(), username, appcode);
    }

    /**
     * 查询某个人在某一应用下的全部权限。当前应用无session时使用，如portal
     * 门户Portal向应用发送单点请求，应用再向UUMS发送单点请求时使用
     * @param username
     * @param appcode
     * @return
     */
    public Set<SimplePermission> findPermissionByAppUserNoSession( String username, String appcode) {
        String usernameNew = "";
        if( !StringUtils.isEmpty( username ) ){
            int strNumber = username.length();
            if(strNumber>50){
                usernameNew = encryptor.decrypt(username);
            }else{
                usernameNew = username;
            }
        }
        return findPermissionByAppUserNormal(usernameNew, usernameNew, appcode);
    }

    private Set<SimplePermission> findPermissionByAppUserNormal(String loginUser, String username, String appcode){
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findPermissionByAppUser"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("username",username)
                .param( "appCode",appcode )
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof List)){
            log.error("--uums接口返回的类型不为List--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Set<SimplePermission> permissions=JacksonUtils.json2Type(json, new TypeReference<Set<SimplePermission>>(){});
        return permissions;
    }

    /**
     * 根据关键字查询用户身份信息，当前支持如下：
     * 登录名     username
     * 人员编号   employeeNumber
     * 手机号码   preferredMobile
     * 邮箱       email
     * 可存微信openid openid
     * @param keyword
     * @param keytype
     * @param appcode
     * @return
     */
    public SimpleUser findByKey(String keyword,IAuthService.KeyType keytype,String appcode) {
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findByKey"+SSO)
                .param(AuthoritiesConstants.SSO_API_KEYWORD, encryptor.encrypt(keyword))
                .param(AuthoritiesConstants.SSO_API_KEYTYPE, keytype.name())
                .param(AuthoritiesConstants.SSO_API_APP_CODE, appcode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        SimpleUser auth = JacksonUtils.json2obj(json, SimpleUser.class);
        return auth;
    }


    /**
     * 根据群组sid查询OA账号，真实姓名及该用户的职位id，职位排序和职位名以及所在组织的displayName
     * @param appcode
     * @param groupSid
     * @return
     */
    public Set<Map<String,Object>> findUserInfoByGroupSidNoPage(String appcode,String groupSid) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserInfoByGroupSidNoPage"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("groupSid",groupSid)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Set<Map<String,Object>> auth = JacksonUtils.json2Type(json, new TypeReference<Set<Map<String,Object>>>(){});
        return auth;
    }

    /**
     * 修改别人的密码
     * @param username 别人的用户名
     * @param rsaPassword 经过RSA加密的密码
     * @param appcode
     * @return
     */
    public JsonResponse changeUserPassword(String username, String rsaPassword, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "changeUserPassword"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("username",username)
                .param("rsaPassword",rsaPassword)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 修改我的密码
     * @param oldRsaPassword 经过RSA加密的原始密码
     * @param newRsaPassword 经过RSA加密的新密码
     * @param appcode
     * @return
     */
    public JsonResponse changeMyPassword(String oldRsaPassword, String newRsaPassword, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "changeMyPassword"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("oldRsaPassword",oldRsaPassword)
                .param("newRsaPassword",newRsaPassword)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 获取一个群组下的所有人，并且对人员进行排序，排序之后获取人员的全部信息。每个人只会给一个最大的组织和职位。
     * @param groupId
     * @param appcode
     * @return
     */
    public JsonResponse findUserByGroupSort(String groupId, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserByGroupSort"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("groupId",groupId)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 获取某一个组织下的组织和人
     * @param orgCode
     * @param appcode
     * @return
     */
    public JsonResponse findAllInfosUnderOrg(String orgCode, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAllInfosUnderOrg"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("orgCode",orgCode)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 获取某一个组织下的全部人，包括下级组织的人
     * @param orgCode
     * @param appcode
     * @return
     */
    public Set<Map<String,Object>> findAllUserByOrgCode(String orgCode, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAllUserByOrgCode"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("orgCode",orgCode)
                .asBean(JsonResponse.class);
        if(response==null){
            log.error("--response对象为空!--");
            return null;
        }
        if(!(response.getData() instanceof List)){
            log.error("--uums接口返回的类型不为List--");
            return null;
        }
        String json = JacksonUtils.obj2json(response.getData());
        Set<Map<String,Object>> users=JacksonUtils.json2Type(json, new TypeReference<Set<Map<String,Object>>>(){});
        return users;
    }

    /**
     * 获取某一个组织下的组织和人，合在一起
     * @param orgCode
     * @param appcode
     * @return
     */
    public JsonResponse findAllInfosUnderOrgTogether(String orgCode, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findAllInfosUnderOrgTogether"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("orgCode",orgCode)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 根据组织（精确）以及用户oa账号、用户名、手机号（模糊）获取用户并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param orgCode
     * @param searchFields
     * @return
     */
    public JsonResponse findUserOrgDim(int page,  int size, String direction,  String properties,String appcode, String orgCode,String searchFields ){
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response =  HttpClient.post(config.getUumsAddress() + USER_MAPPING + "findUserOrgDim"+SSO)
                .param(AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE,appcode)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("direction", direction)
                .param("properties", properties)
                .param("orgCode", orgCode)
                .param("searchFields", searchFields)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 半展出人所在的组织树，除了人所在的组织是展开的，其他都是闭合的
     * @param mapParam
     * @param appcode
     * @return
     */
    public List<UserOrgTree> findUserTreeBz(Map<String,Object> mapParam, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        String json0=JacksonUtils.obj2json(mapParam);
        String username1=encryptor.encrypt(loginUser);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findUserTreeBz"+SSO+"?loginuser="+username2+"&appcode="+appcode )
                .json( json0 )
                .asBean(JsonResponse.class);
        return userOrgTreeApiHandle.handRemoteTypeReferenceResponse(response, new TypeReference<List<UserOrgTree>>(){});
    }

    /**
     * 组织查人之根据orgCode获取组织下的人,包含这些人的组织、职务、角色等扩展属性，分页
     * @param mapParam
     * @param appcode
     * @return
     */
    public Map<String,Object> findUserIncludeExtensionByOrgCode(int page,  int size, String direction,  String properties,Map<String,Object> mapParam, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        String json0=JacksonUtils.obj2json(mapParam);
        String username1=encryptor.encrypt(loginUser);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findUserIncludeExtensionByOrgCode/paging"+SSO+"?loginuser="+username2+"&appcode="+appcode
                +"&page="+page+"&size="+size+"&direction="+direction+"&properties="+properties)
                .json( json0 )
                .asBean(JsonResponse.class);
        return mapUserApiHandlemapUserApiHandle.handRemoteTypeReferenceResponse(response, new TypeReference<Map<String,Object>>(){});
    }

    /**
     * 获取管理层
     * @return
     */
    public JsonResponse findLeaderShip (String appcode ) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        JsonResponse response= HttpClient.post(config.getUumsAddress() + USER_MAPPING +"findLeaderShip" + SSO)
                .param( AuthoritiesConstants.SSO_API_USERNAME, encryptor.encrypt(loginUser))
                .param(AuthoritiesConstants.SSO_API_APP_CODE, appcode)
                .asBean(JsonResponse.class);
        return response;
    }

    /**
     * 根据corpId和userType出人员
     * @param mapParam
     * @param appcode
     * @return
     */
    public Set<SimpleUser> findUserFromCorpType(Map<String,Object> mapParam, String appcode) {
        String loginUser = SecurityUtils.getCurrentUserName();
        log.debug("Http remote request user by username: {}", loginUser);
        String json0=JacksonUtils.obj2json(mapParam);
        String username1=encryptor.encrypt(loginUser);
        String username2=username1.replace("+","%2B");
        JsonResponse response= HttpClient.textBody(config.getUumsAddress() + USER_MAPPING + "findUserFromCorpType"+SSO+"?loginuser="+username2+"&appcode="+appcode )
                .json( json0 )
                .asBean(JsonResponse.class);
        return setSimpleUserApiHandle.handRemoteTypeReferenceResponse(response, new TypeReference<Set<SimpleUser>>(){});
    }

}


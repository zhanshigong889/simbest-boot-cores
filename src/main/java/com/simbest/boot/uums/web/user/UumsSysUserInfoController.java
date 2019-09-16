package com.simbest.boot.uums.web.user;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.SimpleUser;
import com.simbest.boot.uums.api.user.UumsSysUserinfoApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Api (description = "系统用户操作相关接口",tags={"人员api 全部接口"})
@Slf4j
@RestController
@RequestMapping(value = {"/uums/sys/userinfo", "/sys/uums/userinfo"})
public class UumsSysUserInfoController {

    @Autowired
    private UumsSysUserinfoApi uumsSysUserinfoApi;

    /**
     * 不登录更新用户信息
     * @param keyword
     * @param keyType
     * @param appcode
     * @param simpleUser
     * @return
     */
    @ApiOperation(value = "不登录更新用户信息", notes = "不登录更新用户信息",tags={"人员api 不登录更新人员信息"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = "用户名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "keyType", value = "用户名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/update")
    public JsonResponse update( @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) IAuthService.KeyType keyType,
                                @RequestParam(required = false) String appcode,
                                @RequestBody(required = false) SimpleUser simpleUser) {
        return JsonResponse.success( uumsSysUserinfoApi.update(keyword,keyType,appcode,simpleUser));
    }

    /**
     * 获取用户信息列表并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param map
     * @return
     */
    @ApiOperation(value = "获取用户信息列表并分页", notes = "获取用户信息列表并分页",tags={"人员api findAll"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam (name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query") //
    })
    @PostMapping("/findAll")
    public JsonResponse findAll( @RequestParam(required = false, defaultValue = "1") int page, //
                                 @RequestParam(required = false, defaultValue = "10") int size, //
                                 @RequestParam(required = false) String direction, //
                                 @RequestParam(required = false) String properties,
                                 @RequestParam String appcode,
                                 @RequestBody Map map ) {
        return JsonResponse.success(uumsSysUserinfoApi.findAll(page,size,direction,properties,appcode,map));
    }

    /**
     * 获取用户信息列表不分页
     * @param appcode
     * @return
     */
    @ApiOperation(value = "获取用户信息列表不分页", notes = "获取用户信息列表不分页",tags={"人员api findAll"})
    @ApiImplicitParam(name = "appcode", value = "应用代码", dataType = "String", paramType = "query") //
    @PostMapping("/findAllNoPage")
    public JsonResponse findAllNoPage(@RequestParam String appcode, @RequestBody Map simpleUserMap ) {
        return JsonResponse.success(uumsSysUserinfoApi.findAllNoPage(appcode,simpleUserMap));
    }

    /**
     *根据用户名查询用户信息
     * @param username
     * @return
     */
    @ApiOperation(value = "根据用户名查询用户信息", notes = "根据用户名查询用户信息",tags={"人员api 根据用户名查"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/findByUsername")
    public JsonResponse findByUsername(@RequestParam String username,@RequestParam String appcode) {
        return JsonResponse.success(uumsSysUserinfoApi.findByUsername(username,appcode));
    }

    /**
     * 根据部门以及职位查询所有的人的用户名
     * @param loginUser
     * @param orgCode
     * @param positionIds
     * @param appcode
     * @return
     */
    @ApiOperation(value = "根据部门以及职位查询所有的人的用户名", notes = "根据部门以及职位查询所有的人的用户名",tags={"人员api 根据部门以及职位查"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "loginUser", value = "要验证的用户", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "orgCode", value = "组织编码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "positionIds", value = "职位id", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/findUsernameByOrgAndPosition")
    public JsonResponse findUsernameByOrgAndPosition(@RequestParam String loginUser,
                                                     @RequestParam String orgCode,
                                                     @RequestParam String positionIds,
                                                     @RequestParam String appcode) {
        return JsonResponse.success(uumsSysUserinfoApi. findUsernameByOrgAndPosition(loginUser, orgCode, positionIds, appcode));
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
    @ApiOperation(value = "根据组织orgcode获取用户并分页", notes = "根据组织orgcode获取用户并分页",tags={"人员api 根据组织查"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam (name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "orgCode", value = "组织appcode", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/findUserByOrg")
    public JsonResponse findUserByOrg( @RequestParam(required = false, defaultValue = "1") int page, //
                                      @RequestParam(required = false, defaultValue = "10") int size, //
                                      @RequestParam(required = false) String direction, //
                                      @RequestParam(required = false) String properties,
                                      @RequestParam String appcode,
                                       @RequestParam String orgCode) {
        return JsonResponse.success(uumsSysUserinfoApi.findUserByOrg(page,size,direction,properties,appcode,orgCode));
    }

    /**
     * 根据角色id获取用户但不分页
     * @param roleId
     * @return
     */
    @ApiOperation(value = "根据角色id获取用户但不分页", notes = "根据角色id获取用户但不分页",tags={"人员api 根据角色查"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "roleId", value = "roleId", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/findUserByRoleNoPage")
    public JsonResponse findUserByRoleNoPage( @RequestParam String roleId , @RequestParam String appcode){
        return JsonResponse.success(uumsSysUserinfoApi.findUserByRoleNoPage(roleId,appcode ));
    }

    /**
     * 根据过滤条件获取决策下的用户，有session
     * @param appcode
     * @param sysAppDecisionMap
     * @return
     */
    @ApiOperation(value = "根据过滤条件获取决策下的用户，有session", notes = "根据过滤条件获取决策下的用户，有session",tags={"人员api 决策项出人"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String" ,paramType = "query")
    })
    @PostMapping(value ="/findUserByDecisionNoPage")
    public JsonResponse findUserByDecisionNoPage(@RequestParam String appcode,@RequestBody Map sysAppDecisionMap){
        return uumsSysUserinfoApi.findUserByDecisionNoPage(appcode,sysAppDecisionMap);
    }

    /**
     * 根据过滤条件获取决策下的用户，无session
     * @param appcode
     * @param sysAppDecisionMap
     * @return
     */
    @ApiOperation(value = "根据过滤条件获取决策下的用户，无session", notes = "根据过滤条件获取决策下的用户，无session",tags={"人员api 决策项出人"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String" ,paramType = "query")
    })
    @PostMapping(value ="/findUserByDecisionNoPageGrouping")
    public JsonResponse findUserByDecisionNoPageGrouping(@RequestParam String appcode,@RequestBody Map sysAppDecisionMap){
        return uumsSysUserinfoApi.findUserByDecisionNoPageGrouping(appcode,sysAppDecisionMap);
    }

    /**
     * 根据用户返回用户以及用户的的组织树
     * @param appcode
     * @param username
     * @return
     */
    @ApiOperation(value = "根据用户返回用户以及用户的的组织树", notes = "根据用户返回用户以及用户的的组织树",tags={"人员api 根据人员查"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "username", value = "要查询的人的username", dataType = "String" ,paramType = "query")
    })
    @PostMapping(value ="/findUserByUsernameNoPage")
    public JsonResponse findUserByUsernameNoPage(@RequestParam String appcode,@RequestParam String username){
        return uumsSysUserinfoApi.findUserByUsernameNoPage(appcode,username);
    }

    /**
     * 一层层去查询全部组织和人
     * @param appcode
     * @param orgCode
     * @return
     */
    @ApiOperation(value = "一层层去查询全部组织和人", notes = "一层层去查询全部组织和人",tags={"人员api 层级查"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "orgCode", value = "组织编码", dataType = "String" ,paramType = "query")
    })
    @PostMapping(value ="/findOneStep")
    public JsonResponse findOneStep(@RequestParam String appcode,@RequestParam(required = false) String orgCode,@RequestBody(required = false) Map<String,Object> extraValueMap){
        return JsonResponse.success(uumsSysUserinfoApi.findOneStep(appcode,orgCode,extraValueMap));
    }

    /**
     * 根据用户中文姓名以及主数据首先移动号码模糊查询并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param truename
     * @param preferredMobile
     * @return
     */
    @ApiOperation(value = "根据用户中文姓名以及主数据首先移动号码模糊查询并分页", notes = "根据用户中文姓名以及主数据首先移动号码模糊查询并分页",tags={"人员api 根据人员查"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam (name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "truename", value = "用户真实姓名", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "preferredMobile", value = "用户移动电话", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/findRoleNameIsARoleDim")
    public JsonResponse findRoleNameIsARoleDim( @RequestParam(required = false, defaultValue = "1") int page, //
                                       @RequestParam(required = false, defaultValue = "10") int size, //
                                       @RequestParam(required = false) String direction, //
                                       @RequestParam(required = false) String properties,
                                       @RequestParam(required = false) String appcode,
                                       @RequestParam(required = false) String truename,
                                                @RequestParam(required = false) String preferredMobile ) {
        return uumsSysUserinfoApi.findRoleNameIsARoleDim(page,size,direction,properties,appcode,truename,preferredMobile);
    }

   /**
     * 检测用户是否有app的权限
    * 应用向UUMS发送单点请求时使用
     * @param username
     * @param appcode
     * @return
     */
    @ApiOperation(value = "检测用户是否有app的权限,应用向UUMS发送单点请求时使用", notes = "检测用户是否有app的权限,应用向UUMS发送单点请求时使用",tags={"人员api 检验用户是否有app权限"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/checkUserAccessApp")
    public JsonResponse checkUserAccessApp(@RequestParam(required = false) String username
                                           ,@RequestParam(required = false) String appcode ){
        return JsonResponse.success( uumsSysUserinfoApi.checkUserAccessApp(username,appcode));
    }

    /**
     * 检测用户是否有app的权限。当前应用无session时使用，如portal。门户Portal向应用发送单点请求，应用再向UUMS发送单点请求时使用
     * @param username
     * @param appcode
     * @return
     */
    @ApiOperation(value = "检测用户是否有app的权限。当前应用无session时使用，如portal。门户Portal向应用发送单点请求，应用再向UUMS发送单点请求时使用", notes = "检测用户是否有app的权限。当前应用无session时使用，如portal。门户Portal向应用发送单点请求，应用再向UUMS发送单点请求时使用",tags={"人员api 检验用户是否有app权限"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/checkUserAccessAppNoSession")
    public JsonResponse checkUserAccessAppNoSession(@RequestParam(required = false) String username
            ,@RequestParam(required = false) String appcode ){
        return JsonResponse.success( uumsSysUserinfoApi.checkUserAccessAppNoSession(username,appcode));
    }

    /**
     * 查询某个人在某一应用下的全部权限。普通应用使用
     * 应用向UUMS发送单点请求时使用
     * @param username
     * @param appcode
     * @return
     */
    @ApiOperation(value = "查询某个人在某一应用下的全部权限。普通应用使用。应用向UUMS发送单点请求时使用。", notes = "查询某个人在某一应用下的全部权限。普通应用使用。应用向UUMS发送单点请求时使用。",tags={"人员api 查询权限"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/findPermissionByAppUser")
    public JsonResponse findPermissionByAppUser(@RequestParam(required = false) String username
            ,@RequestParam(required = false) String appcode ){
        return JsonResponse.success( uumsSysUserinfoApi.findPermissionByAppUser(username,appcode));
    }

    /**
     * 查询某个人在某一应用下的全部权限。当前应用无session时使用，如portal
     * 门户Portal向应用发送单点请求，应用再向UUMS发送单点请求时使用
     * @param loginuser
     * @param appcode
     * @return
     */
    @ApiOperation(value = "查询某个人在某一应用下的全部权限。当前应用无session时使用，如portal。门户Portal向应用发送单点请求，应用再向UUMS发送单点请求时使用。", notes = "查询某个人在某一应用下的全部权限。当前应用无session时使用，如portal。门户Portal向应用发送单点请求，应用再向UUMS发送单点请求时使用。",tags={"人员api 查询权限"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam(name = "loginuser", value = "rsa加密后的用户名", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping(value = {"/findPermissionByAppUserNoSession","/findPermissionByAppUserNoSession/sso"})
    public JsonResponse findPermissionByAppUserNoSession(@RequestParam(required = false) String loginuser
            ,@RequestParam(required = false) String appcode ){
        return JsonResponse.success( uumsSysUserinfoApi.findPermissionByAppUserNoSession(loginuser,appcode));
    }

    /**
     * 根据关键字查询用户身份信息，当前支持如下：
     * 登录名     username
     * 人员编号   employeeNumber
     * 手机号码   preferredMobile
     * 邮箱       email
     * 保留关键字 reserve1 可存微信openid
     * @param keyword
     * @param keyType
     * @param appcode
     * @return
     */
    @ApiOperation(value = "根据关键字查询用户身份信息", notes = "根据关键字查询用户身份信息",tags={"人员api 根据关键字查"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam(name = "keyword", value = "关键字", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "keyType", value = "类型", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/findByKey")
    public JsonResponse findByKey(@RequestParam(required = false) String keyword
            ,@RequestParam(required = false) IAuthService.KeyType keyType,@RequestParam(required = false) String appcode){
        return JsonResponse.success( uumsSysUserinfoApi.findByKey(keyword,keyType,appcode));
    }

    /**
     * 根据群组sid查询OA账号，真实姓名及该用户的职位id，职位排序和职位名以及所在组织的displayName
     * @param appcode
     * @param groupSid
     * @return
     */
    @ApiOperation(value = "根据群组sid查询OA账号，真实姓名及该用户的职位id，职位排序和职位名以及所在组织的displayName", notes = "根据群组sid查询OA账号，真实姓名及该用户的职位id，职位排序和职位名以及所在组织的displayName",tags={"人员api 根据群组查"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "groupSid", value = "群组sid", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/findUserInfoByGroupSidNoPage")
    public JsonResponse findUserInfoByGroupSidNoPage(@RequestParam(required = false) String appcode,@RequestParam(required = false) String groupSid){
        return JsonResponse.success( uumsSysUserinfoApi.findUserInfoByGroupSidNoPage(appcode,groupSid));
    }

    /**
     * 修改用户密码
     * @param username
     * @param rsaPassword
     * @param appCode
     * @return
     */
    @ApiOperation(value = "修改用户密码", notes = "修改用户密码",tags={"人员api 修改密码"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "rsaPassword", value = "经过RSA加密的密码", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "appCode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value ="/changeUserPassword")
    public JsonResponse changeUserPassword(@RequestParam String username, @RequestParam String rsaPassword, @RequestParam String appCode){
        return uumsSysUserinfoApi.changeUserPassword(username,rsaPassword,appCode);
    }

    /**
     * 修改我的密码
     * @param oldRsaPassword
     * @param newRsaPassword
     * @param appCode
     * @return
     */
    @ApiOperation(value = "修改我的密码", notes = "修改我的密码",tags={"人员api 修改密码"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "oldRsaPassword", value = "经过RSA加密的原始密码", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "newRsaPassword", value = "经过RSA加密的新密码", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "appCode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value ="/changeMyPassword")
    public JsonResponse changeMyPassword(@RequestParam String oldRsaPassword, @RequestParam String newRsaPassword, @RequestParam String appCode){
        return uumsSysUserinfoApi.changeMyPassword(oldRsaPassword,newRsaPassword,appCode);
    }

    /**
     * 获取一个群组下的所有人，并且对人员进行排序，排序之后获取人员的全部信息
     * @param groupId
     * @param appCode
     * @return
     */
    @ApiOperation(value = "获取一个群组下的所有人，并且对人员进行排序，排序之后获取人员的全部信息", notes = "获取一个群组下的所有人，并且对人员进行排序，排序之后获取人员的全部信息",tags={"人员api 根据群组查"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "groupId", value = "群组id", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "appCode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value ="/findUserByGroupSort")
    public JsonResponse findUserByGroupSort(@RequestParam String groupId, @RequestParam String appCode){
        return uumsSysUserinfoApi.findUserByGroupSort(groupId,appCode);
    }

    /**
     * 获取某一个组织下的组织和人
     * @param orgCode
     * @param appCode
     * @return
     */
    @ApiOperation(value = "获取某一个组织下的组织和人", notes = "获取某一个组织下的组织和人",tags={"人员api 根据组织查"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "orgCode", value = "组织编码", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "appCode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value ="/findAllInfosUnderOrg")
    public JsonResponse findAllInfosUnderOrg(@RequestParam String orgCode, @RequestParam String appCode){
        return uumsSysUserinfoApi.findAllInfosUnderOrg(orgCode,appCode);
    }

    /**
     * 获取某一个组织下的全部人，包括下级组织的人
     * @param orgCode
     * @param appCode
     * @return
     */
    @ApiOperation(value = "获取某一个组织下的全部人，包括下级组织的人", notes = "获取某一个组织下的全部人，包括下级组织的人",tags={"人员api 根据组织查"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "orgCode", value = "组织编码", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "appCode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value ="/findAllUserByOrgCode")
    public JsonResponse findAllUserByOrgCode(@RequestParam String orgCode, @RequestParam String appCode){
        return JsonResponse.success( uumsSysUserinfoApi.findAllUserByOrgCode(orgCode,appCode),"获取组织下的全部人成功！");
    }

    /**
     * 根据职位名获取用户不分页
     * @param orgCode
     * @param positionId
     * @return
     */
    @ApiOperation(value = "根据职位名获取用户不分页", notes = "根据职位名获取用户不分页",tags={"人员api 根据职位查"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "positionId", value = "多个职位id", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "appCode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value ="/findUserByPositionNoPage")
    public JsonResponse findUserByPositionNoPage(@RequestParam String orgCode, @RequestParam String positionId){
        return JsonResponse.success( uumsSysUserinfoApi.findUserByPositionNoPage(orgCode,positionId),"获取职位下的人成功！");
    }

    /**
     * 获取某一个组织下的组织和人，合在一起
     * @param orgCode
     * @param appCode
     * @return
     */
    @ApiOperation(value = "获取某一个组织下的组织和人，合在一起", notes = "获取某一个组织下的组织和人，合在一起",tags={"人员api 根据组织查"})
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "orgCode", value = "组织编码", dataType = "String" ,paramType = "query"),
            @ApiImplicitParam(name = "appCode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value ="/findAllInfosUnderOrgTogether")
    public JsonResponse findAllInfosUnderOrgTogether(@RequestParam String orgCode, @RequestParam String appCode){
        return  uumsSysUserinfoApi.findAllInfosUnderOrgTogether(orgCode,appCode);
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
    @ApiOperation(value = "根据组织（精确）以及用户oa账号、用户名、手机号（模糊）获取用户并分页", notes = "根据组织（精确）以及用户oa账号、用户名、手机号（模糊）获取用户并分页",tags={"人员api 根据人员查"})
    @ApiImplicitParams ({ //
            @ApiImplicitParam (name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "appcode", value = "当前应用appcode", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "orgCode", value = "组织编码", dataType = "String", //
                    paramType = "query"),
            @ApiImplicitParam(name = "searchFields", value = "搜索内容", dataType = "String", //
                    paramType = "query")
    })
    @PostMapping("/findUserOrgDim")
    public JsonResponse findUserOrgDim( @RequestParam(required = false, defaultValue = "1") int page, //
                                                @RequestParam(required = false, defaultValue = "10") int size, //
                                                @RequestParam(required = false) String direction, //
                                                @RequestParam(required = false) String properties,
                                                @RequestParam(required = false) String appcode,
                                                @RequestParam(required = false) String orgCode,
                                                @RequestParam(required = false) String searchFields ) {
        return uumsSysUserinfoApi.findUserOrgDim(page,size,direction,properties,appcode,orgCode,searchFields);
    }

    /**
     * 半展出人所在的组织树，除了人所在的组织是展开的，其他都是闭合的
     * @param mapParam
     * @param appcode
     * @return
     */
    @ApiOperation(value = "半展出人所在的组织树，除了人所在的组织是展开的，其他都是闭合的", notes = "半展出人所在的组织树，除了人所在的组织是展开的，其他都是闭合的",tags={"人员api 根据组织查"})
    @ApiImplicitParams ({
            @ApiImplicitParam (name = "appcode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping("/findUserTreeBz")
    public JsonResponse findUserTreeBz( @RequestBody(required = false) Map<String,Object> mapParam,
                                        @RequestParam(required = false) String appcode) {
        return JsonResponse.success( uumsSysUserinfoApi.findUserTreeBz(mapParam,appcode) );
    }

    /**
     * 获取管理层
     * @return
     */
    @ApiOperation(value = "获取管理层", notes = "获取管理层", tags = {"获取管理层"})
    @PostMapping(value = {"/findLeaderShip","/sso/findLeaderShip"})
    public JsonResponse findManagement(@RequestParam(required = false) String appcode) {
        return uumsSysUserinfoApi.findLeaderShip(appcode);
    }
}


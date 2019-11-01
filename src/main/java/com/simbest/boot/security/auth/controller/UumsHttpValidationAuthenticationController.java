package com.simbest.boot.security.auth.controller;


import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.SimpleUser;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import com.simbest.boot.uums.api.user.UumsSysUserinfoApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用途：登录校验控制器
 * 作者: lishuyi
 * 时间: 2018/1/31  15:49
 */
@Api(description = "UumsHttpValidationAuthenticationController", tags = {"权限管理-UUMS登录校验"})
@Slf4j
@RestController
@RequestMapping("/httpauth")
public class UumsHttpValidationAuthenticationController {

    private final static String LOGTAG = "UUMS认证控制器";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RsaEncryptor rsaEncryptor;

    @Autowired
    private IAuthService authService;

    @Autowired
    private UumsSysUserinfoApi uumsSysUserinfoApi;

    @Autowired
    private  PasswordEncoder myBCryptPasswordEncoder;

    @ApiOperation(value = "从UUMS认证登录", notes = "应用向远程UUMS发起认证请求")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "用户账号关键字", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping("/validate")
    public JsonResponse validate(@RequestParam String username, @RequestParam String password, @RequestParam String appcode) {
        try {
            if(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(appcode)) {
                String passwordDecode = rsaEncryptor.decrypt(password);
                UsernamePasswordAuthenticationToken passwordToken = new UsernamePasswordAuthenticationToken(username, passwordDecode);
                Authentication authentication = authenticationManager.authenticate(passwordToken);
                if(authentication.isAuthenticated()) {
                    if(authentication.getPrincipal() instanceof IUser){
                        IUser iUser = (IUser)authentication.getPrincipal();
                        if(StringUtils.isEmpty(iUser.getReserve4()) ||
                                !passwordDecode.equalsIgnoreCase(rsaEncryptor.decrypt(iUser.getReserve4()))){
                            SimpleUser simpleUser = new SimpleUser();
                            simpleUser.setUsername( username );
                            simpleUser.setReserve4( password );
                            try{
                                IUser newUser = authService.updateUser(username,IAuthService.KeyType.username, appcode,simpleUser);
                                if(null != newUser){
                                    log.debug("用户【{}】的冗余密码字段Reserve4已更新为【{}】", username, password);
                                }
                                //uumsSysUserinfoApi.update( username,IAuthService.KeyType.username, appcode,simpleUser);
                            }catch ( Exception e ){
                                log.error( "UumsHttpValidationAuthenticationController后台Reserve4更新用户密码失败!" );
                                Exceptions.printException( e );
                            }
                        }
                    }
                    return JsonResponse.success(authentication.getPrincipal());
                }
                else {
                    log.error(LOGTAG + "认证用户账号关键字【{}】通过密码【{}】访问应用【{}】失败", username, passwordDecode, appcode);
                    return JsonResponse.fail(ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
                }
            } else {
                log.error(LOGTAG + "认证用户账号【{}】密码【{}】访问【{}】失败", username, password, appcode);
                return JsonResponse.fail(ErrorCodeConstants.LOGIN_ERROR_INVALIDATE_USERNAME_PASSWORD);
            }
        } catch (AuthenticationException e){
            log.error(LOGTAG + "认证用户账号关键字【{}】密码【{}】访问【{}】发生【{}】异常", username, password, appcode, e.getMessage());
            return JsonResponse.fail(ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
        } catch (Exception e){
            log.error(LOGTAG + "认证用户账号关键字【{}】密码【{}】访问【{}】发生【{}】异常", username, password, appcode, e.getMessage());
            return JsonResponse.fail(ErrorCodeConstants.LOGIN_ERROR_BAD_CREDENTIALS);
        }
    }
}

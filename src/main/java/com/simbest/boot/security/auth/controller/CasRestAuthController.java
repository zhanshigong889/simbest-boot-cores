/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.controller;

import com.simbest.boot.config.AppConfig;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.auth.authentication.CasUser;
import com.simbest.boot.sys.model.SysLogLogin;
import com.simbest.boot.sys.service.ISysLogLoginService;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.encrypt.Des3Encryptor;
import com.simbest.boot.util.encrypt.Md5Encryptor;
import com.simbest.boot.util.http.MvcUtil;
import com.simbest.boot.util.server.HostUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * 用途：
 * 作者: lishuyi
 * 时间: 2018/8/22  19:18
 */
@Api(description = "CasRestAuthController", tags = {"权限管理-统一认证登录校验"})
@Slf4j
@RestController
@RequestMapping(value = "/anonymous/cas")
public class CasRestAuthController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private IAuthService authService;

    @Autowired
    private Des3Encryptor des3Encryptor;

    @Autowired
    private Md5Encryptor md5Encryptor;

    @Autowired
    private PasswordEncoder myBCryptPasswordEncoder;


    @Autowired
    private ISysLogLoginService logLoginService;

    @PostMapping("/auth")
    public void auth(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest request, HttpServletResponse response){
        Object result = null;
        try {
            IUser userWeb = MvcUtil.obtainUserFormHeader(httpHeaders);
            //当没有 传递 参数的情况
            if(userWeb == null){
                result = new ResponseEntity<CasUser>(HttpStatus.NOT_FOUND);
            }
            //数据库查找
            IUser dbUser = this.authService.findByKey(userWeb.getUsername(), IAuthService.KeyType.username, appConfig.getAppcode());
            if (dbUser != null) {
                String rawPassword = des3Encryptor.decrypt(userWeb.getPassword());
//                rawPassword = md5Encryptor.encrypt(rawPassword);
                if(!myBCryptPasswordEncoder.matches(rawPassword, dbUser.getPassword())) {
                    //密码不匹配
                    result = new ResponseEntity<CasUser>(HttpStatus.BAD_REQUEST);
                }else if (!dbUser.isEnabled()) {
                    //禁用 403
                    result = new ResponseEntity<CasUser>(HttpStatus.FORBIDDEN);
                }else if (!dbUser.isAccountNonLocked()) {
                    //锁定 423
                    result = new ResponseEntity<CasUser>(HttpStatus.LOCKED);
                }else if (!dbUser.isAccountNonExpired()) {
                    //过期 428
                    result = new ResponseEntity<CasUser>(HttpStatus.PRECONDITION_REQUIRED);
                }else{
                    //正常的数据
                    //直接返回User数据
                    CasUser casUser = new CasUser();
                    casUser.setUsername(dbUser.getUsername());
                    result = casUser;

                    //记录登录日志
                    recordSuccessLogin(dbUser, request);
                }
            } else {
                //不存在 404
                result = new ResponseEntity<CasUser>(HttpStatus.NOT_FOUND);
            }
        } catch (UnsupportedEncodingException e) {
            result = new ResponseEntity<CasUser>(HttpStatus.BAD_REQUEST);
        }
        //将数据输出到客户端
        MvcUtil.writeJsonToResponse(response, result);
    }

    private void recordSuccessLogin(IUser iUser, HttpServletRequest request){
        SysLogLogin logLogin = SysLogLogin.builder()
                .account(iUser.getUsername())
                .loginEntry(1) //CAS登录入口
                .loginType(0)  //用户名登录方式
                .loginTime(DateUtil.getCurrent())
                .isSuccess(true)
                .trueName(iUser.getTruename())
                .belongOrgName(iUser.getBelongOrgName())
                .build();
        logLogin.setIp(HostUtil.getClientIpAddress(request));
        logLogin.setSessionid(request.getSession().getId());
        logLoginService.insert(logLogin);
    }
}

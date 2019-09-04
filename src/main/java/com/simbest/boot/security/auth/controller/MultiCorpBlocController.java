package com.simbest.boot.security.auth.controller;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.IBloc;
import com.simbest.boot.security.ICorp;
import com.simbest.boot.security.IUser;
import com.simbest.boot.util.security.SecurityUtils;
import com.simbest.boot.uums.api.bloc.UumsSysBlocApi;
import com.simbest.boot.uums.api.corp.UumsSysCorpApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用途：权限管理-多企业多集团控制器
 * 作者: lishuyi
 * 时间: 2019/8/21  15:49
 */
@Api(description = "MultiCorpBlocController", tags = {"权限管理-多企业多集团控制器"})
@Slf4j
@RestController
@RequestMapping("/multi/principal")
public class MultiCorpBlocController {

    @Autowired
    private AppConfig config;

    @Autowired
    private UumsSysCorpApi corpApi;

    @Autowired
    private UumsSysBlocApi blocApi;

    @Autowired
    private IAuthService authService;

    /**
     * 用户切换企业
     * @return login
     */
    @ApiOperation(value = "用户切换企业")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "corpid", value = "企业id", dataType = "String", paramType = "query", required = true)
    })
    @PostMapping(value={"/changeCorp","/sso/changeCorp","/api/changeCorp"})
    public JsonResponse changeCorp(@RequestParam String corpid) {
        ICorp corp = corpApi.findById(corpid, config.getAppcode());
        IUser currentUser = SecurityUtils.getCurrentUser();
        currentUser.setCurrentCorp(corp.getId());
        currentUser.setCurrentCorpCode(corp.getCorpCode());
        currentUser.setCurrentBloc(corp.getBlocId());
        IBloc bloc = blocApi.findById(corp.getBlocId(), config.getAppcode());
        currentUser.setCurrentBlocCode(bloc.getBlocCode());
        authService.changeUserSessionByCorp(currentUser);
        return JsonResponse.defaultSuccessResponse();
    }


}

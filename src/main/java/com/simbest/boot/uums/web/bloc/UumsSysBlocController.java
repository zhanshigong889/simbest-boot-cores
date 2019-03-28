/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.bloc;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.bloc.UumsSysBlocApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用途：集团信息控制器
 * 作者: lishuyi
 * 时间: 2019/3/14  18:11
 */
@Api (description = "集团信息控制器",tags={"集团信息api"} )
@RestController
@RequestMapping(value = {"/uums/sys/bloc", "/sys/uums/bloc"})
public class UumsSysBlocController {

    @Autowired
    private UumsSysBlocApi uumsSysBlocApi;

    /**
     * 查询集团信息
     */
    @ApiOperation(value = "查询集团信息",notes = "查询集团信息" )
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id",value = "集团ID", dataType = "String", paramType = "query" ),
        @ApiImplicitParam(name = "appcode", value = "appcode",dataType = "String",paramType = "query" )
    })
    @PostMapping(value="/findById")
    public JsonResponse findById( @RequestParam String id, @RequestParam String appcode) {
        return JsonResponse.success(uumsSysBlocApi.findById(id, appcode));
    }


}



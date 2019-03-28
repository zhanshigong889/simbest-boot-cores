/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.corp;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.corp.UumsSysCorpApi;
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
 * 用途：公司企业信息控制器
 * 作者: lishuyi
 * 时间: 2019/3/14  18:11
 */
@Api (description = "公司企业信息控制器",tags={"企业信息api"} )
@RestController
@RequestMapping(value = {"/uums/sys/corp", "/sys/uums/corp"})
public class UumsSysCorpController {

    @Autowired
    private UumsSysCorpApi uumsSysCorpApi;

    /**
     * 查询企业信息
     */
    @ApiOperation(value = "查询企业信息",notes = "查询企业信息" )
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id",value = "企业ID", dataType = "String", paramType = "query" ),
        @ApiImplicitParam(name = "appcode", value = "appcode",dataType = "String",paramType = "query" )
    })
    @PostMapping(value="/findById")
    public JsonResponse findById( @RequestParam String id, @RequestParam String appcode) {
        return JsonResponse.success(uumsSysCorpApi.findById(id, appcode));
    }


}



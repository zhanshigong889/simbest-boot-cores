/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.app;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.app.UumsSysAppConfigApi;
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

@Api (description = "应用配置相关接口",tags={"应用配置api"} )
@RestController
@RequestMapping("/uums/sys/config")
public class UumsSysAppConfigController {

    @Autowired
    private UumsSysAppConfigApi uumsSysAppConfigApi;

    /**
     * 根据接口类型获取app配置信息
     * @param interfaceStyle
     * @param appcode
     * @param username
     * @return
     */
    @ApiOperation(value = "根据接口类型获取app配置信息", notes = "根据接口类型获取app配置信息")
    @ApiImplicitParams( {
            @ApiImplicitParam(name = "interfaceStyle", value = "应用类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    } )

    @PostMapping("/findAppConfigByStyle")
    public JsonResponse findAppConfigByStyle(@RequestParam(required = false) String interfaceStyle,@RequestParam(required = false)  String appcode,@RequestParam(required = false)  String username) {
        return JsonResponse.success(uumsSysAppConfigApi.findAppConfigByStyle(interfaceStyle,username,appcode));
    }

}



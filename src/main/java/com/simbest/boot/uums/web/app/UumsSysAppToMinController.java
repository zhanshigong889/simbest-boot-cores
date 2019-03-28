/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.app;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.app.UumsSysAppApi;
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

@Api (description = "应用决策群组相关接口",tags={"应用api 给手机用"} )
@RestController
@RequestMapping("/sys/uums/app")
public class UumsSysAppToMinController {

    @Autowired
    private UumsSysAppApi uumsSysAppApi;

    /**
     * 根据appCode查询应用的消息
     * @param appcode
     * @return
     */
    @ApiOperation(value = "根据appCode查询应用的消息", notes = "根据appCode查询应用的消息")
    @ApiImplicitParams({ //
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", paramType = "query"), //
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String", paramType = "query")
    })
    @PostMapping("/findAppByAppCode")
    public JsonResponse findById(@RequestParam(required = false)  String appcode,@RequestParam(required = false)  String username) {
        return JsonResponse.success(uumsSysAppApi.findAppByAppCode( appcode ,username));
    }

}



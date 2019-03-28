/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.group;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.group.UumsSysGroupApi;
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

@Api (description = "群组操作相关接口",tags={"群组api"}  )
@RestController
@RequestMapping(value = {"/uums/sys/group", "/sys/uums/group"})
public class UumsSysGroupController {

    @Autowired
    private UumsSysGroupApi uumsSysGroupApi;

    /**
     * 查看某个用户拥有哪种数据权限
     * @param appcode
     * @return
     */
    @ApiOperation (value = "查看某个用户拥有哪种数据权限", notes = "查看某个用户拥有哪种数据权限")
    @ApiImplicitParams( {
            @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    } )
    @PostMapping ("/findDataPermission")
    public JsonResponse findDataPermission( @RequestParam(required = false)  String appcode) {
        return JsonResponse.success(uumsSysGroupApi.findDataPermission( appcode ));
    }

}



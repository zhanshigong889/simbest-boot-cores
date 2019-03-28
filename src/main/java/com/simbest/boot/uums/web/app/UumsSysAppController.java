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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

@Api (description = "应用决策群组相关接口",tags={"应用api"} )
@RestController
@RequestMapping("/uums/sys/app")
public class UumsSysAppController {

    @Autowired
    private UumsSysAppApi uumsSysAppApi;

    /**
     * 根据appCode查询应用的消息
     * @param appcode
     * @return
     */
    @ApiOperation(value = "根据appCode查询应用的消息", notes = "根据appCode查询应用的消息")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping("/findAppByAppCode")
    public JsonResponse findById(@RequestParam(required = false)  String appcode,@RequestParam(required = false)  String username) {
        return JsonResponse.success(uumsSysAppApi.findAppByAppCode( appcode ,username));
    }

    /**
     * 单表条件查询
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @return
     */
    @ApiOperation(value = "单表条件查询", notes = "单表条件查询")
    @ApiImplicitParams ({ //
            @ApiImplicitParam(name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query") //
    })
    @PostMapping("/findAll")
    public JsonResponse findAll( @RequestParam(required = true, defaultValue = "1") int page, //
                                 @RequestParam(required = true, defaultValue = "10") int size, //
                                 @RequestParam(required = true) String direction,
                                 @RequestParam(required = true) String properties,
                                 @RequestParam String appcode,
                                 @RequestBody Map sysAppMap) {
        return uumsSysAppApi.findAll( page , size , direction , properties , appcode, sysAppMap);
    }

    /**
     * 获取全部应用职位列表无分页
     * @param appcode
     * @param sysAppMap
     * @return
     */
    @ApiOperation(value = "单表条件查询", notes = "单表条件查询")
    @ApiImplicitParams ({ //
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", paramType = "query", //
                    required = true, example = "1")
    })
    @PostMapping("/findAllNoPage")
    public JsonResponse findAllNoPage(
                                 @RequestParam String appcode,
                                 @RequestBody(required = false) Map sysAppMap) {
        return uumsSysAppApi.findAllNoPage(  appcode, sysAppMap);
    }

}



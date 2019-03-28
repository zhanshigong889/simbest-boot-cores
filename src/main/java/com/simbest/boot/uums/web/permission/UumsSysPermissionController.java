/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.permission;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.permission.UumsSysPermissionApi;
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

@Api (description = "系统权限操作相关接口",tags={"权限api"}  )
@RestController
@RequestMapping(value = {"/uums/sys/perimission", "/sys/uums/perimission"})
public class UumsSysPermissionController {

    @Autowired
    private UumsSysPermissionApi uumsSysPermissionApi;

    /**
     *根据id查看权限信息
     * @param id
     * @return
     */
    @ApiOperation(value = "查询权限信息", notes = "通过此接口来查询权限信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "权限ID", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    })
    @PostMapping("/findById")
    public JsonResponse findById(@RequestParam String id,@RequestParam  String appcode) {
        return JsonResponse.success(uumsSysPermissionApi.findById( id,appcode ));
    }

    /**
     *获取全部权限信息列表不分页
     * @return
     */
    @ApiOperation(value = "获取全部权限信息列表不分页", notes = "获取全部权限信息列表不分页")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping("/findAllNoPage")
    public JsonResponse findAllNoPage(@RequestParam(required = false) String appcode,@RequestBody(required = false) Map simplePermissionMap) {
        return JsonResponse.success(uumsSysPermissionApi.findAllNoPage(appcode,simplePermissionMap ));
    }

    /**
     *获取权限信息列表并分页
     * @param page
     * @param size
     * @param direction
     * @param appcode
     * @param jsonString
     * @return
     */
  /*  @ApiOperation(value = "获取权限列表", notes = "通过此接口来获取权限列表")
    @ApiImplicitParams({ //
            @ApiImplicitParam(name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query") //
    })
    @PostMapping(value = "/findAll")
    public JsonResponse findAll( @RequestParam(required = false, defaultValue = "1") int page, //
                                 @RequestParam(required = false, defaultValue = "10") int size, //
                                 @RequestParam(required = false) String direction,
                                 @RequestParam(required = false) String properties,
                                 @RequestParam String appcode,
                                 @RequestParam String jsonString ) {
        return JsonResponse.success(uumsSysPermissionApi.findAll(page, size,direction,properties,appcode,jsonString ));
    }*/

    /**
     * 根据某个角色以及应用查询其下的权限
     * @param sosAppcode
     * @param roleName
     * @param appCode
     * @return
     */
    /*@ApiOperation(value = "根据某个角色以及应用查询其下的权限", notes = "根据某个角色以及应用查询其下的权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ssoAppcode", value = "应用code", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "roleName", value = "角色名", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam (name = "appCode", value = "应用code", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping (value = "/findRoleAppPermission")
    public JsonResponse findRoleAppPermission( @RequestParam String sosAppcode, @RequestParam String roleName,@RequestParam String appCode) {
        return JsonResponse.success(uumsSysPermissionApi.findRoleAppPermission( sosAppcode,roleName,appCode));
    }*/

    /**
     * 根据某个职位以及应用查询其下的权限
     * @param sosAppcode
     * @param positionName
     * @param appCode
     * @return
     */
    /*@ApiOperation (value = "根据某个职位以及应用查询其下的权限", notes = "根据某个职位以及应用查询其下的权限")
    @ApiImplicitParams ({
            @ApiImplicitParam (name = "sosAppcode", value = "应用code", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "positionName", value = "职位名", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam (name = "appCode", value = "应用code", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/findPositionAppPermission")
    public JsonResponse findPositionAppPermission(@RequestParam String sosAppcode,@RequestParam String positionName,@RequestParam String appCode) {
        return JsonResponse.success(uumsSysPermissionApi.findPositionAppPermission( sosAppcode,positionName,appCode));
    }*/
}



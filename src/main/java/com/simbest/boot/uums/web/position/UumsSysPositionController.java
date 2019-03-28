/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.position;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.position.UumsSysPositionApi;
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

@Api (description = "系统职位操作相关接口",tags={"职位api"}  )
@RestController
@RequestMapping(value = {"/uums/sys/position", "/sys/uums/position"})
public class UumsSysPositionController {

    @Autowired
    private UumsSysPositionApi uumsSysPositionApi;

    /**
     * 查询职位信息
     * @param id
     * @param appcode
     * @return
     */
    @ApiOperation(value = "查询职位信息",notes = "查询职位信息" )
    @ApiImplicitParams({
        @ApiImplicitParam(name = "id",value = "职位ID",dataType = "Integer",paramType = "query" ),
        @ApiImplicitParam(name = "appcode",value = "appcode",dataType = "String",paramType = "query" )
    })
    @PostMapping(value="/findById")
    public JsonResponse findById( @RequestParam String id, @RequestParam String appcode) {
        return JsonResponse.success(uumsSysPositionApi.findById(id,appcode));
    }

    /**
     * 获取角色信息列表并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param appcode
     * @param map
     * @return
     */
    @ApiOperation(value = "获取职位列表", notes = "通过此接口来获取职位列表")
    @ApiImplicitParams ({ //
            @ApiImplicitParam (name = "page", value = "当前页码", dataType = "int", paramType = "query", //
                    required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", //
                    required = true, example = "10"), //
            @ApiImplicitParam(name = "direction", value = "排序规则（asc/desc）", dataType = "String", //
                    paramType = "query"), //
            @ApiImplicitParam(name = "properties", value = "排序规则（属性名称）", dataType = "String", //
                    paramType = "query") //
    })
    @PostMapping("/findAll")
    public JsonResponse findAll( @RequestParam(required = false, defaultValue = "1") int page, //
                                 @RequestParam(required = false, defaultValue = "10") int size, //
                                 @RequestParam(required = false) String direction, //
                                 @RequestParam(required = false) String properties,
                                 @RequestParam String appcode,
                                 @RequestBody Map map ) {
        return JsonResponse.success(uumsSysPositionApi.findAll(page,size,direction,properties,appcode,map));
    }

    /**
     *根据用户名查询其职务
     * @param username
     * @return
     */
    @ApiOperation(value = "根据用户名查询其职务", notes = "根据用户名查询其职务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String", paramType = "query")
    })

    @PostMapping("/findPositionByUsername")
    public JsonResponse findPositionByUsername(@RequestParam String username,@RequestParam String appcode) {
        return JsonResponse.success(uumsSysPositionApi.findPositionByUsername(username,appcode));
    }

    /**
     *查询某一组织下全部用户的职务
     * @param orgCode
     * @return
     */
    @ApiOperation(value = "查询某一组织下全部用户的职务", notes = "查询某一组织下全部用户的职务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orgCode", value = "组织code", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "appcode", dataType = "String", paramType = "query")
    })
    @PostMapping("/findPositionOrgcodeAndUsername")
    public JsonResponse findPositionOrgcodeAndUsername(@RequestParam String orgCode,@RequestParam String appcode) {
        return JsonResponse.success(uumsSysPositionApi.findPositionOrgcodeAndUsername(orgCode,appcode));
    }
}



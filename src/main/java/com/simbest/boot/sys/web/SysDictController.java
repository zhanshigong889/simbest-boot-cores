/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.web;

import com.simbest.boot.base.web.controller.LogicController;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.sys.model.SysDict;
import com.simbest.boot.sys.service.ISysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.simbest.boot.base.web.response.JsonResponse.SUCCESS_CODE;
import static com.simbest.boot.constants.ApplicationConstants.MSG_SUCCESS;

/**
 * 用途：数据字典控制器
 * 作者: zlxtk
 * 时间: 2018/2/22  10:14
 */
@Api(description = "SysDictController", tags = {"系统管理-数据字典管理"})
@RestController
@RequestMapping("/sys/dict")
public class SysDictController extends LogicController<SysDict, String> {

    private ISysDictService sysDictService;

    @Autowired
    public SysDictController(ISysDictService sysDictService) {
        super(sysDictService);
        this.sysDictService=sysDictService;
    }

    /**
     * 新增一个字典类型
     * @param sysDict
     * @return
     */
    //设置权限，后面再开启
    //@PreAuthorize ("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @ApiOperation(value = "新增一个字典类型", notes = "新增一个字典类型")
    public JsonResponse create(@RequestBody(required = false) SysDict sysDict) {
        JsonResponse response = super.create( sysDict );
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     * 修改一个字典类型
     * @param sysDict
     * @return
     */
    //设置权限，后面再开启
    //@PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @ApiOperation(value = "修改一个字典类型", notes = "修改一个字典类型")
    public JsonResponse update( @RequestBody(required = false) SysDict sysDict) {
        JsonResponse response = super.update(sysDict );
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     * 根据id逻辑删除
     * @param id
     * @return
     */
    //@PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @ApiOperation(value = "根据id删除字典类型", notes = "根据id删除字典类型")
    @ApiImplicitParam(name = "id", value = "字典类型ID",  dataType = "String", paramType = "query")
    public JsonResponse deleteById(@RequestParam(required = false) String id) {
        JsonResponse response = super.deleteById( id );
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     * 先修改再逻辑删除字典类型
     * @param sysDict
     * @return
     */
    @ApiOperation(value = "先修改再逻辑删除字典类型", notes = "先修改再逻辑删除字典类型")
    public JsonResponse delete(@RequestBody(required = false) SysDict sysDict) {
        JsonResponse response = super.delete(sysDict);
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     * 批量逻辑删除字典类型
     * @param ids
     * @return JsonResponse
     */
    //@PreAuthorize("hasAuthority('ROLE_SUPER')")  // 指定角色权限才能操作方法
    @ApiOperation(value = "批量逻辑删除字典类型", notes = "批量逻辑删除字典类型")
    public JsonResponse deleteAllByIds(@RequestBody(required = false) String[] ids) {
        JsonResponse response = super.deleteAllByIds(ids);
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     *修改可见
     * @param id
     * @param enabled
     * @return
     */
    @ApiOperation(value = "修改可见", notes = "修改可见")
    @ApiImplicitParams ({@ApiImplicitParam(name = "id", value = "角色ID", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "enabled", value = "是否可用", required = true, dataType = "Boolean", paramType = "query")
    })
    public JsonResponse updateEnable(@RequestParam(required = false) String id, @RequestParam(required = false) Boolean enabled) {
        JsonResponse response = super.updateEnable( id,enabled );
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    //批量修改可见

    /**
     *根据id查询字典类型信息
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id查询字典类型信息", notes = "根据id查询字典类型信息")
    @ApiImplicitParam(name = "id", value = "字典类型ID", dataType = "String", paramType = "query")
    @PostMapping(value = {"/findById","/findById/sso"})
    public JsonResponse findById(@RequestParam(required = false) String id) {
        JsonResponse response = super.findById( id );
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     *获取字典类型信息列表并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param sysDict
     * @return
     */
    @ApiOperation(value = "获取字典类型信息列表并分页", notes = "获取字典类型信息列表并分页")
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
    @PostMapping(value = {"/findAll","/findAll/sso"})
    public JsonResponse findAll( @RequestParam(required = false, defaultValue = "1") int page, //
                                 @RequestParam(required = false, defaultValue = "10") int size, //
                                 @RequestParam(required = false) String direction, //
                                 @RequestParam(required = false) String properties, //
                                 @RequestBody(required = false) SysDict sysDict //
    ) {
        return super.findAll( page,size,direction, properties,sysDict);
    }


    /**
     * 获取字典树不分页
     *
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/findDictTree")
    public JsonResponse findDictTree() {
        Iterable<SysDict> roots = sysDictService.findAllNoPage();
        return JsonResponse.success( roots);
    }


    /**
     * 新增下级 "sys/dict/createChild"
     *
     * @param dict
     * @return
     */
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")  // 指定角色权限才能操作方法
    @PostMapping(value = "/createChild")
    public JsonResponse createChild(@RequestBody(required = false) SysDict dict) {
        if (dict == null) {
            return JsonResponse.defaultErrorResponse();
        }
        dict.setParentId(dict.getId());
        dict.setId(null);
        SysDict newDict = sysDictService.insert(dict);
        return JsonResponse.success(null, MSG_SUCCESS);
    }


    /**
     * 获取json格式数据字典
     * @return
     */
    //@PreAuthorize("hasAuthority('ROLE_USER')")  // 指定角色权限才能操作方法
    @PostMapping(value = "/listJson")
    public JsonResponse listJson() {
        List<SysDict> list = sysDictService.findByEnabled(true);
        return JsonResponse.builder().errcode(SUCCESS_CODE).message(MSG_SUCCESS).data(list).build();
    }


   /* @ApiOperation(value = "查询字段树", notes = "通过此接口来查询字段树信息")
    @ApiImplicitParam(name = "id", value = "字典ID", required = true, dataType = "Long", paramType = "path")
    @RequestMapping(value = "/select/tree", method = RequestMethod.GET)
    public ModelAndView getSelectTree() {
        // 获取查询结果
        List<SysDict> pages = sysDictService.findByEnabled(true);

        Map<String, Object> map = new HashMap<>();
        map.put("list", pages);
        map.put("size", pages.size());

        Map<String, Object> maps = new HashMap<>();
        maps.put("tree", map);
        return new ModelAndView("sys/sysdict/chooseDictValue", "dictModel", maps);
    }*/
}

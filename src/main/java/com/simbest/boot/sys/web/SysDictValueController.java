/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.web;

import com.simbest.boot.base.web.controller.LogicController;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.sys.service.ISysDictService;
import com.simbest.boot.sys.service.ISysDictValueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.simbest.boot.base.web.response.JsonResponse.SUCCESS_CODE;
import static com.simbest.boot.constants.ApplicationConstants.MSG_SUCCESS;

/**
 * 用途：数据字典值控制器
 * 作者: zlxtk
 * 时间: 2018/2/23  10:14
 */
@Api(description = "SysDictValueController", tags = {"系统管理-数据字典值管理"})
@RestController
@RequestMapping("/sys/dictValue")
public class SysDictValueController extends LogicController<SysDictValue,String>{

    private ISysDictValueService sysDictValueService;

    @Autowired
    private ISysDictService dictService;

    @Autowired
    public SysDictValueController(ISysDictValueService sysDictValueService) {
        super(sysDictValueService);
        this.sysDictValueService=sysDictValueService;
    }

    /**
     * 新增一个字典值
     * @param sysDictValue
     * @return JsonResponse
     */
    //设置权限，后面再开启
    //@PreAuthorize ("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @ApiOperation(value = "新增一个字典值", notes = "新增一个字典值")
    public JsonResponse create(@RequestBody(required = false) SysDictValue sysDictValue) {
        JsonResponse response = super.create( sysDictValue );
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     * 修改一个字典值
     * @param sysDictValue
     * @return JsonResponse
     */
    //设置权限，后面再开启
    //@PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @ApiOperation(value = "修改一个字典值", notes = "修改一个字典值")
    public JsonResponse update( @RequestBody(required = false) SysDictValue sysDictValue) {
        JsonResponse response = super.update(sysDictValue );
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     * 根据id逻辑删除
     * @param id
     * @return JsonResponse
     */
    //@PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @ApiOperation(value = "根据id删除字典值", notes = "根据id删除字典值")
    @ApiImplicitParam (name = "id", value = "字典值ID",  dataType = "String", paramType = "query")
    public JsonResponse deleteById(@RequestParam(required = false) String id) {
        JsonResponse response = super.deleteById( id );
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     * 先修改再逻辑删除字典值
     * @param sysDictValue
     * @return JsonResponse
     */
    @ApiOperation(value = "先修改再逻辑删除字典值", notes = "先修改再逻辑删除字典值")
    public JsonResponse delete(SysDictValue sysDictValue) {
        JsonResponse response = super.delete(sysDictValue);
        if(response.getErrcode().equals(SUCCESS_CODE)) {
            response.setMessage(MSG_SUCCESS);
        }
        return response;
    }

    /**
     * 批量逻辑删除字典值
     * @param ids
     * @return JsonResponse
     */
    //@PreAuthorize("hasAuthority('ROLE_SUPER')")  // 指定角色权限才能操作方法
    @ApiOperation(value = "批量逻辑删除字典值", notes = "批量逻辑删除字典值")
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
     * @return JsonResponse
     */
    @ApiOperation(value = "修改可见", notes = "修改可见")
    @ApiImplicitParams ({@ApiImplicitParam(name = "id", value = "字典值ID", required = true, dataType = "String", paramType = "query"),
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
     *根据id查询字典值
     * @param id
     * @return JsonResponse
     */
    @ApiOperation(value = "根据id查询字典值", notes = "根据id查询字典值")
    @ApiImplicitParam(name = "id", value = "字典类型ID", dataType = "String", paramType = "query")
    @PostMapping(value = {"/findById","/findById/sso","/findById/api"})
    public JsonResponse findById(@RequestParam(required = false) String id) {
        return super.findById( id );
    }

    /**
     *获取字典值信息列表并分页
     * @param page
     * @param size
     * @param direction
     * @param properties
     * @param sysDictValue
     * @return JsonResponse
     */
    @ApiOperation(value = "获取字典值信息列表并分页", notes = "获取字典值信息列表并分页")
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
    @PostMapping(value = {"/findAll","/findAll/sso","/findAll/api"})
    public JsonResponse findAll( @RequestParam(required = false, defaultValue = "1") int page, //
                                 @RequestParam(required = false, defaultValue = "10") int size, //
                                 @RequestParam(required = false) String direction, //
                                 @RequestParam(required = false) String properties, //
                                 @RequestBody(required = false) SysDictValue sysDictValue //
    ) {
        return super.findAll( page,size,direction, properties,sysDictValue);
    }

    /**
     * 新增子字典值
     * @param dictValue
     * @return JsonResponse
     */
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")  // 指定角色权限才能操作方法
    @PostMapping(value = "/createChild")
    @ResponseBody
    public JsonResponse createChild(@RequestBody(required = false) SysDictValue dictValue) {
        if (dictValue == null) {
            return JsonResponse.defaultErrorResponse();
        }
        dictValue.setParentId(dictValue.getId());
        dictValue.setId(null);
        SysDictValue newSysDictValue = sysDictValueService.insert(dictValue);
        return JsonResponse.defaultSuccessResponse();
    }

    /**
     *
     * @param sysDictValue
     * @return JsonResponse
     */
    @ApiOperation (value = "根据字典值对象查询满足条件的数据字典值，若提供上级数据字典值id，则直接返回所有字典值")
    @PostMapping(value = {"/findDictValue", "/findDictValue/sso", "/findDictValue/api"})
    public JsonResponse findDictValue(@RequestBody(required = false) SysDictValue sysDictValue){
        return JsonResponse.success(sysDictValueService.findDictValue(sysDictValue));
    }

    /**
     *
     * @param dictType
     * @param name
     * @return JsonResponse
     */
    @ApiOperation (value = "根据字典类型和字典值名称，获取字典值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dictType", value = "字典类型", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "name", value = "字典值名称", dataType = "String", paramType = "query", required = true)
    })
    @PostMapping(value = {"/findByDictTypeAndName", "/findByDictTypeAndName/sso", "/findByDictTypeAndName/api"})
    public JsonResponse findByDictTypeAndName(@RequestParam String dictType, @RequestParam String name){
        return JsonResponse.success(sysDictValueService.findByDictTypeAndName(dictType, name));
    }

    /**
     *
     * @param dictType
     * @param name
     * @param blocid
     * @param corpid
     * @return JsonResponse
     */
    @ApiOperation (value = "根据字典类型和字典值名称，以及集团Id、企业Id，获取字典值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dictType", value = "字典类型", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "name", value = "字典值名称", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "blocid", value = "集团Id", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "corpid", value = "企业Id", dataType = "String", paramType = "query", required = true)
    })
    @PostMapping(value = {"/findByDictTypeAndNameAndBlocidAndCorpid", "/findByDictTypeAndNameAndBlocidAndCorpid/sso", "/findByDictTypeAndNameAndBlocidAndCorpid/api"})
    public JsonResponse findByDictTypeAndNameAndBlocidAndCorpid(@RequestParam String dictType, @RequestParam String name,
                                                                @RequestParam String blocid, @RequestParam String corpid){
        return JsonResponse.success(sysDictValueService.findByDictTypeAndNameAndBlocidAndCorpid(dictType, name, blocid, corpid));
    }

    /**
     *
     * @return JsonResponse
     */
    @ApiOperation(value = "查看数据字典的所有值", notes = "查看数据字典的所有值")
    @PostMapping(value = {"/findAllDictValue", "/findAllDictValue/sso", "/findAllDictValue/api"})
    public JsonResponse findAllDictValue(){
        return JsonResponse.success(sysDictValueService.findAllDictValue());
    }

    /**
     *
     * @return JsonResponse
     */
    @ApiOperation(value = "查看数据字典的所有值, Map结构，key为dictType，value为字典值list")
    @PostMapping(value = {"/findAllDictValueMapList", "/findAllDictValueMapList/sso", "/findAllDictValueMapList/api"})
    public JsonResponse findAllDictValueMapList(){
        return JsonResponse.success(sysDictValueService.findAllDictValueMapList());
    }

    /**
     *
     * @return JsonResponse
     */
    @ApiOperation(value = "查看指定数据字典类型的字典值, Map结构，key为dictType，value为字典值list")
    @PostMapping(value = {"/findDictValueMapList", "/findDictValueMapList/sso", "/findDictValueMapList/api"})
    public JsonResponse findDictValueMapList(@ApiParam(name = "typeList", value = "字典类型") @RequestBody String[] typeList){
        return JsonResponse.success(sysDictValueService.findDictValueMapList(typeList));
    }


}

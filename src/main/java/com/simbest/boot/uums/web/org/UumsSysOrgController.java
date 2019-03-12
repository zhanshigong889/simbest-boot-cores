/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.org;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.uums.api.org.UumsSysOrgApi;
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

@Api (description = "系统组织操作相关接口" )
@RestController
@RequestMapping(value = {"/uums/sys/org", "/sys/uums/org"})
public class UumsSysOrgController {

    @Autowired
    private UumsSysOrgApi uumsSysOrgApi;

    /**
     * 查看某个父组织的子组织
     * @param appcode
     * @param orgCode
     * @return
     */
    @ApiOperation (value = "查看某个父组织的子组织", notes = "查看某个父组织的子组织")
    @ApiImplicitParams( {
            @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "orgCode", value = "组织code", dataType = "String", paramType = "query")
    } )
    @PostMapping ("/findSonByParentOrgId")
    public JsonResponse findSonByParentOrgId( @RequestParam  String appcode,@RequestParam String orgCode) {
        return JsonResponse.success(uumsSysOrgApi.findSonByParentOrgId( appcode,orgCode ));
    }

    /**
     *页面初始化时获取根组织以及根组织下一级组织
     * @param appcode
     * @return
     */
    @ApiOperation (value = "页面初始化时获取根组织以及根组织下一级组织", notes = "页面初始化时获取根组织以及根组织下一级组织")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping ("/findRootAndNextRoot")
    public JsonResponse findRootAndNextRoot( @RequestParam  String appcode) {
        return JsonResponse.success(uumsSysOrgApi.findRootAndNext( appcode ));
    }

    /**
     * 出省公司以及地市分公司，还有省公司下的部门
     * @param appcode
     * @return
     */
    @ApiOperation (value = "出省公司以及地市分公司，还有省公司下的部门", notes = "出省公司以及地市分公司，还有省公司下的部门")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping ("/findPOrgAndCityOrg")
    public JsonResponse findPOrgAndCityOrg( @RequestParam  String appcode) {
        return JsonResponse.success(uumsSysOrgApi.findPOrgAndCityOrg( appcode ));
    }

    /**
     * 查出用户所在的市公司下的部门以及市公司下的县公司
     * @param appcode
     * @return
     */
    @ApiOperation (value = "查出用户所在的市公司下的部门以及市公司下的县公司", notes = "查出用户所在的市公司下的部门以及市公司下的县公司")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping ("/findCityDeapartmentAndCountyCompany")
    public JsonResponse findCityDeapartmentAndCountyCompany( @RequestParam  String appcode) {
        return JsonResponse.success(uumsSysOrgApi.findCityDeapartmentAndCountyCompany( appcode ));
    }

    /**
     * 根据用户名以及规则出组织
     * @param appcode
     * @param userMap
     * @return
     */
    @ApiOperation (value = "根据用户名以及规则出组织", notes = "根据用户名以及规则出组织")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping ("/findOrgByUserMap")
    public JsonResponse findCityDeapartmentAndCountyCompany( @RequestParam(required = false)  String appcode, @RequestBody(required = false) Map userMap) {
        return JsonResponse.success(uumsSysOrgApi.findOrgByUserMap(appcode,userMap));
    }

    @ApiOperation(value = "根据corpId查询企业根节点", notes = "根据corpId查询企业根节点")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "corpId", value = "企业id", dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/findRootByCorpId")
    public JsonResponse findRootByCorpId(@RequestParam(required = false)  String appcode,@RequestParam String corpId) {
        return JsonResponse.success(uumsSysOrgApi.findRootByCorpId(appcode,corpId));
    }
}



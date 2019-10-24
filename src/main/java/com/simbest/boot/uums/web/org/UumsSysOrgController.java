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

@Api (description = "系统组织操作相关接口",tags={"组织api"}  )
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
     * 查看某个父组织的子组织,返回树形结构UserOrgTree
     * @param appcode
     * @param mapOrg
     * @return
     */
    @ApiOperation (value = "查看某个父组织的子组织,返回树形结构UserOrgTree", notes = "查看某个父组织的子组织,返回树形结构UserOrgTree")
    @ApiImplicitParams( {
            @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    } )
    @PostMapping ("/findSonByParentOrgIdTree")
    public JsonResponse findSonByParentOrgIdTree( @RequestParam  String appcode,@RequestBody Map<String,Object> mapOrg) {
        return JsonResponse.success(uumsSysOrgApi.findSonByParentOrgIdTree( appcode,mapOrg ));
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
     * 出某些组织之出省公司以及18个地市分公司，不要省飞达等公司
     * @param appcode
     * @return
     */
    @ApiOperation (value = "出某些组织之出省公司以及18个地市分公司，不要省飞达等公司", notes = "出某些组织之出省公司以及18个地市分公司，不要省飞达等公司")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping ("/findPOrgAnd18CityOrg")
    public JsonResponse findPOrgAnd18CityOrg( @RequestParam  String appcode) {
        return JsonResponse.success(uumsSysOrgApi.findPOrgAnd18CityOrg( appcode ));
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

    /**
     * 根据corpId查询企业根节点
     * @param appcode
     * @param corpId
     * @return
     */
    @ApiOperation(value = "根据corpId查询企业根节点", notes = "根据corpId查询企业根节点")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "corpId", value = "企业id", dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/findRootByCorpId")
    public JsonResponse findRootByCorpId(@RequestParam(required = false)  String appcode,@RequestParam String corpId) {
        return JsonResponse.success(uumsSysOrgApi.findRootByCorpId(appcode,corpId));
    }

    /**
     * 用于查询当前人所在的组织树，直到企业的顶级
     * @param corpMap
     * @return
     */
    @ApiOperation(value = "用于查询当前人所在的组织树，直到企业的顶级", notes = "用于查询当前人所在的组织树，直到企业的顶级")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping(value = {"/findOrgTreeFromCorp","/findOrgTreeFromCorp/sso"})
    public JsonResponse findOrgTreeFromCorp(@RequestParam(required = false) String appcode, @RequestBody(required = false) Map<String,Object> corpMap) {
        return JsonResponse.success(uumsSysOrgApi.findUserTreeFromCorp(appcode,corpMap));
    }

    /**
     * 出某些组织的上级组织以及它的全部下级组织以及某些组织的上级组织
     * @param orgMap
     * @return
     */
    @ApiOperation(value = "出某些组织的上级组织以及它的全部下级组织以及某些组织的上级组织", notes = "出某些组织的上级组织以及它的全部下级组织以及某些组织的上级组织")
    @ApiImplicitParam(name = "appcode", value = "应用code", dataType = "String", paramType = "query")
    @PostMapping(value = {"/findRuleOrgTree","/findRuleOrgTree/sso"})
    public JsonResponse findRuleOrgTree(@RequestParam(required = false) String appcode, @RequestBody(required = false) Map<String,Object> orgMap) {
        return JsonResponse.success(uumsSysOrgApi.findRuleOrgTree(appcode,orgMap));
    }

    /**
     * 根据组织code查询组织信息
     * @param orgCode
     */
    @ApiOperation(value = "特殊查询之根据组织编码查询存在的组织列表", notes = "根据组织编码查询存在的组织列表",tags={"组织 组织特殊查询"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orgCode", value = "组织编码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value = {"/findListByOrgCode","/findListByOrgCode/sso"})
    public JsonResponse findListByOrgCode( @RequestParam(required = false) String orgCode,@RequestParam(required = false) String appcode) {
        return JsonResponse.success(uumsSysOrgApi.findListByOrgCode(orgCode,appcode));
    }


    /**
     * 查询某个子组织的父组织
     * @param orgCode
     * @param appcode
     * @return
     */
    @ApiOperation(value = "子父级之查看某个子组织的父组织", notes = "子父级之查看某个子组织的父组织",tags={"组织 组织特殊查询"})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orgCode", value = "组织编码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "appcode", value = "应用编码", dataType = "String", paramType = "query")
    })
    @PostMapping(value = {"/findParentBySon","/findParentBySon/sso"})
    public JsonResponse findParentBySon( @RequestParam(required = false) String orgCode,@RequestParam(required = false) String appcode) {
        return JsonResponse.success(uumsSysOrgApi.findParentBySon(orgCode,appcode));
    }

}



/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.web;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.sys.model.SysHealth;
import com.simbest.boot.sys.service.IHeartTestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用途：系统健康检查控制器
 * 作者: lishuyi
 * 时间: 2019/12/6  10:01
 */
@Api(description = "SysHealthController", tags = {"系统管理-系统健康检查控制器"})
@Slf4j
@RestController
@RequestMapping("/sys/health")
public class SysHealthController {

    @Autowired
    private IHeartTestService heartTestService;

    @ApiOperation(value = "系统心跳", notes = "系统心跳")
    @RequestMapping(value = "/anonymous/heart", method = {RequestMethod.HEAD})
    public JsonResponse healthHeart() {
        return JsonResponse.defaultSuccessResponse();
    }


    @ApiOperation(value = "系统健康检查", notes = "系统健康检查")
    @PostMapping(value = "/anonymous/check")
    public SysHealth healthCheck() {
        return heartTestService.doTest();
    }


}

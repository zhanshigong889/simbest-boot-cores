/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.web;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.sys.service.ISimpleSmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用途：简单短信接口控制器
 * 作者: lishuyi
 * 时间: 2019/8/10  9:16
 */
@Api(description = "SimpleSmsController", tags = {"系统管理-短信管理"})
@Slf4j
@RestController
@RequestMapping("/sys/sms")
public class SimpleSmsController {

    @Autowired
    private ISimpleSmsService smsService;

    @ApiOperation(value = "发送随机验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "接收手机号", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "randomCode", value = "随机数据码", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "minutes", value = "有效分钟数", dataType = "String", paramType = "query", required = true)
    })
    @PostMapping(value = {"/sendRandomCode", "/sso/sendRandomCode", "/api/sendRandomCode"})
    public JsonResponse sendRandomCode(@RequestParam String phone,
                                       @RequestParam String randomCode,
                                       @RequestParam int minutes) {
        if (smsService.sendRandomCode(phone, randomCode, minutes)) {
            return JsonResponse.defaultSuccessResponse();
        } else {
            return JsonResponse.defaultErrorResponse();
        }
    }
}

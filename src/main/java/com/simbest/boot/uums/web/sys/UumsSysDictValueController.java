/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.uums.web.sys;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.uums.api.sys.UumsSysDictValueApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用途：操作UUMS应用中的数据字典值
 * 作者: lishuyi
 * 时间: 2018/4/8  11:55
 */
@Api (description = "UUMS的数据字典相关接口",tags={"UUMS的数据字典"} )
@RestController
@RequestMapping("/uums/sys/dictvalue")
public class UumsSysDictValueController {

    @Autowired
    private UumsSysDictValueApi uumsSysDictValueApi;


    /**
     * pc单表条件查询
     * @param sysDictValue
     * @return
     */
    @ApiOperation(value = "pc单表条件查询", notes = "pc单表条件查询")
    @PostMapping("/findDictValue")
    public JsonResponse findDictValue(@RequestBody SysDictValue sysDictValue) {
        List<SysDictValue> list =uumsSysDictValueApi.findDictValue(sysDictValue);
        return JsonResponse.success(list);
    }

}



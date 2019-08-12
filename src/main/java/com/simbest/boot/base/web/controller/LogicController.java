/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.base.web.controller;

import com.simbest.boot.base.model.LogicModel;
import com.simbest.boot.base.service.ILogicService;
import com.simbest.boot.base.web.response.JsonResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;


/**
 * 用途：通用类对象控制器
 * 作者: lishuyi
 * 时间: 2018/6/7  15:59
 */
public class LogicController<T extends LogicModel, PK extends Serializable> extends GenericController<T, PK>{

    private ILogicService<T, PK> service;

    public LogicController(ILogicService<T, PK> service) {
        super(service);
        this.service = service;
    }

    @PostMapping(value = {"/updateEnable", "/sso/updateEnable", "/api/updateEnable"})
    public JsonResponse updateEnable(@RequestParam PK id, @RequestParam boolean enabled) {
        return JsonResponse.success(service.updateEnable(id, enabled));
    }



}

/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.base.web.controller;

import com.simbest.boot.base.exception.GlobalExceptionRegister;
import com.simbest.boot.base.model.GenericModel;
import com.simbest.boot.base.repository.Condition;
import com.simbest.boot.base.service.IGenericService;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.util.CustomBeanUtil;
import com.simbest.boot.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;


/**
 * 用途：通用类对象控制器
 * 作者: lishuyi
 * 时间: 2018/6/7  15:59
 */
@Slf4j
public class GenericController<T extends GenericModel, PK extends Serializable> {

    private IGenericService<T, PK> service;

    public GenericController(IGenericService<T, PK> service) {
        this.service = service;
    }

    @PostMapping(value = {"/findById", "/sso/findById", "/api/findById"})
    public JsonResponse findById(@RequestParam PK id) {
        return JsonResponse.success(service.findById(id));
    }

    @PostMapping(value = {"/findAll", "/sso/findAll", "/api/findAll"})
    public JsonResponse findAll(@RequestParam(required = false, defaultValue = "1") int page, //
                                @RequestParam(required = false, defaultValue = "10") int size, //
                                @RequestParam(required = false) String direction, //
                                @RequestParam(required = false) String properties,
                                @RequestBody T o) {
        //获取分页规则, page第几页 size每页多少条 direction升序还是降序 properties排序规则（属性名称）
        Pageable pageable = service.getPageable(page, size, direction, properties);
        // 获取查询条件
        Condition condition = new Condition();
        Map<String, Object> params = ObjectUtil.getEntityPersistentFieldValueExceptId(o);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            condition.eq(entry.getKey(), entry.getValue());
        }
        Specification<T> specification = service.getSpecification(condition);
        // 获取查询结果
        Page<T> pages = service.findAll(specification, pageable);
        return JsonResponse.success(pages);
    }

    @PostMapping(value = {"/findAllNoPage", "/sso/findAllNoPage", "/api/findAllNoPage"})
    public JsonResponse findAllNoPage(@RequestBody T o) {
        // 获取查询条件
        Condition condition = new Condition();
        Map<String, Object> params = ObjectUtil.getEntityPersistentFieldValueExceptId(o);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            condition.eq(entry.getKey(), entry.getValue());
        }
        Specification<T> specification = service.getSpecification(condition);
        Iterable<T> datas = service.findAllNoPage(specification);
        return JsonResponse.success(datas);
    }

    @PostMapping(value = {"/create", "/sso/create", "/api/create"})
    public JsonResponse create(@RequestBody T o) {
        try {
            o = service.insert(o);
            return JsonResponse.success(o);
        } catch (Exception e) {
            return GlobalExceptionRegister.returnErrorResponse(e);
        }
    }

    @PostMapping(value = {"/update", "/sso/update", "/api/update"})
    public JsonResponse update(@RequestBody T newObj) {
        T oldObj = service.findById((PK) ObjectUtil.getEntityIdVaue(newObj));
        CustomBeanUtil.copyPropertiesIgnoreNull(newObj, oldObj);
        try {
            oldObj = service.update(oldObj);
            return JsonResponse.success(oldObj);
        } catch (Exception e) {
            return GlobalExceptionRegister.returnErrorResponse(e);
        }
    }

    @PostMapping(value = {"/deleteById", "/sso/deleteById", "/api/deleteById"})
    public JsonResponse deleteById(@RequestParam PK id) {
        try {
            service.deleteById(id);
            return JsonResponse.defaultSuccessResponse();
        } catch (Exception e) {
            return GlobalExceptionRegister.returnErrorResponse(e);
        }
    }

    @PostMapping(value = {"/delete", "/sso/delete", "/api/delete"})
    public JsonResponse delete(@RequestBody T o) {
        try {
            service.delete(o);
            return JsonResponse.defaultSuccessResponse();
        } catch (Exception e) {
            return GlobalExceptionRegister.returnErrorResponse(e);
        }
    }

    @PostMapping(value = {"/deleteAllByIds", "/sso/deleteAllByIds", "/api/deleteAllByIds"})
    public JsonResponse deleteAllByIds(@RequestBody PK[] ids) {
        try {
            service.deleteAllByIds(Arrays.asList(ids));
            return JsonResponse.defaultSuccessResponse();
        } catch (Exception e) {
            return GlobalExceptionRegister.returnErrorResponse(e);
        }
    }

}

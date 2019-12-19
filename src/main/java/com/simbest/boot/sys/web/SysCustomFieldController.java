/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.web;

import com.google.common.collect.Maps;
import com.simbest.boot.base.enums.SysCustomFieldType;
import com.simbest.boot.base.repository.Condition;
import com.simbest.boot.base.web.controller.LogicController;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.sys.model.SysCustomField;
import com.simbest.boot.sys.model.SysCustomFieldValue;
import com.simbest.boot.sys.model.SysDict;
import com.simbest.boot.sys.repository.SysCustomFieldRepository;
import com.simbest.boot.sys.service.ISysCustomFieldService;
import com.simbest.boot.sys.service.ISysCustomFieldValueService;
import com.simbest.boot.sys.service.ISysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用途：实体自定义字段控制器
 * 作者: lishuyi
 * 时间: 2017/12/22  15:51
 */
@Api(description = "SysCustomFieldController", tags = {"系统管理-自定义字段管理"})
@Slf4j
@RestController
@RequestMapping("/sys/sysfield")
public class SysCustomFieldController extends LogicController<SysCustomField, String> {

    private ISysCustomFieldService fieldService;

    @Autowired
    private ISysCustomFieldValueService fieldValueService;

    @Autowired
    private SysCustomFieldRepository fieldRepository;

    @Autowired
    private ISysDictService dictService;

    @Autowired
    public SysCustomFieldController(ISysCustomFieldService fieldService) {
        super(fieldService);
        this.fieldService=fieldService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ModelAndView list(@RequestParam int page, @RequestParam int size, //
                             @RequestParam(required = false, defaultValue = "") String fieldClassify, //
                             @RequestParam(required = false) SysCustomField field, Model model) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("fieldClassify", fieldClassify);

        Condition c = new Condition();
        c.like("fieldClassify", "%" + fieldClassify + "%");

        // 生成排序规则
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageable = PageRequest.of(page - 1, size, sort);

        Specification<SysCustomField> s = fieldService.getSpecification(c);
        Page allField = fieldService.findAll(s, pageable);

        model.addAttribute("totalSize", allField.getTotalElements());
        model.addAttribute("totalPage", allField.getTotalPages());
        model.addAttribute("fieldList", allField.getContent());
        model.addAttribute("size", allField.getSize());
        model.addAttribute("page", page);
        model.addAttribute("title", "自定义字段列表");
        model.addAttribute("paramMap", paramMap);
        model.addAttribute("fieldClassifyMap", fieldService.getFieldClassifyMap());

        return new ModelAndView("sys/sysfield/list", "fieldModel", model);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @RequestMapping(value = "getById", method = RequestMethod.GET)
    public ModelAndView getById(@RequestParam(required = false, defaultValue = "-1") final String id, Model model) {
        SysCustomField field = fieldService.findById(id);
        model.addAttribute("field", field);
        model.addAttribute("fieldClassifyMap", fieldService.getFieldClassifyMap());
        model.addAttribute("dictList", dictService.findByEnabled(true));
        SysCustomFieldType[] types = SysCustomFieldType.values();
        Map<String, String> typeMap = Maps.newTreeMap();
        for (SysCustomFieldType type : types) {
            typeMap.put(type.name(), type.getValue());
        }
        model.addAttribute("fieldTypes", typeMap);
        return new ModelAndView("sys/sysfield/form", "fieldModel", model);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping(value = "/create")
    public JsonResponse create(@RequestBody SysCustomField field) {
        return super.create(field);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @PostMapping(value = "/update")
    public JsonResponse update(@RequestBody SysCustomField field) {
        return super.update(field);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER','ROLE_ADMIN')")
    @ApiOperation(value = "根据id删除自定义字段", notes = "根据id删除自定义字段")
    public JsonResponse deleteById(@RequestParam(required = false) String id) {
        return super.deleteById( id );
    }

    @ApiOperation(value = "获取自定义字段列表", notes = "通过此接口来获取某实体类型自定义字段")
    @ApiImplicitParams({ //
            @ApiImplicitParam(name = "page", value = "当前页码", dataType = "int", paramType = "query", required = true, example = "1"), //
            @ApiImplicitParam(name = "size", value = "每页数量", dataType = "int", paramType = "query", required = true, example = "10"), //
            @ApiImplicitParam(name = "fieldClassify", value = "所属实体分类", dataType = "String", paramType = "query", required = true),
            @ApiImplicitParam(name = "fieldClassify", value = "所属实体Id", dataType = "String", paramType = "query", required = true)
    })
    @PostMapping(value = "getSysCustomFieldsByFieldClassify")
    public JsonResponse getSysCustomFieldsByFieldClassify(@RequestParam(required = false, defaultValue = "1") int page, //
                                                          @RequestParam(required = false, defaultValue = "10") int size, //
                                                          @RequestParam String fieldClassify, //
                                                          @RequestParam String fieldEntityId //
    ) {
        // 获取分页规则
        Pageable pageable = fieldRepository.getPageable(page, size, null, null);

        // 获取查询条件
        Condition condition = new Condition();
        condition.eq("fieldClassify", fieldClassify);

        Specification<SysCustomField> s = fieldRepository.getSpecification(condition);

        // 获取查询结果
        Page<SysCustomField> pages = fieldRepository.findAll(s, pageable);

        // 构成返回信息
        Map<String, Object> searchD = new HashMap<>();
        searchD.put("fieldClassify", fieldClassify);
        SysCustomField param = new SysCustomField();
        param.setFieldClassify(fieldClassify);
        searchD.put("fieldClassifyCn", param.getFieldClassifyCn());

        Map<String, Object> map = new HashMap<>();
        map.put("totalSize", pages.getTotalElements());
        map.put("totalPage", pages.getTotalPages());
        map.put("list", pages.getContent());
        map.put("size", pages.getSize());
        map.put("page", page);
        map.put("searchD", searchD);

        //当fieldEntityId存在时，即某个实体类型存在时，读取该实体类型已经设置过的自定义字段的字段值
        if (StringUtils.isNotEmpty(fieldEntityId)) {
            Condition condition1 = new Condition();
            condition1.eq("fieldClassify", fieldClassify);
            condition1.eq("fieldEntityId", fieldEntityId);
            Specification<SysCustomFieldValue> s1 = fieldValueService.getSpecification(condition1);
            Iterable<SysCustomFieldValue> values = fieldValueService.findAllNoPage(s1);
            map.put("values", values);
        }

        JsonResponse res = JsonResponse.builder() //
                .errcode(JsonResponse.SUCCESS_CODE) //
                .message("查询成功！") //
                .data(map)
                .build();
        return res;
    }
}

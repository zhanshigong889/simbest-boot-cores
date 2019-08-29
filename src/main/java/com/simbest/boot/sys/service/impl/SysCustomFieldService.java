/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service.impl;


import com.simbest.boot.base.annotations.AnnotationUtils;
import com.simbest.boot.base.repository.Condition;
import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysCustomField;
import com.simbest.boot.sys.model.SysDict;
import com.simbest.boot.sys.repository.SysCustomFieldRepository;
import com.simbest.boot.sys.repository.SysDictRepository;
import com.simbest.boot.sys.service.ISysCustomFieldService;
import com.simbest.boot.util.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 用途：实体自定义字段逻辑层
 * 作者: lishuyi
 * 时间: 2017/12/22  15:51
 */
@Service
@CacheConfig(cacheNames = ApplicationConstants.REDIS_DEFAULT_CACHE_PREFIX)
public class SysCustomFieldService extends LogicService<SysCustomField, String> implements ISysCustomFieldService {

    private SysCustomFieldRepository fieldRepository;

    @Autowired
    public SysCustomFieldService(SysCustomFieldRepository fieldRepository ) {
        super(fieldRepository);
        this.fieldRepository = fieldRepository;
    }

    @Autowired
    private AnnotationUtils annotationUtils;

    @Override
    public Map<String, String> getFieldClassifyMap() {
        return annotationUtils.getEntityCnNameClassifyMap();
    }



    @Override
    public SysCustomField findByFieldClassify(String fieldClassify) {
        return fieldRepository.findByFieldClassify(fieldClassify);
    }

    @CachePut(key = "#p0.id")
    @Override
    public SysCustomField insert(SysCustomField field) {
        if (field.getId() == null) {
            field.setEnabled(true);
            field.setCreator(SecurityUtils.getCurrentUserName());
            field.setModifier(SecurityUtils.getCurrentUserName());
            return fieldRepository.save(field);
        } else {
            SysCustomField dbFiled = findById(field.getId());
            dbFiled.setEnabled(true);
            dbFiled.setFieldName(field.getFieldName());
            dbFiled.setModifier(SecurityUtils.getCurrentUserName());
            return fieldRepository.save(dbFiled);
        }
    }

}

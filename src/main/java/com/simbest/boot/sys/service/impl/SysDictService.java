package com.simbest.boot.sys.service.impl;


import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.exceptions.BusinessForbiddenException;
import com.simbest.boot.sys.model.SysDict;
import com.simbest.boot.sys.repository.SysDictRepository;
import com.simbest.boot.sys.service.ISysDictService;
import com.simbest.boot.util.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static com.simbest.boot.constants.AuthoritiesConstants.ACCESS_FORBIDDEN;
import static com.simbest.boot.constants.AuthoritiesConstants.BUSINESS_FORBIDDEN;
import static com.simbest.boot.constants.AuthoritiesConstants.SUPER_ADMIN;

@Service
public class SysDictService extends LogicService<SysDict, String> implements ISysDictService {

    private SysDictRepository dictRepository;

    @Autowired
    public SysDictService(SysDictRepository dictRepository ) {
        super(dictRepository);
        this.dictRepository = dictRepository;
    }

    @Override
    public SysDict findByDictType(String dictType){
        return dictRepository.findByDictType(dictType);
    }

    @Override
    public List<SysDict> findByParentId(String parentId) {
        return dictRepository.findByParentId(parentId);
    }


    @Override
    @Cacheable
    public List<SysDict> findByEnabled(Boolean enabled) {
        return dictRepository.findByEnabled(enabled);
    }




    @Override
    @Transactional
    public SysDict updateEnable(String id, boolean enabled) {
        if(SecurityUtils.hasPermission(SUPER_ADMIN))
            return super.updateEnable(id, enabled);
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public SysDict insert(SysDict source) {
        if(SecurityUtils.hasPermission(SUPER_ADMIN))
            return super.insert(source);
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public SysDict update(SysDict source) {
        if(SecurityUtils.hasPermission(SUPER_ADMIN))
            return super.update(source);
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public List<SysDict> saveAll(Iterable<SysDict> entities) {
        if(SecurityUtils.hasPermission(SUPER_ADMIN))
            return super.saveAll(entities);
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void deleteById(String id ) {
        if(SecurityUtils.hasPermission(SUPER_ADMIN))
            super.deleteById(id);
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void delete(SysDict o ) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends SysDict> iterable ) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void deleteAll() {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void deleteAllByIds(Iterable<? extends String> pks ) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void scheduleLogicDelete(String id, LocalDateTime localDateTime) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void scheduleLogicDelete(SysDict entity, LocalDateTime localDateTime) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

}

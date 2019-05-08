package com.simbest.boot.base.service.impl;

import com.google.common.collect.Sets;
import com.simbest.boot.base.model.GenericModel;
import com.simbest.boot.base.repository.Condition;
import com.simbest.boot.base.repository.GenericRepository;
import com.simbest.boot.base.service.IGenericService;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.exceptions.InsertExistObjectException;
import com.simbest.boot.exceptions.UpdateNotExistObjectException;
import com.simbest.boot.util.CustomBeanUtil;
import com.simbest.boot.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <strong>Title : 基础实体通用服务层</strong><br>
 * <strong>Description : 基础实体通用服务层</strong><br>
 * <strong>Create on : 14:52</strong><br>
 * <strong>Modify on : 14:52</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * 增加缓存
 * -------------------------------------------<br>
 */
@Slf4j
@CacheConfig(cacheNames = ApplicationConstants.REDIS_DEFAULT_CACHE_PREFIX)
public class GenericService<T extends GenericModel,PK extends Serializable> implements IGenericService<T,PK> {

    private GenericRepository<T,PK> genericRepository;

    public GenericService(){}

    public GenericService ( GenericRepository<T, PK> genericRepository ) {
        this.genericRepository = genericRepository;
    }

    @Override
    public Pageable getPageable() {
        return genericRepository.getPageable();
    }

    @Override
    public Pageable getPageable(int page, int size) {
        return genericRepository.getPageable(page, size);
    }

    @Override
    public Pageable getPageable(int page, int size, String direction, String properties) {
//        return genericRepository.getPageable(page, size, direction, properties);
        // 页码是从零开始的
        int pagePage = page < 1 ? 0 : (page - 1);
        int pageSize = size < 1 ? 1 : (size > 100 ? 100 : size);

        Pageable pageable;

        if (StringUtils.isNotEmpty(direction) && StringUtils.isNotEmpty(properties)) {
            // 生成指定排序规则-顺序
            Sort.Direction sortDirection;
            String[] sortProperties;
            try {
                // 先转换为大写
                direction = direction.toUpperCase();
                // 再获取枚举
                sortDirection = Sort.Direction.valueOf(direction);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                sortDirection = Sort.Direction.ASC;
            }

            // 生成指定排序规则-关键字
            sortProperties = properties.split(",");

            // 生成排序规则
            Sort sort = new Sort(sortDirection, sortProperties);
            pageable = PageRequest.of(pagePage, pageSize, sort);
        } else {
            pageable = PageRequest.of(pagePage, pageSize);
        }
        return pageable;
    }

    @Override
    public Specification<T> getSpecification(T entity) {
        return genericRepository.getSpecification(entity);
    }

    @Override
    public Specification<T> getSpecification(Map<String, Object> conditions) {
        return genericRepository.getSpecification(conditions);
    }

    @Override
    public Specification<T> getSpecification(Condition condition) {
        return genericRepository.getSpecification(condition);
    }

    /**
     * @see
     */
    @Override
    public long count ( ) {
        log.debug("@Generic Repository Service count null param");
        return genericRepository.count();
    }

    /**
     * @see
     */
    @Override
    public long count ( Specification<T> specification ) {
        log.debug("@Generic Repository Service count Specification param");
        return genericRepository.count( specification );
    }

    /**
     * @see
     */
    @Override
    public boolean exists ( PK id ) {
        log.debug("@Generic Repository Service exists object by id: " + id);
        return genericRepository.existsById( id );
    }

    /**
     * @see
     */
    @Override
    public T findOne ( PK id ) {
        log.debug("@Generic Repository Service getOne object by id: " + id);
        return genericRepository.getOne(id);
    }

    @Override
    public T findOne(Specification<T> conditions){
        log.debug("@Generic Repository Service findOne Specification param");
        return genericRepository.findOne(conditions).orElse(null);
    }

    /**
     * @see
     */
    @Override
    public T findById ( PK id ) {
        log.debug("@Generic Repository Service get single object by id: " + id);
        return genericRepository.findById(id).orElse( null );
    }

    /**
     * @see
     * @return
     */
    @Override
    public Page<T> findAll ( ) {
        log.debug("@Generic Repository Service findAll");
        return genericRepository.findAll(genericRepository.getPageable());
    }

    @Override
    public Iterable<T> findAllNoPage(){
        log.debug("@Generic Repository Service findAllNoPage");
        return genericRepository.findAll();
    }

    /**
     * @see
     */
    @Override
    public Page<T>  findAll ( Pageable pageable ) {
        log.debug("@Generic Repository Service findAll object PageSize:" + pageable.getPageSize() + ":PageNumber:" + pageable.getPageNumber());
        return genericRepository.findAll( pageable );
    }

    /**
     * @see
     * @param sort  排序字段
     * @return
     */
    @Override
    public Page<T>  findAll ( Sort sort ) {
        log.debug("@Generic Repository Service object by Sort");
        return genericRepository.findAll(PageRequest.of(ApplicationConstants.DEFAULT_PAGE, ApplicationConstants.DEFAULT_SIZE, sort));
    }

    @Override
    public Iterable<T> findAllByIDs(Iterable<PK> ids) {
        log.debug("@Generic Repository Service object by findAllByIDs");
        return genericRepository.findAllById(ids);
    }

    /**
     * @see
     */
    @Override
    public Page<T> findAll ( Specification<T> conditions, Pageable pageable ) {
        log.debug("@Generic Repository Service findAll Specification object PageSize:" + pageable.getPageSize() + ":PageNumber:" + pageable.getPageNumber());
        return genericRepository.findAll(conditions, pageable);
    }

    @Override
    public Iterable<T> findAllNoPage(Specification<T> conditions){
        log.debug("@Generic Repository Service object by findAllNoPage");
        return genericRepository.findAll(conditions);
    }

    /**
     * @see
     */
    @Override
    @Transactional
    public T insert ( T source ) {
        if(null == ObjectUtil.getEntityIdVaue(source)) {
            log.debug("@Generic Repository Service create new object: " + source);
            T target = genericRepository.save(source);
            CustomBeanUtil.copyTransientProperties(source,target);
            return target;
        } else {
            throw new InsertExistObjectException();
        }
    }

    @Override
    @Transactional
    public T update ( T source ) {
        PK pk = (PK)ObjectUtil.getEntityIdVaue(source);
        if(null != pk) {
            log.debug("@Generic Repository Service update a already object: " + source);
            T target = findById(pk);
            CustomBeanUtil.copyPropertiesIgnoreNull(source, target);
            T newTarget = genericRepository.save(target);
            CustomBeanUtil.copyTransientProperties(target,newTarget);
            return newTarget;
        } else {
            throw new UpdateNotExistObjectException();
        }
    }

    @Override
    @Transactional
    public List<T> saveAll(Iterable<T> entities){
        log.debug("@Generic Repository Service saveAll");
        return genericRepository.saveAll(entities);
    }


    /**
     * @see
     */
    @Override
    @Transactional
    public T saveAndFlush ( T o ) {
        log.debug("@Generic Repository Service saveAndFlush object:" + o);
        return genericRepository.saveAndFlush( o );
    }

    /**
     * @see
     */
    @Override
    @Transactional
    public void deleteById ( PK id ) {
        log.debug("@Generic Repository Service deleteById object by id: " + id);
        genericRepository.deleteById( id );
    }

    /**
     * @see
     */
    @Override
    @Transactional
    public void delete ( T o ) {
        log.debug("@Generic Repository Service delete object: " + o);
        genericRepository.delete( o );
    }

    /**
     * @see
     */
    @Override
    @Transactional
    public void deleteAll ( Iterable<? extends T> iterable ) {
        log.debug("@Generic Repository Service deleteAll Iterable param");
        genericRepository.deleteAll( iterable );
    }

    /**
     * @see
     */
    @Override
    @Transactional
    public void deleteAll ( ) {
        log.debug("@Generic Repository Service deleteAll null param");
        genericRepository.deleteAll();
    }

    /**
     * @see
     */
    @Override
    @Transactional
    public void deleteAllByIds ( Iterable<? extends PK> ids ) {
        log.debug("@Generic Repository Service deleteAllByIds Iterable param");
        Set<T> psSet = Sets.newHashSet();
        for(PK pk: ids){
            psSet.add(genericRepository.findById(pk).orElse(null));
        }
        genericRepository.deleteAll( psSet );
    }


}

package com.simbest.boot.base.service.impl;

import com.simbest.boot.base.model.GenericModel;
import com.simbest.boot.base.repository.GenericRepository;
import com.simbest.boot.base.service.IGenericService;
import com.simbest.boot.constants.ApplicationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.List;

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

    /**
     * @see
     */
    @Override
    public T getById ( PK id ) {
        log.debug("@Generic Repository Service get single object by id: " + id);
        return (T)genericRepository.findById( id ).orElse( null );
    }

    /**
     * @see
     */
    @Override
    public T getOne ( PK id ) {
        log.debug("@Generic Repository Service getOne object by id: " + id);
        return (T)genericRepository.getOne(id);
    }

    /**
     * @see
     */
    @Override
    public T save ( T o ) {
        log.debug("@Generic Repository Service save: " + o);
        return (T)genericRepository.save( o );
    }

    /**
     * @see
     */
    @Override
    public <S extends T> List<S> saveAll ( Iterable<? extends T> param ) {
        log.debug("@Generic Repository Service saveAll");
        return (List<S>)genericRepository.saveAll( param );
    }


    /**
     * @see
     */
    @Override
    public T saveAndFlush ( T o ) {
        log.debug("@Generic Repository Service saveAndFlush object:" + o);
        return (T)genericRepository.saveAndFlush( o );
    }


    /**
     * @see
     */
    @Override
    public Page<T>  findAll ( Specification<T> conditions, Pageable pageable ) {
        log.debug("@Generic Repository Service findAll Specification object PageSize:" + pageable.getPageSize() + ":PageNumber:" + pageable.getPageNumber());
        return genericRepository.findAll( conditions,pageable );
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
     * @return
     */
    @Override
    public List<T> getAll ( ) {
        log.debug("@Generic Repository Service getAll");
        return genericRepository.findAll();
    }

    /**
     * @see
     * @param sort  排序字段
     * @return
     */
    @Override
    public List<T> getAllBySort ( Sort sort ) {
        log.debug("@Generic Repository Service object by Sort");
        return genericRepository.findAll( sort );
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
    public void deleteById ( PK id ) {
        log.debug("@Generic Repository Service deleteById object by id: " + id);
        genericRepository.deleteById( id );
    }

    /**
     * @see
     */
    @Override
    public void delete ( T o ) {
        log.debug("@Generic Repository Service delete object: " + o);
        genericRepository.delete( o );
    }

    /**
     * @see
     */
    @Override
    public void deleteAll ( Iterable<? extends T> iterable ) {
        log.debug("@Generic Repository Service deleteAll Iterable param");
        genericRepository.deleteAll( iterable );
    }

    /**
     * @see
     */
    @Override
    public void deleteAll ( ) {
        log.debug("@Generic Repository Service deleteAll null param");
        genericRepository.deleteAll();
    }

    /**
     * @see
     */
    @Override
    public void deleteInBatch ( Iterable<T> entities ) {
        log.debug("@Generic Repository Service deleteInBatch Iterable param");
        genericRepository.deleteInBatch( entities );
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
}
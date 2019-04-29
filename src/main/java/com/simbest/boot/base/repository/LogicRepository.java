package com.simbest.boot.base.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <strong>Title : 业务实体通用数据持久层</strong><br>
 * <strong>Description : 业务实体通用数据持久层</strong><br>
 * <strong>Create on : 2018/5/18</strong><br>
 * <strong>Modify on : 2018/5/18</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
@Transactional
@NoRepositoryBean
public interface LogicRepository<T, PK extends Serializable> extends SystemRepository<T, PK> {

    long countActive();

    long countActive(Specification<T> conditions);

    boolean existsActive(PK id);

    Page<T> findAllActive();

    List<T> findAllActiveNoPage();

    Page<T> findAllActive(Sort sort);

    Page<T> findAllActive(Pageable pageable);

    List<T> findAllActive(Iterable<PK> ids);

    List<T> findAllActive(Specification<T> conditions);

    Page<T> findAllActive(Specification<T> conditions, Pageable pageable);

    T findByIdActive(PK id);

    T findOneActive(PK id);

    T findOneActive(Specification<T> conditions);

    @Modifying
    void logicDelete(PK id);

    @Modifying
    void logicDelete(T entity);

    @Modifying
    void logicDelete(Iterable<? extends T> entities);

    @Modifying
    void logicDeleteAll();

    @Modifying
    void deleteAllByIds(Iterable<? extends PK> ids);

    @Modifying
    void scheduleLogicDelete(PK id, LocalDateTime localDateTime);

    @Modifying
    void scheduleLogicDelete(T entity, LocalDateTime localDateTime);

}

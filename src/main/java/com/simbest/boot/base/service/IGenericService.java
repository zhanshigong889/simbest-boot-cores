package com.simbest.boot.base.service;

import com.simbest.boot.base.model.GenericModel;
import com.simbest.boot.base.repository.Condition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
/**
 * <strong>Title : 基础实体通用服务层</strong><br>
 * <strong>Description : 基础实体通用服务层</strong><br>
 * <strong>Create on : 2018-05-16</strong><br>
 * <strong>Modify on : 2018-05-16</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 *          <strong>修改历史:</strong><br>
 *          修改人 修改日期 修改描述<br>
 *          -------------------------------------------<br>
 */
public interface IGenericService <T extends GenericModel,PK extends Serializable> {

    Pageable getPageable();

    Pageable getPageable(int page, int size);

    Pageable getPageable(int page, int size, String direction, String properties);

    Specification<T> getSpecification(T entity);

    Specification<T> getSpecification(Map<String, Object> conditions);

    Specification<T> getSpecification(Condition condition);

    long count();

    long count(Specification<T> specification);

    /**
     * 根据id判断实体是否存在
     */
    boolean exists(PK id);

    /**
     * getOne取出的是实体的引用
     */
    T findOne(PK id);

    T findOne(Specification<T> conditions);

    T findById(PK id);

    /**
     * 查询全部记录
     */
    Page<T> findAll();

    /**
     * 分页查询（含排序功能）
     *
     * @param pageable
     * @return
     */
    Page<T> findAll(Pageable pageable);

    /**
     * 根据排序字段查询全部记录
     *
     * @param sort 排序字段
     * @return
     */
    Page<T> findAll(Sort sort);
    /**
     * 查询全部记录
     */
    Iterable<T> findAllNoPage();

    /**
     * 查询全部记录
     */
    Iterable<T> findAllNoPage(Sort sort);

    /**
     * 根据主键查询
     *
     * @param ids
     * @return
     */
    Iterable<T> findAllByIDs(Iterable<PK> ids);

    /**
     * 按条件分页查询（含排序功能）
     *
     * @param conditions
     * @param pageable
     * @return
     */
    Page<T> findAll(Specification<T> conditions, Pageable pageable);

    /**
     * 按条件查询全部记录
     * @param conditions
     * @return
     */
    Iterable<T> findAllNoPage(Specification<T> conditions);

    /**
     * 按条件查询全部记录，并排序
     * @param conditions
     * @param sort
     * @return
     */
    Iterable<T> findAllNoPage(Specification<T> conditions, Sort sort);

    /**
     * 新增-不允许实体主键字段有值
     * @param o
     * @return
     */
    T insert(T o);

    /**
     * 修改-不允许实体主键字段无值
     * @param o
     * @return
     */
    T update(T o);

    /**
     * 强制执行持久化
     *
     * @param o
     * @return
     */
    T saveAndFlush(T o);

    /**
     * 保存集合(save 待区分)
     *
     * @param iterable
     * @param <S>
     * @return
     */
    List<T> saveAll(Iterable<T> entities);

    /**
     * 根据主键删除数据
     *
     * @param id
     */
    void deleteById(PK id);

    /**
     * 根据传入的实体对象属性删除数据
     *
     * @param o
     */
    void delete(T o);

    /**
     * 根据传入的实体对象属性批量删除
     *
     * @param iterable
     */
    void deleteAll(Iterable<? extends T> iterable);

    /**
     * 批量删除（相当于清空数据）
     */
    void deleteAll();

    /**
     * 删除一个实体集合
     *
     * @param entities
     */
    void deleteAllByIds(Iterable<? extends PK> pks);

}

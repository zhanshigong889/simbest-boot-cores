/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.base.repository;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.ObjectUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.persistence.TypedQuery;
import javax.persistence.UniqueConstraint;
import javax.persistence.criteria.*;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * 用途：定义逻辑删除Repository
 * 作者: lishuyi
 * 时间: 2018/6/8  18:07
 */
public class LogicDeleteRepositoryImpl <T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements LogicRepository<T, ID> {

    private final JpaEntityInformation<T, ?> entityInformation;
    private final EntityManager em;
    private final Class<T> domainClass;
    private static final String DELETED_FIELD = "removedTime";
    private static final String MODIFIER_FIELD = "modifier";
    private static final String MODIFIEDTIME_FIELD = "modifiedTime";
    private static final String ENABLED_FIELD = "enabled";
    private static final Object NOT_EXIST_ID_VALUE = null;

    public LogicDeleteRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.em = em;
        this.domainClass = domainClass;
        this.entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, em);
    }

    @Override
    public long countActive() {
        return super.count(notDeleted());
    }

    @Override
    public long countActive(Specification<T> conditions){
        return super.count(conditions.and(notDeleted()));
    }

    @Override
    public boolean existsActive(ID id) {
        Assert.notNull(id, "实体ID不能为空");
        return findOneActive(id) != null ? true : false;
    }

    @Override
    public T findByIdActive(ID id) {
        return findOneActive(id);
    }

    @Override
    public T findOneActive(ID id) {
        return super.findOne(
                Specifications.where(new ByIdSpecification<T, ID>(entityInformation, id)).and(notDeleted())).orElse(null);
    }

    @Override
    public T findOneActive(Specification<T> conditions) {
        List<T> list = super.findAll(conditions.and(notDeleted()));
        return list.size()==0 ? null : list.get(0);
    }

    @Override
    public Page<T> findAllActive() {
        return super.findAll(notDeleted(), PageRequest.of(ApplicationConstants.DEFAULT_PAGE, ApplicationConstants.DEFAULT_SIZE));
    }

    @Override
    public List<T> findAllActiveNoPage() {
        return super.findAll(notDeleted());
    }

    @Override
    public Page<T> findAllActive(Sort sort) {
        return super.findAll(notDeleted(), PageRequest.of(ApplicationConstants.DEFAULT_PAGE, ApplicationConstants.DEFAULT_SIZE, sort));
    }

    @Override
    public Page<T> findAllActive(Pageable pageable) {
        return super.findAll(notDeleted(), pageable);
    }

    @Override
    public List<T> findAllActive(Iterable<ID> ids) {
        if (ids == null || !ids.iterator().hasNext())
            return Collections.emptyList();

        if (entityInformation.hasCompositeId()) {
            List<T> results = new ArrayList<T>();

            for (ID id : ids)
                results.add(findOneActive(id));

            return results;
        }

        ByIdsSpecification<T> specification = new ByIdsSpecification<T>(entityInformation);
        TypedQuery<T> query = getQuery(Specifications.where(specification).and(notDeleted()), (Sort) null);

        return query.setParameter(specification.parameter, ids).getResultList();
    }

    @Override
    public List<T> findAllActive(Specification<T> conditions) {
        return super.findAll(conditions.and(notDeleted()));
    }

    @Override
    public Page<T> findAllActive(Specification<T> conditions, Pageable pageable) {
        return super.findAll(conditions.and(notDeleted()), pageable);
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <S extends T> S save(S entity) {
        Set<ConstraintViolation<S>> constraintViolations = Validation.buildDefaultValidatorFactory().getValidator()
                .validate(entity);
        if (constraintViolations.size() > 0)
            throw new ConstraintViolationException(constraintViolations.toString(), constraintViolations);
        Class<?> entityClass = entity.getClass();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
        Root<?> root = criteriaQuery.from(entityClass);
        List<Predicate> predicates = new ArrayList<Predicate>();

        //判断是否有联合主键
        if (entityInformation.hasCompositeId()) {
            for (String s : entityInformation.getIdAttributeNames())
                predicates.add(criteriaBuilder.equal(root.<ID>get(s),
                        entityInformation.getCompositeIdAttributeValue(entityInformation.getId(entity), s)));
            //无论是逻辑删除，还是物理删除，联合主键都必须满足唯一性
//            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(DELETED_FIELD), LocalDateTime.now()));
            criteriaQuery.select(root).where(predicates.toArray(new Predicate[predicates.size()]));
            TypedQuery<Object> typedQuery = em.createQuery(criteriaQuery);
            List<Object> resultSet = typedQuery.getResultList();
            if (resultSet.size() > 0) {
                S result = (S) resultSet.get(0);
                BeanUtils.copyProperties(entity, result, getNullPropertyNames(entity));
                return (S) super.save(result);
            }
        }
        //判断是否有联合索引
        if (entity.getClass().isAnnotationPresent(Table.class)) {
            Annotation a = entity.getClass().getAnnotation(Table.class);
            try {
                UniqueConstraint[] uniqueConstraints = (UniqueConstraint[]) a.annotationType()
                        .getMethod("uniqueConstraints").invoke(a);
                if (uniqueConstraints != null) {
                    for (UniqueConstraint uniqueConstraint : uniqueConstraints) {
                        Map<String, Object> data = new HashMap<>();
                        for (String name : uniqueConstraint.columnNames()) {
                            name = CaseFormat.LOWER_UNDERSCORE.to( CaseFormat.LOWER_CAMEL, name );
                            PropertyDescriptor pd = new PropertyDescriptor(name, entityClass);
                            Object value = pd.getReadMethod().invoke(entity);
                            if (value == null) {
                                data.clear();
                                break;
                            }
                            data.put(name, value);
                        }
                        if (!data.isEmpty()) {
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                predicates.add(criteriaBuilder.equal(root.get(entry.getKey()), entry.getValue()));
                            }
                        }
                    }
                    if (predicates.isEmpty()) {
                        return super.save(entity);
                    }
                    //无论是逻辑删除，还是物理删除，联合索引都必须满足唯一性
//                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(DELETED_FIELD), LocalDateTime.now()));
                    criteriaQuery.select(root).where(predicates.toArray(new Predicate[predicates.size()]));
                    TypedQuery<Object> typedQuery = em.createQuery(criteriaQuery);
                    List<Object> resultSet = typedQuery.getResultList();
                    if (resultSet.size() > 0) {
                        S result = (S) resultSet.get(0);
                        BeanUtils.copyProperties(entity,
                                result, Stream
                                        .concat(Arrays.stream(getNullPropertyNames(entity)),
                                                Arrays.stream(
                                                        new String[] { entityInformation.getIdAttribute().getName() }))
                                        .toArray(String[]::new));
                        return (S) super.save(result);
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException | IntrospectionException e) {
                e.printStackTrace();
            }
        }
        //没有联合主键、没有联合索引，直接保存
        return super.save(entity);
    }


    @Override
    @Transactional
    public <S extends T> S saveAndFlush(S entity) {
        S result = this.save(entity);
        flush();
        return result;
    }

    @Override
    @Transactional
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<S>();
        if (entities == null) {
            return result;
        }
        for (S entity : entities) {
            result.add(super.save(entity));
        }
        return result;
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> propertyNames = new HashSet<String>();
        for (PropertyDescriptor pd : pds)
            if (!pd.getName().equals(DELETED_FIELD) && src.getPropertyValue(pd.getName()) == null)
                propertyNames.add(pd.getName());

        return propertyNames.toArray(new String[propertyNames.size()]);
    }

    @Override
    @Transactional
    public void logicDelete(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        logicDelete(id, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void logicDelete(T entity) {
        Assert.notNull(entity, "The entity must not be null!");
        logicDelete(entity, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void logicDelete(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        for (T entity : entities) {
            logicDelete(entity);
        }
    }

    @Override
    @Transactional
    public void logicDeleteAll() {
        for (T entity : findAllActive()) {
            logicDelete(entity);
        }
    }

    @Override
    public void deleteAllByIds(Iterable<? extends ID> ids) {
        for(ID id: ids) {
            logicDelete(id);
        }
    }


    @Override
    @Transactional
    public void scheduleLogicDelete(ID id, LocalDateTime localDateTime) {
        logicDelete(id, localDateTime);
    }

    @Override
    @Transactional
    public void scheduleLogicDelete(T entity, LocalDateTime localDateTime) {
        logicDelete(entity, localDateTime);
    }

    private void logicDelete(ID id, LocalDateTime localDateTime) {
        Assert.notNull(id, "The given id must not be null!");

        T entity = findOneActive(id);

        if (entity == null)
            throw new EmptyResultDataAccessException(
                    String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id), 1);

        logicDelete(entity, localDateTime);
    }

    private void logicDelete(T entity, LocalDateTime localDateTime) {
        Assert.notNull(entity, "The entity must not be null!");

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaUpdate<T> update = cb.createCriteriaUpdate((Class<T>) domainClass);

        Root<T> root = update.from((Class<T>) domainClass);

        update.set(DELETED_FIELD, localDateTime); //设置删除时间
        Map<String, Object> params = ObjectUtil.getEntityPersistentFieldValueExceptId(entity);
        update.set(MODIFIER_FIELD, params.get(MODIFIER_FIELD)); //设置删除人
        update.set(MODIFIEDTIME_FIELD, localDateTime);
        update.set(ENABLED_FIELD, false); //删除数据后，设置为不可用

        final List<Predicate> predicates = new ArrayList<Predicate>();

        if (entityInformation.hasCompositeId()) {
            for (String s : entityInformation.getIdAttributeNames())
                predicates.add(cb.equal(root.<ID>get(s),
                        entityInformation.getCompositeIdAttributeValue(entityInformation.getId(entity), s)));
            update.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        } else if(null != entityInformation.getId(entity)) {
            update.where(cb.equal(root.<ID>get(entityInformation.getIdAttribute().getName()),
                    entityInformation.getId(entity)));
        } else {
            List<Predicate> predicateList = Lists.newArrayList();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                //删除人和可用性字段不作为参数
                if(!MODIFIER_FIELD.equals(entry.getKey()) && !ENABLED_FIELD.equals(entry.getKey())) {
                    predicateList.add(cb.equal(root.get(entry.getKey()), entry.getValue()));
                }
            }
            if(!predicateList.isEmpty()){
                update.where(predicateList.toArray(new Predicate[]{}));
            } else{
                //无条件不允许删除
                update.where(cb.equal(root.<ID>get(entityInformation.getIdAttribute().getName()), NOT_EXIST_ID_VALUE));
            }
        }

        em.createQuery(update).executeUpdate();
    }

    private static final class ByIdSpecification<T, ID extends Serializable> implements Specification<T> {

        private final JpaEntityInformation<T, ?> entityInformation;
        private final ID id;

        public ByIdSpecification(JpaEntityInformation<T, ?> entityInformation, ID id) {
            this.entityInformation = entityInformation;
            this.id = id;
        }

        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final List<Predicate> predicates = new ArrayList<Predicate>();
            if (entityInformation.hasCompositeId()) {
                for (String s : entityInformation.getIdAttributeNames())
                    predicates.add(cb.equal(root.<ID>get(s), entityInformation.getCompositeIdAttributeValue(id, s)));

                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
            return cb.equal(root.<ID>get(entityInformation.getIdAttribute().getName()), id);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final class ByIdsSpecification<T> implements Specification<T> {

        private final JpaEntityInformation<T, ?> entityInformation;

        ParameterExpression<Iterable> parameter;

        public ByIdsSpecification(JpaEntityInformation<T, ?> entityInformation) {
            this.entityInformation = entityInformation;
        }

        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            Path<?> path = root.get(entityInformation.getIdAttribute());
            parameter = cb.parameter(Iterable.class);
            return path.in(parameter);
        }
    }

    private static final class DeletedIsNull<T> implements Specification<T> {
        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.isNull(root.<LocalDateTime>get(DELETED_FIELD));
        }
    }

    private static final class DeletedTimeGreatherThanNow<T> implements Specification<T> {
        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.greaterThan(root.<LocalDateTime>get(DELETED_FIELD), LocalDateTime.now());
        }
    }

    private static final <T> Specification<T> notDeleted() {
        return Specifications.where(new DeletedIsNull<T>()).or(new DeletedTimeGreatherThanNow<T>());
    }

    public static void main ( String[] args ) {
        System.out.println(  CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,"permission_code") );
    }
}

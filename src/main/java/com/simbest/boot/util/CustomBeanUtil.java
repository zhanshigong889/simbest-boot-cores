/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <strong>Description : 基于Spring BeanUtils的自定义工具类</strong><br>
 * <strong>Create on : 2017年08月23日</strong><br>
 * <strong>Modify on : 2017年11月08日</strong><br>
 * <strong>Copyright Beijing Simbest Technology Ltd.</strong><br>
 *
 * @author lishuyi
 */
@Slf4j
public class CustomBeanUtil extends BeanUtils {

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            try {
                //下面的判断为避免业务实体类重写属性get方法后，src.getPropertyValue(pd.getName())获取不到值报错
                if( StrUtil.isEmptyIfStr( pd.getReadMethod() ) ){
                    continue;
                }
                Object srcValue = src.getPropertyValue(pd.getName());
                if (srcValue == null) {
                    emptyNames.add(pd.getName());
                }
            }catch (InvalidPropertyException e){
                emptyNames.add(pd.getName());
                log.error("获取属性【{}】发生异常【{}】", pd.getName(), e.getMessage());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 拷贝source非空的字段至target
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 拷贝source非空的字段至target，并排除target中的持久化ID
     */
    public static void copyPropertiesIgnoreNullAndId(Object source, Object target) {
        Field id = ObjectUtil.getEntityIdField(target);
        String[] ignoreProperties = getNullPropertyNames(source);
        List<String> ignorePropertyList = Arrays.asList(ignoreProperties);
        List<String> ignorePropertyListMerge = Lists.newArrayList();
        ignorePropertyListMerge.addAll(ignorePropertyList);
        ignorePropertyListMerge.add(id.getName());
        BeanUtils.copyProperties(source, target, ignorePropertyListMerge.toArray(new String[]{}));
    }

    /**
     * 拷贝source非持久化字段至target
     */
    public static void copyTransientProperties(Object source, Object target) {
        BeanWrapper srcWrap = PropertyAccessorFactory.forBeanPropertyAccess(source);
        BeanWrapper trgWrap = PropertyAccessorFactory.forBeanPropertyAccess(target);
        Set<Field> fields = ObjectUtil.getEntityTransientField(source);
        fields.forEach(f -> trgWrap.setPropertyValue(f.getName(), srcWrap.getPropertyValue(f.getName())));
    }
}

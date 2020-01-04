/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：反射工具类
 * 作者: lishuyi
 * 时间: 2018/5/12  18:36
 */
@Slf4j
public class ReflectionUtils {
    /**
     * 获取所有的属性（遍历所有父类）
     *
     * @param clazz
     * @return Field[]
     */
    public static Field[] getAllDeclaredFields(Class<?> clazz) {
        return getAllDeclaredFields(clazz, null);
    }

    /**
     * 获取所有的属性（遍历所有父类）
     *
     * @param clazz
     * @return Map<String, Field>
     */
    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        return getFieldMap(clazz, null);
    }

    /**
     * 获取指定的属性（包括父类），没有则返回空
     *
     * @param clazz
     * @return Field
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        return getField(clazz, null, fieldName);
    }

    /**
     * 获取所有的属性（遍历所有父类,直到topClazz为止,不包括topClazz中的属性），如果子类、父类中有相同的属性名，则以子类为准
     *
     * @param clazz
     * @return Field[]
     */
    public static Field[] getAllDeclaredFields(Class<?> clazz, Class<?> topClazz) {
        Map<String, Field> fieldMap = getFieldMap(clazz, topClazz);
        List<Field> fieldList = new ArrayList<>(fieldMap.size());

        fieldMap.forEach((fieldName, field) -> {
            fieldList.add(field);
        });

        return fieldList.toArray(new Field[fieldList.size()]);
    }

    /**
     * 获取所有的属性（遍历所有父类,直到topClazz为止,不包括topClazz中的属性），如果子类、父类中有相同的属性名，则以子类为准
     *
     * @param clazz
     * @return Map<String, Field>
     */
    public static Map<String, Field> getFieldMap(Class<?> clazz, Class<?> topClazz) {
        if (topClazz == null) {
            topClazz = Object.class;
        } else if (!topClazz.isAssignableFrom(clazz)) {// 如果clazz不是topClazz的子类
            topClazz = Object.class;
        }
        Class<?> superClazz = clazz;
        List<Class<?>> supperClazzList = new ArrayList<>();
        while (superClazz != null && !topClazz.getName().equals(superClazz.getName())) {
            supperClazzList.add(superClazz);
            superClazz = superClazz.getSuperclass();
        }

        Map<String, Field> fieldMap = new HashMap<>();
        // 如果子类中有与父类同名的属性，则直接覆盖父类的，以子类的为准，所以逆序遍历
        for (int i = supperClazzList.size() - 1; i >= 0; i--) {
            Field[] fields = supperClazzList.get(i).getDeclaredFields();// 获得该类中所有的Field，不包括父类中的
            if (fields == null || fields.length == 0) {
                continue;
            }
            for (Field f : fields) {
                fieldMap.put(f.getName(), f);
            }
        }
        return fieldMap;
    }

    /**
     * 获取指定的属性（包括父类），没有则返回空
     *
     * @param clazz
     * @return Field
     */
    public static Field getField(Class<?> clazz, Class<?> topClazz, String fieldName) {
        Map<String, Field> fieldMap = getFieldMap(clazz, topClazz);
        return fieldMap.get(fieldName);
    }

    /**
     * 获取属性的get方法
     *
     * @param targetClazz
     * @param fieldName 属性名
     * @return Method
     */
    public static Method findGetMethod(Class<?> targetClazz, String fieldName) {
        String getMethodName = "get" + StringUtils.capitalize(fieldName);
        return findMethod(targetClazz, getMethodName);
    }

    /**
     * 获取属性的set方法
     *
     * @param targetClazz
     * @param fieldName 属性名
     * @param paramType set方法的参数类型
     * @return Method
     */
    public static Method findSetMethod(Class<?> targetClazz, String fieldName, Class<?> paramType) {
        String setMethodName = "set" + StringUtils.capitalize(fieldName);
        return findMethod(targetClazz, setMethodName, paramType);

    }

    /**
     * 获取Method
     *
     * @param targetClazz
     * @param methodName 函数名称
     * @param paramTypes 参数类型
     * @return Method
     */
    public static Method findMethod(Class<?> targetClazz, String methodName, Class<?>... paramTypes) {
        try {
            return targetClazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 属性的get方法调用
     *
     * @param targetObj 对象示例
     * @param fieldName 属性名
     * @return Object
     */
    public static Object invokeGetMethod(Object targetObj, String fieldName) {
        Method getMethod = findGetMethod(targetObj.getClass(), fieldName);
        return invokeMethod(targetObj, getMethod);
    }

    /**
     * 属性的set方法调用
     *
     * @param targetObj
     * @param fieldName
     * @param paramValue
     */
    public static void invokeSetMethod(Object targetObj, String fieldName, Object paramValue) {
        Method setMethod = findSetMethod(targetObj.getClass(), fieldName, paramValue.getClass());
        invokeMethod(targetObj, setMethod, paramValue);
    }

    /**
     * 调用指定的方法
     *
     * @param targetObj
     * @param method
     * @param paramValues
     * @return Object
     */
    public static Object invokeMethod(Object targetObj, Method method, Object... paramValues) {
        try {
            if (method != null) {
                return method.invoke(targetObj, paramValues);
            }
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("", e);
            return null;
        }
    }


    /**
     * 调用指定的方法
     *
     * @param targetObj
     * @param methodName 该方法如果有参数，则不能定义成基本类型，如int应该定义成Integer，否则会报错
     * @param paramValues
     * @return Object
     */
    public static Object invokeMethod(Object targetObj, String methodName, Object... paramValues) {
        try {
            Class<?>[] paramTypes = null;
            if (paramValues != null && paramValues.length != 0) {
                paramTypes = new Class<?>[paramValues.length];
                for (int i = 0; i < paramValues.length; i++) {
                    paramTypes[i] = paramValues[i].getClass();
                }
            }
            Method method = findMethod(targetObj.getClass(), methodName, paramTypes);
            if (method != null) {
                return method.invoke(targetObj, paramValues);
            }
            return null;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error("", e);
            return null;
        }
    }

    public static Object getValue(Field field, Object model) {
        try {
            String name = field.getName();
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            String type = field.getGenericType().toString();
            if (type.equals("class java.lang.String")) {
                Method m = model.getClass().getMethod("get" + name);
                return (String) m.invoke(model);
            }
            if (type.equals("class java.lang.Integer")) {
                Method m = model.getClass().getMethod("get" + name);
                return (Integer) m.invoke(model);
            }
            if (type.equals("class java.lang.Short")) {
                Method m = model.getClass().getMethod("get" + name);
                return (Short) m.invoke(model);
            }
            if (type.equals("class java.lang.Long")) {
                Method m = model.getClass().getMethod("get" + name);
                return (Long) m.invoke(model);
            }
            if (type.equals("class java.lang.Double")) {
                Method m = model.getClass().getMethod("get" + name);
                return (Double) m.invoke(model);
            }
            if (type.equals("class java.lang.Boolean")) {
                Method m = model.getClass().getMethod("get" + name);
                return (Boolean) m.invoke(model);
            }
            if (type.equals("class java.util.Date")) {
                Method m = model.getClass().getMethod("get" + name);
                return (Date) m.invoke(model);
            }
            if (type.equals("class java.math.BigDecimal")) {
                Method m = model.getClass().getMethod("get" + name);
                return (BigDecimal) m.invoke(model);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}

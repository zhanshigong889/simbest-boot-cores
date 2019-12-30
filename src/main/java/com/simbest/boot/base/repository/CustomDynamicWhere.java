package com.simbest.boot.base.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <strong>Title : CustomDynamicWhere</strong><br>
 * <strong>Description : 自定义动态条件</strong><br>
 * <strong>Create on : $date$</strong><br>
 * <strong>Modify on : $date$</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
@Slf4j
@Repository
public class CustomDynamicWhere implements Serializable {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("namedParameterJdbcTemplate")
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 根据自定义进行原生的sql查询，无参数
     * @param sql                       执行的sql语句
     * @return List
     */
    public List<Map<String, Object>> queryForList(String sql){
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * 根据自定义的动态参数进行原生的sql查询
     * @param sql                       执行的sql语句
     * @param params                    注入的参数  为占位符 ？
     * @return List
     */
    public List<Map<String, Object>> queryForList(String sql,Object[] params){
        return jdbcTemplate.queryForList(sql,params);
    }

    /**
     * 根据自定义的动态参数进行原生的sql查询
     * @param sql                       执行的sql语句
     * @param paramMap                    注入的参数  为命名参数 :value
     * @return List
     */
    public List<Map<String, Object>> queryNamedParameterForList(String sql,Map<String, ?> paramMap){
        return namedParameterJdbcTemplate.queryForList(sql, paramMap);
    }
}

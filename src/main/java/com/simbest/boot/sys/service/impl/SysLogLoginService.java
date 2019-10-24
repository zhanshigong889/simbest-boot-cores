package com.simbest.boot.sys.service.impl;

import com.simbest.boot.base.repository.CustomDynamicWhere;
import com.simbest.boot.base.service.impl.GenericService;
import com.simbest.boot.sys.model.SysLogLogin;
import com.simbest.boot.sys.repository.SysLogLoginRepository;
import com.simbest.boot.sys.service.ISysLogLoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用途：登录日志管理逻辑层
 * 作者: lishuyi
 * 时间: 2018/2/23  10:14
 */
@Slf4j
@Service
public class SysLogLoginService extends GenericService<SysLogLogin, String> implements ISysLogLoginService {

    @Autowired
    private SysLogLoginRepository repository;

    @Autowired
    private CustomDynamicWhere dynamicRepository;

    @Autowired
    public SysLogLoginService(SysLogLoginRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public List<Map<String, Object>> countLogin(Map<String, ?> paramMap) {
        String selectSQL = "SELECT account, login_entry AS loginEntry, is_success AS isSuccess, count(*) AS count FROM SYS_LOG_LOGIN ";
//        String whereSQL = "WHERE 1=1 "; //sql injection 漏洞
        String whereSQL = " WHERE";
        String groupBySQL = "GROUP BY account, login_entry, is_success ";

        for(String property : paramMap.keySet()){
            if(property.equals("account")){
                whereSQL += "AND account = :account ";
            }
            if(property.equals("loginEntry")){
                whereSQL += "AND login_entry = :loginEntry ";
            }
            if(property.equals("trueName")){
                whereSQL += "AND true_name = :trueName ";
            }
            if(property.equals("belongOrgName")){
                whereSQL += "AND belong_org_name = :belongOrgName ";
            }
            if(property.equals("ssDate")){
                whereSQL += "AND login_time >= :ssDate ";
            }
            if(property.equals("eeDate")){
                whereSQL += "AND login_time <= :eeDate ";
            }
        }

        whereSQL = StringUtils.replace(whereSQL, "WHEREAND", "WHERE ");
        String querySQL = selectSQL+whereSQL+groupBySQL;
        log.debug("Query sql is : {} and parameter is {}", querySQL, paramMap);
        return dynamicRepository.queryNamedParameterForList(querySQL, paramMap);
    }
}

/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.base.repository;

import com.simbest.boot.base.model.Oauth2ClientDetails;
import org.springframework.stereotype.Repository;

/**
 * 用途：Oauth2 客户端信息持久层
 * 作者: lishuyi
 * 时间: 2018/6/10  13:42
 */
@Repository
public interface Oauth2ClientDetailsRepository extends GenericRepository<Oauth2ClientDetails, String> {

    Oauth2ClientDetails findByClientId(String clientId);
}


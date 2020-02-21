package com.simbest.boot.base.service.impl;

import com.simbest.boot.base.model.Oauth2ClientDetails;
import com.simbest.boot.base.repository.Oauth2ClientDetailsRepository;
import com.simbest.boot.base.service.IOauth2ClientDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import static com.simbest.boot.constants.AuthoritiesConstants.OAUTH2_UNKNOW_CLIENT;

/**
 * 用途：Oauth2 客户端信息逻辑层
 * 作者: lishuyi
 * 时间: 2018/2/23  10:14
 */
@Slf4j
@Service
public class Oauth2ClientDetailsService extends GenericService<Oauth2ClientDetails, String> implements IOauth2ClientDetailsService {

    @Autowired
    private Oauth2ClientDetailsRepository repository;

    @Autowired
    public Oauth2ClientDetailsService(Oauth2ClientDetailsRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        ClientDetails clientDetails = repository.findByClientId(clientId);
        if(null == clientDetails){
            log.error("无法获取到OAuth2的信息，目前接收的clientId为【{}】，请检查数据库oauth2client_details配置和请求参数", clientId);
            throw new BadCredentialsException(OAUTH2_UNKNOW_CLIENT+clientId);
        }
        return clientDetails;
    }

}

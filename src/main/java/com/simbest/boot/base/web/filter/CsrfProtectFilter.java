/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.base.web.filter;

import com.google.common.collect.Sets;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * 用途：验证HTTP Referer字段防御CSRF攻击
 * 作者: lishuyi
 * 时间: 2018/10/9  11:34
 */
@Slf4j
@WebFilter(urlPatterns = "/*", filterName = "csrfProtectFilter")
public class CsrfProtectFilter implements Filter {
    private Set<String> whiteHostList = Sets.newHashSet();

    @Autowired
    private AppConfig appConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String whiteHosts = appConfig.getWhiteHostList();
        if(StringUtils.isNotEmpty(whiteHosts)){
            log.info("Csrf 防盗链主机源端白名单为：");
            log.info(whiteHosts);
            String[] whiteHostss = whiteHosts.split(ApplicationConstants.COMMA);
            for(String s:whiteHostss){
                whiteHostList.add(s.toLowerCase());
            }
        }
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        log.debug("访问目录URL地址为【{}】", request.getRequestURI());
        // 链接来源地址，通过获取请求头 referer 得到
        String referer = request.getHeader("referer");
        try {
            if (referer != null) {
                URI referUri = new URI(referer);
                String host = referUri.getHost().toLowerCase();
                if (whiteHostList.size()>0 && !whiteHostList.contains(host)) {
                    log.error("Csrf 防盗链主机源端白名单为【{}】, 当前主机【{}】不在白名单内，禁止访问!", whiteHostList, host);
                    PrintWriter writer = servletResponse.getWriter();
                    JsonResponse jsonResponse = JsonResponse.builder().errcode(JsonResponse.ERROR_CODE).timestamp(DateUtil.getCurrent())
                            .status(JsonResponse.ERROR_STATUS).path(request.getServletPath())
                            .message("Forbidden: invalid host access: "+host).build();
                    String jsonResponseStr = JacksonUtils.obj2json(jsonResponse);
                    writer.write(jsonResponseStr);
                    writer.flush();
                    writer.close();
                } else {
                    filterChain.doFilter(request, response);
                }
            }else {
                filterChain.doFilter(request, response);
            }
        } catch (URISyntaxException e) {
            Exceptions.printException(e);
        }

    }

    @Override
    public void destroy() {

    }
}

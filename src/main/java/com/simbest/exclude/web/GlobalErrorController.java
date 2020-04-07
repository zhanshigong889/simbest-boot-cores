/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.exclude.web;

import com.simbest.boot.base.web.response.JsonResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 用途：统一异常处理 参考：https://blog.csdn.net/king_is_everyone/article/details/53080851
 * 作者: lishuyi
 * 时间: 2018/5/15  22:03
 */
@Api(description = "GlobalErrorController", tags = {"系统管理-全局异常日志管理"})
@Slf4j
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class GlobalErrorController extends AbstractErrorController {
    private final ErrorProperties errorProperties;
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicErrorController.class);
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Create a new {@link org.springframework.boot.autoconfigure.web.BasicErrorController} instance.
     *
     * @param errorAttributes the error attributes
     * @param errorProperties configuration properties
     */
    public GlobalErrorController(ErrorAttributes errorAttributes,
                                ErrorProperties errorProperties) {
        this(errorAttributes, errorProperties,
                Collections.<ErrorViewResolver>emptyList());
    }

    /**
     * Create a new {@link org.springframework.boot.autoconfigure.web.BasicErrorController} instance.
     *
     * @param errorAttributes    the error attributes
     * @param errorProperties    configuration properties
     * @param errorViewResolvers error view resolvers
     */
    public GlobalErrorController(ErrorAttributes errorAttributes,
                                ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
        Assert.notNull(errorProperties, "ErrorProperties must not be null");
        this.errorProperties = errorProperties;
    }

    @Override
    public String getErrorPath() {
        return this.errorProperties.getPath();
    }

    /**
     * 处理错误页面
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(produces = "text/html")
    public ModelAndView errorHtml(HttpServletRequest request,
                                  HttpServletResponse response) {
        logErrorInformation(request);
        HttpStatus status = getStatus(request);
        Map<String, Object> model = getErrorAttributes(
                request, isIncludeStackTrace(request, MediaType.TEXT_HTML));
        model.put("errcode", JsonResponse.ERROR_CODE);
        response.setStatus(status.value());
        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
        return modelAndView == null ? new ModelAndView("error", model) : modelAndView;
    }

    /**
     * 处理Restful请求Json数据
     * @param request
     * @return
     */
    @RequestMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        logErrorInformation(request);
        Map<String, Object> body = getErrorAttributes(request,
                isIncludeStackTrace(request, MediaType.ALL));
        body.put("errcode", JsonResponse.ERROR_CODE);
        HttpStatus status = getStatus(request);
        return new ResponseEntity(body, status);
    }

    /**
     * Determine if the stacktrace attribute should be included.
     *
     * @param request  the source request
     * @param produces the media type produced (or {@code MediaType.ALL})
     * @return if the stacktrace attribute should be included
     */
    protected boolean isIncludeStackTrace(HttpServletRequest request,
                                          MediaType produces) {
        ErrorProperties.IncludeStacktrace include = getErrorProperties().getIncludeStacktrace();
        if (include == ErrorProperties.IncludeStacktrace.ALWAYS) {
            return true;
        }
        if (include == ErrorProperties.IncludeStacktrace.ON_TRACE_PARAM) {
            return getTraceParameter(request);
        }
        return false;
    }

    /**
     * Provide access to the error properties.
     *
     * @return the error properties
     */
    protected ErrorProperties getErrorProperties() {
        return this.errorProperties;
    }

    public void logErrorInformation(HttpServletRequest request){
        HttpStatus status = getStatus(request);
        log.error("Access Error Attention, httpstatus name {} code {}, AEA请注意,请求响应发生异常!!!", status.name(), status.value());
        Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
        if(NOT_FOUND.equals(status)){
            log.warn("请注意所访问URL【{}】发生404错误，资源地址不存在！", body.get("path"));
        }
        else {
            for (Map.Entry<String, Object> entry : body.entrySet()) {
                log.error("Error body key 【{}】 value 【{}】", entry.getKey(), entry.getValue());
            }
            if (null != request.getCookies() && request.getCookies().length > 0) {
                for (int i = 0; i < request.getCookies().length; i++) {
                    log.error("Cookie 名称【{}】 值【{}】", i, request.getCookies()[i]);
                }
            }
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                log.error("Header 【{}】 值【{}】", header, request.getHeader(header));
            }
            Enumeration<String> parameterNames = request.getHeaderNames();
            while (parameterNames.hasMoreElements()) {
                String parameter = parameterNames.nextElement();
                log.error("Parameter 【{}】 值 【{}】", parameter, request.getParameter(parameter));
            }
        }
    }
}

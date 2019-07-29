/**
 * 版权所有 © 北京晟壁科技有限公司 2017-2027。保留一切权利!
 */
package com.simbest.boot.base.exception;


import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 用途：全局异常处理，只处理应用内runtime的JSON请求错误，其他错误由com.simbest.exclude.GlobalErrorController处理，包括未定义的页面和JSON请求
 * 作者: lishuyi
 * 时间: 2017/11/4  15:34
 */
@Slf4j
@RestControllerAdvice
public class RestControllerExceptionHandler {

    /**
     * @param req Http请求
     * @param e   JsonException
     * @return JsonResponse
     */
    @ExceptionHandler(value = Exception.class)
    public JsonResponse handleErrorException(HttpServletRequest req, Exception e) {
        return handleException(req, e);
    }

    /**
     * @param req Http请求
     * @param e   AccessDeniedException
     * @return JsonResponse
     */
    @ExceptionHandler(value = RuntimeException.class)
    public JsonResponse handleRuntimeException(HttpServletRequest req, RuntimeException e) {
        return handleException(req, e);
    }

    /**
     * 通用异常处理入口
     *
     * @param req Http请求
     * @param e   Exception
     * @return JsonResponse
     */
    private JsonResponse handleException(HttpServletRequest req, Exception e) {
        log.warn("执行请求【{}】发生异常", req.getRequestURL().toString());
        Exceptions.printException(e);
        JsonResponse response = GlobalExceptionRegister.returnErrorResponse(e);
        response.setPath(req.getRequestURL().toString());
        return response;
    }

}

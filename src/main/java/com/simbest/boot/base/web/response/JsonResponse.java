/**
 * 版权所有 © 北京晟壁科技有限公司 2017-2027。保留一切权利!
 */
package com.simbest.boot.base.web.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.util.DateUtil;
import com.simbest.boot.util.json.JacksonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 用途：Restful 接口通用返回的JSON对象
 * 作者: lishuyi 
 * 时间: 2017/11/4  15:43 
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Builder
public class JsonResponse {
    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = -1;
    public static final int SUCCESS_STATUS = 200;
    public static final int ERROR_STATUS = 500;

    //状态码，请求正常返回为0
    @NonNull
    private Integer errcode;

    //时间戳
    @CreationTimestamp// 创建时自动更新时间
    @JsonFormat(pattern = DateUtil.timestampPattern1, timezone = "GMT+8")
    private Date timestamp;

    //Http请求状态码
    private int status;

    //Http请求内部错误
    private String error;

    //Http请求提示信息
    private String message;

    //Http请求路径
    private String path;

    //业务数据
    private Object data;

    /**
     *
     * @return 默认成功输出
     */
    public static JsonResponse defaultSuccessResponse() {
        return JsonResponse.builder().errcode(SUCCESS_CODE).timestamp(DateUtil.getCurrent())
                .status(SUCCESS_STATUS).build();
    }

    /**
     *
     * @return 默认失败输出
     */
    public static JsonResponse defaultErrorResponse() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return JsonResponse.builder().errcode(ERROR_CODE).message(ErrorCodeConstants.UNKNOW_ERROR)
                .timestamp(DateUtil.getCurrent()).status(ERROR_STATUS).path(request.getServletPath()).build();
    }

    /**
     *
     * @param obj 成功数据
     * @return 输出成功数据
     */
    public static JsonResponse success(Object obj) {
        JsonResponse response = defaultSuccessResponse();
        response.setData(obj);
        return response;
    }

    /**
     *
     * @param obj 失败数据
     * @return 输出失败数据
     */
    public static JsonResponse fail(Object obj) {
        JsonResponse response = defaultErrorResponse();
        response.setData(obj);
        return response;
    }

    /**
     *
     * @param obj 成功数据
     * @param message 提示信息
     * @return 输出成功数据
     */
    public static JsonResponse success(Object obj, String message) {
        JsonResponse response = success(obj);
        response.setMessage(message);
        return response;
    }

    /**
     *
     * @param obj 失败数据
     * @param message 提示信息
     * @return 输出失败数据
     */
    public static JsonResponse fail(Object obj, String message) {
        JsonResponse response = fail(obj);
        response.setMessage(message);
        return response;
    }

    /**
     *
     * @param obj 失败数据
     * @param message 提示信息
     * @param errcode 错误代码  详见 ErrorCodeConstants
     * @return
     */
    public static JsonResponse fail(Object obj, String message, Integer errcode) {
        JsonResponse response = fail(obj);
        response.setMessage(message);
        return response;
    }

    public static JsonResponse unauthorized() {
        JsonResponse response =  JsonResponse.builder().
                errcode(HttpStatus.UNAUTHORIZED.value())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.name())
                .timestamp(new Date())
                .build();
        log.warn("无权限访问，即将返回【{}】", JacksonUtils.obj2json(response));
        return response;
    }

    public static JsonResponse unauthorized(HttpServletRequest request, Exception exception) {
        JsonResponse response = JsonResponse.builder().
                errcode(HttpStatus.UNAUTHORIZED.value())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.name())
                .timestamp(new Date())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build();
        log.warn("无权限访问，即将返回【{}】", JacksonUtils.obj2json(response));
        return response;
    }

    public static JsonResponse authorized() {
        return JsonResponse.builder().
                errcode(SUCCESS_CODE)
                .status(HttpStatus.OK.value())
                .error(HttpStatus.OK.name())
                .build();
    }
}

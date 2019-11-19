/**
 * 版权所有 © 北京晟壁科技有限公司 2017-2027。保留一切权利!
 */
package com.simbest.boot.base.exception;


import com.google.common.collect.Maps;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.constants.ErrorCodeConstants;
import com.simbest.boot.exceptions.InsertExistObjectException;
import com.simbest.boot.exceptions.UpdateNotExistObjectException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.TransactionException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import javax.persistence.NonUniqueResultException;
import java.util.Map;

/**
 * 用途：系统中注册对应返回信息
 * 作者: lishuyi
 * 时间: 2017/11/4  15:46
 */
@Slf4j
public final class GlobalExceptionRegister {
    private static Map<Class<? extends Exception>, JsonResponse> errorMap = Maps.newHashMap();

    public static void main(String[] args) {
        HttpStatus a = HttpStatus.BAD_REQUEST;
        System.out.println(a.name());
        System.out.println(a.value());
    }

    //初始化状态码与文字说明
    static {
        errorMap.put(Exception.class,
                JsonResponse.builder().errcode(HttpStatus.BAD_REQUEST.value()).status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.name()).message("请求参数错误").build());
        errorMap.put(RuntimeException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("系统内部错误")
                        .build());
        errorMap.put(NullPointerException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("系统空指针异常")
                        .build());
        errorMap.put(AccessDeniedException.class,
                JsonResponse.builder().errcode(HttpStatus.FORBIDDEN.value()).status(HttpStatus.FORBIDDEN.value()).error(HttpStatus.FORBIDDEN.name()).message("权限禁止访问").build());
        errorMap.put(HttpRequestMethodNotSupportedException.class,
                JsonResponse.builder().errcode(HttpStatus.METHOD_NOT_ALLOWED.value()).status(HttpStatus.METHOD_NOT_ALLOWED.value()).error(HttpStatus.METHOD_NOT_ALLOWED.name())
                        .build());

        errorMap.put(MultipartException.class,
                JsonResponse.builder().errcode(ErrorCodeConstants.ERRORCODE_ATTACHMENT_SIZE_EXCEEDS).status(HttpStatus.BAD_REQUEST.value()).error(HttpStatus.BAD_REQUEST.name()).message("文件上传失败")
                        .build());
        errorMap.put(MaxUploadSizeExceededException.class,
                JsonResponse.builder().errcode(ErrorCodeConstants.ERRORCODE_ATTACHMENT_SIZE_EXCEEDS).status(HttpStatus.REQUEST_ENTITY_TOO_LARGE.value()).error(HttpStatus.REQUEST_ENTITY_TOO_LARGE.name()).message("文件大小受限")
                        .build());

        errorMap.put(InsertExistObjectException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("不能写入有ID的对象")
                        .build());
        errorMap.put(UpdateNotExistObjectException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("不能更新不存在的对象")
                        .build());

        errorMap.put(IllegalArgumentException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .build());

        errorMap.put(DataRetrievalFailureException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("提取数据发出错误")
                        .build());
        errorMap.put(IncorrectResultSizeDataAccessException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("数据记录于预期不符")
                        .build());
        errorMap.put(NonUniqueResultException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("数据记录于预期不符")
                        .build());
        errorMap.put(HibernateException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("持久化数据异常")
                        .build());
        errorMap.put(TransactionException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("数据库事务异常")
                        .build());
        errorMap.put(DataAccessException.class,
                JsonResponse.builder().errcode(HttpStatus.INTERNAL_SERVER_ERROR.value()).status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error(HttpStatus.INTERNAL_SERVER_ERROR.name()).message("数据库处理异常")
                        .build());

    }

    private GlobalExceptionRegister() {
    }

    /**
     *
     * @param e Exception
     * @return 返回JsonResponse
     */
    public static JsonResponse returnErrorResponse(Exception e) {
        Exceptions.printException(e);
        JsonResponse response = errorMap.get(e.getClass());
        if (response == null) {
            log.debug("当前异常为【{}】，父类异常为【{}】，异常信息为【{}】", e.getClass().getSimpleName(), e.getClass().getSuperclass().getSimpleName(), e.getMessage());
            Class superClass = e.getClass().getSuperclass();
            while(null != superClass){
                if(errorMap.keySet().contains(superClass)){
                    response = errorMap.get(superClass);
                    superClass = null;
                } else {
                    log.debug("当前父类异常为【{}】，父类的父类异常为【{}】，异常信息为【{}】", superClass.getSimpleName(), superClass.getSuperclass().getSimpleName(), e.getMessage());
                    superClass = superClass.getSuperclass();
                }
            }
        }
        //循环遍历完成后，依然没有找到注册异常，返回默认异常错误
        if (response == null) {
            response = JsonResponse.defaultErrorResponse();
            response.setError(e.getClass().getSimpleName());
        }
        setCorrectErrorMessage(response, e);
        return response;
    }

    private static void setCorrectErrorMessage(JsonResponse response, Exception e){
        if(StringUtils.isNotEmpty(e.getMessage()) && e.getMessage().contains(" constraint [")){
            String constraintField = e.getMessage();
            constraintField = StringUtils.substringAfter(constraintField, "constraint [");
            constraintField = StringUtils.substringBefore(constraintField, "]");
            response.setMessage("数据".concat(constraintField).concat("唯一性校验错误"));
        }
        else if(StringUtils.isEmpty(response.getMessage())){
            response.setMessage(e.getMessage());
        }
    }

}

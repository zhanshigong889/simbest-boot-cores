/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.config.JacksonConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.File;
import java.io.IOException;

/**
 * 用途：基于Jackson 封装的工具类
 * 作者: lishuyi
 * 时间: 2018/2/28  23:43
 */
@Slf4j
public class JacksonUtils {
    private static ObjectMapper mapper = null;

    static {
        JacksonConfiguration configuration = new JacksonConfiguration();
        mapper = configuration.objectMapper();
    }

    private JacksonUtils() {
    }

    /**
     * javaBean,list,array 转JSON字符串
     *
     * @param obj 对象
     * @return JSON串
     */
    public static String obj2json(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            Exceptions.printException(e);
            return null;
        }
    }

    /**
     * JSON串转对象
     *
     * @param jsonStr JSON字符串
     * @param clazz   序列化的对象
     * @param <T>     泛型
     * @return 返回转换好的对象
     * @throws IOException 转换IO异常
     */
    public static <T> T json2obj(String jsonStr, Class<T> clazz) {
        try {
            return mapper.readValue(jsonStr, clazz);
        } catch (IOException e) {
            Exceptions.printException(e);
            return null;
        }
    }

    /**
     * JSON串 转 JsonNode
     *
     * @param jsonStr JSON串
     * @return 转换结果
     */
    public static JsonNode json2node(String jsonStr) {
        try {
            return mapper.readTree(jsonStr);
        } catch (IOException e) {
            Exceptions.printException(e);
            return null;
        }
    }

    public static <T> T jsonFile2obj(File jsonFile, Class<T> clazz) {
        if (jsonFile != null && jsonFile.exists()) {
            try {
                return mapper.readValue(jsonFile, clazz);
            } catch (IOException e) {
                log.error(String.format("Error 12000: Json source is {}, translate class is {}", jsonFile, clazz));
                Exceptions.printException(e);
            }
        } else {
            log.error(String.format("@@@@Error 12000: Json File is empty!!!"));
        }
        return null;
    }

    /**
     * 将Json字符串转换为对象实体列表
     *
     * @param jsonStr
     * @param listType
     * @return
     */
    public static <T> T json2Type(String jsonStr, TypeReference<T> listType) {
        T result = null;
        if (!StringUtils.isEmpty(jsonStr)) {
            if (jsonStr != null && listType != null) {
                try {
                    result = mapper.readValue(jsonStr, listType);
                } catch (IOException e) {
                    log.error(String.format("Error 12000: failed translate Json source {} to list object", jsonStr));
                    log.error(Exceptions.getStackTraceAsString(e));
                }
            }
        } else {
            log.error(String.format("@@@@Error 12000: Json source is empty!!!"));
        }
        return result;
    }


    public static String format(String jsonStr) {
        String result = null;
        try {
            Object json = mapper.readValue(jsonStr, Object.class);
            result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (IOException e) {
            Exceptions.printException(e);
        }
        return result;
    }

    public static String escapeString(String value){
        String escapeValue = HtmlUtils.htmlEscape(value);
        //String escapeValue = StringEscapeUtils.escapeHtml4(value.toString());
        //以下定义因为特殊业务要求，放开的字符
//            escapeValue=escapeValue.replaceAll("&quot;", "\"");
            escapeValue=escapeValue.replaceAll("&amp;", "&");
            escapeValue=escapeValue.replaceAll("&ldquo;", "“");
            escapeValue=escapeValue.replaceAll("&rdquo;", "”");
//            escapeValue=escapeValue.replaceAll("&mdash;", "—");
//            escapeValue=escapeValue.replaceAll("&times;", "×");
//            escapeValue=escapeValue.replaceAll("&lt;", "<");
//            escapeValue=escapeValue.replaceAll("&gt;", ">");
//            escapeValue=escapeValue.replaceAll("&le;", "<=");
//            escapeValue=escapeValue.replaceAll("&ge;", ">=");
        return escapeValue;
    }

    public static String unescapeString(String value){
        String unescapeValue = HtmlUtils.htmlUnescape(value);
        return unescapeValue;
    }

}

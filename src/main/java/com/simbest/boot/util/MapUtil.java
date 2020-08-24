/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.mzlion.core.lang.Assert;
import com.simbest.boot.base.exception.Exceptions;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static cn.hutool.core.util.CharUtil.UNDERLINE;
import static com.simbest.boot.constants.ApplicationConstants.EMPTY;

/**
 * 用途：Map工具类
 * 作者: lishuyi
 * 时间: 2018/6/7  0:48
 */
public class MapUtil {

    /**
     * Map转对象
     * @param map
     * @param beanClass
     * @return
     * @throws Exception
     */
    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null || map.size()<=0)
            return null;

        Object obj = beanClass.newInstance();
        //获取关联的所有类，本类以及所有父类
        boolean ret = true;
        Class oo = obj.getClass();
        List<Class> clazzs = new ArrayList<Class>();
        while(ret){
            clazzs.add(oo);
            oo = oo.getSuperclass();
            if(oo == null || oo == Object.class)break;
        }

        for(int i=0;i<clazzs.size();i++){
            Field[] fields = clazzs.get(i).getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if(Modifier.isStatic(mod) || Modifier.isFinal(mod)){
                    continue;
                }
                //由字符串转换回对象对应的类型
                if (field != null) {
                    field.setAccessible(true);
                    Object value = map.get(field.getName());
                    // 如果类型是Boolean 是封装类
                    if ("class java.lang.Boolean".equals(field.getGenericType().toString())){
                        value = StrUtil.equals( "1",cn.hutool.core.map.MapUtil.getStr( map,field.getName())) ?true:false;
                    }
                    field.set(obj, value);
                }
            }
        }
        return obj;
    }

    public static Map<String, ?> objectToMap(Object obj, Set<String> ignoreProperties) {
        Map<String, ?> resultMap = objectToMap(obj);
        for(String key : ignoreProperties) {
            resultMap.remove(key);
        }
        return resultMap;
    }

    /**
     * 对象转Map
     * @param obj
     * @return
     */
    public static Map<String, ?> objectToMap(Object obj) {
        if(obj == null){
            return null;
        }
        //获取关联的所有类，本类以及所有父类
        boolean ret = true;
        Class oo = obj.getClass();
        List<Class> clazzs = new ArrayList<Class>();
        while(ret){
            clazzs.add(oo);
            oo = oo.getSuperclass();
            if(oo == null || oo == Object.class)break;
        }

        Map<String, Object> map = new HashMap<String, Object>();

        for(int i=0;i<clazzs.size();i++){
            Field[] declaredFields = clazzs.get(i).getDeclaredFields();
            for (Field field : declaredFields) {
                int mod = field.getModifiers();
                //过滤 static 和 final 类型
                if(Modifier.isStatic(mod) || Modifier.isFinal(mod)){
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if(null != value) {
                        map.put(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    Exceptions.printException(e);
                }
            }
        }
        return map;
    }

    /**
     * 简单 xml 转换为 Map
     * @param xml
     * @return
     */
    public static Map<String,String> xmlToMap(String xml){
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
            Element element = document.getDocumentElement();
            NodeList nodeList = element.getChildNodes();
            Map<String, String> map = new LinkedHashMap<String, String>();
            for(int i=0;i<nodeList.getLength();i++){
                Element e = (Element) nodeList.item(i);
                map.put(e.getNodeName(),e.getTextContent());
            }
            return map;
        } catch (DOMException e) {
            Exceptions.printException(e);
        } catch (ParserConfigurationException e) {
            Exceptions.printException(e);
        } catch (SAXException e) {
            Exceptions.printException(e);
        } catch (IOException e) {
            Exceptions.printException(e);
        }
        return null;
    }

    /**
     * url 参数串连
     * @param map
     * @param valueUrlencode
     * @return
     */
    public static String mapJoin(Map<String, String> map, boolean valueUrlencode){
        StringBuilder stringBuilder = new StringBuilder();
        for(String key :map.keySet()){
            if(map.get(key)!=null&&!"".equals(map.get(key))){
                try {
                    String temp = (key.endsWith("_")&&key.length()>1)?key.substring(0,key.length()-1):key;
                    stringBuilder.append(temp)
                            .append("=")
                            .append(valueUrlencode? URLEncoder.encode(map.get(key),"utf-8").replace("+", "%20"):map.get(key))
                            .append("&");
                } catch (UnsupportedEncodingException e) {
                    Exceptions.printException(e);;
                }
            }
        }
        if(stringBuilder.length()>0){
            stringBuilder.deleteCharAt(stringBuilder.length()-1);
        }
        return stringBuilder.toString();
    }

    /**
     * Map key 排序
     * @param map
     * @return
     */
    public static Map<String,String> orderMapByKey(Map<String, String> map){
        HashMap<String, String> tempMap = new LinkedHashMap<String, String>();
        List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(	map.entrySet());

        Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1,Map.Entry<String, String> o2) {
                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });

        for (int i = 0; i < infoIds.size(); i++) {
            Map.Entry<String, String> item = infoIds.get(i);
            tempMap.put(item.getKey(), item.getValue());
        }
        return tempMap;
    }

    /**
     * Map key 排序
     * @param map
     * @return
     */
    public static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, String> sortMap = new TreeMap<String, String>(
                new MapKeyComparator());

        sortMap.putAll(map);

        return sortMap;
    }

    /**
     * 获取Http请求的参数
     * @param request
     * @return
     */
    public static Map<String, String> getRequestParameters(HttpServletRequest request){
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = Maps.newHashMap();
        for (String key : requestParams.keySet()) {
            String value = "";
            String[] values = requestParams.get(key);
            for (int i = 0; i < values.length; i++) {
                value += values[i];
            }
            if(StringUtils.isNotEmpty(value)){
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * 获取Http请求的URL及参数
     * 按照腾讯官方要求，将请求参数拼装至URL中，以便计算签名
     * @param request
     * @return
     */
    public static String getRequestUrlWithParameters(HttpServletRequest request) {
        String pageUrl = request.getRequestURI();
        String queryString = request.getQueryString();
        if(StringUtils.isNotEmpty(queryString)){
            String[] queryStrings = StringUtils.split(queryString, "&");
            if(queryStrings.length > 0){
                pageUrl += "?";
                for(String q:queryStrings){
                    if(!StringUtils.startsWith(q, "#") && !StringUtils.startsWith(q, "code=") && !StringUtils.startsWith(q, "state="))
                        pageUrl += q+"&";
                }
            }
            pageUrl = StringUtils.removeEnd(pageUrl, "&");
        }
        return pageUrl;
    }

    /**
     * 追加Http请求的URL参数
     * @param request
     * @return
     */
    public static String addRequestUrlWithParameters(HttpServletRequest request, Map<String, String> parameters) {
        Assert.notEmpty(parameters, "Add parameters can not be null");
        String pageUrl = request.getRequestURI();
        String queryString = request.getQueryString();
        if(StringUtils.isNotEmpty(queryString)){
            pageUrl += "?" + queryString +"&";
            for (String key : parameters.keySet()) {
                pageUrl += key+"="+parameters.get(key)+"&";
            }
        } else{
            pageUrl += "?";
            for (String key : parameters.keySet()) {
                pageUrl += key+"="+parameters.get(key)+"&";
            }
        }
        return StringUtils.removeEnd(pageUrl, "&");
    }

    // 将map值全部转换为大写
    public static Map<String, Object> transformUpperCase(Map<String, Object> orgMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (orgMap == null || orgMap.isEmpty()) {
            return resultMap;
        }
        Set<String> keySet = orgMap.keySet();
        for (String key : keySet) {
            String newKey = key.toUpperCase();
//            newKey = newKey.replace("_", "");
            resultMap.put(newKey, orgMap.get(key));
        }
        return resultMap;
    }

    // 将map值全部转换为小写
    public static Map<String, Object> transformLowerCase(Map<String, Object> orgMap) {
        Map<String, Object> resultMap = new HashMap<>();
        if (orgMap == null || orgMap.isEmpty()) {
            return resultMap;
        }
        Set<String> keySet = orgMap.keySet();
        for (String key : keySet) {
            String newKey = key.toLowerCase();
            resultMap.put(newKey, orgMap.get(key));
        }
        return resultMap;
    }

    /**
     * 将List中map的key值命名方式格式化为驼峰
     */
    public static List<Map<String, Object>> formatHumpNameForList(List<Map<String, Object>> list) {
        List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> o : list) {
            newList.add(formatHumpName(o));
        }
        return newList;
    }

    /**
     * 将map的key值命名方式格式化为驼峰
     */
    public static Map<String, Object> formatHumpName(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<String, Object>();
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            String newKey = underlineToCamel(key);
            newMap.put(newKey, entry.getValue());
        }
        return newMap;
    }

    public static String underlineToCamel(String param) {
        if (StringUtils.isEmpty(param)) {
            return EMPTY;
        }
        else if(StringUtils.contains(param, UNDERLINE)) {
            int len = param.length();
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                char c = param.charAt(i);
                if (c == UNDERLINE) {
                    if (++i < len) {
                        sb.append(Character.toUpperCase(param.charAt(i)));
                    }
                } else {
                    sb.append(Character.toLowerCase(param.charAt(i)));
                }
            }
            return sb.toString();
        }
        else if(StringUtils.isAllUpperCase(param)){
            return param.toLowerCase();
        }
        else{
            return param;
        }
    }

}

class MapKeyComparator implements Comparator<String>{

    @Override
    public int compare(String str1, String str2) {

        return str1.compareTo(str2);
    }
}
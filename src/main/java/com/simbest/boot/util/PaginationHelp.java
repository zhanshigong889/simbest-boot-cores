package com.simbest.boot.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <strong>Title : PaginationHelp</strong><br>
 * <strong>Description : 分页组件工具</strong><br>
 * <strong>Create on : 2018/12/21</strong><br>
 * <strong>Modify on : 2018/12/21</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
@Component
public class PaginationHelp {

    /**
     * 获取分页参数
     * @param page              页码
     * @param size              当前页数量
     * @param direction         排序字段
     * @param properties        排序规则 desc、asc
     * @return Pageable
     */
    public Pageable getPageable( int page, int size, String direction, String properties) {
        int pagePage = page < 1 ? 0 : (page - 1);
        int pageSize = size < 1 ? 1 : (size > 100 ? 100 : size);
        Pageable pageable;
        if ( StringUtils.isNotEmpty(direction) && StringUtils.isNotEmpty(properties)) {
            // 生成指定排序规则-顺序
            Sort.Direction sortDirection;
            String[] sortProperties;
            try {
                // 先转换为大写
                direction = direction.toUpperCase();
                // 再获取枚举
                sortDirection = Sort.Direction.valueOf(direction);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                sortDirection = Sort.Direction.ASC;
            }
            // 生成指定排序规则-关键字
            sortProperties = properties.split(",");
            // 生成排序规则
            // 生成排序规则
            Sort sort = Sort.by(sortDirection, sortProperties);
            pageable = PageRequest.of(pagePage, pageSize, sort);
        } else {
            pageable = PageRequest.of(pagePage, pageSize);
        }
        return pageable;
    }

    /**
     * 把返回的List<Map<String, Object>>结果数据根据传递的分页参数转换成分页对象
     *     1.该方法用于当使用自定义sql查询出来的list，封装成带分页的对象
     * @param mapList           查询出来的list结果数据
     * @param pageIndex         页码
     * @param pageSize          每页数量
     * @param direction         排序字段
     * @param properties        排序规则 desc、asc
     * @return Page<List<Map<String, Object>>>
     */
    public Page<List<Map<String, Object>>> getPageList( List<Map<String, Object>> mapList,
                                                        Integer pageIndex, Integer pageSize,
                                                        String direction, String properties){
        Page<List<Map<String, Object>>> page;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int count = 0;
        for (int i = 0,countS = mapList.size();i < countS;i++){
            if (i >= (pageIndex - 1) * pageSize){
                count++;
                list.add(mapList.get(i));
            }
            if (count == pageSize ){
                break;
            }
        }
        Pageable pageable = getPageable(pageIndex, pageSize, direction, properties);
        page = new PageImpl(list, pageable, mapList.size());
        return page;
    }
}

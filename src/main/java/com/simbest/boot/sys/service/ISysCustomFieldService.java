/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.service;

import com.simbest.boot.base.service.ILogicService;
import com.simbest.boot.sys.model.SysCustomField;

import java.util.Map;

/**
 * 用途：实体自定义字段逻辑层
 * 作者: lishuyi
 * 时间: 2017/12/22  15:51
 */
public interface ISysCustomFieldService extends ILogicService<SysCustomField, String> {

    /**
     * 获取系统有自定义字段的实体类型
     *
     * @return Map
     */
    Map<String, String> getFieldClassifyMap();

    /**
     * 查询某个自定义字段
     *
     * @param fieldClassify 所属实体分类
     * @return SysCustomField
     */
    SysCustomField findByFieldClassify(String fieldClassify);

}

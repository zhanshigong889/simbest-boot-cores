package com.simbest.boot.sys.service;

import com.simbest.boot.base.service.ILogicService;
import com.simbest.boot.sys.model.SysDictValue;

import java.util.List;
import java.util.Map;

public interface ISysDictValueService extends ILogicService<SysDictValue,String>{

    int updateEnable(boolean enabled, String dictValueId);

    List<SysDictValue> findByParentId(String parentId);

    /**
     * 根据字典值对象查询满足条件的数据字典值，若提供上级数据字典值id，则直接返回所有字典值
     */
    List<SysDictValue> findDictValue(SysDictValue sysDictValue);

    /**
     * 根据字典类型和字典值名称，获取字典值(集团和企业特有字典值)
     * @param dictType
     * @param name
     * @param blocid
     * @param corpid
     * @return SysDictValue
     */
    SysDictValue findByDictTypeAndNameAndBlocidAndCorpid(String dictType, String name, String blocid, String corpid);

    /**
     * 根据字典类型和字典值名称，获取字典值
     * @param dictType
     * @param name
     * @return SysDictValue
     */
    SysDictValue findByDictTypeAndName(String dictType, String name);

    /**
     * 根据字典类型，获取字典值
     * @param dictType
     * @return List<SysDictValue>
     */
    List<SysDictValue> findByDictType(String dictType);

    /**
     * 查看数据字典的所有值
     */
    List<Map<String,String>> findAllDictValue();

    /**
     * 查看数据字典的所有值, Map结构，key为dictType，value为字典值list
     */
    Map<String,List<Map<String,String>>> findAllDictValueMapList();

}

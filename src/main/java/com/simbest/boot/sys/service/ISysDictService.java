package com.simbest.boot.sys.service;

import com.simbest.boot.base.service.ILogicService;
import com.simbest.boot.sys.model.SysDict;

import java.util.List;

public interface ISysDictService extends ILogicService<SysDict, String> {

    SysDict findByDictType(String dictType);

    List<SysDict> findByParentId(String parentId);

    List<SysDict> findByEnabled(Boolean enabled);

}

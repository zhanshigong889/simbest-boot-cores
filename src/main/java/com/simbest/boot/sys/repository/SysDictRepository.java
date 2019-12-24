package com.simbest.boot.sys.repository;

import com.simbest.boot.base.repository.LogicRepository;
import com.simbest.boot.sys.model.SysDict;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysDictRepository extends LogicRepository<SysDict, String> {

    SysDict findByDictType(String dictType);

    List<SysDict> findByParentId(String parentId);

    List<SysDict> findByParentIdAndEnabled(String parentId, Boolean enabled);

    List<SysDict> findByEnabled(Boolean enabled);


}

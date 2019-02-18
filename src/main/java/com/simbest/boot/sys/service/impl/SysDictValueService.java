package com.simbest.boot.sys.service.impl;

import com.mzlion.core.lang.Assert;
import com.simbest.boot.base.repository.CustomDynamicWhere;
import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.sys.repository.SysDictValueRepository;
import com.simbest.boot.sys.service.ISysDictValueService;
import com.simbest.boot.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SysDictValueService extends LogicService<SysDictValue,String> implements ISysDictValueService{

    private SysDictValueRepository dictValueRepository;

    @Autowired
    private CustomDynamicWhere dynamicRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SysDictValueService(SysDictValueRepository dictValueRepository) {
        super(dictValueRepository);
        this.dictValueRepository = dictValueRepository;
    }

    @Override
    public int updateEnable(boolean enabled, String dictValueId) {
        SysDictValue val = findById(dictValueId);
        if (val == null) {
            return 0;
        }
        val.setEnabled(enabled);
        this.update(val);
        List<SysDictValue> list = findByParentId(dictValueId);
        for (SysDictValue v : list) {
            updateEnable(enabled, v.getId());
        }
        return 1;
    }

    @Override
    public List<SysDictValue> findByParentId(String parentId) {
        return dictValueRepository.findByParentId(parentId);
    }

    @Override
    public SysDictValue findById(String id) {
        return dictValueRepository.findById(id).orElse(null);
    }

    /**
     * 根据字典类型以及上级数据字典值id查询数据字典中相应值的name以及value的值
     * @param sysDictValue
     * @return
     */
    @Override
    public List<SysDictValue> findDictValue (SysDictValue sysDictValue) {
        Assert.notNull(sysDictValue, "查询参数不能为空！");
        List<SysDictValue> sysDictValueList;
        if(sysDictValue.getParentId()!=null){
            sysDictValueList=dictValueRepository.findDictValue(sysDictValue.getDictType(),sysDictValue.getParentId());
        } else {
            String selectSQL = "SELECT dv.* from sys_dict d,sys_dict_value dv ";
            String whereSQL = "WHERE d.dict_type=dv.dict_type and d.enabled=1 and dv.enabled=1 ";
            String orderBySQL = " order by dv.display_order asc";
            Map<String, Object> paramMap = ObjectUtil.getEntityPersistentFieldValueExceptId(sysDictValue);
            for(String param : paramMap.keySet()){
                if(param.equals("name")){
                    whereSQL += "AND dv.name=:name ";
                }
                if(param.equals("value")){
                    whereSQL += "AND dv.value=:value ";
                }
                if(param.equals("dictType")){
                    whereSQL += "AND d.dict_type=:dictType ";
                }
            }
            String querySQL = selectSQL+whereSQL+orderBySQL;
            Query dataQuery = entityManager.createNativeQuery(querySQL, SysDictValue.class);
            for(String param : paramMap.keySet()){
                if(param.equals("name")){
                    dataQuery.setParameter("name", paramMap.get(param));
                }
                if(param.equals("value")){
                    dataQuery.setParameter("value", paramMap.get(param));
                }
                if(param.equals("dictType")){
                    dataQuery.setParameter("dictType", paramMap.get(param));
                }
            }
            sysDictValueList = dataQuery.getResultList();
        }
        if(sysDictValueList.size() > 0) {
            sysDictValueList.get(0).setIsDefault(true);
        }
        return sysDictValueList;
    }

    /**
     * 查看数据字典的所有值
     * @return
     */
    @Override
    public List<Map<String, String>> findAllDictValue () {
        return dictValueRepository.findAllDictValue();
    }

}

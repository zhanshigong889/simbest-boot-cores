package com.simbest.boot.sys.service.impl;

import com.google.common.collect.Lists;
import com.simbest.boot.base.repository.CustomDynamicWhere;
import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.sys.repository.SysDictValueRepository;
import com.simbest.boot.sys.service.ISysDictValueService;
import com.simbest.boot.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SysDictValueService extends LogicService<SysDictValue,String> implements ISysDictValueService {

    public static final String CACHE_KEY = "SYS_DICT_VALUE_CACHE:";
    public static final int CACHE_EXPIRE_SECONDES = 3600;

    private SysDictValueRepository dictValueRepository;

    @Autowired
    private CustomDynamicWhere dynamicRepository;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private RedisTemplate<String, SysDictValue> dvRedisTemplate;

    @Autowired
    private RedisTemplate<String, List<SysDictValue>> dvListRedisTemplate;

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


    @Override
    public SysDictValue findByDictTypeAndNameAndBlocidAndCorpid(String dictType, String name, String blocid, String corpid) {
        String key = CACHE_KEY+appConfig.getAppcode().concat(ApplicationConstants.COLON)
                .concat(dictType).concat(ApplicationConstants.COLON).concat(name)
                .concat(ApplicationConstants.COLON).concat(blocid)
                .concat(ApplicationConstants.COLON).concat(corpid);
        SysDictValue dv = dvRedisTemplate.opsForValue().get(key);
        if(null == dv) {
            dv = dictValueRepository.findByDictTypeAndNameAndBlocidAndCorpid(dictType, name, blocid, corpid);
            dvRedisTemplate.opsForValue().set(key, dv, CACHE_EXPIRE_SECONDES, TimeUnit.SECONDS);
        }
        return dv;
    }

    @Override
    public SysDictValue findByDictTypeAndName(String dictType, String name){
        String key = CACHE_KEY+appConfig.getAppcode().concat(ApplicationConstants.COLON)
                .concat(dictType).concat(ApplicationConstants.COLON).concat(name);
        SysDictValue dv = dvRedisTemplate.opsForValue().get(key);
        if(null == dv) {
            dv = dictValueRepository.findByDictTypeAndName(dictType, name);
            dvRedisTemplate.opsForValue().set(key, dv, CACHE_EXPIRE_SECONDES, TimeUnit.SECONDS);
        }
        return dv;
    }

    /**
     * 根据字典值对象查询满足条件的数据字典值，若提供上级数据字典值id，则直接返回所有字典值
     * @param dv
     * @return
     */
    @Override
    public List<SysDictValue> findDictValue (SysDictValue dv) {
        List<String> params = Lists.newArrayList();
        if(StringUtils.isNotEmpty(dv.getBlocid()))
            params.add(dv.getBlocid());
        if(StringUtils.isNotEmpty(dv.getCorpid()))
            params.add(dv.getCorpid());
        if(StringUtils.isNotEmpty(dv.getDictType()))
            params.add(dv.getDictType());
        if(StringUtils.isNotEmpty(dv.getName()))
            params.add(dv.getName());
        if(StringUtils.isNotEmpty(dv.getValue()))
            params.add(dv.getValue());
        String key = CACHE_KEY.concat(appConfig.getAppcode()).concat(ApplicationConstants.COLON)+StringUtils.join(params, ApplicationConstants.LINE);
        List<SysDictValue> result = dvListRedisTemplate.opsForValue().get(key);
        if(null == result || result.size()==0){
            result = findDictValueDatabase(dv);
            dvListRedisTemplate.opsForValue().set(key, result, CACHE_EXPIRE_SECONDES, TimeUnit.SECONDS);
        }
        return result;
    }

    private List<SysDictValue> findDictValueDatabase (SysDictValue sysDictValue) {
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
                if(param.equals("dictType")){
                    whereSQL += "AND d.dict_type=:dictType ";
                    whereSQL += "AND dv.dict_type=:dictType ";
                }
                if(param.equals("isPublic")){
                    whereSQL += "AND d.is_public=:isPublic ";
                }
                if(param.equals("flag")){
                    whereSQL += "AND d.flag=:flag ";
                }

                if(param.equals("name")){
                    whereSQL += "AND dv.name=:name ";
                }
                if(param.equals("value")){
                    whereSQL += "AND dv.value=:value ";
                }
                if(param.equals("flag")){
                    whereSQL += "AND dv.flag=:flag ";
                }
                if(param.equals("isPublic")){
                    whereSQL += "AND dv.is_public=:isPublic ";
                }
                if(param.equals("blocid")){
                    whereSQL += "AND dv.blocid=:blocid ";
                }
                if(param.equals("corpid")){
                    whereSQL += "AND dv.corpid=:corpid ";
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
                if(param.equals("flag")){
                    dataQuery.setParameter("flag", paramMap.get(param));
                }
                if(param.equals("dictType")){
                    dataQuery.setParameter("dictType", paramMap.get(param));
                }
                if(param.equals("isPublic")){
                    dataQuery.setParameter("isPublic", paramMap.get(param));
                }
                if(param.equals("blocid")){
                    dataQuery.setParameter("blocid", paramMap.get(param));
                }
                if(param.equals("corpid")){
                    dataQuery.setParameter("corpid", paramMap.get(param));
                }
            }
            sysDictValueList = dataQuery.getResultList();
        }
        if(sysDictValueList.size() > 0) {
            //判断字典值是否有默认值
            AtomicBoolean haveDefault = new AtomicBoolean(false);
            sysDictValueList.forEach( dv ->{
                if(null!=dv.getIsDefault() && dv.getIsDefault()){
                    haveDefault.set(true);
                }
            });
            //如果没有默认值，则设置第一条字典值为默认值
            if(!haveDefault.get()) {
                sysDictValueList.get(0).setIsDefault(true);
            }
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

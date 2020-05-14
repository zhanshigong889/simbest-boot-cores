package com.simbest.boot.sys.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.exceptions.BusinessForbiddenException;
import com.simbest.boot.sys.model.SysDict;
import com.simbest.boot.sys.model.SysDictValue;
import com.simbest.boot.sys.repository.SysDictValueRepository;
import com.simbest.boot.sys.service.ISysDictService;
import com.simbest.boot.sys.service.ISysDictValueService;
import com.simbest.boot.util.ObjectUtil;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.simbest.boot.constants.ApplicationConstants.ONE;
import static com.simbest.boot.constants.ApplicationConstants.ZERO;
import static com.simbest.boot.constants.AuthoritiesConstants.ACCESS_FORBIDDEN;
import static com.simbest.boot.constants.AuthoritiesConstants.BUSINESS_FORBIDDEN;
import static com.simbest.boot.constants.AuthoritiesConstants.ROLE_ADMIN;
import static com.simbest.boot.constants.AuthoritiesConstants.SUPER_ADMIN;
import static com.simbest.boot.constants.AuthoritiesConstants.USER;

@Slf4j
@Service
public class SysDictValueService extends LogicService<SysDictValue,String> implements ISysDictValueService {

    private SysDictValueRepository dictValueRepository;

    @Autowired
    private ISysDictService dictService;

    @Autowired
    private SysDictValueCacheUtil dvCacheUtil;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SysDictValueService(SysDictValueRepository dictValueRepository) {
        super(dictValueRepository);
        this.dictValueRepository = dictValueRepository;
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
        SysDictValue dv = dvCacheUtil.loadByDictTypeAndNameAndBlocidAndCorpid(dictType, name, blocid, corpid);
        if(null == dv) {
            dv = dictValueRepository.findByDictTypeAndNameAndBlocidAndCorpid(dictType, name, blocid, corpid);
            dvCacheUtil.setByDictTypeAndNameAndBlocidAndCorpid(dictType, name, blocid, corpid, dv);
        }
        return dv;
    }

    @Override
    public SysDictValue findByDictTypeAndName(String dictType, String name){
        SysDictValue dv = dvCacheUtil.loadByDictTypeAndName(dictType, name);
        if(null == dv) {
            dv = dictValueRepository.findByDictTypeAndName(dictType, name);
            dvCacheUtil.setByDictTypeAndName(dictType, name, dv);
        }
        return dv;
    }

    @Override
    public List<SysDictValue> findByDictType(String dictType){
        SysDictValue dv = SysDictValue.builder().dictType(dictType).build();
        return findDictValue(dv);
    }

    /**
     * 根据字典值对象查询满足条件的数据字典值，若提供上级数据字典值id，则直接返回所有字典值
     * @param dv
     * @return List<SysDictValue>
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
        List<SysDictValue> result = dvCacheUtil.loadByParameters(params);
        if(null == result || result.size()==0){
            result = findDictValueDatabase(dv);
            dvCacheUtil.setByParameters(params, result);
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
     * @return List<Map<String, String>>
     */
    @Override
    public List<Map<String, String>> findAllDictValue () {
        return dictValueRepository.findAllDictValue();
    }

    @Override
    public Map<String,List<Map<String,String>>> findAllDictValueMapList() {
        Map<String,List<Map<String,String>>> resultMap = Maps.newHashMap();
        List<Map<String, String>> dictValueMapList = dictValueRepository.findAllDictValueMapList();
        for(Map<String, String> rowMap : dictValueMapList){
            String dictType = rowMap.get("DICT_TYPE");
            if(null == resultMap.get(dictType)){
                List<Map<String,String>> dictTypeList = Lists.newArrayList();
                dictTypeList.add(rowMap);
                resultMap.put(dictType, dictTypeList);
            }
            else{
                resultMap.get(dictType).add(rowMap);
            }

        }
        return resultMap;
    }

    @Override
    public Map<String, List<Map<String, String>>> findDictValueMapList(String[] typeList) {
        Map<String,List<Map<String,String>>> resultMap = Maps.newHashMap();
        List<Map<String, String>> dictValueMapList = dictValueRepository.findDictValueMapList(typeList);
        for(Map<String, String> rowMap : dictValueMapList){
            String dictType = rowMap.get("DICT_TYPE");
            if(null == resultMap.get(dictType)){
                List<Map<String,String>> dictTypeList = Lists.newArrayList();
                dictTypeList.add(rowMap);
                resultMap.put(dictType, dictTypeList);
            }
            else{
                resultMap.get(dictType).add(rowMap);
            }
        }
        return resultMap;
    }


    @Override
    public int updateEnable(boolean enabled, String dictValueId) {
        if(SecurityUtils.hasAnyPermission(new String[]{SUPER_ADMIN, ROLE_ADMIN})) {
            dvCacheUtil.expireAllCache();
            SysDictValue val = findById(dictValueId);
            if (val == null) {
                return ZERO;
            }
            val.setEnabled(enabled);
            this.update(val);
            List<SysDictValue> list = findByParentId(dictValueId);
            for (SysDictValue v : list) {
                updateEnable(enabled, v.getId());
            }
            return ONE;
        }
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public SysDictValue updateEnable (String id, boolean enabled) {
        if(SecurityUtils.hasAnyPermission(new String[]{SUPER_ADMIN, ROLE_ADMIN})) {
            int ret = this.updateEnable(enabled, id);
            if(ONE == ret){
                return this.findById(id);
            }
            else{
                return null;
            }
        }
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    private void publicDictCheck(SysDictValue source){
        SysDict dict = dictService.findByDictType(source.getDictType());
        Assert.notNull(dict, "关联的数据字典不能为空");
        Assert.notNull(source.getDictType(), "字典类型不能为空");
        Assert.notNull(source.getName(), "字典值名称不能为空");
        //StrUtil.isEmptyIfStr(dict.getIsPublic())改判断是为了解决历史数据为空的情况
        if( StrUtil.isEmptyIfStr(dict.getIsPublic()) || dict.getIsPublic()){
            Assert.isTrue(StringUtils.isEmpty(source.getBlocid()), "公共字典，字典值不能维护blocid");
            Assert.isTrue(StringUtils.isEmpty(source.getCorpid()), "公共字典，字典值不能维护corpid");
            //去除以下两句判断，因为更新的时候也过执行这里，这时候数据肯定存在了
            //SysDictValue dv = findByDictTypeAndName(source.getDictType(), source.getName());
            //Assert.isNull(dv, "公共字典值已存在，不能重复添加");
        }else{
            Assert.isTrue(StringUtils.isNotEmpty(source.getBlocid()), "非公共字典，字典值必须维护blocid");
            Assert.isTrue(StringUtils.isNotEmpty(source.getCorpid()), "非公共字典，字典值必须维护corpid");
        }
    }

    @Override
    @Transactional
    public SysDictValue insert(SysDictValue source) {
        if(SecurityUtils.hasAnyPermission(new String[]{SUPER_ADMIN, ROLE_ADMIN, USER})) {
            publicDictCheck(source);
            dvCacheUtil.expireAllCache();
            return super.insert(source);
        }else {
            throw new AccessDeniedException( ACCESS_FORBIDDEN );
        }
    }

    @Override
    @Transactional
    public SysDictValue update(SysDictValue source) {
        if(SecurityUtils.hasAnyPermission(new String[]{SUPER_ADMIN, ROLE_ADMIN, USER})) {
            publicDictCheck(source);
            dvCacheUtil.expireAllCache();
            return super.update(source);
        }
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public List<SysDictValue> saveAll(Iterable<SysDictValue> entities) {
        if(SecurityUtils.hasAnyPermission(new String[]{SUPER_ADMIN, ROLE_ADMIN})) {
            List<SysDictValue> list = Lists.newArrayList();
            for(SysDictValue dv : entities){
                list.add(this.insert(dv));
            }
            return list;
        }
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        if(SecurityUtils.hasAnyPermission(new String[]{SUPER_ADMIN, ROLE_ADMIN})) {
            dvCacheUtil.expireAllCache();
            super.deleteById(id);
        }
        else
            throw new AccessDeniedException(ACCESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void delete(SysDictValue o) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends SysDictValue> iterable ) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void deleteAll() {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void deleteAllByIds(Iterable<? extends String> pks ) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void scheduleLogicDelete(String id, LocalDateTime localDateTime) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

    @Override
    @Transactional
    public void scheduleLogicDelete(SysDictValue entity, LocalDateTime localDateTime) {
        throw new BusinessForbiddenException(BUSINESS_FORBIDDEN);
    }

}

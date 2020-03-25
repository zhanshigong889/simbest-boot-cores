package com.simbest.boot.sys.repository;

import com.simbest.boot.base.repository.LogicRepository;
import com.simbest.boot.sys.model.SysDictValue;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SysDictValueRepository extends LogicRepository<SysDictValue, String> {

    List<SysDictValue> findByParentId(String parentId);

    List<SysDictValue> findByEnabled(Boolean enabled);

    SysDictValue findByDictTypeAndNameAndBlocidAndCorpid(String dictType, String name, String blocid, String corpid);
    
    SysDictValue findByDictTypeAndName(String dictType, String name);

    /**
     * 根据字典类型查询数据字典中的相应值name以及value的值
     */
    String sql1 = " SELECT dv.* from sys_dict d,sys_dict_value dv where d.dict_type=dv.dict_type and d.enabled=1 and dv.enabled=1 and d.dict_type=:dictType"+
            " order by dv.display_order asc ";
    @Query (value = sql1,nativeQuery = true)
    List<SysDictValue> findDictValue(@Param ("dictType") String dictType);

    /**
     *根据字典类型以及值的父类型来查询数据字典中的相应值name以及value的值
     */
    String sql2 = " SELECT dv.* from sys_dict d,sys_dict_value dv where d.dict_type=dv.dict_type and d.enabled=1 and dv.enabled=1 " +
            " and d.dict_type=:dictType and dv.parent_id=:parentId"+
            " order by dv.display_order asc ";
    @Query (value = sql2,nativeQuery = true)
    List<SysDictValue> findDictValue(@Param ("dictType") String dictType,@Param ("parentId")String parentId);

    /**
     *根据字典类型以及值的父类型来查询数据字典中的相应值name以及value的值
     */
    String sql4 = " SELECT dv.name as name,dv.value as value,dv.isDefault as isDefault from sys_dict d,sys_dict_value dv where d.dict_type=dv.dict_type and d.enabled=1 and dv.enabled=1 " +
            " and d.dict_type=:dictType and dv.parent_id=:parentId"+
            " order by dv.display_order asc";
    @Query (value = sql4,nativeQuery = true)
    List<Map<String,String>> findDictValue2(@Param ("dictType") String dictType,@Param ("parentId")String parentId);

    /**
     * 查看数据字典的所有值
     */
    String sql3 = " SELECT dv.*,d.name as dict_type_name from sys_dict d,sys_dict_value dv where d.dict_type=dv.dict_type and d.enabled=1 and dv.enabled=1 "+
            " order by d.display_order asc, dv.display_order asc ";
    @Query (value = sql3,nativeQuery = true)
    List<Map<String,String>> findAllDictValue();

    /**
     * 查看数据字典的所有值
     */
    String sql6 = " SELECT dv.id, dv.name, dv.value, dv.display_order, dv.dict_type, dv.value_type, dv.is_default from sys_dict d,sys_dict_value dv where d.dict_type=dv.dict_type and d.enabled=1 and dv.enabled=1 "+
            " order by d.display_order asc, dv.display_order asc ";
    @Query (value = sql6,nativeQuery = true)
    List<Map<String,String>> findAllDictValueMapList();

    /**
     * 根据dictType还有父id来查看字典的信息,单表查询
     */
    String sql5 = " SELECT dv.* from sys_dict_value dv where dv.enabled=1 and dv.dict_type=:dictType and dv.parent_id=:parentId "+
            " order by case dv.display_order when null then 10000000 end,dv.display_order asc ";
    @Query (value = sql5,nativeQuery = true)
    List<SysDictValue> findDictVlueByTypeAndParent(@Param( "dictType" ) String dictType,@Param( "parentId" ) String parentId);
}

package com.simbest.boot.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <strong>Title : ListUtil</strong><br>
 * <strong>Description : List操作工具类</strong><br>
 * <strong>Create on : 2019/2/15</strong><br>
 * <strong>Modify on : 2019/2/15</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
public final class ListUtil {

    private ListUtil() {}

    /**
     * 获取src和target中的差集
     *   测试 src = 100万
     *        target = 1万
     *   结果用时：191ms
     *
     * @param src           大集合
     * @param target        小集合（交集）
     * @return
     */
    private static List<Object> removeAll(List<Object> src,List<Object> target){
        //大集合用linkedlist
        LinkedList<Object> result = new LinkedList<Object>(src);
        //小集合用hashset
        HashSet<Object> targetHash = new HashSet<Object>(target);
        //采用Iterator迭代器进行数据的操作
        Iterator<Object> iter = result.iterator();
        while ( iter.hasNext() ){
            if(targetHash.contains(iter.next())){
                iter.remove();
            }
        }
        return result;
    }
}

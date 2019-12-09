package com.simbest.boot.util;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <strong>Title : FSearchListTool</strong><br>
 * <strong>Description : 利用二分法对List对象列表属性值的快速搜索工具</strong><br>
 * <strong>Create on : $date$</strong><br>
 * <strong>Modify on : $date$</strong><br>
 * <strong>Copyright (C) Ltd.</strong><br>
 *
 * @author LJW lijianwu@simbest.com.cn
 * @version <strong>V1.0.0</strong><br>
 * <strong>修改历史:</strong><br>
 * 修改人 修改日期 修改描述<br>
 * -------------------------------------------<br>
 */
public class FSearchListTool<T> {

    private StringBuffer mKeyWordString = new StringBuffer();   //搜索的关键字内容
    private List<T> mSearchObjs = new ArrayList<>();       //搜索的对象
    private int[] mIndexes;                                     //搜索字段的索引值

    public FSearchListTool(List<T> objects, String... fields) throws Exception {
        super();
        init(objects, fields);
    }

    private void init(List<T> objs, String... fields) throws Exception {
        if (objs != null) {
            mKeyWordString.setLength(0);
            mSearchObjs.clear();
            mSearchObjs = new ArrayList<>(objs);
            mIndexes = new int[mSearchObjs.size() * 2];
            int index = 0;
            for (int i = 0; i < mSearchObjs.size(); i++) {
                Object info = mSearchObjs.get(i);
                // 指定要搜索的字段
                String searchKey = getSearchKey(info, fields);
                // 将该字符串在总字符串中的起终位置保存下来,位置是索引值而非长度
                int length = mKeyWordString.length();
                mIndexes[index] = length;
                mKeyWordString.append(searchKey);
                length = mKeyWordString.length();
                index++;
                // 保存新加搜索字段的索引值
                mIndexes[index] = (length > 0) ? length - 1 : 0;
                index++;
            }
        }
    }

    /**
     * 通过反射从对象中取出指定字段的值
     */
    private String getSearchKey(Object obj, String... fields) throws Exception {
        StringBuilder searchKeys = new StringBuilder();
        Class<? extends Object> clazz = obj.getClass();
        try {
            for (String str : fields) {
                // 搜索字段使用空格隔开
                Field f = clazz.getDeclaredField(str);
                f.setAccessible(true);
                Object val = f.get(obj);
                searchKeys.append(val).append(" ");
                f.setAccessible(false);
            }
        } catch (Exception e) {
            throw new Exception("取值异常：" + e.getMessage());
        }
        return searchKeys.toString();
    }

    /**
     * 搜索结果
     *
     * @param keyWords 搜索的关键字，要去掉首尾的空格
     *
     * @return 返回搜索到的对象
     */
    public List<T> searchTasks(String keyWords) {
        List<T> searchedTask = new ArrayList<>();
        int[] searchIndex = getSearchIndex(keyWords);
        for (int index : searchIndex) {
            if (index != -1 && index < mSearchObjs.size() * 2) {
                T info = mSearchObjs.get(index / 2);
                if (info != null && !searchedTask.contains(info)) {
                    searchedTask.add(info);
                }
            }
        }
        return searchedTask;
    }

    /**
     * 找到匹配的索引数据
     *
     * @param keyWords 搜索的关键字
     *
     * @return 在初始化的索引数组的下标数组
     */
    private int[] getSearchIndex(String keyWords) {
        //大小写不敏感
        Pattern pattern = Pattern.compile(keyWords, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
        Matcher matcher = pattern.matcher(mKeyWordString.toString());
        ArrayList<Integer> searchResult = new ArrayList<>();
        while (matcher.find()) {
            // 不宜在此处再做循环，否则可能造成循环次数过多错误
            searchResult.add(matcher.start());
        }
        int[] searchIndexes = new int[searchResult.size()];
        for (int i = 0; i < searchIndexes.length; i++) {
            int findIndex = findIndex(searchResult.get(i));
            searchIndexes[i] = (findIndex / 2) * 2;
        }
        return searchIndexes;
    }

    /**
     * 使用二分法找到指定字符位置在索引数组中的位置
     *
     * @param charAt 字符在整个字符串中的位置
     *
     * @return 在索引数组中的位置
     */
    private int findIndex(int charAt) {
        int low = 0;
        int high = mIndexes.length - 1;
        int mid = -1;
        while (low <= high) {
            mid = (low + high) >>> 1;
            int midVal = mIndexes[mid];
            if (midVal < charAt) {
                low = mid + 1;
            } else if (midVal > charAt) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return mid;
    }

    @Data
    private static class SearchStudent {
        private String id;
        private String name;
        private String address;

        public SearchStudent(String id, String name, String address) {
            this.id = id;
            this.name = name;
            this.address = address;
        }
    }

    public static void main ( String[] args ) {
        List<SearchStudent> students = new ArrayList<>();
        students.add(new SearchStudent("001", "小李", "12"));
        students.add(new SearchStudent("002", "小王", "13"));
        students.add(new SearchStudent("003", "小红", "14"));
        students.add(new SearchStudent("004", "小明", "15"));
        students.add(new SearchStudent("005", "小王", "16"));
        students.add(new SearchStudent("006", "小王", "17"));
        students.add(new SearchStudent("007", "小张", "18"));
        try {
            FSearchListTool<SearchStudent> tool = new FSearchListTool<>(students, "id","name", "address");
            System.out.println(tool.searchTasks("小王").get(0).getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


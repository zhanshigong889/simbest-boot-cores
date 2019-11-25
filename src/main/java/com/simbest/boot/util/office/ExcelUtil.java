/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.office;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.simbest.boot.base.annotations.ExcelVOAttribute;
import com.simbest.boot.base.exception.Exceptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用途：Excel导入导出工具类
 * 作者: lishuyi
 * 时间: 2018/5/29  22:21
 */
@Slf4j
public class ExcelUtil<T> {
    Class<T> clazz;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }

    private void setFieldValue(T entity, String c, Class<?> fieldType, Field field) throws IllegalAccessException {
        if (StringUtils.isNotEmpty(c) && (String.class == fieldType)) {
            field.set(entity, StringUtils.trim(c));
        } else if (StringUtils.isNotEmpty(c) && (Date.class == fieldType)) {
            field.set(entity, com.simbest.boot.util.DateUtil.parseDate(c));
        } else if (StringUtils.isNotEmpty(c) && ((Integer.TYPE == fieldType) || (Integer.class == fieldType))) {
            field.set(entity, Integer.parseInt(c));
        } else if (StringUtils.isNotEmpty(c) && ((Long.TYPE == fieldType) || (Long.class == fieldType))) {
            field.set(entity, Long.valueOf(c));
        } else if (StringUtils.isNotEmpty(c) && ((Float.TYPE == fieldType) || (Float.class == fieldType))) {
            field.set(entity, Float.valueOf(c));
        } else if (StringUtils.isNotEmpty(c) && ((Short.TYPE == fieldType) || (Short.class == fieldType))) {
            field.set(entity, Short.valueOf(c));
        } else if (StringUtils.isNotEmpty(c) && ((Double.TYPE == fieldType) || (Double.class == fieldType))) {
            field.set(entity, Double.valueOf(c));
        } else if (Character.TYPE == fieldType) {
            if ((c != null) && (c.length() > 0)) {
                field.set(entity, Character.valueOf(c.charAt(0)));
            }
        }
    }

    public List<T> importExcel0307(String sheetName, InputStream input) throws InvalidFormatException {
        int maxCol = 0;
        List<T> list = Lists.newArrayList();
        try {
//			HSSFWorkbook workbook = new HSSFWorkbook(input);
            Workbook workbook = WorkbookFactory.create(input);
            Sheet sheet = workbook.getSheet(sheetName);
            if (!sheetName.trim().equals("")) {
                sheet = workbook.getSheet(sheetName);// 如果指定sheet名,则取指定sheet中的内容.
            }
            if (sheet == null) {
                sheet = workbook.getSheetAt(0); // 如果传入的sheet名不存在则默认指向第1个sheet.
            }
            int rows = sheet.getPhysicalNumberOfRows();

            if (rows > 0) {// 有数据时才处理
                // Field[] allFields = clazz.getDeclaredFields();// 得到类的所有field.
                Collection<Field> allFields = getMappedFiled(clazz, null, null);

                Map<Integer, Field> fieldsMap = new HashMap<Integer, Field>();// 定义一个map用于存放列的序号和field.
                for (Field field : allFields) {
                    // 将有注解的field存放到map中.
                    if (field.isAnnotationPresent(ExcelVOAttribute.class)) {
                        ExcelVOAttribute attr = field
                                .getAnnotation(ExcelVOAttribute.class);
                        int col = getExcelCol(attr.column());// 获得列号
                        maxCol = Math.max(col, maxCol);
                        // log.info(col + "====" + field.getName());
                        field.setAccessible(true);// 设置类的私有字段属性可访问.
                        fieldsMap.put(col, field);
                    }
                }
                for (int i = 1; i < rows; i++) {// 从第2行开始取数据,默认第一行是表头.
                    Row row = sheet.getRow(i);
                    // int cellNum = row.getPhysicalNumberOfCells();
                    // int cellNum = row.getLastCellNum();
                    int cellNum = maxCol;
                    T entity = null;
                    for (int j = 0; j <= cellNum; j++) {
                        Cell cell = row.getCell(j);
                        if (cell == null) {
                            continue;
                        }
                        CellType cellType = cell.getCellTypeEnum();
                        String c = "";
                        if (cellType == CellType.NUMERIC) {
                            c = String.valueOf(cell.getNumericCellValue());
                        } else if (cellType == CellType.BOOLEAN) {
                            c = String.valueOf(cell.getBooleanCellValue());
                        } else {
                            c = cell.getStringCellValue();
                        }
                        if (c == null || c.equals("")) {
                            continue;
                        }
                        entity = (entity == null ? clazz.newInstance() : entity);// 如果不存在实例则新建.
                        // log.info(cells[j].getContents());
                        Field field = fieldsMap.get(j);// 从map中得到对应列的field.
                        if (field == null) {
                            continue;
                        }
                        // 取得类型,并根据对象类型设置值.
                        Class<?> fieldType = field.getType();
                        setFieldValue(entity, c, fieldType, field);
//                        if (String.class == fieldType) {
//                            field.set(entity, String.valueOf(c));
//                        } else if ((Integer.TYPE == fieldType)
//                                || (Integer.class == fieldType)) {
//                            field.set(entity, Integer.parseInt(c));
//                        } else if ((Long.TYPE == fieldType)
//                                || (Long.class == fieldType)) {
//                            field.set(entity, Long.valueOf(c));
//                        } else if ((Float.TYPE == fieldType)
//                                || (Float.class == fieldType)) {
//                            field.set(entity, Float.valueOf(c));
//                        } else if ((Short.TYPE == fieldType)
//                                || (Short.class == fieldType)) {
//                            field.set(entity, Short.valueOf(c));
//                        } else if ((Double.TYPE == fieldType)
//                                || (Double.class == fieldType)) {
//                            field.set(entity, Double.valueOf(c));
//                        } else if (Character.TYPE == fieldType) {
//                            if ((c != null) && (c.length() > 0)) {
//                                field.set(entity,
//                                        Character.valueOf(c.charAt(0)));
//                            }
//                        }

                    }
                    if (entity != null) {
                        list.add(entity);
                    }
                }
            }

        } catch (IOException e) {
            Exceptions.printException(e);;
        } catch (InstantiationException e) {
            Exceptions.printException(e);;
        } catch (IllegalAccessException e) {
            Exceptions.printException(e);;
        } catch (IllegalArgumentException e) {
            Exceptions.printException(e);;
        }
        return list;
    }

    /**
     * 导入Excel文件，支持多个sheet页
     * @param input
     * @return
     */
    public Map<String,List<T>> importExcel(InputStream input) {
        Assert.notNull(input, "导入的Excel文件InputStream不能为空！");
        Map<String,List<T>> retMap = Maps.newHashMap();
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(input);
            int sheetNumber = workbook.getNumberOfSheets();
            for(int i=0; i<sheetNumber; i++){
                HSSFSheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                List<T> list = importExcel(workbook, sheet,1);
                retMap.put(sheetName, list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retMap;
    }

    /**
     * 导入指定sheet页
     * @param sheetName
     * @param input
     * @return
     */
    public List<T> importExcel(String sheetName, InputStream input) {
        Assert.notNull(input, "导入的Excel文件InputStream不能为空！");
        HSSFWorkbook workbook = null;
        HSSFSheet sheet = null;
        try {
            workbook = new HSSFWorkbook(input);
            sheet = workbook.getSheet(sheetName);
            if (StringUtils.isEmpty(sheetName)) {
                sheet = workbook.getSheet(sheetName);// 如果指定sheet名,则取指定sheet中的内容.
            } else{
                sheet = workbook.getSheetAt(0); // 如果传入的sheet名不存在则默认指向第1个sheet.
            }
        } catch (Exception e) {
            Exceptions.printException(e);
        }
        return importExcel(workbook,sheet,1);
    }

    /**
     * 从指定行数开始读取数据
     * @param sheetName     工作区
     * @param input         输入流
     * @param inputRow      指定行数
     * @return
     */
    public List<T> importExcel(String sheetName, InputStream input,int inputRow) {
        Assert.notNull(input, "导入的Excel文件InputStream不能为空！");
        HSSFWorkbook workbook = null;
        HSSFSheet sheet = null;
        try {
            workbook = new HSSFWorkbook(input);
            sheet = workbook.getSheet(sheetName);
            if (StringUtils.isEmpty(sheetName)) {
                sheet = workbook.getSheet(sheetName);// 如果指定sheet名,则取指定sheet中的内容.
            } else{
                sheet = workbook.getSheetAt(0); // 如果传入的sheet名不存在则默认指向第1个sheet.
            }
        } catch (Exception e) {
            Exceptions.printException(e);
        }
        return importExcel(workbook,sheet,inputRow);
    }

    /**
     * excel2003
     * @param workbook
     * @param sheet
     * @return
     */
    private List<T> importExcel(HSSFWorkbook workbook, HSSFSheet sheet,int inputRow) {
        int maxCol = 0;
        List<T> list = Lists.newArrayList();
        try {
            int rows = sheet.getPhysicalNumberOfRows();
            if (rows > 0) {// 有数据时才处理
                // Field[] allFields = clazz.getDeclaredFields();// 得到类的所有field.
                Collection<Field> allFields = getMappedFiled(clazz, null, null);
                Map<Integer, Field> fieldsMap = new HashMap<Integer, Field>();// 定义一个map用于存放列的序号和field.
                for (Field field : allFields) {
                    // 将有注解的field存放到map中.
                    if (field.isAnnotationPresent(ExcelVOAttribute.class)) {
                        ExcelVOAttribute attr = field.getAnnotation(ExcelVOAttribute.class);
                        int col = getExcelCol(attr.column());// 获得列号
                        maxCol = Math.max(col, maxCol);
                        // log.info(col + "====" + field.getName());
                        field.setAccessible(true);// 设置类的私有字段属性可访问.
                        fieldsMap.put(col, field);
                    }
                }
                for (int i = inputRow; i < rows; i++) {// 从第2行开始取数据,默认第一行是表头,从0开始算
                    HSSFRow row = sheet.getRow(i);
                    if ( row == null ){    //空行
                        continue;
                    }
                    // int cellNum = row.getPhysicalNumberOfCells();
                    // int cellNum = row.getLastCellNum();
                    int cellNum = maxCol;
                    T entity = null;
                    for (int j = 0; j <= cellNum; j++) {
                        HSSFCell cell = row.getCell(j);
                        if (cell == null) {
                            continue;
                        }
                        String c = getExcelCellValue(cell);
                        entity = (entity == null ? clazz.newInstance() : entity);// 如果不存在实例则新建.
                        Field field = fieldsMap.get(j);// 从map中得到对应列的field.
                        if (field == null) {
                            continue;
                        }
                        // 取得类型,并根据对象类型设置值.
                        Class fieldType = field.getType();
                        setFieldValue(entity, c, fieldType, field);
//                        if (String.class == fieldType) {
//                            field.set(entity, String.valueOf(c));
//                        } else if ((Integer.TYPE == fieldType)
//                                || (Integer.class == fieldType)) {
//                            field.set(entity, Integer.parseInt(c));
//                        } else if ((Long.TYPE == fieldType)
//                                || (Long.class == fieldType)) {
//                            field.set(entity, Long.valueOf(c));
//                        } else if ((Float.TYPE == fieldType)
//                                || (Float.class == fieldType)) {
//                            field.set(entity, Float.valueOf(c));
//                        } else if ((Short.TYPE == fieldType)
//                                || (Short.class == fieldType)) {
//                            field.set(entity, Short.valueOf(c));
//                        } else if ((Double.TYPE == fieldType)
//                                || (Double.class == fieldType)) {
//                            field.set(entity, Double.valueOf(c));
//                        } else if (Character.TYPE == fieldType) {
//                            if ((c != null) && (c.length() > 0)) {
//                                field.set(entity,
//                                        Character.valueOf(c.charAt(0)));
//                            }
//                        }

                    }
                    if (entity != null) {
                        list.add(entity);
                    }
                }
            }

        } catch (InstantiationException e) {
            Exceptions.printException(e);;
        } catch (IllegalAccessException e) {
            Exceptions.printException(e);;
        } catch (IllegalArgumentException e) {
            Exceptions.printException(e);;
        }
        return list;
    }


//    /*excel2003*/
//    public List<T> importExcel(String sheetName, InputStream input) {
//        int maxCol = 0;
//        List<T> list = Lists.newArrayList();
//        try {
//            HSSFWorkbook workbook = new HSSFWorkbook(input);
//            HSSFSheet sheet = workbook.getSheet(sheetName);
//            if (!sheetName.trim().equals("")) {
//                sheet = workbook.getSheet(sheetName);// 如果指定sheet名,则取指定sheet中的内容.
//            }
//            if (sheet == null) {
//                sheet = workbook.getSheetAt(0); // 如果传入的sheet名不存在则默认指向第1个sheet.
//            }
//            int rows = sheet.getPhysicalNumberOfRows();
//
//            if (rows > 0) {// 有数据时才处理
//                // Field[] allFields = clazz.getDeclaredFields();// 得到类的所有field.
//                Collection<Field> allFields = getMappedFiled(clazz, null, null);
//
//                Map<Integer, Field> fieldsMap = new HashMap<Integer, Field>();// 定义一个map用于存放列的序号和field.
//                for (Field field : allFields) {
//                    // 将有注解的field存放到map中.
//                    if (field.isAnnotationPresent(ExcelVOAttribute.class)) {
//                        ExcelVOAttribute attr = field
//                                .getAnnotation(ExcelVOAttribute.class);
//                        int col = getExcelCol(attr.column());// 获得列号
//                        maxCol = Math.max(col, maxCol);
//                        // log.info(col + "====" + field.getName());
//                        field.setAccessible(true);// 设置类的私有字段属性可访问.
//                        fieldsMap.put(col, field);
//                    }
//                }
//                for (int i = 1; i < rows; i++) {// 从第2行开始取数据,默认第一行是表头.
//                    HSSFRow row = sheet.getRow(i);
//                    // int cellNum = row.getPhysicalNumberOfCells();
//                    // int cellNum = row.getLastCellNum();
//                    int cellNum = maxCol;
//                    T entity = null;
//                    for (int j = 0; j <= cellNum; j++) {
//                        HSSFCell cell = row.getCell(j);
//                        if (cell == null) {
//                            continue;
//                        }
//                        String c = getExcelCellValue(cell);
//                        entity = (entity == null ? clazz.newInstance() : entity);// 如果不存在实例则新建.
//                        Field field = fieldsMap.get(j);// 从map中得到对应列的field.
//                        if (field == null) {
//                            continue;
//                        }
//                        // 取得类型,并根据对象类型设置值.
//                        Class fieldType = field.getType();
//                        if (String.class == fieldType) {
//                            field.set(entity, String.valueOf(c));
//                        } else if ((Integer.TYPE == fieldType)
//                                || (Integer.class == fieldType)) {
//                            field.set(entity, Integer.parseInt(c));
//                        } else if ((Long.TYPE == fieldType)
//                                || (Long.class == fieldType)) {
//                            field.set(entity, Long.valueOf(c));
//                        } else if ((Float.TYPE == fieldType)
//                                || (Float.class == fieldType)) {
//                            field.set(entity, Float.valueOf(c));
//                        } else if ((Short.TYPE == fieldType)
//                                || (Short.class == fieldType)) {
//                            field.set(entity, Short.valueOf(c));
//                        } else if ((Double.TYPE == fieldType)
//                                || (Double.class == fieldType)) {
//                            field.set(entity, Double.valueOf(c));
//                        } else if (Character.TYPE == fieldType) {
//                            if ((c != null) && (c.length() > 0)) {
//                                field.set(entity,
//                                        Character.valueOf(c.charAt(0)));
//                            }
//                        }
//
//                    }
//                    if (entity != null) {
//                        list.add(entity);
//                    }
//                }
//            }
//
//        } catch (IOException e) {
//            Exceptions.printException(e);;
//        } catch (InstantiationException e) {
//            Exceptions.printException(e);;
//        } catch (IllegalAccessException e) {
//            Exceptions.printException(e);;
//        } catch (IllegalArgumentException e) {
//            Exceptions.printException(e);;
//        }
//        return list;
//    }

    public static String getExcelCellValue(Cell cell) {
        DataFormatter df = new DataFormatter();
        String result;
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType) {
            case STRING:
                result = cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    result = new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                    break;
                } else {
                    String num = df.formatCellValue(cell);
                    result = !num.contains(".") ? num : num.replaceAll("0*$", "").replaceAll("\\.$", "");
                    break;
                }
            case BOOLEAN:
                result = "" + cell.getBooleanCellValue();
                break;
            case FORMULA:
                result = cell.getCellFormula();
                break;
            case BLANK:
                result = "";
                break;
            case ERROR:
                result = Byte.valueOf(cell.getErrorCellValue()).toString();
                break;
            default:
                result = "";
        }
        return result.trim();
    }

    /**
     * 传递过来的list的元素大于65535时，会自动分离成多个list导出到多个sheet页中
     * 为什么是65535？
     * excel的sheet页支持65536行
     * 第一行被表头占用，所以都用65536-1=65535来表示sheetSize。
     * @param list 集合记录
     * @param sheetName sheet页名称
     * @param rowSize 记录大小
     * @param output 输出流
     * @param isTagValue 标记是否导出此列
     * @return
     */
    public boolean exportExcel(List<T> list, String sheetName, OutputStream output, String isTagValue) {
        int rowSize = 65535;
        double sheetNo = Math.ceil(list.size() / rowSize)+1;// 取出一共有多少个sheet.
        String sheetNames[] = new String[(int) sheetNo];
        List<T>[] lists = new ArrayList[(int) sheetNo];
        sheetNames[0] = sheetName;
        for(int i=1; i<sheetNo; i++){
            sheetNames[i] = sheetName + i;
        }
        for(int i=0; i<sheetNo; i++){
            int startNo = i * rowSize;
            int endNo = Math.min(startNo + rowSize, list.size());
            List listi = new ArrayList();
            for (int j=startNo; j<endNo; j++) {
                listi.add(list.get(j));
            }
            lists[i] = listi;
        }
        return exportExcel(lists,sheetNames,output,isTagValue);
    }


    /**
     * 对list数据源将其里面的数据导入到excel表单
     *
     * @param output
     *            java输出流
     */
    public boolean exportExcel(List<T> lists[], String sheetNames[], OutputStream output, String isTagValue) {
        if (lists.length != sheetNames.length) {
            log.error("无法导出Excel，原因为对象列表长度与Sheet页数组长度不一致");
            return false;
        }

        HSSFWorkbook workbook = new HSSFWorkbook();// 产生工作薄对象
        for (int ii = 0; ii < lists.length; ii++) {
            List<T> list = lists[ii];
            String sheetName = sheetNames[ii];
            List<Field> fields = getMappedFiled(clazz, null, isTagValue);
            HSSFSheet sheet = workbook.createSheet();// 产生工作表对象
            workbook.setSheetName(ii, sheetName);
            HSSFRow row;
            HSSFCell cell;// 产生单元格
            HSSFCellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.SKY_BLUE.getIndex());
            style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
            style.setBorderBottom(BorderStyle.THIN); //下边框
            style.setBorderLeft(BorderStyle.THIN);//左边框
            style.setBorderTop(BorderStyle.THIN);//上边框
            style.setBorderRight(BorderStyle.THIN);//右边框
            style.setAlignment(HorizontalAlignment.CENTER); // 居中
            Font font = workbook.createFont();
            font.setFontName("微软雅黑");
            font.setFontHeightInPoints((short)14); //字体大小
            font.setBold(Boolean.TRUE);
            style.setFont(font);
            row = sheet.createRow(0);// 产生一行
            int intArray[]=new int[fields.size()];//定义列宽
            // 写入各个字段的列头名称
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                ExcelVOAttribute attr = field.getAnnotation(ExcelVOAttribute.class);
                int col = getExcelCol(attr.column());// 获得列号
                cell = row.createCell(col);// 创建列
                cell.setCellType(CellType.STRING);// 设置列中写入内容为String类型
                cell.setCellValue(attr.name());// 写入列名
                int iLength = attr.name().getBytes().length*256*2;
                intArray[i] = iLength;//初始化列宽
                sheet.setColumnWidth(i, iLength);//手动设置列宽
                // 如果设置了提示信息则鼠标放上去提示.
                if (!attr.prompt().trim().equals("")) {
                    setHSSFPrompt(sheet, "", attr.prompt(), 1, 100, col, col);// 这里默认设了2-101列提示.
                }
                // 如果设置了combo属性则本列只能选择不能输入
                if (attr.combo().length > 0) {
                    setHSSFValidation(sheet, attr.combo(), 1, 100, col, col);// 这里默认设了2-101列只能选择不能输入.
                }
                cell.setCellStyle(style);
            }

            int startNo = 0;
            int endNo = list.size();
            // 写入各条记录,每条记录对应excel表中的一行
            for (int i = startNo; i < endNo; i++) {
                row = sheet.createRow(i + 1 - startNo);
                T vo = (T) list.get(i); // 得到导出对象.
                for (int j = 0; j < fields.size(); j++) {
                    Field field = fields.get(j);// 获得field.
                    field.setAccessible(true);// 设置实体类私有属性可访问
                    ExcelVOAttribute attr = field
                            .getAnnotation(ExcelVOAttribute.class);
                    try {
                        // 根据ExcelVOAttribute中设置情况决定是否导出,有些情况需要保持为空,希望用户填写这一列.
                        if (attr.isExport()) {
                            cell = row.createCell(getExcelCol(attr.column()));// 创建cell
                            cell.setCellType(CellType.STRING);
                            cell.setCellValue(field.get(vo) == null ? ""
                                    : String.valueOf(field.get(vo)));// 如果数据存在就填入,不存在填入空格.
                            if(field.get(vo) != null){
                                String value = field.get(vo)+"";
                                //int iLength = value.getBytes().length*256*2;
                                int iLength = value.length()*256%2;
                                if(iLength>intArray[j]){
                                    intArray[j] = iLength;//设置列宽
                                    sheet.setColumnWidth(j, iLength);//手动设置列宽
                                }
                            }
                            font.setFontHeightInPoints((short)12); //字体大小
                            font.setBold( Boolean.TRUE );
                            style.setFont(font);
                            cell.setCellStyle(style);
                        }
                    } catch (IllegalArgumentException e) {
                        Exceptions.printException(e);
                    } catch (IllegalAccessException e) {
                        Exceptions.printException(e);
                    }
                }
            }
        }

        try {
            output.flush();
            workbook.write(output);
            output.close();
            return true;
        } catch (IOException e) {
            Exceptions.printException(e);
            return false;
        }

    }

    /**
     * 将EXCEL中A,B,C,D,E列映射成0,1,2,3
     *
     * @param col
     */
    public static int getExcelCol(String col) {
        col = col.toUpperCase();
        // 从-1开始计算,字母重1开始运算。这种总数下来算数正好相同。
        int count = -1;
        char[] cs = col.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            count += (cs[i] - 64) * Math.pow(26, cs.length - 1 - i);
        }
        return count;
    }

    /**
     * 设置单元格上提示
     *
     * @param sheet
     *            要设置的sheet.
     * @param promptTitle
     *            标题
     * @param promptContent
     *            内容
     * @param firstRow
     *            开始行
     * @param endRow
     *            结束行
     * @param firstCol
     *            开始列
     * @param endCol
     *            结束列
     * @return 设置好的sheet.
     */
    public static HSSFSheet setHSSFPrompt(HSSFSheet sheet, String promptTitle,
                                          String promptContent, int firstRow, int endRow, int firstCol,
                                          int endCol) {
        // 构造constraint对象
        DVConstraint constraint = DVConstraint
                .createCustomFormulaConstraint("DD1");
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_view = new HSSFDataValidation(
                regions, constraint);
        data_validation_view.createPromptBox(promptTitle, promptContent);
        sheet.addValidationData(data_validation_view);
        return sheet;
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet
     *            要设置的sheet.
     * @param textlist
     *            下拉框显示的内容
     * @param firstRow
     *            开始行
     * @param endRow
     *            结束行
     * @param firstCol
     *            开始列
     * @param endCol
     *            结束列
     * @return 设置好的sheet.
     */
    public static HSSFSheet setHSSFValidation(HSSFSheet sheet,
                                              String[] textlist, int firstRow, int endRow, int firstCol,
                                              int endCol) {
        // 加载下拉列表内容
        DVConstraint constraint = DVConstraint.createExplicitListConstraint(textlist);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_list = new HSSFDataValidation(
                regions, constraint);
        sheet.addValidationData(data_validation_list);
        return sheet;
    }

    /**
     * 得到实体类所有通过注解映射了数据表的字段
     *
     * @param map
     * @return
     */
    private List<Field> getMappedFiled(@SuppressWarnings("rawtypes") Class clazz, List<Field> fields, String isTagValue) {
        if (fields == null) {
            fields = new ArrayList<Field>();
        }

        Field[] allFields = clazz.getDeclaredFields();// 得到所有定义字段
        // 得到所有field并存放到一个list中.
        for (Field field : allFields) {
            if (field.isAnnotationPresent(ExcelVOAttribute.class)) {
                ExcelVOAttribute attr = field.getAnnotation(ExcelVOAttribute.class);
                String [] tag = attr.isTag();
                if(tag.length>0 && isTagValue != null){
                    for(String s :tag){
                        if(s.equals(isTagValue)){
                            fields.add(field);
                        }
                    }
                }
                else{
                    fields.add(field);
                }
            }
        }
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            getMappedFiled(clazz.getSuperclass(), fields, isTagValue);
        }

        return fields;
    }
}

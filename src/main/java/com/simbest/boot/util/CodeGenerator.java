package com.simbest.boot.util;

import com.google.common.collect.Maps;
import com.simbest.boot.constants.ApplicationConstants;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * @author lishuyi
 */
public class CodeGenerator {
    public static final String ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static Map<String, Integer> prefixs = Maps.newHashMap();
    private static char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static void main(String[] args) {
        System.out.println(systemUUID());
        System.out.println(addLeftZeroForNum(4, 9));
        System.out.println(addRightZeroForNum(4, 9));
        System.out.println(timestampRandomLast());
        System.out.println(timestampRandomLast());
        for (int i = 0; i < 1000; i++) {
            //System.out.println(timestampRandomLast("OA"));
            System.out.println(randomInt(1, 1000));
        }
        String s = "B-20150604-00000008";
        System.out.println(s.substring(s.lastIndexOf("-") + 1));
    }


    /**
     * 获取系统UUID，格式bd1bf93cd7e5401883693e3e7019884f
     *
     * @return
     */
    public static String systemUUID() {
        return UUID.randomUUID().toString()
                .replace(ApplicationConstants.LINE, ApplicationConstants.EMPTY);
    }

    /**
     * @param num 返回随机数的位数, 如3则可能返回029
     * @return
     */
    public static String randomInt(int num) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            sb.append((int) (Math.random() * (10)));
        }
        return sb.toString();
    }

    /**
     * 生成指定范围内的随机数
     * @param min
     * @param max
     * @return
     */
    public static int randomInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /**
     * 返回无限制编码，支持每毫秒3位随机数并发
     * 如：20160715003629069001，含义2016-07-15 00:36:29.069 001
     * <p>
     * 随机生成 a 到 b (不包含b)的整数:
     * (int)(Math.random()*(b-a))+a;
     * 随机生成 a 到 b (包含b)的整数:
     * (int)(Math.random()*(b-a+1))+a;
     *
     * @return
     */
    public static synchronized String timestampRandomLast() {
        return String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS%2$03d", DateUtil.getCurrent(), (int) (Math.random() * (899)) + 100);
    }

    public static synchronized String timestampRandomLast(String prefix) {
        return prefix + timestampRandomLast();
    }

    /**
     * 字符串不足位数补长
     *
     * @param num   长度
     * @param value 值
     * @return
     */
    public static String addLeftZeroForNum(int num, int value) {
        return String.format("%" + num + "d", value).replace(" ", "0");
    }

    /**
     * 字符串不足位数补长
     *
     * @param num   长度
     * @param value 值
     * @return
     */
    public static String addRightZeroForNum(int num, int value) {
        return String.format("%-" + num + "d", value).replace(" ", "0");
    }

    /**
     * 返回一个定长的随机字符串(只包含大小写字母、数字)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomChar(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString().toUpperCase();
    }


    /**
     * The byte[] returned by MessageDigest does not have a nice textual
     * representation, so some form of encoding is usually performed.
     * <p>
     * This implementation follows the example of David Flanagan's book
     * "Java In A Nutshell", and converts a byte array into a String of hex
     * characters.
     * <p>
     * Another popular alternative is to use a "Base64" encoding.
     */
    private static String hexEncode(byte[] aInput) {
        StringBuilder result = new StringBuilder();
        for (int idx = 0; idx < aInput.length; ++idx) {
            byte b = aInput[idx];
            result.append(chars[(b & 0xf0) >> 4]);
            result.append(chars[b & 0x0f]);
        }
        return result.toString();
    }

}

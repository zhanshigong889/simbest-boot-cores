/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 用途：手机号码校验工具类
 * 作者: lishuyi
 * 时间: 2019/8/21  15:49
 */
public class PhoneCheckUtil {

    public static void main(String[] args) {
        System.out.println(isChinaPhoneLegal("13111112222"));
        System.out.println(isChinaPhoneLegal("15711112222"));
        System.out.println(isChinaPhoneLegal("15411112222"));
        System.out.println(isChinaPhoneLegal("18011112222"));
        System.out.println(isChinaPhoneLegal("18111112222"));
        System.out.println(isChinaPhoneLegal("19711112222"));
        System.out.println(isChinaPhoneLegal("19811112222"));
        System.out.println(isChinaPhoneLegal("19911112222"));
        System.out.println(isChinaPhoneLegal("199111122221"));
        System.out.println(isChinaPhoneLegal("1991111222"));
    }

    /**
     * 大陆号码或香港号码均可
     */
    public static boolean isPhoneLegal(String str)throws PatternSyntaxException {
        return isChinaPhoneLegal(str) || isHKPhoneLegal(str);
    }

    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 15+除4的任意数
     * 18+除1和4的任意数
     * 17+除9的任意数
     * 147
     * 166
     * 198 199
     */
    public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
        String regExp = "^((13[0-9])|(15[^4])|(18[0,1,2,3,5-9])|(17[0-8])|(147)|(166)|(19[8|9]))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 香港手机号码8位数，5|6|8|9开头+7位任意数
     */
    public static boolean isHKPhoneLegal(String str)throws PatternSyntaxException {
        String regExp = "^(5|6|8|9)\\d{7}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }
}

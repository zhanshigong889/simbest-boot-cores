/**
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.util.encrypt;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用途：BigInteger加解密工具类
 * 作者: lishuyi 
 * 时间: 2017/12/30  16:35 
 */
@Slf4j
public class BigIntegerEncryptor {

    public static void main(String[] args) {
        BigInteger base = new BigInteger("20151231");
        String aaa = encryptor(base, DICTIONARY_62);
        log.debug(aaa);
        log.debug(decryptor(aaa, DICTIONARY_62).toString());
        log.debug("-----------------------------------------");
    }

    /**
     * contains hexadecimals 0-F only.
     */
    private static final char[] DICTIONARY_16 = new char[] {'0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'K', 'C', 'H', 'Y', 'W' };

    /**
     * contains only alphanumerics, in capitals and excludes letters/numbers
     * which can be confused, eg. 0 and O or L and I and 1.
     */
    private static final char[] DICTIONARY_32 = new char[] {'1', '2', '3', '4',
            '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z' };

    /**
     * contains only alphanumerics, including both capitals and smalls.
     */
    private static final char[] DICTIONARY_62 = new char[] {'0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z' };

    /**
     * contains alphanumerics, including both capitals and smalls, and the
     * following special chars: +"@*#%&/|()=?'~[!]{}-_:.,; (you might not be
     * able to read all those using a browser!
     */
    private static final char[] DICTIONARY_89 = new char[] {'0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
            'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', '+', '"', '@', '*', '#', '%', '&',
            '/', '|', '(', ')', '=', '?', '~', '[', ']', '{', '}', '$', '-',
            '_', '.', ':', ',', ';', '<', '>' };

    /**
     * 加密
     * @param source 值
     * @param dictionary 加密字典
     * @return String
     */
    public static String encryptor(BigInteger source, char[] dictionary) {
        List<Character> result = new ArrayList<Character>();
        BigInteger base = new BigInteger("" + dictionary.length);
        int exponent = 1;
        BigInteger remaining = source;
        while (true) {
            BigInteger a = base.pow(exponent);
            BigInteger b = remaining.mod(a);
            BigInteger c = base.pow(exponent - 1);
            BigInteger d = b.divide(c);
            result.add(dictionary[d.intValue()]);
            remaining = remaining.subtract(b);
            if (remaining.equals(BigInteger.ZERO)) {
                break;
            }
            exponent++;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = result.size() - 1; i >= 0; i--) {
            sb.append(result.get(i));
        }
        return sb.toString();
    }

    /**
     * 解密
     * @param code 编码
     * @param dictionary 字典
     * @return BigInteger
     */
    public static BigInteger decryptor(String code, char[] dictionary) {
        char[] chars = new char[code.length()];
        code.getChars(0, code.length(), chars, 0);
        char[] chars2 = new char[code.length()];
        int i = chars2.length - 1;
        for (char c : chars) {
            chars2[i--] = c;
        }
        Map<Character, BigInteger> dictMap = new HashMap<Character, BigInteger>();
        int j = 0;
        for (char c : dictionary) {
            dictMap.put(c, new BigInteger("" + j++));
        }
        BigInteger bi = BigInteger.ZERO;
        BigInteger base = new BigInteger("" + dictionary.length);
        int exponent = 0;
        for (char c : chars2) {
            BigInteger a = dictMap.get(c);
            BigInteger b = base.pow(exponent).multiply(a);
            bi = bi.add(new BigInteger("" + b));
            exponent++;
        }
        return bi;
    }
}

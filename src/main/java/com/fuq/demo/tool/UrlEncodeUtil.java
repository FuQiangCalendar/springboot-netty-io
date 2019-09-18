/**
 * FileName: UrlEncodeUtil
 * Author:   Fang Tao Tao
 * Date:     2018/7/31 14:51
 * Description: 编码工具
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.fuq.demo.tool;

import java.io.UnsupportedEncodingException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈编码工具〉
 *
 * @author Fang Tao Tao
 * @create 2018/7/31
 * @since 1.0.0
 */
public class UrlEncodeUtil {

    private final static String ENCODE = "UTF-8";

    /**
     * URL 解码
     */
    public static String getURLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * URL 转码
     */
    public static String getURLEncoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLEncoder.encode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
}


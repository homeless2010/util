package com.step.esms.util.com.github.homeless2010.util;

import com.step.esms.util.com.github.homeless2010.util.NumberUtil;

public class StrUtil {
    public static boolean isNotBlank(CharSequence str) {
        return false == isBlank(str);
    }

    public static boolean isBlank(CharSequence str) {
        int length;

        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (false == NumberUtil.isBlankChar(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}

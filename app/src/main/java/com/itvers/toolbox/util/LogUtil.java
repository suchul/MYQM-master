package com.itvers.toolbox.util;

import android.util.Log;

/**
 * Created by itvers on 2018. 6. 07..
 */

public class LogUtil {
    private static boolean isEnable = false;    // 로그 사용 플래그 (true: 사용, false: 미사용)

    /**
     * 로그 사용 설정
     *
     * @param enable (true: 사용, false: 미사용)
     */
    public static void setEnableLog(boolean enable) {
        isEnable = enable;
    }

    /**
     * 로그 사용 여부
     *
     * @return (true : 사용, false : 미사용)
     */
    public static boolean getEnableLog() {
        return isEnable;
    }

    /**
     * Information
     *
     * @param tag 디버그 태그
     * @param msg 로그 메시지
     */
    public static void i(String tag, String msg) {
        if (isEnable && msg != null) Log.i(tag, msg);
    }

    /**
     * Debug
     *
     * @param tag 디버그 태그
     * @param msg 로그 메시지
     */
    public static void d(String tag, String msg) {
        if (isEnable && msg != null) Log.d(tag, msg);
    }

    /**
     * Warning
     *
     * @param tag 디버그 태그
     * @param msg 로그 메시지
     */
    public static void w(String tag, String msg) {
        if (isEnable && msg != null) Log.w(tag, msg);
    }

    /**
     * Error
     *
     * @param tag 디버그 태그
     * @param msg 로그 메시지
     */
    public static void e(String tag, String msg) {
        if (isEnable && msg != null) Log.e(tag, msg);
    }

    /**
     * Verbose
     *
     * @param tag 디버그 태그
     * @param msg 로그 메시지
     */
    public static void v(String tag, String msg) {
        if (isEnable && msg != null) Log.v(tag, msg);
    }
}
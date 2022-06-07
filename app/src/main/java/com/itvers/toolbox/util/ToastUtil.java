package com.itvers.toolbox.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by itvers on 2018. 6. 07..
 */

public class ToastUtil {
    public static final String TAG = ToastUtil.class.getSimpleName();
    private static volatile ToastUtil singletonInstance = null;   // 싱글턴 인스턴스
    private Toast toast;

    /**
     * 싱글턴 인스턴스
     *
     * @return instance
     */
    public static ToastUtil getInstance() {
        if (null == singletonInstance) {
            synchronized (ToastUtil.class) {
                if (null == singletonInstance) {
                    singletonInstance = new ToastUtil();
                }
            }
        }
        return singletonInstance;
    }


    /**
     * 토스트 메시지
     *
     * @param context
     * @param message
     * @param isLong
     */
    public void show(Context context, String message, boolean isLong) {
        // 토스트 메시지 중지
        stop();

        // 토스트 메시지 팝업 타임
        if (isLong) {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        }

        toast.show();
    }

    /**
     * 토스트 메시지 중지
     */
    public void stop() {
        if (null != toast) {
            toast.cancel();
            toast = null;
        }
    }
}
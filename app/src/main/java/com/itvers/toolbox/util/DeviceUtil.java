package com.itvers.toolbox.util;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

public class DeviceUtil {
    private final static String TAG = DeviceUtil.class.getSimpleName(); // 디버그 태그

    /**
     * 스마트폰 모델명
     * @return
     */
    public static String getDeviceModelName() {
        String modelNumber = Build.MODEL;
        if (modelNumber.startsWith("SM")) {
            return "Samsung";
        } else if (modelNumber.startsWith("LG")) {
            return "LG";
        } else if (modelNumber.startsWith("IM")) {
            return "Pantech";
        }
        return "Others";
    }


    /**
     * 배터리 잔량 체크
     *
     * @param activity
     * @return
     */
    public static int getDeviceBatteryLevel(Activity activity) {
        Intent intent = activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return ( int ) (level / ( float ) scale * 100);
    }
}

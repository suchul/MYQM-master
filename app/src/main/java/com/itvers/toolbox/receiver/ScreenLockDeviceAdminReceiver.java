package com.itvers.toolbox.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import com.itvers.toolbox.R;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ToastUtil;

public class ScreenLockDeviceAdminReceiver extends DeviceAdminReceiver {
    private final static String TAG = ScreenLockDeviceAdminReceiver.class.getSimpleName(); // 디버그 태그
    @Override
    public void onEnabled(Context context, Intent intent) {
        LogUtil.i(TAG, "onEnabled() -> Start !!!");
        super.onEnabled(context, intent);
        //ToastUtil.getInstance().show(context, context.getResources().getString(R.string.screen_unlock), false);

    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        LogUtil.i(TAG, "onDisabled() -> Start !!!");
        super.onDisabled(context, intent);
        //ToastUtil.getInstance().show(context, context.getResources().getString(R.string.screen_lock) , false);
    }
}

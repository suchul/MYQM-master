package com.itvers.toolbox.activity.main.hotkey;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.item.Key;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.PackageUtil;
import com.itvers.toolbox.util.PreferencesUtil;
import com.itvers.toolbox.util.StringUtil;

import java.util.HashMap;
import java.util.List;

public class HotKeyCLongActivity extends Activity {

    private final static String TAG = HotKeyCLongActivity.class.getSimpleName();   // 디버그 태그
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        int vendorCode = PreferencesUtil.getInstance(HotKeyCLongActivity.this).getVendorCode();
        LogUtil.d(TAG, "onCreate() -> vendorCode: " + vendorCode);

        if (vendorCode > 0
                && vendorCode != 3) { // TODO SUNING
            boolean success;
            switch(vendorCode) {
                case 1:
                    String tel = "tel:" + "027801185";
                    LogUtil.d(TAG, "onCreate() -> tel: " + tel);
                    startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                    break;
                case 2:
                    success = PackageUtil.executeApp(HotKeyCLongActivity.this, "com.kt.mykt");
                    LogUtil.d(TAG, "onCreate() -> executeApp: " + success);
                    break;
                case 3:
                    success = PackageUtil.executeApp(HotKeyCLongActivity.this, "com.taobao.litetao");
                    LogUtil.d(TAG, "onCreate() -> executeApp: " + success);
                    break;
                default:
                    break;
            }
        } else {
            HashMap<String, String> hashMap = PreferencesUtil.getInstance(HotKeyCLongActivity.this).getCkeyLong();
            if ((hashMap.containsKey(Definition.KEY_HOTKEY_TYPE))
                    && (hashMap.containsKey(Definition.KEY_HOTKEY_URL))
                    && (hashMap.containsKey(Definition.KEY_HOTKEY_APP_NAME))
                    && (hashMap.containsKey(Definition.KEY_HOTKEY_PACKAGE_NAME))
                    && (hashMap.containsKey(Definition.KEY_HOTKEY_PHONE))) {

                String type = hashMap.get(Definition.KEY_HOTKEY_TYPE);
                String url = hashMap.get(Definition.KEY_HOTKEY_URL);
                String appName = hashMap.get(Definition.KEY_HOTKEY_APP_NAME);
                String packageName = hashMap.get(Definition.KEY_HOTKEY_PACKAGE_NAME);
                String phone = hashMap.get(Definition.KEY_HOTKEY_PHONE);

                switch (Key.valueOf(type)) {
                    case WEB: {
                        if (StringUtil.isNotNull(url)) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            } catch (Exception e) {
                                LogUtil.i(TAG, "onCreate() -> Exception: " + e.getLocalizedMessage());
                            }
                        }
                    }
                    break;
                    case APP: {
                        if (StringUtil.isNotNull(packageName)
                                && StringUtil.isNotNull(appName)) {
                            boolean success = PackageUtil.executeApp(HotKeyCLongActivity.this, packageName);
                            LogUtil.d(TAG, "onCreate() -> executeApp: " + success);
                        }
                    }
                    break;
                    case PHONE: {
                        if (StringUtil.isNotNull(phone)) {
                            String tel = "tel:" + phone;
                            LogUtil.d(TAG, "onCreate() -> tel: " + tel);
                            startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                        }
                    }
                    break;
                    default:
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_APP_MAPS);
                        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                        if (!list.isEmpty()) {
                            for (ResolveInfo resolveInfo : list) {
                                String defaultMapsPackageName = resolveInfo.activityInfo.packageName;
                                LogUtil.d(TAG, "onCreate() -> defaultMapsPackageName: " + defaultMapsPackageName);
                                if (!defaultMapsPackageName.equalsIgnoreCase(getApplicationContext().getPackageName())) {
                                    boolean success = PackageUtil.executeApp(HotKeyCLongActivity.this, defaultMapsPackageName);
                                    LogUtil.d(TAG, "onCreate() -> executeApp: " + success);
                                }
                            }
                        }
                        break;
                }
            }
        }
        finish();
    }
}


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

public class HotKeyCDoubleActivity extends Activity {

    private final static String TAG = HotKeyCDoubleActivity.class.getSimpleName();   // 디버그 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        int vendorCode = PreferencesUtil.getInstance(HotKeyCDoubleActivity.this).getVendorCode();
        LogUtil.d(TAG, "onCreate() -> vendorCode: " + vendorCode);

        if (vendorCode > 0
                && vendorCode != 3) { // TODO SUNING
            String tel = "";
            switch(vendorCode) {
                case 1:
                    boolean success = PackageUtil.executeApp(HotKeyCDoubleActivity.this, "com.itvers.toolbox");
                    LogUtil.d(TAG, "onCreate() -> executeApp: " + success);
                    break;
                case 2:
                    tel = "tel:" + "0220707327";
                    LogUtil.d(TAG, "onCreate() -> tel: " + tel);
                    startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                    break;
                case 3:
                    tel = "tel:" + "02584418888";
                    LogUtil.d(TAG, "onCreate() -> tel: " + tel);
                    startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                    break;
                default:
                    break;
            }
        } else {
            HashMap<String, String> hashMap = PreferencesUtil.getInstance(HotKeyCDoubleActivity.this).getCkeyDouble();
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
                            boolean success = PackageUtil.executeApp(HotKeyCDoubleActivity.this, packageName);
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
                    default: {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
                        if (!list.isEmpty()) {
                            for (ResolveInfo resolveInfo : list) {
                                String defaultEmailPackageName = resolveInfo.activityInfo.packageName;
                                LogUtil.d(TAG, "onCreate() -> defaultEmailPackageName: " + defaultEmailPackageName);
                                if (!defaultEmailPackageName.equalsIgnoreCase(getApplicationContext().getPackageName())) {
                                    boolean success = PackageUtil.executeApp(HotKeyCDoubleActivity.this, defaultEmailPackageName);
                                    LogUtil.d(TAG, "onCreate() -> executeApp: " + success);
                                }
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
package com.itvers.toolbox.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.item.Key;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.PreferencesUtil;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends Activity {
    private final static String TAG = IntroActivity.class.getSimpleName();  // 디버그 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        setContentView(R.layout.activity_intro);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        // HOT KEY 초기화 설정 여부
        boolean isSetHotKey =  PreferencesUtil.getInstance(IntroActivity.this).getIsSetHotKey();
        LogUtil.d(TAG, "onCreate() >> isSetHotKey: " + isSetHotKey);

        // HOT KEY 초기화 설정이 안되어 있으면
        if (!isSetHotKey) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.NONE));
            hashMap.put(Definition.KEY_HOTKEY_URL, "");
            hashMap.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
            hashMap.put(Definition.KEY_HOTKEY_APP_NAME, "");
            hashMap.put(Definition.KEY_HOTKEY_PHONE, "");

            // C Key
            PreferencesUtil.getInstance(IntroActivity.this).setBkeyDouble(hashMap);
            PreferencesUtil.getInstance(IntroActivity.this).setBkeyLong(hashMap);
            PreferencesUtil.getInstance(IntroActivity.this).setCkeyDouble(hashMap);
            PreferencesUtil.getInstance(IntroActivity.this).setCkeyLong(hashMap);

            // HOT KEY 초기화 설정
            PreferencesUtil.getInstance(IntroActivity.this).setIsSetHotKey(true);
        }

        // VENDOR 코드 버전
        int vendorCodeVersion =  PreferencesUtil.getInstance(IntroActivity.this).getVendorCodeVersion();
        LogUtil.d(TAG, "onCreate() >> vendorCodeVersion: " + vendorCodeVersion);

        if (vendorCodeVersion < Definition.VENDOR_CODE_VERSION) {
            HashMap<Integer, String> vendorCodes = new HashMap<>();
            vendorCodes.put(0, " NONE ");
            vendorCodes.put(1, "ITVERS");
            vendorCodes.put(2, "KT M&S");
            vendorCodes.put(3, "SUNING");
            PreferencesUtil.getInstance(IntroActivity.this).setVendorCodes(vendorCodes);
            PreferencesUtil.getInstance(IntroActivity.this).setVendorCodeVersion(Definition.VENDOR_CODE_VERSION);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                goToHomeActivity();
            }
        }, 2000);
    }

    /**
     * 홈엑티비티로 화면 이동
     */
    private void goToHomeActivity() {
        Intent intent = new Intent(IntroActivity.this, HomeActivity.class);
        startActivity(intent);

        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        return super.onKeyDown(keyCode, event);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

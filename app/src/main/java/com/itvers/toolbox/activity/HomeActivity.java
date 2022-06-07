package com.itvers.toolbox.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.itvers.toolbox.R;
import com.itvers.toolbox.activity.main.MainActivity;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.RequestCode;
import com.itvers.toolbox.dialog.Dialog;
import com.itvers.toolbox.util.IntentUtil;
import com.itvers.toolbox.util.LogUtil;

import java.util.HashMap;

public class HomeActivity extends Activity implements View.OnClickListener {
    private final static String TAG = HomeActivity.class.getSimpleName();   // 디버그 태그

    /**
     * 엑티비티 결과
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() -> "
                + "requestCode : " + requestCode
                + ", resultCode : " + resultCode
                + ", data : " + data);
        switch (requestCode) {
            case RequestCode.REQUEST_PERMISSION_ALL:
                // 퍼미션 요청
                if (checkPermissionsAll(HomeActivity.this)) {
                    // 클릭 리스너 등록
                    setOnClickListener();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 퍼미션 결과
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        LogUtil.i(TAG, "onRequestPermissionsResult() -> Start !!!");
        boolean granted = true;
        switch (requestCode) {
            case RequestCode.PERMISSION_REQUEST_ALL: {
                int length = permissions.length;
                for (int i = 0; i < length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        granted = false;
                        break;
                    }
                }

                if (granted) {
                    // 클릭 리스너 등록
                    setOnClickListener();
                } else {
                    Dialog.getInstance().showDual(
                            this,
                            getResources().getString(R.string.notice),
                            getResources().getString(R.string.dialog_permission_description),
                            getResources().getString(R.string.setting),
                            getResources().getString(R.string.close),
                            false,
                            new Dialog.DialogOnClickListener() {
                                @Override
                                public void OnItemClickResult(HashMap<String, Object> hashMap) {
                                    int result = (int) hashMap.get(Definition.KEY_DIALOG_DUAL);
                                    if (result == Definition.DIALOG_BUTTON_POSITIVE) {
                                        // 설정으로 이동
                                        IntentUtil.intentSetting(HomeActivity.this, RequestCode.REQUEST_PERMISSION_ALL);
                                    }
                                }
                            });
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        // 퍼미션 요청
        if (checkPermissionsAll(HomeActivity.this)) {
            // 클릭 리스너 등록
            setOnClickListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume() -> Start !!!");
    }

    /**
     * 클릭 리스너 등록
     */
    private void setOnClickListener() {
        LogUtil.i(TAG, "setOnClickListener() -> Start !!!");
//        findViewById(R.id.activity_home_btn_patch).setOnClickListener(this);
//        findViewById(R.id.activity_home_btn_mouse).setOnClickListener(this);
//        findViewById(R.id.activity_home_btn_buttons).setOnClickListener(this);


        // TODO MOUSE 사용 안하므로 PATCH로 바로 이동
        Intent intent = new Intent(HomeActivity.this, SelectDeviceActivity.class);
//        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.putExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
        startActivity(intent);

        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        intent = new Intent(HomeActivity.this, MainActivity.class);
        switch (v.getId()) {
            // PATCH
            case R.id.activity_home_btn_patch:
                intent.putExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
                break;
            // MOUSE
            case R.id.activity_home_btn_mouse:
                intent.putExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_MOUSE);
                break;
            // BUTTONS
            case R.id.activity_home_btn_buttons:
                intent.putExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_BUTTONS);
                break;
            // DEFAULT
            default:
                break;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
    }

    /**
     * 퍼미션 체크
     *
     * @param activity
     */
    private boolean checkPermissionsAll(Activity activity) {
        LogUtil.i(TAG, "checkPermissions() -> Start !!!");
        boolean granted = true;
        // 퍼미션 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 마시멜로우 (Ver.6.0)
            for (int i = 0; i < Definition.PERMISSIONS.length; i++) {
                if (ContextCompat.checkSelfPermission(activity, Definition.PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            // 퍼미션 요청
            if (!granted) {
                ActivityCompat.requestPermissions(activity, Definition.PERMISSIONS, RequestCode.PERMISSION_REQUEST_ALL);
            }
        }
        return granted;
    }
}

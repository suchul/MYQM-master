package com.itvers.toolbox.activity.main.admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.itvers.toolbox.R;
import com.itvers.toolbox.activity.main.FirmwareActivity;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.RequestCode;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ToastUtil;

public class PasswordActivity extends Activity {
    private final static String TAG = PasswordActivity.class.getSimpleName();   // 디버그 태그

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() -> "
                + "requestCode : " + requestCode
                + ", resultCode : " + resultCode
                + ", data : " + data);
        switch (requestCode) {
            case RequestCode.ACTIVITY_REQUEST_CODE_USER:
                if (resultCode == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        // 뒤로가기 버튼
        findViewById(R.id.activity_password_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
        });

        // 다음 버튼
        findViewById(R.id.activity_password_tv_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideKeypad(PasswordActivity.this, ((EditText) findViewById(R.id.activity_password_et_vendorcode)));
                    }
                });

                String password = ((EditText) findViewById(R.id.activity_password_et_vendorcode)).getText().toString().trim();
                LogUtil.e(TAG, "onClick() -> password: " + password);
                if (Definition.VENDORCODE_PASSWORD.equals(password)) {
                    LogUtil.e(TAG, "onClick() -> VendorCode password!! Jump to  UserActivity - PW : " + password);
                    Intent intent = new Intent(PasswordActivity.this, UserActivity.class);
                    startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_USER);
                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                } else if (Definition.UPGRADE_PASSWORD.equals(password)) {
                    LogUtil.e(TAG, "onClick() -> EmDownload password!! Jump to  FirmwareActivity - PW : " + password);
                    Intent intent = new Intent(PasswordActivity.this, FirmwareActivity.class);
                    intent.putExtra(Definition.KEY_IS_EMERGENCY, true);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                } else {
                    ToastUtil
                            .getInstance()
                            .show(PasswordActivity.this,
                                    getResources().getString(R.string.invalid_password),
                                    false);
                }
            }
        });
    }

    /**
     * 키보드 내리기
     *
     * @param editText
     */
    protected void hideKeypad(Context context, final EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }
}


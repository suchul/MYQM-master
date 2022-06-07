package com.itvers.toolbox.activity.main.hotkey;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.util.IntentUtil;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ToastUtil;

public class HotKeyPatchActivity extends Activity {

    private final static String TAG = HotKeyPatchActivity.class.getSimpleName();   // 디버그 태그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotkey_patch);

        // 뒤로가기 버튼
        findViewById(R.id.activity_hotkey_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
        });

        // Q Mouse App이 설치되어 있으면,
        if (IntentUtil.isInstalledApp(HotKeyPatchActivity.this, Definition.PACKEGE_FUNKEY) != null) {
            ((TextView) findViewById(R.id.activity_hotkey_tv_download)).setText(getResources().getString(R.string.hotkey_app_execute));
        }

        // 다운로드 버튼
        findViewById(R.id.activity_hotkey_tv_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "onClick() -> VERSION.SDK_INT : " + Build.VERSION.SDK_INT);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // 롤리팝 (Ver.5.0, API21)
                    ToastUtil.getInstance().show(HotKeyPatchActivity.this, getResources().getString(R.string.os_version_warning), false);
                    return;
                }

                if (IntentUtil.isInstalledApp(HotKeyPatchActivity.this, Definition.PACKEGE_FUNKEY) != null) {
                    IntentUtil.executeApp(HotKeyPatchActivity.this, Definition.PACKEGE_FUNKEY);
                } else {
                    // FUNKEY Google Market 이동
                    IntentUtil.gotoGoogleMarket(HotKeyPatchActivity.this, Definition.PACKEGE_FUNKEY);
                }
            }
        });
    }

    // 백버튼
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }
}
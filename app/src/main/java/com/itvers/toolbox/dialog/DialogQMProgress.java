package com.itvers.toolbox.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.itvers.toolbox.R;
import com.itvers.toolbox.util.StringUtil;

public class DialogQMProgress extends Dialog {
    public DialogQMProgress(Activity activity, String msg, boolean isGoneMent) {
        super(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 다이얼 로그 제목
        setContentView(R.layout.qm_progress_bar);       // 다이얼로그 보여줄 레이아웃
        setCancelable(false);

        if (isGoneMent) {
            findViewById(R.id.qm_progress_bar_tv_description).setVisibility(View.GONE);
        } else {
            if (StringUtil.isNotNull(msg)) {
                ((TextView ) findViewById(R.id.qm_progress_bar_tv_description)).setText(msg);
            }
        }
    }
}

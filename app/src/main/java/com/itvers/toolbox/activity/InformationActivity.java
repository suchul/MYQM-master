package com.itvers.toolbox.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.itvers.toolbox.App;
import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.GooglePlayVersion;
import com.itvers.toolbox.dialog.Dialog;
import com.itvers.toolbox.dialog.DialogQMProgress;
import com.itvers.toolbox.util.IntentUtil;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.StringUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class InformationActivity extends Activity {
    private final static String TAG = InformationActivity.class.getSimpleName(); // 디버그 태그

    private DialogQMProgress dialogQMProgress;                                  // 프로그레스 다이얼로그
    private String appVersion;                                                  // 앱 버전
    private String googlePlayAppVersion;                                        // 구글플레이 앱 버전

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        // 프로그레스 다이얼로그 시작
        showProgress();

        // 뒤로가기 버튼
        findViewById(R.id.activity_information_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
        });

        findViewById(R.id.activity_information_tv_goto_market).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentUtil.intentGoToGooglePlay(InformationActivity.this, getPackageName());
            }
        });

        // 앱 버전
        appVersion = IntentUtil.getAppVersion(InformationActivity.this);
        if (StringUtil.isNotNull(appVersion)) {
            LogUtil.d(TAG, "onCreate() -> appVersion : " + appVersion);
            ((TextView) findViewById(R.id.activity_information_tv_current_version)).setText(
                    getResources().getString(R.string.ver) + appVersion);
        }

        // 프로그레스 다이얼로그 중지
        dismissProgress();

    }

    /**
     * 프로그레스 다이얼로그 시작
     */
    private void showProgress() {
        LogUtil.i(TAG, "showProgress() -> Start !!!");

        // 프로그레스 다이얼로그 종료
        boolean success = dismissProgress();
        LogUtil.d(TAG, "showProgress() -> success : " + success);

        dialogQMProgress = new DialogQMProgress(this, null, true);
        dialogQMProgress.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        if (null != dialogQMProgress) dialogQMProgress.show();
    }

    /**
     * 프로그레스 다이얼로그 중지
     */
    private boolean dismissProgress() {
        LogUtil.i(TAG, "dismissProgress() -> Start !!!");

        if ((dialogQMProgress != null)
                && dialogQMProgress.isShowing()) {
            dialogQMProgress.dismiss();
            dialogQMProgress = null;
        }
        return (dialogQMProgress == null);
    }

    private class GooglePlayVersionTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... string) {

            String newVersion = null;

            try {
                Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + string[0]  + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                if (document != null) {
                    Elements element = document.getElementsContainingOwnText("Current Version");
                    for (Element ele : element) {
                        if (ele.siblingElements() != null) {
                            Elements sibElemets = ele.siblingElements();
                            for (Element sibElemet : sibElemets) {
                                newVersion = sibElemet.text();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LogUtil.e(TAG, "GooglePlayVersionTask >> IOException: " + e.getLocalizedMessage());
            }
            return newVersion;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            LogUtil.d(TAG, "onPostExecute() >> result : " + result);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy() -> Start !!!");

        // 프로그레스 다이얼로그 중지
        dismissProgress();
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
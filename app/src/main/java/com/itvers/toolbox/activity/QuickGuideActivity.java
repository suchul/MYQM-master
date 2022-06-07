package com.itvers.toolbox.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;
import com.itvers.toolbox.R;
import com.itvers.toolbox.util.LogUtil;

import java.util.Locale;

public class QuickGuideActivity extends Activity {
    private final static String TAG = QuickGuideActivity.class.getSimpleName();  // 디버그 태그

    PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_guide);
        LogUtil.i(TAG, "onCreate() >> Start !!!");

        String fileName = "Smart Patch Manual_20200116_en.pdf";
        String coutry = Locale.getDefault().getCountry();
        LogUtil.i(TAG, "onCreate() >> coutry: " + coutry);

        if (coutry.equalsIgnoreCase("KR")) {
            fileName = "Smart Patch Manual_20200116_kr.pdf";
        } else if (coutry.equalsIgnoreCase("JP")) {
            fileName = "Smart Patch Manual_20200116_jp.pdf";
        } else if (coutry.equalsIgnoreCase("CN")) {
            fileName = "Smart Patch Manual_20200116_cn.pdf";
        }

        pdfView = findViewById(R.id.activity_quick_guide_view_pdf);
        pdfView.fromAsset(fileName).load();

        // 뒤로가기 버튼
        findViewById(R.id.activity_quick_guide_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
            }
        });
    }
}

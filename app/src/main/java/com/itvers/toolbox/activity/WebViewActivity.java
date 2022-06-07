package com.itvers.toolbox.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.util.LogUtil;

public class WebViewActivity extends AppCompatActivity {
    private final static String TAG = WebViewActivity.class.getSimpleName();    // 디버그 태그
    private WebView webView;                                                    // 웹뷰
    private String url;                                                         // URL

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        Intent intent = getIntent();
        url = intent.getStringExtra(Definition.KEY_INTENT_URL);
        LogUtil.d(TAG, "onCreate() -> url : " + url);

        webView = findViewById(R.id.activity_webview_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClientClass());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (url == Definition.URL_YOUTUBE_ROBOSTICK) {
//            if ((keyCode == KeyEvent.KEYCODE_BACK)
//                    && webView.canGoBack()) {
//                webView.goBack();
//                return true;
//            }
//        } else {
            finish();
//        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}

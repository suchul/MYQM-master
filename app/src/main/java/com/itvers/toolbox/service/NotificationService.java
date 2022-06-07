package com.itvers.toolbox.service;

import android.app.Activity;
import android.os.Bundle;

import com.itvers.toolbox.util.LogUtil;

public class NotificationService extends Activity {
	private final static String TAG = NotificationService.class.getSimpleName(); // 디버그 태그
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.i(TAG, "onCreate() -> Start !!!");
	}
}
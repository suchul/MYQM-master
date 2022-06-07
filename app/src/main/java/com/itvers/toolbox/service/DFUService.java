package com.itvers.toolbox.service;

import android.app.Activity;

import no.nordicsemi.android.dfu.BuildConfig;
import no.nordicsemi.android.dfu.DfuBaseService;

public class DFUService extends DfuBaseService {

	@Override
	protected Class<? extends Activity> getNotificationTarget() {
		return NotificationService.class;
	}

	@Override
	protected boolean isDebug() { return BuildConfig.DEBUG; }
}

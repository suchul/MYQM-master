package com.itvers.toolbox.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

public class IntentUtil {
    private final static String TAG = IntentUtil.class.getSimpleName(); // 디버그 태그

    /**
     * 설정으로 이동
     *
     * @param activity
     */
    public final static void intentSetting(Activity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 구글플레이 이동
     *
     * @param activity
     */
    public final static void intentGoToGooglePlay(Activity activity, String packageName) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

    /**
     * 앱 설치 여부
     *
     * @param packageName
     */
    public final static Intent isInstalledApp(Activity activity, String packageName) {
        return activity.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    /**
     * 앱 실행
     *
     * @param packageName
     */
    public final static void executeApp(Activity activity, String packageName) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    /**
     * 구글 마켓 이동
     *
     * @param packageName
     */
    public final static void gotoGoogleMarket(Activity activity, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        activity.startActivity(intent);
    }

    /**
     * 서비스 실행 여부
     *
     * @return
     */
    public final static boolean isRunningService(Activity activity, String serviceName) {
        ActivityManager activityManager = ( ActivityManager ) activity.getSystemService(activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 앱 버전
     *
     * @param activity
     * @return
     */
    public final static String getAppVersion(Activity activity) {
        LogUtil.i(TAG, "getAppVersion() -> Start !!!");
        String versionName = null;
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(TAG, "getAppVersion() -> NameNotFoundException : " + e.getLocalizedMessage());
        }
        return versionName;
    }
}

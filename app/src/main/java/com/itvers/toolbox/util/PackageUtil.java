package com.itvers.toolbox.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.itvers.toolbox.item.ItemPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PackageUtil {
    public static final String TAG = PackageUtil.class.getSimpleName();

    /**
     * 설치된 앱리스트
     *
     * @param context
     * @return
     */
    public static ArrayList<ItemPackage> getInstalledPackageList(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        ArrayList<ItemPackage> packageList = new ArrayList<>();
        List<ResolveInfo> AppInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : AppInfos) {
            ActivityInfo ai = info.activityInfo;

            String appTitle = ai.loadLabel(packageManager).toString();
            String packageName = ai.packageName;
            if (packageName.equals("com.google.android.apps.googleassistant")) {

                String appName = ai.name;
                LogUtil.i(TAG, "App Title: " + appTitle);
                LogUtil.i(TAG, "App Package Nam: " + packageName);
                LogUtil.i(TAG, "App Class Name" + appName);

                ItemPackage item = new ItemPackage();
                item.setAppTitle(appTitle);
                item.setPackageName(packageName);
                item.setAppName(appName);

                try {
                    Drawable icon = packageManager.getApplicationIcon(packageName);
                    LogUtil.i(TAG, "App Icon" + icon);
                    item.setIcon(icon);
                } catch (PackageManager.NameNotFoundException nnfe) {
                    LogUtil.d(TAG, "executeApp() -> getInstalledPackageList: " + nnfe.getLocalizedMessage());
                }
                packageList.add(item);
            } else {
                int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
                if ((ai.flags & mask) == 0) {
                    String appName = ai.name;
                    LogUtil.i(TAG, "App Title: " + appTitle);
                    LogUtil.i(TAG, "App Package Nam: " + packageName);
                    LogUtil.i(TAG, "App Class Name" + appName);

                    ItemPackage item = new ItemPackage();
                    item.setAppTitle(appTitle);
                    item.setPackageName(packageName);
                    item.setAppName(appName);

                    try {
                        Drawable icon = packageManager.getApplicationIcon(packageName);
                        LogUtil.i(TAG, "App Icon" + icon);
                        item.setIcon(icon);
                    } catch (PackageManager.NameNotFoundException nnfe) {
                        LogUtil.d(TAG, "getInstalledPackageList() -> getInstalledPackageList: " + nnfe.getLocalizedMessage());
                    }
                    packageList.add(item);
                }
            }
        }

        Collections.sort(packageList, new Comparator<ItemPackage>() {
            @Override
            public int compare(ItemPackage item1, ItemPackage item2) {
                return item1.getAppTitle().compareTo(item2.getAppTitle());
            }
        });
        return packageList;
    }

    /**
     * 패키지명으로 앱실행
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean executeApp(Context context, String packageName) {
        LogUtil.d(TAG, "executeApp() -> packageName: " + packageName);

        PackageManager packageManager = context.getPackageManager();
        try {
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            if (intent == null) {
                throw new PackageManager.NameNotFoundException();
            }
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(intent);
            return true;
        } catch (PackageManager.NameNotFoundException nnfe) {
            nnfe.printStackTrace();
            return false;
        } catch (Exception e) {
            LogUtil.e(TAG, "executeApp() -> Exception: " + e.getLocalizedMessage());
            return false;
        }
    }
}

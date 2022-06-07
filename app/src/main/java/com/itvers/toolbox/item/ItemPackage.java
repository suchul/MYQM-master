package com.itvers.toolbox.item;

import android.graphics.drawable.Drawable;

public class ItemPackage {

    String appTitle = "";
    String packageName = "";
    String appName = "";
    Drawable icon = null;

    public String getAppTitle() {
        return appTitle;
    }
    public String getPackageName() {
        return packageName;
    }
    public String getAppName() {
        return appName;
    }
    public Drawable getIcon() {
        return icon;
    }

    public void setAppTitle(String appTitle) { this.appTitle = appTitle; }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}

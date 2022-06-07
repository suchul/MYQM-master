package com.itvers.toolbox.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itvers.toolbox.common.Definition;

import java.lang.reflect.Type;
import java.util.HashMap;

import static com.itvers.toolbox.common.Definition.PREFERENCES_NAME;

public class PreferencesUtil {
    private static final String TAG = PreferencesUtil.class.getSimpleName();    // 디버그 태그
    private static volatile PreferencesUtil singletonInstance = null;           // 싱글턴 인스턴스
    private Context context;                                                    // 컨텍스트
    private SharedPreferences sharedPreferences;                                // 사용자 살정

    /**
     * 싱글턴 인스턴스
     *
     * @return instance
     */
    public static PreferencesUtil getInstance(Context context) {
        if (null == singletonInstance) {
            synchronized (PreferencesUtil.class) {
                if (null == singletonInstance) {
                    singletonInstance = new PreferencesUtil(context);
                }
            }
        }
        return singletonInstance;
    }

    private PreferencesUtil(Context context) {
        this.context = context;
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        sharedPreferences = context.getSharedPreferences( PREFERENCES_NAME, Context.MODE_PRIVATE);

    }

    /**
     * 펌웨어 버전 저장
     *
     * @param firmwareVersion
     */
    public boolean setFirmwareVersion(String firmwareVersion) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_FIRMWARE_VERSION, firmwareVersion);
        return editor.commit();
    }

    /**
     * 펌웨어
     */
    public String getFirmwareVersion() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_FIRMWARE_VERSION, null);
    }

    /**
     * 펌웨어 버전 초기화
     */
    public boolean clearFirmwareVersion() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Definition.PREFERENCES_KEY_FIRMWARE_VERSION);
        return editor.commit();
    }

    /**
     * HOT KEY1 (MESSAGING) URL 저장
     *
     * @param url
     */
    public boolean setHotKeyMessagingUrl(String url) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_MESSAGING_URL, url);
        return editor.commit();
    }

    /**
     * HOT KEY1 (MESSAGING) URL
     */
    public String getHotKeyMessagingUrl() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_MESSAGING_URL, "");
    }

    /**
     * HOT KEY3 (EMAIL) URL 저장
     *
     * @param url
     */
    public boolean setHotKeyEmailUrl(String url) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_EMAIL_IS_APP, url);
        return editor.commit();
    }

    /**
     * HOT KEY3 (EMAIL) URL
     */
    public String getHotKeyEmailUrl() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_EMAIL_IS_APP, "");
    }

    /**
     * HOT KEY2 (MAP) URL 저장
     *
     * @param url
     */
    public boolean setHotKeyMapUrl(String url) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_MAP_URL, url);
        return editor.commit();
    }

    /**
     * HOT KEY2 (MAP) URL
     */
    public String getHotKeyMapUrl() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_MAP_URL, "");
    }

    /**
     * HOT KEY1 (MESSAGING) APP NAME 저장
     *
     * @param appName
     */
    public boolean setHotKeyMessagingAppName(String appName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_MESSAGING_APP_NAME, appName);
        return editor.commit();
    }

    /**
     * HOT KEY1 (MESSAGING) APP NAME
     */
    public String getHotKeyMessagingAppName() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_MESSAGING_APP_NAME, "");
    }

    /**
     * HOT KEY3 (EMAIL) APP NAME 저장
     *
     * @param appName
     */
    public boolean setHotKeyEmailAppName(String appName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_EMAIL_APP_NAME, appName);
        return editor.commit();
    }

    /**
     * HOT KEY3 (EMAIL) APP NAME
     */
    public String getHotKeyEmailAppName() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_EMAIL_APP_NAME, "");
    }

    /**
     * HOT KEY2 (MAP) APP NAME 저장
     *
     * @param appName
     */
    public boolean setHotKeyMapAppName(String appName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_MAP_APP_NAME, appName);
        return editor.commit();
    }

    /**
     * HOT KEY2 (MAP) APP NAME
     */
    public String getHotKeyMapAppName() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_MAP_APP_NAME, "");
    }

    /**
     * HOT KEY1 (MESSAGING) PACKAGE NAME 저장
     *
     * @param packageName
     */
    public boolean setHotKeyMessagingPackageName(String packageName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_MESSAGING_PACKAG_NAME, packageName);
        return editor.commit();
    }

    /**
     * HOT KEY1 (MESSAGING) PACKAGE NAME
     */
    public String getHotKeyMessagingPackageName() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_MESSAGING_PACKAG_NAME, "");
    }

    /**
     * HOT KEY2 (EMAIL) PACKAGE NAME 저장
     *
     * @param packageName
     */
    public boolean setHotKeyEmailPackageName(String packageName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_EMAIL_PACKAG_NAME, packageName);
        return editor.commit();
    }

    /**
     * HOT KEY2 (EMAIL) PACKAGE NAME
     */
    public String getHotKeyEmailPackageName() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_EMAIL_PACKAG_NAME, "");
    }

    /**
     * HOT KEY2 (MAP) PACKAGE NAME 저장
     *
     * @param packageName
     */
    public boolean setHotKeyMapPackageName(String packageName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Definition.PREFERENCES_KEY_HOT_KEY_MAP_PACKAG_NAME, packageName);
        return editor.commit();
    }

    /**
     * HOT KEY2 (MAP) PACKAGE NAME
     */
    public String getHotKeyMapPackageName() {
        return sharedPreferences.getString(Definition.PREFERENCES_KEY_HOT_KEY_MAP_PACKAG_NAME, "");
    }

    /**
     * HOT KEY1 (MESSAGING) APP 여부 저장
     *
     * @param isApp
     */
    public boolean setIsHotKeyMessagingApp(boolean isApp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Definition.PREFERENCES_KEY_HOT_KEY_MESSAGING_IS_APP, isApp);
        return editor.commit();
    }

    /**
     * HOT KEY1 (MESSAGING) APP 여부
     */
    public boolean getIsHotKeyMessagingApp() {
        return sharedPreferences.getBoolean(Definition.PREFERENCES_KEY_HOT_KEY_MESSAGING_IS_APP, false);
    }

    /**
     * HOT KEY2 (EMAIL) APP 여부 저장
     *
     * @param isApp
     */
    public boolean setIsHotKeyEmailApp(boolean isApp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Definition.PREFERENCES_KEY_HOT_KEY_EMAIL_IS_APP, isApp);
        return editor.commit();
    }

    /**
     * HOT KEY2 (EMAIL) APP 여부
     */
    public boolean getIsHotKeyEmailApp() {
        return sharedPreferences.getBoolean(Definition.PREFERENCES_KEY_HOT_KEY_EMAIL_IS_APP, false);
    }

    /**
     * HOT KEY2 (MAP) APP 여부 저장
     *
     * @param isApp
     */
    public boolean setIsHotKeyMapApp(boolean isApp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Definition.PREFERENCES_KEY_HOT_KEY_MAP_IS_APP, isApp);
        return editor.commit();
    }

    /**
     * HOT KEY3 (MAP) APP 여부
     */
    public boolean getIsHotKeyMapApp() {
        return sharedPreferences.getBoolean(Definition.PREFERENCES_KEY_HOT_KEY_MAP_IS_APP, false);
    }

    /**
     * VENDOR CODE 저장
     * @param object    HashMap<Integer, String>
     */
    public void setVendorCodes(Object object) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        editor.putString(Definition.PREFERENCES_KEY_VENDOR_CODES, json);
        editor.apply();
    }

    /**
     *  VENDOR CODE
     */
    public HashMap<Integer, String> getVendorCodes() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Definition.PREFERENCES_KEY_VENDOR_CODES,"");
        Type type = new TypeToken<HashMap<Integer, String>>(){}.getType();
        HashMap<Integer, String> vendorCodes = gson.fromJson(json, type);
        return vendorCodes;
    }

    /**
     * VENDOR CODE 버전 저장
     *
     * @param versionCode
     */
    public boolean setVendorCodeVersion(int versionCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Definition.PREFERENCES_KEY_VENDOR_CODE_VERSION, versionCode);
        return editor.commit();
    }

    /**
     * VENDOR CODE 버전
     */
    public int getVendorCodeVersion() {
        return sharedPreferences.getInt(Definition.PREFERENCES_KEY_VENDOR_CODE_VERSION, 0);
    }

    /**
     * VENDOR CODE 저장
     *
     * @param vendorCode
     */
    public boolean setVendorCode(int vendorCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Definition.PREFERENCES_KEY_VENDOR_CODE, vendorCode);
        return editor.commit();
    }

    /**
     * VENDOR CODE
     */
    public int getVendorCode() {
        return sharedPreferences.getInt(Definition.PREFERENCES_KEY_VENDOR_CODE, 0);
    }

//    /**
//     * B Key - Short 저장
//     *
//     * @param object HashMap<String, String>
//     */
//    public void setBkeyShort(Object object) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(object);
//        editor.putString(Definition.PREFERENCES_KEY_B_KEY_SHORT, json);
//        editor.apply();
//    }
//
//    /**
//     * B Key - Short
//     */
//    public HashMap<String, String> getBkeyShort() {
//        Gson gson = new Gson();
//        String json = sharedPreferences.getString(Definition.PREFERENCES_KEY_B_KEY_SHORT, "");
//        Type type = new TypeToken<HashMap<String, String>>() {
//        }.getType();
//        HashMap<String, String> bKeyShort = gson.fromJson(json, type);
//        return bKeyShort;
//    }
//
//    /**
//     * B Key - Double 저장
//     *
//     * @param object HashMap<String, String>
//     */
//    public void setBkeyDouble(Object object) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(object);
//        editor.putString(Definition.PREFERENCES_KEY_B_KEY_DOUBLE, json);
//        editor.apply();
//    }
//
//    /**
//     * B Key - Double
//     */
//    public HashMap<String, String> getBkeyDouble() {
//        Gson gson = new Gson();
//        String json = sharedPreferences.getString(Definition.PREFERENCES_KEY_B_KEY_DOUBLE, "");
//        Type type = new TypeToken<HashMap<String, String>>() {
//        }.getType();
//        HashMap<String, String> bKeyDouble = gson.fromJson(json, type);
//        return bKeyDouble;
//    }

    /**
     * B Key - Double 저장
     *
     * @param object HashMap<String, String>
     */
    public void setBkeyDouble(Object object) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        editor.putString(Definition.PREFERENCES_KEY_B_KEY_DOUBLE, json);
        editor.apply();
    }

    /**
     * B Key - Double
     */
    public HashMap<String, String> getBkeyDouble() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Definition.PREFERENCES_KEY_B_KEY_DOUBLE, "");
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> bKeyDouble = gson.fromJson(json, type);
        return bKeyDouble;
    }

    /**
     * B Key - Long 저장
     *
     * @param object HashMap<String, String>
     */
    public void setBkeyLong(Object object) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        editor.putString(Definition.PREFERENCES_KEY_B_KEY_LONG, json);
        editor.apply();
    }

    /**
     * B Key - Long
     */
    public HashMap<String, String> getBkeyLong() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Definition.PREFERENCES_KEY_B_KEY_LONG, "");
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> bKeyLong = gson.fromJson(json, type);
        return bKeyLong;
    }

    /**
     * C Key - Double 저장
     *
     * @param object HashMap<String, String>
     */
    public void setCkeyDouble(Object object) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        editor.putString(Definition.PREFERENCES_KEY_C_KEY_DOUBLE, json);
        editor.apply();
    }

    /**
     * C Key - Double
     */
    public HashMap<String, String> getCkeyDouble() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Definition.PREFERENCES_KEY_C_KEY_DOUBLE, "");
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> cKeyDouble = gson.fromJson(json, type);
        return cKeyDouble;
    }

    /**
     * C Key - Long 저장
     *
     * @param object HashMap<String, String>
     */
    public void setCkeyLong(Object object) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(object);
        editor.putString(Definition.PREFERENCES_KEY_C_KEY_LONG, json);
        editor.apply();
    }

    /**
     * C Key - Long
     */
    public HashMap<String, String> getCkeyLong() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(Definition.PREFERENCES_KEY_C_KEY_LONG, "");
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> cKeyLong = gson.fromJson(json, type);
        return cKeyLong;
    }

    /**
     * HOT KEY 설정 여부 저장
     *
     * @param isSetHotKey
     */
    public boolean setIsSetHotKey(boolean isSetHotKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Definition.PREFERENCES_KEY_IS_SET_HOT_KEY, isSetHotKey);
        return editor.commit();
    }

    /**
     * HOT KEY 설정 여부
     */
    public boolean getIsSetHotKey() {
        return sharedPreferences.getBoolean(Definition.PREFERENCES_KEY_IS_SET_HOT_KEY, false);
    }
}

package com.itvers.toolbox.util;

import com.itvers.toolbox.common.Definition;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class ParserUtil {
    public static final String TAG = ParserUtil.class.getSimpleName();

    /**
     * Product Model
     *
     * @param productModel
     * @return
     */
    public static int getProductModel(String[] productModel) {
        String temp = "";
        for (String string : productModel) {
            temp += string;
        }
        LogUtil.d(TAG, "getProductModel() -> temp : " + temp);

        if (("001").equals(temp)) {
            return Definition.PMOD_SMART_PATCH;
        } else if (("010").equals(productModel)) {
            return Definition.PMOD_ROBOSTICK;
        }
        return Definition.PMOD_SLIM_MOUSE;
    }

    /**
     * Product Model
     *
     * @param productModel
     * @return
     */
    public static String[] getProductModel(int productModel) {
        switch (productModel) {
            case Definition.PMOD_SLIM_MOUSE:
                return new String[]{"0", "0", "0"};
            case Definition.PMOD_SMART_PATCH:
                return new String[]{"0", "0", "1"};
            case Definition.PMOD_ROBOSTICK:
                return new String[]{"0", "1", "0"};
            default:
                return new String[]{"1", "1", "1"};
        }
    }

    /**
     * Product Model
     *
     * @param productModel
     * @return
     */
    public static String[] getProductModel(String productModel) {
        char[] charArray = productModel.toCharArray();
        int count = charArray.length;
        String[] array = new String[3];
        for (int i = 0; i < count; i++) {
            array[i] = Character.toString(charArray[i]);
        }
        return array;
    }

    /**
     * Mouse Mode
     *
     * @param mouseMode
     * @return
     */
    public static int getMouseMode(String mouseMode) {
        if (("1").equals(mouseMode)) {
            return Definition.MOUSE_WHEEL;
        } else {
            return Definition.MOUSE_CURSOR;
        }
    }

    /**
     * Mouse Mode
     *
     * @param mouseMode
     * @return
     */
    public static String getMouseMode(int mouseMode) {
        switch (mouseMode) {
            case Definition.MOUSE_WHEEL:
                return "1";
            default:
                return "0";
        }
    }


    /**
     * Target OS
     *
     * @param targetOS
     * @return
     */
    public static int getTargetOS(String[] targetOS) {
        String temp = "";
        for (String string : targetOS) {
            temp += string;
        }
        LogUtil.d(TAG, "getTargetOS() -> temp : " + temp);

        if (("01").equals(temp)) {
            return Definition.TOS_MACOS;
        } else if (("10").equals(temp)) {
            return Definition.TOS_ANDROID;
        } else if (("11").equals(temp)) {
            return Definition.TOS_IOS;
        } else {
            return Definition.TOS_WINDOWS;
        }
    }

    /**
     * Target OS
     *
     * @param targetOS
     * @return
     */
    public static String[] getTargetOS(int targetOS) {
        switch (targetOS) {
            case Definition.TOS_MACOS:
                return new String[]{"0", "1"};
            case Definition.TOS_ANDROID:
                return new String[]{"1", "0"};
            case Definition.TOS_IOS:
                return new String[]{"1", "1"};
            default:
                return new String[]{"0", "0"};
        }
    }

    /**
     * Target OS
     *
     * @param targetOS
     * @return
     */
    public static String[] getTargetOS(String targetOS) {
        char[] charArray = targetOS.toCharArray();
        int count = charArray.length;
        String[] array = new String[2];
        for (int i = 0; i < count; i++) {
            array[i] = Character.toString(charArray[i]);
        }
        return array;
    }

    /**
     * Working Mode
     *
     * @param workingMode
     * @return
     */
    public static int getWorkingMode(String[] workingMode) {
        String temp = "";
        for (String string : workingMode) {
            temp += string;
        }
        LogUtil.d(TAG, "getWorkingMode() -> temp : " + temp);

        if (("01").equals(workingMode)) {
            return Definition.WMOD_CAMERA;
        } else if (("10").equals(workingMode)) {
            return Definition.WMOD_MUSIC;
        } else {
            return Definition.WMOD_PRESENTER;
        }
    }

    /**
     * Working Mode
     *
     * @param workingMode
     * @return
     */
    public static String[] getWorkingMode(int workingMode) {
        switch (workingMode) {
            case Definition.WMOD_CAMERA:
                return new String[]{"0", "1"};
            case Definition.WMOD_MUSIC:
                return new String[]{"1", "0"};
            default:
                return new String[]{"0", "0"};
        }
    }

    /**
     * Working Mode
     *
     * @param workingMode
     * @return
     */
    public static String[] getWorkingMode(String workingMode) {
        char[] charArray = workingMode.toCharArray();
        int count = charArray.length;
        String[] array = new String[2];
        for (int i = 0; i < count; i++) {
            array[i] = Character.toString(charArray[i]);
        }
        return array;
    }

    /**
     * Vendor Code
     *
     * @param vendorCode
     * @return
     */
    public static int getVendorCode(String[] vendorCode) {
        String temp = "";
        for (String string : vendorCode) {
            temp += string;
        }
        LogUtil.d(TAG, "getVendorCode() -> temp : " + temp);

        if (("00000001").equals(vendorCode)) {
            return Definition.VCOD_ITVERS;
        } else if (("00000010").equals(vendorCode)) {
            return Definition.VCOD_KT_MNS;
        } else if (("00000011").equals(vendorCode)) {
            return Definition.VCOD_SUNING;
        } else {
            return Definition.VCOD_B2C_MODEL;
        }
    }

    /**
     * Vendor Code
     *
     * @param vendorCode
     * @return
     */
    public static String[] getVendorCode(int vendorCode) {
        switch (vendorCode) {
            case Definition.VCOD_ITVERS:
                return new String[]{"0", "0", "0", "0", "0", "0", "0", "1"};
            case Definition.VCOD_KT_MNS:
                return new String[]{"0", "0", "0", "0", "0", "0", "1", "0"};
            case Definition.VCOD_SUNING:
                return new String[]{"0", "0", "0", "0", "0", "0", "1", "1"};
            default:
                return new String[]{"0", "0", "0", "0", "0", "0", "0", "0"};
        }
    }

    /**
     * Vendor Code
     *
     * @param vendorCode
     * @return
     */
    public static String[] getVendorCode(String vendorCode) {
        char[] charArray = vendorCode.toCharArray();
        int count = charArray.length;
        String[] array = new String[8];
        for (int i = 0; i < count; i++) {
            array[i] = Character.toString(charArray[i]);
        }
        return array;
    }

    /**
     * 해상도
     *
     * @param resolution
     * @return
     */
    public static int getScreenResolution(String[] resolution) {
        String temp = "";
        for (String s : resolution) {
            temp += s;
        }
//        LogUtil.d(TAG, "getResolution() -> temp : " + temp);

        if ("001".equals(temp)) {
            return Definition.RESOLUTION_HD;
        } else if ("010".equals(temp)) {
            return Definition.RESOLUTION_FHD;
        } else if ("011".equals(temp)) {
            return Definition.RESOLUTION_QHD;
        } else if ("100".equals(temp)) {
            return Definition.RESOLUTION_UHD;
        } else {
            return Definition.RESOLUTION_DEFAULT; //Steve_20191216
        }
    }

    /**
     * 해상도
     *
     * @param resolution
     * @return
     */
    public static String[] getScreenResolution(int resolution) {
        switch (resolution) {
            case Definition.RESOLUTION_HD:
                return new String[]{"0", "0", "1"};
            case Definition.RESOLUTION_FHD:
                return new String[]{"0", "1", "0"};
            case Definition.RESOLUTION_QHD:
                return new String[]{"0", "1", "1"};
            case Definition.RESOLUTION_UHD:
                return new String[]{"1", "0", "0"};
            default:
                return new String[]{"0", "0", "0"};
        }
    }

    public static String[] getScreenResolution(String resolution) {
        char[] charArray = resolution.toCharArray();
        int count = charArray.length;
        String[] array = new String[3];
        for (int i = 0; i < count; i++) {
            array[i] = Character.toString(charArray[i]);
        }
        return array;
    }

    /**
     * 해상도
     *
     * @param resolution
     * @return
     */
    public static String getResolutionDisplay(int resolution) {
        String result = "2220*1080";
        switch (resolution) {
            case Definition.RESOLUTION_HD:
                result = "1440*720";
                break;
            case Definition.RESOLUTION_QHD:
                result = "2960*1440";
                break;
            case Definition.RESOLUTION_UHD:
                result = "3840*2160";
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Mode
     *
     * @param mode
     * @return
     */
    public static int getMode(String mode) {
        if (("1").equals(mode)) {
            return Definition.MOUSE_MODE;
        }
        return Definition.KEYBOARD_MODE;
    }

    /**
     * Mode
     *
     * @param mode
     * @return
     */
    public static String getMode(int mode) {
        switch (mode) {
            case Definition.KEYBOARD_MODE:
                return "0";
            default:
                return "1";
        }
    }

    /**
     * Sensitive
     *
     * @param sensitive
     * @return
     */
    public static int getSensitive(String[] sensitive) {
        String temp = "";
        for (String string : sensitive) {
            temp += string;
        }
        LogUtil.d(TAG, "getSensitive() -> temp : " + temp);

        if (("01").equals(temp)) {
            return Definition.MMV_SENSITIVE_1;
        } else if (("10").equals(temp)) {
            return Definition.MMV_SENSITIVE_2;
        } else if (("11").equals(temp)) {
            return Definition.MMV_SENSITIVE_3;
        } else {
            return Definition.MMV_DULL;
        }
    }

    /**
     * Sensitive
     *
     * @param sensitive
     * @return
     */
    public static String[] getSensitive(int sensitive) {
        switch (sensitive) {
            case Definition.MMV_SENSITIVE_1:
                return new String[]{"0", "1"};
            case Definition.MMV_SENSITIVE_2:
                return new String[]{"1", "0"};
            case Definition.MMV_SENSITIVE_3:
                return new String[]{"1", "1"};
            default:
                return new String[]{"0", "0"};
        }
    }

    /**
     * Sensitive
     *
     * @param sensitive
     * @return
     */
    public static String[] getSensitive(String sensitive) {
        char[] charArray = sensitive.toCharArray();
        int count = charArray.length;
        String[] array = new String[2];
        for (int i = 0; i < count; i++) {
            array[i] = Character.toString(charArray[i]);
        }
        return array;
    }

    /**
     * LR
     *
     * @param lr
     * @return
     */
    public static int getLR(String lr) {
        if (("1").equals(lr)) {
            return Definition.LR_REVERSE;
        } else {
            return Definition.LR_NORMAL;
        }
    }

    /**
     * LR
     *
     * @param lr
     * @return
     */
    public static String getLR(int lr) {
        switch (lr) {
            case Definition.LR_REVERSE:
                return "1";
            default:
                return "0";
        }
    }

    /**
     * OS
     *
     * @param os
     * @return
     */
    public static int getOS(String os) {
        if (("0").equals(os)) {
            return Definition.IOS;
        }
        return Definition.ANDROID;
    }

    /**
     * OS
     *
     * @param os
     * @return
     */
    public static String getOS(int os) {
        switch (os) {
            case Definition.IOS:
                return "0";
            default:
                return "1";
        }
    }

    /**
     * Binary -> Hexadecimal 변환
     *
     * @param binary
     * @return
     */
    public static String binaryToHexadecimal(String binary) {
        return Long.toHexString(Long.parseLong(binary, 2)).replaceAll(".*.{16}", "$1");
    }

    /**
     * Hexadecimal -> Binary 변환
     *
     * @param hexadecimal
     * @param length
     * @return
     */
    public static String hexadecimalToBinary(String hexadecimal, int length) {
        return StringUtil.fillLeft(new BigInteger(hexadecimal, 16).toString(2), length, "0");
    }

    /**
     * 제조사 코드
     * <p>
     * Samsung  16  0x10	0000010000
     * Samsung  17	0x11	0000010001
     * LG       32	0x20	0000100000
     * Pantech  48	0x30	0000110000
     * Others   64	0x40	0001000000
     *
     * @param modelName
     * @return
     */
    public static int getVendor(String modelName) {
        if (modelName.toUpperCase().startsWith("SM")) {             // Samsung
            if (modelName.toUpperCase().contains("G960")            // Samsung Galaxy S9
                    || modelName.toUpperCase().contains("G965")) {  // Samsung Galaxy S9+
                return 11;
            }
            return 10;
        } else if (modelName.toUpperCase().startsWith("LG")) {      // LG
            return 20;
        } else if (modelName.toUpperCase().startsWith("IM")) {      // Pantech
            return 30;
        } else {                                                    // Others
            return 40;
        }
    }

    /**
     * 제조사명
     * <p>
     * Samsung  16  0x10	0000010000
     * Samsung  17	0x11	0000010001
     * LG       32	0x20	0000100000
     * Pantech  48	0x30	0000110000
     * Others   64	0x40	0001000000
     *
     * @param vender
     * @return
     */
    public static String getVendor(int vender) {
        String modelName = "";
        switch (vender) {
            case 10:
            case 11:
                modelName = "Samsung";
                break;
            case 20:
                modelName = "LG";
                break;
            case 30:
                modelName = "Pantech";
                break;
            default:
                modelName = "Others";
                break;

        }
        return modelName;
    }

    /**
     * Firmware Version
     *
     * @param body
     * @return
     */
    public static String getFirmwareVersion(String body) {
        char[] charArray = body.toCharArray();
        int count = charArray.length;
        String firmwareVersion = "";
        for (int i = 0; i < count; i++) {
            firmwareVersion += Character.toString(charArray[i]);
            firmwareVersion += ".";
        }
        return firmwareVersion.substring(0, firmwareVersion.length() - 1);
    }

    /**
     * 펌웨어 버전
     *
     * @param byteArray
     * @return
     */
    public static String getFirmwareVersion(byte[] byteArray) {
        String firmwareVersion = "";
        try {
            firmwareVersion = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            LogUtil.e(TAG, "getFirmwareVersion() -> UnsupportedEncodingException : " + uee);
        }
        LogUtil.d(TAG, "getFirmwareVersion() -> firmwareVersion : " + firmwareVersion);
        firmwareVersion = firmwareVersion.replaceAll("\\p{Z}", "");
        LogUtil.d(TAG, "getFirmwareVersion() -> firmwareVersion : " + firmwareVersion);
        LogUtil.d(TAG, "getFirmwareVersion() -> length : " + firmwareVersion.length());
        firmwareVersion = firmwareVersion.substring(4, 7);
        LogUtil.d(TAG, "getFirmwareVersion() -> firmwareVersion : " + firmwareVersion);
        firmwareVersion = ParserUtil.getFirmwareVersion(firmwareVersion);
        LogUtil.d(TAG, "getFirmwareVersion() -> firmwareVersion : " + firmwareVersion);
        return firmwareVersion;
    }

    /**
     * User Setting Data
     *
     * @param body
     * @return
     */
    public static String[] getSettingData(String body) {
        char[] charArray = body.toCharArray();
        int count = charArray.length;
        String[] array = new String[count];
        for (int i = 0; i < count; i++) {
            array[i] = Character.toString(charArray[i]);
        }
        return array;
    }

    /**
     * User Setting Data
     *
     * @param body
     * @return
     */
    public static String[] setSettingData(String body) {
        if (body == null || body.length() == 0) return null;
        LogUtil.d(TAG, "setSettingData() -> body.length : " + body.length());
        int count = body.length() / 8;
        LogUtil.d(TAG, "setSettingData() -> count : " + count);
        String[] array = new String[count];
        for (int i = 0; i < count; i++) {
            array[i] = body.substring(8 * i, 8 * i + 8);
        }
        return array;
    }

    /**
     * Byte Array -> Hexadecimal
     *
     * @param byteArray
     * @return
     */
    public static String byteArrayToHexadecimal(byte[] byteArray) {
        if (null == byteArray || byteArray.length == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (final byte b : byteArray) {
            sb.append(String.format("%02x ", b & 0xff));
        }
        return sb.toString();
    }

    public static byte hexadecimalToByte(String hexadecimal) {
        return (byte) Integer.parseInt(hexadecimal, 16);
    }

    /**
     * 펌웨어 버전 요청
     *
     * @return
     */
    public static byte[] requestFirmwareVersion() {
        return new byte[]{
                (byte) 0x11,
                (byte) 0x01,
                (byte) 0x00,
                (byte) 0x99
        };
    }

    /**
     * 사용자 설정 읽기 요청
     *
     * @return
     */
    public static byte[] requestReadUserSetting() {
        return new byte[]{
                (byte) 0x11,
                (byte) 0x02,
                (byte) 0x00,
                (byte) 0x99
        };
    }

    /**
     * 사용자 설정 쓰기 요청
     *
     * @return
     */
    public static byte[] requestWriteUserSetting() {
        return new byte[]{
                (byte) 0x11,
                (byte) 0x03,
                (byte) 0x00,
                (byte) 0x99
        };
    }

    /**
     * DFU 요청
     *
     * @return
     */
    public static byte[] requestBatteryLevel() {
        return new byte[]{
                (byte) 0x11,
                (byte) 0x04,
                (byte) 0x00,
                (byte) 0x99
        };
    }

    /**
     * 단말기 깨우기 요청
     *
     * @return
     */
    public static byte[] requestDeviceAwake() {
        return new byte[]{
                (byte) 0x11,
                (byte) 0x05,
                (byte) 0x00,
                (byte) 0x99
        };
    }

    /**
     * 펌 웨어 버전, 배터리 레벨 요청
     *
     * @return
     */
    public static byte[] requestFirmwareVersionNBatteryLevel() {
        return new byte[]{
                (byte) 0x11,
                (byte) 0x06,
                (byte) 0x00,
                (byte) 0x99
        };
    }

    /**
     * 펌웨어 모든 정보 요청
     *
     * @return
     */
    public static byte[] requestFirmwareInformation() {
        return new byte[]{
                (byte) 0x11,
                (byte) 0x09,
                (byte) 0x00,
                (byte) 0x99
        };
    }

    /**
     * DFU 요청
     *
     * @return
     */
    public static byte[] requestJumpBootloader() {
        return new byte[]{
                (byte) 0x11,
                (byte) 0x08,
                (byte) 0x00,
                (byte) 0x99
        };
    }

    /**
     * Hotkey 실행 여부
     *
     * @return
     */
    public static byte[] responseHotkey(boolean success) {
        if (success) {
            return new byte[]{
                    (byte) 0x11,
                    (byte) 0x8B,
                    (byte) 0x01,
                    (byte) 0x00,
                    (byte) 0x99
            };
        }
        return new byte[]{
                (byte) 0x11,
                (byte) 0x8B,
                (byte) 0x01,
                (byte) 0x01,
                (byte) 0x99
        };
    }
}

package com.itvers.toolbox.activity.main.admin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.itvers.toolbox.App;
import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.dialog.DialogQMProgress;
import com.itvers.toolbox.item.Result;
import com.itvers.toolbox.item.Type;
import com.itvers.toolbox.item.UARTStatus;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ParserUtil;
import com.itvers.toolbox.util.PreferencesUtil;
import com.itvers.toolbox.util.StringUtil;
import com.itvers.toolbox.util.ToastUtil;

public class UserActivity extends Activity implements App.BlutoothListener {
    private final static String TAG = UserActivity.class.getSimpleName();   // 디버그 태그
    private Type type = Type.FIRMWARE_INFORMATION;                          // 요청 타입
    private DialogQMProgress dialogQMProgress;                              // 프로그레스 다이얼로그
    private static Handler progressHandler;                                 // 프로그레스 다이얼로그 핸들러
    private byte[] tempByteArray;                                           // 임시 데이터
    private String[] arrayData;                                             // 데이터
    private String[] arrayTemp;                                             // 임시 데이터
    private  int vendorCode = -1;                                           // 제품 코드
    private boolean isSave = false;                                         // 저장 여부

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        // 데이터 초기화
        arrayData = new String[Definition.DATA_SETTING_LENGTH];
        for (int i = 0; i < Definition.DATA_SETTING_LENGTH; i++) {
            arrayData[i] = "0";
        }
        LogUtil.d(TAG, "onCreate() -> arrayData : " + arrayData.length);

        // 임시 데이터 초기화
        arrayTemp = new String[Definition.DATA_SETTING_LENGTH];
        for (int i = 0; i < Definition.DATA_SETTING_LENGTH; i++) {
            arrayTemp[i] = "0";
        }
        LogUtil.d(TAG, "onCreate() -> arrayTemp : " + arrayTemp.length);

        // 뒤로가기 버튼
        findViewById(R.id.activity_user_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
        });

        // 저장 버튼
        findViewById(R.id.activity_user_tv_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i(TAG, "onClick() -> Start !!!");

                String vendorName = ((Spinner)findViewById(R.id.activity_user_spinner)).getSelectedItem().toString();
                LogUtil.d(TAG, "onCreate() -> vendorName : " + vendorName);
                vendorCode = ((Spinner)findViewById(R.id.activity_user_spinner)).getSelectedItemPosition();
                LogUtil.e(TAG, "onCreate() -> vendorCode : " + vendorCode);
                // 데이터 전송
                if (null != App.getInstance().getUARTService()) {
                    // 프로그레스 다이얼로그
                    showProgress();

                    type = Type.WRITE_SETTING;

                    tempByteArray = getUserSettingValue();
                    LogUtil.e(TAG, "getUserSettingValue() -> " + tempByteArray);
                    // 데이터 전송
                    App.getInstance().writeData(getUserSettingValue());
                }
            }
        });

        String[] items = new String[]{"NONE", "ITVERS", "KT M&S", "SUNING"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        ((Spinner)findViewById(R.id.activity_user_spinner)).setAdapter(adapter);

        byte[] byteArray = App.getInstance().getFirmwareInformationData();
        tempByteArray = byteArray;
        LogUtil.d(TAG, "onCreate() -> byteArray : " + byteArray);

        if (null != byteArray) LogUtil.d(TAG, "onCreate() -> byteArray : " + byteArray.length);
        LogUtil.d(TAG, "onCreate() -> TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH : " + Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH);

        if (null != byteArray)
            LogUtil.d(TAG, "onCreate() -> getBluetoothDevice : " + App.getInstance().getBluetoothDevice());
        if (null != App.getInstance().getBluetoothDevice())
            LogUtil.d(TAG, "onCreate() -> getName : " + App.getInstance().getBluetoothDevice().getName());
        if (null != byteArray
                && byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH
                && null != App.getInstance().getBluetoothDevice()
                && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getName())) {
            // 사용자 정보 파싱
            parseUserSettingData();
        } else {
            if (null == App.getInstance().getBluetoothDevice()) {
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            } else {
                // 프로그레스 다이얼로그 시작
                showProgress();

                if (null != App.getInstance().getUARTService()
                        && null != App.getInstance().getBluetoothDevice()
                        && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getAddress())) {
                    App.getInstance().getUARTService().connect(App.getInstance().getBluetoothDevice().getAddress());
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume() -> Start !!!");

        // UART 리스너 해제
        App.getInstance().setBlutoothListener(null, UserActivity.this);
        // BLE 리스너 등록
        App.getInstance().setBlutoothListener(this, UserActivity.this);
    }

    // 백버튼
    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        setResult(Activity.RESULT_OK);
        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    @Override
    public void onUARTServiceChange(UARTStatus status) {
        LogUtil.d(TAG, "onUARTServiceChange() -> status : " + status);
        switch (status) {
            case SERVICE_CONNECTED:
                break;
            case GATT_CONNECTED: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        type = Type.FIRMWARE_INFORMATION;
                        // 데이터 쓰기
                        App.getInstance().writeData(type);
                    }
                }, Definition.GATT_INTERVAL_TIME); // 2초 후에 실행
            }
            break;
            case GATT_SERVICES_DISCOVERED:
                break;
            case DATA_AVAILABLE:
                break;
            case EMPTY_DEVICE:
            case EMPTY_DEVICE_ADDRESS:
            case SERVICE_DISCONNECTED:
            case SERVICE_BINDING_DIED:
            case DEVICE_DOES_NOT_SUPPORT_UART:
            case ERROR_EMPTY_UART_SERVICE:
            case ERROR_UART_SERVICE_INITIALIZATION:
            case ERROR_UART_SERVICE_CONNECT:
            case GATT_DISCONNECTED: {

                App.getInstance().setFirmwareInformationData(null);
                App.getInstance().setConnectedDevice(false);
                App.getInstance().setConnectedBluetoothDevice(null);

                setResult(Activity.RESULT_OK);
                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
            break;
        }
    }

    @Override
    public void onUARTServiceData(Intent intent) {
        LogUtil.d(TAG, "onUARTServiceData() -> intent : " + intent);
        if (null != intent) {
            byte[] byteArray = intent.getByteArrayExtra(Definition.EXTRA_DATA);

            switch (type) {
                case FIRMWARE_INFORMATION:
                    // 프로그레스 다이얼 로그 종료
                    dismissProgress();

                    // 데이터 읽기
                    Result result = App.getInstance().readData(byteArray, type);
                    if (result == Result.SUCCESS) {
                        if (null != byteArray
                                && byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH
                                && null != App.getInstance().getBluetoothDevice()
                                && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getName())) {
                            App.getInstance().setFirmwareInformationData(byteArray);
                            tempByteArray = App.getInstance().getFirmwareInformationData();
                            // 사용자 정보 파싱
                            parseUserSettingData();

                            if (isSave) {
                                ToastUtil
                                        .getInstance()
                                        .show(UserActivity.this,
                                                getResources().getString(R.string.saved),
                                                false);
                            }
                        }
                    }
                    isSave = false;
                    break;
                case WRITE_SETTING:
                    // 데이터 읽기
                    result = App.getInstance().readData(byteArray, type);
                    if (result == Result.SUCCESS) {
                        if (null != byteArray
                                && byteArray.length == Definition.TOTAL_DATA_RESPONSE_WRITE_SETTING_LENGTH
                                && null != App.getInstance().getBluetoothDevice()
                                && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getName())) {

                            // 사용자 정보 파싱
                            writeUserSettingData();

                            PreferencesUtil.getInstance(UserActivity.this).setVendorCode(vendorCode);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    isSave = true;

                                    type = Type.FIRMWARE_INFORMATION;
                                    // 데이터 쓰기
                                    App.getInstance().writeData(type);
                                }
                            }, Definition.GATT_INTERVAL_TIME); // 2초 후에 실행

                            return;
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 사용자 정보 쓰기
     */
    private void writeUserSettingData() {
        if (null == App.getInstance().getFirmwareInformationData()) return;
        String hexadecimal = ParserUtil.byteArrayToHexadecimal(App.getInstance().getFirmwareInformationData());
        LogUtil.e(TAG, "writeUserSettingData() -> hexadecimal : " + hexadecimal);

        // TODO 텍스트
        String[] array = hexadecimal.split("\\p{Z}");
        LogUtil.d(TAG, "writeUserSettingData() -> array : " + array);
//        for (String string : array) {
//            LogUtil.d(TAG, "writeUserSettingData() -> string : " + string);
//        }
        // 유효성 체크
        String stx = array[0];
        LogUtil.d(TAG, "writeUserSettingData() -> stx : " + stx);
        String length = array[2];
        LogUtil.d(TAG, "writeUserSettingData() -> length : " + length);
        String etx = array[4];
        LogUtil.d(TAG, "writeUserSettingData() -> etx : " + etx);
        String command = array[1];
        LogUtil.d(TAG, "writeUserSettingData() -> command : " + command);

        // 사용자 설정 정보 쓰기
        if (Definition.RESPONSE_COMMAND_WRITE_USER_SETTING.equals(command)) {
            App.getInstance().setFirmwareInformationData(tempByteArray);
            ToastUtil.getInstance().show(UserActivity.this, getResources().getString(R.string.saved), false);
        }
    }

    /**
     * 사용자 설정
     */
    private void parseUserSettingData() {
        if (null == App.getInstance().getFirmwareInformationData()) return;

        String hexadecimal = ParserUtil.byteArrayToHexadecimal(App.getInstance().getFirmwareInformationData());
        LogUtil.d(TAG, "parseUserSettingData() -> hexadecimal : " + hexadecimal);

        // TODO 텍스트
        String[] array = hexadecimal.split("\\p{Z}");
        LogUtil.d(TAG, "parseUserSettingData() -> array : " + array);
//        for (String string : array) {
//            LogUtil.d(TAG, "parseUserSettingData() -> string : " + string);
//        }
        // 유효성 체크
        String stx = array[0];
        LogUtil.d(TAG, "parseUserSettingData() -> stx : " + stx);
        String length = array[2];
        LogUtil.d(TAG, "parseUserSettingData() -> length : " + length);
        String etx = array[11];
        LogUtil.d(TAG, "parseUserSettingData() -> etx : " + etx);
        String command = array[1];
        LogUtil.d(TAG, "parseUserSettingData() -> command : " + command);

        // 펌웨어 정보 읽기
        if (Definition.RESPONSE_COMMAND_FIRMWARE_INFORMATION.equals(command)) {
            String binary = "";
            binary += ParserUtil.hexadecimalToBinary(array[3], 8);
            binary += ParserUtil.hexadecimalToBinary(array[4], 8);
            binary += ParserUtil.hexadecimalToBinary(array[5], 8);
            binary += ParserUtil.hexadecimalToBinary(array[6], 8);
            LogUtil.e(TAG, "parseUserSettingData() -> binary : " + binary + ", length : " + binary.length());

            String[] arrBody = ParserUtil.getSettingData(binary);
            if (null == arrBody) {
                LogUtil.e(TAG, "parseUserSettingData() -> arrBody is null.");
                ToastUtil.getInstance().show(
                        UserActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }

            LogUtil.e(TAG, "parseUserSettingData() -> arrBody : " + arrBody + ", length : " + arrBody.length);
            if (Definition.TOTAL_DATA_SETTING_BINARY_LENGTH != arrBody.length) {
                LogUtil.e(TAG, "parseUserSettingData() -> arrBody length is not invalid.");
                ToastUtil.getInstance().show(
                        UserActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }
            arrayData = arrBody;

            // 제조사
            String[] temp = new String[8];
            temp[0] = arrayData[Definition.INDEX_VENDOR_1];
            temp[1] = arrayData[Definition.INDEX_VENDOR_2];
            temp[2] = arrayData[Definition.INDEX_VENDOR_3];
            temp[3] = arrayData[Definition.INDEX_VENDOR_4];
            temp[4] = arrayData[Definition.INDEX_VENDOR_5];
            temp[5] = arrayData[Definition.INDEX_VENDOR_6];
            temp[6] = arrayData[Definition.INDEX_VENDOR_7];
            temp[7] = arrayData[Definition.INDEX_VENDOR_8];

            String strVendor = "";
            for (String string : temp) {
                strVendor += string;
            }
            LogUtil.e(TAG, "parseUserSettingData() -> strVendor : " + strVendor);
            vendorCode = Integer.parseInt(ParserUtil.binaryToHexadecimal(strVendor));
            LogUtil.e(TAG, "parseUserSettingData() -> vendorCode : " + vendorCode);
            ((Spinner)findViewById(R.id.activity_user_spinner)).setSelection(vendorCode);
        }
    }

    /**
     * 유저 정보 값
     *
     * @return
     */
    private byte[] getUserSettingValue() {
        byte[] value;

        // 제풐 코드
        String[] arrVendorCode = ParserUtil.getVendorCode(vendorCode);
        LogUtil.d(TAG, "getUserSettingValue() -> arrVendorCode : " + arrVendorCode);

        for (int i = 0; i < Definition.TOTAL_DATA_SETTING_BINARY_LENGTH; i++) {
            //  제조사 코드
            if (i == Definition.INDEX_VENDOR_1) arrayData[i] = arrVendorCode[0];
            if (i == Definition.INDEX_VENDOR_2) arrayData[i] = arrVendorCode[1];
            if (i == Definition.INDEX_VENDOR_3) arrayData[i] = arrVendorCode[2];
            if (i == Definition.INDEX_VENDOR_4) arrayData[i] = arrVendorCode[3];
            if (i == Definition.INDEX_VENDOR_5) arrayData[i] = arrVendorCode[4];
            if (i == Definition.INDEX_VENDOR_6) arrayData[i] = arrVendorCode[5];
            if (i == Definition.INDEX_VENDOR_7) arrayData[i] = arrVendorCode[6];
            if (i == Definition.INDEX_VENDOR_8) arrayData[i] = arrVendorCode[7];
        }

        String body = "";
        for (int i = 0; i < Definition.TOTAL_DATA_SETTING_BINARY_LENGTH; i++) {
            body += arrayData[i];
        }
        LogUtil.d(TAG, "getUserSettingValue() -> body : " + body);

        String[] tempArray = ParserUtil.setSettingData(body);

        // TODO 테스트
//        for (String string : tempArray) {
//            LogUtil.d(TAG, "getUserSettingValue() -> string : " + string);
//        }

        for (int i = 0; i < tempArray.length; i++) {
            tempArray[i] = ParserUtil.binaryToHexadecimal(tempArray[i]);
            LogUtil.d(TAG, "getUserSettingValue() -> tempArray[" + i + "] :" + tempArray[i]);
        }
        value = new byte[Definition.TOTAL_DATA_REQUEST_READ_SETTING_LENGTH];
        value[0] = ParserUtil.hexadecimalToByte("11");
        value[1] = ParserUtil.hexadecimalToByte("3");
        value[2] = ParserUtil.hexadecimalToByte("4");
        value[3] = ParserUtil.hexadecimalToByte(tempArray[0]);
        value[4] = ParserUtil.hexadecimalToByte(tempArray[1]);
        value[5] = ParserUtil.hexadecimalToByte(tempArray[2]);
        value[6] = ParserUtil.hexadecimalToByte(tempArray[3]);
        value[7] = ParserUtil.hexadecimalToByte("99");
        LogUtil.d(TAG, "getUserSettingValue() -> value :" + value);
        return value;
    }

    /**
     * 프로그레스 다이얼로그 시작
     */
    private void showProgress() {
        LogUtil.i(TAG, "showProgress() -> Start !!!");

        if (App.getInstance().getIsBackground()) return;

        // 프로그레스 다이얼로그 종료
        dismissProgress();

        dialogQMProgress = new DialogQMProgress(this, null, true);
        dialogQMProgress.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        if (null != dialogQMProgress) {
            dialogQMProgress.show();

            // 프로그레스 다이얼로그 핸들러 시작
            startProgressHandler();
        }
    }

    /**
     * 프로그레스 종료
     */
    private void dismissProgress() {
        LogUtil.i(TAG, "dismissProgress() -> Start !!!");

        // 프로그레스 다이얼로그 핸들러 중지
        stopProgressHandler();

        if (null != dialogQMProgress
                && dialogQMProgress.isShowing()) {
            dialogQMProgress.dismiss();
            dialogQMProgress = null;
        }
    }

    /**
     * 프로그레스 다이얼로그 핸들러 시작
     */
    private void startProgressHandler() {
        LogUtil.i(TAG, "startProgressHandler() -> Start !!!");
        progressHandler = new Handler();
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 프로그레스 다이얼로그 후처리
                postProgress();
            }
        }, Definition.PROGRESS_INTERVAL_TIME);
    }

    /**
     * 프로그레스 다이얼로그 핸들러 중지
     */
    private void stopProgressHandler() {
        LogUtil.i(TAG, "stopProgressHandler() -> Start !!!");
        if (null != progressHandler) progressHandler.removeMessages(0);
    }

    /**
     * 프로그레스 후처리
     */
    private void postProgress() {
        LogUtil.i(TAG, "postProgress() -> Start !!!");

        // 프로그레스 다이얼로그 종료
        dismissProgress();

        ToastUtil.getInstance().show(
                    UserActivity.this,
                getResources().getString(R.string.no_connected_devices_found),
                false);
    }

    @Override
    protected void onDestroy() {
        if (isSave) {
           setResult(RESULT_OK);
        }
        super.onDestroy();
    }
}

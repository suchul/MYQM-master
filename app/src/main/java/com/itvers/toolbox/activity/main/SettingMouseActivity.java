package com.itvers.toolbox.activity.main;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.itvers.toolbox.App;
import com.itvers.toolbox.R;
import com.itvers.toolbox.activity.SelectDeviceActivity;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.RequestCode;
import com.itvers.toolbox.dialog.Dialog;
import com.itvers.toolbox.dialog.DialogQMProgress;
import com.itvers.toolbox.item.Result;
import com.itvers.toolbox.item.Type;
import com.itvers.toolbox.item.UARTStatus;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ParserUtil;
import com.itvers.toolbox.util.StringUtil;
import com.itvers.toolbox.util.ToastUtil;

import java.lang.reflect.Method;
import java.util.HashMap;

public class SettingMouseActivity extends AppCompatActivity implements
        View.OnClickListener,
        App.BlutoothListener {

    private final static String TAG = SettingMouseActivity.class.getSimpleName();   // 디버그 태그

    private DialogQMProgress dialogQMProgress;                                      // 프로그레스 다이얼로그
    private static Handler progressHandler;                                         // 프로그레스 다이얼로그 핸들러
    private boolean isResolutionSynchronization;                                    // 해상도 동기화 여부
    private boolean isSavedFinish;                                                  // 저장 후 종료
    private String[] arrayData;                                                     // 데이터
    private String[] arrayTemp;                                                     // 임시 데이터

    private int os = -1;                                                            // OS
    private int lr = -1;                                                            // 사용방향
    private int sensitive = -1;                                                     // 민감도
    private int mode = -1;                                                          // 모드
    private int screenResolution = -1;                                              // 해상도
    private int vendor = -1;                                                        // 제조사
    private int workingMode = -1;                                                   // 사용모드
    private int targetOS = -1;                                                      // 타겟 OS (00:Windows, 01:MacOS, 10:Android, 11:iOS)
    private int wheelMode = -1;                                                     // 휠 모드 (0:Mouse Cursor, 1:Wheel Mode)
    private int productModel = -1;                                                  // 제품 모델 (1:Slim Mouse, 2:Smart Patch, 3:Robostick, etc:Reserved)
    private int reserved = -1;                                                      // 기타

    private int tmpOS = -1;                                                         // 임시 저장 OS
    private int tmpLR = -1;                                                         // 임시 저장 사용방향
    private int tmpSensitive = -1;                                                  // 임시 저장 포인터 속도
    private int tmpMode = -1;                                                       // 임시 저장 모드
    private int tmpScreenResolution = -1;                                           // 임시 해상도
    private int tmpVendor = -1;                                                     // 임시 제조사
    private int tmpWorkingMode = -1;                                                // 임시 사용모드
    private int tmpTargetOS = -1;                                                   // 임시 타켓 OS
    private int tmpWheelMode = -1;                                                  // 임시 마우스 모드
    private int tmpProductModel = -1;                                               // 임시 제품 모델
    private int tmpReserved = -1;                                                   // 임시 기타

    private AnimationDrawable animationAndroid;                                     // 안드로이드 아이콘 애니메이션
    private AnimationDrawable animationiOS;                                         // iOS 아이콘 애니메이션
    private AnimationDrawable animationKeyboard;                                    // 키보드 모드 애니메이션
    private AnimationDrawable animationMouse;                                       // 마우스 모드 애니메이션
    private AnimationDrawable animationWheel;                                       // 휠 모드 애니메이션
    private AnimationDrawable animationIndexFinger;                                 // 사용방향 검지 애니메이션
    private AnimationDrawable animationThumb;                                       // 사용방향 엄지 애니메이션
    private Handler textChangeHandler;                                              // 저정/변경 택스트 컬러 변경 핸들러
    private final int MSG_CHANGE_WHITE = 0;                                         // 핸들러 메시지 WHITE
    private final int MSG_CHANGE_RED = 1;                                           // 핸들러 메시지 RED

    private byte[] tempByteArray;                                                   // 임시 데이터
    private Type type = Type.FIRMWARE_INFORMATION;                                  // 요청 타입
    private boolean isSave = false;                                                 // 저장 여부

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

                // 장비 선택 엑티비티
                goToSelectDeviceActivity();
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
                                ToastUtil.getInstance().show(SettingMouseActivity.this, getResources().getString(R.string.saved), false);
                                isSave = false;
                            }

                            if (isSavedFinish) {
                                isSavedFinish = false;

                                // 엑티비티 종료
                                finish();
                                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                                return;
                            }
                        }
                    }
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
                    // 프로그레스 다이얼 로그 종료
                    dismissProgress();
                    break;
            }
        }
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_mouse);
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

        // 클릭 리스너 등록
        findViewById(R.id.activity_setting_mouse_ll_back).setOnClickListener(this);                             // 뒤로가기 버튼
        findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).setOnClickListener(this);                      // OS 스위치 버튼
        findViewById(R.id.activity_setting_mouse_rb_mouse).setOnClickListener(this);                            // 마우스 모드 라디오 버튼
        findViewById(R.id.activity_setting_mouse_rb_wheel).setOnClickListener(this);                            // 휠 모드 라디오 버튼
        findViewById(R.id.activity_setting_mouse_rb_keyboard).setOnClickListener(this);                         // 키보드 모드 라디오 버튼
        findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).setOnClickListener(this);               // 사용방향 스위치 버튼
        findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow).setOnClickListener(this);           // 매우 느림 라디오 버튼
        findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow).setOnClickListener(this);                // 느림 라디오 버튼
        findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal).setOnClickListener(this);              // 보통 라디오 버튼
        findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast).setOnClickListener(this);                // 빠름 라디오 버튼
        findViewById(R.id.activity_setting_mouse_tv_resolution_synchronization_start).setOnClickListener(this); // 해상도 동기화 시작 버튼
        findViewById(R.id.activity_setting_mouse_tv_resolution_synchronization_start).setEnabled(false);
        findViewById(R.id.activity_setting_mouse_tv_save_change).setOnClickListener(this);                      // 저장 및 변경 버튼
        findViewById(R.id.activity_setting_mouse_tv_save_change).setEnabled(false);

        // 저장/변경 텍스트 변경 핸들러
        textChangeHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CHANGE_WHITE:
                        ((TextView) findViewById(R.id.activity_setting_mouse_tv_save_change)).setTextColor(getResources().getColor(R.color.colorWhite));
                        textChangeHandler.sendMessageDelayed(textChangeHandler.obtainMessage(MSG_CHANGE_RED), 500);
                        break;
                    case MSG_CHANGE_RED:
                        ((TextView) findViewById(R.id.activity_setting_mouse_tv_save_change)).setTextColor(getResources().getColor(R.color.colorRed));
                        textChangeHandler.sendMessageDelayed(textChangeHandler.obtainMessage(MSG_CHANGE_WHITE), 500);
                        break;
                    default:
                        break;
                }
            }
        };

//        byte[] byteArray = App.getInstance().getFirmwareInformationData();
//        tempByteArray = byteArray;
//        LogUtil.d(TAG, "onCreate() -> byteArray : " + byteArray);
//
//        if (null != byteArray) LogUtil.d(TAG, "onCreate() -> byteArray : " + byteArray.length);
//        if (null != byteArray)
//            LogUtil.d(TAG, "onCreate() -> getBluetoothDevice : " + App.getInstance().getBluetoothDevice());
//        if (null != App.getInstance().getBluetoothDevice())
//            LogUtil.d(TAG, "onCreate() -> getName : " + App.getInstance().getBluetoothDevice().getName());
//        if (null != byteArray
//                && byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH
//                && null != App.getInstance().getBluetoothDevice()
//                && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getName())) {
//            // 사용자 정보 파싱
//            parseUserSettingData();
//        } else {
//            if (null == App.getInstance().getBluetoothDevice()) {
////                finish();
////                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
//            } else {
//                // 프로그레스 다이얼로그 시작
//                showProgress();
//
//                if (null != App.getInstance().getUARTService()
//                        && null != App.getInstance().getBluetoothDevice()
//                        && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getAddress())) {
//                    App.getInstance().getUARTService().connect(App.getInstance().getBluetoothDevice().getAddress());
//                }
//            }
//        }

        // 2019-08-22 진입시 통신 시도
        showProgress();
        if (null != App.getInstance().getUARTService()
                && null != App.getInstance().getBluetoothDevice()
                && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getAddress())) {
            App.getInstance().getUARTService().connect(App.getInstance().getBluetoothDevice().getAddress());
        }

        // 뷰 설정
        setView();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume() -> Start !!!");

        // 백그라운드 여부
        if (App.getInstance().getIsBackground()) {
            App.getInstance().setIsBackground(true);
            LogUtil.d(TAG, "onResume() -> getIsBackground : " + App.getInstance().getIsBackground());
        }

        LogUtil.d(TAG, "onResume() -> getBluetoothAdapter : " + App.getInstance().getBluetoothAdapter());
        // BLE 어댑터 사용 여부 체크
        if (null != App.getInstance().getBluetoothAdapter()) {
            if (!App.getInstance().getBluetoothAdapter().isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RequestCode.REQUEST_ENABLE_BLUTOOTH);
                return;
            }
        }

        // UART 리스너 해제
        App.getInstance().setBlutoothListener(null, SettingMouseActivity.this);
        // BLE 리스너 등록
        App.getInstance().setBlutoothListener(this, SettingMouseActivity.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy() -> Start !!!");

        // 저장/변경 버튼 텍스트 색상 중지
        setAnimationSave(false);

        // 프로그레스 다이얼로그 중지
        dismissProgress();
    }

    // 백버튼
    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        LogUtil.d(TAG, "onBackPressed() -> View : " + checkChangData());
        LogUtil.d(TAG, "onBackPressed() -> getUARTService : " + App.getInstance().getUARTService());

        if (checkChangData() &&
                null != App.getInstance().getUARTService()) {
            Dialog.getInstance().showDual(
                    this,
                    getResources().getString(R.string.notice),
                    getResources().getString(R.string.dialog_save_description),
                    getResources().getString(R.string.yes),
                    getResources().getString(R.string.no),
                    false,
                    new Dialog.DialogOnClickListener() {

                        @Override
                        public void OnItemClickResult(HashMap<String, Object> hashMap) {
                            int result = ( int ) hashMap.get(Definition.KEY_DIALOG_DUAL);
                            switch (result) {
                                case Definition.DIALOG_BUTTON_POSITIVE: {
                                    // 데이터 전송
                                    if (null != App.getInstance().getUARTService()) {
                                        isSavedFinish = true;
                                        type = Type.WRITE_SETTING;
                                        // 데이터 전송
                                        App.getInstance().writeData(getUserSettingValue());
                                    }
                                }
                                break;
                                case Definition.DIALOG_BUTTON_NETURAL:
                                    break;
                                case Definition.DIALOG_BUTTON_NEGATIVE: {
                                    // 엑티비티 종료
                                    finish();
                                    overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                                }
                                break;
                                default:
                                    break;
                            }
                        }
                    });
        } else {

            // 엑티비티 종료
            finish();
            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
        }
    }

    @Override
    public void onClick(View v) {
        LogUtil.d(TAG, "onClick() -> View : " + v);

        switch (v.getId()) {
            // 기기선택 버튼
            case R.id.activity_setting_mouse_ll_back:
                if (checkChangData()
                        && null != App.getInstance().getUARTService()) {
                    Dialog.getInstance().showDual(
                            this,
                            getResources().getString(R.string.notice),
                            getResources().getString(R.string.dialog_save_description),
                            getResources().getString(R.string.yes),
                            getResources().getString(R.string.no),
                            false,
                            new Dialog.DialogOnClickListener() {
                                @Override
                                public void OnItemClickResult(HashMap <String, Object> hashMap) {
                                    int result = ( int ) hashMap.get(Definition.KEY_DIALOG_DUAL);
                                    switch (result) {
                                        case Definition.DIALOG_BUTTON_POSITIVE: {
                                            // 데이터 전송
                                            if (null != App.getInstance().getUARTService()) {
                                                isSavedFinish = true;
                                                type = Type.WRITE_SETTING;
                                                tempByteArray = getUserSettingValue();
                                                // 데이터 전송
                                                App.getInstance().writeData(tempByteArray);
                                            }
                                        }
                                        break;
                                        case Definition.DIALOG_BUTTON_NETURAL:
                                            break;
                                        case Definition.DIALOG_BUTTON_NEGATIVE: {
                                            // 엑티비티 종료
                                            finish();
                                            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                                        }
                                        break;
                                        default:
                                            break;
                                    }
                                }
                            });
                } else {
                    // 엑티비티 종료
                    finish();
                    overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                }
                break;
            // OS 스위치 버튼
            case R.id.activity_setting_mouse_ibtn_os_on_off:
                LogUtil.d(TAG, "onClick() -> os : " + os);
                LogUtil.d(TAG, "onClick() -> isSelected : " + findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).isSelected());
                if (findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).isSelected()) {
                    findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).setSelected(false);
                    ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_os_on_off)).setImageResource(R.drawable.sw_off);
                    os = Definition.IOS;

                    // 변수 -> 임시변수
                    valueToTemp();
                } else {
                    findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).setSelected(true);
                    ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_os_on_off)).setImageResource(R.drawable.sw_on);
                    os = Definition.ANDROID;

                    // 임시변수 -> 변수
                    tempToValue();
                }
                setView();

                break;
            // 키보드 모드 라디오 버튼
            case R.id.activity_setting_mouse_rb_keyboard:
                if (((RadioButton) findViewById(R.id.activity_setting_mouse_rb_keyboard)).isChecked()) {
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_mouse)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_wheel)).setChecked(false);
                    mode = Definition.KEYBOARD_MODE;
                    wheelMode = Definition.MOUSE_CURSOR;
                    setViewMode();
                    break;
                }
                findViewById(R.id.activity_setting_mouse_rb_wheel).setEnabled(true);
                setViewMode();
                break;
            // 마우스 모드 라디오 버튼
            case R.id.activity_setting_mouse_rb_mouse:
                if (((RadioButton) findViewById(R.id.activity_setting_mouse_rb_mouse)).isChecked()) {
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_keyboard)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_wheel)).setChecked(false);
                    mode = Definition.MOUSE_MODE;
                    wheelMode = Definition.MOUSE_CURSOR;
                    setViewMode();
                    break;
                }
                findViewById(R.id.activity_setting_mouse_rb_mouse).setEnabled(true);
                setViewMode();
                break;
            // 휠 모드 라디오 버튼
            case R.id.activity_setting_mouse_rb_wheel:
                if (((RadioButton) findViewById(R.id.activity_setting_mouse_rb_wheel)).isChecked()) {
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_keyboard)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_mouse)).setChecked(false);
                    mode = Definition.MOUSE_MODE;
                    wheelMode = Definition.MOUSE_WHEEL;
                    setViewMode();
                    break;
                }
                findViewById(R.id.activity_setting_mouse_rb_wheel).setEnabled(true);

                // 모드 뷰
                setViewMode();
                break;
            // 사용방향 스위치 버튼
            case R.id.activity_setting_mouse_ibtn_direction_on_off:
                if (findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).isSelected()) {
                    findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).setSelected(false);
                    ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off))
                            .setImageResource(R.drawable.sw_off);
                    lr = Definition.LR_NORMAL;
                } else {
                    findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).setSelected(true);
                    ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off))
                            .setImageResource(R.drawable.sw_on);
                    lr = Definition.LR_REVERSE;
                }
                // 사용방향 뷰
                setViewlr();
                break;
            // 매우 느림 라디오 버튼
            case R.id.activity_setting_mouse_rb_poiner_speed_very_slow:
                if (((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).isChecked()) {
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).setChecked(false);
                    sensitive = Definition.MMV_DULL;
                    setViewsensitive();
                    break;
                }
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow).setEnabled(true);
                setViewsensitive();
                break;

            // 느림 라디오 버튼
            case R.id.activity_setting_mouse_rb_poiner_speed_slow:
                if (((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).isChecked()) {
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).setChecked(false);
                    sensitive = Definition.MMV_SENSITIVE_1;
                    setViewsensitive();
                    break;
                }
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow).setEnabled(true);
                setViewsensitive();
                break;
            // 보통 라디오 버튼
            case R.id.activity_setting_mouse_rb_poiner_speed_normal:
                if (((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).isChecked()) {
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).setChecked(false);
                    sensitive = Definition.MMV_SENSITIVE_2;
                    setViewsensitive();
                    break;
                }
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal).setEnabled(true);
                setViewsensitive();
                break;
            // 빠름 라디오 버튼
            case R.id.activity_setting_mouse_rb_poiner_speed_fast:
                if (((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).isChecked()) {
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).setChecked(false);
                    ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).setChecked(false);
                    sensitive = Definition.MMV_SENSITIVE_3;
                    setViewsensitive();
                    break;
                }
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast).setEnabled(true);
                setViewsensitive();
                break;
            // 해상도 동기화 시작 버튼
            case R.id.activity_setting_mouse_tv_resolution_synchronization_start:
                String[] temp = screenValue();
                int tempResolution = ParserUtil.getScreenResolution(temp);
                if (tempResolution == screenResolution) {
                    ToastUtil.getInstance().show(SettingMouseActivity.this, getResources().getString(R.string.saved_resolution_already), false);
                    return;
                }
                screenResolution = tempResolution;
                LogUtil.d(TAG, "OnClickPositive() -> screenResolution : " + screenResolution);
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_resolution_synchronization_start)).setText(ParserUtil.getResolutionDisplay(screenResolution));
                break;
            // 저장 및 변경 버튼
            case R.id.activity_setting_mouse_tv_save_change:
                if (checkChangData()) {

                    showProgress();

                    type = Type.WRITE_SETTING;

                    tempByteArray = getUserSettingValue();

                    // 데이터 전송
                    App.getInstance().writeData(tempByteArray);
                } else {
                    ToastUtil.getInstance().show(
                            SettingMouseActivity.this,
                            getResources().getString(R.string.no_changes_have_been_made),
                            false);
                }
                finish(); //Steve_20190813 //Exit here right away!
                break;
        }

        if (checkChangData()) {
            // 저장/변경 버튼 텍스트 색상 시작
            setAnimationSave(true);
        } else {
            // 저장/변경 버튼 텍스트 색상 중지
            setAnimationSave(false);
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
            LogUtil.d(TAG, "parseUserSettingData() -> binary : " + binary + ", length : " + binary.length());

            String[] arrBody = ParserUtil.getSettingData(binary);
            if (null == arrBody) {
                LogUtil.e(TAG, "parseUserSettingData() -> arrBody is null.");
                ToastUtil.getInstance().show(
                        SettingMouseActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> arrBody : " + arrBody + ", length : " + arrBody.length);
            if (Definition.TOTAL_DATA_SETTING_BINARY_LENGTH != arrBody.length) {
                LogUtil.e(TAG, "parseUserSettingData() -> arrBody length is not invalid.");
                ToastUtil.getInstance().show(
                        SettingMouseActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }
            arrayData = arrBody;

            // 기타
            String[] temp = new String[8];
            temp[0] = arrayData[Definition.INDEX_RESERVED_1];
            temp[1] = arrayData[Definition.INDEX_RESERVED_2];
            temp[2] = arrayData[Definition.INDEX_RESERVED_3];
            temp[3] = arrayData[Definition.INDEX_RESERVED_4];
            temp[4] = arrayData[Definition.INDEX_RESERVED_5];
            temp[5] = arrayData[Definition.INDEX_RESERVED_6];
            temp[6] = arrayData[Definition.INDEX_RESERVED_7];
            temp[7] = arrayData[Definition.INDEX_RESERVED_8];

            String strReserved = "";
            for (String string : temp) {
                strReserved += string;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> strReserved : " + strReserved);
            reserved = Integer.parseInt(ParserUtil.binaryToHexadecimal(strReserved));
            LogUtil.d(TAG, "parseUserSettingData() -> reserved : " + reserved);

            // 제품 모델
            temp = new String[3];
            temp[0] = arrayData[Definition.INDEX_PRODUCE_MODEL_1];
            temp[1] = arrayData[Definition.INDEX_PRODUCE_MODEL_2];
            temp[2] = arrayData[Definition.INDEX_PRODUCE_MODEL_3];

            String strProductModel = "";
            for (String string : temp) {
                strProductModel += string;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> strProductModel : " + strProductModel);
            productModel = Integer.parseInt(ParserUtil.binaryToHexadecimal(strProductModel));
            LogUtil.d(TAG, "parseUserSettingData() -> productModel : " + productModel);

            // 휠 모드
            wheelMode = ParserUtil.getOS(arrayData[Definition.INDEX_MODE_WHEEL]);
            LogUtil.d(TAG, "parseUserSettingData() -> wheelMode : " + wheelMode);

            // 타겟 OS
            temp = new String[2];
            temp[0] = arrayData[Definition.INDEX_TARGET_OS_1];
            temp[1] = arrayData[Definition.INDEX_TARGET_OS_2];

            String strTargetOS = "";
            for (String string : temp) {
                strTargetOS += string;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> strTargetOS : " + strTargetOS);
            targetOS = Integer.parseInt(ParserUtil.binaryToHexadecimal(strTargetOS));
            LogUtil.d(TAG, "parseUserSettingData() -> targetOS : " + targetOS);

            // 동작 모드
            temp = new String[2];
            temp[0] = arrayData[Definition.INDEX_WORKING_MODE_1];
            temp[1] = arrayData[Definition.INDEX_WORKING_MODE_2];

            String strWorkingMode = "";
            for (String string : temp) {
                strWorkingMode += string;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> strWorkingMode : " + strWorkingMode);
            workingMode = Integer.parseInt(ParserUtil.binaryToHexadecimal(strWorkingMode));
            LogUtil.d(TAG, "parseUserSettingData() -> workingMode : " + workingMode);

            // 제조사
            temp = new String[8];
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

            LogUtil.d(TAG, "parseUserSettingData() -> strVendor : " + strVendor);
            vendor = Integer.parseInt(ParserUtil.binaryToHexadecimal(strVendor));
            LogUtil.d(TAG, "parseUserSettingData() -> vendor : " + vendor);

            // 해상도
            temp = new String[3];
            temp[0] = arrayData[Definition.INDEX_SCREEN_RESOLUTION_1];
            temp[1] = arrayData[Definition.INDEX_SCREEN_RESOLUTION_2];
            temp[2] = arrayData[Definition.INDEX_SCREEN_RESOLUTION_3];

            String strScreenResolution = "";
            for (String string : temp) {
                strScreenResolution += string;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> strScreenResolution : " + strScreenResolution);
            screenResolution = Integer.parseInt(ParserUtil.binaryToHexadecimal(strScreenResolution));
            LogUtil.d(TAG, "parseUserSettingData() -> workingMode : " + workingMode);

            // 모드
            mode = ParserUtil.getMode(arrayData[Definition.INDEX_MODE]);
            LogUtil.d(TAG, "parseUserSettingData() -> mode : " + mode);

            // 민감도
            temp = new String[2];
            temp[0] = arrayData[Definition.INDEX_SENSITIVE_1];
            temp[1] = arrayData[Definition.INDEX_SENSITIVE_2];

            String strSensitive = "";
            for (String string : temp) {
                strSensitive += string;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> strSensitive : " + strSensitive);
            sensitive = Integer.parseInt(ParserUtil.binaryToHexadecimal(strSensitive));
            LogUtil.d(TAG, "parseUserSettingData() -> sensitive : " + sensitive);

            // 방향
            lr = ParserUtil.getLR(arrayData[Definition.INDEX_LR]);
            LogUtil.d(TAG, "parseUserSettingData() -> lr : " + lr);

            // OS
            os = ParserUtil.getOS(arrayData[Definition.INDEX_OS]);
            LogUtil.d(TAG, "parseUserSettingData() -> os : " + os);


            // 임시데이터에 데이터 저장
            arrayTemp = arrayData;

            // OS 타입이 iOS일 경우, 임시 변수에 저장
            if (os == Definition.IOS) valueToTemp();

            findViewById(R.id.activity_setting_mouse_tv_resolution_synchronization_start).setEnabled(true);
            findViewById(R.id.activity_setting_mouse_tv_save_change).setEnabled(true);

            if (isSavedFinish) isSavedFinish = false;

            // 뷰 설정
            setView();
        }
    }

    /**
     * 사용자 정보 쓰기
     */
    private void writeUserSettingData() {
        if (null == App.getInstance().getFirmwareInformationData()) return;
        String hexadecimal = ParserUtil.byteArrayToHexadecimal(App.getInstance().getFirmwareInformationData());
        LogUtil.d(TAG, "writeUserSettingData() -> hexadecimal : " + hexadecimal);

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
            if (isResolutionSynchronization) {
                isResolutionSynchronization = false;
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_resolution_synchronization_start)).setText(ParserUtil.getResolutionDisplay(ParserUtil.getScreenResolution(screenValue())));
                ToastUtil.getInstance().show(SettingMouseActivity.this, getResources().getString(R.string.saved_resolution), false);
            } else {
                // 임시데이터에 데이터 저장
                arrayTemp = arrayData;
                if (checkChangData()) {
                    // 저장/변경 버튼 텍스트 색상 시작
                    setAnimationSave(true);
                } else {
                    // 저장/변경 버튼 텍스트 색상 중지
                    setAnimationSave(false);
                }
            }
        }
    }

    /**
     * 유저 정보 값
     *
     * @return
     */
    private byte[] getUserSettingValue() {
        byte[] value;

        // 제품 모델
        String[] arrProductModel = ParserUtil.getProductModel(productModel);

        // 타겟 OS
        String[] arrTargetOS = ParserUtil.getProductModel(targetOS);

        // 동작 모드
        String[] arrWorkingMode = ParserUtil.getWorkingMode(workingMode);

        // 민감도
        String[] arrsensitive = ParserUtil.getSensitive(sensitive);

        // 민감도
        String[] arrVendorCode = ParserUtil.getVendorCode(vendor);

        // 해상도
        String[] arrResolution = ParserUtil.getScreenResolution(screenResolution);

        for (int i = 0; i < Definition.TOTAL_DATA_SETTING_BINARY_LENGTH; i++) {

            // 제품 모델 1
            if (i == Definition.INDEX_PRODUCE_MODEL_1) {
                arrayData[i] = arrProductModel[0];
            }

            // 제품 모델 2
            if (i == Definition.INDEX_PRODUCE_MODEL_2) {
                arrayData[i] = arrProductModel[1];
            }

            // 제품 모델 3
            if (i == Definition.INDEX_PRODUCE_MODEL_3) {
                arrayData[i] = arrProductModel[2];
            }

            // 휠 모드
            if (i == Definition.INDEX_MODE_WHEEL) {
                arrayData[i] =  ParserUtil.getMouseMode(wheelMode);
            }

            // 타겟 OS 1
            if (i == Definition.INDEX_TARGET_OS_1) {
                arrayData[i] = arrTargetOS[0];
            }

            // 타겟 OS 2
            if (i == Definition.INDEX_TARGET_OS_2) {
                arrayData[i] = arrTargetOS[1];
            }

            // 동작 모드 1
            if (i == Definition.INDEX_WORKING_MODE_1) {
                arrayData[i] = arrWorkingMode[0];
            }

            // 동작 모드 2
            if (i == Definition.INDEX_WORKING_MODE_2) {
                arrayData[i] = arrWorkingMode[1];
            }

            //  제조사 코드
            if (i == Definition.INDEX_VENDOR_1) arrayData[i] = arrVendorCode[0];
            if (i == Definition.INDEX_VENDOR_2) arrayData[i] = arrVendorCode[1];
            if (i == Definition.INDEX_VENDOR_3) arrayData[i] = arrVendorCode[2];
            if (i == Definition.INDEX_VENDOR_4) arrayData[i] = arrVendorCode[3];
            if (i == Definition.INDEX_VENDOR_5) arrayData[i] = arrVendorCode[4];
            if (i == Definition.INDEX_VENDOR_6) arrayData[i] = arrVendorCode[5];
            if (i == Definition.INDEX_VENDOR_7) arrayData[i] = arrVendorCode[6];
            if (i == Definition.INDEX_VENDOR_8) arrayData[i] = arrVendorCode[7];

            // 해상도
            if (i == Definition.INDEX_SCREEN_RESOLUTION_1) arrayData[i] = arrResolution[0];
            if (i == Definition.INDEX_SCREEN_RESOLUTION_2) arrayData[i] = arrResolution[1];
            if (i == Definition.INDEX_SCREEN_RESOLUTION_3) arrayData[i] = arrResolution[2];

//            LogUtil.d(TAG, "getUserSettingValue() -> INDEX_SCREEN_RESOLUTION_1: " + Definition.INDEX_SCREEN_RESOLUTION_1 + ", arrResolution[0]: " + arrResolution[0]);
//            LogUtil.d(TAG, "getUserSettingValue() -> INDEX_SCREEN_RESOLUTION_2: " + Definition.INDEX_SCREEN_RESOLUTION_2 + ", arrResolution[1]: " + arrResolution[1]);
//            LogUtil.d(TAG, "getUserSettingValue() -> INDEX_SCREEN_RESOLUTION_3: " + Definition.INDEX_SCREEN_RESOLUTION_3 + ", arrResolution[2]: " + arrResolution[2]);

            // 마우스 모드, 키보드 모드
            if (i == Definition.INDEX_MODE) {
                arrayData[i] =  ParserUtil.getMode(mode);
            }

            // 민감도 1
            if (i == Definition.INDEX_SENSITIVE_1) {
                arrayData[i] = arrsensitive[0];
            }

            // 민감도 2
            if (i == Definition.INDEX_SENSITIVE_2) {
                arrayData[i] = arrsensitive[1];
            }

            // 사용방향
            if (i == Definition.INDEX_LR) {
                arrayData[i] = ParserUtil.getLR(lr);
            }

            // OS
            if (i == Definition.INDEX_OS) {
                arrayData[i] = ParserUtil.getOS(os);
            }
        }

        LogUtil.d(TAG, "getUserSettingValue() -> mode: " + mode);
        LogUtil.d(TAG, "getUserSettingValue() -> wheelMode: " + wheelMode);

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
     * 뷰 설정
     */
    private void setView() {
        LogUtil.i(TAG, "setView() -> Start !!!");

        // OS 스위치 버튼
        setSwitchOS();

        // OS 뷰
        setViewOS();

        // 모드 라디오 버튼
        setRadioMode();

        // 모드 뷰
        setViewMode();

        // 포인터 속도 뷰
        setViewsensitive();

        // 사용방향 스위치 버튼
        setSwitchLR();

        // 사용방향 뷰
        setViewlr();

        // 포인터 속도
        setRadiosensitive();

        // 해상도
        setScreenResolution();
    }

    /**
     * OS 뷰 설정
     * <p>
     * (IOS=0, ANDROID=1)
     */
    private void setViewOS() {
        LogUtil.i(TAG, "setViewOS() -> Start !!!");
        switch (os) {
            case Definition.ANDROID: {  // 안드로이드
                // 안드로이드 애니매이션 시작
                findViewById(R.id.activity_setting_mouse_iv_android).setBackgroundResource(R.drawable.animation_icon_android);
                animationAndroid = ( AnimationDrawable ) findViewById(R.id.activity_setting_mouse_iv_android).getBackground();
                animationAndroid.start();

                // iOS 애니메이션 중지
                if (null != animationiOS) animationiOS.stop();
                findViewById(R.id.activity_setting_mouse_iv_ios).setBackgroundResource(R.drawable.ic_ios1);
            }
            break;
            case Definition.IOS: {      // iOS
                // iOS 애니매이션 시작
                findViewById(R.id.activity_setting_mouse_iv_ios).setBackgroundResource(R.drawable.animation_icon_ios);
                animationiOS = ( AnimationDrawable ) findViewById(R.id.activity_setting_mouse_iv_ios).getBackground();
                animationiOS.start();

                // 안드로이드 애니메이션 중지
                if (null != animationAndroid) animationAndroid.stop();
                findViewById(R.id.activity_setting_mouse_iv_android).setBackgroundResource(R.drawable.ic_android1);
            }
            break;
            default: {                  // 기본 뷰
                findViewById(R.id.activity_setting_mouse_iv_android).setBackgroundResource(R.drawable.ic_android1);
                findViewById(R.id.activity_setting_mouse_iv_ios).setBackgroundResource(R.drawable.ic_ios1);
            }
            break;
        }
    }

    /**
     * 모드 뷰 설정
     * <p>
     * (MODE_KEYBOARD=0, MODE_MOUSE=1, MODE_WHEEL=2)
     */
    private void setViewMode() {
        LogUtil.i(TAG, "setViewMode() -> Start !!!");

        Typeface bold = Typeface.defaultFromStyle(Typeface.BOLD);
        Typeface normal = Typeface.defaultFromStyle(Typeface.NORMAL);

        switch (mode) {
            case Definition.KEYBOARD_MODE: {    // 키보드 모드
                // 키보드 모드 애니메이션 시작
                findViewById(R.id.activity_setting_mouse_iv_keyboard).setBackgroundResource(R.drawable.animation_icon_keyboard);
                animationKeyboard = ( AnimationDrawable ) findViewById(R.id.activity_setting_mouse_iv_keyboard).getBackground();
                animationKeyboard.start();
                // 키보드 모드 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_keyboard)).setTypeface(bold);

                // 휠 모드 애니메이션 중지
                if (null != animationWheel) animationWheel.stop();
                findViewById(R.id.activity_setting_mouse_iv_wheel).setBackgroundResource(R.drawable.ic_wheel1);
                // 휠 모드 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_wheel)).setTypeface(normal);

                // 마우스 모드 애니메이션 중지
                if (null != animationMouse) animationMouse.stop();
                findViewById(R.id.activity_setting_mouse_iv_mouse).setBackgroundResource(R.drawable.ic_mouse1);
                // 마우스 모드 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_mouse)).setTypeface(normal);
            }
            break;
            case Definition.MOUSE_MODE: {       // 마우스 모드
                switch(wheelMode) {
                    case Definition.MOUSE_WHEEL: {  // 휠 모드
                        // 휠 모드 애니메이션 시작
                        findViewById(R.id.activity_setting_mouse_iv_wheel).setBackgroundResource(R.drawable.animation_icon_wheel);
                        animationWheel = (AnimationDrawable) findViewById(R.id.activity_setting_mouse_iv_wheel).getBackground();
                        animationWheel.start();
                        // 휠 모드 텍스트 변경
                        ((TextView) findViewById(R.id.activity_setting_mouse_tv_wheel)).setTypeface(bold);

                        // 마우스 모드 애니메이션 중지
                        if (null != animationMouse) animationMouse.stop();
                        findViewById(R.id.activity_setting_mouse_iv_mouse).setBackgroundResource(R.drawable.ic_mouse1);
                        // 마우스 모드 텍스트 변경
                        ((TextView) findViewById(R.id.activity_setting_mouse_tv_mouse)).setTypeface(normal);

                        // 키보드 모드 애니메이션 중지
                        if (null != animationKeyboard) animationKeyboard.stop();
                        findViewById(R.id.activity_setting_mouse_iv_keyboard).setBackgroundResource(R.drawable.ic_keyboard1);
                        // 키보드 모드 텍스트 변경
                        ((TextView) findViewById(R.id.activity_setting_mouse_tv_keyboard)).setTypeface(normal);
                    }
                    break;
                    default: {
                        // 마우스 모드 애니메이션 시작
                        findViewById(R.id.activity_setting_mouse_iv_mouse).setBackgroundResource(R.drawable.animation_icon_mouse);
                        animationMouse = (AnimationDrawable) findViewById(R.id.activity_setting_mouse_iv_mouse).getBackground();
                        animationMouse.start();
                        // 키보드 모드 텍스트 변경
                        ((TextView) findViewById(R.id.activity_setting_mouse_tv_mouse)).setTypeface(bold);

                        // 휠 모드 애니메이션 중지
                        if (null != animationWheel) animationWheel.stop();
                        findViewById(R.id.activity_setting_mouse_iv_wheel).setBackgroundResource(R.drawable.ic_wheel1);
                        // 휠 모드 텍스트 변경
                        ((TextView) findViewById(R.id.activity_setting_mouse_tv_wheel)).setTypeface(normal);

                        // 키보드 모드 애니메이션 중지
                        if (null != animationKeyboard) animationKeyboard.stop();
                        findViewById(R.id.activity_setting_mouse_iv_keyboard).setBackgroundResource(R.drawable.ic_keyboard1);
                        // 키보드 모드 텍스트 변경
                        ((TextView) findViewById(R.id.activity_setting_mouse_tv_keyboard)).setTypeface(normal);
                    }
                    break;
                }
            }
            break;
            default: {                          // 기본 뷰
                // 키보드 모드 아이콘 이미지 변경
                findViewById(R.id.activity_setting_mouse_iv_keyboard).setBackgroundResource(R.drawable.ic_keyboard1);
                // 키보드 모드 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_keyboard)).setTypeface(normal);

                // 마우스 모드 아이콘 이미지 변경
                findViewById(R.id.activity_setting_mouse_iv_mouse).setBackgroundResource(R.drawable.ic_mouse1);
                // 마우스 모드 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_mouse)).setTypeface(normal);

                // 휠 모드 아이콘 이미지 변경
                findViewById(R.id.activity_setting_mouse_iv_wheel).setBackgroundResource(R.drawable.ic_wheel1);
                // 휠 모드 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_wheel)).setTypeface(normal);
            }
            break;
        }
    }

    /**
     * 사용방향 뷰 설정
     * <p>
     * (DIRECTION_INDEX_FINGER=0, DIRECTION_THUMB=1)
     */
    private void setViewlr() {
        LogUtil.i(TAG, "setViewlr() -> Start !!!");
        switch (lr) {
            case Definition.LR_REVERSE: {   // 사용방향 반전
                // 사용방향 검지 애니매이션 시작
                findViewById(R.id.activity_setting_mouse_iv_index_finger).setBackgroundResource(R.drawable.animation_icon_index_finger);
                animationIndexFinger = ( AnimationDrawable ) findViewById(R.id.activity_setting_mouse_iv_index_finger).getBackground();
                animationIndexFinger.start();

                // 사용방향 엄지 애니메이션 중지
                if (null != animationThumb) animationThumb.stop();
                findViewById(R.id.activity_setting_mouse_iv_thumb).setBackgroundResource(R.drawable.ic_thumb);
            }
            break;
            case Definition.LR_NORMAL: {    // 사용방향 보통
                // 사용방향 엄지 애니매이션 시작
                findViewById(R.id.activity_setting_mouse_iv_thumb).setBackgroundResource(R.drawable.animation_icon_thumb);
                animationThumb = ( AnimationDrawable ) findViewById(R.id.activity_setting_mouse_iv_thumb).getBackground();
                animationThumb.start();

                // 사용방향 검지 애니메이션 중지
                if (null != animationIndexFinger) animationIndexFinger.stop();
                findViewById(R.id.activity_setting_mouse_iv_index_finger).setBackgroundResource(R.drawable.ic_index_finger);
            }
            break;
            default: {                      // 기본 뷰
                findViewById(R.id.activity_setting_mouse_iv_index_finger).setBackgroundResource(R.drawable.ic_index_finger);
                findViewById(R.id.activity_setting_mouse_iv_thumb).setBackgroundResource(R.drawable.ic_thumb);
            }
            break;
        }
    }

    /**
     * 포인터 속도 뷰 설정
     * <p>
     * (POINTER_SPEED_SLOW=0, POINTER_SPEED_NORMAL=1, POINTER_SPEED_FAST=2)
     */
    private void setViewsensitive() {
        LogUtil.i(TAG, "setViewsensitive() -> Start !!!");

        Typeface bold = Typeface.defaultFromStyle(Typeface.BOLD);
        Typeface normal = Typeface.defaultFromStyle(Typeface.NORMAL);

        switch (sensitive) {
            case Definition.MMV_SENSITIVE_1: {   // 느림
                // 느림 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_slow)).setTypeface(bold);
                // 보통 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_normal)).setTypeface(normal);
                // 빠름 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_fast)).setTypeface(normal);
            }
            break;
            case Definition.MMV_SENSITIVE_2: { // 보통
                // 느림 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_slow)).setTypeface(normal);
                // 보통 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_normal)).setTypeface(bold);
                // 빠름 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_fast)).setTypeface(normal);
            }
            break;
            case Definition.MMV_SENSITIVE_3: {   // 빠름
                // 느림 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_slow)).setTypeface(normal);
                // 보통 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_normal)).setTypeface(normal);
                // 빠름 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_fast)).setTypeface(bold);
            }
            break;
            default: {                              // 기본 뷰
                // 느림 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_slow)).setTypeface(bold);
                // 보통 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_normal)).setTypeface(normal);
                // 빠름 텍스트 변경
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_poiner_speed_fast)).setTypeface(normal);
            }
            break;
        }
    }

    /**
     * OS 스위치 버튼
     */
    private void setSwitchOS() {
        LogUtil.i(TAG, "setSwitchOS() -> Start !!!");
        LogUtil.d(TAG, "setSwitchOS() -> os : " + os);
        switch (os) {
            case Definition.ANDROID: {  // 안드로이드
                findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).setSelected(true);
                findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).setEnabled(true);
                ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_os_on_off)).setImageResource(R.drawable.sw_on);
            }
            break;
            case Definition.IOS: {      // iOS
                findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).setSelected(false);
                findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).setEnabled(true);
                ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_os_on_off)).setImageResource(R.drawable.sw_off);
            }
            break;
            default: {                  // 기본 뷰
                ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_os_on_off)).setImageResource(R.drawable.sw_disable_off);
                findViewById(R.id.activity_setting_mouse_ibtn_os_on_off).setEnabled(false);
            }
            break;
        }
    }

    /**
     * OS 해상도 버튼
     */
    private void setScreenResolution() {
        LogUtil.i(TAG, "setScreenResolution() -> Start !!!");
        LogUtil.d(TAG, "setScreenResolution() -> screenResolution : " + screenResolution);

        if (screenResolution > 0) {
            LogUtil.d(TAG, "setScreenResolution() -> getScreenResolution : " + ParserUtil.getScreenResolution(screenValue()));
            if (screenResolution == ParserUtil.getScreenResolution(screenValue())) {
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_resolution_synchronization_start)).setText(ParserUtil.getResolutionDisplay(ParserUtil.getScreenResolution(screenValue())));
            }
        }
    }

    /**
     * 모드 라디오 버튼
     */
    private void setRadioMode() {
        LogUtil.i(TAG, "setRadioMode() -> Start !!!");
        LogUtil.d(TAG, "setRadioMode() -> mode : " + mode);
        switch (mode) {
            case Definition.KEYBOARD_MODE: {        // 키보드 모드
                findViewById(R.id.activity_setting_mouse_rb_keyboard).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_mouse).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_wheel).setEnabled(true);

                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_keyboard)).setChecked(true);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_mouse)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_wheel)).setChecked(false);
            }
            break;
            case Definition.MOUSE_MODE: {           // 마우스 모드
                switch(wheelMode) {
                    case Definition.MOUSE_WHEEL: {  // 휠 모드
                        findViewById(R.id.activity_setting_mouse_rb_keyboard).setEnabled(true);
                        findViewById(R.id.activity_setting_mouse_rb_mouse).setEnabled(true);
                        findViewById(R.id.activity_setting_mouse_rb_wheel).setEnabled(true);

                        ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_keyboard)).setChecked(false);
                        ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_mouse)).setChecked(false);
                        ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_wheel)).setChecked(true);
                    }
                    break;
                    default: {                      // 마우스 모드
                        findViewById(R.id.activity_setting_mouse_rb_keyboard).setEnabled(true);
                        findViewById(R.id.activity_setting_mouse_rb_mouse).setEnabled(true);
                        findViewById(R.id.activity_setting_mouse_rb_wheel).setEnabled(true);

                        ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_keyboard)).setChecked(false);
                        ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_mouse)).setChecked(true);
                        ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_wheel)).setChecked(false);
                    }
                    break;
                }
            }
            break;
            default: {                              // 기본 뷰
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_keyboard)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_mouse)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_wheel)).setChecked(false);

                findViewById(R.id.activity_setting_mouse_rb_keyboard).setEnabled(false);
                findViewById(R.id.activity_setting_mouse_rb_mouse).setEnabled(false);
                findViewById(R.id.activity_setting_mouse_rb_wheel).setEnabled(false);
            }
            break;
        }
    }

    /**
     * 사용방향 스위치 버튼
     */
    private void setSwitchLR() {
        LogUtil.i(TAG, "setSwitchLR() -> Start !!!");
        LogUtil.d(TAG, "setSwitchLR() -> lr : " + lr);
        switch (lr) {
            case Definition.LR_REVERSE: {
                findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).setSelected(true);
                findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).setEnabled(true);
                ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off)).setImageResource(R.drawable.sw_on);
            }
            break;
            case Definition.LR_NORMAL: {
                findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).setSelected(false);
                findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).setEnabled(true);
                ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off)).setImageResource(R.drawable.sw_off);
            }
            break;
            default:
                ((ImageView) findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off)).setImageResource(R.drawable.sw_disable_off);
                findViewById(R.id.activity_setting_mouse_ibtn_direction_on_off).setEnabled(false);
                break;
        }
    }

    /**
     * 포인터 속도 라디오 버튼
     */
    private void setRadiosensitive() {
        LogUtil.i(TAG, "setRadiosensitive() -> Start !!!");
        LogUtil.d(TAG, "setRadiosensitive() -> sensitive : " + sensitive);
        switch (sensitive) {
            case Definition.MMV_DULL: {   // 매우 느림
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast).setEnabled(true);

                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).setChecked(true);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).setChecked(false);
            }

            break;
            case Definition.MMV_SENSITIVE_1: {   // 느림
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast).setEnabled(true);

                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).setChecked(true);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).setChecked(false);
            }
            break;
            case Definition.MMV_SENSITIVE_2: { // 보통
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast).setEnabled(true);

                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).setChecked(true);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).setChecked(false);
            }
            break;
            case Definition.MMV_SENSITIVE_3: {   // 빠름
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal).setEnabled(true);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast).setEnabled(true);

                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).setChecked(true);
            }
            break;
            default: {                              // 기본 뷰
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow).setEnabled(false);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow).setEnabled(false);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal).setEnabled(false);
                findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast).setEnabled(false);

                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_very_slow)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_slow)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_normal)).setChecked(false);
                ((RadioButton) findViewById(R.id.activity_setting_mouse_rb_poiner_speed_fast)).setChecked(false);
            }
            break;
        }
    }

    /**
     * 스크린 사이즈
     */
    private String[] screenValue() {
        int screencnt = getRealScreenSize();
        String str = Integer.toBinaryString(screencnt);
        String[] screenSize = new String[3];
        int cnt = 0;
        for (int i = 0; i < 3; i++) {
            if (i >= (3 - str.length())) {
                screenSize[i] = str.substring(cnt, cnt + 1);
                cnt++;
            } else {
                screenSize[i] = "0";
            }
        }
        return screenSize;
    }

    /**
     * 실제 스크린 사이즈
     *
     * @return
     */
    private int getRealScreenSize() {
        int result = 0;
        int realHigh;
        Display display = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 17) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getRealMetrics(displayMetrics);
            realHigh = DisplaySupport(displayMetrics.heightPixels);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                Method method = Display.class.getMethod("getRawWidth");
                realHigh = ( Integer ) method.invoke(display);
            } catch (Exception e) {
                realHigh = display.getHeight();
                LogUtil.e(TAG, "getRealScreenSize() -> Exception : " + e.getLocalizedMessage());
            }
        } else {
            realHigh = display.getHeight();
        }

        if (realHigh >= Definition.DISPLAY_QHD) {
            result = 3;
        } else if ((realHigh >= Definition.DISPLAY_FHD)
                && (realHigh < Definition.DISPLAY_QHD)) {
            result = 2;
        } else if ((realHigh >= Definition.DISPLAY_HD)
                && (realHigh < Definition.DISPLAY_FHD)) {
            result = 1;
        }
        return result;
    }

    /**
     * 해상도
     *
     * @param high
     * @return
     */
    private int DisplaySupport(int high) {
        String model = Build.MODEL;
        int result = high;
        if ((high >= Definition.DISPLAY_HD)
                && (high < Definition.DISPLAY_FHD)) {
            if (Definition.SAMSUNG_EXCEPTION.equals(model)) {
                result = ( int ) (Definition.DISPLAY_HD * 1.15625);
            }
        } else if ((high >= Definition.DISPLAY_FHD)
                && (high < Definition.DISPLAY_QHD)) {
            if (Definition.SAMSUNG_EXCEPTION.equals(model)) {
                result = ( int ) (Definition.DISPLAY_FHD * 1.15625);
            }
        } else if (high >= Definition.DISPLAY_QHD) {
            if (Definition.SAMSUNG_EXCEPTION.equals(model)) {
                result = ( int ) (Definition.DISPLAY_QHD * 1.15625);
            }
        }
        return result;
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
     * 프로그레스 후처리
     */
    private void postProgress() {
        LogUtil.i(TAG, "postProgress() -> Start !!!");

        // 프로그레스 다이얼로그 종료
        dismissProgress();

        ToastUtil.getInstance().show(
                SettingMouseActivity.this,
                getResources().getString(R.string.no_connected_devices_found),
                false);
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
     * 임시변수 -> 변수
     */
    private void tempToValue() {
        if (tmpOS != -1) os = tmpOS;
        if (tmpLR != -1) lr = tmpLR;
        if (tmpSensitive != -1) sensitive = tmpSensitive;
        if (tmpMode != -1) mode = tmpMode;
        if (tmpScreenResolution != -1) screenResolution = tmpScreenResolution;
        if (tmpVendor != -1) vendor = tmpVendor;
        if (tmpWorkingMode != -1) vendor = tmpWorkingMode;
        if (tmpTargetOS != -1) targetOS = tmpTargetOS;
        if (tmpWheelMode != -1) wheelMode = tmpWheelMode;
        if (tmpProductModel != -1) productModel = tmpProductModel;
        if (tmpReserved != -1) reserved = tmpReserved;

        tmpOS = -1;
        tmpLR = -1;
        tmpSensitive = -1;
        tmpMode = -1;
        tmpScreenResolution = -1;
        tmpVendor = -1;
        tmpWorkingMode = -1;
        tmpTargetOS = -1;
        tmpWheelMode = -1;
        tmpProductModel = -1;
        tmpReserved = -1;
    }

    /**
     * 변수 -> 임시변수
     */
    private void valueToTemp() {
        LogUtil.i(TAG, "valueToTemp() -> Start !!!");

        tmpOS = os;
        tmpLR = lr;
        tmpSensitive = sensitive;
        tmpMode = mode;
        tmpScreenResolution = screenResolution;
        tmpVendor = vendor;
        tmpWorkingMode = workingMode;
        tmpTargetOS = targetOS;
        tmpWheelMode = wheelMode;
        tmpProductModel = productModel;
        tmpReserved = reserved;

        tmpOS = os;
        tmpLR = lr;
        tmpSensitive = sensitive;
        tmpMode = mode;
        tmpScreenResolution = screenResolution;
        tmpVendor = vendor;
        tmpWorkingMode = workingMode;
        tmpTargetOS = targetOS;
        tmpWheelMode = wheelMode;
        tmpProductModel = productModel;
        tmpReserved = reserved;

        mode = -1;
        lr = -1;
        sensitive = -1;
        mode = -1;
        screenResolution = -1;
        vendor = -1;
        workingMode = -1;
        targetOS = -1;
        wheelMode = -1;
        productModel = -1;
        reserved = -1;
    }

    /**
     * 저장/변경 버튼 텍스트 색상
     *
     * @param start (true:시작, false:종료)
     */
    private void setAnimationSave(boolean start) {
        if (start) {
            new Thread() {
                public void run() {
                    if (null != textChangeHandler) {
                        textChangeHandler.removeMessages(0);
                        textChangeHandler.removeCallbacksAndMessages(null);
                        textChangeHandler.sendMessage(textChangeHandler.obtainMessage(MSG_CHANGE_RED));
                    }
                }
            }.start();
        } else {
            if (null != textChangeHandler) {
                textChangeHandler.removeMessages(0);
                textChangeHandler.removeCallbacksAndMessages(null);
                ((TextView) findViewById(R.id.activity_setting_mouse_tv_save_change)).setTextColor(getResources().getColor(R.color.colorWhite));
            }
        }
    }

    /**
     * 데이터 변경 여부
     *
     * @return
     */
    private boolean checkChangData() {
        LogUtil.d(TAG, "checkChangData() -> arrayTemp : " + arrayTemp.length);
        if (arrayTemp.length != Definition.TOTAL_DATA_SETTING_BINARY_LENGTH) return false;
        // OS
        int temp = ParserUtil.getOS(arrayTemp[Definition.INDEX_OS]);
        LogUtil.d(TAG, "checkChangData() -> getOS : " + temp);
        LogUtil.d(TAG, "checkChangData() -> os : " + os);
        if (os != -1
                && os != temp) return true;

        // 모드
        temp = ParserUtil.getMode(arrayTemp[Definition.INDEX_MODE]);
        LogUtil.d(TAG, "checkChangData() -> getMode : " + temp);
        LogUtil.d(TAG, "checkChangData() -> mode : " + mode);
        if (mode != -1
                && mode != temp) return true;

        // 마우스 모드
        temp = ParserUtil.getMouseMode(arrayTemp[Definition.INDEX_MODE_WHEEL]);
        LogUtil.d(TAG, "checkChangData() -> getMouseMode : " + temp);
        LogUtil.d(TAG, "checkChangData() -> wheelMode : " + wheelMode);
        if (wheelMode != -1
                && wheelMode != temp) return true;

        // 사용방향
        temp = ParserUtil.getLR(arrayTemp[Definition.INDEX_LR]);
        LogUtil.d(TAG, "checkChangData() -> getLR : " + temp);
        LogUtil.d(TAG, "checkChangData() -> lr : " + lr);
        if (lr != -1
                && lr != temp) return true;

        String[] arrTemp = new String[2];
        arrTemp[0] = arrayData[Definition.INDEX_SENSITIVE_1];
        arrTemp[1] = arrayData[Definition.INDEX_SENSITIVE_2];
        LogUtil.d(TAG, "checkChangData() -> sensitive : " + arrTemp[0]);
        LogUtil.d(TAG, "checkChangData() -> sensitive : " + arrTemp[1]);

        // 민감도
        temp = ParserUtil.getSensitive(arrTemp);
        LogUtil.d(TAG, "checkChangData() -> sensitive : " + temp);
        LogUtil.d(TAG, "checkChangData() -> sensitive : " + sensitive);
        if (sensitive != -1
                && sensitive != temp) return true;

        // 해상도
        arrTemp = new String[3];
        arrTemp[0] = arrayData[Definition.INDEX_SCREEN_RESOLUTION_1];
        arrTemp[1] = arrayData[Definition.INDEX_SCREEN_RESOLUTION_2];
        arrTemp[2] = arrayData[Definition.INDEX_SCREEN_RESOLUTION_3];
        temp = ParserUtil.getScreenResolution(arrTemp);
        LogUtil.d(TAG, "checkChangData() -> temp : " + temp);
        LogUtil.d(TAG, "checkChangData() -> screenResolution : " + screenResolution);
        if (screenResolution != -1
                && screenResolution != temp) return true;
        // 제조사
        arrTemp = new String[8];
        arrTemp[0] = arrayData[Definition.INDEX_VENDOR_1];
        arrTemp[1] = arrayData[Definition.INDEX_VENDOR_2];
        arrTemp[2] = arrayData[Definition.INDEX_VENDOR_3];
        arrTemp[3] = arrayData[Definition.INDEX_VENDOR_4];
        arrTemp[4] = arrayData[Definition.INDEX_VENDOR_5];
        arrTemp[5] = arrayData[Definition.INDEX_VENDOR_6];
        arrTemp[6] = arrayData[Definition.INDEX_VENDOR_7];
        arrTemp[7] = arrayData[Definition.INDEX_VENDOR_8];

        LogUtil.d(TAG, "checkChangData() -> arrTemp[0] : " + arrTemp[0]);
        LogUtil.d(TAG, "checkChangData() -> arrTemp[1] : " + arrTemp[1]);
        LogUtil.d(TAG, "checkChangData() -> arrTemp[2] : " + arrTemp[2]);
        LogUtil.d(TAG, "checkChangData() -> arrTemp[3] : " + arrTemp[3]);
        LogUtil.d(TAG, "checkChangData() -> arrTemp[4] : " + arrTemp[4]);
        LogUtil.d(TAG, "checkChangData() -> arrTemp[5] : " + arrTemp[5]);
        LogUtil.d(TAG, "checkChangData() -> arrTemp[6] : " + arrTemp[6]);
        LogUtil.d(TAG, "checkChangData() -> arrTemp[7] : " + arrTemp[7]);

        String strVendor = "";
        for (String string : arrTemp) {
            strVendor += string;
        }
        LogUtil.d(TAG, "checkChangData() -> strVendor : " + strVendor);

        strVendor = ParserUtil.binaryToHexadecimal(strVendor);
        LogUtil.d(TAG, "checkChangData() -> vendor : " + vendor);

        if (StringUtil.isNotNull(strVendor)) {
            if (vendor != Integer.parseInt(strVendor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 장비 선택 엑티비티
     */
    private void goToSelectDeviceActivity() {
        LogUtil.i(TAG, "goToSelectDeviceActivity() -> Gone!!");
        Intent intent = new Intent(SettingMouseActivity.this, SelectDeviceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }
}
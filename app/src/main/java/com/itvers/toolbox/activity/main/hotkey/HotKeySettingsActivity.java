package com.itvers.toolbox.activity.main.hotkey;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.itvers.toolbox.App;
import com.itvers.toolbox.R;
import com.itvers.toolbox.activity.SelectDeviceActivity;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.RequestCode;
import com.itvers.toolbox.common.VendorType;
import com.itvers.toolbox.dialog.Dialog;
import com.itvers.toolbox.dialog.DialogDefault;
import com.itvers.toolbox.dialog.DialogInput;
import com.itvers.toolbox.dialog.DialogList;
import com.itvers.toolbox.dialog.DialogQMProgress;
import com.itvers.toolbox.item.Key;
import com.itvers.toolbox.item.Result;
import com.itvers.toolbox.item.Type;
import com.itvers.toolbox.item.UARTStatus;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ParserUtil;
import com.itvers.toolbox.util.PreferencesUtil;
import com.itvers.toolbox.util.StringUtil;
import com.itvers.toolbox.util.ToastUtil;
import com.itvers.toolbox.util.ValidationUtil;

import java.util.HashMap;

import static com.itvers.toolbox.common.Definition.VCOD_ITVERS;

public class HotKeySettingsActivity extends Activity implements
        View.OnClickListener,
        App.BlutoothListener {
    private final static String TAG = HotKeySettingsActivity.class.getSimpleName(); // 디버그 태그

    private HashMap<String, String> hmBKeyDouble = new HashMap<>();                 // B Key - Double
    private HashMap<String, String> hmBKeyLong = new HashMap<>();                   // B Key - Long
    private HashMap<String, String> hmCKeyDouble = new HashMap<>();                 // C Key - Double
    private HashMap<String, String> hmCKeyLong = new HashMap<>();                   // C Key - Long


    // 핫키 변경내용 비교용
    private HashMap<String, String> tempBKeyDouble = new HashMap<>();               // B Key - Double (임시저장)
    private HashMap<String, String> tempBKeyLong = new HashMap<>();                 // B Key - Long (임시저장)
    private HashMap<String, String> tempCKeyDouble = new HashMap<>();               // B Key - Double (임시저장)
    private HashMap<String, String> tempCKeyLong = new HashMap<>();                 // C Key - Long (임시저장)

    private int vendorCode = -1;                                                    // 제품 코드
    private int tempVendorCode = -1;                                                // 제품 코드 (임시용)

    private Handler textChangeHandler;                                              // 저정/변경 택스트 컬러 변경 핸들러
    private final int MSG_CHANGE_WHITE = 0;                                         // 핸들러 메시지 WHITE
    private final int MSG_CHANGE_RED = 1;                                           // 핸들러 메시지 RED

    private byte[] tempByteArray;                                                   // 임시 데이터
    private Type type = Type.FIRMWARE_INFORMATION;                                  // 요청 타입

    private String[] arrayData;                                                     // 데이터
    private String[] arrayTemp;                                                     // 임시 데이터

    private DialogQMProgress dialogQMProgress;                                      // 프로그레스 다이얼로그
    private static Handler progressHandler;                                         // 프로그레스 다이얼로그 핸들러

    private boolean isSave = false;                                                 // 저장 여부
    private boolean isReset = false;                                                // 리셋 여부

    private String bKeyDouble = "1";                                                // B Key - Double
    private String bKeyLong = "1";                                                  // B Key - Long
    private String cKeyDouble = "1";                                                // C Key - Double
    private String cKeyLong = "1";                                                  // C Key - Long

    /**
     * 엑티비티 결과
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() -> "
                + "requestCode : " + requestCode
                + ", resultCode : " + resultCode
                + ", data : " + data);
        if (resultCode == Activity.RESULT_OK) {
            Key key = (Key) data.getSerializableExtra(Definition.KEY_HOTKEY_TYPE);
            if (Key.APP == key) {
                String packageName = data.getStringExtra(Definition.KEY_PACKAGE_NAME);
                LogUtil.d(TAG, "onActivityResult() -> packageName: " + packageName);
                String appName = data.getStringExtra(Definition.KEY_APP_TITLE);
                LogUtil.d(TAG, "onActivityResult() -> appName: " + appName);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(key));
                hashMap.put(Definition.KEY_HOTKEY_URL, "");
                hashMap.put(Definition.KEY_HOTKEY_PACKAGE_NAME, packageName);
                hashMap.put(Definition.KEY_HOTKEY_APP_NAME, appName);
                hashMap.put(Definition.KEY_HOTKEY_PHONE, "");

                switch (requestCode) {
                    case RequestCode.ACTIVITY_REQUEST_CODE_BKEY_DOUBLE: {
                        hmBKeyDouble = hashMap;
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setText(appName);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setTextColor(Color.BLUE);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setPaintFlags(0);
                        bKeyDouble = "0";
                    }
                    break;
                    case RequestCode.ACTIVITY_REQUEST_CODE_BKEY_LONG: {
                        hmBKeyLong = hashMap;
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setText(appName);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setTextColor(Color.BLUE);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setPaintFlags(0);
                        bKeyLong = "0";
                    }
                    break;
                    case RequestCode.ACTIVITY_REQUEST_CODE_CKEY_DOUBLE: {
                        hmCKeyDouble = hashMap;
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setText(appName);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setTextColor(Color.BLUE);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(0);
                        cKeyDouble = "0";
                    }
                    break;
                    case RequestCode.ACTIVITY_REQUEST_CODE_CKEY_LONG: {
                        hmCKeyLong = hashMap;
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setText(appName);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setTextColor(Color.BLUE);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(0);
                        cKeyLong = "0";
                    }
                    break;
                    default:
                        break;
                }
            } else if (Key.PHONE == key) {
                String phoneNumber = data.getStringExtra(Definition.KEY_HOTKEY_PHONE);
                LogUtil.d(TAG, "onActivityResult() -> phoneNumber: " + phoneNumber);

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(key));
                hashMap.put(Definition.KEY_HOTKEY_URL, "");
                hashMap.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                hashMap.put(Definition.KEY_HOTKEY_APP_NAME, "");
                hashMap.put(Definition.KEY_HOTKEY_PHONE, phoneNumber);

                switch (requestCode) {
                    case RequestCode.ACTIVITY_REQUEST_CODE_BKEY_DOUBLE: {
                        hmBKeyDouble = hashMap;
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setText(phoneNumber);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setTextColor(Color.BLUE);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setPaintFlags(0);
                        bKeyDouble = "0";
                    }
                    break;
                    case RequestCode.ACTIVITY_REQUEST_CODE_BKEY_LONG: {
                        hmBKeyLong = hashMap;
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setText(phoneNumber);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setTextColor(Color.BLUE);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setPaintFlags(0);
                        bKeyLong = "0";
                    }
                    break;
                    case RequestCode.ACTIVITY_REQUEST_CODE_CKEY_DOUBLE: {
                        hmCKeyDouble = hashMap;
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setText(phoneNumber);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setTextColor(Color.BLUE);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(0);
                        cKeyDouble = "0";
                    }
                    break;
                    case RequestCode.ACTIVITY_REQUEST_CODE_CKEY_LONG: {
                        hmCKeyLong = hashMap;
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setText(phoneNumber);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setTextColor(Color.BLUE);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(0);
                        cKeyLong = "0";
                    }
                    break;
                    default:
                        break;
                }
            }

            LogUtil.d(TAG, "onActivityResult() -> checkChangData: " + checkChangData());
            if (checkChangData()) {
                // 저장/변경 버튼 텍스트 색상 시작
                setAnimationSave(true);
            } else {
                // 저장/변경 버튼 텍스트 색상 중지
                setAnimationSave(false);
            }
        }
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
            LogUtil.d(TAG, "onUARTServiceData() -> byteArray : " + byteArray);
            LogUtil.d(TAG, "onUARTServiceData() -> type : " + type);
            LogUtil.d(TAG, "onUARTServiceData() -> isSave : " + isSave);
            LogUtil.d(TAG, "onUARTServiceData() -> isReset : " + isReset);

            switch (type) {
                case FIRMWARE_INFORMATION:
                    if (byteArray.length == 5) {
                        if (isSave) {
                            ToastUtil.getInstance().show(HotKeySettingsActivity.this, getResources().getString(R.string.saved), false);
                            isSave = false;

                            // 엑티비티 종료
                            finish();
                            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                        } else if (isReset) {
                            ToastUtil.getInstance().show(HotKeySettingsActivity.this, getResources().getString(R.string.hoykey_setting_is_cleared), false);
                            isReset = false;

                            // 엑티비티 종료
                            finish();
                            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                        }
                    }

                    // 프로그레스 다이얼 로그 종료
                    dismissProgress();

                    // 데이터 읽기
                    Result result = App.getInstance().readData(byteArray, type);
                    LogUtil.d(TAG, "onUARTServiceData() -> result : " + type);
                    if (result == Result.SUCCESS) {
                        if (null != byteArray
                                && byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH
                                && null != App.getInstance().getBluetoothDevice()
                                && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getName())) {
                            App.getInstance().setFirmwareInformationData(byteArray);
                            tempByteArray = App.getInstance().getFirmwareInformationData();
                            // 사용자 정보 파싱
                            parseUserSettingData();
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
        setContentView(R.layout.activity_setting_hotkey);
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
        findViewById(R.id.activity_setting_hotkey_ll_back).setOnClickListener(this);        // 뒤로가기 버튼

        findViewById(R.id.activity_setting_hotkey_tv_akey_short).setOnClickListener(this);  // A Key - Short
        findViewById(R.id.activity_setting_hotkey_tv_akey_double).setOnClickListener(this); // A Key - Double
        findViewById(R.id.activity_setting_hotkey_tv_akey_long).setOnClickListener(this);   // A Key - Long

        findViewById(R.id.activity_setting_hotkey_tv_bkey_short).setOnClickListener(this);  // B Key - Short
        findViewById(R.id.activity_setting_hotkey_tv_bkey_double).setOnClickListener(this); // B Key - Double
        findViewById(R.id.activity_setting_hotkey_tv_bkey_long).setOnClickListener(this);   // B Key - Long

        findViewById(R.id.activity_setting_hotkey_tv_ckey_short).setOnClickListener(this);  // C Key - Short
        findViewById(R.id.activity_setting_hotkey_tv_ckey_double).setOnClickListener(this); // C Key - Double
        findViewById(R.id.activity_setting_hotkey_tv_ckey_long).setOnClickListener(this);   // C Key - Long

        findViewById(R.id.activity_setting_hotkey_tv_reset).setOnClickListener(this);       // 리셋
        findViewById(R.id.activity_setting_hotkey_tv_save).setOnClickListener(this);        // 저장

        // 뷰 세팅
//        setView();

        // 저장/변경 텍스트 변경 핸들러
        textChangeHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CHANGE_WHITE:
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_save)).setTextColor(getResources().getColor(R.color.colorWhite));
                        textChangeHandler.sendMessageDelayed(textChangeHandler.obtainMessage(MSG_CHANGE_RED), 500);
                        break;
                    case MSG_CHANGE_RED:
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_save)).setTextColor(getResources().getColor(R.color.colorRed));
                        textChangeHandler.sendMessageDelayed(textChangeHandler.obtainMessage(MSG_CHANGE_WHITE), 500);
                        break;
                    default:
                        break;
                }
            }
        };

        showProgress();

        if (null != App.getInstance().getUARTService()
                && null != App.getInstance().getBluetoothDevice()
                && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getAddress())) {
            App.getInstance().getUARTService().connect(App.getInstance().getBluetoothDevice().getAddress());
        }
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
        App.getInstance().setBlutoothListener(null, HotKeySettingsActivity.this);
        // BLE 리스너 등록
        App.getInstance().setBlutoothListener(this, HotKeySettingsActivity.this);
    }

    @Override
    public void onClick(View v) {
        LogUtil.i(TAG, "onClick() >> Start !!!");
        LogUtil.d(TAG, "onClick() >> v.getId(): " + v.getId());

        Bundle bundle = new Bundle();

        switch (v.getId()) {
            case R.id.activity_setting_hotkey_ll_back: {
                // 엑티비티 종료
                onBackPressed();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
            break;
            case R.id.activity_setting_hotkey_tv_akey_short:
            case R.id.activity_setting_hotkey_tv_akey_double:
            case R.id.activity_setting_hotkey_tv_akey_long:
            case R.id.activity_setting_hotkey_tv_bkey_short:
            case R.id.activity_setting_hotkey_tv_bkey_double:
                ToastUtil
                        .getInstance()
                        .show(HotKeySettingsActivity.this,
                                getResources().getString(R.string.can_not_be_changed),
                                false);

                break;
            // B Key - Double

            case R.id.activity_setting_hotkey_tv_ckey_short:
                int vendorType = VendorType.getVendorType(vendorCode);
                LogUtil.d(TAG, "onClick() >> vendorType : " + vendorType);

                if (vendorType == 1) {
                    ToastUtil
                            .getInstance()
                            .show(HotKeySettingsActivity.this,
                                    getResources().getString(R.string.can_not_be_changed),
                                    false);
                    return;
                }
                //sckang 다이얼로그 팝업 웹 설정
                bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_HOTKEY);
                DialogList.show(getFragmentManager(), bundle, new DialogList.ListListener() {
                    @Override
                    public void OnItemClickListener(int position, String item) {
                        LogUtil.d(TAG, "OnItemClickListener - > position : " + position + ", item : " + item);

                        if (position == 0) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_INPUT_WEB);
                            String type = hmBKeyDouble.get(Definition.KEY_HOTKEY_TYPE);

                            switch (Key.valueOf(type)) {
                                case WEB: {
                                    String url = hmBKeyDouble.get(Definition.KEY_HOTKEY_URL);
                                    if (StringUtil.isNotNull(url)) {
                                        bundle.putString(Definition.KEY_HOTKEY_URL, url);
                                    }
                                }
                                break;
                                default:
                                    break;
                            }

                            bundle.putInt(Definition.PREFERENCES_KEY_HOT_KEY_MAP_URL, Definition.TYPE_DIALOG_INPUT_WEB);
                            DialogInput.show(getFragmentManager(), bundle, new DialogInput.InputListener() {

                                @Override
                                public void OnConfirmListener(String input) {
                                    String url = input;
                                    LogUtil.d(TAG, "onClick() -> url: " + url);

                                    if (StringUtil.isNull(url)) {
                                        ToastUtil
                                                .getInstance()
                                                .show(HotKeySettingsActivity.this,
                                                        getResources().getString(R.string.hotkey_invalid_url),
                                                        false);
                                        return;
                                    }

                                    if (!url.startsWith("http")) {
                                        url = "http://" + url;
                                    }

                                    if (!ValidationUtil.isUrl(url)) {
                                        ToastUtil.getInstance().show(HotKeySettingsActivity.this,
                                                getResources().getString(R.string.hotkey_invalid_url),
                                                false);
                                        return;
                                    }
                                    hmBKeyDouble = new HashMap<>();
                                    hmBKeyDouble.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.WEB));
                                    hmBKeyDouble.put(Definition.KEY_HOTKEY_URL, url);
                                    hmBKeyDouble.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                                    hmBKeyDouble.put(Definition.KEY_HOTKEY_APP_NAME, "");
                                    hmBKeyDouble.put(Definition.KEY_HOTKEY_PHONE, "");
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setText(url);
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setPaintFlags(
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).getPaintFlags()
                                                    | Paint.UNDERLINE_TEXT_FLAG);
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setTextColor(Color.BLUE);

                                    bKeyDouble = "0";

                                    if (checkChangData()) {
                                        // 저장/변경 버튼 텍스트 색상 시작
                                        setAnimationSave(true);
                                    } else {
                                        // 저장/변경 버튼 텍스트 색상 중지
                                        setAnimationSave(false);
                                    }
                                }

                                @Override
                                public void OnCancelListener() {
                                }
                            });
                        } else if (position == 1) {
                            Intent intent = new Intent(HotKeySettingsActivity.this, HotKeyPackageActivity.class);
                            startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_BKEY_DOUBLE);
                            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        } else if (position == 2) {

                            Bundle bundle = new Bundle();
                            bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_SELECT_DUAL);
                            DialogDefault.show(getFragmentManager(), bundle, new DialogDefault.DefaultListener() {
                                @Override
                                public void OnConfirmListener() {   // 직접입력
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_INPUT_PHONE_NUMBER);

                                    String type = hmBKeyDouble.get(Definition.KEY_HOTKEY_TYPE);

                                    switch (Key.valueOf(type)) {
                                        case PHONE: {
                                            String phoneNumber = hmBKeyDouble.get(Definition.KEY_HOTKEY_PHONE);
                                            if (StringUtil.isNotNull(phoneNumber)) {
                                                bundle.putString(Definition.KEY_HOTKEY_PHONE, phoneNumber);
                                            }
                                        }
                                        break;
                                        default:
                                            break;
                                    }
                                    DialogInput.show(getFragmentManager(), bundle, new DialogInput.InputListener() {
                                        @Override
                                        public void OnConfirmListener(String input) {
                                            String phoneNumber = input;
                                            LogUtil.d(TAG, "onClick() -> phoneNumber: " + phoneNumber);

                                            if (StringUtil.isNull(phoneNumber)) {
                                                ToastUtil
                                                        .getInstance()
                                                        .show(HotKeySettingsActivity.this,
                                                                getResources().getString(R.string.hotkey_invalid_phone_number),
                                                                false);
                                                return;
                                            }

                                            if (!ValidationUtil.isCellphoneNumber(phoneNumber)
                                                    && !ValidationUtil.isTelePhoneNumber(phoneNumber)) {
                                                ToastUtil.getInstance().show(HotKeySettingsActivity.this,
                                                        getResources().getString(R.string.hotkey_invalid_phone_number),
                                                        false);
                                                return;
                                            }
                                            hmBKeyDouble = new HashMap<>();
                                            hmBKeyDouble.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.PHONE));
                                            hmBKeyDouble.put(Definition.KEY_HOTKEY_URL, "");
                                            hmBKeyDouble.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                                            hmBKeyDouble.put(Definition.KEY_HOTKEY_APP_NAME, "");
                                            hmBKeyDouble.put(Definition.KEY_HOTKEY_PHONE, phoneNumber);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setText(phoneNumber);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setTextColor(Color.BLUE);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setPaintFlags(0);

                                            bKeyDouble = "0";

                                            if (checkChangData()) {
                                                // 저장/변경 버튼 텍스트 색상 시작
                                                setAnimationSave(true);
                                            } else {
                                                // 저장/변경 버튼 텍스트 색상 중지
                                                setAnimationSave(false);
                                            }
                                        }

                                        @Override
                                        public void OnCancelListener() {

                                        }
                                    });
                                }

                                @Override
                                public void OnCancelListener() {    // 주소록
                                    Intent intent = new Intent(HotKeySettingsActivity.this, HotKeyContactsActivity.class);
                                    startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_BKEY_DOUBLE);
                                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                                }
                            });
                        }
                    }
                });
                break;
            // B Key - Long
            case R.id.activity_setting_hotkey_tv_bkey_long:
                vendorType = VendorType.getVendorType(vendorCode);
                LogUtil.d(TAG, "onClick() >> vendorType : " + vendorType);

                if (vendorType == 0) { //sckang 설정 못하도록 함
                    ToastUtil
                            .getInstance()
                            .show(HotKeySettingsActivity.this,
                                    getResources().getString(R.string.can_not_be_changed),
                                    false);
                    return;
                }


                if (vendorType == 1) {
                    ToastUtil
                            .getInstance()
                            .show(HotKeySettingsActivity.this,
                                    getResources().getString(R.string.can_not_be_changed),
                                    false);
                    return;
                }

                bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_HOTKEY);
                DialogList.show(getFragmentManager(), bundle, new DialogList.ListListener() {
                    @Override
                    public void OnItemClickListener(int position, String item) {
                        LogUtil.d(TAG, "OnItemClickListener - > position : " + position + ", item : " + item);

                        if (position == 0) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_INPUT_WEB);
                            String type = hmBKeyLong.get(Definition.KEY_HOTKEY_TYPE);

                            switch (Key.valueOf(type)) {
                                case WEB: {
                                    String url = hmBKeyLong.get(Definition.KEY_HOTKEY_URL);
                                    if (StringUtil.isNotNull(url)) {
                                        bundle.putString(Definition.KEY_HOTKEY_URL, url);
                                    }
                                }
                                break;
                                default:
                                    break;
                            }

                            bundle.putInt(Definition.PREFERENCES_KEY_HOT_KEY_MAP_URL, Definition.TYPE_DIALOG_INPUT_WEB);
                            DialogInput.show(getFragmentManager(), bundle, new DialogInput.InputListener() {

                                @Override
                                public void OnConfirmListener(String input) {
                                    String url = input;
                                    LogUtil.d(TAG, "onClick() -> url: " + url);

                                    if (StringUtil.isNull(url)) {
                                        ToastUtil
                                                .getInstance()
                                                .show(HotKeySettingsActivity.this,
                                                        getResources().getString(R.string.hotkey_invalid_url),
                                                        false);
                                        return;
                                    }

                                    if (!url.startsWith("http")) {
                                        url = "http://" + url;
                                    }

                                    if (!ValidationUtil.isUrl(url)) {
                                        ToastUtil.getInstance().show(HotKeySettingsActivity.this,
                                                getResources().getString(R.string.hotkey_invalid_url),
                                                false);
                                        return;
                                    }
                                    hmBKeyLong = new HashMap<>();
                                    hmBKeyLong.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.WEB));
                                    hmBKeyLong.put(Definition.KEY_HOTKEY_URL, url);
                                    hmBKeyLong.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                                    hmBKeyLong.put(Definition.KEY_HOTKEY_APP_NAME, "");
                                    hmBKeyLong.put(Definition.KEY_HOTKEY_PHONE, "");
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setText(url);
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setPaintFlags(
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).getPaintFlags()
                                                    | Paint.UNDERLINE_TEXT_FLAG);
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setTextColor(Color.BLUE);

                                    bKeyLong = "0";

                                    if (checkChangData()) {
                                        // 저장/변경 버튼 텍스트 색상 시작
                                        setAnimationSave(true);
                                    } else {
                                        // 저장/변경 버튼 텍스트 색상 중지
                                        setAnimationSave(false);
                                    }
                                }

                                @Override
                                public void OnCancelListener() {
                                }
                            });
                        } else if (position == 1) {
                            Intent intent = new Intent(HotKeySettingsActivity.this, HotKeyPackageActivity.class);
                            startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_BKEY_LONG);
                            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        } else if (position == 2) {

                            Bundle bundle = new Bundle();
                            bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_SELECT_DUAL);
                            DialogDefault.show(getFragmentManager(), bundle, new DialogDefault.DefaultListener() {
                                @Override
                                public void OnConfirmListener() {   // 직접입력
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_INPUT_PHONE_NUMBER);

                                    String type = hmBKeyLong.get(Definition.KEY_HOTKEY_TYPE);

                                    switch (Key.valueOf(type)) {
                                        case PHONE: {
                                            String phoneNumber = hmBKeyLong.get(Definition.KEY_HOTKEY_PHONE);
                                            if (StringUtil.isNotNull(phoneNumber)) {
                                                bundle.putString(Definition.KEY_HOTKEY_PHONE, phoneNumber);
                                            }
                                        }
                                        break;
                                        default:
                                            break;
                                    }
                                    DialogInput.show(getFragmentManager(), bundle, new DialogInput.InputListener() {
                                        @Override
                                        public void OnConfirmListener(String input) {
                                            String phoneNumber = input;
                                            LogUtil.d(TAG, "onClick() -> phoneNumber: " + phoneNumber);

                                            if (StringUtil.isNull(phoneNumber)) {
                                                ToastUtil
                                                        .getInstance()
                                                        .show(HotKeySettingsActivity.this,
                                                                getResources().getString(R.string.hotkey_invalid_phone_number),
                                                                false);
                                                return;
                                            }

                                            if (!ValidationUtil.isCellphoneNumber(phoneNumber)
                                                    && !ValidationUtil.isTelePhoneNumber(phoneNumber)) {
                                                ToastUtil.getInstance().show(HotKeySettingsActivity.this,
                                                        getResources().getString(R.string.hotkey_invalid_phone_number),
                                                        false);
                                                return;
                                            }
                                            hmBKeyLong = new HashMap<>();
                                            hmBKeyLong.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.PHONE));
                                            hmBKeyLong.put(Definition.KEY_HOTKEY_URL, "");
                                            hmBKeyLong.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                                            hmBKeyLong.put(Definition.KEY_HOTKEY_APP_NAME, "");
                                            hmBKeyLong.put(Definition.KEY_HOTKEY_PHONE, phoneNumber);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setText(phoneNumber);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setTextColor(Color.BLUE);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setPaintFlags(0);

                                            bKeyLong = "0";

                                            if (checkChangData()) {
                                                // 저장/변경 버튼 텍스트 색상 시작
                                                setAnimationSave(true);
                                            } else {
                                                // 저장/변경 버튼 텍스트 색상 중지
                                                setAnimationSave(false);
                                            }
                                        }

                                        @Override
                                        public void OnCancelListener() {

                                        }
                                    });
                                }

                                @Override
                                public void OnCancelListener() {    // 주소록
                                    Intent intent = new Intent(HotKeySettingsActivity.this, HotKeyContactsActivity.class);
                                    startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_BKEY_LONG);
                                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.activity_setting_hotkey_tv_ckey_double:
                vendorType = VendorType.getVendorType(vendorCode);
                LogUtil.d(TAG, "onClick() >> vendorType : " + vendorType);

                if (vendorType == 2) {
                    ToastUtil
                            .getInstance()
                            .show(HotKeySettingsActivity.this,
                                    getResources().getString(R.string.can_not_be_changed),
                                    false);
                    return;
                }

                bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_HOTKEY);//sckang 핫키 설정
                DialogList.show(getFragmentManager(), bundle, new DialogList.ListListener() {
                    @Override
                    public void OnItemClickListener(int position, String item) {
                        LogUtil.d(TAG, "OnItemClickListener - > position : " + position + ", item : " + item);

                        if (position == 0) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_INPUT_WEB);
                            String type = hmCKeyDouble.get(Definition.KEY_HOTKEY_TYPE);

                            switch (Key.valueOf(type)) {
                                case WEB: {
                                    String url = hmCKeyDouble.get(Definition.KEY_HOTKEY_URL);
                                    if (StringUtil.isNotNull(url)) {
                                        bundle.putString(Definition.KEY_HOTKEY_URL, url);
                                    }
                                }
                                break;
                                default:
                                    break;
                            }

                            bundle.putInt(Definition.PREFERENCES_KEY_HOT_KEY_MAP_URL, Definition.TYPE_DIALOG_INPUT_WEB);
                            DialogInput.show(getFragmentManager(), bundle, new DialogInput.InputListener() {

                                @Override
                                public void OnConfirmListener(String input) {
                                    String url = input;
                                    LogUtil.d(TAG, "onClick() -> url: " + url);

                                    if (StringUtil.isNull(url)) {
                                        ToastUtil
                                                .getInstance()
                                                .show(HotKeySettingsActivity.this,
                                                        getResources().getString(R.string.hotkey_invalid_url),
                                                        false);
                                        return;
                                    }

                                    if (!url.startsWith("http")) {
                                        url = "http://" + url;
                                    }

                                    if (!ValidationUtil.isUrl(url)) {
                                        ToastUtil.getInstance().show(HotKeySettingsActivity.this,
                                                getResources().getString(R.string.hotkey_invalid_url),
                                                false);
                                        return;
                                    }
                                    hmCKeyDouble = new HashMap<>();
                                    hmCKeyDouble.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.WEB));
                                    hmCKeyDouble.put(Definition.KEY_HOTKEY_URL, url);
                                    hmCKeyDouble.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                                    hmCKeyDouble.put(Definition.KEY_HOTKEY_APP_NAME, "");
                                    hmCKeyDouble.put(Definition.KEY_HOTKEY_PHONE, "");
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setText(url);
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).getPaintFlags()
                                                    | Paint.UNDERLINE_TEXT_FLAG);
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setTextColor(Color.BLUE);

                                    cKeyDouble = "0";

                                    if (checkChangData()) {
                                        // 저장/변경 버튼 텍스트 색상 시작
                                        setAnimationSave(true);
                                    } else {
                                        // 저장/변경 버튼 텍스트 색상 중지
                                        setAnimationSave(false);
                                    }
                                }

                                @Override
                                public void OnCancelListener() {
                                }
                            });
                        } else if (position == 1) {
                            Intent intent = new Intent(HotKeySettingsActivity.this, HotKeyPackageActivity.class);
                            startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_CKEY_DOUBLE);
                            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        } else if (position == 2) {

                            Bundle bundle = new Bundle();
                            bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_SELECT_DUAL);
                            DialogDefault.show(getFragmentManager(), bundle, new DialogDefault.DefaultListener() {
                                @Override
                                public void OnConfirmListener() {   // 직접입력
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_INPUT_PHONE_NUMBER);

                                    String type = hmCKeyDouble.get(Definition.KEY_HOTKEY_TYPE);

                                    switch (Key.valueOf(type)) {
                                        case PHONE: {
                                            String phoneNumber = hmCKeyDouble.get(Definition.KEY_HOTKEY_PHONE);
                                            if (StringUtil.isNotNull(phoneNumber)) {
                                                bundle.putString(Definition.KEY_HOTKEY_PHONE, phoneNumber);
                                            }
                                        }
                                        break;
                                        default:
                                            break;
                                    }
                                    DialogInput.show(getFragmentManager(), bundle, new DialogInput.InputListener() {
                                        @Override
                                        public void OnConfirmListener(String input) {
                                            String phoneNumber = input;
                                            LogUtil.d(TAG, "onClick() -> phoneNumber: " + phoneNumber);

                                            if (StringUtil.isNull(phoneNumber)) {
                                                ToastUtil
                                                        .getInstance()
                                                        .show(HotKeySettingsActivity.this,
                                                                getResources().getString(R.string.hotkey_invalid_phone_number),
                                                                false);
                                                return;
                                            }

                                            if (!ValidationUtil.isCellphoneNumber(phoneNumber)
                                                    && !ValidationUtil.isTelePhoneNumber(phoneNumber)) {
                                                ToastUtil.getInstance().show(HotKeySettingsActivity.this,
                                                        getResources().getString(R.string.hotkey_invalid_phone_number),
                                                        false);
                                                return;
                                            }
                                            hmCKeyDouble = new HashMap<>();
                                            hmCKeyDouble.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.PHONE));
                                            hmCKeyDouble.put(Definition.KEY_HOTKEY_URL, "");
                                            hmCKeyDouble.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                                            hmCKeyDouble.put(Definition.KEY_HOTKEY_APP_NAME, "");
                                            hmCKeyDouble.put(Definition.KEY_HOTKEY_PHONE, phoneNumber);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setText(phoneNumber);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setTextColor(Color.BLUE);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(0);

                                            cKeyDouble = "0";

                                            if (checkChangData()) {
                                                // 저장/변경 버튼 텍스트 색상 시작
                                                setAnimationSave(true);
                                            } else {
                                                // 저장/변경 버튼 텍스트 색상 중지
                                                setAnimationSave(false);
                                            }
                                        }

                                        @Override
                                        public void OnCancelListener() {

                                        }
                                    });
                                }

                                @Override
                                public void OnCancelListener() {    // 주소록
                                    Intent intent = new Intent(HotKeySettingsActivity.this, HotKeyContactsActivity.class);
                                    startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_CKEY_DOUBLE);
                                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.activity_setting_hotkey_tv_ckey_long:
                vendorType = VendorType.getVendorType(vendorCode);
                LogUtil.d(TAG, "onClick() >> vendorType : " + vendorType);

                if (vendorType == 3) {
                    ToastUtil
                            .getInstance()
                            .show(HotKeySettingsActivity.this,
                                    getResources().getString(R.string.can_not_be_changed),
                                    false);
                    return;
                }

                bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_HOTKEY);
                DialogList.show(getFragmentManager(), bundle, new DialogList.ListListener() {
                    @Override
                    public void OnItemClickListener(int position, String item) {
                        LogUtil.d(TAG, "OnItemClickListener - > position : " + position + ", item : " + item);

                        if (position == 0) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_INPUT_WEB);
                            String type = hmCKeyLong.get(Definition.KEY_HOTKEY_TYPE);

                            switch (Key.valueOf(type)) {
                                case WEB: {
                                    String url = hmCKeyLong.get(Definition.KEY_HOTKEY_URL);
                                    if (StringUtil.isNotNull(url)) {
                                        bundle.putString(Definition.KEY_HOTKEY_URL, url);
                                    }
                                }
                                break;
                                default:
                                    break;
                            }

                            bundle.putInt(Definition.PREFERENCES_KEY_HOT_KEY_MAP_URL, Definition.TYPE_DIALOG_INPUT_WEB);
                            DialogInput.show(getFragmentManager(), bundle, new DialogInput.InputListener() {

                                @Override
                                public void OnConfirmListener(String input) {
                                    String url = input;
                                    LogUtil.d(TAG, "onClick() -> url: " + url);

                                    if (StringUtil.isNull(url)) {
                                        ToastUtil
                                                .getInstance()
                                                .show(HotKeySettingsActivity.this,
                                                        getResources().getString(R.string.hotkey_invalid_url),
                                                        false);
                                        return;
                                    }

                                    if (!url.startsWith("http")) {
                                        url = "http://" + url;
                                    }

                                    if (!ValidationUtil.isUrl(url)) {
                                        ToastUtil.getInstance().show(HotKeySettingsActivity.this,
                                                getResources().getString(R.string.hotkey_invalid_url),
                                                false);
                                        return;
                                    }
                                    hmCKeyLong = new HashMap<>();
                                    hmCKeyLong.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.WEB));
                                    hmCKeyLong.put(Definition.KEY_HOTKEY_URL, url);
                                    hmCKeyLong.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                                    hmCKeyLong.put(Definition.KEY_HOTKEY_APP_NAME, "");
                                    hmCKeyLong.put(Definition.KEY_HOTKEY_PHONE, "");
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setText(url);
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).getPaintFlags()
                                                    | Paint.UNDERLINE_TEXT_FLAG);
                                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setTextColor(Color.BLUE);

                                    cKeyLong = "0";

                                    if (checkChangData()) {
                                        // 저장/변경 버튼 텍스트 색상 시작
                                        setAnimationSave(true);
                                    } else {
                                        // 저장/변경 버튼 텍스트 색상 중지
                                        setAnimationSave(false);
                                    }
                                }

                                @Override
                                public void OnCancelListener() {
                                }
                            });
                        } else if (position == 1) {
                            Intent intent = new Intent(HotKeySettingsActivity.this, HotKeyPackageActivity.class);
                            startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_CKEY_LONG);
                            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                        } else if (position == 2) {

                            Bundle bundle = new Bundle();
                            bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_SELECT_DUAL);
                            DialogDefault.show(getFragmentManager(), bundle, new DialogDefault.DefaultListener() {
                                @Override
                                public void OnConfirmListener() {   // 직접입력
                                    Bundle bundle = new Bundle();
                                    bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_INPUT_PHONE_NUMBER);

                                    String type = hmCKeyLong.get(Definition.KEY_HOTKEY_TYPE);

                                    switch (Key.valueOf(type)) {
                                        case PHONE: {
                                            String phoneNumber = hmCKeyLong.get(Definition.KEY_HOTKEY_PHONE);
                                            if (StringUtil.isNotNull(phoneNumber)) {
                                                bundle.putString(Definition.KEY_HOTKEY_PHONE, phoneNumber);
                                            }
                                        }
                                        break;
                                        default:
                                            break;
                                    }
                                    DialogInput.show(getFragmentManager(), bundle, new DialogInput.InputListener() {
                                        @Override
                                        public void OnConfirmListener(String input) {
                                            String phoneNumber = input;
                                            LogUtil.d(TAG, "onClick() -> phoneNumber: " + phoneNumber);

                                            if (StringUtil.isNull(phoneNumber)) {
                                                ToastUtil
                                                        .getInstance()
                                                        .show(HotKeySettingsActivity.this,
                                                                getResources().getString(R.string.hotkey_invalid_phone_number),
                                                                false);
                                                return;
                                            }

                                            if (!ValidationUtil.isCellphoneNumber(phoneNumber)
                                                    && !ValidationUtil.isTelePhoneNumber(phoneNumber)) {
                                                ToastUtil.getInstance().show(HotKeySettingsActivity.this,
                                                        getResources().getString(R.string.hotkey_invalid_phone_number),
                                                        false);
                                                return;
                                            }
                                            hmCKeyLong = new HashMap<>();
                                            hmCKeyLong.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.PHONE));
                                            hmCKeyLong.put(Definition.KEY_HOTKEY_URL, "");
                                            hmCKeyLong.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                                            hmCKeyLong.put(Definition.KEY_HOTKEY_APP_NAME, "");
                                            hmCKeyLong.put(Definition.KEY_HOTKEY_PHONE, phoneNumber);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setText(phoneNumber);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setTextColor(Color.BLUE);
                                            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(0);

                                            cKeyLong = "0";

                                            if (checkChangData()) {
                                                // 저장/변경 버튼 텍스트 색상 시작
                                                setAnimationSave(true);
                                            } else {
                                                // 저장/변경 버튼 텍스트 색상 중지
                                                setAnimationSave(false);
                                            }
                                        }

                                        @Override
                                        public void OnCancelListener() {

                                        }
                                    });
                                }

                                @Override
                                public void OnCancelListener() {    // 주소록
                                    Intent intent = new Intent(HotKeySettingsActivity.this, HotKeyContactsActivity.class);
                                    startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_CKEY_LONG);
                                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.activity_setting_hotkey_tv_reset: {
                bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_RESET_HOTKEY);
                DialogDefault.show(getFragmentManager(), bundle, new DialogDefault.DefaultListener() {
                    @Override
                    public void OnConfirmListener() {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.NONE));
                        hashMap.put(Definition.KEY_HOTKEY_URL, "");
                        hashMap.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                        hashMap.put(Definition.KEY_HOTKEY_APP_NAME, "");
                        hashMap.put(Definition.KEY_HOTKEY_PHONE, "");

                        PreferencesUtil.getInstance(HotKeySettingsActivity.this).setBkeyDouble(hashMap);
                        PreferencesUtil.getInstance(HotKeySettingsActivity.this).setBkeyLong(hashMap);
                        PreferencesUtil.getInstance(HotKeySettingsActivity.this).setCkeyDouble(hashMap);
                        PreferencesUtil.getInstance(HotKeySettingsActivity.this).setCkeyLong(hashMap);

                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(0);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(0);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(0);
                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(0);

                        bKeyDouble = "1";
                        bKeyLong = "1";
                        cKeyDouble = "1";
                        cKeyLong = "1";

                        App.getInstance().writeData(getUserSettingValue());
                        isReset = true;
                    }

                    @Override
                    public void OnCancelListener() {

                    }
                });
                break;
            }
            case R.id.activity_setting_hotkey_tv_save: {
                ///////////////////////////////////////////////////////////////////////////////////
                //  The assumption is that all the C Hotkeys are pre-registered by the Vendor
                //  So, if not, we should change below code.
                ///////////////////////////////////////////////////////////////////////////////////
                if (vendorCode > 0) {
                    ToastUtil
                            .getInstance()
                            .show(HotKeySettingsActivity.this,
                                    getResources().getString(R.string.hotkey_cannot_saved),
                                    false);
                    return;
                }

                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setBkeyDouble(hmBKeyDouble);
                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setBkeyLong(hmBKeyLong);
                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setCkeyDouble(hmCKeyDouble);
                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setCkeyLong(hmCKeyLong);

                tempBKeyDouble =  hmBKeyDouble;
                tempBKeyLong =  hmBKeyLong;
                tempCKeyDouble =  hmCKeyDouble;
                tempCKeyLong =  hmCKeyLong;

//                ToastUtil
//                        .getInstance()
//                        .show(HotKeySettingsActivity.this,
//                                getResources().getString(R.string.new_hotkey_is_registered),
//                                false);

                LogUtil.d(TAG, "onClick() -> getBkeyDouble(): " + PreferencesUtil.getInstance(HotKeySettingsActivity.this).getBkeyDouble());
                LogUtil.d(TAG, "onClick() -> getBkeyLong(): " + PreferencesUtil.getInstance(HotKeySettingsActivity.this).getBkeyLong());
                LogUtil.d(TAG, "onClick() -> getCkeyDouble(): " + PreferencesUtil.getInstance(HotKeySettingsActivity.this).getCkeyDouble());
                LogUtil.d(TAG, "onClick() -> getCkeyLong(): " + PreferencesUtil.getInstance(HotKeySettingsActivity.this).getCkeyLong());
//                onBackPressed(); //Steve_20190813 //Exit here right away!

                App.getInstance().writeData(getUserSettingValue());

                isSave = true;

                break;
            }
            // DEFAULT
            default:
                break;
        }
    }

    /**
     * 뷰 세팅
     */
    private void setView() {
        vendorCode = PreferencesUtil.getInstance(HotKeySettingsActivity.this).getVendorCode();
        tempVendorCode = vendorCode;
        if (vendorCode == VCOD_ITVERS) { //Steve_20190723 //Only ITVERS hotkey is defined at this moment!!
            switch (vendorCode) {
                case VCOD_ITVERS:
//                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_short)).setHint("ITVERS Home Page");
//                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setHint("MyQM");
//                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setHint("Customer Service");
                    break;
//                case VCOD_KT_MNS:
//                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_short)).setHint("KT M&S");
//                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setHint("고객센터 연결");
//                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setHint("마이 KT");
//                    break;
                default:
                    break;
            }

        } else {
            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setHintTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setHintTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setHintTextColor(Color.BLACK);
            ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setHintTextColor(Color.BLACK);

            boolean isSetHotKey = PreferencesUtil.getInstance(HotKeySettingsActivity.this).getIsSetHotKey();
            LogUtil.d(TAG, "onCreate() >> isSetHotKey: " + isSetHotKey);

            if (!isSetHotKey) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(Definition.KEY_HOTKEY_TYPE, String.valueOf(Key.NONE));
                hashMap.put(Definition.KEY_HOTKEY_URL, "");
                hashMap.put(Definition.KEY_HOTKEY_PACKAGE_NAME, "");
                hashMap.put(Definition.KEY_HOTKEY_APP_NAME, "");
                hashMap.put(Definition.KEY_HOTKEY_PHONE, "");

                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setBkeyDouble(hashMap);
                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setBkeyLong(hashMap);
                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setCkeyDouble(hashMap);
                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setCkeyLong(hashMap);

                PreferencesUtil.getInstance(HotKeySettingsActivity.this).setIsSetHotKey(true);
            }

            isSetHotKey = PreferencesUtil.getInstance(HotKeySettingsActivity.this).getIsSetHotKey();
            LogUtil.d(TAG, "onCreate() >> isSetHotKey: " + isSetHotKey);

            if (isSetHotKey) {
                ////////////////////////////////////////////////////////////////////////////////////
                //
                //                             User Hotkey for B Double Key
                //
                ////////////////////////////////////////////////////////////////////////////////////
                hmBKeyDouble = PreferencesUtil.getInstance(HotKeySettingsActivity.this).getBkeyDouble();
                tempBKeyDouble = hmBKeyDouble;
                LogUtil.d(TAG, "onCreate() >> hmBKeyDouble: " + hmBKeyDouble);
                if ((hmBKeyDouble.containsKey(Definition.KEY_HOTKEY_TYPE))
                        && (hmBKeyDouble.containsKey(Definition.KEY_HOTKEY_URL))
                        && (hmBKeyDouble.containsKey(Definition.KEY_HOTKEY_APP_NAME))
                        && (hmBKeyDouble.containsKey(Definition.KEY_HOTKEY_PACKAGE_NAME))
                        && (hmBKeyDouble.containsKey(Definition.KEY_HOTKEY_PHONE))) {

                    String type = hmBKeyDouble.get(Definition.KEY_HOTKEY_TYPE);
                    String url = hmBKeyDouble.get(Definition.KEY_HOTKEY_URL);
                    String appName = hmBKeyDouble.get(Definition.KEY_HOTKEY_APP_NAME);
                    String packageName = hmBKeyDouble.get(Definition.KEY_HOTKEY_PACKAGE_NAME);
                    String phone = hmBKeyDouble.get(Definition.KEY_HOTKEY_PHONE);

                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setText("");

                    switch (Key.valueOf(type)) {
                        case WEB: {
                            if (StringUtil.isNotNull(url)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setText(url);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setPaintFlags(
                                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).getPaintFlags()
                                                | Paint.UNDERLINE_TEXT_FLAG);
                                bKeyDouble = "0";
                            }
                        }
                        break;
                        case APP: {
                            if (StringUtil.isNotNull(packageName)
                                    && StringUtil.isNotNull(appName)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setText(appName);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setPaintFlags(0);
                                bKeyDouble = "0";
                            }
                        }
                        break;
                        case PHONE: {
                            if (StringUtil.isNotNull(phone)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setText(phone);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_double)).setPaintFlags(0);
                                bKeyDouble = "0";
                            }
                        }
                        break;
                        default:
                            break;
                    }
                }

                ////////////////////////////////////////////////////////////////////////////////////
                //
                //                             User Hotkey for B Long Key
                //
                ////////////////////////////////////////////////////////////////////////////////////
                hmBKeyLong = PreferencesUtil.getInstance(HotKeySettingsActivity.this).getBkeyLong();
                tempBKeyLong = hmBKeyLong;
                LogUtil.d(TAG, "onCreate() >> hmBKeyLong: " + hmBKeyLong);
                if ((hmBKeyLong.containsKey(Definition.KEY_HOTKEY_TYPE))
                        && (hmBKeyLong.containsKey(Definition.KEY_HOTKEY_URL))
                        && (hmBKeyLong.containsKey(Definition.KEY_HOTKEY_APP_NAME))
                        && (hmBKeyLong.containsKey(Definition.KEY_HOTKEY_PACKAGE_NAME))
                        && (hmBKeyLong.containsKey(Definition.KEY_HOTKEY_PHONE))) {

                    String type = hmBKeyLong.get(Definition.KEY_HOTKEY_TYPE);
                    String url = hmBKeyLong.get(Definition.KEY_HOTKEY_URL);
                    String appName = hmBKeyLong.get(Definition.KEY_HOTKEY_APP_NAME);
                    String packageName = hmBKeyLong.get(Definition.KEY_HOTKEY_PACKAGE_NAME);
                    String phone = hmBKeyLong.get(Definition.KEY_HOTKEY_PHONE);

                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setText("");

                    switch (Key.valueOf(type)) {
                        case WEB: {
                            if (StringUtil.isNotNull(url)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setText(url);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setPaintFlags(
                                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).getPaintFlags()
                                                | Paint.UNDERLINE_TEXT_FLAG);
                                bKeyLong = "0";
                            }
                        }
                        break;
                        case APP: {
                            if (StringUtil.isNotNull(packageName)
                                    && StringUtil.isNotNull(appName)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setText(appName);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setPaintFlags(0);
                                bKeyLong = "0";
                            }
                        }
                        break;
                        case PHONE: {
                            if (StringUtil.isNotNull(phone)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setText(phone);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_bkey_long)).setPaintFlags(0);
                                bKeyLong = "0";
                            }
                        }
                        break;
                        default:
                            break;
                    }
                }

                ////////////////////////////////////////////////////////////////////////////////////
                //
                //                             User Hotkey for C Double Key
                //
                ////////////////////////////////////////////////////////////////////////////////////
                hmCKeyDouble = PreferencesUtil.getInstance(HotKeySettingsActivity.this).getCkeyDouble();
                tempCKeyDouble = hmCKeyDouble;

                LogUtil.d(TAG, "onCreate() >> hmCKeyDouble: " + hmCKeyDouble);
                if ((hmCKeyDouble.containsKey(Definition.KEY_HOTKEY_TYPE))
                        && (hmCKeyDouble.containsKey(Definition.KEY_HOTKEY_URL))
                        && (hmCKeyDouble.containsKey(Definition.KEY_HOTKEY_APP_NAME))
                        && (hmCKeyDouble.containsKey(Definition.KEY_HOTKEY_PACKAGE_NAME))
                        && (hmCKeyDouble.containsKey(Definition.KEY_HOTKEY_PHONE))) {

                    String type = hmCKeyDouble.get(Definition.KEY_HOTKEY_TYPE);
                    String url = hmCKeyDouble.get(Definition.KEY_HOTKEY_URL);
                    String appName = hmCKeyDouble.get(Definition.KEY_HOTKEY_APP_NAME);
                    String packageName = hmCKeyDouble.get(Definition.KEY_HOTKEY_PACKAGE_NAME);
                    String phone = hmCKeyDouble.get(Definition.KEY_HOTKEY_PHONE);

                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setText("");

                    switch (Key.valueOf(type)) {
                        case WEB: {
                            if (StringUtil.isNotNull(url)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setText(url);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(
                                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).getPaintFlags()
                                                | Paint.UNDERLINE_TEXT_FLAG);
                                cKeyDouble = "0";
                            }
                        }
                        break;
                        case APP: {
                            if (StringUtil.isNotNull(packageName)
                                    && StringUtil.isNotNull(appName)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setText(appName);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(0);
                                cKeyDouble = "0";
                            }
                        }
                        break;
                        case PHONE: {
                            if (StringUtil.isNotNull(phone)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setText(phone);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_double)).setPaintFlags(0);
                                cKeyDouble = "0";
                            }
                        }
                        break;
                        default:
                            break;
                    }
                }

                ////////////////////////////////////////////////////////////////////////////////////
                //
                //                             User Hotkey for C Long Key
                //
                ////////////////////////////////////////////////////////////////////////////////////
                hmCKeyLong = PreferencesUtil.getInstance(HotKeySettingsActivity.this).getCkeyLong();
                tempCKeyLong = hmCKeyLong;

                LogUtil.d(TAG, "onCreate() >> hmCKeyLong: " + hmCKeyLong);
                if ((hmCKeyLong.containsKey(Definition.KEY_HOTKEY_TYPE))
                        && (hmCKeyLong.containsKey(Definition.KEY_HOTKEY_URL))
                        && (hmCKeyLong.containsKey(Definition.KEY_HOTKEY_APP_NAME))
                        && (hmCKeyLong.containsKey(Definition.KEY_HOTKEY_PACKAGE_NAME))
                        && (hmCKeyLong.containsKey(Definition.KEY_HOTKEY_PHONE))) {

                    String type = hmCKeyLong.get(Definition.KEY_HOTKEY_TYPE);
                    String url = hmCKeyLong.get(Definition.KEY_HOTKEY_URL);
                    String appName = hmCKeyLong.get(Definition.KEY_HOTKEY_APP_NAME);
                    String packageName = hmCKeyLong.get(Definition.KEY_HOTKEY_PACKAGE_NAME);
                    String phone = hmCKeyLong.get(Definition.KEY_HOTKEY_PHONE);

                    ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setText("");

                    switch (Key.valueOf(type)) {
                        case WEB: {
                            if (StringUtil.isNotNull(url)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setText(url);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(
                                        ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).getPaintFlags()
                                                | Paint.UNDERLINE_TEXT_FLAG);
                                cKeyLong = "0";
                            }
                        }
                        break;
                        case APP: {
                            if (StringUtil.isNotNull(packageName)
                                    && StringUtil.isNotNull(appName)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setText(appName);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(0);
                                cKeyLong = "0";
                            }
                        }
                        break;
                        case PHONE: {
                            if (StringUtil.isNotNull(phone)) {
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setText(phone);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setTextColor(Color.BLUE);
                                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_ckey_long)).setPaintFlags(0);
                                cKeyLong = "0";
                            }
                        }
                        break;
                        default:
                            break;
                    }
                }
            }
            App.getInstance().writeData(getUserSettingValue());
        }
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

    /**
     * 데이터 변경 여부
     *
     * @return
     */
    private boolean checkChangData() {
        LogUtil.d(TAG, "checkChangData() -> Start !!!");
        if (hmBKeyLong != tempBKeyLong) { return true; }
        if (hmBKeyDouble != tempBKeyDouble) { return true; }
        if (hmCKeyDouble != tempCKeyDouble) { return true; }
        if (hmCKeyLong != tempCKeyLong) { return true; }
        return false;
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
                ((TextView) findViewById(R.id.activity_setting_hotkey_tv_save)).setTextColor(getResources().getColor(R.color.colorWhite));
            }
        }
    }

    // 백버튼
    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        if (checkChangData()) {
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
                                    findViewById(R.id.activity_setting_hotkey_tv_save).performClick();
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
            return;
        }

        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    /**
     * 장비 선택 엑티비티
     */
    private void goToSelectDeviceActivity() {
        LogUtil.i(TAG, "goToSelectDeviceActivity() -> Gone!!");
        Intent intent = new Intent(HotKeySettingsActivity.this, SelectDeviceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
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
                HotKeySettingsActivity.this,
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
                        HotKeySettingsActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> arrBody : " + arrBody + ", length : " + arrBody.length);
            if (Definition.TOTAL_DATA_SETTING_BINARY_LENGTH != arrBody.length) {
                LogUtil.e(TAG, "parseUserSettingData() -> arrBody length is not invalid.");
                ToastUtil.getInstance().show(
                        HotKeySettingsActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }
            arrayData = arrBody;

            // 기타
            String[] temp = new String[4];
            temp[0] = arrayData[Definition.INDEX_RESERVED_1];
            temp[1] = arrayData[Definition.INDEX_RESERVED_2];
            temp[2] = arrayData[Definition.INDEX_RESERVED_3];
            temp[3] = arrayData[Definition.INDEX_RESERVED_4];

            String strReserved = "";
            for (String string : temp) {
                strReserved += string;
            }
            LogUtil.d(TAG, "parseUserSettingData() -> strReserved : " + strReserved);
//            reserved = Integer.parseInt(ParserUtil.binaryToHexadecimal(strReserved));
//            LogUtil.d(TAG, "parseUserSettingData() -> reserved : " + reserved);

            temp = new String[1];
            temp[0] = arrayData[Definition.INDEX_RESERVED_5];
            LogUtil.d(TAG, "parseUserSettingData() -> strReserved : " + temp[0]);

            temp = new String[1];
            temp[0] = arrayData[Definition.INDEX_RESERVED_6];
            LogUtil.d(TAG, "parseUserSettingData() -> strReserved : " + temp[0]);

            temp = new String[1];
            temp[0] = arrayData[Definition.INDEX_RESERVED_7];
            LogUtil.d(TAG, "parseUserSettingData() -> strReserved : " + temp[0]);

            temp = new String[1];
            temp[0] = arrayData[Definition.INDEX_RESERVED_8];
            LogUtil.d(TAG, "parseUserSettingData() -> strReserved : " + temp[0]);

            // 제품 모델
            temp = new String[3];
            temp[0] = arrayData[Definition.INDEX_PRODUCE_MODEL_1];
            temp[1] = arrayData[Definition.INDEX_PRODUCE_MODEL_2];
            temp[2] = arrayData[Definition.INDEX_PRODUCE_MODEL_3];
        }

        // 뷰 설정
        setView();
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

    /**
     * 유저 정보 값
     *
     * @return
     */
    private byte[] getUserSettingValue() {
        byte[] value;
        for (int i = 0; i < Definition.TOTAL_DATA_SETTING_BINARY_LENGTH; i++) {

            // B Key Double
            if (i == Definition.INDEX_RESERVED_5) {
                arrayData[i] = bKeyDouble;
            }

            // B Key Long
            if (i == Definition.INDEX_RESERVED_6) {
                arrayData[i] = bKeyLong;
            }

            // C Key Double
            if (i == Definition.INDEX_RESERVED_7) {
                arrayData[i] = cKeyDouble;
            }

            // C Key Long
            if (i == Definition.INDEX_RESERVED_8) {
                arrayData[i] =  cKeyLong;
            }
        }
        LogUtil.d(TAG, "getUserSettingValue() -> bKeyDouble: " + bKeyDouble);
        LogUtil.d(TAG, "getUserSettingValue() -> bKeyLong: " + bKeyLong);
        LogUtil.d(TAG, "getUserSettingValue() -> cKeyDouble: " + cKeyDouble);
        LogUtil.d(TAG, "getUserSettingValue() -> cKeyLong: " + cKeyLong);

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
}

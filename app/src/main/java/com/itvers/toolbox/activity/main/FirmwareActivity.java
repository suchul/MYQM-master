package com.itvers.toolbox.activity.main;

import android.annotation.SuppressLint;

import android.app.LoaderManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.Loader;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.itvers.toolbox.App;
import com.itvers.toolbox.R;
import com.itvers.toolbox.activity.SelectDeviceActivity;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.RequestCode;
import com.itvers.toolbox.item.Scan;
import com.itvers.toolbox.item.UARTStatus;
import com.itvers.toolbox.dialog.Dialog;
import com.itvers.toolbox.dialog.DialogQMProgress;
import com.itvers.toolbox.scanner.ScannerFragment;
import com.itvers.toolbox.service.DFUService;
import com.itvers.toolbox.util.IntentUtil;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ParserUtil;
import com.itvers.toolbox.util.PreferencesUtil;
import com.itvers.toolbox.util.StringUtil;
import com.itvers.toolbox.util.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import no.nordicsemi.android.dfu.DfuLogListener;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class FirmwareActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks <Cursor>,
        View.OnClickListener,
        App.BlutoothListener,
        ScannerFragment.ScannerFragmentListener {

    private final static String TAG = FirmwareActivity.class.getSimpleName();   // 디버그 태그

    private DialogQMProgress dialogQMProgress;                                  // 프로그레스 다이얼로그
    private int activityType = Definition.ACTIVITY_MODE_PATCH;                  // 엑티비티 타입 (ACTIVITY_MODE_PATCH, ACTIVITY_MODE_MOUSE)
    private static Handler updateHandler;                                       // 업데이트 핸들러
    private static Handler progressHandler;                                     // 프로그레스 핸들러
    private Runnable progressRunnable;                                          // 프로그레스 런에이블
    private final int MSG_UPDATE_ON = 1;                                        // 핸들러 메시지 UPDATE ON
    private final int MSG_UPDATE_OFF = 0;                                       // 핸들러 메시지 UPDATE OFF
    private String filePath;                                                    // 파일 경로
    private boolean isUpgrading;                                                // 업그레이드 여부
    private ScannerFragment scannerFragment = null;                             // 기기 스캔 플래그먼트
    private ArrayList <Integer> listProgress = new ArrayList();                 // 프로그레스 리스트
    private TimerTask upgradeTimeTask;
    private Timer upgradeTimer;

    @Override
    public void onUARTServiceChange(UARTStatus status) {
        LogUtil.d(TAG, "onUARTServiceChange() -> status : " + status);
        switch (status) {
            case SERVICE_CONNECTED: {
                if (null != App.getInstance().getUARTService()) {
                    BluetoothDevice bluetoothDevice = App.getInstance().getUARTService().addBondedDevice(activityType);
                    if (null != bluetoothDevice
                            && StringUtil.isNotNull(bluetoothDevice.getAddress())) {
                        App.getInstance().getUARTService().connect(bluetoothDevice.getAddress());
                    }
                }
            }
            break;
            case DEVICE_DOES_NOT_SUPPORT_UART:
                // 프로그레스 다이얼 로그 종료
                dismissProgress();
                // 프로그레스 다이얼로그 핸들러 중지
                stopProgressHandler();
                break;
            case SERVICE_DISCONNECTED:
            case SERVICE_BINDING_DIED:
            case ERROR_EMPTY_UART_SERVICE:
            case ERROR_UART_SERVICE_INITIALIZATION:
            case ERROR_UART_SERVICE_CONNECT:
                // 프로그레스 다이얼 로그 종료
                dismissProgress();
                break;
            case GATT_CONNECTED: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 데이터 쓰기
                        writeData(false);
                    }
                }, Definition.GATT_INTERVAL_TIME); // 2초 후에 실행
            }
            break;
            case GATT_DISCONNECTED:
                // 프로그레스 다이얼 로그 종료
                dismissProgress();
                // 스캐너 플래그먼트 종료
                dismissFragmentScanner();
                break;
            case GATT_SERVICES_DISCOVERED:
                break;
            case DATA_AVAILABLE:
                break;
            case EMPTY_DEVICE:
            case EMPTY_DEVICE_ADDRESS:
                // 프로그레스 다이얼 로그 종료
                dismissProgress();
                // 스캐너 플래그먼트 종료
                dismissFragmentScanner();
                break;
        }
    }

    @Override
    public void onUARTServiceData(Intent intent) {
        if (null != intent) {
            final byte[] byteArray = intent.getByteArrayExtra(Definition.EXTRA_DATA);
            try {
                // 데이터 읽기
                readData(byteArray);
            } catch (Exception e) {
                LogUtil.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    /**
     * DFU 로그 리스너
     */
    private final DfuLogListener dfuLogListener = new DfuLogListener() {
        @Override
        public void onLogEvent(String bluetoothDeviceAddress, int level, String message) {
            LogUtil.d(TAG, "onLogEvent() -> level : " + String.valueOf(level) + ", message : " + message);
        }
    };

    /**
     * DFU 프로그레스 리스너
     */
    private DfuProgressListener dfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String bluetoothDeviceAddress) {
            LogUtil.i(TAG, "onDeviceConnecting() -> Start !!!");
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(getResources().getString(R.string.dfu_service_device_connecting));
        }

        @Override
        public void onDfuProcessStarting(final String bluetoothDeviceAddress) {
            LogUtil.i(TAG, "onDfuProcessStarting() -> Start !!!");
            LoadingStatus(true);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextColor(getResources().getColor(R.color.colorDFUText));
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(R.string.firmware_status_starting);
        }

        @Override
        public void onEnablingDfuMode(final String bluetoothDeviceAddress) {
            LogUtil.i(TAG, "onEnablingDfuMode() -> Start !!!");
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(getResources().getString(R.string.dfu_service_dfu_mode));
        }

        @Override
        public void onFirmwareValidating(final String bluetoothDeviceAddress) {
            LogUtil.i(TAG, "onFirmwareValidating() -> Start !!!");
            LoadingStatus(true);
        }

        @Override
        public void onDeviceDisconnecting(final String bluetoothDeviceAddress) {
            LogUtil.i(TAG, "onDeviceDisconnecting() -> Start !!!");
            if (((TextView) findViewById(R.id.activity_firmware_tv_update_state)).getText().equals("100")) {
            } else {
                LoadingStatus(false);
            }
        }

        @Override
        public void onDfuCompleted(final String bluetoothDeviceAddress) {
            LogUtil.i(TAG, "onDfuCompleted() -> Start !!!");

            listProgress.clear();

            // 업데이트 핸들러 중지
            stopUpdateHandler();

            ToastUtil.getInstance().show(
                    FirmwareActivity.this,
                    getResources().getString(R.string.dfu_service_update_success),
                    false);

            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextColor(getResources().getColor(R.color.colorDFUText));
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(R.string.firmware_status_success);
            ((TextView) findViewById(R.id.activity_firmware_tv_comment)).setText(R.string.firmware_comment_upgrade);
            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(R.string.installed_latest_version);

            ((ImageView) findViewById(R.id.activity_firmware_iv_update)).setImageResource(R.drawable.bt_firmware_off);
            ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.bt_firmware_normal);

            findViewById(R.id.activity_firmware_fl_update).setEnabled(false);
//            Dialog.getInstance().showSingle(
//                    FirmwareActivity.this,
//                    getResources().getString(R.string.notice),
//                    getResources().getString(R.string.firmware_upgrade_is_completed),
//                    getResources().getString(R.string.confirm),
//                    false,
//                    new Dialog.DialogOnClickListener() {
//                        @Override
//                        public void OnItemClickResult(HashMap <String, Object> hashMap) {
//                            int result = ( int ) hashMap.get(Definition.KEY_DIALOG_SINGLE);
//                            if (result == Definition.DIALOG_BUTTON_POSITIVE) {
//                                // 엑티비티 종료
//                                finish();
//                                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
//
//                                goToSelectDeviceActivity();
//                            }
//                        }
//                    });
            finish();
        }

        @Override
        public void onDfuAborted(final String bluetoothDeviceAddress) {
            LogUtil.i(TAG, "onDfuAborted() -> Start !!!");

            LoadingStatus(false);

            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(R.string.firmware_status_fail);

            // UART Timer 중지
            stopUpgradeTimer();
        }

        @Override
        public void onProgressChanged(final String bluetoothDeviceAddress,
                                      final int percent,
                                      final float speed,
                                      final float avgSpeed,
                                      final int currentPart,
                                      final int partsTotal) {
            LogUtil.i(TAG, "onProgressChanged() -> Start !!!");

            // UART Timer 중지
            stopUpgradeTimer();

            // 프로그래스 다이얼로그 중지
            dismissProgress();

            if (listProgress.size() == 0) {
                listProgress.add(percent);
            } else {
                for (int i : listProgress) {
                    if (i == percent) {
                        LogUtil.i(TAG, "onProgressChanged() -> i : " + i + ", percent : " + percent);
                        return;
                    }
                }
                listProgress.add(Integer.valueOf(percent));
            }

            ((TextView) findViewById(R.id.activity_firmware_tv_comment)).setText(R.string.firmware_comment_upgrading);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextColor(getResources().getColor(R.color.colorDFUText));
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(String.valueOf(percent) + "%");

            // 업데이트 진행상황
            getProgress(percent);
        }

        @Override
        public void onError(final String bluetoothDeviceAddress,
                            final int error,
                            final int errorType,
                            final String message) {
            //LogUtil.i(TAG, "onError() -> Start !!!");
            LogUtil.e(TAG, "onError() -> error : " + error + ", errorType : " + errorType + ", message : " + message);

            // UART Timer 중지
            stopUpgradeTimer();

            listProgress.clear();

            // 프로그래스 다이얼로그 중지
            dismissProgress();

            LoadingStatus(false);

            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(R.string.firmware_status_fail);
        }
    };

    @Override
    public void onDeviceSelected(BluetoothDevice bluetoothDevice, String deviceName) {
        LogUtil.d(TAG, "onDeviceSelected() -> deviceName : " + deviceName);
        if (StringUtil.isNull(deviceName)
                || StringUtil.isNull(bluetoothDevice.getAddress())) {
            ToastUtil.getInstance().show(
                    FirmwareActivity.this,
                    getResources().getString(R.string.unknown_error),
                    false);
            return;
        }

        // 프로그레스 다이얼로그 시작
        showProgress();

        App.getInstance().getUARTService().connect(bluetoothDevice.getAddress());

        if (deviceName.startsWith(Definition.DEVICE_NAME_DFU)) {

            ((TextView) findViewById(R.id.activity_firmware_tv_selected_device)).setText(deviceName);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(R.string.firmware_status_update);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextColor(getResources().getColor(R.color.colorDFUText));
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);

            findViewById(R.id.activity_firmware_fl_update).setEnabled(true);

            // 업데이트 핸들러 시작
            startUpdateHandler();

            // Upgrade Timer 시작
            startUpgradeTimer();
        }
    }

    @Override
    public void onDialogCanceled() {
        LogUtil.d(TAG, "onDialogCanceled() -> Start !!!");

        ToastUtil.getInstance().show(
                FirmwareActivity.this,
                getResources().getString(R.string.empty_selected_device),
                false);

        dismissFragmentScanner();

        goToSelectDeviceActivity();
    }

    @Override
    public void onDialogCreate() {
        LogUtil.d(TAG, "onDialogCreate() -> Start !!!");
    }

    @Override
    public void onDialogDismissed() {
        scannerFragment = null;
        LogUtil.d(TAG, "onDialogDismissed() -> Start !!!");
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firmware);

        // 인텐트 타입
        Intent intent = getIntent();
        activityType = intent.getIntExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
        boolean forceful_dfu_mode = intent.getBooleanExtra("DFU_MODE", false);
        LogUtil.d(TAG, "onCreate() -> activityType : " + activityType);

        if (null != intent) {
            if(!forceful_dfu_mode) { //Steve_20191230 //For forceful DFU Mode support!!!
                byte[] byteArray = App.getInstance().getFirmwareInformationData();
                if (null != byteArray) {
                    if (byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH) {
                        // 레벨 데이터 파싱
                        int batteryLevel = parseBatteryLevelData();
                        if (10 > batteryLevel) {
                            LogUtil.d(TAG, "writeData() -> batteryLevel : " + batteryLevel);
                            ToastUtil.getInstance().show(
                                    FirmwareActivity.this,
                                    String.format(getResources().getString(R.string.firmware_update_low_battery), "Smart Patch", String.valueOf(batteryLevel) + "%"),
                                    false);
                            return;
                        }
                    }
                }
            }
        }

        // 뒤로가기 버튼
        findViewById(R.id.activity_firmware_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 엑티비티 종료
                finish();
            }
        });

        filePath = copyFileFromAssets();
        LogUtil.d(TAG, "onCreate() -> filePath : " + filePath);

        File file = new File(filePath);
        LogUtil.d(TAG, "onCreate() -> file : " + file.exists());
        if (!file.exists()) {
            ToastUtil.getInstance().show(
                    FirmwareActivity.this,
                    getResources().getString(R.string.firmware_update_file_not_found),
                    false);
            return;
        }

        // 업데이트 이미지 핸들러
        updateHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_ON:
                        ((ImageView) findViewById(R.id.activity_firmware_iv_update)).setImageResource(R.drawable.bt_firmware_on1);
                        updateHandler.sendMessageDelayed(updateHandler.obtainMessage(MSG_UPDATE_OFF), 1000);
                        break;
                    case MSG_UPDATE_OFF:
                        ((ImageView) findViewById(R.id.activity_firmware_iv_update)).setImageResource(R.drawable.bt_firmware_on2);
                        updateHandler.sendMessageDelayed(updateHandler.obtainMessage(MSG_UPDATE_ON), 1000);
                        break;
                    default:
                        break;

                }
            }
        };

        // 로그 리스너 등록
        DfuServiceListenerHelper.registerLogListener(FirmwareActivity.this, dfuLogListener);
        // 프로그레스 리스너 등록
        DfuServiceListenerHelper.registerProgressListener(FirmwareActivity.this, dfuProgressListener);


        // 프로그레스 다이얼로그 핸들러
        if (null == progressHandler) progressHandler = new Handler();
        progressRunnable = new Runnable() {
            public void run() {
                postProgress();
            }
        };
        progressHandler.postDelayed(progressRunnable, Definition.PROGRESS_DURATION);

        // 펌웨어 버전
        String firmwareVersion = PreferencesUtil.getInstance(FirmwareActivity.this).getFirmwareVersion();

        if(forceful_dfu_mode) firmwareVersion = "DFU!!!"; //For forceful DFU Mode!!!

        LogUtil.d(TAG, "onCreate() -> firmwareVersion : " + firmwareVersion);
        if (StringUtil.isNotNull(firmwareVersion)) {
            // 버전 정보 설명
            String description = getVersionDescription(firmwareVersion);
            LogUtil.d(TAG, "onCreate() -> description : " + description);
            if (StringUtil.isNotNull(description)) {
                final String msg = description;
                runOnUiThread(new Runnable() {
                    public void run() {
                        ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(msg);
                    }
                });
            }
            showFragmentScanner(Scan.DFU_ONLY);
        }

        // UART 리스너 등록
        App.getInstance().setBlutoothListener(this, FirmwareActivity.this);

        if (null != App.getInstance().getUARTService()
                && null != App.getInstance().getBluetoothDevice()
                && null != App.getInstance().getBluetoothDevice().getName()
                && null != App.getInstance().getBluetoothDevice().getAddress()) {

            ((TextView) findViewById(R.id.activity_firmware_tv_selected_device)).setText(App.getInstance().getBluetoothDevice().getName());

            boolean isEmergency = intent.getBooleanExtra(Definition.KEY_IS_EMERGENCY, false);
            if (isEmergency) {
                showFragmentScanner(Scan.DFU_ONLY);
            } else {
                // 프로그래스 다이얼로그
                showProgress();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 데이터 쓰기
                        writeData(false);
                    }
                }, Definition.GATT_INTERVAL_TIME); // 2초 후에 실행
            }
        }

        findViewById(R.id.activity_firmware_tv_select_device).setOnClickListener(this);     // 기기 선택 버튼
        findViewById(R.id.activity_firmware_tv_update_state).setOnClickListener(this);      // 업데이트 상태 버튼
        findViewById(R.id.activity_firmware_fl_update).setOnClickListener(this);            // 펌웨어 업데이트 버튼
        findViewById(R.id.activity_firmware_fl_update).setEnabled(false);                   // 펌웨어 업데이트 버튼 클릭 방지

        // 뒤로 가기 버튼
        findViewById(R.id.activity_firmware_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpgrading) return;
                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume() -> Start !!!");
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
    }

    @Override
    protected void onDestroy() {
        LogUtil.i(TAG, "onDestroy() -> Start !!!");
        super.onDestroy();

        // 프로그레스 다이얼로그 중지
        dismissProgress();

        // 스캔 플래그먼트 중지
        dismissFragmentScanner();

        // 업데이트 이미지 핸들러 중지
        stopUpdateHandler();

        if (null != dfuProgressListener) {
            DfuServiceListenerHelper.unregisterProgressListener(this, dfuProgressListener);
            dfuProgressListener = null;
        }

        runOnUiThread(new Runnable() {
            public void run() {
                if (scannerFragment != null) {
                    scannerFragment.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        LogUtil.i(TAG, "onBackPressed() -> Start !!!");
//        super.onBackPressed();
        // 업데이트중이면,
        if (isUpgrading) return;
        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    /**
     * 기기 스캔 다이얼로그
     */
    private void showFragmentScanner(final Scan scan) {
        LogUtil.i(TAG, "showFragmentScanner() -> Start !!!");

        // 프로그레스 다이얼로그 종료
        dismissProgress();

        LogUtil.d(TAG, "showFragmentScanner() -> getIsBackground : " + App.getInstance().getIsBackground());
        // 백그라운드 여부
        if (App.getInstance().getIsBackground()) return;

        // BLE 어댑터 사용 여부 체크
        if (null != App.getInstance().getBluetoothAdapter()) {
            if (!App.getInstance().getBluetoothAdapter().isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RequestCode.REQUEST_ENABLE_BLUTOOTH);
                return;
            }
        }

        runOnUiThread(new Runnable() {
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putInt(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
                bundle.putSerializable(Definition.KEY_INTENT_IS_FIRMWARE, scan);

                scannerFragment = ScannerFragment.newInstance(bundle, FirmwareActivity.this);
                scannerFragment.show(getFragmentManager(), Definition.SCAN_FRAGMENT);
            }
        });
    }

    /**
     * 기기 스캔 다이얼로그 종료
     */
    private void dismissFragmentScanner() {
        LogUtil.i(TAG, "dismissFragmentScanner() -> Start !!!");
        if (null != scannerFragment) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (null != scannerFragment) scannerFragment.dismiss();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_firmware_fl_update:
            case R.id.activity_firmware_tv_update_state:
                updateHandler.removeMessages(0);
                updateHandler.removeCallbacksAndMessages(null);
                onUploadClicked();
                break;
            case R.id.activity_firmware_tv_select_device:
                if (isUpgrading) return;
                // 기기 스캔 다이얼로그
                if (null == scannerFragment) showFragmentScanner(Scan.DEVICE_DFU);
                break;
            default:
                break;
        }
    }

    /**
     * 업데이트 파일 복사
     */
    public String copyFileFromAssets() {
        LogUtil.i(TAG, "copyFileFromAssets() -> Start !!!");
        AssetManager assetManager = this.getAssets();
        String fileName;

        if (activityType == Definition.ACTIVITY_MODE_PATCH) {
            fileName = Definition.FILE_PATH_SMART_PATCH;
        } else {
            fileName = Definition.FILE_PATH_MOUSE;
        }
        String filePath = null;

        InputStream is;
        OutputStream os;
        try {
            filePath = getFilesDir().getAbsolutePath() + File.separator + fileName;

            is = assetManager.open(fileName);
            os = new FileOutputStream(filePath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            is.close();
            os.flush();
            os.close();
        } catch (IOException ioe) {
            LogUtil.i(TAG, "copyFileFromAssets() -> IOException : " + ioe.getLocalizedMessage());
        }
        return filePath;
    }

    /**
     * 업데이트
     */
    public void onUploadClicked() {
        if (IntentUtil.isRunningService(FirmwareActivity.this, DFUService.class.getName()))
            return;

        LogUtil.d(TAG, "onUploadClicked() -> filePath : " + filePath);
        if (filePath == null) return;

        Dialog.getInstance().showDual(
                this,
                getResources().getString(R.string.warning),
                getResources().getString(R.string.firmware_upgrade_warning),
                getResources().getString(R.string.confirm),
                getResources().getString(R.string.close),
                false,
                new Dialog.DialogOnClickListener() {
                    @Override
                    public void OnItemClickResult(HashMap <String, Object> hashMap) {
                        int result = ( int ) hashMap.get(Definition.KEY_DIALOG_DUAL);
                        switch (result) {
                            case Definition.DIALOG_BUTTON_POSITIVE: {
                                // 데이터 전송
                                if (null != App.getInstance().getUARTService()) {
                                    File file = new File(filePath);
                                    LogUtil.d(TAG, "onUploadClicked() -> file : " + file.exists());
                                    if (!file.exists()) {
                                        // 프로그래서 다이얼로그 중지
                                        dismissProgress();

                                        ToastUtil.getInstance().show(
                                                FirmwareActivity.this,
                                                getResources().getString(R.string.firmware_update_file_not_found),
                                                false);
                                        return;
                                    }

                                    // 프로그레스 다이얼로그 시작
                                    showProgress();

                                    int numberOfPackets;
                                    try {
                                        numberOfPackets = Integer.parseInt(String.valueOf(DfuServiceInitiator.DEFAULT_PRN_VALUE));
                                    } catch (final NumberFormatException e) {
                                        numberOfPackets = DfuServiceInitiator.DEFAULT_PRN_VALUE;
                                    }

                                    if (null == App.getInstance().getUARTService()
                                            || null == App.getInstance().getBluetoothDevice()
                                            || null == App.getInstance().getBluetoothDevice().getName()
                                            || null == App.getInstance().getBluetoothDevice().getAddress()) {
                                        ToastUtil.getInstance().show(
                                                FirmwareActivity.this,
                                                getResources().getString(R.string.ble_connect_error),
                                                false);
                                        return;
                                    }
                                    final DfuServiceInitiator dfuServiceInitiator = new DfuServiceInitiator(App.getInstance().getBluetoothDevice().getAddress())
                                            .setDeviceName(Definition.DEVICE_NAME_DFU)
                                            .setKeepBond(false)
                                            .setForceDfu(false)
                                            .setPacketsReceiptNotificationsEnabled(false)
                                            .setPacketsReceiptNotificationsValue(numberOfPackets)
                                            .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)

                                            // 2019-06-17 DFU 옵션 추가
                                            .setForeground(false)
                                            .setDisableNotification(true)
                                            ;

                                    dfuServiceInitiator.setZip(null, filePath);
                                    dfuServiceInitiator.start(FirmwareActivity.this, DFUService.class);
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
        return;
    }

    @Override
    public Loader <Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.d(TAG, "onCreateLoader() -> Start !!!");
        return null;
    }

    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor data) {
        LogUtil.d(TAG, "onLoadFinished() -> Start !!!");
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
        LogUtil.d(TAG, "onCreateLoader() -> Start !!!");
    }

    /**
     * 업데이트 진행상황
     */
    private void getProgress(int rate) {
        LogUtil.i(TAG, "getProgress() -> Start !!!");

        int value = rate / 10;
        LogUtil.d(TAG, "getProgress() -> value : " + value);

        if (value == 10) {
            // 프로그레스 다이얼로그 시작
            showProgress();
        }

        switch (value) {
            case 0:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_10); //Steve_20191121
                break;
            case 1:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_10);
                break;
            case 2:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_20);
                break;
            case 3:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_30);
                break;
            case 4:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_40);
                break;
            case 5:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_50);
                break;
            case 6:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_60);
                break;
            case 7:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_70);
                break;
            case 8:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_80);
                break;
            case 9:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_90);
                break;
            case 10:
                ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_100);
                break;
            default:
                break;
        }
    }

    /**
     * 업데이트 상태
     */
    private void LoadingStatus(boolean isRunning) {
        LogUtil.i(TAG, "LoadingStatus() -> Start !!!");

        if (!isRunning) {
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextColor(getResources().getColor(R.color.colorDFUText));
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);

            ((ImageView) findViewById(R.id.activity_firmware_iv_update)).setImageResource(R.drawable.bt_firmware_off);
            ((ImageView) findViewById(R.id.activity_firmware_iv_update_gauge)).setImageResource(R.drawable.ugrade_gauge_0);

            ((TextView) findViewById(R.id.activity_firmware_tv_selected_device)).setText("");
            ((TextView) findViewById(R.id.activity_firmware_tv_selected_device)).setHint(R.string.select_device);

            findViewById(R.id.activity_firmware_fl_update).setEnabled(false);

            // 업그레이드 여부 설정
            isUpgrading = false;
        } else {
            // 업그레이드 여부 설정
            isUpgrading = true;
        }
    }

    /**
     * 데이터 쓰기
     */
    private void writeData(boolean isRequestDFUMode) {
        LogUtil.i(TAG, "writeData() -> Start !!!");
        byte[] value; //Steve_20191129  //byte=>unsigned byte

        LogUtil.d(TAG, "writeData() -> isRequestDFUMode : " + isRequestDFUMode);
        if (isRequestDFUMode) {
            value = ParserUtil.requestJumpBootloader();
        } else {
            value = ParserUtil.requestFirmwareVersionNBatteryLevel();
            LogUtil.d(TAG, "writeData() -> value : " + value);

            // TODO 단말기 배터리 잔량 체크
//            int batteryLevel = DeviceUtil.getDeviceBatteryLevel(FirmwareActivity.this);
//            LogUtil.d(TAG, "writeData() -> batteryLevel : " + batteryLevel);
//            if (10 > batteryLevel) {
//                LogUtil.d(TAG, "writeData() -> batteryLevel : " + batteryLevel);
//                ToastUtil.getInstance().show(
//                        FirmwareActivity.this,
//                        String.format(getResources().getString(R.string.firmware_update_low_battery), "Smart Patch", String.valueOf(batteryLevel) + "%"),
//                        false);
//                return;
//            }

            if (null == App.getInstance().getUARTService()) {
                LogUtil.e(TAG, "writeData() -> uartService is null.");
                // 프로그레스 다이얼로그 중지
                dismissProgress();

                ToastUtil.getInstance().show(
                        FirmwareActivity.this,
                        getResources().getString(R.string.ble_connect_error),
                        false);
                return;
            }
            if (null == value) {
                LogUtil.e(TAG, "writeData() -> value is null.");
                // 프로그레스 다이얼로그 중지
                dismissProgress();

                ToastUtil.getInstance().show(
                        FirmwareActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }
            if (value.length == 0) {
                LogUtil.e(TAG, "writeData() -> value length is 0.");
                // 프로그레스 다이얼로그 중지
                dismissProgress();

                ToastUtil.getInstance().show(
                        FirmwareActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }
            LogUtil.d(TAG, "writeData() -> value : " + value.length);
        }

        App.getInstance().getUARTService().writeRXCharacteristic(value);
        App.getInstance().getUARTService().readRXCharacteristic();
    }

    /**
     * 데이터 읽기
     */
    private void readData(final byte[] byteArray) {
        LogUtil.i(TAG, "readData() -> Start !!!");

        // 프로그레스 다이얼로그 핸들러 중지
        dismissProgress();

        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    LogUtil.d(TAG, "readData() -> byteArray : " + byteArray + ", length : " + byteArray.length);

                    if (null == byteArray) {
                        LogUtil.e(TAG, "readData() -> byteArray is null.");
                        ToastUtil.getInstance().show(
                                FirmwareActivity.this,
                                getResources().getString(R.string.unknown_error),
                                false);
                        return;
                    }
                    if (byteArray.length != Definition.TOTAL_DATA_FIRMWAVER_LENGTH) {
                        LogUtil.e(TAG, "readData() -> byteArray length is not invalid. [" + byteArray.length + "]");
                        ToastUtil.getInstance().show(
                                FirmwareActivity.this,
                                getResources().getString(R.string.unknown_error),
                                false);
                        return;
                    }

                    ((TextView) findViewById(R.id.activity_firmware_tv_selected_device)).setText(App.getInstance().getBluetoothDevice().getName());

                    String hexadecimal = ParserUtil.byteArrayToHexadecimal(byteArray);
                    LogUtil.d(TAG, "readData() -> hexadecimal : " + hexadecimal);

                    String[] array = hexadecimal.split("\\p{Z}");
                    LogUtil.d(TAG, "readData() -> array : " + array);
                    for (String string : array) {
                        LogUtil.d(TAG, "readData() -> string : " + string);
                    }

                    // 유효성 체크
                    String stx = array[0];
                    LogUtil.d(TAG, "STEVE(Rx) : readData() -> stx : " + stx);
                    String length = array[2];
                    LogUtil.d(TAG, "STEVE(Rx) : readData() -> length : " + length);
                    String etx = array[8];
                    LogUtil.d(TAG, "STEVE(Rx) : readData() -> etx : " + etx);

                    String command = array[1];
                    LogUtil.d(TAG, "STEVE(Rxed Command) : readData() -> command : " + command);

                    // 펨웨어 업데이트
                    if (Definition.RESPONSE_COMMAND_FIRMWARE_UPDATE.equals(command)) {
                        // 에러 코드
                        int error = byteArray[3]; //Battery Level이 20 미만인 경우 '1'(F/W Upgrade 진행), 20% 이상인 경우 '0'(F/W Upgrade진행 불가)
                        LogUtil.d(TAG, "STEVE( : readData() -> error : " + error);

                        boolean success = PreferencesUtil.getInstance(FirmwareActivity.this).clearFirmwareVersion();
                        LogUtil.d(TAG, "readData() -> clearFirmwareVersion : " + success);

                        switch (error) {
                            case 0:
                                // 펌웨어 버전
                                String firmwareVersion = ParserUtil.getFirmwareVersion(byteArray);
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> firmwareVersion : " + firmwareVersion);

                                String versionString = firmwareVersion;
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> versionString : " + versionString);

                                versionString = versionString.replaceAll("\\.", "");
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> versionString : " + versionString);

                                int versionCode = Integer.parseInt(versionString);
                                if (versionCode <= Definition.FIRMWARE_VERSION_SMART_PATCH_MIN) {
                                    final String finalFirmwareVersion = firmwareVersion;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(getResources().getString(R.string.installed_latest_version) + " (" + finalFirmwareVersion + ")");
                                        }
                                    });
                                    return;
                                }

                                success = PreferencesUtil.getInstance(FirmwareActivity.this).setFirmwareVersion(firmwareVersion);
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> 인스턴스 : " + success);


                                // 버전 정보 설명
                                String description = getVersionDescription(firmwareVersion);
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> description : " + description); //아마도 펌웨어 버전!!!
                                if (StringUtil.isNotNull(description)) {
                                    final String msg = description;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(msg);
                                        }
                                    });
                                }
                                LogUtil.d(TAG, "STEVE(OK) : 앱에 포함된 펌웨어 버전 : " + Definition.FIRMWARE_VERSION_SMART_PATCH);

                                // TODO 버전 체크 //Steve_20181011 //위에서 Version에 대한 Description을 먼저 만들고 동일한 경우
                                if (Definition.FIRMWARE_VERSION_SMART_PATCH.equals(firmwareVersion)) {
                                    LogUtil.d(TAG, "STEVE(OK) : BAT 충분 & FIRMWARE VERSION 일치!!!!");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(getResources().getString(R.string.installed_latest_version));
                                        }
                                    });
                                    return;
                                }

                                //Steve_20181011
                                findViewById(R.id.activity_firmware_fl_update).setEnabled(true);

                                // 업데이트 핸들러 시작
                                startUpdateHandler();

                                // 버전이 다른 경우 DFU 업그레이드 진행하겠다는 경고 팝업
                                popupWarningReadyToUpgrade();
                                break;

                            case 1:
                                ToastUtil.getInstance().show(
                                        FirmwareActivity.this,
                                        getResources().getString(R.string.firmware_update_low_battery, "Smart Patch", String.valueOf(byteArray[7]) + "%"),
                                        false);
                                // TODO 테스트
                                // 펌웨어 버전
                                firmwareVersion = ParserUtil.getFirmwareVersion(byteArray);
                                LogUtil.d(TAG, "STEVE(LBAT) : readData() -> firmwareVersion : " + firmwareVersion);

                                versionString = firmwareVersion;
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> versionString : " + versionString);

                                versionString = versionString.replaceAll("\\.", "");
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> versionString : " + versionString);

                                versionCode = Integer.parseInt(versionString);
                                if (versionCode <= Definition.FIRMWARE_VERSION_SMART_PATCH_MIN) {
                                    final String finalFirmwareVersion = firmwareVersion;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(getResources().getString(R.string.installed_latest_version) + " (" + finalFirmwareVersion + ")");
                                        }
                                    });
                                    return;
                                }

                                success = PreferencesUtil.getInstance(FirmwareActivity.this).setFirmwareVersion(firmwareVersion);
                                LogUtil.d(TAG, "STEVE(LBAT) : readData() -> setFirmwareVersion : " + success);

                                // 버전 정보 설명
                                description = getVersionDescription(firmwareVersion);
                                LogUtil.d(TAG, "STEVE(LBAT) : readData() -> Description : " + description);
                                if (StringUtil.isNotNull(description)) {
                                    final String msg = description;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(msg);
                                        }
                                    });
                                }

                                // TODO 버전 체크
                                if (Definition.FIRMWARE_VERSION_SMART_PATCH.equals(firmwareVersion)) {
                                    LogUtil.d(TAG, "STEVE(LBAT) But Newest Version installed!!!");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(getResources().getString(R.string.installed_latest_version));
                                        }
                                    });
                                    return;
                                }

                                // DFU 업그레이드 경고 팝업
                                //popupWarningDenyToUpgrade();
                                ToastUtil.getInstance().show(
                                        FirmwareActivity.this,
                                        getResources().getString(R.string.firmware_upgrade_deny),
                                        false);
                                break;
                            case 2:
                                LogUtil.d(TAG, "STEVE(HACK) : HACK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                ToastUtil.getInstance().show(
                                        FirmwareActivity.this,
                                        getResources().getString(R.string.installed_latest_version),
                                        false);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(getResources().getString(R.string.installed_latest_version));
                                    }
                                });
                                break;
                            default:
                                LogUtil.e(TAG, "STEVE(HACK Other) : HACK!!!!!!!!!!!!(" + error + ")");
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(getResources().getString(R.string.can_not_verify_the_latest_version));
                                    }
                                });
                                break;
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, "readData() -> Exception : " + e.getLocalizedMessage());
                }
            }
        });
    }

    /**
     * 프로그레스 다이얼로그 핸들러 시작
     */
    private void startProgressHandler() {
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
        if (null != progressHandler) progressHandler.removeMessages(0);
        if (upgradeTimeTask != null) upgradeTimeTask.cancel();
    }

    /**
     * 업데이트 핸들러 시작
     */
    private void startUpdateHandler() {
        new Thread() {
            public void run() {

                // 업데이트 핸들러 중지
                stopUpdateHandler();

                updateHandler.sendMessage(updateHandler.obtainMessage(MSG_UPDATE_ON));
            }
        }.start();
    }

    /**
     * 업데이트 핸들러 중지
     */
    private void stopUpdateHandler() {
        if (null != updateHandler) {
            updateHandler.removeMessages(0);
            updateHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 프로그레스 후처리
     */
    private void postProgress() {
        LogUtil.i(TAG, "postProgress() -> Start !!!");
        // 프로그레스 다이얼로그 종료
        dismissProgress();
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
     * 버전 정보 설명 문구
     */
    private String getVersionDescription(String firmwareVersion) {
        // 버전 체크

        if (Definition.FIRMWARE_VERSION_SMART_PATCH.equals(firmwareVersion)) {
            //LogUtil.e(TAG, "STEVE(HACK Other) : HACK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return getResources().getString(R.string.installed_latest_version);
        } else {
            //LogUtil.e(TAG, "STEVE(VERSION) : HACK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return getResources().getString(R.string.current_version)
                    + getResources().getString(R.string.ver) + firmwareVersion + "\n"
                    + getResources().getString(R.string.latest_version)
                    + getResources().getString(R.string.ver) + Definition.FIRMWARE_VERSION_SMART_PATCH;

        }
    }

    /**
     * DFU 업그레이드 경고 팝업
     */
    private void popupWarningReadyToUpgrade() {
        Dialog.getInstance().showDual(
                FirmwareActivity.this,
                getResources().getString(R.string.notice),
                String.format(getResources().getString(R.string.firmware_upgrade_ready_warning), "Smart Patch"),
                getResources().getString(R.string.confirm),
                getResources().getString(R.string.cancel),
                false,
                new Dialog.DialogOnClickListener() {
                    @Override
                    public void OnItemClickResult(HashMap <String, Object> hashMap) {
                        int result = ( int ) hashMap.get(Definition.KEY_DIALOG_DUAL);
                        switch (result) {
                            case Definition.DIALOG_BUTTON_POSITIVE: {
                                // 프로그레스 다이얼로그 시작
                                showProgress();

                                // 데이터 전송
                                writeData(true);
                            }
                            break;
                            case Definition.DIALOG_BUTTON_NETURAL:
                                break;
                            case Definition.DIALOG_BUTTON_NEGATIVE:
                                finish(); //Steve_20181011 //아니오 선택시 메인 메뉴로 복귀!!
                                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    /**
     * Upgrade Timer 시작
     */
    private void startUpgradeTimer() {
        LogUtil.i(TAG, "startUpgradeTimer() -> Start !!!");

        // UART Timer 중지
        stopUpgradeTimer();

        // 업그레이드 대기시간 초과
        upgradeTimeTask = new TimerTask() {
            @Override
            public void run() {
                LogUtil.d(TAG, "startUARTTimer() -> getUARTService : " + App.getInstance().getUARTService());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(Dialog.getInstance() == null) return; //Steve_20181012
                        Dialog.getInstance().showSingle(
                                FirmwareActivity.this,
                                getResources().getString(R.string.notice),
                                getResources().getString(R.string.firmware_upgrade_time_out),
                                getResources().getString(R.string.confirm),
                                false,
                                new Dialog.DialogOnClickListener() {
                                    @Override
                                    public void OnItemClickResult(HashMap <String, Object> hashMap) {
                                        int result = ( int ) hashMap.get(Definition.KEY_DIALOG_SINGLE);
                                        if (result == Definition.DIALOG_BUTTON_POSITIVE) {
                                            // 엑티비티 종료
                                            finish();
                                            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                                        }
                                    }
                                });

                    }
                });
            }
        };

        upgradeTimer = new Timer();
        upgradeTimer.schedule(upgradeTimeTask, 20000);
    }

    /**
     * Upgrade Timer 중지
     */
    private void stopUpgradeTimer() {
        if (null != upgradeTimeTask) upgradeTimeTask.cancel();
        if (null != upgradeTimer) upgradeTimer.cancel();
    }

    /**
     * 장비 선택 엑티비티
     */
    private void goToSelectDeviceActivity() {
        LogUtil.i(TAG, "goToSelectDeviceActivity() -> Gone!!");
        Intent intent = new Intent(FirmwareActivity.this, SelectDeviceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    /**
     * 배터리 레벨
     */
    private int parseBatteryLevelData() {
        LogUtil.i(TAG, "parseBatteryLevelData() -> Start !!!");
        if (null == App.getInstance().getFirmwareInformationData()) return 0;
        String hexadecimal = ParserUtil.byteArrayToHexadecimal(App.getInstance().getFirmwareInformationData());
        LogUtil.d(TAG, "parseBatteryLevelData() -> hexadecimal : " + hexadecimal);
        // TODO 텍스트
        String[] array = hexadecimal.split("\\p{Z}");
        LogUtil.d(TAG, "parseBatteryLevelData() -> array : " + array);
//        for (String string : array) {
//            LogUtil.d(TAG, "parseBatteryLevelData() -> string : " + string);
//        }
        LogUtil.d(TAG, "parseBatteryLevelData() -> array : " + array.length);
        // 유효성 체크
        String stx = array[0];
        LogUtil.d(TAG, "parseBatteryLevelData() -> stx : " + stx);
        String length = array[2];
        LogUtil.d(TAG, "parseBatteryLevelData() -> length : " + length);
        String etx = array[11];
        LogUtil.d(TAG, "parseBatteryLevelData() -> etx : " + etx);
        String command = array[1];
        LogUtil.d(TAG, "parseBatteryLevelData() -> command : " + command);

        // 펌웨어 정보
        if (Definition.RESPONSE_COMMAND_FIRMWARE_INFORMATION.equals(command)) {
            LogUtil.d(TAG, "parseBatteryLevelData() -> body : " + array[3]);
            return Integer.parseInt(array[10], 16);
        }
        return -1;
    }
}
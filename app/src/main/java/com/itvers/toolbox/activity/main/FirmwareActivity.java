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

    private final static String TAG = FirmwareActivity.class.getSimpleName();   // ????????? ??????

    private DialogQMProgress dialogQMProgress;                                  // ??????????????? ???????????????
    private int activityType = Definition.ACTIVITY_MODE_PATCH;                  // ???????????? ?????? (ACTIVITY_MODE_PATCH, ACTIVITY_MODE_MOUSE)
    private static Handler updateHandler;                                       // ???????????? ?????????
    private static Handler progressHandler;                                     // ??????????????? ?????????
    private Runnable progressRunnable;                                          // ??????????????? ????????????
    private final int MSG_UPDATE_ON = 1;                                        // ????????? ????????? UPDATE ON
    private final int MSG_UPDATE_OFF = 0;                                       // ????????? ????????? UPDATE OFF
    private String filePath;                                                    // ?????? ??????
    private boolean isUpgrading;                                                // ??????????????? ??????
    private ScannerFragment scannerFragment = null;                             // ?????? ?????? ???????????????
    private ArrayList <Integer> listProgress = new ArrayList();                 // ??????????????? ?????????
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
                // ??????????????? ????????? ?????? ??????
                dismissProgress();
                // ??????????????? ??????????????? ????????? ??????
                stopProgressHandler();
                break;
            case SERVICE_DISCONNECTED:
            case SERVICE_BINDING_DIED:
            case ERROR_EMPTY_UART_SERVICE:
            case ERROR_UART_SERVICE_INITIALIZATION:
            case ERROR_UART_SERVICE_CONNECT:
                // ??????????????? ????????? ?????? ??????
                dismissProgress();
                break;
            case GATT_CONNECTED: {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // ????????? ??????
                        writeData(false);
                    }
                }, Definition.GATT_INTERVAL_TIME); // 2??? ?????? ??????
            }
            break;
            case GATT_DISCONNECTED:
                // ??????????????? ????????? ?????? ??????
                dismissProgress();
                // ????????? ??????????????? ??????
                dismissFragmentScanner();
                break;
            case GATT_SERVICES_DISCOVERED:
                break;
            case DATA_AVAILABLE:
                break;
            case EMPTY_DEVICE:
            case EMPTY_DEVICE_ADDRESS:
                // ??????????????? ????????? ?????? ??????
                dismissProgress();
                // ????????? ??????????????? ??????
                dismissFragmentScanner();
                break;
        }
    }

    @Override
    public void onUARTServiceData(Intent intent) {
        if (null != intent) {
            final byte[] byteArray = intent.getByteArrayExtra(Definition.EXTRA_DATA);
            try {
                // ????????? ??????
                readData(byteArray);
            } catch (Exception e) {
                LogUtil.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    /**
     * DFU ?????? ?????????
     */
    private final DfuLogListener dfuLogListener = new DfuLogListener() {
        @Override
        public void onLogEvent(String bluetoothDeviceAddress, int level, String message) {
            LogUtil.d(TAG, "onLogEvent() -> level : " + String.valueOf(level) + ", message : " + message);
        }
    };

    /**
     * DFU ??????????????? ?????????
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

            // ???????????? ????????? ??????
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
//                                // ???????????? ??????
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

            // UART Timer ??????
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

            // UART Timer ??????
            stopUpgradeTimer();

            // ??????????????? ??????????????? ??????
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

            // ???????????? ????????????
            getProgress(percent);
        }

        @Override
        public void onError(final String bluetoothDeviceAddress,
                            final int error,
                            final int errorType,
                            final String message) {
            //LogUtil.i(TAG, "onError() -> Start !!!");
            LogUtil.e(TAG, "onError() -> error : " + error + ", errorType : " + errorType + ", message : " + message);

            // UART Timer ??????
            stopUpgradeTimer();

            listProgress.clear();

            // ??????????????? ??????????????? ??????
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

        // ??????????????? ??????????????? ??????
        showProgress();

        App.getInstance().getUARTService().connect(bluetoothDevice.getAddress());

        if (deviceName.startsWith(Definition.DEVICE_NAME_DFU)) {

            ((TextView) findViewById(R.id.activity_firmware_tv_selected_device)).setText(deviceName);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setText(R.string.firmware_status_update);
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextColor(getResources().getColor(R.color.colorDFUText));
            ((TextView) findViewById(R.id.activity_firmware_tv_update_state)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);

            findViewById(R.id.activity_firmware_fl_update).setEnabled(true);

            // ???????????? ????????? ??????
            startUpdateHandler();

            // Upgrade Timer ??????
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

        // ????????? ??????
        Intent intent = getIntent();
        activityType = intent.getIntExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
        boolean forceful_dfu_mode = intent.getBooleanExtra("DFU_MODE", false);
        LogUtil.d(TAG, "onCreate() -> activityType : " + activityType);

        if (null != intent) {
            if(!forceful_dfu_mode) { //Steve_20191230 //For forceful DFU Mode support!!!
                byte[] byteArray = App.getInstance().getFirmwareInformationData();
                if (null != byteArray) {
                    if (byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH) {
                        // ?????? ????????? ??????
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

        // ???????????? ??????
        findViewById(R.id.activity_firmware_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ???????????? ??????
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

        // ???????????? ????????? ?????????
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

        // ?????? ????????? ??????
        DfuServiceListenerHelper.registerLogListener(FirmwareActivity.this, dfuLogListener);
        // ??????????????? ????????? ??????
        DfuServiceListenerHelper.registerProgressListener(FirmwareActivity.this, dfuProgressListener);


        // ??????????????? ??????????????? ?????????
        if (null == progressHandler) progressHandler = new Handler();
        progressRunnable = new Runnable() {
            public void run() {
                postProgress();
            }
        };
        progressHandler.postDelayed(progressRunnable, Definition.PROGRESS_DURATION);

        // ????????? ??????
        String firmwareVersion = PreferencesUtil.getInstance(FirmwareActivity.this).getFirmwareVersion();

        if(forceful_dfu_mode) firmwareVersion = "DFU!!!"; //For forceful DFU Mode!!!

        LogUtil.d(TAG, "onCreate() -> firmwareVersion : " + firmwareVersion);
        if (StringUtil.isNotNull(firmwareVersion)) {
            // ?????? ?????? ??????
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

        // UART ????????? ??????
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
                // ??????????????? ???????????????
                showProgress();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // ????????? ??????
                        writeData(false);
                    }
                }, Definition.GATT_INTERVAL_TIME); // 2??? ?????? ??????
            }
        }

        findViewById(R.id.activity_firmware_tv_select_device).setOnClickListener(this);     // ?????? ?????? ??????
        findViewById(R.id.activity_firmware_tv_update_state).setOnClickListener(this);      // ???????????? ?????? ??????
        findViewById(R.id.activity_firmware_fl_update).setOnClickListener(this);            // ????????? ???????????? ??????
        findViewById(R.id.activity_firmware_fl_update).setEnabled(false);                   // ????????? ???????????? ?????? ?????? ??????

        // ?????? ?????? ??????
        findViewById(R.id.activity_firmware_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpgrading) return;
                // ???????????? ??????
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
        // BLE ????????? ?????? ?????? ??????
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

        // ??????????????? ??????????????? ??????
        dismissProgress();

        // ?????? ??????????????? ??????
        dismissFragmentScanner();

        // ???????????? ????????? ????????? ??????
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
        // ?????????????????????,
        if (isUpgrading) return;
        // ???????????? ??????
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    /**
     * ?????? ?????? ???????????????
     */
    private void showFragmentScanner(final Scan scan) {
        LogUtil.i(TAG, "showFragmentScanner() -> Start !!!");

        // ??????????????? ??????????????? ??????
        dismissProgress();

        LogUtil.d(TAG, "showFragmentScanner() -> getIsBackground : " + App.getInstance().getIsBackground());
        // ??????????????? ??????
        if (App.getInstance().getIsBackground()) return;

        // BLE ????????? ?????? ?????? ??????
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
     * ?????? ?????? ??????????????? ??????
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
                // ?????? ?????? ???????????????
                if (null == scannerFragment) showFragmentScanner(Scan.DEVICE_DFU);
                break;
            default:
                break;
        }
    }

    /**
     * ???????????? ?????? ??????
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
     * ????????????
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
                                // ????????? ??????
                                if (null != App.getInstance().getUARTService()) {
                                    File file = new File(filePath);
                                    LogUtil.d(TAG, "onUploadClicked() -> file : " + file.exists());
                                    if (!file.exists()) {
                                        // ??????????????? ??????????????? ??????
                                        dismissProgress();

                                        ToastUtil.getInstance().show(
                                                FirmwareActivity.this,
                                                getResources().getString(R.string.firmware_update_file_not_found),
                                                false);
                                        return;
                                    }

                                    // ??????????????? ??????????????? ??????
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

                                            // 2019-06-17 DFU ?????? ??????
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
                                // ???????????? ??????
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
     * ???????????? ????????????
     */
    private void getProgress(int rate) {
        LogUtil.i(TAG, "getProgress() -> Start !!!");

        int value = rate / 10;
        LogUtil.d(TAG, "getProgress() -> value : " + value);

        if (value == 10) {
            // ??????????????? ??????????????? ??????
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
     * ???????????? ??????
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

            // ??????????????? ?????? ??????
            isUpgrading = false;
        } else {
            // ??????????????? ?????? ??????
            isUpgrading = true;
        }
    }

    /**
     * ????????? ??????
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

            // TODO ????????? ????????? ?????? ??????
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
                // ??????????????? ??????????????? ??????
                dismissProgress();

                ToastUtil.getInstance().show(
                        FirmwareActivity.this,
                        getResources().getString(R.string.ble_connect_error),
                        false);
                return;
            }
            if (null == value) {
                LogUtil.e(TAG, "writeData() -> value is null.");
                // ??????????????? ??????????????? ??????
                dismissProgress();

                ToastUtil.getInstance().show(
                        FirmwareActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }
            if (value.length == 0) {
                LogUtil.e(TAG, "writeData() -> value length is 0.");
                // ??????????????? ??????????????? ??????
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
     * ????????? ??????
     */
    private void readData(final byte[] byteArray) {
        LogUtil.i(TAG, "readData() -> Start !!!");

        // ??????????????? ??????????????? ????????? ??????
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

                    // ????????? ??????
                    String stx = array[0];
                    LogUtil.d(TAG, "STEVE(Rx) : readData() -> stx : " + stx);
                    String length = array[2];
                    LogUtil.d(TAG, "STEVE(Rx) : readData() -> length : " + length);
                    String etx = array[8];
                    LogUtil.d(TAG, "STEVE(Rx) : readData() -> etx : " + etx);

                    String command = array[1];
                    LogUtil.d(TAG, "STEVE(Rxed Command) : readData() -> command : " + command);

                    // ????????? ????????????
                    if (Definition.RESPONSE_COMMAND_FIRMWARE_UPDATE.equals(command)) {
                        // ?????? ??????
                        int error = byteArray[3]; //Battery Level??? 20 ????????? ?????? '1'(F/W Upgrade ??????), 20% ????????? ?????? '0'(F/W Upgrade?????? ??????)
                        LogUtil.d(TAG, "STEVE( : readData() -> error : " + error);

                        boolean success = PreferencesUtil.getInstance(FirmwareActivity.this).clearFirmwareVersion();
                        LogUtil.d(TAG, "readData() -> clearFirmwareVersion : " + success);

                        switch (error) {
                            case 0:
                                // ????????? ??????
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
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> ???????????? : " + success);


                                // ?????? ?????? ??????
                                String description = getVersionDescription(firmwareVersion);
                                LogUtil.d(TAG, "STEVE(OK) : readData() -> description : " + description); //????????? ????????? ??????!!!
                                if (StringUtil.isNotNull(description)) {
                                    final String msg = description;
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(msg);
                                        }
                                    });
                                }
                                LogUtil.d(TAG, "STEVE(OK) : ?????? ????????? ????????? ?????? : " + Definition.FIRMWARE_VERSION_SMART_PATCH);

                                // TODO ?????? ?????? //Steve_20181011 //????????? Version??? ?????? Description??? ?????? ????????? ????????? ??????
                                if (Definition.FIRMWARE_VERSION_SMART_PATCH.equals(firmwareVersion)) {
                                    LogUtil.d(TAG, "STEVE(OK) : BAT ?????? & FIRMWARE VERSION ??????!!!!");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(getResources().getString(R.string.installed_latest_version));
                                        }
                                    });
                                    return;
                                }

                                //Steve_20181011
                                findViewById(R.id.activity_firmware_fl_update).setEnabled(true);

                                // ???????????? ????????? ??????
                                startUpdateHandler();

                                // ????????? ?????? ?????? DFU ??????????????? ?????????????????? ?????? ??????
                                popupWarningReadyToUpgrade();
                                break;

                            case 1:
                                ToastUtil.getInstance().show(
                                        FirmwareActivity.this,
                                        getResources().getString(R.string.firmware_update_low_battery, "Smart Patch", String.valueOf(byteArray[7]) + "%"),
                                        false);
                                // TODO ?????????
                                // ????????? ??????
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

                                // ?????? ?????? ??????
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

                                // TODO ?????? ??????
                                if (Definition.FIRMWARE_VERSION_SMART_PATCH.equals(firmwareVersion)) {
                                    LogUtil.d(TAG, "STEVE(LBAT) But Newest Version installed!!!");
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ((TextView) findViewById(R.id.activity_firmware_tv_comment_below)).setText(getResources().getString(R.string.installed_latest_version));
                                        }
                                    });
                                    return;
                                }

                                // DFU ??????????????? ?????? ??????
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
     * ??????????????? ??????????????? ????????? ??????
     */
    private void startProgressHandler() {
        progressHandler = new Handler();
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // ??????????????? ??????????????? ?????????
                postProgress();
            }
        }, Definition.PROGRESS_INTERVAL_TIME);
    }

    /**
     * ??????????????? ??????????????? ????????? ??????
     */
    private void stopProgressHandler() {
        if (null != progressHandler) progressHandler.removeMessages(0);
        if (upgradeTimeTask != null) upgradeTimeTask.cancel();
    }

    /**
     * ???????????? ????????? ??????
     */
    private void startUpdateHandler() {
        new Thread() {
            public void run() {

                // ???????????? ????????? ??????
                stopUpdateHandler();

                updateHandler.sendMessage(updateHandler.obtainMessage(MSG_UPDATE_ON));
            }
        }.start();
    }

    /**
     * ???????????? ????????? ??????
     */
    private void stopUpdateHandler() {
        if (null != updateHandler) {
            updateHandler.removeMessages(0);
            updateHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * ??????????????? ?????????
     */
    private void postProgress() {
        LogUtil.i(TAG, "postProgress() -> Start !!!");
        // ??????????????? ??????????????? ??????
        dismissProgress();
    }


    /**
     * ??????????????? ??????????????? ??????
     */
    private void showProgress() {
        LogUtil.i(TAG, "showProgress() -> Start !!!");

        if (App.getInstance().getIsBackground()) return;

        // ??????????????? ??????????????? ??????
        dismissProgress();

        dialogQMProgress = new DialogQMProgress(this, null, true);
        dialogQMProgress.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        if (null != dialogQMProgress) {
            dialogQMProgress.show();

            // ??????????????? ??????????????? ????????? ??????
            startProgressHandler();
        }
    }

    /**
     * ??????????????? ??????
     */
    private void dismissProgress() {
        LogUtil.i(TAG, "dismissProgress() -> Start !!!");

        // ??????????????? ??????????????? ????????? ??????
        stopProgressHandler();

        if (null != dialogQMProgress
                && dialogQMProgress.isShowing()) {
            dialogQMProgress.dismiss();
            dialogQMProgress = null;
        }
    }

    /**
     * ?????? ?????? ?????? ??????
     */
    private String getVersionDescription(String firmwareVersion) {
        // ?????? ??????

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
     * DFU ??????????????? ?????? ??????
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
                                // ??????????????? ??????????????? ??????
                                showProgress();

                                // ????????? ??????
                                writeData(true);
                            }
                            break;
                            case Definition.DIALOG_BUTTON_NETURAL:
                                break;
                            case Definition.DIALOG_BUTTON_NEGATIVE:
                                finish(); //Steve_20181011 //????????? ????????? ?????? ????????? ??????!!
                                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    /**
     * Upgrade Timer ??????
     */
    private void startUpgradeTimer() {
        LogUtil.i(TAG, "startUpgradeTimer() -> Start !!!");

        // UART Timer ??????
        stopUpgradeTimer();

        // ??????????????? ???????????? ??????
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
                                            // ???????????? ??????
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
     * Upgrade Timer ??????
     */
    private void stopUpgradeTimer() {
        if (null != upgradeTimeTask) upgradeTimeTask.cancel();
        if (null != upgradeTimer) upgradeTimer.cancel();
    }

    /**
     * ?????? ?????? ????????????
     */
    private void goToSelectDeviceActivity() {
        LogUtil.i(TAG, "goToSelectDeviceActivity() -> Gone!!");
        Intent intent = new Intent(FirmwareActivity.this, SelectDeviceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // ???????????? ??????
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    /**
     * ????????? ??????
     */
    private int parseBatteryLevelData() {
        LogUtil.i(TAG, "parseBatteryLevelData() -> Start !!!");
        if (null == App.getInstance().getFirmwareInformationData()) return 0;
        String hexadecimal = ParserUtil.byteArrayToHexadecimal(App.getInstance().getFirmwareInformationData());
        LogUtil.d(TAG, "parseBatteryLevelData() -> hexadecimal : " + hexadecimal);
        // TODO ?????????
        String[] array = hexadecimal.split("\\p{Z}");
        LogUtil.d(TAG, "parseBatteryLevelData() -> array : " + array);
//        for (String string : array) {
//            LogUtil.d(TAG, "parseBatteryLevelData() -> string : " + string);
//        }
        LogUtil.d(TAG, "parseBatteryLevelData() -> array : " + array.length);
        // ????????? ??????
        String stx = array[0];
        LogUtil.d(TAG, "parseBatteryLevelData() -> stx : " + stx);
        String length = array[2];
        LogUtil.d(TAG, "parseBatteryLevelData() -> length : " + length);
        String etx = array[11];
        LogUtil.d(TAG, "parseBatteryLevelData() -> etx : " + etx);
        String command = array[1];
        LogUtil.d(TAG, "parseBatteryLevelData() -> command : " + command);

        // ????????? ??????
        if (Definition.RESPONSE_COMMAND_FIRMWARE_INFORMATION.equals(command)) {
            LogUtil.d(TAG, "parseBatteryLevelData() -> body : " + array[3]);
            return Integer.parseInt(array[10], 16);
        }
        return -1;
    }
}
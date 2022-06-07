package com.itvers.toolbox.scanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.RequestCode;
import com.itvers.toolbox.item.Scan;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.StringUtil;
import com.itvers.toolbox.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class ScannerFragment extends DialogFragment {
    private static final String TAG = ScannerFragment.class.getSimpleName();    // 디버그 태그
    private ScannerFragmentListener listener;                                   // 스캐너 플래그먼트 리스너
    private DeviceListAdapter deviceListAdapter;                                // 장비 리스트 어댑터
    private Button btnScan;                                                     // 스캔 버튼
    private View view;                                                          // 뷰
    private ParcelUuid parcelUuid;                                              // UUID
    private int mode;                                                           // 요청 모드
    private boolean isScanning = false;                                         // 스캔 여부
    private final Handler stopHandler = new Handler();                          // 중지 핸들러
    private static Scan scan = Scan.DEVICE_OLNY;                                // 스캔 종류
    private BluetoothLeScannerCompat bluetoothLeScannerCompat;                  // 스캐너
    private AlertDialog dialog;
    private TimerTask dismissTimerTask;                                         // Dismiss TimerTask
    private Timer dismissTimer;                                                 // Dismiss Timer

    public static ScannerFragment newInstance(Bundle bundle, ScannerFragmentListener listener) {
        ScannerFragment scannerFragment = new ScannerFragment();
        scannerFragment.setArguments(bundle);
        scannerFragment.setCancelable(false);
        scannerFragment.listener = listener;
        return scannerFragment;
    }

    /**
     * 스캐너 플래그먼트 리스너
     */
    public interface ScannerFragmentListener {
        void onDeviceSelected(
                BluetoothDevice bluetoothDevice,
                String deviceName);

        void onDialogCanceled();

        void onDialogCreate();

        void onDialogDismissed();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        final Bundle args = getArguments();
        if (args.containsKey(Definition.KEY_INTENT_UUID))
            parcelUuid = args.getParcelable(Definition.KEY_INTENT_UUID);
        if (null != parcelUuid) LogUtil.d(TAG, "onCreate() -> parcelUuid : " + parcelUuid);

        if (args.containsKey(Definition.KEY_INTENT_ACTIVITY_MODE))
            mode = args.getInt(Definition.KEY_INTENT_ACTIVITY_MODE);
        LogUtil.d(TAG, "onCreate() -> mode : " + mode);

        if (args.containsKey(Definition.KEY_INTENT_IS_FIRMWARE))
            scan = ( Scan ) args.get(Definition.KEY_INTENT_IS_FIRMWARE);
        LogUtil.d(TAG, "onCreate() -> isFirmWare : " + scan);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreateDialog() -> Start !!!");

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_device_selection, null);
        final ListView listview = dialogView.findViewById(android.R.id.list);

        listview.setEmptyView(dialogView.findViewById(android.R.id.empty));
        listview.setAdapter(deviceListAdapter = new DeviceListAdapter(getActivity()));

        builder.setTitle(R.string.scanner_title);
        if (null == dialog) dialog = builder.setView(dialogView).create();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView <?> parent, final View view, final int position, final long id) {
                stopScan();
                final ExtendedBluetoothDevice d = ( ExtendedBluetoothDevice ) deviceListAdapter.getItem(position);
                if (null != listener) listener.onDeviceSelected(d.bluetoothDevice, d.deviceName);
                LogUtil.i(TAG, "onItemClick() -> Start !!!");
                dialog.dismiss();
            }
        });

        view = dialogView.findViewById(R.id.permission_rationale); // this is not null only on API23+

        btnScan = dialogView.findViewById(R.id.action_cancel);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.action_cancel) {
                    LogUtil.i(TAG, "onClick() -> Start !!!");
                    if (isScanning) {
                        dialog.cancel();
                    } else {
                        startScan();
                    }
                }
            }
        });

        // 연결된 장비 검색
//        addBondedDevices();

        // 스캔 시작
        startScan();

        if (null != listener) listener.onDialogCreate();
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        LogUtil.i(TAG, "onCancel() -> Start !!!");
        if (null != listener) listener.onDialogCanceled();
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        LogUtil.i(TAG, "onDismiss() -> Start !!!");
        if (null != listener) listener.onDialogDismissed();
        super.onDismiss(dialog);
    }

    /**
     * 퍼미션 결과
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCode.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 스캔 시작
                    startScan();
                } else {
                    view.setVisibility(View.VISIBLE);
                    ToastUtil.getInstance().show(getActivity(), getResources().getString(R.string.do_not_have_permission), false);
                }
                break;
            }
        }
    }

    /**
     * 스캔 시작
     */
    @SuppressLint("NewApi")
    private void startScan() {
        LogUtil.i(TAG, "startScan() -> Start !!!");
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    && view.getVisibility() == View.GONE) {
                view.setVisibility(View.VISIBLE);
                return;
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, RequestCode.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
            return;
        }

        if (view != null) view.setVisibility(View.GONE);

        deviceListAdapter.clearDevices();
        btnScan.setText(R.string.cancel);

        if (null == bluetoothLeScannerCompat)
            bluetoothLeScannerCompat = BluetoothLeScannerCompat.getScanner();

        final ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(1000)
                .setUseHardwareBatchingIfSupported(false)
                .build();
        final List <ScanFilter> scanFilter = new ArrayList <>();
        scanFilter.add(new ScanFilter
                .Builder()
                .setServiceUuid(parcelUuid)
                .build());
        if (null != bluetoothLeScannerCompat)
            bluetoothLeScannerCompat.startScan(scanFilter, scanSettings, scanCallback);

        isScanning = true;
        stopHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isScanning) {
                    // 스캔 중지
                    stopScan();
                }
            }
        }, Definition.SCAN_DURATION);
    }

    /**
     * 스캔 중지
     */
    private void stopScan() {
        LogUtil.i(TAG, "stopScan() -> Start !!!");
        LogUtil.i(TAG, "stopScan() -> isScanning: " + isScanning);
        if (isScanning) {
            stopHandler.removeMessages(0);

            //Steve_20181011 //Delete rescanning!!!
            //btnScan.setText(R.string.search);

            if (null != bluetoothLeScannerCompat)
                bluetoothLeScannerCompat.stopScan(scanCallback);

            isScanning = false;
        }
    }

    /**
     * 스캔 결과 콜백
     */
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            LogUtil.d(TAG, "onScanResult() -> Start !!!");
        }

        @Override
        public void onBatchScanResults(final List <ScanResult> results) {
            LogUtil.d(TAG, "onBatchScanResults() -> Start !!!");
            List <ScanResult> list = new ArrayList <>();
            LogUtil.d(TAG, "onBatchScanResults() -> scan : " + scan);

            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).getDevice().getName() != null) {
                    String deviceName = results.get(i).getDevice().getName();
                    if (StringUtil.isNull(deviceName)) continue;
                    LogUtil.d(TAG, "onBatchScanResults() -> deviceName : " + deviceName);
                    if (deviceName.length() > 9) {
                        deviceName = deviceName.substring(0, 9);
                        LogUtil.d(TAG, "onBatchScanResults() -> mode : " + mode);
                        switch (scan) {
                            case DEVICE_DFU:
                                if (mode == Definition.ACTIVITY_MODE_PATCH) {           // PATCH
                                    if (deviceName.equals(Definition.DEVICE_NAME_DFU)
                                            || deviceName.equals(Definition.DEVICE_NAME_HEADER_SMARTPATCH)) {
                                        list.add(results.get(i));
                                    }
                                } else if (mode == Definition.ACTIVITY_MODE_MOUSE) {    // MOUSE
                                    if (deviceName.equals(Definition.DEVICE_NAME_DFU)
                                            || deviceName.equals(Definition.DEVICE_NAME_HEADER_SLIMMOUSE)) {
                                        list.add(results.get(i));
                                    }
                                }
                                break;
                            case DFU_ONLY:
                                if (deviceName.equals(Definition.DEVICE_NAME_DFU)) {
                                    list.add(results.get(i));
                                }
                                break;
                            default:
                                LogUtil.d(TAG, "onBatchScanResults() -> deviceName : " + deviceName);
                                LogUtil.d(TAG, "onBatchScanResults() -> header : " + Definition.DEVICE_NAME_HEADER_SMARTPATCH);
                                if (mode == Definition.ACTIVITY_MODE_PATCH) {           // PATCH
                                    if (deviceName.equals(Definition.DEVICE_NAME_HEADER_SMARTPATCH)) {
                                        list.add(results.get(i));
                                    }
                                } else if (mode == Definition.ACTIVITY_MODE_MOUSE) {    // MOUSE
                                    if (deviceName.equals(Definition.DEVICE_NAME_HEADER_SLIMMOUSE)) {
                                        list.add(results.get(i));
                                    }
                                }
                                break;
                        }
                    }
                }
            }

            // TODO 테스트
            for (ScanResult result : list) {
                LogUtil.d(TAG, "onBatchScanResults() -> result : " + result.getDevice().getName());
            }

            LogUtil.d(TAG, "onBatchScanResults() -> list : " + list.size());
            if (null != list
                    && list.size() > 0) deviceListAdapter.update(list);

//            if (list.size() == 0) {
//                if (listener != null) {
//                    listener.onDialogCanceled();
//                    listener = null;
//                }
//            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            LogUtil.e(TAG, "onScanFailed() -> errorCode : " + errorCode);
        }
    };

    /**
     * 스캔 다이얼로그 중지
     */
    public AlertDialog getScannerFragmentDialog() {
        return dialog;
    }

    /**
     * 스캔 다이얼로그 중지
     */
    public void dismiss() {
        // 스캔 중지
        stopScan();
        if (null != dialog) dialog.dismiss();
    }

    /**
     * Dismiss Timer 시작
     */
    private void startDismissTimer() {
        LogUtil.i(TAG, "startDismissTimer() -> Start !!!");

        // Dismiss Timer 중지
        stopDismissTimer();

        dismissTimerTask = new TimerTask() {
            @Override
            public void run() {
                dismiss();
            }
        };

        // 타이머 시작 (35초)
        dismissTimer = new Timer();
        dismissTimer.schedule(dismissTimerTask, 0, 35 * 1000);
    }

    /**
     * DismissTimer Timer 중지
     */
    private void stopDismissTimer() {
        LogUtil.i(TAG, "stopDismissTimer() -> Stop !!!");
        if (null != dismissTimerTask) dismissTimerTask.cancel();
        if (null != dismissTimer) dismissTimer.cancel();
    }

}
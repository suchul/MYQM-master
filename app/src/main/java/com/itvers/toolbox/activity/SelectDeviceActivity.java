package com.itvers.toolbox.activity;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itvers.toolbox.App;
import com.itvers.toolbox.R;
import com.itvers.toolbox.activity.main.FirmwareActivity;
import com.itvers.toolbox.activity.main.MainActivity;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.RequestCode;
import com.itvers.toolbox.common.VendorType;
import com.itvers.toolbox.dialog.DialogDefault;
import com.itvers.toolbox.dialog.DialogQMProgress;
import com.itvers.toolbox.item.Result;
import com.itvers.toolbox.item.Scan;
import com.itvers.toolbox.item.Type;
import com.itvers.toolbox.item.UARTStatus;
import com.itvers.toolbox.scanner.DeviceListAdapter;
import com.itvers.toolbox.scanner.ExtendedBluetoothDevice;
import com.itvers.toolbox.scanner.ScannerFragment;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.StringUtil;
import com.itvers.toolbox.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class SelectDeviceActivity extends AppCompatActivity implements App.BlutoothListener, ScannerFragment.ScannerFragmentListener {
    private static final String TAG = SelectDeviceActivity.class.getSimpleName();   // ????????? ??????

    private long backPressedTime = 0;                                               // ????????? ?????? ??????
    private boolean isProgress = false;                                             // ??????????????? ??????
    private BluetoothAdapter bluetoothAdapter;                                      // BLE ?????????
    private BluetoothManager bluetoothManager;                                      // BLE ?????????
    private DeviceListAdapter deviceListAdapter;                                    // ?????? ????????? ?????????
    private ArrayList<HashMap<String, Object>> listBluetoothDevice
            = new ArrayList <>();                                                   // ???????????? ?????? ?????????
    private ArrayList<BluetoothDevice> tempBluetoothDevice
            = new ArrayList <>();

    private DialogQMProgress dialogQMProgress;                                      // ??????????????? ???????????????
    private static Handler progressHandler;                                         // ??????????????? ??????????????? ?????????

    private int selectedIndex = -1;                                                 // ?????? ?????????

    private boolean isStartMainActivity = false;                                    // ?????? ???????????? ?????? ?????? ??????
    private boolean isClicked = false;                                              // ?????? ??????

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult() -> "
                + "requestCode : " + requestCode
                + ", resultCode : " + resultCode
                + ", data : " + data);
        switch (requestCode) {
            case RequestCode.REQUEST_ENABLE_BLUTOOTH:
                if(resultCode == RESULT_OK){
                  isProgress = false;
                    findViewById(R.id.activity_select_device_tv_search).performClick();
                } else if(resultCode == RESULT_CANCELED){
                  finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onUARTServiceChange(UARTStatus status) {
        LogUtil.i(TAG, "onUARTServiceChange() -> Start !!!");
        LogUtil.d(TAG, "onUARTServiceChange() -> status : " + status);

        switch (status) {
            case SERVICE_CONNECTED: {
                // GATT ??????
//                if (null != App.getInstance().getUARTService()) {
//                    BluetoothDevice bluetoothDevice = App.getInstance().getUARTService().addBondedDevice(Definition.ACTIVITY_MODE_PATCH);
//                    LogUtil.d(TAG, "onUARTServiceChange() -> bluetoothDevice: " + bluetoothDevice);
//
//                    if (null != bluetoothDevice
//                            && StringUtil.isNotNull(bluetoothDevice.getAddress())) {
//                        App.getInstance().getUARTService().connect(bluetoothDevice.getAddress());
//                    }
//                }
            }
            break;
            case GATT_CONNECTED: {
                // ????????? ??????
                App.getInstance().writeData(Type.FIRMWARE_INFORMATION);
            }
            break;
            case GATT_SERVICES_DISCOVERED:
                break;
            case DATA_AVAILABLE:
                break;
            case SERVICE_DISCONNECTED:
            case SERVICE_BINDING_DIED:
            case DEVICE_DOES_NOT_SUPPORT_UART:
            case ERROR_EMPTY_UART_SERVICE:
            case ERROR_UART_SERVICE_INITIALIZATION:
            case ERROR_UART_SERVICE_CONNECT:
            case GATT_DISCONNECTED:
            case EMPTY_DEVICE:
            case EMPTY_DEVICE_ADDRESS: {

                findViewById(R.id.activity_select_device_pb_progress).setVisibility(View.GONE);

                // ????????? ???????????? ?????? ?????????
                initConnectedBluetoothDevice();

                // ???????????? ????????? ??????
                deviceListAdapter.notifyDataSetChanged();

                // ??????????????? ??????????????? ????????? ??????
                dismissProgress();
            }
                break;
        }
    }

    @Override
    public void onUARTServiceData(Intent intent) {
        LogUtil.i(TAG, "onUARTServiceData() -> Start !!!");
        LogUtil.i(TAG, "onUARTServiceData() -> intent : " + intent);
        findViewById(R.id.activity_select_device_pb_progress).setVisibility(View.GONE);

        if (null != intent) {
            byte[] byteArray = intent.getByteArrayExtra(Definition.EXTRA_DATA);
            // ????????? ??????
            Result result = App.getInstance().readData(byteArray, Type.FIRMWARE_INFORMATION);
            if (result == Result.SUCCESS) {
                if (byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH) {
                    String address = App.getInstance().getBluetoothDevice().getAddress();
                    if (!address.isEmpty()) {
                        for (HashMap<String, Object> hashMap : listBluetoothDevice) {
                            BluetoothDevice bluetoothDevice = (BluetoothDevice) hashMap.get(Definition.KEY_BLUETOOTH_DEVICE);
                            if (address.equalsIgnoreCase(bluetoothDevice.getAddress())) {
                                if (isClicked) {
                                    App.getInstance().setFirmwareInformationData(byteArray);
                                    App.getInstance().setConnectedBluetoothDevice(App.getInstance().getBluetoothDevice());

                                    if (!App.getInstance().getConnectedDevice()) {
                                        App.getInstance().setConnectedDevice(true);
                                    }

                                    if (isStartMainActivity) return;
                                    isStartMainActivity = true;

                                    // ?????? ???????????? ?????? ??????
                                    gotoMainActivity();
                                    return;
                                } else {
                                    if (listBluetoothDevice.size() > 1) {
                                        for (int i=0; i<listBluetoothDevice.size(); i++) {
                                            BluetoothDevice device = tempBluetoothDevice.get(tempBluetoothDevice.size()-1);

                                            LogUtil.d(TAG, "onUARTServiceData() -> address : " + address);
                                            LogUtil.d(TAG, "onUARTServiceData() -> device : " + device.getAddress());
                                            if (address.equals(device.getAddress())) {
                                                listBluetoothDevice.get(i).put(Definition.KEY_IS_CONNECTED, true);
                                            }
                                        }
                                    } else {
                                        App.getInstance().setFirmwareInformationData(byteArray);
                                        App.getInstance().setConnectedBluetoothDevice(App.getInstance().getBluetoothDevice());

                                        if (!App.getInstance().getConnectedDevice()) {
                                            App.getInstance().setConnectedDevice(true);
                                        }

                                        if (isStartMainActivity) return;
                                        isStartMainActivity = true;

                                        // ?????? ???????????? ?????? ??????
                                        gotoMainActivity();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ???????????? ????????? ??????
        deviceListAdapter.notifyDataSetChanged();
        // ??????????????? ??????????????? ????????? ??????
        dismissProgress();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device);
        LogUtil.d(TAG, "onCreate() -> Start !!!");

        if (null == bluetoothManager) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }
        bluetoothAdapter = bluetoothManager.getAdapter();

        // ????????? ???
        deviceListAdapter = new DeviceListAdapter();
        ListView listView = findViewById(R.id.activity_select_device_lv_device);
        listView.setAdapter(deviceListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "onItemClick() -> Start !!!");

                LogUtil.i(TAG, "onItemClick() -> getUARTService(): " + App.getInstance().getUARTService());
                if (null == App.getInstance().getUARTService()) return;
                LogUtil.i(TAG, "onItemClick() -> getBlutoothListener(): " + App.getInstance().getBlutoothListener());
                if (null == App.getInstance().getBlutoothListener()) return;

                selectedIndex = position;

                deviceListAdapter.notifyDataSetChanged();

                HashMap<String, Object> hashMap = (HashMap<String, Object>) deviceListAdapter.getItem(position);

                BluetoothDevice device = (BluetoothDevice) hashMap.get(Definition.KEY_BLUETOOTH_DEVICE);
                LogUtil.i(TAG, "onItemClick() -> device: " + device);

                isClicked = true;

                findViewById(R.id.activity_select_device_pb_progress).setVisibility(View.VISIBLE);

                // GATT ??????
                App.getInstance().getUARTService().connect(device.getAddress());
            }
        });

        // ????????? ?????????
        deviceListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                LogUtil.i(TAG, "onChanged() -> Start !!!");

                isProgress = false;
                ((TextView) findViewById(R.id.activity_select_device_tv_search)).setText(getResources().getString(R.string.device_search));
                findViewById(R.id.activity_select_device_pb_search).setVisibility(View.GONE);
            }
        });

        // ?????? ?????? ??????
        findViewById(R.id.activity_select_device_tv_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.d(TAG, "onCreate() -> getBlutoothListener : " + App.getInstance().getBlutoothListener());
                if (isProgress) return;

                isProgress = true;

                // BLE ????????? ?????? ?????? ??????
                if (null == App.getInstance().getBluetoothAdapter()) {
                    LogUtil.d(TAG, "onCreate() -> getBlutoothListener : " + App.getInstance().getBlutoothListener());
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, RequestCode.REQUEST_ENABLE_BLUTOOTH);
//                    Bundle bundle = new Bundle();
//                    bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_BLE_ENABLE);
//                    DialogDefault.show(getFragmentManager(), bundle, new DialogDefault.DefaultListener() {
//                        @Override
//                        public void OnConfirmListener() {
//                            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
//                            startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_BLUETOOTH_SETTINGS);
//                        }
//
//                        @Override
//                        public void OnCancelListener() {
//                            finish();
//                        }
//                    });
                } else {
                    LogUtil.d(TAG, "onCreate() -> FindDevice : getBluetoothAdapter : " + App.getInstance().getBluetoothAdapter().isEnabled());
                    if (!App.getInstance().getBluetoothAdapter().isEnabled()) {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, RequestCode.REQUEST_ENABLE_BLUTOOTH);
                        return;
                    }

                    ((TextView) findViewById(R.id.activity_select_device_tv_search)).setText(getResources().getString(R.string.searching));
                    findViewById(R.id.activity_select_device_pb_search).setVisibility(View.VISIBLE);

                    new Handler().postDelayed(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        public void run() {
                            // ????????? ?????? ?????????
                            addBondedDevices();
                        }
                    }, 2000);
                }
            }
        });

        // DFU ??????
        findViewById(R.id.activity_select_device_tv_dfu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProgress) return;
//Steve_20191230 //Not working!!!				
                Intent intent = new Intent(SelectDeviceActivity.this, FirmwareActivity.class);
                intent.putExtra("DFU_MODE", true);
                startActivity(intent);
//				showFragmentScanner(Scan.DFU_ONLY);
            }
        });

        // ?????? ??????
        findViewById(R.id.activity_select_device_ll_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i(TAG, "onClick() -> getConnectedDevice(): " + App.getInstance().getConnectedDevice());

                if (!App.getInstance().getConnectedDevice()) {
                    Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_BLUETOOTH_SETTINGS);
                    return;
                }

                // TODO Offline
                if (!App.getInstance().getConnectedDevice()) {
//                    ToastUtil.getInstance().show(SelectDeviceActivity.this, getResources().getString(R.string.select_device_empty_device_name), false);
                    return;
                }

                // ?????? ???????????? ?????? ??????
                gotoMainActivity();
            }
        });

        findViewById(R.id.activity_select_device_tv_search).performClick();

//        new Handler().postDelayed(new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.P)
//            public void run() {
//                // BLE ????????? ?????? ?????? ??????
//                if (null == App.getInstance().getBluetoothAdapter()) {
//                    LogUtil.d(TAG, "onCreate() -> getBlutoothListener : " + App.getInstance().getBlutoothListener());
//                    Bundle bundle = new Bundle();
//                    bundle.putInt(Definition.KEY_DIALOG_TYPE, Definition.TYPE_DIALOG_BLE_ENABLE);
//                    DialogDefault.show(getFragmentManager(), bundle, new DialogDefault.DefaultListener() {
//                        @Override
//                        public void OnConfirmListener() {
//                            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
//                            startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_BLUETOOTH_SETTINGS);
//                        }
//
//                        @Override
//                        public void OnCancelListener() {
//                            finish();
//                        }
//                    });
//                } else {
//                    LogUtil.d(TAG, "onCreate() -> getBluetoothAdapter : " + App.getInstance().getBluetoothAdapter().isEnabled());
//                    if (!App.getInstance().getBluetoothAdapter().isEnabled()) {
//                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        startActivityForResult(intent, RequestCode.REQUEST_ENABLE_BLUTOOTH);
//                    } else {
//                        findViewById(R.id.activity_select_device_tv_search).performClick();
//                    }
//                }
//            }
//        }, 2000);
    }

    @Override
    protected void onResume() {
        LogUtil.i(TAG, "onResume() -> Start !!!");
        super.onResume();

        if (null == App.getInstance().getUARTService()) {
            App.getInstance().startUARTService();
        }
        LogUtil.d(TAG, "onResume() -> getBluetoothAdapter : " + App.getInstance().getBluetoothAdapter());

        // UART ????????? ??????
        App.getInstance().setBlutoothListener(null, SelectDeviceActivity.this);
        // BLE ????????? ??????
        App.getInstance().setBlutoothListener(this, SelectDeviceActivity.this);

        LogUtil.d(TAG, "onResume() -> getBluetoothAdapter : " + App.getInstance().getBluetoothAdapter());
    }


    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.i(TAG, "onStop() -> Start !!!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.i(TAG, "onPause() -> Start !!!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy() -> Start !!!");
        // BLE ????????? ??????
        App.getInstance().setBlutoothListener(null, SelectDeviceActivity.this);
    }

    /**
     * ????????? ?????? ??????
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void addBondedDevices() {
        LogUtil.i(TAG, "addBondedDevices() -> Start !!!");

        // TODO Offline
        final Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
//        LogUtil.e(TAG, "addBondedDevices() -> devices: " + devices);

        ///////////////////////////////////////////////////////
        //Steve_20190801 //List up the bonded devices.
        ///////////////////////////////////////////////////////
        listBluetoothDevice = new ArrayList <>();
        for (BluetoothDevice device : devices) {
            String deviceName = device.getName();
//            LogUtil.e(TAG, "addBondedDevices() -> deviceName : " + deviceName);
            if (StringUtil.isNull(deviceName)) continue;
            if (deviceName.length() >= 9) { //Steve_20190731 //Changed for simple device name
                deviceName = deviceName.substring(0, 9);

                if (StringUtil.isNull(deviceName)) continue;
                HashMap<String, Object> hashMap = new HashMap<>();
                if (deviceName.equals(Definition.DEVICE_NAME_HEADER_SMARTPATCH)) {
                    hashMap.put(Definition.KEY_BLUETOOTH_DEVICE, device);
                    hashMap.put(Definition.KEY_IS_CONNECTED, false);
                    listBluetoothDevice.add(hashMap);
//                    LogUtil.e(TAG, "addBondedDevices() -> device : " + device.getAddress());
                }
            }
        }

        tempBluetoothDevice.clear();
        if (listBluetoothDevice.size() > 0) {
            findViewById(R.id.activity_select_device_tv_alert).setVisibility(View.GONE);

            if (listBluetoothDevice.size() == 1) { // ????????? ??????????????? ????????? ??????,
                if (App.getInstance().getUARTService() != null) {
                    HashMap<String, Object> hashMap = listBluetoothDevice.get(0);
                    final BluetoothDevice device = (BluetoothDevice) hashMap.get(Definition.KEY_BLUETOOTH_DEVICE);
                    LogUtil.e(TAG, "addBondedDevices() -> address : " + device.getAddress());
                    App.getInstance().getUARTService().connect(device.getAddress());
                }
            } else { // ????????? ??????????????? ????????? ??????,
                for (int i = 0; i < listBluetoothDevice.size(); i++) {
                    HashMap<String, Object> hashMap = listBluetoothDevice.get(i);
                    final BluetoothDevice device = (BluetoothDevice) hashMap.get(Definition.KEY_BLUETOOTH_DEVICE);
                    LogUtil.e(TAG, "addBondedDevices() -> address : " + device.getAddress());

                    try {
                        App.getInstance().getUARTService().connect(device.getAddress());
                        tempBluetoothDevice.add(device);

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            findViewById(R.id.activity_select_device_tv_alert).setVisibility(View.VISIBLE);
            deviceListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeviceSelected(BluetoothDevice bluetoothDevice, String deviceName) {
        LogUtil.d(TAG, "onDeviceSelected >> Start !!!");

        // ????????? ???????????? ?????? ??????
        gotoFirmwareActivity();

    }

    @Override
    public void onDialogCanceled() {
        LogUtil.d(TAG, "onDialogCanceled >> Start !!!");
    }

    @Override
    public void onDialogCreate() {
        LogUtil.d(TAG, "onDialogCreate >> Start !!!");
    }

    @Override
    public void onDialogDismissed() {
        LogUtil.d(TAG, "onDialogDismissed >> Start !!!");
    }

    /**
     * ????????? ?????? ????????? ?????????
     */
    private class DeviceListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listBluetoothDevice.size();
        }

        @Override
        public Object getItem(int position) {
            return listBluetoothDevice.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(SelectDeviceActivity.this, R.layout.layout_list_device_row, null);
                holder = new ViewHolder();

                holder.deviceName = convertView.findViewById(R.id.layout_list_device_row_name);
                holder.deviceAddress = convertView.findViewById(R.id.layout_list_device_row_address);
                holder.rssi = convertView.findViewById(R.id.layout_list_device_row_rssi);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, Object> hashMap = listBluetoothDevice.get(position);
            BluetoothDevice device = (BluetoothDevice) hashMap.get(Definition.KEY_BLUETOOTH_DEVICE);
            String name = device.getName();
            holder.deviceName.setText(name != null ? name : getString(R.string.not_available));

            boolean isConnected = (boolean) hashMap.get(Definition.KEY_IS_CONNECTED);
            LogUtil.d(TAG, "getView() -> isConnected: " + isConnected);

//            LogUtil.d(TAG, "getView() -> selectedIndex: " + selectedIndex);
//            LogUtil.d(TAG, "getView() -> position: " + position);
//            LogUtil.d(TAG, "getView() >> name: " + name);

            if (isConnected) {
                holder.deviceName.setTextColor(Color.rgb(255, 255, 153));
//                ((TextView) findViewById(R.id.activity_select_device_tv_next)).setText(getResources().getString(R.string.next));
            } else {
                if (selectedIndex == position) {
                    holder.deviceName.setTextColor(Color.rgb(255, 165, 0));
                    holder.deviceAddress.setTextColor(Color.rgb(255, 165, 0));
                    selectedIndex = -1;
                } else {
                    holder.deviceName.setTextColor(Color.GRAY);
//                    ((TextView) findViewById(R.id.activity_select_device_tv_next)).setText(getResources().getString(R.string.next));
                }
            }
            holder.rssi.setVisibility(View.GONE);
            return convertView;
        }

        /**
         * ??? ??????
         */
        private class ViewHolder {
            private TextView deviceName;
            private TextView deviceAddress;
            private ImageView rssi;
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
            dismissDialog(SelectDeviceActivity.this, dialogQMProgress);
            dialogQMProgress = null;
        }
    }

    /**
     * ??????????????? ??????
     *
     * @param activity  ????????????
     * @param dialog    ???????????????
     */
    private static void dismissDialog(Activity activity, Dialog dialog) {
        if (activity.isDestroyed()) {
            return;
        }
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * ??????????????? ??????????????? ????????? ??????
     */
    private void startProgressHandler() {
        LogUtil.i(TAG, "startProgressHandler() -> Start !!!");
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
        LogUtil.i(TAG, "stopProgressHandler() -> Start !!!");
        if (null != progressHandler) progressHandler.removeMessages(0);
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
     * ?????? ???????????? ?????? ??????
     */
    private void gotoMainActivity() {
        Intent intent = new Intent(SelectDeviceActivity.this, MainActivity.class);
        startActivity(intent);

        // ???????????? ??????
        finish();
        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
    }

    /**
     * ????????? ???????????? ?????? ??????
     */
    private void gotoFirmwareActivity() {
        Intent intent = new Intent(SelectDeviceActivity.this, FirmwareActivity.class);
        startActivity(intent);

        // ???????????? ??????
        finish();
        overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
    }

    /**
     * ????????? ???????????? ?????? ?????????
     */
    private void initConnectedBluetoothDevice() {
        LogUtil.i(TAG, "initConnectedBluetoothDevice() -> Start !!!");
        App.getInstance().setConnectedDevice(false);
        App.getInstance().setFirmwareInformationData(null);
        App.getInstance().setConnectedBluetoothDevice(null);
    }

    /**
     * ?????????
     */
    @Override
    public void onBackPressed() {
        LogUtil.i(TAG, "onBackPressed() -> Start !!!");

        // TODO MOUSE ?????? ???????????? ?????? ??????
//        super.onBackPressed();
//        LogUtil.i(TAG, "onBackPressed() -> Start !!!");
//        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);

        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;
        if (0 <= intervalTime
                && Definition.FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            ToastUtil.getInstance().show(SelectDeviceActivity.this, getResources().getString(R.string.back_exit), false);
        }
    }

    /**
     * ?????? ?????? ???????????????
     */
//    private void showFragmentScanner(final Scan scan) {
//        LogUtil.i(TAG, "showFragmentScanner() -> Start !!!");
//
//        // ??????????????? ??????????????? ??????
//        dismissProgress();
//
//        LogUtil.d(TAG, "showFragmentScanner() -> getIsBackground : " + App.getInstance().getIsBackground());
//        // ??????????????? ??????
//        if (App.getInstance().getIsBackground()) return;
//
//        // BLE ????????? ?????? ?????? ??????
//        if (null != App.getInstance().getBluetoothAdapter()) {
//            if (!App.getInstance().getBluetoothAdapter().isEnabled()) {
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(intent, RequestCode.REQUEST_ENABLE_BLUTOOTH);
//                return;
//            }
//        }
//
//        runOnUiThread(new Runnable() {
//            public void run() {
//                Bundle bundle = new Bundle();
//                bundle.putInt(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
//                bundle.putSerializable(Definition.KEY_INTENT_IS_FIRMWARE, scan);
//                ScannerFragment scannerFragment = ScannerFragment.newInstance(bundle, SelectDeviceActivity.this);
//                scannerFragment.show(getFragmentManager(), Definition.SCAN_FRAGMENT);
//            }
//        });
//    }
}

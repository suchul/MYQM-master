package com.itvers.toolbox;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.itvers.toolbox.activity.main.hotkey.HotKeyBDoubleActivity;
import com.itvers.toolbox.activity.main.hotkey.HotKeyBLongActivity;
import com.itvers.toolbox.activity.main.hotkey.HotKeyCDoubleActivity;
import com.itvers.toolbox.activity.main.hotkey.HotKeyCLongActivity;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.item.Result;
import com.itvers.toolbox.item.Type;
import com.itvers.toolbox.item.UARTStatus;
import com.itvers.toolbox.service.UARTService;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ParserUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();        // 디버그 태그
    private static volatile App singletonInstance = null;               // 싱글턴 인스턴스
    private static Context context;                                     // 컨텍스트

    private static UARTService uartService;                             // UART Service
    private static BlutoothListener blutoothListener;                   // 블루투스 리스너
    private static BluetoothAdapter bluetoothAdapter;                   // 블루투스 어댑터
    private BluetoothDevice bluetoothDevice;                            // 블루투스 장비
    private static BluetoothDevice connectedBluetoothDevice;            // 연결된 블루투스 장비
    private static ArrayList arrayBluetoothDevice = new ArrayList();    // 연결된 블루투스 장비들
    private boolean isBackground;                                       // 백그라운드 여부
    private static boolean isConnectedDevice;                           // 디바이스 연결 여부
    private byte[] byteArray;                                           // 데이터
    private ArrayList arrayByteArray = new ArrayList();                 // 데이터 배열

    public interface BlutoothListener {
        void onUARTServiceChange(UARTStatus status);

        void onUARTServiceData(Intent intent);
    }

    /**
     * UART Service Connection
     */
    private ServiceConnection uartServiceConnection = new ServiceConnection() {
        /**
         * 서비스 연결
         * @param className
         * @param rawBinder
         */
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            uartService = ((UARTService.LocalBinder) rawBinder).getService();

            if (null == uartService) {
                LogUtil.e(TAG, "onServiceConnected() -> uartService is null.");
                if (null != blutoothListener)
                    blutoothListener.onUARTServiceChange(UARTStatus.ERROR_EMPTY_UART_SERVICE);
                return;
            }

            LogUtil.d(TAG, "onServiceConnected() -> uartService : " + uartService);
            if (!uartService.initialization()) {
                LogUtil.e(TAG, "onServiceConnected() -> uartService initialization fail !!!");
                if (null != blutoothListener)
                    blutoothListener.onUARTServiceChange(UARTStatus.ERROR_UART_SERVICE_INITIALIZATION);
                return;
            }
            if (null != blutoothListener)
                blutoothListener.onUARTServiceChange(UARTStatus.SERVICE_CONNECTED);
        }

        /**
         * 서비스 연결 해제
         * @param classname
         */
        public void onServiceDisconnected(ComponentName classname) {
            if (null != blutoothListener)
                blutoothListener.onUARTServiceChange(UARTStatus.SERVICE_DISCONNECTED);
        }

        @Override
        public void onBindingDied(ComponentName name) {
            if (null != blutoothListener)
                blutoothListener.onUARTServiceChange(UARTStatus.SERVICE_BINDING_DIED);
        }
    };

    /**
     * BroadcastReceiver
     */
    private final BroadcastReceiver uartStatusChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, "uartStatusChangeReceiver -> action : " + action);
            LogUtil.d(TAG, "uartStatusChangeReceiver -> blutoothListener : " + blutoothListener);

            // 연결
            if (action.equals(Definition.ACTION_GATT_CONNECTED)) {
                if (null != blutoothListener)
                    blutoothListener.onUARTServiceChange(UARTStatus.GATT_CONNECTED);
            }

            // 연결 해제
            if (action.equals(Definition.ACTION_GATT_DISCONNECTED)) {
                if (null != blutoothListener)
                    blutoothListener.onUARTServiceChange(UARTStatus.GATT_DISCONNECTED);
                byteArray = null;
            }

            // BLE GATT 서비스 발견
            if (action.equals(Definition.ACTION_GATT_SERVICES_DISCOVERED)) {
                if (null != uartService) uartService.enableTXNotification();
                if (null != blutoothListener)
                    blutoothListener.onUARTServiceChange(UARTStatus.GATT_SERVICES_DISCOVERED);
            }

            // 데이터 읽기 가능
            if (action.equals(Definition.ACTION_DATA_AVAILABLE)) {
                if (null != blutoothListener) blutoothListener.onUARTServiceData(intent);
                if (null != blutoothListener)
                    blutoothListener.onUARTServiceChange(UARTStatus.DATA_AVAILABLE);
            }

            // 지원 하지 않음
            if (action.equals(Definition.DEVICE_DOES_NOT_SUPPORT_UART)) {
                if (null != blutoothListener)
                    blutoothListener.onUARTServiceChange(UARTStatus.DEVICE_DOES_NOT_SUPPORT_UART);
                byteArray = null;
            }
        }
    };

    /**
     * 싱글턴 인스턴스
     *
     * @return instance
     */
    public static App getInstance() {
        if (null == singletonInstance) {
            synchronized (App.class) {
                if (null == singletonInstance) {
                    singletonInstance = new App();
                }
            }
        }
        return singletonInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.setEnableLog(true);
        LogUtil.d(TAG, "setEnableLog : " + LogUtil.getEnableLog());
        LogUtil.i(TAG, "onCreate() -> Start !!!");

        context = getApplicationContext();
        LogUtil.i(TAG, "onCreate() -> context : " + context);

        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                setIsBackground(false);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                setIsBackground(false);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    /**
     * 리스너 설정
     *
     * @param listener
     * @return
     */
    public boolean setBlutoothListener(BlutoothListener listener,
                                       Activity activity) {
        blutoothListener = null;
        blutoothListener = listener;
        LogUtil.d(TAG, "setBlutoothListener() >> blutoothListener: " + blutoothListener);
        LogUtil.d(TAG, "setBlutoothListener() >> activity: " + activity);
        return (null != blutoothListener);
    }

    /**
     * 블루투스 리스너
     *
     * @return
     */
    public BlutoothListener getBlutoothListener() {
        return blutoothListener;
    }


    /**
     * 블루투스 장비 설정
     *
     * @param bluetoothDevice
     */
    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }


    /**
     * 쁠루투스 장비
     *
     * @return
     */
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    /**
     * 쁠루투스 장비
     *
     * @return
     */
    public boolean getIsBackground() {
        return isBackground;
    }

    /**
     * 쁠루투스 장비
     *
     * @return
     */
    public boolean setIsBackground(boolean isBackground) {
        this.isBackground = isBackground;
        return this.isBackground;
    }


    /**
     * UART Service
     *
     * @return
     */
    public UARTService getUARTService() {
        return uartService;
    }

    /**
     * BLE GATT 업데이트 인텐트 필터
     *
     * @return
     */
    private static IntentFilter gattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Definition.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Definition.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Definition.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Definition.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    /**
     * UART 서비스 시작
     */
    public void startUARTService() {
        LogUtil.d(TAG, "startUARTService() -> context : " + context);
        Intent intent = new Intent(context, UARTService.class);
        LogUtil.d(TAG, "startUARTService() -> intent : " + intent);
        context.bindService(intent, uartServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(context)
                .registerReceiver(uartStatusChangeReceiver, gattUpdateIntentFilter());
    }

    /**
     * UART 서비스 중지
     */
    public void stopUARTService() {
        if (null != uartService) {
            context.unbindService(uartServiceConnection);
            uartService.stopSelf();
            uartService = null;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        LogUtil.i(TAG, "onTrimMemory() -> Start !!!");
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            setIsBackground(true);
            LogUtil.d(TAG, "onTrimMemory() -> getIsBackground : " + getIsBackground());
        }
    }

    /**
     * BLE 장비 페어링 해제 요청
     */
    public void requestUnpairBluetooth() throws
            InvocationTargetException,
            IllegalAccessException,
            NoSuchMethodException {
    }

    /**
     * BLE 장비 페어링 요청
     */
    public void requestPairBluetooth() throws
            InvocationTargetException,
            IllegalAccessException,
            NoSuchMethodException {
        Method method = bluetoothDevice.getClass().getMethod("createBond", ( Class[] ) null);
        method.invoke(bluetoothDevice, ( Object[] ) null);
    }

    /**
     * 데이터 쓰기
     *
     * @param type
     * @return
     */
    public Result writeData(Type type) {
        LogUtil.e(TAG, "writeData() -> Start !!!");
        byte[] value = null;
        try {
            switch (type) {
                case FIRMWARE_INFORMATION:
                    value = ParserUtil.requestFirmwareInformation();
                    break;
                case WRITE_SETTING:
                    value = ParserUtil.requestWriteUserSetting();
                    break;
                default:
                    break;
            }
            if (null == value) return Result.NULL_VALUE;

            for (byte b : value) { //Steve_20191129  //byte=>unsigned byte
                LogUtil.e(TAG, "writeData() -> value : " + b);
            }

            if (null == getUARTService()) {
                LogUtil.w(TAG, "writeData() -> UARTService is null.");
                return Result.NULL_UART_SERVICE;
            }

            final byte[] finalValue = value;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() { //Steve_20181011 //Null Pointer Exception
                    if (null != App.getInstance().getUARTService()) {
                        App.getInstance().getUARTService().writeRXCharacteristic(finalValue);
                        App.getInstance().getUARTService().readRXCharacteristic();
                    }
                }
            }, Definition.GATT_INTERVAL_TIME); // 2초 후에 실행


        } catch (Exception e) {
            LogUtil.e(TAG, "writeData() -> Exception : " + e.getLocalizedMessage());
        }
        return Result.SUCCESS;
    }

    /**
     * 데이터 쓰기
     *
     * @param value
     * @return
     */
    public Result writeData(final byte[] value) {
        LogUtil.e(TAG, "writeData() -> Start !!!");
        if (null == value) return Result.NULL_VALUE;
        try {
            for (byte b : value) {
                LogUtil.e(TAG, "writeData() -> value : " + b);
            }

            if (null == getUARTService()) {
                LogUtil.w(TAG, "writeData() -> UARTService is null.");
                return Result.NULL_UART_SERVICE;
            }

            App.getInstance().getUARTService().writeRXCharacteristic(value);
            App.getInstance().getUARTService().readRXCharacteristic();

        } catch (Exception e) {
            LogUtil.e(TAG, "writeData() -> Exception : " + e.getLocalizedMessage());
        }
        return Result.SUCCESS;
    }

    /**
     * @param byteArray
     * @param type
     */
    public Result readData(byte[] byteArray, Type type) {
        LogUtil.i(TAG, "readData() -> Start !!!");
        if (null != byteArray)
            LogUtil.d(TAG, "readData() -> byteArray : " + byteArray + ", length : " + byteArray.length);

        if (byteArray.length == 5) {
            String hexadecimal = ParserUtil.byteArrayToHexadecimal(byteArray);
            LogUtil.d(TAG, "readData() -> hexadecimal : " + hexadecimal);

            String[] array = hexadecimal.split("\\p{Z}");
            LogUtil.d(TAG, "readData() -> array : " + array);

            String command = array[3];
            LogUtil.d(TAG, "readData() -> command : " + command);

            if (command.equals("07")) {
                Intent intent = new Intent(context, HotKeyBDoubleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else if (command.equals("08")) {
                Intent intent = new Intent(context, HotKeyBLongActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else if (command.equals("0a")) {
                Intent intent = new Intent(context, HotKeyCDoubleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else if (command.equals("0b")) {
                Intent intent = new Intent(context, HotKeyCLongActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

        LogUtil.d(TAG, "readData() -> type : " + type);
        // 펌웨어 정보 읽기
        switch (type) {
            case FIRMWARE_INFORMATION:
                LogUtil.d(TAG, "readData() -> byteArray : " + byteArray.length);
                if (null != byteArray
                        && byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH) {
                    return Result.SUCCESS;
                } else if (null != byteArray
                        && byteArray.length == 5) {
                    return Result.SUCCESS;
                }
                break;
            case WRITE_SETTING:
                if (null != byteArray
                        && byteArray.length == Definition.TOTAL_DATA_RESPONSE_WRITE_SETTING_LENGTH) {
                    return Result.SUCCESS;
                }
                break;
            default:
                break;
        }
        return Result.UNKNOWN_ERROR;
    }

    /**
     * 펌웨어 정보 저장
     *
     * @param byteArray
     * @return
     */
    public boolean setFirmwareInformationData(byte[] byteArray) {
        this.byteArray = byteArray;
        if (null == byteArray) if (null == this.byteArray) return true;
        if (null != byteArray) if (null != this.byteArray) return true;
        return false;
    }

    /**
     * 펌웨어 정보
     *
     * @return
     */
    public byte[] getFirmwareInformationData() {
        return this.byteArray;
    }

    /**
     * 디바이스 연결 여부 설정
     *
     * @param isConnectedDevice
     * @return
     */
    public boolean setConnectedDevice(boolean isConnectedDevice) {
        App.isConnectedDevice = isConnectedDevice;
        return App.isConnectedDevice;
    }

    /**
     * 디바이스 연결 여부
     *
     * @return
     */
    public boolean getConnectedDevice() {
        return isConnectedDevice;
    }

    /**
     * 연결된 블루투스 장비 설정
     *
     * @param connectedBluetoothDevice
     */
    public void setConnectedBluetoothDevice(BluetoothDevice connectedBluetoothDevice) {
        App.connectedBluetoothDevice = connectedBluetoothDevice;
    }

    /**
     * 연결된 쁠루투스 장비
     *
     * @return
     */
    public BluetoothDevice getConnectedBluetoothDevice() { return connectedBluetoothDevice; }


    /**
     * 펌웨어 정보 저장
     *
     * @param byteArray
     * @return
     */
    public void setFirmwareInformationDatas(byte[] byteArray) {
        this.arrayByteArray.add(byteArray);
    }

    /**
     * 펌웨어 정보
     *
     * @return
     */
    public ArrayList<byte[]> getFirmwareInformationDatas() {
        return this.arrayByteArray;
    }


    /**
     * 연결된 블루투스 장비 설정들
     *
     * @param arrayBluetoothDevice
     */
    public void setConnectedBluetoothDevices(BluetoothDevice arrayBluetoothDevice) {
        App.arrayBluetoothDevice.add(arrayBluetoothDevice);
    }

    /**
     * 연결된 쁠루투스 장비
     *
     * @return
     */
    public ArrayList<BluetoothDevice> getConnectedBluetoothDevices() { return  App.arrayBluetoothDevice; }
}

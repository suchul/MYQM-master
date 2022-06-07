package com.itvers.toolbox.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.itvers.toolbox.App;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class UARTService extends Service {
    private final static String TAG = UARTService.class.getSimpleName();    // 디버그 태그

    private BluetoothManager bluetoothManager;                              // BLE 매니저
    private BluetoothAdapter bluetoothAdapter;                              // BLE 어댑터
    private String bluetoothDeviceAddress;                                  // BLE 장치 주소
    private BluetoothGatt bluetoothGatt;                                    // BLE GATT

    /**
     * BLE GATT 콜백
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        /**
         * BLE연결 상태 변경
         *
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt,
                                            int status,
                                            int newState) {
            String action = null;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                action = Definition.ACTION_GATT_CONNECTED;
                if (null != bluetoothGatt)
                    LogUtil.i(TAG, "bluetoothGatt : " + bluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                action = Definition.ACTION_GATT_DISCONNECTED;
            }
            LogUtil.i(TAG, "onConnectionStateChange() -> action : " + action);
            // 브로드 캐스트 업데이트
            broadcastUpdate(action);
            if (null != action) LogUtil.i(TAG, action);
        }

        /**
         * BLE 서비스 발견
         *
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt,
                                         int status) {
            LogUtil.d(TAG, "onServicesDiscovered() -> status : " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (null != bluetoothGatt)
                    LogUtil.d(TAG, "onServicesDiscovered() -> bluetoothGatt : " + bluetoothGatt);
                // 브로드 캐스트 업데이트
                broadcastUpdate(Definition.ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        /**
         * 데이터 읽기
         *
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (null != bluetoothGatt)
                LogUtil.d(TAG, "onCharacteristicRead() -> bluetoothGatt : " + bluetoothGatt);
            if (null != gatt)
                LogUtil.d(TAG, "onCharacteristicRead() -> gatt : " + gatt);
            if (null != characteristic)
                LogUtil.d(TAG, "onCharacteristicRead() -> characteristic : " + characteristic);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 브로드 캐스트 업데이트
                broadcastUpdate(Definition.ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        /**
         * 데이터 변경
         *
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (null != bluetoothGatt)
                LogUtil.d(TAG, "onCharacteristicChanged() -> bluetoothGatt : " + bluetoothGatt);
            if (null != gatt)
                LogUtil.d(TAG, "onCharacteristicChanged() -> gatt : " + gatt);
            if (null != characteristic)
                LogUtil.d(TAG, "onCharacteristicChanged() -> characteristic : " + characteristic);
            broadcastUpdate(Definition.ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    /**
     * 브로드 캐스트 업데이트
     *
     * @param action
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * 브로드 캐스트 업데이트
     *
     * @param action
     * @param characteristic
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (Definition.UUID_TX_CHAR.equals(characteristic.getUuid())) {
            intent.putExtra(Definition.EXTRA_DATA, characteristic.getValue());

            try {
                LogUtil.d(TAG, "broadcastUpdate() -> characteristic : " + new String(characteristic.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LogUtil.e(TAG, "broadcastUpdate() -> UnsupportedEncodingException : " + e.getLocalizedMessage());
                // 브로드캐스트 업데이트
                broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            }
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public UARTService getService() {
            return UARTService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder iBinder = new LocalBinder();

    /**
     * BLE 매니저 초기화
     *
     * @return
     */
    public boolean initialization() {
        if (bluetoothManager == null) {
            bluetoothManager = ( BluetoothManager ) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                LogUtil.e(TAG, "initialization() -> bluetoothManager is null.");
                // 브로드캐스트 업데이트
                broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (null == bluetoothAdapter) {
            LogUtil.e(TAG, "initialization() -> bluetoothAdapter is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return false;
        }
        App.getInstance().setBluetoothAdapter(bluetoothAdapter);
        return true;
    }

    /**
     * BLE GATT 연결
     *
     * @param address
     * @return
     */
    public synchronized boolean connect(final String address) {
        LogUtil.i(TAG, "connect() -> Start !!!");
        if (bluetoothAdapter == null || address == null) {
            if (null == bluetoothAdapter) {
                LogUtil.e(TAG, "connect() ->  bluetoothAdapter is null.");
                // 브로드캐스트 업데이트
                broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            }
            if (null == address) {
                LogUtil.e(TAG, "connect() ->  address is null.");
                // 브로드캐스트 업데이트
                broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            }
            return false;
        }

        // TODO 테스트
//        if (null != bluetoothDeviceAddress
//                && address.equals(bluetoothDeviceAddress)
//                && bluetoothGatt != null) {
//            LogUtil.e(TAG, "connect() -> 이미 연결되어 있습니다.");
//
//            if (bluetoothGatt.connect()) return true;
//            return false;
//        }

        final BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        if (bluetoothDevice == null) {
            LogUtil.e(TAG, "connect() -> bluetoothDevice is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return false;
        }

        bluetoothGatt = bluetoothDevice.connectGatt(this, false, gattCallback);
        bluetoothDeviceAddress = address;
        boolean success = refreshDeviceCache(bluetoothGatt);
        LogUtil.d(TAG, "connect() -> success : " + success);
        LogUtil.d(TAG, "connect() -> bluetoothGatt : " + bluetoothGatt);
        LogUtil.d(TAG, "connect() -> bluetoothDeviceAddress : " + bluetoothDeviceAddress);

        App.getInstance().setBluetoothDevice(bluetoothDevice);

        return true;
    }

    private boolean refreshDeviceCache(BluetoothGatt bluetoothGatt) {
        LogUtil.i(TAG, "refreshDeviceCache() -> Start !!!");
        try {
            this.bluetoothGatt = bluetoothGatt;
            Method localMethod = this.bluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                return ((Boolean) localMethod.invoke(this.bluetoothGatt, new Object[0])).booleanValue();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "refreshDeviceCache() -> Exception : " + e.getLocalizedMessage());
        }
        return false;
    }

    /**
     * BLE GATT 연결 해제
     */
    public void disconnect() {
        LogUtil.i(TAG, "disconnect() -> Start !!!");
        if (bluetoothAdapter == null
                || bluetoothGatt == null) {
            if (null == bluetoothAdapter)
                LogUtil.e(TAG, "disconnect() ->  bluetoothAdapter is null.");
            if (null == bluetoothGatt) LogUtil.e(TAG, "disconnect() ->  bluetoothGatt is null.");
            return;
        }
        bluetoothGatt.disconnect();
        App.getInstance().setBluetoothDevice(null);
    }

    /**
     * BLE GATT 종료
     */
    public void close() {
        LogUtil.i(TAG, "close() -> Start !!!");
        if (null == bluetoothGatt) return;
        bluetoothDeviceAddress = null;
        bluetoothGatt.close();
        bluetoothGatt = null;
        App.getInstance().setBluetoothDevice(null);
    }

    /**
     * TXNotification 사용 설정
     *
     * @return
     */
    public void enableTXNotification() {
        if (null == bluetoothGatt) {
            LogUtil.e(TAG, "enableTXNotification() -> bluetoothGatt is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(Definition.UUID_RX_SERVICE);
        if (null == bluetoothGattService) {
            LogUtil.e(TAG, "enableTXNotification() -> bluetoothGattService is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic txCharacteristic = bluetoothGattService.getCharacteristic(Definition.UUID_TX_CHAR);
        if (null == txCharacteristic) {
            LogUtil.e(TAG, "enableTXNotification() -> txCharacteristic is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        bluetoothGatt.setCharacteristicNotification(txCharacteristic, true);

        BluetoothGattDescriptor descriptor = txCharacteristic.getDescriptor(Definition.UUID_CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * 데이터 쓰기
     *
     * @param value 데이터
     */
    public void writeRXCharacteristic(byte[] value) {
        LogUtil.i(TAG, "writeRXCharacteristic() -> Start !!!");
        LogUtil.d(TAG, "writeRXCharacteristic() -> value : " + new String(value));

        if (null == bluetoothGatt) {
            LogUtil.e(TAG, "writeRXCharacteristic() -> bluetoothGatt is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(Definition.UUID_RX_SERVICE);
        if (null == bluetoothGattService) {
            LogUtil.e(TAG, "writeRXCharacteristic() -> bluetoothGattService is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic rxCharacteristic = bluetoothGattService.getCharacteristic(Definition.UUID_RX_CHAR);
        if (null == rxCharacteristic) {
            LogUtil.e(TAG, "writeRXCharacteristic() -> rxCharacteristic is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        rxCharacteristic.setValue(value);
        boolean status = bluetoothGatt.writeCharacteristic(rxCharacteristic);
        LogUtil.d(TAG, "writeRXCharacteristic() -> status : " + status);
    }

    /**
     * 데이터 읽기
     */
    public void readRXCharacteristic() {
        if (null == bluetoothGatt) {
            LogUtil.e(TAG, "readRXCharacteristic() -> bluetoothGatt is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(Definition.UUID_RX_SERVICE);
        if (null == bluetoothGattService) {
            LogUtil.e(TAG, "readRXCharacteristic() -> bluetoothGattService is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }

        BluetoothGattCharacteristic rxCharacteristic = bluetoothGattService.getCharacteristic(Definition.UUID_RX_CHAR);
        if (null == rxCharacteristic) {
            LogUtil.e(TAG, "readRXCharacteristic() -> rxCharacteristic is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        boolean status = bluetoothGatt.readCharacteristic(rxCharacteristic);
        LogUtil.d(TAG, "readRXCharacteristic() -> status : " + status);
    }

    /**
     * 지원 가능한 GATT 서비스
     *
     * @return
     */
    public List <BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) {
            LogUtil.e(TAG, "getSupportedGattServices() -> bluetoothGatt is null.");
            // 브로드캐스트 업데이트
            broadcastUpdate(Definition.DEVICE_DOES_NOT_SUPPORT_UART);
            return null;
        }
        return bluetoothGatt.getServices();
    }

    /**
     * 연결된 장비 검색
     */
    public BluetoothDevice addBondedDevice(int activityType) {
        LogUtil.i(TAG, "addBondedDevice() -> Start !!!");
        final Set <BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) continue;
            String deviceName = device.getName();
            if (StringUtil.isNull(deviceName)) continue;
            if (deviceName.length() > 9) {
                deviceName = device.getName().substring(0, 9);
                if (activityType == Definition.ACTIVITY_MODE_PATCH) {
                    if (deviceName.equalsIgnoreCase(Definition.DEVICE_NAME_HEADER_SMARTPATCH)) {
                        return device;
                    }
                } else if (activityType == Definition.ACTIVITY_MODE_MOUSE) {
                    if (deviceName.equalsIgnoreCase(Definition.DEVICE_NAME_HEADER_SLIMMOUSE)) {
                        return device;
                    }
                }
            }
        }
        return null;
    }

    /**
     * BLE 사용 유무
     *
     * @return
     */
    public boolean isBluetoothAdapterEnabled() {
        if (null != bluetoothAdapter) {
            return (bluetoothAdapter.isEnabled());
        }
        return false;
    }
}

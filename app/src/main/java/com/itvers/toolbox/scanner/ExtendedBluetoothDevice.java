package com.itvers.toolbox.scanner;

import android.bluetooth.BluetoothDevice;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class ExtendedBluetoothDevice {
    public static final int NO_RSSI = -1000;
    public final BluetoothDevice bluetoothDevice;

    public String deviceName;
    public int rssi;
    public boolean isBonded;

    public ExtendedBluetoothDevice(final ScanResult scanResult) {
        this.bluetoothDevice = scanResult.getDevice();
        this.deviceName = scanResult.getScanRecord() != null ? scanResult.getScanRecord().getDeviceName() : null;
        this.rssi = scanResult.getRssi();
        this.isBonded = false;
    }

    public ExtendedBluetoothDevice(final BluetoothDevice device) {
        this.bluetoothDevice = device;
        this.deviceName = device.getName();
        this.rssi = NO_RSSI;
        this.isBonded = true;
    }

    public boolean matches(final ScanResult scanResult) {
        return bluetoothDevice.getAddress().equals(scanResult.getDevice().getAddress());
    }
}

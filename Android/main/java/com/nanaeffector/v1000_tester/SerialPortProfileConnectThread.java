// Author: Nana's Effector
// Date  : 2021/03/04


package com.nanaeffector.v1000_tester;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

public class SerialPortProfileConnectThread extends BluetoothConnectThread {

    // "00001101-0000-1000-8000-00805f9b34fb" = SPP (シリアルポートプロファイル) の UUID.
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public SerialPortProfileConnectThread(BluetoothDevice device, BluetoothConnectThreadListener listner) {
        super(device, SPP_UUID, listner);
        TAG = "SerialPortProfileConnectThread";
    }
}
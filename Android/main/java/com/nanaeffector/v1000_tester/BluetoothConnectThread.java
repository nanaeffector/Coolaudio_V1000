// Author: Nana's Effector
// Date  : 2021/03/04


package com.nanaeffector.v1000_tester;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.EventListener;
import java.util.UUID;

public class BluetoothConnectThread extends Thread {
    protected String TAG = "BluetoothConnectThread";
    protected final BluetoothSocket mmSocket;

    public interface BluetoothConnectThreadListener extends EventListener {
        void notifyConnectedStatus(boolean result, String status);
    }
    public BluetoothConnectThreadListener m_listener;

    private void setListner(BluetoothConnectThreadListener listner){
        m_listener = listner;
    }

    public void doNotify(boolean result, String status){
        if(m_listener != null){
            m_listener.notifyConnectedStatus(result, status);
        }
    }

    public BluetoothSocket getSocket() {
        return mmSocket;
    }

    public boolean isConnected(){
        return mmSocket.isConnected();
    }

    public void disconnect(){
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BluetoothConnectThread(BluetoothDevice device, UUID uuid, BluetoothConnectThreadListener listner) {
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            // NOP.
        }

        setListner(listner);
        mmSocket = tmp;
    }

    public void run() {
        if (mmSocket == null) {
            doNotify(false, "socket is null.");
            return;
        }

        try {
            mmSocket.connect();
        }
        catch (IOException e) {
            String msg = e.getMessage();
            e.printStackTrace();
            try {
                mmSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                msg += "\n" + e1.getMessage();
            }

            doNotify(false, msg);
            return;
        }

        doNotify(true, "connected");
    }
}

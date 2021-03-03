// Author: Nana's Effector
// Date  : 2021/03/04


package com.nanaeffector.v1000_tester;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.EventListener;

public class BluetoothSendTask extends Thread{
    public static final String TAG = "BluetoothSendTask";
    protected OutputStream mOutputStream;
    protected BluetoothSocket mSocket;
    protected volatile boolean mIsCancel;
    protected volatile String sendBuffer = "";

    public interface BluetoothSendListener extends EventListener{
        void notifySendEnd(String Data);
    };
    private BluetoothSendListener m_listener;

    private void setListener(BluetoothSendListener listener){
        m_listener = listener;
    }

    private void doNotify(String data){
        if(m_listener != null){
            m_listener.notifySendEnd(data);
        }
    }

    public boolean sendStart(String data){
        while(sendBuffer != ""){
            try {
                sleep(1);
            } catch(InterruptedException e){
                e.printStackTrace();
                return false;
            }
        }

        sendBuffer = data;
        return true;
    }

    public BluetoothSendTask(BluetoothSocket socket, BluetoothSendListener listener) {
        mIsCancel = false;
        mSocket = null;
        mOutputStream = null;
        if (socket == null) {
            Log.e(TAG, "parameter socket is null.");
            return;
        }

        sendBuffer = "";
        setListener(listener);
        try {
            mOutputStream = socket.getOutputStream();
            mSocket = socket;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Log.i(TAG, "start write task.");
        while (mOutputStream != null) {
            if (mIsCancel) {
                break;
            }

            try {
                if(sendBuffer != ""){
                    byte[] d = sendBuffer.getBytes(Charset.forName("ASCII"));
                    Log.i(TAG, "Start Send:" + sendBuffer);
                    mOutputStream.write(d, 0, sendBuffer.length());
                    sendBuffer = "";
                }

                Thread.sleep(1);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException!!");
                // NOP.
                break;
            }
        }
        Log.i(TAG, "exit write task.");
    }

    public void cancel() {
        mIsCancel = true;
    }

    public void finish() {
        if (mSocket == null) {
            return;
        }

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

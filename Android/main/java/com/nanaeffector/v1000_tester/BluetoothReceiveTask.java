// Author: Nana's Effector
// Date  : 2021/03/04


package com.nanaeffector.v1000_tester;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.io.InputStream;

class BluetoothReceiveTask extends Thread {
    public static final String TAG = "BluetoothReceiveTask";

    protected InputStream mInputStream;

    protected BluetoothSocket mSocket;

    protected volatile boolean mIsCancel;

    public interface BluetoothReceiveListener extends EventListener {
        void notifyReceive(byte[] dataArray, int length, String Data);
    }
    private BluetoothReceiveListener m_listener;

    private void setListener(BluetoothReceiveListener listener){
        m_listener = listener;
    }

    private void doNotify(byte[] dataArray, int length, String data){
        if(m_listener != null){
            m_listener.notifyReceive(dataArray, length, data);
        }
    }

    public BluetoothReceiveTask(BluetoothSocket socket, BluetoothReceiveListener listener) {
        mIsCancel = false;
        mSocket = null;
        mInputStream = null;
        if (socket == null) {
            Log.e(TAG, "parameter socket is null.");
            return;
        }

        setListener(listener);
        try {
            mInputStream = socket.getInputStream();
            mSocket = socket;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        byte[] buffer = new byte[64];
        ArrayList<Byte> RcvBuf = new ArrayList<>();
        int readSize;

        Arrays.fill(buffer, (byte)0);

        Log.i(TAG, "start read task.");
        while (mInputStream != null) {
            if (mIsCancel) {
                break;
            }

            try {
                readSize = mInputStream.read(buffer);
                if(readSize > 0){
                    for (int x=0; x<readSize; x++) {
                        byte b = buffer[x];
                        if (b != 0 && b != '\r' && b != '\n') {
                            RcvBuf.add(b);
                        } else if (b != '\n') {
                            String str = "l:" + readSize + ". " + new String(buffer, 0, readSize, StandardCharsets.US_ASCII);
                            byte[] byteBuf = new byte[RcvBuf.size()];
                            for(int i=0; i<RcvBuf.size(); i++){
                                byteBuf[i] = RcvBuf.get(i);
                            }
                            doNotify(byteBuf, byteBuf.length, str);
                            RcvBuf.clear();
                        }
                    }
                }
                Arrays.fill(buffer, (byte)0);
                Thread.sleep(0);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException!!");
                // NOP.
                break;
            }
        }
        Log.i(TAG, "exit read task.");
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

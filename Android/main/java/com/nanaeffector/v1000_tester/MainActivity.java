// Author: Nana's Effector
// Date  : 2021/03/04


package com.nanaeffector.v1000_tester;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.nanaeffector.v1000_tester.BluetoothConnectThread.BluetoothConnectThreadListener;
import com.nanaeffector.v1000_tester.BluetoothReceiveTask.BluetoothReceiveListener;
import com.nanaeffector.v1000_tester.BluetoothSendTask.BluetoothSendListener;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    protected String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;
    SerialPortProfileConnectThread connectThread = null;
    private BluetoothReceiveTask mReceiveTask;
    private BluetoothSendTask mSendTask;
    boolean isConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // connectボタン.
        Button mButton_Connect = (Button)findViewById( R.id.button_connect );
        mButton_Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnect){
                    v.setEnabled(false);
                    isConnect = connectBluetoothSerial("V1000_Efcector");
                }
            }
        });

        // seekbar depth.
        SeekBar seekBar = findViewById(R.id.seekbar_depth);
        seekBar.setProgress(0);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(seekbarChangeListener);

        // seekbar speed.
        seekBar = findViewById(R.id.seekbar_speed);
        seekBar.setProgress(0);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(seekbarChangeListener);

        // seekbar mix.
        seekBar = findViewById(R.id.seekbar_mix);
        seekBar.setProgress(0);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(seekbarChangeListener);

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // 切断
        disconnect();
    }

    // アクティビティの終了直前
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // 切断
        disconnect();
    }

    private void disconnect(){
        if(connectThread != null && connectThread.isConnected()){
            mReceiveTask.finish();

            connectThread.disconnect();
            isConnect = false;

            Button mButton_Connect = (Button)findViewById( R.id.button_connect );
            mButton_Connect.setEnabled(true);
        }
    }

    private boolean connectBluetoothSerial(String deviceName){
        if(connectThread != null && connectThread.isConnected()){
            Log.i(TAG, "already connected.");
            return true;
        }

        // Bluetoothアダプタを取得する.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // エラー: Bluetooth なし.
            Log.i(TAG, "nothing bluetooth.");
            return false;
        }

        // 対象 Bluetooth デバイスのシリアルポートプロファイルに接続する.
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // ペアリング済みデバイス一覧の中から接続するデバイスを検索する.
        connectThread = null;
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                if (deviceName.equals(device.getName())) {
                    connectThread = new SerialPortProfileConnectThread(device, btConnectListner);
                    break;
                }
            }
        }

        if (connectThread == null) {
            // エラー: 接続対象のペアリング済みデバイスなし.
            Log.i(TAG, "nothing pairing device:" + deviceName);
            return false;
        }

        // 接続前に検索を終了させる.
        mBluetoothAdapter.cancelDiscovery();
        // 接続開始.
        connectThread.start();
        return true;
    }

    void updateSeekbar(){
        mSendTask.sendStart("BA\n");
        mSendTask.sendStart("BB\n");
        mSendTask.sendStart("BC\n");
    }

    BluetoothConnectThreadListener btConnectListner = new BluetoothConnectThreadListener(){
        @Override
        public void notifyConnectedStatus(boolean result, String status) {
            Log.i(TAG, status);

            isConnect = result;
            if(isConnect) {
                // 受信タスク開始.
                mReceiveTask = new BluetoothReceiveTask(connectThread.getSocket(), btReceiveListener);
                mReceiveTask.start();

                // 送信タスク開始.
                mSendTask = new BluetoothSendTask(connectThread.getSocket(), btSendListener);
                mSendTask.start();

                // seekbar更新.
                updateSeekbar();
            }
        }
    };

    BluetoothReceiveListener btReceiveListener = new BluetoothReceiveListener() {
        @Override
        public void notifyReceive(byte[] dataArray, int length, String Data) {
            String msg = "rcv:";
            for(int i=0; i<length; i++){
                msg += String.format("%02x ", dataArray[i]);
            }
            Log.i(TAG, msg);

            String strval;
            int val = 0;
            SeekBar seekBar = null;
            if(dataArray[0] == 'B'){
                // ヘッダ一致.
                if(dataArray.length >= 3) {
                    // データ十分.
                    switch (dataArray[1]) {
                        case 'A':   // read mix.
                            strval = new String(dataArray, 2, length - 2, StandardCharsets.US_ASCII);
                            try {
                                val = Integer.parseInt(strval);
                                seekBar = findViewById(R.id.seekbar_mix);
                            } catch (NumberFormatException e) {
                                Log.i(TAG, "err:data");
                            }
                            break;
                        case 'B':   // read depth.
                            strval = new String(dataArray, 2, length - 2, StandardCharsets.US_ASCII);
                            try {
                                val = Integer.parseInt(strval);
                                seekBar = findViewById(R.id.seekbar_depth);
                            } catch (NumberFormatException e) {
                                Log.i(TAG, "err:data");
                            }
                            break;
                        case 'C':   // read speed.
                            strval = new String(dataArray, 2, length - 2, StandardCharsets.US_ASCII);
                            try {
                                val = Integer.parseInt(strval);
                                seekBar = findViewById(R.id.seekbar_speed);
                            } catch (NumberFormatException e) {
                                Log.i(TAG, "err:data");
                            }
                            break;
                        default:
                            Log.i(TAG, "err:cmd");
                            break;
                    }

                    if (seekBar != null) {
                        seekBar.setProgress(val);
                    }
                }
                else{
                    // データ不足.
                    Log.i(TAG, "err:dataLength");
                }
            }
            else{
                Log.i(TAG, "err:headder");
            }
        }
    };

    BluetoothSendListener btSendListener = new BluetoothSendListener() {
        @Override
        public void notifySendEnd(String Data) {
            Log.i(TAG, "send:"+Data);
        }
    };

    SeekBar.OnSeekBarChangeListener seekbarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        //ツマミがドラッグされると呼ばれる
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(!isConnect){
                Log.i(TAG, "bluetooth disconnecting.");
                return;
            }

            String cmd = "";
            String readback = "";
            switch(seekBar.getId()){
                case R.id.seekbar_depth:
                    if(fromUser) {
                        cmd = String.format("BD%d\n", progress);
                        readback = "BB\n";
                    }
                    break;
                case R.id.seekbar_speed:
                    cmd = String.format("BE%d\n", progress);
                    readback = "BC\n";
                    break;
                case R.id.seekbar_mix:
                    cmd = String.format("BF%d\n", progress);
                    break;
                default:
                    break;
            }

            mSendTask.sendStart(cmd);
            //mSendTask.sendStart(readback);
        }

        //ツマミがタッチされた時に呼ばれる
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        //ツマミがリリースされた時に呼ばれる
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    };
}

package com.khmelenko.lab.bluetootharduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Main application activity
 *
 * @author Dmytro Khmelenko
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Arduino_BT";

    @Bind(R.id.btnOn)
    private Button mBtnOn;
    @Bind(R.id.btnOff)
    private Button mBtnOff;

    private TextView mStatusView;

    private Handler mUiHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    final int RECIEVE_MESSAGE = 1;

    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket mBtSocket;
    private StringBuilder sb = new StringBuilder();

    private CommunicationThread mConnectedThread;

    private static final UUID CLIENT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String MAC_ADDRESS = "00:0E:EA:CF:1A:83";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mStatusView = (TextView) findViewById(R.id.txtArduino);

        mUiHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
//                        try {
//                            strIncom = new String(readBuf, "US-ASCII");
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
                        sb.append(strIncom);
                        int endOfLineIndex = sb.indexOf("\r\n");
//                        if (endOfLineIndex > 0) {
//                            String sbprint = sb.substring(0, endOfLineIndex);
//                            sb.delete(0, sb.length());
//                            mStatusView.setText("Arduino: " + sbprint);
//                            mBtnOff.setEnabled(true);
//                            mBtnOn.setEnabled(true);
//                        }
                        mStatusView.setText("Arduino: " + sb);
                        break;
                }
            }

            ;
        };

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();
    }

    @Override
    public void onResume() {
        super.onResume();

        BluetoothDevice device = mBtAdapter.getRemoteDevice(MAC_ADDRESS);
        try {
            mBtSocket = device.createRfcommSocketToServiceRecord(CLIENT_UUID);
        } catch (IOException e) {
            Log.d(TAG, "Socket create failed\n" + e.getMessage());
        }

        mBtAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "Connecting...");
        try {
            mBtSocket.connect();
            Log.d(TAG, "Connected");
        } catch (IOException e) {
            closeConnection();
        }

        // start communication thread
        mConnectedThread = new CommunicationThread(mBtSocket, mUiHandler);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        closeConnection();
    }

    @OnClick(R.id.btnOn)
    public void handleOnBtn() {
        // TODO Use another commands
        mConnectedThread.send("1");
    }

    @OnClick(R.id.btnOff)
    public void handleOffBtn() {
        // TODO Use another commands
        mConnectedThread.send("0");
    }

    /**
     * Closes socket connection
     */
    private void closeConnection() {
        try {
            mBtSocket.close();
        } catch (IOException e) {
            Log.d(TAG, "Failed to close connection\n" + e.getMessage());
        }
    }

    /**
     * Checks Bluetooth state
     */
    private void checkBluetoothState() {
        if (mBtAdapter != null) {
            if (!mBtAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

}

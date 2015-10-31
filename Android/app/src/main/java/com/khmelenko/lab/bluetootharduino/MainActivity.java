package com.khmelenko.lab.bluetootharduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Main application activity
 *
 * @author Dmytro Khmelenko
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Arduino_BT";

    private static final int REQUEST_ENABLE_BT = 0;

    @Bind(R.id.btnOn)
    public Button mBtnOn;
    @Bind(R.id.btnOff)
    public Button mBtnOff;
    @Bind(R.id.txtArduino)
    public TextView mStatusView;

    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket mBtSocket;
    private CommunicationThread mConnectedThread;

    private Handler mUiHandler;

    private static final UUID CLIENT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String MAC_ADDRESS = "98:D3:31:70:4D:E3";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUiHandler = new UiHandler(this);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        checkBluetoothState();
    }

    @Override
    public void onResume() {
        super.onResume();

        BluetoothDevice device = mBtAdapter.getRemoteDevice(MAC_ADDRESS);
        try {
            mBtSocket = device.createRfcommSocketToServiceRecord(CLIENT_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Socket create failed\n" + e.getMessage());
        }

        mBtAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "Connecting...");
        try {
            mBtSocket.connect();
            Log.d(TAG, "Connected");
        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * Handler for UI
     */
    private static class UiHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;

        public UiHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();

            switch (msg.what) {
                case CommunicationThread.RECEIVE_MESSAGE:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readStr = new String(readBuf, 0, msg.arg1);
                    try {
                        readStr = new String(readBuf, "US-ASCII");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Received message: " + readStr);

                    String responseData = activity.mStatusView.getText().toString();
                    responseData += readStr;
                    activity.mStatusView.setText(responseData);
                    break;
            }
        }
    }

}

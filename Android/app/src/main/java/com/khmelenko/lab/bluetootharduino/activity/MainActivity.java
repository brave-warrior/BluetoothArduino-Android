package com.khmelenko.lab.bluetootharduino.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.khmelenko.lab.bluetootharduino.BtApplication;
import com.khmelenko.lab.bluetootharduino.R;
import com.khmelenko.lab.bluetootharduino.connectivity.ConnectionService;
import com.khmelenko.lab.bluetootharduino.connectivity.OnConnectionListener;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Main application activity
 *
 * @author Dmytro Khmelenko
 */
public class MainActivity extends AppCompatActivity implements OnConnectionListener {

    private static final int REQUEST_ENABLE_BT = 0;

    @Bind(R.id.main_send_command_btn)
    public Button mSendCommandBtn;
    @Bind(R.id.main_search_btn)
    public Button mSearchBtn;
    @Bind(R.id.txtArduino)
    public TextView mStatusView;

    private BluetoothAdapter mBtAdapter;

    private ConnectionService mConnectionService;
    private Handler mUiHandler;

    private static final String MAC_ADDRESS = "98:D3:31:70:4D:E3";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolbar();

        mUiHandler = new UiHandler(this);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBluetoothState();

        checkConnection();
    }

    /**
     * Checks connection with the device
     */
    private void checkConnection() {
        mConnectionService = ((BtApplication) getApplication()).getConnectionService();

        // TODO Connect to saved device
        if(mConnectionService != null) {
            if(!mConnectionService.isConnected()) {
                BluetoothDevice device = mBtAdapter.getRemoteDevice(MAC_ADDRESS);
                mConnectionService.connect(device, mUiHandler, this);
            } else {
                mConnectionService.setReceiver(mUiHandler);
            }
        }
    }

    @OnClick(R.id.main_send_command_btn)
    public void handleSendCommand() {
        checkConnection();

        // TODO Use another commands
        mConnectionService.send("1");
    }

    @OnClick(R.id.main_search_btn)
    public void handleSearch() {
        Intent intent = new Intent(this, SearchDevicesActivity.class);
        startActivity(intent);
    }

    /**
     * Initializes toolbar
     */
    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

    @Override
    public void onConnected(BluetoothDevice device) {
        // TODO Enable UI controls for communication
    }

    @Override
    public void onFailed() {
        Toast.makeText(this, R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
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
                case ConnectionService.RECEIVE_MESSAGE:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readStr = new String(readBuf, 0, msg.arg1);
                    try {
                        readStr = new String(readBuf, "US-ASCII");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d(BtApplication.TAG, "Received message: " + readStr);

                    String responseData = activity.mStatusView.getText().toString();
                    responseData += readStr;
                    activity.mStatusView.setText(responseData);
                    break;
            }
        }
    }

}

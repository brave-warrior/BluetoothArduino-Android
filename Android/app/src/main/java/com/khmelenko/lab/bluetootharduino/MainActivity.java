package com.khmelenko.lab.bluetootharduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "bluetooth2";

    private Button mBtnOn;
    private Button mBtnOff;
    private TextView mStatusView;

    private Handler h;

    private static final int REQUEST_ENABLE_BT = 1;
    final int RECIEVE_MESSAGE = 1;

    private BluetoothAdapter mBtAdapter;
    private BluetoothSocket mBtSocket;
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String sMacAddress = "00:0E:EA:CF:1A:83";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mBtnOn = (Button) findViewById(R.id.btnOn);
        mBtnOff = (Button) findViewById(R.id.btnOff);
        mStatusView = (TextView) findViewById(R.id.txtArduino);

        h = new Handler() {
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
        checkBTState();

        mBtnOn.setOnClickListener(new View.OnClickListener() {        // ���������� ���������� ��� ������� �� ������
            public void onClick(View v) {
                mConnectedThread.write("1");
            }
        });

        mBtnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up a pointer to the remote node using it's sMacAddress.
        BluetoothDevice device = mBtAdapter.getRemoteDevice(sMacAddress);

        // Two things are needed to make a connection:
        //   A MAC sMacAddress, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            mBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        mBtAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "Establish");
        try {
            mBtSocket.connect();
            Log.d(TAG, "Connecting");
        } catch (IOException e) {
            try {
                mBtSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "Socket");

        mConnectedThread = new ConnectedThread(mBtSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            mBtSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (mBtAdapter == null) {
            errorExit("Fatal Error", "Bluetooth null");
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "BT enabled");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String message) {
            Log.d(TAG, "Writing message: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "Exception while writing: " + e.getMessage() + "...");
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

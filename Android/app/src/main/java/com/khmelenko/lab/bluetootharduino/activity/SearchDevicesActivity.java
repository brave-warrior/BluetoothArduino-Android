package com.khmelenko.lab.bluetootharduino.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.khmelenko.lab.bluetootharduino.BtApplication;
import com.khmelenko.lab.bluetootharduino.R;
import com.khmelenko.lab.bluetootharduino.adapter.DevicesListAdapter;
import com.khmelenko.lab.bluetootharduino.adapter.OnListItemListener;
import com.khmelenko.lab.bluetootharduino.connectivity.reactive.ConnectionService;
import com.khmelenko.lab.bluetootharduino.connectivity.OnConnectionListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;

/**
 * Activity for searching bluetooth devices
 *
 * @author Dmytro Khmelenko
 */
public class SearchDevicesActivity extends AppCompatActivity implements OnListItemListener, OnConnectionListener {

    @Bind(R.id.search_devices_recycler_view)
    RecyclerView mDevicesRecyclerView;

    @Bind(R.id.progressbarview)
    View mProgressBar;

    private ProgressDialog mProgressDialog;

    private DevicesListAdapter mDevicesListAdapter;
    private List<BluetoothDevice> mDevices;

    private BluetoothAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_devices);
        ButterKnife.bind(this);
        initToolbar();

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        mDevices = new ArrayList<>();
        mDevicesRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mDevicesRecyclerView.setLayoutManager(layoutManager);

        mDevicesListAdapter = new DevicesListAdapter(this, mDevices, this);
        mDevicesRecyclerView.setAdapter(mDevicesListAdapter);

        registerSearchResultsReceiver();

        mBtAdapter.startDiscovery();
    }

    /**
     * Registers the broadcast receiver for the search results
     */
    private void registerSearchResultsReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mBtAdapter.cancelDiscovery();
    }

    @Override
    public void onItemSelected(int position) {
        BluetoothDevice device = mDevices.get(position);
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.message_connecting));
            ConnectionService connectionService = ((BtApplication) getApplication()).getConnectionService();
            connectionService.connect(device, prepareConnectionSubscriber());
        } else {
            // notify that pairing required
            Toast.makeText(this, R.string.error_device_not_paired, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Initializes toolbar
     */
    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(BtApplication.TAG, "Receiver action: " + action);

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        // TODO Handle different states
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevices.add(device);
                    mDevicesListAdapter.notifyDataSetChanged();
                    Log.d(BtApplication.TAG, String.format("Found device: %s, %s", device.getName(), device.getAddress()));
                    break;
            }
        }
    };

    @Override
    public void onConnected(BluetoothDevice device) {
        // TODO Save connected device
        mProgressDialog.dismiss();
        finish();
    }

    @Override
    public void onFailed() {
        mProgressDialog.dismiss();
        Toast.makeText(this, R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
    }

    /**
     * Prepares subscriber for handling connection
     *
     * @return Connection subscriber
     */
    private Subscriber<BluetoothDevice> prepareConnectionSubscriber() {
        Subscriber<BluetoothDevice> subscriber = new Subscriber<BluetoothDevice>() {
            @Override
            public void onCompleted() {
                mProgressDialog.dismiss();
                finish();
            }

            @Override
            public void onError(Throwable e) {
                mProgressDialog.dismiss();
                Toast.makeText(SearchDevicesActivity.this, R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(BluetoothDevice device) {
                Log.d(BtApplication.TAG, "Connection Subscriber: " + device.getName());
                // TODO Save connected device
            }
        };
        return subscriber;
    }
}

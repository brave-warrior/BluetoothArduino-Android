package com.khmelenko.lab.bluetootharduino.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.khmelenko.lab.bluetootharduino.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * List adapter for devices
 *
 * @author Dmytro Khmelenko
 */
public class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.DeviceViewHolder> {

    private List<BluetoothDevice> mDevices;
    private final Context mContext;
    private final OnListItemListener mListener;

    public DevicesListAdapter(Context context, List<BluetoothDevice> devices, OnListItemListener listener) {
        mContext = context;
        mDevices = devices;
        mListener = listener;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        BluetoothDevice device = mDevices.get(position);

        holder.mDeviceName.setText(device.getName());
        holder.mDeviceAddress.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    /**
     * Viewholder class
     */
    class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.card_view)
        View mParent;

        @Bind(R.id.item_device_name)
        TextView mDeviceName;

        @Bind(R.id.item_device_address)
        TextView mDeviceAddress;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mParent.setOnClickListener(this);
            itemView.setClickable(true);
        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onItemSelected(getLayoutPosition());
            }
        }
    }
}

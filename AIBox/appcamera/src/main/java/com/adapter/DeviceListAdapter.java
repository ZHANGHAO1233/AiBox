package com.adapter;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends SpinnerAdapter {

    private final LayoutInflater mInflater;
    private final List<UsbDevice> mList;
    private final Context mContext;

    public DeviceListAdapter(final Context context, final List<UsbDevice> list) {
        mInflater = LayoutInflater.from(context);
        mList = list != null ? list : new ArrayList<>();
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public UsbDevice getItem(final int position) {
        if ((position >= 0) && (position < mList.size()))
            return mList.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        TextView tv;
        UsbDevice device = getItem(position);
        if (convertView == null) {
            tv = new TextView(mContext);
            tv.setPadding(5, 15, 5, 15);
            tv.setTag(device);
        } else {
            tv = (TextView) convertView;
            tv.setTag(device);
        }
        tv.setText(String.format("UVC Camera:(%s)", device.getSerialNumber()));
        return tv;
    }

    @Override
    public String getName(int position) {
        UsbDevice device = getItem(position);
        return String.format("UVC Camera:(%s)", device.getSerialNumber());
    }
}
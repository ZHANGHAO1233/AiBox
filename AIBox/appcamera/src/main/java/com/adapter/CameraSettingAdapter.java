package com.adapter;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bean.Tuple2;
import com.idata.aibox.R;
import com.widget.DropDownListView;

import java.util.List;

/**
 * @author heyb
 * 已点列表标签适配器
 */
public class CameraSettingAdapter extends BaseAdapter {

    public List<Tuple2<Integer, Tuple2<List<UsbDevice>, Integer>>> list;
    public Context context;
    private OnCameraSettingListener listener;

    public CameraSettingAdapter(List<Tuple2<Integer, Tuple2<List<UsbDevice>, Integer>>> list,
                                Context context) {
        super();
        this.list = list;
        this.context = context;
    }

    public int getCount() {
        return list.size();
    }

    public void setListener(OnCameraSettingListener listener) {
        this.listener = listener;
    }

    @Override
    public Tuple2<Integer, Tuple2<List<UsbDevice>, Integer>> getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View contextView, ViewGroup viewGroup) {
        int resource = R.layout.layout_item_camera_floor;
        View view = LayoutInflater.from(context).inflate(resource, viewGroup, false);
        TextView tv_floor = view.findViewById(R.id.tv_floor);
        DropDownListView spinner_camera = view.findViewById(R.id.spinner_camera);
        Tuple2<List<UsbDevice>, Integer> usbDevices = getItem(position).getValue2();
        spinner_camera.setAdapter(new DeviceListAdapter(context, usbDevices.getValue1()));
        tv_floor.setText("第" + (position + 1) + "层");
        if (usbDevices.getValue1() != null && usbDevices.getValue1().size() > 0) {
            spinner_camera.setItemSelectedListener((adapter, parent, view1, position1, id) -> {
                if (listener != null) {
                    listener.onCameraSetting(position + 1, usbDevices.getValue1().get(position1));
                }
            });
            spinner_camera.setSelection(usbDevices.getValue2() == null ? -1 : usbDevices.getValue2());
        }
        return view;
    }

    public interface OnCameraSettingListener {
        void onCameraSetting(int floor, UsbDevice usbDevice);
    }
}

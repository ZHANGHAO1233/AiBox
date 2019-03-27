package com.notebook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adapter.LogAdapter;
import com.adapter.RetailInputParamAdapter;
import com.bean.CloseParam;
import com.bean.OpenParam;
import com.bean.Order;
import com.bean.UsbDevcieEntity;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.idata.aibox.R;
import com.lib.sdk.bean.StringUtils;
import com.mgr.ConfigPropertiesManager;
import com.mgr.OrderFileManager;
import com.mgr.serial.comn.util.GsonUtil;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.UVCCameraTextureView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.box.utils.ILog.TIME_TAG;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_1;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_2;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_3;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_4;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_CAMREA_PATHS;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_INITED;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_MESS;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_ORDER;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_PARMA;

public class NotebookActivity extends android.app.Activity implements View.OnClickListener {
    private static final float[] BANDWIDTH_FACTORS = {0.5f, 0.5f};
    private TextView tv_order_no;
    private ListView lv_open_params;
    private ListView lv_close_params;
    private ListView lv_time_log;
    private Button btn_setting;
    private UVCCameraTextureView floor_1;
    private UVCCameraTextureView floor_2;
    private UVCCameraTextureView floor_3;
    private UVCCameraTextureView floor_4;
    private BdManager nh;
    public RequestParamsHandler handler = new RequestParamsHandler();
    private List<String> logs;
    private LogAdapter logAdapter;
    private Order order;
    public static USBMonitor usbMonitor;
    private List<UsbDevcieEntity> handlers;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        nh = BdManager.getBd();
        nh.init(handler);
        ILog.setTagHandler(TIME_TAG, handler);
        OsModule.get().setHandler(handler);
        initView();
    }

    private void initView() {
        this.tv_order_no = findViewById(R.id.tv_order_no);
        this.lv_open_params = findViewById(R.id.lv_open_params);
        this.lv_close_params = findViewById(R.id.lv_close_params);
        this.lv_time_log = findViewById(R.id.lv_time_log);
        this.btn_setting = findViewById(R.id.btn_setting);
        this.floor_1 = findViewById(R.id.camera_floor_1_view);
        this.floor_2 = findViewById(R.id.camera_floor_2_view);
        this.floor_3 = findViewById(R.id.camera_floor_3_view);
        this.floor_4 = findViewById(R.id.camera_floor_4_view);
        this.btn_setting.setOnClickListener(this);
        findViewById(R.id.download1).setOnClickListener(this);
        findViewById(R.id.download2).setOnClickListener(this);
        findViewById(R.id.order_succ).setOnClickListener(this);
        findViewById(R.id.order_fail).setOnClickListener(this);
        this.usbMonitor = new USBMonitor(this, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(UsbDevice device) {
                Toast.makeText(NotebookActivity.this, device.getSerialNumber() + " Attach", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDettach(UsbDevice device) {

            }

            @Override
            public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                for (UsbDevcieEntity entity : handlers) {
                    if (entity.getUsbDevice().getSerialNumber().equals(device.getSerialNumber())) {
                        if (entity.getSurface() == null) {
                            Toast.makeText(NotebookActivity.this, device.getSerialNumber() + " Connected", Toast.LENGTH_SHORT).show();
                            UVCCameraHandler cameraHandler = entity.getHandler();
                            UVCCameraTextureView view = entity.getCameraTextureView();
                            cameraHandler.open(ctrlBlock);
                            SurfaceTexture st = view.getSurfaceTexture();
                            Surface surface = new Surface(st);
                            entity.setSurface(surface);
                            cameraHandler.startPreview(surface);
                        }
                    }
                }
            }

            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                for (UsbDevcieEntity entity : handlers) {
                    if (entity.getUsbDevice().getSerialNumber().equals(device.getSerialNumber())) {
                        UVCCameraHandler handler = entity.getHandler();
                        handler.close();
                        Surface surface = entity.getSurface();
                        if (surface != null)
                            surface.release();
                    }
                }
            }

            @Override
            public void onCancel(UsbDevice device) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.handlers = new ArrayList<>();
        this.usbMonitor.register();
        List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
        List<UsbDevice> usbDevices = usbMonitor.getDeviceList(filter.get(0));
        Map<String, String> cameraSet = this.getCameraSeting();
        if (cameraSet.get(SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_1) != null)
            getFloorHandler(SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_1, floor_1, usbDevices, cameraSet);
        if (cameraSet.get(SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_2) != null)
            getFloorHandler(SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_2, floor_2, usbDevices, cameraSet);
        if (cameraSet.get(SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_3) != null)
            getFloorHandler(SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_3, floor_3, usbDevices, cameraSet);
        if (cameraSet.get(SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_4) != null)
            getFloorHandler(SETTING_CONFIG_PROPERTY_CAMREA_FLOOR_KEY_4, floor_4, usbDevices, cameraSet);
    }

    private void getFloorHandler(String floor, UVCCameraTextureView textureView, List<UsbDevice> usbDevices,
                                 Map<String, String> cameraSet) {
        //设置显示宽高
        textureView.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        UVCCameraHandler handler = UVCCameraHandler.createHandler(this, textureView
                , UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT
                , BANDWIDTH_FACTORS[0], data -> BdManager.getBd().putLastCameraImage(floor, data));
        textureView.onResume();
        new Handler().postDelayed(() -> {
            for (UsbDevice usb : usbDevices) {
                if (cameraSet.get(floor).equals(usb.getSerialNumber())) {
                    this.handlers.add(new UsbDevcieEntity(floor, usb, textureView, handler));
                    usbMonitor.requestPermission(usb);//获取设备信息，并检查打开此设备的权限
                }
            }
        }, 500);
    }

    private Map<String, String> getCameraSeting() {
        String s_paths = ConfigPropertiesManager.getInstance().getConfigProperty(SETTING_CONFIG_PROPERTY_CAMREA_PATHS);
        Map<String, String> paths;
        if (!StringUtils.isStringNULL(s_paths)) {
            paths = GsonUtil.fromJson(s_paths, Map.class);
        } else {
            paths = new HashMap<>();
        }
        return paths;
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (UsbDevcieEntity entity : handlers) {
            Surface surface = entity.getSurface();
            UVCCameraHandler handler = entity.getHandler();
            //调用会报错
            // if (handler != null)
            //handler.close();
            if (surface != null)
                surface.release();
        }
        this.floor_1.onPause();
        this.floor_2.onPause();
        this.floor_3.onPause();
        this.floor_4.onPause();
        usbMonitor.unregister();//usb管理器解绑
        this.handlers = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BdManager.getBd().closeAllWeightSerials();
        if (usbMonitor != null) {
            usbMonitor.destroy();
        }
        usbMonitor = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.download1:
                if (handler != null) {
                    Message message = new Message();
                    message.what = HANDLER_MESSAGE_WHAT_INITED;
                    handler.sendMessage(message);
                }
                nh.testOpen();
                break;
            case R.id.download2:
                nh.testClose();
                break;
            case R.id.order_succ:
                if (order != null) {
                    try {
                        OrderFileManager.getInstance().writeOrder(true, order);
                        BdManager.getBd().updateOrderResult(true, order);
                        Toast.makeText(this, "订单" + order.getOrder() + "写入成功", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "订单" + order.getOrder() + "写入失败" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    order = null;
                }
                break;
            case R.id.order_fail:
                if (order != null) {
                    try {
                        OrderFileManager.getInstance().writeOrder(false, order);
                        BdManager.getBd().updateOrderResult(false, order);
                        Toast.makeText(this, "订单" + order.getOrder() + "写入成功", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "订单" + order.getOrder() + "写入失败" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    order = null;
                }
                break;
            case R.id.btn_setting:
                Intent intent = new Intent();
                intent.setClass(this, SettingActivity.class);
                this.startActivity(intent);
                break;
        }
    }

    class RequestParamsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MESSAGE_WHAT_MESS:
                    String mess = (String) msg.obj;
                    updateLog(mess);
                    break;
                case HANDLER_MESSAGE_WHAT_PARMA:
                    if (msg.obj != null) {
                        if (msg.obj instanceof OpenParam) {
                            updateOpenParam((OpenParam) msg.obj);
                        } else if (msg.obj instanceof CloseParam) {
                            updateCloseParam((CloseParam) msg.obj);
                        }
                    }
                    break;
                case HANDLER_MESSAGE_WHAT_INITED:
                    initLogView();
                    break;
                case HANDLER_MESSAGE_WHAT_ORDER:
                    NotebookActivity.this.order = (Order) msg.obj;
                    break;
            }
        }
    }

    private void initLogView() {
        if (this.logs != null) {
            this.logs.clear();
        }
        if (this.logAdapter != null) {
            this.logAdapter.notifyDataSetChanged();
        }
    }

    private void updateLog(String mess) {
        if (this.logs == null) {
            this.logs = new ArrayList<>();
        }
        this.logs.add(mess);
        if (this.logAdapter == null) {
            this.logAdapter = new LogAdapter(this.logs, this);
            this.lv_time_log.setAdapter(this.logAdapter);
        } else {
            this.logAdapter.notifyDataSetChanged();
        }
    }

    private void updateCloseParam(CloseParam param) {
        this.lv_close_params.setAdapter(new RetailInputParamAdapter(param.getRetailInputParams(), this));
        this.updateLog(" ");
    }

    @SuppressLint("SetTextI18n")
    private void updateOpenParam(OpenParam param) {
        this.lv_close_params.setAdapter(new RetailInputParamAdapter(new ArrayList<>(), this));
        this.tv_order_no.setText("订单号 " + param.getOrder_no());
        this.lv_open_params.setAdapter(new RetailInputParamAdapter(param.getRetailInputParams(), this));
        this.updateLog(" ");
    }
}

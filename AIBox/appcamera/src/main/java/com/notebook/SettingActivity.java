package com.notebook;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MainActivity;
import com.adapter.CameraSettingAdapter;
import com.bean.Tuple2;
import com.box.utils.LogUtil;
import com.idata.aibox.R;
import com.lib.sdk.bean.StringUtils;
import com.mgr.ConfigPropertiesManager;
import com.mgr.ImageCacheManager;
import com.mgr.serial.comn.util.GsonUtil;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_CAMREA_PATHS;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_MAX_CAMREA_SIZE_DEFAULT;

public class SettingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private TextView tv_clean_cache;
    private ListView lv_cameras_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        this.tv_clean_cache = findViewById(R.id.tv_clean_cache);
        this.lv_cameras_setting = findViewById(R.id.lv_cameras_setting);
        this.tv_clean_cache.setOnClickListener(this);
        this.initData();
    }

    private void initData() {
        this.initCamera();
    }

    private void initCamera() {
        USBMonitor usbMonitor = usbMonitor = new USBMonitor(this, null);
        List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
        List<UsbDevice> usbDevices = usbMonitor.getDeviceList(filter.get(0));
        Map<String, String> paths = getCameraSeting();
        List<Tuple2<Integer, Tuple2<List<UsbDevice>, Integer>>> usbs = new ArrayList<>();
        for (int i = 1; i <= SETTING_CONFIG_PROPERTY_MAX_CAMREA_SIZE_DEFAULT; i++) {
            String path = "";
            if (paths != null) {
                path = paths.get(i + "");
            }
            Integer selected = null;
            for (int j = 0; j < usbDevices.size(); j++) {
                UsbDevice ud = usbDevices.get(j);
                if (ud.getSerialNumber().equals(path)) {
                    selected = j;
                    break;
                }
            }
            usbs.add(new Tuple2<>(i, new Tuple2<>(usbDevices, selected)));
        }
        CameraSettingAdapter adapter = new CameraSettingAdapter(usbs, this);
        this.lv_cameras_setting.setAdapter(adapter);
        adapter.setListener((floor, usbDevice) -> {
            Map<String, String> cameraSeting = this.getCameraSeting();
            if (cameraSeting == null) {
                cameraSeting = new HashMap<>();
            }
            cameraSeting.put(floor + "", usbDevice.getSerialNumber());
            ConfigPropertiesManager.getInstance().setConfigProperty(SETTING_CONFIG_PROPERTY_CAMREA_PATHS,
                    GsonUtil.toJson(cameraSeting));
        });
    }

    private Map<String, String> getCameraSeting() {
        String s_paths = ConfigPropertiesManager.getInstance().getConfigProperty(SETTING_CONFIG_PROPERTY_CAMREA_PATHS);
        Map<String, String> paths = null;
        if (!StringUtils.isStringNULL(s_paths)) {
            paths = GsonUtil.fromJson(s_paths, Map.class);
        }
        return paths;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_clean_cache:
                this.cleanCache();
                break;
        }
    }

    private void cleanCache() {
        String base_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        List<String> cache_paths = new ArrayList<>();
        cache_paths.add(ImageCacheManager.getInstance().getBase_image_path());
        cache_paths.add(LogUtil.getBase_log_path());
        cache_paths.add(base_path + File.separator + "AiImages");
        cache_paths.add(base_path + File.separator + "AILogs");
        cache_paths.add(base_path + File.separator + "aa_retail");
        for (String path : cache_paths) {
            FileUtil.deleteFile(new File(path));
        }
        Toast.makeText(this, "清除缓存成功,正在重启", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(() -> reStartApp(), 1000);
    }

    public void reStartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "正在更新", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> reStartApp(), 1000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

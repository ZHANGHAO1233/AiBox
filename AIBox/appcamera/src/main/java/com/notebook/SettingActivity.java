package com.notebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.MainActivity;
import com.box.utils.LogUtil;
import com.idata.aibox.R;
import com.lib.sdk.bean.StringUtils;
import com.mgr.ConfigPropertiesManager;
import com.mgr.ImageCacheManager;
import com.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_HOST;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_HOST_DEFAULT_VALUE;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_PORT;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_PORT_DEFAULT_VALUE;

public class SettingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";
    private EditText et_host;
    private EditText et_host_port;
    private Button btn_host_reset;
    private Button btn_host_confirm;
    private TextView tv_clean_cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        this.et_host = (EditText) findViewById(R.id.et_host);
        this.et_host_port = (EditText) findViewById(R.id.et_host_port);
        this.btn_host_reset = (Button) findViewById(R.id.btn_host_reset);
        this.btn_host_confirm = (Button) findViewById(R.id.btn_host_confirm);
        this.tv_clean_cache = (TextView) findViewById(R.id.tv_clean_cache);
        this.btn_host_reset.setOnClickListener(this);
        this.btn_host_confirm.setOnClickListener(this);
        this.tv_clean_cache.setOnClickListener(this);
        this.initData();
    }

    private void initData() {
        String host = ConfigPropertiesManager.getInstance().getConfigProperty(SETTING_CONFIG_PROPERTY_HOST,
                SETTING_CONFIG_PROPERTY_HOST_DEFAULT_VALUE);
        String port = ConfigPropertiesManager.getInstance().getConfigProperty(SETTING_CONFIG_PROPERTY_PORT,
                SETTING_CONFIG_PROPERTY_PORT_DEFAULT_VALUE);
        this.et_host.setText(host);
        this.et_host_port.setText(port);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_host_confirm:
                this.confirmHost();
                break;
            case R.id.btn_host_reset:
                this.et_host.setText(SETTING_CONFIG_PROPERTY_HOST_DEFAULT_VALUE);
                this.et_host_port.setText(SETTING_CONFIG_PROPERTY_PORT_DEFAULT_VALUE);
                this.confirmHost();
                break;
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                reStartApp();
            }
        }, 1000);
    }

    public void reStartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void confirmHost() {
        String host = this.et_host.getText().toString();
        String port = this.et_host_port.getText().toString();
        if (!StringUtils.isStringNULL(host) && !StringUtils.isStringNULL(port)) {
            ConfigPropertiesManager.getInstance().setConfigProperty(SETTING_CONFIG_PROPERTY_HOST, host);
            ConfigPropertiesManager.getInstance().setConfigProperty(SETTING_CONFIG_PROPERTY_PORT, port);
            Toast.makeText(this, "设置成功", Toast.LENGTH_LONG).show();
        }
    }
}

package com.mgr;

import android.content.Context;
import android.os.Environment;

import com.box.utils.ILog;
import com.utils.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 * 配置文件管理类<br/>
 * <p>
 * 使用方法<br/>
 * <pre>
 *      如果需要保存一些系统配置，可在项目asset目录下存放config.properties文件；
 *      系统启动会将该文件复制到/sd/lenx/包名/conf下
 *      后续需要更改配置，则在conf目录下修改
 *      默认有HOST属性文件
 * </pre>
 * </p>
 *
 * @author zhangh
 * @version 1.0.1
 */
public class ConfigPropertiesManager {
    private static final String TAG = "ConfigPropertiesManager";
    private static final String SETTING_CONFIG_NAME = "config.txt";

    private static ConfigPropertiesManager configPropertiesManager;
    private Context context;
    private Properties properties;
    private ConfigPropertiesListener configPropertiesListener;

    private ConfigPropertiesManager() {
    }

    public void init(Context context) {
        this.context = context;
        this.initPropertiesFile();
        this.initProperties();
    }

    /**
     * 初始化配置文件
     */
    private void initPropertiesFile() {
        String base_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AiBox" + File.separator + "conf";
        String path = base_path + File.separator + SETTING_CONFIG_NAME;
        File dir = new File(base_path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!new File(path).exists()) {
            try {
                InputStream inputStream = context.getResources().getAssets().open(SETTING_CONFIG_NAME);
                FileOutputStream outputStream = new FileOutputStream(path);
                FileUtil.copy(inputStream, outputStream);
            } catch (IOException e) {
                ILog.d(TAG, "配置文件初始化失败");
            }
        }
    }

    /**
     * 初始化配置
     */
    private void initProperties() {
        try {
            String base_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AiBox" + File.separator + "conf";
            String path = base_path + File.separator + SETTING_CONFIG_NAME;
            this.properties = new Properties();
            InputStream in = new BufferedInputStream(new FileInputStream(path));
            this.properties.load(new InputStreamReader(in, "utf-8"));
        } catch (IOException e) {
            ILog.d(TAG, "配置文件读取失败");
        }
    }

    /**
     * 获取系统某个属性
     *
     * @param key
     * @return
     */
    public String getConfigProperty(String key) {
        if (this.properties != null) {
            return this.properties.getProperty(key, "");
        } else {
            ILog.d(TAG, "属性列表为空，请先调用init()");
            return "";
        }
    }

    /**
     * 获取系统某个属性
     *
     * @param key
     * @return
     */
    public String getConfigProperty(String key, String default_value) {
        if (this.properties != null) {
            return this.properties.getProperty(key, default_value);
        } else {
            ILog.d(TAG, "属性列表为空，请先调用init()");
            return "";
        }
    }

    public boolean setConfigProperty(String key, String value) {
        if (this.properties != null) {
            String base_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AiBox" + File.separator + "conf";
            ;
            String path = base_path + File.separator + SETTING_CONFIG_NAME;
            FileOutputStream oFile;
            try {
                oFile = new FileOutputStream(path, false);
                this.properties.setProperty(key, value);
                this.properties.store(new OutputStreamWriter(oFile, "utf-8"), "The New properties file");
                oFile.close();
                return true;
            } catch (Exception e) {
                ILog.d(TAG, "属性" + key + "保存失败");
                return false;
            }
        } else {
            ILog.d(TAG, "属性列表为空，请先调用init()");
            return false;
        }
    }

    public void setConfigPropertiesListener(ConfigPropertiesListener configPropertiesListener) {
        this.configPropertiesListener = configPropertiesListener;
    }

    public static ConfigPropertiesManager getInstance() {
        if (configPropertiesManager == null) {
            configPropertiesManager = new ConfigPropertiesManager();
        }
        return configPropertiesManager;
    }

    interface ConfigPropertiesListener {
        void onConfigPropertitySaved(String propertity);
    }
}

package com.camera;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.baidu.retail.ImageSaver;
import com.baidu.retail.RetailVisManager;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.lib.funsdk.support.FunPath;
import com.lib.funsdk.support.FunSupport;
import com.lib.funsdk.support.config.OPPTZPreset;
import com.lib.funsdk.support.config.SystemInfo;
import com.lib.funsdk.support.models.FunDevType;
import com.lib.funsdk.support.models.FunDevice;
import com.lib.funsdk.support.utils.FileUtils;
import com.notebook.BdCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Curry on 2018/9/29.
 */

public class CameraHelper {//解耦UI
    //    货柜里面摄像头地址是192.168.1.10,新的两个分别是192.168.1.11,192.168.1.12
//    public static String ips[] = new String[]{"192.168.1.10","192.168.1.11", "192.168.1.12","192.168.1.13"};
    public static String ips[] = new String[]{"192.168.1.10", "192.168.1.11", "192.168.1.12", "192.168.1.13",};
    //    public static String ips[] = new String[]{ "192.168.1.11"};
    public List<FunDevice> devices = new ArrayList<>();
    public Map<FunDevice, Integer> deviceMap = new HashMap<>();
    public CameraActivity activity;
    public final long reloginDelay = 2000;
    public final FunDevType[] mSupportDevTypes = {FunDevType.EE_DEV_NORMAL_MONITOR,
            FunDevType.EE_DEV_INTELLIGENTSOCKET, FunDevType.EE_DEV_SMALLEYE};

    public CameraHelper(CameraActivity activity) {
        this.activity = activity;
    }

    public void initBdConfig() {
        RetailVisManager.load(activity, new BdCallback(null));
        RetailVisManager.setAppid(OsModule.get().getSn());
        RetailVisManager.setSK("K1GhMzLG6YAWY3oDSWCjTWIiKo7SjDSP");
//        RetailVisManager.setAK("W7SFpfC5o7tS3d4CQugZ8YEglb9QVEzN");
        RetailVisManager.setBoxid(OsModule.get().getSn());
    }

    public void startConnect() {
        new ConnectThread().start();
    }

    public void loginDevice(int i) {
        FunSupport.getInstance().requestDeviceLogin(devices.get(i));
    }

    public void requestSystemInfo(int i) {
        ILog.d("requestSystemInfo:i:" + i);
        FunSupport.getInstance().requestDeviceConfig(devices.get(i), SystemInfo.CONFIG_NAME);
    }

    // 获取设备预置点列表
    public void requestPTZPreset(int i) {
        FunSupport.getInstance().requestDeviceConfig(devices.get(i), OPPTZPreset.CONFIG_NAME, 0);
    }

    private class ConnectThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < devices.size(); i++) {
                try {
                    FunDevice device = devices.get(i);
                    if (!device.hasLogin() || !device.hasConnected()) {
                        loginDevice(i);
                    } else {
                        requestSystemInfo(i);
                    }
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 设备登录
    public void initDevice() {
        for (int i = 0; i < ips.length; i++) {
            String devIP = ips[i];
            String devport = "34567";
            if (devIP.length() == 0) {
                devIP = "" + i;
            }
            FunDevType devType = null;
            String devMac = null;
            String dev = null;
            dev = devIP + ":" + devport;
            FunDevice mFunDevice = FunSupport.getInstance().buildTempDeivce(devType, devMac);
            mFunDevice.devType = mSupportDevTypes[0];
            mFunDevice.devIp = devIP;
            mFunDevice.tcpPort = Integer.parseInt(devport);
            mFunDevice.devSn = dev;
            // 用户名默认是:admin
            mFunDevice.loginName = "admin";
            mFunDevice.loginPsw = "";
            devices.add(mFunDevice);
            deviceMap.put(mFunDevice, i);
        }
//        mFunDevice = devices.get(0);
    }

    public Bitmap path2Bitmap(String path) {
        Bitmap bitmap = null;
        try {
            ImageSaver.getImageFromSDCardPath(path);
            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
            ILog.d("path 2 bitmap exception:" + e.getMessage());
        }
        return bitmap;
    }

    public String getType(int i) {
        switch (i) {
            case 0:
                return "P2P";
            case 1:
                return "Forward";
            case 2:
                return "IP";
            case 5:
                return "RPS";
            default:
                return "";
        }
    }

    public int findDeviceNum(FunDevice device) {
        return deviceMap.get(device);
    }

    public void saveImages(List<String> paths) {
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            if (!TextUtils.isEmpty(path)) {
                saveImage(path);
            }
        }
    }

    public void saveImage(String path) {
        try {
            File file = new File(path);
            File imgPath = new File(FunPath.PATH_PHOTO + File.separator
                    + file.getName());
            if (imgPath.exists()) {
//                showToast(R.string.device_socket_capture_exist);
            } else {
                FileUtils.copyFile(path, FunPath.PATH_PHOTO + File.separator
                        + file.getName());
//                showToast(R.string.device_socket_capture_save_success);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ILog.d("save image excetion:" + e.getMessage());
        }
    }
}

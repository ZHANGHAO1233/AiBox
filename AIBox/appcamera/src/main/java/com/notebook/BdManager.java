package com.notebook;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.baidu.retail.Classifier;
import com.baidu.retail.ImageSaver;
import com.baidu.retail.RetailInputParam;
import com.baidu.retail.RetailVisManager;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.box.utils.TimeUtil;
import com.example.funsdkdemo.MyApplication;
import com.mgr.serial.comn.Device;
import com.mgr.serial.comn.SerialPortManager;
import com.mgr.serial.comn.util.GsonUtil;
import com.thefunc.serialportlibrary.SerialPortFinder;
import com.utils.DownloadUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Curry on 2018/9/29.
 */

public class BdManager implements OsModule.OnDoorStatusListener, DownloadUtil.OnDownloadListener {
    private static final String TAG = "BdManager";
    private static BdManager bd;
    private final String ipAndPort = "192.168.1.186:8080";
    private final String http = "http://";
    private String urls[] = new String[]{http + ipAndPort + "/cap_0.jpg", http + ipAndPort + "/cap_1.jpg", http + ipAndPort + "/cap_2.jpg", http + ipAndPort + "/cap_3.jpg"};
    //    private String urls[] = new String[]{"https://www.baidu.com/img/bd_logo1.png", "https://www.baidu.com/img/bd_logo1.png", "https://www.baidu.com/img/bd_logo1.png", "https://www.baidu.com/img/bd_logo1.png"};
    //    public String urls[] = new String[]{"http://192.168.1.185:8080/cap_0.jpg", "http://192.168.1.185:8080/cap_1.jpg", "http://192.168.1.185:8080/cap_2.jpg", "http://192.168.1.185:8080/cap_3.jpg"};
    private String downloadParentDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AiImages";
    private String currentImageDir = "";
    //    private String downloadParentDir = MyApplication.getContext().getCacheDir().getAbsolutePath();
    private List<String> paths = new ArrayList<>();
    private volatile int finishNum = urls.length;
    private boolean hasFinish = true;
    private Handler handler;
    private volatile String currentOrder = "";//规则，设备sn+时间戳
    public static volatile int transactionStatus = 2;//0:opendoor,1:closedoor,2:result;
    private final String linkChar = "-";
    private Map<String, SerialPortManager> serials;
    private Map<String, List<String>> commandMap;
    private static final String BAUDRATE_DEFAULT_VALUE = "115200";

    private BdManager() {
        OsModule.get().addDoorListener(this);
        initBdConfig();
        createPath(downloadParentDir);
        initSerialCommands();
        openSerialPorts();
    }

    private void initSerialCommands() {
        String command1 = "1 G \n";
//        String command10 = "10 G \n";
        this.commandMap = new HashMap<>();
        List<String> commands = new ArrayList<>();
        this.commandMap.put(command1, commands);
        for (int i = 1; i <= 12; i++) {
//            if (i == 10) {
//                commands = new ArrayList<>();
//                this.commandMap.put(command10, commands);
//            }
            String command = i + " G \n";
            commands.add(command);
        }
        ILog.d(TAG, "初始化指令集" + GsonUtil.toJson(commandMap));
    }

    /**
     * 打开或关闭串口
     */
    private void openSerialPorts() {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        // 设备
        String[] paths = serialPortFinder.getAllDevicesPath();
        this.serials = new HashMap<>();
        k:
        for (String path : paths) {
            if (path.toUpperCase().contains("USB")) {
                Device device = new Device(path, BAUDRATE_DEFAULT_VALUE);
                SerialPortManager manager = new SerialPortManager();
                boolean opend = manager.open(device) != null;
                ILog.d(TAG, device.toString() + (opend ? ",打开成功" : ",打开失败"));
                if (opend) {
                    for (String key : this.commandMap.keySet()) {
                        String command = this.commandMap.get(key).get(0);
                        Map<String, Double> data = manager.sendCommand(Collections.singletonList(command));
                        if (data.get(command) != null) {
                            ILog.d(TAG, "匹配到" + command + "对应串口:" + manager.toString());
                            this.serials.put(key, manager);
                            continue k;
                        }
                    }
                }
            }
        }
        ILog.d(TAG, "初始化" + this.serials.size() + "个串口");
    }


//
//    /**
//     * 获取第一个串口设备
//     *
//     * @return
//     */
//    private Device getDevice1() {
//        String path = ConfigPropertiesManager.getInstance().getConfigProperty(ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_STEELYARD_PATH_1,
//                ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_STEELYARD_PATH_1_DEFAULT_VALUE);
//        String baudrate = ConfigPropertiesManager.getInstance().getConfigProperty(ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_STEELYARD_BAUDRATE_1,
//                ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_STEELYARD_BAUDRATE_1_DEFAULT_VALUE);
//        Device device = new Device(path, baudrate);
//        ILog.d(TAG, "获取第1个串口设备" + device.toString());
//        return device;
//    }
//
//    /**
//     * 获取第0个串口设备
//     *
//     * @return
//     */
//    private Device getDevice() {
//        String path = ConfigPropertiesManager.getInstance().getConfigProperty(ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_STEELYARD_PATH,
//                ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_STEELYARD_PATH_DEFAULT_VALUE);
//        String baudrate = ConfigPropertiesManager.getInstance().getConfigProperty(ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_STEELYARD_BAUDRATE,
//                ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_STEELYARD_BAUDRATE_DEFAULT_VALUE);
//        Device device = new Device(path, baudrate);
//        ILog.d(TAG, "获取第0个串口设备" + device.toString());
//        return device;
//    }


    private void createPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            File f = new File(path);
            if (f.exists()) {
                f.mkdirs();
            }
        }
    }

    public static BdManager getBd() {
        if (bd == null) {
            synchronized (BdManager.class) {
                if (bd == null) {
                    bd = new BdManager();
                }
            }
        }
        return bd;
    }

    public void init(Handler handler) {
        setHandler(handler);
        initBdConfig();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onDoorClose() {
        downloadImages(false);
    }

    @Override
    public void onDoorOpen() {
        downloadImages(true);
    }

    public void initBdConfig() {
        RetailVisManager.load((Activity) null, new BdCallback());
        RetailVisManager.setAppid(OsModule.get().getSn());
        RetailVisManager.setSK("K1GhMzLG6YAWY3oDSWCjTWIiKo7SjDSP");
        RetailVisManager.setAK("W7SFpfC5o7tS3d4CQugZ8YEglb9QVEzN");
        RetailVisManager.setBoxid(OsModule.get().getSn());
    }

    public Bitmap path2Bitmap(String path) {
        Bitmap bitmap = null;
        try {
            ImageSaver.getImageFromSDCardPath(path);
            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            bitmap = MediaStore.Images.Media.getBitmap(MyApplication.getContext().getContentResolver(), uri);
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

    public synchronized void downloadImages(boolean isOpenDoor) {
        if (hasFinish) {
            paths.clear();
            finishNum = 0;
            hasFinish = false;
            String currentTime = TimeUtil.getTimeStr();
            String currentDirTime = TimeUtil.getTimeStr2();
            if (isOpenDoor) {
                currentImageDir = downloadParentDir + File.separator + currentDirTime;
                createPath(currentImageDir);
            }
            String imageName = isOpenDoor ? linkChar + "open" + linkChar + currentTime + ".jpg" : linkChar + "close" + linkChar + currentTime + ".jpg";
            for (int i = 0; i < urls.length; i++) {
                DownloadUtil.get().download(urls[i], currentImageDir, i + imageName, this, isOpenDoor);
            }
        }
    }

    @Override
    public void onDownloadSuccess(File file, boolean isOpenDoor) {
        ILog.d("download succuss,file:" + file);
        if (file != null) {
            paths.add(file.getAbsolutePath());
            if (handler != null) {
                Message m = new Message();
                m.what = 0;
                m.obj = file;
                handler.sendMessage(m);
            }
        }
        checkFinish(isOpenDoor);
    }

    @Override
    public void onDownloading(int progress, boolean isOpenDoor) {
//        ILog.d("downloading,progress:" + progress);
    }

    @Override
    public void onDownloadFailed(Exception e, boolean isOpenDoor) {
        ILog.d("download fail:e:" + e.getMessage());
        checkFinish(isOpenDoor);
    }

    private synchronized void checkFinish(boolean isOpenDoor) {
        finishNum++;
        if (finishNum >= urls.length) {
            hasFinish = true;
            startRecognize(isOpenDoor);
        }
        ILog.d("finish num:" + finishNum + ":has finish:" + hasFinish);
    }

    public void testOpen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                downloadImages(true);
            }
        }).start();
    }

    public void testClose() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                downloadImages(false);
            }
        }).start();
    }

    private void startRecognize(final boolean isOpenDoor) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (serials) {
                    ILog.d(TAG, "逻辑加锁开始");
                    Looper.prepare();
                    ILog.d("open:" + isOpenDoor + ":ready to recognize:");
                    List<BdParam> bdParams = new ArrayList<>();
                    Map<String, Double> serialResults = getSerialResults();
                    for (int i = 0; i < paths.size(); i++) {
                        //在这里把图片和摄像头编号对应
                        int cameraNum = -1;
                        Bitmap bitmap = null;
                        String path = paths.get(i);
                        if (!TextUtils.isEmpty(path)) {
                            bitmap = path2Bitmap(path);
                            if (bitmap != null) {
                                cameraNum = getCameraNum(path);
                            }
                        }
                        if (cameraNum != -1) {
                            List<Double> weights = getWeight(serialResults, cameraNum);
                            BdParam param = new BdParam(bitmap, cameraNum, weights);
                            StringBuilder builder = new StringBuilder();
                            builder.append("cameraNum:" + cameraNum + ";\nbitmap:" + bitmap + ";\npath:" + path + ";\nweights:");
                            builder.append("[");
                            for (double weight : weights) {
                                builder.append(weight + ",");
                            }
                            builder.append("]");
                            ILog.d(TAG, builder.toString());
                            bdParams.add(param);
                        }
                    }
                    //参数获取完整后，才开门
                    if (isOpenDoor) {
                        OsModule.get().unlock();
                    }
                    ILog.d("BdParam size:" + bdParams.size() + ":ready to recognize:");
                    if (bdParams.size() > 0) {
                        //进行一个排序
                        Collections.sort(bdParams);
                        final List<RetailInputParam> params = new ArrayList<>();
                        for (int i = 0; i < bdParams.size(); i++) {
                            BdParam bdParam = bdParams.get(i);
                            RetailInputParam retailInputParam = new RetailInputParam(bdParam.getBitmap(), bdParam.getFloor() + "", bdParam.getWeights());
                            ILog.d("generate bd params,current floor:" + bdParam.getFloor());
                            params.add(retailInputParam);
                        }
                        if (isOpenDoor) {
                            if (transactionStatus >= 2) {
                                try {
                                    currentOrder = OsModule.get().getSn() + "-" + System.currentTimeMillis();
                                    ILog.d("start to recognize open picture");
                                    transactionStatus = RetailVisManager.openDoor(currentOrder, params) ? 0 : transactionStatus;
                                    List<Classifier.Recognition> recognitions = RetailVisManager.getOpenClassify(0);
                                    ILog.d("recognitions open:" + recognitions);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ILog.d("recognize open picture excetion:" + e.getMessage());
                                }
                                ILog.d("open picture recognize end!");
                            } else {
                                ILog.d("start recognize open picture stop,transactionStatus:" + transactionStatus);
                            }
                        } else {
                            if (transactionStatus == 0) {
                                try {
                                    ILog.d("start to recognize close picture");
                                    transactionStatus = RetailVisManager.closeDoor(currentOrder, params) ? 1 : transactionStatus;
                                    List<Classifier.Recognition> recognitions = RetailVisManager.getOpenClassify(0);
                                    ILog.d("recognitions close:" + recognitions);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ILog.d("recognize close picture excetion:" + e.getMessage());
                                }
                                ILog.d("close picture recognize end!");
                            } else {
                                ILog.d("start recognize close picture stop,transactionStatus:" + transactionStatus);
                            }
                        }
                    } else {
                        ILog.d("params is null,stop recognize picture!");
                    }
                }
                ILog.d(TAG, "逻辑加锁结束");
            }
        }).start();
    }


    private List<Float> getTestWeight(boolean isOpenDoor, int floor) {
        List<Float> weights = new ArrayList<>();
        if (isOpenDoor) {
            weights.add(1000.0f);
            weights.add(2000.0f);
            weights.add(3000.0f);
        } else {
            weights.add(1000.0f);
            weights.add(2000.0f);
            weights.add(2500.0f);
        }
        return weights;
    }

    /**
     * 获取商品重量
     */
    private List<Double> getWeight(Map<String, Double> serialResultMap, int camera_num) {
        List<Double> weights = new ArrayList<>();
        //单个摄像头对应称的数量
        int camera_weights_size = 3;
        for (int j = camera_num * camera_weights_size + 1; j <= (camera_num + 1) * camera_weights_size; j++) {
            String command = j + " G \n";
            Double weight = serialResultMap.get(command);
            if (weight != null) {
                weights.add(weight);
            } else {
                weights.add((double) Integer.MAX_VALUE);
            }
        }
        return weights;
    }

    private Map<String, Double> getSerialResults() {
        Map<String, Double> resultMap = new HashMap<>();
        for (String key : this.commandMap.keySet()) {
            List<String> commands = this.commandMap.get(key);
            SerialPortManager manager = this.serials.get(key);
            if (manager != null) {
                resultMap.putAll(manager.sendCommand(commands));
            }
        }
        return resultMap;
    }

    private int getCameraNum(String path) {
        int num = 0;
        if (!TextUtils.isEmpty(path)) {
            int lastFileSeperatorIndex = path.lastIndexOf(File.separator) + 1;
            int index = path.indexOf(linkChar);
            if (index > -1 && lastFileSeperatorIndex > -1) {
                try {
                    ILog.d(TAG, "string num:" + path.substring(lastFileSeperatorIndex, index));
                    num = Integer.valueOf(path.substring(lastFileSeperatorIndex, index));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        ILog.d("camera num:" + num);
        return num;
    }

    public void closeAllSerials() {
        for (String key : this.serials.keySet()) {
            SerialPortManager manager = this.serials.get(key);
            manager.close();
        }
    }
}

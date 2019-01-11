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

import com.baidu.retail.ImageSaver;
import com.baidu.retail.RetailInputParam;
import com.baidu.retail.RetailVisManager;
import com.bean.CloseParam;
import com.bean.OpenParam;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.box.utils.ILog.TIME_TAG;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_PARMA;

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
    private boolean hasDownLoadFinish = true;
    private Handler handler;
    private volatile String currentOrder = "";//规则，设备sn+时间戳
    public static int TRANSACTION_STATUS_INITED = 0;//订单初始化
    public static int TRANSACTION_STATUS_OPENDOOR = 1;//订单开门
    public static int TRANSACTION_STATUS_CLOSEDOOR = 2;//订单关门
    public static int TRANSACTION_STATUS_RESULT = 3;//无订单状态
    public static volatile int transactionStatus = TRANSACTION_STATUS_RESULT;
    public Map<String, Double> serialResults;

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
        ILog.d(TIME_TAG, new Date().getTime() + ",开始收集关门数据");
        downloadImages(false);
        this.serialResults = null;
        this.serialResults = getSerialResults();
    }

    @Override
    public void onDoorOpen() {
        ILog.d(TIME_TAG, new Date().getTime() + ",开始收集开门数据");
        downloadImages(true);
        this.serialResults = null;
        this.serialResults = getSerialResults();
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
        ILog.d(TIME_TAG, new Date().getTime() + ",开始下载图片");
        if (hasDownLoadFinish) {
            paths.clear();
            finishNum = 0;
            hasDownLoadFinish = false;
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
        ILog.d(TIME_TAG, new Date().getTime() + ",图片下载成功,file:" + file);
        if (file != null) {
            paths.add(file.getAbsolutePath());
        }
        checkDownloadFinish(isOpenDoor);
    }

    @Override
    public void onDownloading(int progress, boolean isOpenDoor) {
//        ILog.d("downloading,progress:" + progress);
    }

    @Override
    public void onDownloadFailed(Exception e, boolean isOpenDoor) {
        ILog.d("download fail:e:" + e.getMessage());
        checkDownloadFinish(isOpenDoor);
    }

    private synchronized void checkDownloadFinish(boolean isOpenDoor) {
        finishNum++;
        if (finishNum >= urls.length) {
            ILog.d(TIME_TAG, new Date().getTime() + ",图片下载完成");
            hasDownLoadFinish = true;
            startRecognize(isOpenDoor);
        }
        ILog.d("finish num:" + finishNum + ":has finish:" + hasDownLoadFinish);
    }

    public void testOpen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                if (BdManager.transactionStatus == TRANSACTION_STATUS_RESULT) {
                    BdManager.transactionStatus = TRANSACTION_STATUS_INITED;
                    downloadImages(true);
                    serialResults = null;
                    serialResults = getSerialResults();
                }
            }
        }).start();
    }

    public void testClose() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                downloadImages(false);
                serialResults = null;
                serialResults = getSerialResults();
            }
        }).start();
    }

    private void startRecognize(final boolean isOpenDoor) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (serials) {
                    while (serialResults == null) {
                    }
                    Looper.prepare();
                    ILog.d(TIME_TAG, new Date().getTime() + ",开始整理" + (isOpenDoor ? "开门" : "关门") + "参数");
                    List<BdParam> bdParams = new ArrayList<>();
                    for (int i = 0; i < paths.size(); i++) {
                        //在这里把图片和摄像头编号对应
                        int cameraNum = -1;
                        Bitmap bitmap = null;
                        String path = paths.get(i);
                        if (!TextUtils.isEmpty(path)) {
                            cameraNum = getCameraNum(path);
                            bitmap = path2Bitmap(path);
                        }
                        if (cameraNum != -1) {
                            List<Double> weights = getWeight(serialResults, cameraNum);
                            BdParam param = new BdParam(bitmap, cameraNum, weights);
                            StringBuilder builder = new StringBuilder();
                            builder.append("cameraNum:" + cameraNum + ";\n");
                            if (bitmap != null) {
                                builder.append("bitmap: width:" + bitmap.getWidth() + "height:" + bitmap.getHeight() + ";\n");
                            }
                            builder.append("path:" + path + ";\n");
                            builder.append("weights:");
                            builder.append("[");
                            for (double weight : weights) {
                                builder.append(weight + ",");
                            }
                            builder.append("]");
                            ILog.d(TAG, builder.toString());
                            bdParams.add(param);
                        }
                    }
                    ILog.d("BdParam size:" + bdParams.size() + ":ready to recognize:");
                    if (bdParams.size() > 0) {
                        //进行一个排序
                        Collections.sort(bdParams);
                        final List<RetailInputParam> params = new ArrayList<>();
                        for (int i = 0; i < bdParams.size(); i++) {
                            BdParam bdParam = bdParams.get(i);
                            RetailInputParam retailInputParam = new RetailInputParam(bdParam.getBitmap(),
                                    bdParam.getFloor() + "", bdParam.getWeights());
                            params.add(retailInputParam);
                        }
                        if (isOpenDoor) {
                            //当前状态为已初始化状态
                            if (transactionStatus == TRANSACTION_STATUS_INITED) {
                                //参数获取完整后，才开门
                                OsModule.get().unlock();
                                try {
                                    currentOrder = OsModule.get().getSn() + "-" + System.currentTimeMillis();
                                    ILog.d(TIME_TAG, new Date().getTime() + ",开始提交开门数据");
                                    boolean succ = RetailVisManager.openDoor(currentOrder, params);
                                    transactionStatus = succ ? TRANSACTION_STATUS_OPENDOOR : TRANSACTION_STATUS_RESULT;
                                    ILog.d(TIME_TAG, new Date().getTime() + "提交开门数据" + (succ ? "成功" : "失败") + "\n，transactionStatus 更新为 " + transactionStatus);
                                    if (handler != null) {
                                        Message m = new Message();
                                        m.obj = new OpenParam(currentOrder, params);
                                        m.what = HANDLER_MESSAGE_WHAT_PARMA;
                                        handler.sendMessage(m);
                                    }
                                } catch (Exception e) {
                                    //参数提交异常，恢复为结束状态，需要用户重新扫码开门
                                    transactionStatus = TRANSACTION_STATUS_RESULT;
                                    ILog.d("提交开门数据异常:" + e.getMessage());
                                }
                                ILog.d("提交开门数据结束!");
                            } else {
                                //当前状态异常，恢复为结束状态
                                ILog.d("错误的订单状态,transactionStatus:" + transactionStatus);
                                transactionStatus = TRANSACTION_STATUS_RESULT;
                            }
                        } else {
                            //当前状态为已开门状态
                            if (transactionStatus == TRANSACTION_STATUS_OPENDOOR) {
                                try {
                                    ILog.d(TIME_TAG, new Date().getTime() + "，开始提交关门数据");
                                    boolean succ = RetailVisManager.closeDoor(currentOrder, params);
                                    transactionStatus = succ ? TRANSACTION_STATUS_CLOSEDOOR : TRANSACTION_STATUS_RESULT;
                                    ILog.d(TIME_TAG, new Date().getTime() + "，提交关门数据" + (succ ? "成功" : "失败") + "\ntransactionStatus 更新为 " + transactionStatus);
                                    if (handler != null) {
                                        Message m = new Message();
                                        m.obj = new CloseParam(params);
                                        m.what = HANDLER_MESSAGE_WHAT_PARMA;
                                        handler.sendMessage(m);
                                    }
                                } catch (Exception e) {
                                    //参数提交异常，恢复为结束状态，数据未提交。
                                    transactionStatus = TRANSACTION_STATUS_RESULT;
                                    ILog.d("开始提交关门数据异常:" + e.getMessage());
                                }
                                ILog.d("提交关门数据结束!");
                            } else {
                                ILog.d("错误的订单状态,transactionStatus:" + transactionStatus);
                                transactionStatus = TRANSACTION_STATUS_RESULT;
                            }
                        }
                    } else {
                        ILog.d("无有效参数，订单结束");
                        transactionStatus = TRANSACTION_STATUS_RESULT;
                    }
                }
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
        ILog.d(TIME_TAG, new Date().getTime() + ",开始获取摄像头" + camera_num + "的重量信息");
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
        ILog.d(TIME_TAG, new Date().getTime() + ",摄像头" + camera_num + "获取重量信息完成");
        return weights;
    }

    private Map<String, Double> getSerialResults() {
        ILog.d(TIME_TAG, new Date().getTime() + ",发送获取重量指令集");
        Map<String, Double> resultMap = new HashMap<>();
        for (String key : this.commandMap.keySet()) {
            List<String> commands = this.commandMap.get(key);
            SerialPortManager manager = this.serials.get(key);
            if (manager != null) {
                resultMap.putAll(manager.sendCommand(commands));
            }
        }
        ILog.d(TIME_TAG, new Date().getTime() + ",获取重量集完成" + GsonUtil.toJson(resultMap));
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

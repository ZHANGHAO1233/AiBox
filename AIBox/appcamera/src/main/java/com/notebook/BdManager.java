package com.notebook;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.retail.RetailInputParam;
import com.baidu.retail.RetailVisManager;
import com.bean.CloseParam;
import com.bean.OpenParam;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.box.utils.TimeUtil;
import com.consts.TimeConsts;
import com.lib.sdk.bean.StringUtils;
import com.mgr.ConfigPropertiesManager;
import com.mgr.ImageCacheManager;
import com.mgr.serial.comn.Device;
import com.mgr.serial.comn.SerialPortManager;
import com.mgr.serial.comn.util.GsonUtil;
import com.thefunc.serialportlibrary.SerialPortFinder;
import com.utils.DownloadUtil;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.box.utils.ILog.TIME_TAG;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_HOST;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_HOST_DEFAULT_VALUE;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_PORT;
import static com.consts.ConfigPropertiesConsts.SETTING_CONFIG_PROPERTY_PORT_DEFAULT_VALUE;
import static com.consts.HandleConsts.HANDLER_MESSAGE_WHAT_PARMA;
import static com.consts.TimeConsts.CLOSE_DISPOSAL_DATA_END_TIME;
import static com.consts.TimeConsts.CLOSE_DISPOSAL_DATA_START_TIME;
import static com.consts.TimeConsts.OPEN_DISPOSAL_DATA_END_TIME;
import static com.consts.TimeConsts.OPEN_DISPOSAL_DATA_START_TIME;

/**
 * Created by Curry on 2018/9/29.
 */

public class BdManager implements OsModule.OnDoorStatusListener, DownloadUtil.OnDownloadListener {
    private static final String TAG = "BdManager";
    private static BdManager bd;
    private String urls[];
    private Map<String, String> paths = new HashMap<>();
    private volatile int finishNum;
    private boolean hasDownLoadFinish = true;
    private Handler messHandler;
    private static final int TRANSACTION_STATUS_INITED = 0;//订单初始化
    public static int TRANSACTION_STATUS_OPENDOOR = 1;//订单开门
    public static int TRANSACTION_STATUS_CLOSEDOOR = 2;//订单关门
    //    public static int TRANSACTION_STATUS_RESULT = 3;//无订单状态
    public int transactionStatus = TRANSACTION_STATUS_CLOSEDOOR;
    private volatile String currentOrder = "";//规则，设备sn+时间戳
    public Map<String, Double> serialResults;

    private final String linkChar = "-";
    private Map<String, SerialPortManager> serials;
    private Map<String, List<String>> commandMap;
    private static final String BAUDRATE_DEFAULT_VALUE = "115200";
    private String currentImageDir;

    private BdManager() {
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
//            if (path.toUpperCase().contains("USB")) {
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
                    } else {
                        manager.close();
                    }
                }
//                }
            }
        }
        ILog.d(TAG, "初始化" + this.serials.size() + "个串口");
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
        this.messHandler = handler;
        OsModule.get().addDoorListener(this);
        initBdConfig();
        initSerialCommands();
        openSerialPorts();
    }

    private void initHost() {
        String host = ConfigPropertiesManager.getInstance().getConfigProperty(SETTING_CONFIG_PROPERTY_HOST,
                SETTING_CONFIG_PROPERTY_HOST_DEFAULT_VALUE);
        String port = ConfigPropertiesManager.getInstance().getConfigProperty(SETTING_CONFIG_PROPERTY_PORT,
                SETTING_CONFIG_PROPERTY_PORT_DEFAULT_VALUE);
        String path = host + ":" + port;
        ILog.d(TAG, "获取到path：" + path);
        this.urls = new String[]{path + "/cap_0.jpg", path + "/cap_1.jpg", path + "/cap_2.jpg", path + "/cap_3.jpg"};
//        this.urls = new String[]{"https://www.baidu.com/img/bd_logo1.png", "https://www.baidu.com/img/bd_logo1.png", "https://www.baidu.com/img/bd_logo1.png", "https://www.baidu.com/img/bd_logo1.png"};
        this.finishNum = this.urls.length;
    }

    private void createPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
        }
    }


    @Override
    public void onDoorClose() {
        long now = new Date().getTime();
        TimeConsts.CLOSE_COLLECTION_START_TIME = now;
        ILog.d(TIME_TAG, now + ",开始收集关门数据");
        downloadImages(false);
        this.serialResults = null;
        this.serialResults = getSerialResults(false);
    }

    @Override
    public void onDoorOpen() {
        long now = new Date().getTime();
        TimeConsts.OPEN_COLLECTION_START_TIME = now;
        ILog.d(TIME_TAG, now + ",开始收集开门数据");
        downloadImages(true);
        this.serialResults = null;
        this.serialResults = getSerialResults(true);
    }

    public void initBdConfig() {
        RetailVisManager.load((Activity) null, new BdCallback(this.messHandler));
        RetailVisManager.setAppid(OsModule.get().getSn());
        RetailVisManager.setSK("K1GhMzLG6YAWY3oDSWCjTWIiKo7SjDSP");
        RetailVisManager.setAK("W7SFpfC5o7tS3d4CQugZ8YEglb9QVEzN");
        RetailVisManager.setBoxid(OsModule.get().getSn());
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
        this.initHost();
        long now = new Date().getTime();
        if (isOpenDoor) {
            TimeConsts.OPEN_DOWNLOAD_IMAGES_START_TIME = now;
        } else {
            TimeConsts.CLOSE_DOWNLOAD_IMAGES_START_TIME = now;
        }
        ILog.d(TIME_TAG, now + ",开始下载图片");
        if (hasDownLoadFinish) {
            paths.clear();
            finishNum = 0;
            hasDownLoadFinish = false;
            String currentTime = TimeUtil.getTimeStr();
            String currentDirTime = TimeUtil.getTimeStr2();
            if (isOpenDoor) {
                currentImageDir = ImageCacheManager.getInstance().getBase_image_path() + File.separator + currentDirTime;
                createPath(currentImageDir);
            }
            String imageName = isOpenDoor ? linkChar + "open" + linkChar + currentTime + ".jpg" : linkChar + "close" + linkChar + currentTime + ".jpg";
            for (int i = 0; i < urls.length; i++) {
                DownloadUtil.get().download(urls[i], i + "", currentImageDir + File.separator + i + imageName, this, isOpenDoor);
            }
        }
    }

    @Override
    public void onDownloadSuccess(String camera, String path, boolean isOpenDoor) {
        ILog.d(TIME_TAG, new Date().getTime() + ",图片下载成功,camera:" + camera);
        if (!StringUtils.isStringNULL(path)) {
            paths.put(camera, path);
        }
        checkDownloadFinish(isOpenDoor);
    }

    @Override
    public void onDownloadFailed(Exception e, boolean isOpenDoor) {
        ILog.d("download fail:e:" + e.getMessage());
        checkDownloadFinish(isOpenDoor);
    }

    private synchronized void checkDownloadFinish(boolean isOpenDoor) {
        finishNum++;
        if (finishNum >= urls.length) {
            long now = new Date().getTime();
            if (isOpenDoor) {
                TimeConsts.OPEN_DOWNLOAD_IMAGES_END_TIME = now;
            } else {
                TimeConsts.CLOSE_DOWNLOAD_IMAGES_END_TIME = now;
            }
            ILog.d(TIME_TAG, now + ",图片下载完成");
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
                if (setTransactionStatusInited(0)) {
                    TimeConsts.OPEN_COLLECTION_START_TIME = new Date().getTime();
                    downloadImages(true);
                    serialResults = null;
                    serialResults = getSerialResults(true);
                }
            }
        }).start();
    }

    public void testClose() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                TimeConsts.CLOSE_COLLECTION_START_TIME = new Date().getTime();
                downloadImages(false);
                serialResults = null;
                serialResults = getSerialResults(false);
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
                    long now = new Date().getTime();
                    ILog.d(TIME_TAG, now + ",开始整理" + (isOpenDoor ? "开门" : "关门") + "参数");
//                    testImage(isOpenDoor);
//                    testWeight(isOpenDoor);
                    if (isOpenDoor) {
                        TimeConsts.OPEN_COLLECTION_END_TIME = now;
                        TimeConsts.OPEN_DISPOSAL_DATA_START_TIME = now;
                    } else {
                        TimeConsts.CLOSE_COLLECTION_END_TIME = now;
                        TimeConsts.CLOSE_DISPOSAL_DATA_START_TIME = now;
                    }
                    List<RetailInputParam> params = new ArrayList<>();
                    for (String camera : paths.keySet()) {
                        String path = paths.get(camera);
                        List<Double> weights = getWeight(serialResults, Integer.parseInt(camera));
                        RetailInputParam retailInputParam = new RetailInputParam(path, camera, weights);
                        params.add(retailInputParam);
                    }
                    if (params.size() > 0) {
                        Collections.sort(params, new Comparator<RetailInputParam>() {
                            @Override
                            public int compare(RetailInputParam o1, RetailInputParam o2) {
                                return Integer.parseInt(o1.getMcastId()) - Integer.parseInt(o2.getMcastId());
                            }
                        });
                        if (isOpenDoor) {
                            boolean succ = setTransactionStatusOpendoor(params);
                            if (!succ) {
                                setTransactionStatusError("开门数据提交失败");
                            }
                        } else {
                            boolean succ = setTransactionStatusClosedoor(params);
                            if (!succ) {
                                setTransactionStatusError("关门数据提交失败");
                            }
                        }
                    } else {
                        ILog.d("无有效参数，订单结束");
                        setTransactionStatusError("无有效参数，订单结束");
                    }
                }
            }
        }).start();
    }

    /**
     * 测试数据，在内存卡目录下自己放置文件
     */
    private void testImage(boolean isOpenDoor) {
        paths.clear();
        if (isOpenDoor) {
            paths.put("0", ImageCacheManager.getInstance().getBase_image_path() + File.separator + "test" + File.separator + "0_open.jpg");
            paths.put("1", ImageCacheManager.getInstance().getBase_image_path() + File.separator + "test" + File.separator + "1_open.jpg");
            paths.put("2", ImageCacheManager.getInstance().getBase_image_path() + File.separator + "test" + File.separator + "2_open.jpg");
            paths.put("3", ImageCacheManager.getInstance().getBase_image_path() + File.separator + "test" + File.separator + "3_open.jpg");
        } else {
            paths.put("0", ImageCacheManager.getInstance().getBase_image_path() + File.separator + "test" + File.separator + "0_close.jpg");
            paths.put("1", ImageCacheManager.getInstance().getBase_image_path() + File.separator + "test" + File.separator + "1_close.jpg");
            paths.put("2", ImageCacheManager.getInstance().getBase_image_path() + File.separator + "test" + File.separator + "2_close.jpg");
            paths.put("3", ImageCacheManager.getInstance().getBase_image_path() + File.separator + "test" + File.separator + "3_close.jpg");
        }
    }

    /**
     * 测试数据
     */
    private void testWeight(boolean isOpenDoor) {
        this.serialResults.clear();
        if (isOpenDoor) {
            this.serialResults.put("1 G \n", 2770.145);
            this.serialResults.put("2 G \n", 2469.934);
            this.serialResults.put("3 G \n", 1572.042);

            this.serialResults.put("4 G \n", 5317.201);
            this.serialResults.put("5 G \n", 5129.623);
            this.serialResults.put("6 G \n", 5121.365);

            this.serialResults.put("7 G \n", 4689.8);
            this.serialResults.put("8 G \n", 5272.384);
            this.serialResults.put("9 G \n", 5158.187);

            this.serialResults.put("10 G \n", 3351.305);
            this.serialResults.put("11 G \n", 2621.768);
            this.serialResults.put("12 G \n", 3305.15);
        } else {
            this.serialResults.put("1 G \n", 2770.207);
            this.serialResults.put("2 G \n", 2469.869);
            this.serialResults.put("3 G \n", 1496.766);

            this.serialResults.put("4 G \n", 4772.632);
            this.serialResults.put("5 G \n", 5129.669);
            this.serialResults.put("6 G \n", 4631.766);

            this.serialResults.put("7 G \n", 4689.662);
            this.serialResults.put("8 G \n", 5272.469);
            this.serialResults.put("9 G \n", 4629.493);

            this.serialResults.put("10 G \n", 2990.628);
            this.serialResults.put("11 G \n", 2621.458);
            this.serialResults.put("12 G \n", 3304.83);
        }
    }


    public boolean setTransactionStatusInited(Integer wxUserId) {
        if (this.transactionStatus == TRANSACTION_STATUS_CLOSEDOOR) {
            this.transactionStatus = TRANSACTION_STATUS_INITED;
            this.currentOrder = OsModule.get().getSn() + "-" + System.currentTimeMillis();
            if (wxUserId != null) {
                this.currentOrder += ("--" + wxUserId);
            }
            return true;
        } else {
            ILog.d(TAG, "当前订单状态" + this.transactionStatus + "未完成，不允许重置");
            return false;
        }
    }

    public boolean setTransactionStatusOpendoor(List<RetailInputParam> params) {
        //当前状态为已初始化状态
        if (transactionStatus == TRANSACTION_STATUS_INITED) {
            boolean succ;
            long now = new Date().getTime();
            OPEN_DISPOSAL_DATA_END_TIME = now;
            //参数获取完整后，才开门
            OsModule.get().unlock();
            now = new Date().getTime();
            TimeConsts.OPEN_UPLOAD_DATA_START_TIME = now;
            ILog.d(TIME_TAG, now + ",开始提交开门数据");
            succ = RetailVisManager.openDoor(currentOrder, params);
            if (succ) {
                this.transactionStatus = TRANSACTION_STATUS_OPENDOOR;
            }
            now = new Date().getTime();
            ILog.d(TIME_TAG, now + "提交开门数据" + (succ ? "成功" : "失败") + "\n，transactionStatus 更新为 " + transactionStatus);
            TimeConsts.OPEN_UPLOAD_DATA_END_TIME = now;
            if (messHandler != null) {
                Message m = new Message();
                m.obj = new OpenParam(currentOrder, params);
                m.what = HANDLER_MESSAGE_WHAT_PARMA;
                messHandler.sendMessage(m);
            }
            StringBuilder mess = new StringBuilder();
            mess.append("整理数据时间:").append(OPEN_DISPOSAL_DATA_END_TIME - OPEN_DISPOSAL_DATA_START_TIME).append("ms\n");
            ILog.d(TIME_TAG, mess.toString());
            ILog.d("提交开门数据结束!");
            return succ;
        } else {
            //当前状态异常，恢复为结束状态
            ILog.d("错误的订单状态,transactionStatus:" + transactionStatus);
            return false;
        }
    }

    public boolean setTransactionStatusClosedoor(List<RetailInputParam> params) {
        //当前状态为已开门状态
        if (transactionStatus == TRANSACTION_STATUS_OPENDOOR) {
            boolean succ;
            long now = new Date().getTime();
            CLOSE_DISPOSAL_DATA_END_TIME = now;
            TimeConsts.CLOSE_UPLOAD_DATA_START_TIME = now;
            ILog.d(TIME_TAG, now + "，开始提交关门数据");
            succ = RetailVisManager.closeDoor(currentOrder, params);
            if (succ) {
                this.transactionStatus = TRANSACTION_STATUS_CLOSEDOOR;
            }
            ILog.d(TIME_TAG, new Date().getTime() + "，提交关门数据" + (succ ? "成功" : "失败") + "\ntransactionStatus 更新为 " + transactionStatus);
            TimeConsts.CLOSE_UPLOAD_DATA_END_TIME = now;
            if (messHandler != null) {
                Message m = new Message();
                m.obj = new CloseParam(params);
                m.what = HANDLER_MESSAGE_WHAT_PARMA;
                messHandler.sendMessage(m);
            }
            ILog.d("提交关门数据结束!");
            StringBuilder mess = new StringBuilder();
            mess.append("整理数据时间:").append(CLOSE_DISPOSAL_DATA_END_TIME - CLOSE_DISPOSAL_DATA_START_TIME).append("ms\n");
            ILog.d(TIME_TAG, mess.toString());
            return succ;
        } else {
            ILog.d("错误的订单状态,transactionStatus:" + transactionStatus);
            return false;
        }
    }

    public boolean setTransactionStatusError(String mess) {
        ILog.d(TIME_TAG, "当前订单" + currentOrder + "," + mess + ",重置状态");
        this.transactionStatus = TRANSACTION_STATUS_CLOSEDOOR;
        this.currentOrder = "";
        return true;
    }

    public boolean setOrderResult(String orderno, Integer wxUserId, JSONArray products) {
        if (products != null) {
            OsModule.get().sendRecognizeResult(orderno, wxUserId, products);
        }
        return true;
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

    private Map<String, Double> getSerialResults(boolean isOpen) {
        long now = new Date().getTime();
        if (isOpen) {
            TimeConsts.OPEN_GET_WEIGTHS_START_TIME = now;
        } else {
            TimeConsts.CLOSE_GET_WEIGTHS_START_TIME = now;
        }
        ILog.d(TIME_TAG, new Date().getTime() + ",发送获取重量指令集");
        Map<String, Double> resultMap = new HashMap<>();
        for (String key : this.commandMap.keySet()) {
            List<String> commands = this.commandMap.get(key);
            SerialPortManager manager = this.serials.get(key);
            if (manager != null) {
                resultMap.putAll(manager.sendCommand(commands));
            }
        }
        now = new Date().getTime();
        if (isOpen) {
            TimeConsts.OPEN_GET_WEIGTHS_END_TIME = now;
        } else {
            TimeConsts.CLOSE_GET_WEIGTHS_END_TIME = now;
        }
        ILog.d(TIME_TAG, now + ",获取重量集完成" + GsonUtil.toJson(resultMap));
        return resultMap;
    }


    public void closeAllSerials() {
        for (String key : this.serials.keySet()) {
            SerialPortManager manager = this.serials.get(key);
            manager.close();
        }
    }

}

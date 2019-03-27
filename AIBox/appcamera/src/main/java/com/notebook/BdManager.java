package com.notebook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.retail.RetailInputParam;
import com.baidu.retail.RetailVisManager;
import com.bean.CloseParam;
import com.bean.OpenParam;
import com.bean.Order;
import com.bean.Tuple2;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.box.utils.TimeUtil;
import com.consts.TimeConsts;
import com.lib.sdk.bean.StringUtils;
import com.mgr.ImageCacheManager;
import com.mgr.serial.comn.Device;
import com.mgr.serial.comn.SerialPortManager;
import com.mgr.serial.comn.util.GsonUtil;
import com.serenegiant.usb.UVCCamera;
import com.thefunc.serialportlibrary.SerialPortFinder;
import com.utils.DownloadUtil;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.box.utils.ILog.TIME_TAG;
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
    private Map<String, Tuple2<Long, byte[]>> cameraHandlerMap;
    private static final String BAUDRATE_DEFAULT_VALUE = "115200";
    private String currentImageDir;
    private Map<String, Order> orderMap;

    private BdManager() {
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
        initWeightCommands();
        openWeightPorts();
        this.orderMap = new HashMap<>();
    }


    private void createPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
        }
    }


    private void initWeightCommands() {
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
    @SuppressLint("StaticFieldLeak")
    private void openWeightPorts() {
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        // 设备
        String[] paths = serialPortFinder.getAllDevicesPath();
        this.serials = new HashMap<>();
        k:
        for (String path : paths) {
            if (path.toUpperCase().contains("TTYSWK0")) {
                Device device = new Device(path, BAUDRATE_DEFAULT_VALUE);
                SerialPortManager manager = new SerialPortManager();
                boolean opend = manager.open(device) != null;
                ILog.d(TAG, device.toString() + (opend ? ",打开成功" : ",打开失败"));
                if (opend) {
                    for (String key : commandMap.keySet()) {
                        String command = commandMap.get(key).get(0);
                        Map<String, Double> data = manager.sendCommand(Collections.singletonList(command));
                        if (data.get(command) != null) {
                            ILog.d(TAG, "匹配到" + command + "对应串口:" + manager.toString());
                            serials.put(key, manager);
                            continue k;
                        } else {
                            manager.close();
                        }
                    }
                }
            }
        }
        ILog.d(TAG, "初始化" + serials.size() + "个串口");
    }

    public void putLastCameraImage(String floor, byte[] data) {
        if (hasDownLoadFinish) {
            if (this.cameraHandlerMap == null) {
                this.cameraHandlerMap = new ConcurrentHashMap<>();
            }
            Tuple2<Long, byte[]> floor_data = this.cameraHandlerMap.get(floor);
            long now = new Date().getTime();
            if (floor_data != null) {
                if (now - floor_data.getValue1() > 200) {
                    floor_data.setValue1(now);
                    floor_data.setValue2(data);
                    ILog.d(TAG, "更新" + floor + "图片缓存");
                }
            } else {
                this.cameraHandlerMap.put(floor, new Tuple2<>(now, data));
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
        this.serialResults = getWeightResults(false);
    }


    @Override
    public void onDoorOpen() {
        long now = new Date().getTime();
        TimeConsts.OPEN_COLLECTION_START_TIME = now;
        ILog.d(TIME_TAG, now + ",开始收集开门数据");
        downloadImages(true);
        this.serialResults = null;
        this.serialResults = getWeightResults(true);
    }

    public void initBdConfig() {
        RetailVisManager.load((Activity) null, new BdCallback(this.messHandler));
        RetailVisManager.setAppid(OsModule.get().getSn());
        RetailVisManager.setSK("K1GhMzLG6YAWY3oDSWCjTWIiKo7SjDSP");
        RetailVisManager.setAK("W7SFpfC5o7tS3d4CQugZ8YEglb9QVEzN");
        RetailVisManager.setBoxid(OsModule.get().getSn());
        RetailVisManager.setupCachePath(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "aa_retail");
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

    @SuppressLint("StaticFieldLeak")
    public synchronized void downloadImages(boolean isOpenDoor) {
        long now = new Date().getTime();
        if (isOpenDoor) {
            TimeConsts.OPEN_DOWNLOAD_IMAGES_START_TIME = now;
        } else {
            TimeConsts.CLOSE_DOWNLOAD_IMAGES_START_TIME = now;
        }
        if (hasDownLoadFinish) {
            hasDownLoadFinish = false;
            paths.clear();
            finishNum = 0;
            String currentTime = TimeUtil.getTimeStr();
            String currentDirTime = TimeUtil.getTimeStr2();
            if (isOpenDoor) {
                currentImageDir = ImageCacheManager.getInstance().getBase_image_path() + File.separator + currentDirTime;
                createPath(currentImageDir);
            }
            ILog.d(TIME_TAG, now + ",开始下载图片");
            for (String floor : cameraHandlerMap.keySet()) {
                byte[] bytes = cameraHandlerMap.get(floor).getValue2();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String imageName = currentImageDir + File.separator + floor + linkChar +
                                (isOpenDoor ? "open" + linkChar + currentTime + ".jpg"
                                        : "close" + linkChar + currentTime + ".jpg");
                        if (bytes != null) {
                            try {
                                long start_time = new Date().getTime();
                                Bitmap bitmap = RGB_565ToBitmap(bytes, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT);
                                long decode_time = new Date().getTime();
                                ILog.d(TIME_TAG, imageName + "解析图像时长" + (decode_time - start_time));
                                FileOutputStream fos = new FileOutputStream(imageName, false);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                                fos.flush();
                                fos.close();
                                bitmap.recycle();
                                ILog.d(TIME_TAG, imageName + "保存图像时长" + (new Date().getTime() - decode_time));
                            } catch (Exception e) {
                                onDownloadFailed(new Exception("保存图片失败" + e.getMessage()), isOpenDoor);
                            }
                            if (new File(imageName).exists()) {
                                onDownloadSuccess(floor, imageName, isOpenDoor);
                            } else {
                                onDownloadFailed(new Exception("下载图片超时"), isOpenDoor);
                            }
                        } else {
                            onDownloadFailed(new Exception("下载图片失败"), isOpenDoor);
                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private Bitmap RGB_565ToBitmap(byte[] data, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(data));
        return bmp;
    }

    private Bitmap nv12ToBitmap(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                y = y < 16 ? 16 : y;
                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
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
        if (finishNum >= this.cameraHandlerMap.size()) {
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
        new Thread(() -> {
            Looper.prepare();
            if (setTransactionStatusInited(0)) {
                serialResults = null;
                OsModule.get().sendLockStatus2Server(true);
                TimeConsts.OPEN_COLLECTION_START_TIME = new Date().getTime();
                downloadImages(true);
                serialResults = getWeightResults(true);
            }
        }).start();
    }

    public void testClose() {
        new Thread(() -> {
            serialResults = null;
            Looper.prepare();
            OsModule.get().sendLockStatus2Server(false);
            TimeConsts.CLOSE_COLLECTION_START_TIME = new Date().getTime();
            downloadImages(false);
            serialResults = getWeightResults(false);
        }).start();
    }

    @SuppressLint("StaticFieldLeak")
    private void startRecognize(final boolean isOpenDoor) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (serialResults == null) {
                    try {
                        Thread.sleep(50);
                        ILog.d(TAG, "等待获取重力完成");
                    } catch (InterruptedException e) {
                        ILog.d(TAG, "线程休眠失败");
                    }
                }
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
                    Collections.sort(params, (o1, o2) ->
                            Integer.parseInt(o1.getMcastId()) - Integer.parseInt(o2.getMcastId()));
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
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            this.orderMap.put(this.currentOrder, new Order(this.currentOrder, wxUserId));
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

    public Order setOrderResult(String orderno, JSONArray products, Exception e) {
        Order order = this.orderMap.get(orderno);
        if (order != null) {
            if (e != null) {
                order.setMess(e.getMessage());
            } else if (products != null) {
                order.setProducts(products);
                OsModule.get().sendRecognizeResult(orderno, order.getWxId(), products);
            }
            this.orderMap.remove(order);
            return order;
        }
        return null;
    }

    /**
     * 上传手动验证的订单结果
     */
    public void updateOrderResult(boolean succ, Order order) {
        RetailVisManager.recordOrderResult(order.getOrder(), succ, order.getProducts());
    }

    /**
     * 获取商品重量
     */
    private List<Double> getWeight(Map<String, Double> serialResultMap, int floor) {
        ILog.d(TIME_TAG, new Date().getTime() + ",开始获取摄像头" + floor + "的重量信息");
        List<Double> weights = new ArrayList<>();
        //单个摄像头对应称的数量
        int camera_weights_size = 3;
        for (int j = (floor - 1) * camera_weights_size + 1; j <= floor * camera_weights_size; j++) {
            String command = j + " G \n";
            Double weight = serialResultMap.get(command);
            if (weight != null) {
                weights.add(weight);
            } else {
                weights.add((double) Integer.MAX_VALUE);
            }
        }
        ILog.d(TIME_TAG, new Date().getTime() + ",摄像头" + floor + "获取重量信息完成");
        return weights;
    }


    /**
     * 获取重量
     *
     * @param isOpen
     * @return
     */
    private Map<String, Double> getWeightResults(boolean isOpen) {
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
                ILog.d(TIME_TAG, new Date().getTime() + "开始获取" + key + "数据");
                resultMap.putAll(manager.sendCommand(commands));
                ILog.d(TIME_TAG, new Date().getTime() + "获取" + key + "数据结束");
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


    /**
     * 关闭重量串口
     */
    public void closeAllWeightSerials() {
        for (String key : this.serials.keySet()) {
            SerialPortManager manager = this.serials.get(key);
            manager.close();
        }
    }

}

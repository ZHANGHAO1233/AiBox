package com.mgr.serial.comn;

import android.os.HandlerThread;

import com.box.utils.ILog;
import com.mgr.serial.comn.util.GsonUtil;
import com.thefunc.serialportlibrary.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/3/28 0028.
 * 单个串口管理
 */
public class SerialPortManager implements SerialReadThread.DataReceiver {

    private static final String TAG = "SerialPortManager";

    private SerialReadThread mReadThread;
    private OutputStream mOutputStream;
    private HandlerThread mWriteThread;
    private String desc;

    private SerialPort mSerialPort;
    private Map<String, Double> resultMap;
    private String mResponseBuffer = "";
    private List<String> commands;

    public SerialPortManager() {
    }

    /**
     * 打开串口
     *
     * @param device
     * @return
     */
    public SerialPort open(Device device) {
        this.desc = device.toString();
        return open(device.getPath(), device.getBaudrate());
    }

    /**
     * 打开串口
     *
     * @param devicePath
     * @param baudrate
     * @return
     */
    private SerialPort open(String devicePath, String baudrate) {
        if (mSerialPort != null) {
            close();
        }
        ILog.d(TAG, "SerialPortManager打开串口 devicePath：" + devicePath + "baudrate:" + baudrate);
        try {
            File device = new File(devicePath);
            int baurate = Integer.parseInt(baudrate);
            mSerialPort = new SerialPort(device, baurate, 0);
            mReadThread = new SerialReadThread(mSerialPort.getInputStream(), this);
            mReadThread.start();
            mOutputStream = mSerialPort.getOutputStream();
            mWriteThread = new HandlerThread("write-thread");
            mWriteThread.start();
            ILog.d(TAG, "SerialPortManager打开串口成功");
            return mSerialPort;
        } catch (Throwable tr) {
            ILog.d(TAG, "SerialPortManager打开串口失败" + tr.getMessage());
            close();
            return null;
        }
    }

    /**
     * 关闭串口
     */
    public void close() {
        ILog.d(TAG, "关闭串口");
        if (mReadThread != null) {
            mReadThread.close();
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mWriteThread != null) {
            mWriteThread.quit();
        }

        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    /**
     * 发送数据
     *
     * @param datas
     * @return
     */
    private void sendData(byte[] datas) throws IOException {
        mOutputStream.write(datas);
    }


    /**
     * 发送命令包
     * count 重试次数
     */
    public Map<String, Double> sendCommand(List<String> commands) {
        ILog.d(TAG, "准备发送指令集" + GsonUtil.toJson(commands));
        this.commands = commands;
        this.resultMap = new TreeMap<>();
        this.mResponseBuffer = "";
        int times = 3;
        int current_time = 0;
        while (current_time < times) {
            if (this.resultMap.size() >= commands.size()) {
                ILog.d(TAG, "指令集返回完成");
                break;
            }
            ILog.d(TAG, "正在尝试第" + current_time + "次发送指令集");
            for (String command : commands) {
                if (this.resultMap.get(command) == null) {
                    sendData(command);
                } else {
                    ILog.d(TAG, "指令" + command + "已返回数据，跳过");
                }
            }
            current_time++;
        }
        ILog.d(TAG, "返回质量集" + GsonUtil.toJson(this.resultMap));
        return this.resultMap;
    }

    /**
     * 发送指令
     *
     * @param command 指令
     */
    private void sendData(String command) {
        int single_time = 50;
        try {
            ILog.d(TAG, "发送指令" + command);
            this.sendData(command.getBytes());
            long send_now = new Date().getTime();
            while (new Date().getTime() - send_now <= single_time) {
            }
        } catch (IOException e) {
            ILog.d(TAG, "指令" + command + "发送异常:" + e.getMessage());
        }
    }

    @Override
    public void onDataReceive(byte[] received, int size) {
        String str_received = new String(received, 0, size);
        ILog.d(TAG, "接收到原始数据" + str_received);
        mResponseBuffer += str_received;
        ILog.d(TAG, "拼装缓存数据" + mResponseBuffer);
        int index = mResponseBuffer.indexOf('\n');
        while (index > 0) {
            handleSerialResult(mResponseBuffer.substring(0, index));
            mResponseBuffer = mResponseBuffer.substring(index + 1);
            index = mResponseBuffer.indexOf('\n');
        }
    }

    private void handleSerialResult(String result) {
        ILog.d(TAG, "返回数据" + result);
        String[] datas = result.split(" ");
        ILog.d(TAG, "返回数据拆分为" + GsonUtil.toJson(datas));
        if (datas.length >= 3) {
            String command = datas[0] + " " + datas[1] + " \n";
            ILog.d(TAG, "当前数据对应指令" + command);
            if (this.commands.contains(command)) {
                try {
                    double weight = Double.parseDouble(datas[2]);
                    this.resultMap.put(command, weight);
                    ILog.d(TAG, "添加质量" + command + ":" + this.resultMap.get(command));
                } catch (NumberFormatException e) {
                    ILog.d(TAG, "错误的返回重量");
                }
            } else {
                ILog.d(TAG, "无匹配的目标指令");
            }
        } else {
            ILog.d(TAG, "错误的返回数据");
        }
    }


    //判断浮点数（double和float）
    public static boolean isDouble(String str) {
        ILog.d(TAG, "传入重量" + str);
        if (null == str || "".equals(str)) {
            ILog.d(TAG, "错误的重量值");
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        boolean pass = pattern.matcher(str).matches();
        ILog.d(TAG, pass ? "正确的重量值" : "错误的重量值");
        return pass;
    }

    //判断整数（int）
    public static boolean isInteger(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    @Override
    public String toString() {
        return desc == null ? "" : desc;
    }
}

package com.mgr.serial.comn;

import android.os.HandlerThread;
import android.text.TextUtils;

import com.box.utils.ILog;
import com.thefunc.serialportlibrary.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/3/28 0028.
 * 单个串口管理
 */
public class SerialPortManager1 implements SerialReadThread.DataReceiver {

    private static final String TAG = "SerialPortManager";

    private SerialReadThread mReadThread;
    private OutputStream mOutputStream;
    private HandlerThread mWriteThread;
    private String receive_data;
    private String command;
    private String desc;
    private long returned_time;
    private boolean returned;

    private SerialPort mSerialPort;

    public SerialPortManager1() {
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

    public boolean isOpened() {
        return this.mSerialPort != null;
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
    public String sendCommand(String command, int count) {
        //试count次
        int i = 0;
        String data = "";
        while (i < count) {
            ILog.d(TAG, "尝试发送第" + i + "次指令");
            data = this.sendCommand(command);
            if (!TextUtils.isEmpty(data)) {
                break;
            }
            i++;
        }
        return data;
    }

    /**
     * 发送命令包
     */
    public String sendCommand(String command) {
        ILog.d(TAG, "发送指令：" + command);
        byte[] bytes = command.getBytes();
        this.receive_data = "";
        this.command = command;
        this.returned_time = -1;
        this.returned = false;
        try {
            this.sendData(bytes);
            long start = new Date().getTime();
//            while (new Date().getTime() - start <= 1000 && !(this.receive_data.contains("\n") && new Date().getTime() - start >= 100)) {
            while (new Date().getTime() - start <= 1000) {
                if (!TextUtils.isEmpty(this.receive_data) && this.returned_time != -1 && new Date().getTime() - this.returned_time >= 200) {
                    ILog.d(TAG, command + "已获取到完整数据" + receive_data);
                    break;
                }
            }
            this.returned = true;
            return this.receive_data;
        } catch (IOException e) {
            return "";
        }
    }


    @Override
    public void onDataReceive(byte[] received, int size) {
//        String hexStr = ByteUtil.bytes2HexStr(received, 0, size);
        //当前发送的指令不空
        String command = this.command.replace("\n", "");
        if (!TextUtils.isEmpty(command) && !this.returned) {
            //接收到的数据
            String buff = new String(received);
            ILog.d(TAG, "接收到数据" + buff);
            String[] datas = buff.split(" ");
            if (buff.startsWith(command) && datas.length >= 3 && !isInteger(datas[2]) && isDouble(datas[2])) {
                this.receive_data = buff.split("\n")[0];
                this.returned_time = new Date().getTime();
            }
        }
    }


    //判断浮点数（double和float）
    public static boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
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

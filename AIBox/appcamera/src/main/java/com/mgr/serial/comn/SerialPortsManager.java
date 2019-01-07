package com.mgr.serial.comn;

import com.box.utils.ILog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 串口集合管理
 *
 * @author zhangh
 * @version 1.0.1
 */

public class SerialPortsManager {
    private static final String TAG = "SerialPortsManager";

    private static SerialPortsManager serialPortsManager;


    private Map<Device, SerialPortManager> serialPortManagers;

    private SerialPortsManager() {
    }

    public static SerialPortsManager getInstance() {
        if (serialPortsManager == null) {
            serialPortsManager = new SerialPortsManager();
        }
        return serialPortsManager;
    }

    public void initSerialPort(List<Device> devices) {
        ILog.d(TAG, "初始化" + devices.size() + "个串口");
        this.serialPortManagers = new HashMap<>();
        for (Device device : devices) {
            SerialPortManager serialPortManager = new SerialPortManager();
            boolean opend = serialPortManager.open(device) != null;
            ILog.d(TAG, device.toString() + (opend ? ",打开成功" : ",打开失败"));
            this.serialPortManagers.put(device, serialPortManager);
        }
    }

    /**
     * 关闭串口
     */
    public void close(Device device) {
        ILog.d(TAG, "关闭第" + device.toString());
        if (this.serialPortManagers != null) {
            SerialPortManager manager = this.serialPortManagers.get(device);
            if (manager != null) {
                manager.close();
            }
        }
    }

    /**
     * 关闭所有串口
     */
    public void closeAll() {
        if (this.serialPortManagers != null) {
            for (Device device : this.serialPortManagers.keySet()) {
                this.close(device);
            }
        }
    }

//    /**
//     * 发送指令
//     *
//     * @return
//     */
//    public String sendCommand(Device device, String command, int count) {
//        ILog.d(TAG, "发送指令给第" + device.toString());
//        if (this.serialPortManagers != null) {
//            SerialPortManager manager = this.serialPortManagers.get(device);
//            if (manager != null && manager.isOpened()) {
//                return manager.sendCommand(command, count);
//            }
//            ILog.d(TAG, "该串口未打开");
//        }
//        return "";
//    }
}

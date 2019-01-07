package com.mgr.serial.comn;

import android.os.SystemClock;

import com.box.utils.ILog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 读串口线程
 */
public class SerialReadThread extends Thread {

    private static final String TAG = "SerialReadThread";

    private BufferedInputStream mInputStream;
    private DataReceiver receiver;

    public SerialReadThread(InputStream is, DataReceiver receiver) {
        mInputStream = new BufferedInputStream(is);
        this.receiver = receiver;
    }

    @Override
    public void run() {
        byte[] received = new byte[1024];
        int size;

        ILog.d(TAG, "开始读线程");

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            try {

                int available = mInputStream.available();

                if (available > 0) {
                    size = mInputStream.read(received);
                    if (size > 0) {
                        onDataReceive(received, size);
                    }
                } else {
                    // 暂停一点时间，免得一直循环造成CPU占用率过高
                    SystemClock.sleep(1);
                }
            } catch (IOException e) {
            }
            //Thread.yield();
        }

        ILog.d(TAG, "结束读进程");
    }

    /**
     * 处理获取到的数据
     *
     * @param received
     * @param size
     */
    private void onDataReceive(byte[] received, int size) {
        if (this.receiver != null) {
            this.receiver.onDataReceive(received, size);
        }
    }

    /**
     * 停止读线程
     */
    public void close() {

        try {
            mInputStream.close();
        } catch (IOException e) {
            ILog.d(TAG, "异常" + e.getMessage());
        } finally {
            super.interrupt();
        }
    }

    interface DataReceiver {
        void onDataReceive(byte[] received, int size);
    }
}

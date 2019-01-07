package com.box.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by DELL on 2017/11/9.
 */

public class LogUtil {//日志管理工具�?
    private static String TAG = "box_business_log";
    private static String logPath = "";//log日志存放路径
    private static String logName = "";//日志文件名称
    private static final String FILE_END_PREFIX = ".txt";//日志命名后缀
    private static final int logSaveDays = 7;
    private static String deleteLogDate = "";


    public static void init(Context context) {
        logPath = getFilePath();//获得文件储存路径,在后面加"/Logs"建立子文件夹
        if (TextUtils.isEmpty(logPath)) {
            Log.d(TAG, "logPath == null ，未初始化LogToFile");
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String time = dateFormat.format(new Date());
        logName = logPath + File.separator + "logs" + time + FILE_END_PREFIX;//log日志�?
        Log.d(TAG, "logName：" + logName);
        try {
            File file = new File(logPath);
            File file1 = new File(logName);
            if (file1.isDirectory()) {
                file1.delete();
            }
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("打开程序；" + "\r\n");
        writeMsg(TAG, new String(builder));

        deleteOutdateLog(time);
    }

    private static void deleteOutdateLog(String date) {
        try {
            if (!TextUtils.isEmpty(date) && !date.equals(deleteLogDate)) {
                deleteLogDate = date;
                File files[] = new File(logPath).listFiles();
                if (files != null && files.length > logSaveDays) {
                    List<String> fs = new ArrayList<>(logSaveDays);
                    Date dNow = new Date();   //当前时间
                    Date dBefore;
                    Calendar calendar = Calendar.getInstance(); //得到日历
                    calendar.setTime(dNow);//把当前时间赋给日历
                    calendar.add(Calendar.DAY_OF_MONTH, -1);  //设置为前一天
                    dBefore = calendar.getTime();   //得到前一天的时间
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                    for (int i = 0; i < logSaveDays; i++) {
                        calendar.add(Calendar.DAY_OF_MONTH, 0 - i);
                        String time = dateFormat.format(dBefore);
                        String filename = "logs" + time + FILE_END_PREFIX;
                        fs.add(filename);
                    }
                    for (File file : files) {
                        if (!fs.contains(file.getName())) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeBroadcast(boolean receiveOrSend, Intent intent) {
        Bundle bundle = intent.getExtras();
        String info = "";
        String getOrSend = receiveOrSend ? "receive broadcast:" : "send broadcast:";
        Date ss = new Date();
        SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        info = format0.format(ss.getTime()) + "," + getOrSend;//这个就是把时间戳经过处理得到期望格式的时间
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                info = info + "Key=" + key + ", value=" + value + ";";
            }
        }
        info = info + "\r\n" + "\r\n";
        writeMsg(TAG, info);
    }

    /**
     * 获得文件存储路径
     *
     * @return
     */
    private static String getFilePath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(path);
        ILog.d("path:" + path + ":exists:" + file.exists());
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    /**
     * 将log信息写入文件�?
     *
     * @param msg
     */
    public static void writeMsg(String tag, String msg) {
        SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time0 = format0.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格式的时间
        msg = time0 + ":" + msg + "\r\n";
        FileOutputStream fos = null;//FileOutputStream会自动调用底层的close()方法，不用关�?
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(logName, true);//这里的第二个参数代表追加还是覆盖，true为追加，flase为覆�?
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(tag + ":" + msg);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();//关闭缓冲�?
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

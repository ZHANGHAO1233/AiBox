package com.box.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by DELL on 2017/11/9.
 */

public class LogUtil {//日志管理工具�?
    private static String TAG = "box_business_log";
    private static String logPath = "";//log日志存放路径
    private static String logName = "";//日志文件名称
    private static final String FILE_END_PREFIX = ".txt";//日志命名后缀
    private static final int logSaveDays = 3;


    public static void init(Context context) {
        logPath = getFilePath();//获得文件储存路径,在后面加"/Logs"建立子文件夹
        if (TextUtils.isEmpty(logPath)) {
            Log.d(TAG, "logPath == null ，未初始化LogToFile");
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(new Date().getTime());//这个就是把时间戳经过处理得到期望格式的时间
        logName = logPath + File.separator + "logs" + time + FILE_END_PREFIX;//log日志
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

        deleteOutdateLog();
    }

    private static void deleteOutdateLog() {
        try {
            File files[] = new File(logPath).listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile() && files[i].getName().contains("logs")) {
                    try {
                        File ff = files[i];
                        long time = ff.lastModified();
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(time);
                        Date lastModified = cal.getTime();
                        //(int)(today.getTime() - lastModified.getTime())/86400000;
                        long days = getDistDates(new Date(), lastModified);
                        if (days >= logSaveDays) {
                            files[i].delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param startDate
     * @param endDate
     */
    public static long getDistDates(Date startDate, Date endDate) {
        long totalDate = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        long timestart = calendar.getTimeInMillis();
        calendar.setTime(endDate);
        long timeend = calendar.getTimeInMillis();
        totalDate = Math.abs((timeend - timestart)) / (1000 * 60 * 60 * 24);
        return totalDate;
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
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"AILogs";
        File file = new File(path);
        ILog.d("path:" + path + ":exists:" + file.exists());
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    /**
     * 将log信息写入文件
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

package com.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Curry on 2018/10/17.
 */

public class DownloadUtil {
    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;

    public static DownloadUtil get() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    public DownloadUtil() {
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(10 * 1000, TimeUnit.MILLISECONDS)//设置读取超时时间
                .writeTimeout(10 * 1000, TimeUnit.MILLISECONDS)//设置写的超时时间
                .connectTimeout(10 * 1000, TimeUnit.MILLISECONDS)//设置连接超时时间
                .build();
    }

    /**
     * @param url      下载连接
     * @param listener 下载监听
     */
    public void download(final String url, final String camera, final OnDownloadListener listener, final boolean isOpenDoor) {
        Request request = new Request.Builder().url(url).build();
        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败监听回调
                listener.onDownloadFailed(e, isOpenDoor);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = response.body().byteStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buf = new byte[2048];
                int len;
                while ((len = is.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                byte[] bu = outputStream.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bu, 0, bu.length);
                //下载完成
                listener.onDownloadSuccess(camera, bitmap, isOpenDoor);
            }
        });
    }

    public interface OnDownloadListener {
        /**
         * 下载成功之后的文件
         */
        void onDownloadSuccess(String camera, Bitmap bitmap, boolean isOpenDoor);

        /**
         * 下载异常信息
         */

        void onDownloadFailed(Exception e, boolean isOpenDoor);
    }
}

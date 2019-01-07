package com.utils;

import java.io.File;
import java.io.FileOutputStream;
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
     * @param url          下载连接
     * @param destFileDir  下载的文件储存目录
     * @param fileName 下载文件名称
     * @param listener     下载监听
     */
    public void download(final String url, final String destFileDir, final String fileName, final OnDownloadListener listener, final boolean isOpenDoor) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败监听回调
                listener.onDownloadFailed(e, isOpenDoor);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                //储存下载文件的目录
                File dir = new File(destFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir+File.separator+ fileName);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        //下载中更新进度条
                        listener.onDownloading(progress, isOpenDoor);
                    }
                    fos.flush();
                    //下载完成
                    listener.onDownloadSuccess(file, isOpenDoor);
                } catch (Exception e) {
                    listener.onDownloadFailed(e, isOpenDoor);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public interface OnDownloadListener {
        /**
         * 下载成功之后的文件
         */
        void onDownloadSuccess(File file, boolean isOpenDoor);

        /**
         * 下载进度
         */
        void onDownloading(int progress, boolean isOpenDoor);

        /**
         * 下载异常信息
         */

        void onDownloadFailed(Exception e, boolean isOpenDoor);
    }
}

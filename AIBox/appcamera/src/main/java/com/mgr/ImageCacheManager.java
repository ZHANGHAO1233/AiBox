package com.mgr;

import android.os.Environment;
import android.os.Handler;

import com.box.utils.ILog;

import java.io.File;

/**
 * @author zhangh
 * @version 1.0.1
 */

public class ImageCacheManager {
    private static final String TAG = "ImageCacheManager";
    private static ImageCacheManager sFaceCacheManager;
    //图片缓存最大数量
    private static int MAX_CACHE_IMAGE_SIZE = 10;
    //图片缓存清除定时时间
    private static int CACHE_IMAGE_DELAY = 1000 * 60 * 30;
    //图片地址
    private String base_image_path;

    private ImageCacheManager() {

    }


    public static ImageCacheManager getInstance() {
        if (sFaceCacheManager == null) {
            sFaceCacheManager = new ImageCacheManager();
        }
        return sFaceCacheManager;
    }

    public void init() {
        this.base_image_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AiBox" + File.separator + "AiImages";
        File f = new File(base_image_path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    public String getBase_image_path() {
        return base_image_path == null ? "" : base_image_path;
    }

    /**
     * 开始图片缓存清理
     */
    public void startImageCacheCleaning() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                cleaningImageCache();
                handler.postDelayed(this, CACHE_IMAGE_DELAY);
            }
        });
    }


    private int cleaningImageCache() {
        int result = 0;
        File file = new File(this.base_image_path);
        if (!file.exists() || !file.isDirectory()) {
            return result;
        }
        String[] paths = file.list();
        for (int i = 0; i < paths.length - MAX_CACHE_IMAGE_SIZE; i++) {
            if (deleteDirectory(this.base_image_path + File.separator + paths[i])) {
                ++result;
            }
        }
        return result;
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            ILog.d(TAG, "删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = files[i].delete();
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            ILog.d(TAG, "删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            ILog.d(TAG, "删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }


    public int cleaningAllImageCache() {
        int result = 0;
        File file = new File(this.base_image_path);
        if (!file.exists() || !file.isDirectory()) {
            return result;
        }
        String[] paths = file.list();
        for (int i = 0; i < paths.length; i++) {
            File image = new File(this.base_image_path + File.separator + paths[i]);
            if (image.delete()) {
                ++result;
            }
        }
        return result;
    }


}

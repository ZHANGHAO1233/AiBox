package com.mgr;

import android.os.Environment;

import com.bean.Order;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author zhangh
 * @version 1.0.1
 */

public class OrderFileManager {
    private static final String TAG = "OrderFileManager";

    private static OrderFileManager sOrderFileManager;
    private String base_path;

    private OrderFileManager() {
        this.base_path = Environment.getExternalStorageDirectory() + File.separator + "aa_retail";
        File file = new File(base_path);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static OrderFileManager getInstance() {
        if (sOrderFileManager == null) {
            sOrderFileManager = new OrderFileManager();
        }
        return sOrderFileManager;
    }

    public void writeOrder(boolean succ, Order order) throws Exception {
        String path = createOrderDir(order);
        File saveFile = new File(path, succ ? "ok.txt" : "error.txt");
        FileOutputStream outStream = new FileOutputStream(saveFile);
        if (order.getProducts() != null)
            outStream.write(order.getProducts().toString().getBytes());
        if (order.getMess() != null)
            outStream.write(order.getMess().getBytes());
        outStream.close();
    }

    private String createOrderDir(Order order) {
        String dir = this.base_path + File.separator + order.getOrder();
        File dir_file = new File(dir);
        if (!dir_file.exists()) {
            dir_file.mkdir();
        }
        return dir;
    }
}

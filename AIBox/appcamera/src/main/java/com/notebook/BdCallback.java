package com.notebook;

import android.os.Handler;
import android.os.Message;

import com.baidu.retail.BaiduGlobalVar;
import com.baidu.retail.IRetailCallBack;
import com.bean.Order;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.consts.HandleConsts;
import com.consts.TimeConsts;

import org.json.JSONArray;

import java.util.Date;
import java.util.List;

import static com.box.utils.ILog.TIME_TAG;
import static com.notebook.BdManager.TRANSACTION_STATUS_RESULT;

/**
 * Created by Curry on 2018/10/12.
 */
public class BdCallback implements IRetailCallBack {
    private Handler handler;

    public BdCallback(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void callbackOrder(String order, Exception e, JSONArray products) {
        long now = new Date().getTime();
        String mess = now + "，订单返回结果:" + order + ",";
        if (e != null) {
            mess += e.getMessage() + ",";
        }
        if (products != null) {
            mess += products;
        }
        ILog.d(TIME_TAG, mess);
        if (BdManager.getBd().setTransactionStatusResult(order,products)) {
            if (this.handler != null) {
                Message message = new Message();
                message.what = HandleConsts.HANDLER_MESSAGE_WHAT_ORDER;
                String error_mess = e == null ? "" : e.getMessage();
                String products_mess = products == null ? "" : products.toString();
                message.obj = new Order(order, products_mess, error_mess);
                this.handler.sendMessage(message);
            }
            TimeConsts.ORDER_END_TIME = now;
            ILog.d(TIME_TAG, now + "，transactionStatus 更新为 " + TRANSACTION_STATUS_RESULT);
            ILog.d(TIME_TAG, "SDK上传前处理时间" + (BaiduGlobalVar.endSDKTime - BaiduGlobalVar.startSDKTime));
            ILog.d(TIME_TAG, "SDK网络返回后处理时间:" + (System.currentTimeMillis() - BaiduGlobalVar.startResponseTime));
            ILog.d(TIME_TAG, "调用关门到返回的时间：time:" + (BaiduGlobalVar.endNetTime - BaiduGlobalVar.endSDKTime));
            String detail = TimeConsts.write();
            ILog.d(TIME_TAG, detail);
        }
    }

    @Override
    public void callbackOrderWithStrange(String s, Exception e, JSONArray jsonArray, List<String> list) {
        ILog.d(TIME_TAG, "回调新方法");
    }
}

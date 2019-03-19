package com.notebook;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.retail.BaiduGlobalVar;
import com.baidu.retail.IRetailCallBack;
import com.bean.Order;
import com.box.utils.ILog;
import com.consts.HandleConsts;
import com.consts.TimeConsts;

import org.json.JSONArray;

import java.util.Date;

import static com.box.utils.ILog.TIME_TAG;

/**
 * Created by Curry on 2018/10/12.
 */
public class BdCallback implements IRetailCallBack {
    private Handler handler;

    public BdCallback(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void callbackOrder(String orderno, Exception e, JSONArray products) {
        long now = new Date().getTime();
        String mess = now + "，订单返回结果:" + orderno + ",";
        if (e != null) {
            mess += e.getMessage() + ",";
        }
        if (products != null) {
            mess += products;
        }
        ILog.d(TIME_TAG, mess);
        Order order = BdManager.getBd().setOrderResult(orderno, products, e);
        if (order != null) {
            if (this.handler != null) {
                Message message = new Message();
                message.what = HandleConsts.HANDLER_MESSAGE_WHAT_ORDER;
                message.obj = order;
                this.handler.sendMessage(message);
            }
            TimeConsts.ORDER_END_TIME = now;
            ILog.d(TIME_TAG, "SDK上传前处理时间" + (BaiduGlobalVar.endSDKTime - BaiduGlobalVar.startSDKTime));
            ILog.d(TIME_TAG, "SDK网络返回后处理时间:" + (System.currentTimeMillis() - BaiduGlobalVar.startResponseTime));
            ILog.d(TIME_TAG, "调用关门到返回的时间：time:" + (BaiduGlobalVar.endNetTime - BaiduGlobalVar.endSDKTime));
            String detail = TimeConsts.write();
            ILog.d(TIME_TAG, detail);
        }
    }

    @Override
    public void containerCallback(String orderno, JSONArray jsonArray, int i, Exception e) {
        String mess = "containerCallback:";
        if (!TextUtils.isEmpty(orderno)) {
            mess += (orderno + ",");
        }
        if (e != null) {
            mess += (e.getMessage() + ",");
        }
        if (mess != null) {
            mess += mess;
        }
        ILog.d(TIME_TAG, mess);
    }


    @Override
    public void recordOrderResultCallback(String errorCode, Exception e) {
        String mess = "订单结果上传:" + ("0".equals(errorCode) ? "成功" : "失败");
        if (e != null) {
            mess += e.getMessage() + ",";
        }
        ILog.d(TIME_TAG, mess);
    }
}

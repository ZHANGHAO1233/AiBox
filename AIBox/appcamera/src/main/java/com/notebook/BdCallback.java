package com.notebook;

import android.os.SystemClock;

import com.baidu.retail.BaiduGlobalVar;
import com.baidu.retail.IRetailCallBack;
import com.box.core.OsModule;
import com.box.utils.ILog;

import org.json.JSONArray;

import java.util.Date;

import static com.box.utils.ILog.TIME_TAG;
import static com.notebook.BdManager.TRANSACTION_STATUS_RESULT;

/**
 * Created by Curry on 2018/10/12.
 */

public class BdCallback implements IRetailCallBack {

    @Override
    public void callbackOrder(String order, Exception e, JSONArray products) {
        ILog.d(TIME_TAG, new Date().getTime() + "，订单返回结果:" + order + "," + e.getMessage() + "," + products);
        BdManager.transactionStatus = TRANSACTION_STATUS_RESULT;
        ILog.d(TIME_TAG, new Date().getTime() + "，transactionStatus 更新为 " + TRANSACTION_STATUS_RESULT);
        ILog.d(TIME_TAG,"SDK上传前处理时间"+ (BaiduGlobalVar.endSDKTime- BaiduGlobalVar.startSDKTime));
        ILog.d(TIME_TAG, "SDK网络返回后处理时间:" + (System.currentTimeMillis() - BaiduGlobalVar.startResponseTime));
        ILog.d(TIME_TAG,"调用关门到返回的时间：time:"+(BaiduGlobalVar.endNetTime-BaiduGlobalVar.endSDKTime));
        OsModule.get().sendRecognizeResult(products);
    }
}

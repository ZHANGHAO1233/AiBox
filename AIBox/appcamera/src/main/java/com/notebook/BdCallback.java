package com.notebook;

import com.baidu.retail.IRetailCallBack;
import com.box.core.OsModule;
import com.box.utils.ILog;

import org.json.JSONArray;

/**
 * Created by Curry on 2018/10/12.
 */

public class BdCallback implements IRetailCallBack {

    @Override
    public void callbackOrder(String order, Exception e, JSONArray products) {
        ILog.d("--callbackOrder:" + order + "," + e + "," + products);
        BdManager.transactionStatus = 2;
//        if (null != products && products.length() > 0) {
        ILog.d("send recognize result to server,msg:" + products);
        OsModule.get().sendRecognizeResult(products);
//        }
    }
}

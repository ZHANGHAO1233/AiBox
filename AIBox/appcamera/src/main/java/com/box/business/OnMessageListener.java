package com.box.business;

/**
 * Created by WESHAPE-DEV02 on 2018/3/10.
 */

public interface OnMessageListener {
    void onMessage(String msg);
    void onConnetting();
    void onOpen();
    void onClosed();
    void onClosing();
}

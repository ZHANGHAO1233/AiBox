package com.idata.aibox.core;

import android.text.TextUtils;

import com.idata.aibox.MyApplication;
import com.idata.aibox.business.OnMessageListener;
import com.idata.aibox.utils.AesUtil;
import com.idata.aibox.utils.MD5Test;
import com.idata.aibox.utils.MyLog;
import com.idata.aibox.utils.NetworkUtil;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Curry on 2018/7/17.
 */

public class ServerModule {
    private static WebSocketClient socketClient;
    private static ServerModule server;
    private String encryptPwd = "9U6wnu";
    private String accessId = "jm6S8u";
    private int max = 1000000;//最大连接次数
    private int connectCount = 0;//已连接次数
    private int pingDiff = 5 * 60 * 1000;//检测是否断线间隔,3分钟
    private int reConnectDiff = 12000;//重连间隔
    private int connectTimeout = 10000;//连接超时时间
    //        private String serverAddr = "ws://192.168.1.235:8080/retailws/box";
//    private String serverAddr = "ws://120.76.27.230:9010/retailws/box";
//    private String serverAddr = "ws://192.168.1.150:8080/retailws/box";
    private String serverAddr = "wss://retail.idatachina.com/box";
    private PingThread pingThread;

    private ServerModule() {
    }

    public static ServerModule getServer() {//网络状态监测，开机自启动
        if (server == null) {
            server = new ServerModule();
        }
        return server;
    }

    public boolean isConnecting() {
        return socketClient != null && socketClient.isOpen();
    }

    private String getServerAddress() {
//        String sn = TextUtils.isEmpty(OsModule.getOsOperator().getSn()) ? "1274023" : OsModule.getOsOperator().getSn();
        String sn = "1274023";
        MyLog.d("device sn:" + sn);
//        String signStr = "sn=$" + sn + "&containerid=$" + sn + "&requestmillis=$" + System.currentTimeMillis() + "&accessid=$" + accessId;
        long millis = System.currentTimeMillis();
        String signStr = sn + "&" + sn + "&" + millis;
        MyLog.d("MD5:" + MD5Test.getMD5(signStr));
        MyLog.d("zero:" + signStr);
        String signEncrypt = AesUtil.encrypt(signStr, encryptPwd);
        MyLog.d("one:" + signEncrypt);
        MyLog.d("erjizhi:" + signEncrypt.getBytes().length);
        String sign = MD5Test.getMD5(signEncrypt);
        MyLog.d("two:" + sign);
//        signStr = sign + "&signature=$" + sign;
        signStr = "sn=" + sn + "&containerid=" + sn + "&requestmillis=" + millis + "&accessid=" + accessId + "&signature=" + sign;
        MyLog.d("three:" + signStr);
        String serverAddress = serverAddr + "?" + signStr;
        MyLog.d("url info:" + serverAddress);
        return serverAddress;
    }

    public void connetToServer() {
        try {
            if (isConnecting()) {
                return;
            }
            MyLog.d("start connect to server!");
            socketClient = new WebSocketClient(new URI(getServerAddress()), new Draft_6455() {
            }, null, connectTimeout) {
                @Override
                public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {
                    super.onWebsocketHandshakeSentAsClient(conn, request);
                    MyLog.d("发送握手了");
                }

                @Override
                public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException {
                    super.onWebsocketHandshakeReceivedAsClient(conn, request, response);
                    MyLog.d("接受到握手了");
                }

                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    MyLog.d("连接打开onOpen()");
                    if (listener != null)
                        listener.onOpen();

                    //连接次数归零，取消定时连接任务
                    cancelTimer();

                    //开启定时心跳机制
                    if (pingThread != null) {
                        pingThread.stopThread();
                    }
                    pingThread = new PingThread();
                    pingThread.start();
                }

                @Override
                public void onMessage(String message) {
                    MyLog.d("received msg:" + message);
                    if (listener != null)
                        listener.onMessage(message);
                    if (!TextUtils.isEmpty(message)) {
                        try {
                            String sid, cmd;
                            JSONObject json = new JSONObject(message);
                            sid = json.getString("sid");
                            cmd = json.getString("cmd");
                            OsModule os = OsModule.getOsOperator();
                            if (cmd.equals("RetailOpen")) {
                                os.unlock();
                            } else if (cmd.equals("RetailClose")) {
                                os.lock();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            MyLog.d("onMessage（） exception:" + e.getMessage());
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    MyLog.d("关闭断开连接onClose（）,reason:" + reason + ":remote:" + remote + ":code:" + code);
                    if (listener != null)
                        listener.onClosed();
                    if (CloseFrame.ABNORMAL_CLOSE == code || CloseFrame.NORMAL == code || code == -1) {
                        //code=-1,表示网络没有连接！
                        if (timer == null) {
                            reconnet();
                        }
                    }
                }

                @Override
                public void onClosing(int code, String reason, boolean remote) {
                    MyLog.d("连接断开中:reason:" + reason + ":remote:" + remote + ":code:" + code);
                    if (listener != null)
                        listener.onClosing();
                    super.onClosing(code, reason, remote);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                    MyLog.d("onError（） exception:" + ex.getMessage());
                }
            };
            //设置间隔检查
            socketClient.setConnectionLostTimeout(pingDiff);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            MyLog.d("URISyntaxException:" + e.getMessage());
        }
        if (listener != null) {
            listener.onConnetting();
        }
        socketClient.connect();
    }

    public void disconnect() {
        if (socketClient != null && socketClient.isOpen()) {
            socketClient.close();
            socketClient = null;
        }

        if (pingThread != null) {
            pingThread.stopThread();
            pingThread = null;
        }
    }

    /**
     * 发送消息
     *
     * @param message
     */
    public void sendMessage(String message) {
        if (socketClient != null && socketClient.isOpen()) {
            MyLog.d("发送的消息:" + message);
            socketClient.send(message);
        } else {
            MyLog.d("已经断线,需要重新。。。");
            if (timer == null) {
                reconnet();
            }
        }
    }

    private Timer timer;

    /**
     * 重新连接
     */
    public void reconnet() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    //打印一下网络信息
                    MyLog.d("准备重连，检查网络状况！");
                    if (NetworkUtil.isNetworkAvailable(MyApplication.getContext())) {
                        if (socketClient.isClosed() && !socketClient.reconnectBlocking()) {
                            MyLog.d("正在进行第" + (connectCount + 1) + "次重连");
                            connectCount++;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 0, reConnectDiff);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            connectCount = 0;
        }
    }

    private OnMessageListener listener;

    public void setListener(OnMessageListener listener) {
        this.listener = listener;
    }

    private class PingThread extends Thread {
        private boolean goon = true;

        public void stopThread() {
            this.goon = false;
        }

        @Override
        public void run() {
            try {
                while (goon) {
                    Thread.sleep(pingDiff);
                    if (isConnecting()) {
                        MyLog.d("send ping()");
                        socketClient.sendPing();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

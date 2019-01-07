package com;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.box.business.WebsocketService;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.idata.aibox.R;
import com.notebook.BdManager;
import com.notebook.NotebookActivity;


public class MainActivity extends Activity implements ServiceConnection, View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectAndStartService();
        initView();
        Intent intent = new Intent(MainActivity.this, NotebookActivity.class);
        startActivity(intent);
        finish();
    }

    private void initView() {
        findViewById(R.id.bt_send).setOnClickListener(this);//发送
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_send:
                OsModule.get().sendLockStatus2Server(false);
                break;
        }
    }

    private void connectAndStartService() {
        ILog.d("activity start service!is service running:" + WebsocketService.isServiceRunning("com.idata.aibox.business.WebsocketService", this));
        if (!WebsocketService.isServiceRunning("com.idata.aibox.business.WebsocketService", this)) {
            startService(new Intent(this, WebsocketService.class));
        }
        bindService(new Intent(this, WebsocketService.class), this, 0);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        WebsocketService socketService = ((WebsocketService.MyBinder) service).getService();
//        ServerModule.getServer().setListener(new OnMessageListener() {
//            @Override
//            public void onMessage(String msg) {
//
////                tvStatus.setText(msg);
//            }
//
//            @Override
//            public void onConnetting() {
////                tvStatus.setText("连接中。。。。");
//            }
//
//            @Override
//            public void onOpen() {
////                tvStatus.setText("处于打开状态。。。。");
//            }
//
//            @Override
//            public void onClosed() {
////                tvStatus.setText("已经关闭。。。。");
//            }
//
//            @Override
//            public void onClosing() {
////                tvStatus.setText("正在关闭。。。。");
//            }
//        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
//        stopService(new Intent(this, WebsocketService.class));
    }
}


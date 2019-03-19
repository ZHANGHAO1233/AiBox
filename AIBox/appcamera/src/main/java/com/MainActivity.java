package com;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.view.View;

import com.box.business.WebsocketService;
import com.box.core.OsModule;
import com.box.utils.CrashHandler;
import com.box.utils.ILog;
import com.box.utils.LogUtil;
import com.box.utils.NetworkUtil;
import com.example.download.XDownloadFileManager;
import com.idata.aibox.R;
import com.idata.iot.sdk.api.IDataApi;
import com.lib.funsdk.support.FunPath;
import com.lib.funsdk.support.FunSupport;
import com.mgr.ConfigPropertiesManager;
import com.mgr.ImageCacheManager;
import com.mgr.UncaughtExceptionManager;
import com.notebook.NotebookActivity;
import com.utils.PermissionUtil;

import java.util.List;


public class MainActivity extends Activity implements ServiceConnection, View.OnClickListener
        , PermissionUtil.OnRequestPermissionsResultCallbacks {
    private static final int REQUEST_CODE_CONFIG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectAndStartService();
        initView();
        initConfig();
    }

    /**
     * 初始化Log4j
     */
    private void initConfig() {
        if (PermissionUtil.getExternalStoragePermissions(this, REQUEST_CODE_CONFIG)) {
            LogUtil.init();
            ImageCacheManager.getInstance().init();
            ImageCacheManager.getInstance().startImageCacheCleaning();
            NetworkUtil.init(this);
            CrashHandler.getInstance().init(this);
            OsModule.get().init(this);
            ILog.d("定时重启时间：" + IDataApi.System.getTimeReboot());
            /**
             * 以下是FunSDK初始化
             */
            FunSupport.getInstance().init(this);

            /**
             * 以下是网络图片下载等的本地缓存初始化,可以加速图片显示,和节省用户流量
             * 跟FunSDK无关,只跟com.example.download内容相关
             */
            String cachePath = FunPath.getCapturePath();
            XDownloadFileManager.setFileManager(
                    cachePath,                // 缓存目录
                    20 * 1024 * 1024        // 20M的本地缓存空间
            );

            //新添加的代码
            IDataApi.init(this);
//        IDataApi.System.requestRouteToHost(ConnectivityManager.TYPE_ETHERNET, CameraConfigs.ips[0]);
            ConfigPropertiesManager.getInstance().init(this);
            UncaughtExceptionManager.getInstance().init();
            Intent intent = new Intent(MainActivity.this, NotebookActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 请求结果回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 允许权限请求回调
     *
     * @param requestCode
     * @param list
     * @param all
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list, boolean all) {
        if (all) {
            switch (requestCode) {
                case REQUEST_CODE_CONFIG:
                    this.initConfig();
                    break;
            }
        }
    }

    /**
     * 拒绝权限请求回调
     *
     * @param requestCode
     * @param list
     * @param all
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list, boolean all) {

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


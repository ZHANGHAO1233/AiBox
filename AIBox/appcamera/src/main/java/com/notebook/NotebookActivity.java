package com.notebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.box.utils.ILog;
import com.idata.aibox.R;
import com.mgr.serial.comn.SerialPortManager;
import com.utils.DownloadUtil;

import java.io.File;

public class NotebookActivity extends android.app.Activity {
    private BdManager nh;
    private ImageView iv;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    try {
                        File file = (File) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        iv.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        ILog.d("bitmap exception:" + e.getMessage());
                    }
                    break;
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handler = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);
        nh = BdManager.getBd();
        nh.init(handler);
        initView();
    }

    private void initView() {
        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadUtil.get().download("http://ww4.sinaimg.cn/large/610dc034gw1fafmi73pomj20u00u0abr.jpg",
                        Environment.getExternalStorageDirectory().getAbsolutePath(), "test.jpg", new DownloadUtil.OnDownloadListener() {
                            @Override
                            public void onDownloadSuccess(File file, boolean isOpenDoor) {
                                ILog.d("succuss path:" + file.getAbsolutePath());
                                Message m = new Message();
                                m.what = 0;
                                m.obj = file;
                                handler.sendMessage(m);
                            }

                            @Override
                            public void onDownloading(int progress, boolean isOpenDoor) {
                                ILog.d("progress:" + progress);
                            }

                            @Override
                            public void onDownloadFailed(Exception e, boolean isOpenDoor) {
                                ILog.d("exception:" + e.getMessage());
                            }
                        }, true);
            }
        });
        iv = (ImageView) findViewById(R.id.iv);
        findViewById(R.id.download1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nh.testOpen();
            }
        });
        findViewById(R.id.download2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nh.testClose();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BdManager.getBd().closeAllSerials();
    }
}

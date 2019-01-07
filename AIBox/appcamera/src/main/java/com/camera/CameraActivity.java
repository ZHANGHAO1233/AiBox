package com.camera;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.retail.Classifier;
import com.baidu.retail.RetailInputParam;
import com.baidu.retail.RetailVisManager;
import com.box.core.OsModule;
import com.box.utils.ILog;
import com.example.common.DialogInputPasswd;
import com.example.common.UIFactory;
import com.example.funsdkdemo.ActivityDemo;
import com.example.funsdkdemo.devices.ActivityDeviceFishEyeInfo;
import com.example.funsdkdemo.devices.ActivityGuideDevicePictureList;
import com.example.funsdkdemo.devices.ActivityGuideDevicePreview;
import com.example.funsdkdemo.devices.ActivityGuideDeviceRecordList;
import com.example.funsdkdemo.devices.ActivityGuideDeviceSetup;
import com.example.funsdkdemo.devices.ActivityGuideDeviceSportPicList;
import com.idata.aibox.R;
import com.lib.EPTZCMD;
import com.lib.FunSDK;
import com.lib.funsdk.support.FunDevicePassword;
import com.lib.funsdk.support.FunError;
import com.lib.funsdk.support.FunLog;
import com.lib.funsdk.support.FunPath;
import com.lib.funsdk.support.FunSupport;
import com.lib.funsdk.support.OnFunDeviceOptListener;
import com.lib.funsdk.support.config.OPPTZControl;
import com.lib.funsdk.support.config.OPPTZPreset;
import com.lib.funsdk.support.config.SystemInfo;
import com.lib.funsdk.support.models.FunDevType;
import com.lib.funsdk.support.models.FunDevice;
import com.lib.funsdk.support.models.FunStreamType;
import com.lib.funsdk.support.utils.TalkManager;
import com.lib.funsdk.support.widget.FunVideoView;
import com.lib.sdk.struct.H264_DVR_FILE_DATA;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.lib.funsdk.support.models.FunDevType.EE_DEV_SPORTCAMERA;

/**
 * Demo: 监控类设备播放控制等
 *
 * @author Administrator
 */
@SuppressLint("ClickableViewAccessibility")
public class CameraActivity extends ActivityDemo implements OnClickListener, OnFunDeviceOptListener {
    private RelativeLayout mLayoutTop = null;
    private TextView mTextTitle = null;
    private ImageButton mBtnBack = null;
    private ImageButton mBtnSetup = null;
    private FunDevice mFunDevice = null;
    private RelativeLayout mLayoutVideoWnd = null;
    private FunVideoView mFunVideoView = null;
    private LinearLayout mVideoControlLayout = null;
    private TextView mTextStreamType = null;//可以不处理
    private Button mBtnScreenRatio, mBtnScreenRatio1, mBtnScreenRatio2, mBtnScreenRatio3;
    private Button mBtnGetPreset = null;
    private Button mBtnSetPreset = null;
    private View mSplitView = null;
    private RelativeLayout mLayoutRecording = null;
    private LinearLayout mLayoutControls = null;
    private LinearLayout mLayoutChannel = null;
    private RelativeLayout mBtnVoiceTalk = null;
    private Button mBtnVoice = null;
    private ImageButton mBtnQuitVoice = null;
    private ImageButton mBtnDevCapture = null;
    private ImageButton mBtnDevRecord = null;
    private RelativeLayout mLayoutDirectionControl = null;
    private ImageButton mPtz_up = null;
    private ImageButton mPtz_down = null;
    private ImageButton mPtz_left = null;
    private ImageButton mPtz_right = null;
    private TextView mTextVideoStat, mTextVideoStat1, mTextVideoStat2, mTextVideoStat3;
    private List<TextView> statTvs = new ArrayList<>();
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;

    private String preset = null;
    private int mChannelCount;
    private boolean isGetSysFirst = true;
    private DoorStatus doorStatus;

    private final int MESSAGE_PLAY_MEDIA = 0x100;
    private final int MESSAGE_AUTO_HIDE_CONTROL_BAR = 0x102;
    private final int MESSAGE_TOAST_SCREENSHOT_PREVIEW = 0x103;
    private final int MESSAGE_OPEN_VOICE = 0x104;

    // 自动隐藏底部的操作控制按钮栏的时间
    private final int AUTO_HIDE_CONTROL_BAR_DURATION = 10000;
    private TalkManager mTalkManager = null;
    private boolean mCanToPlay = false;
    public String NativeLoginPsw; //本地密码
    // 定义当前支持通过序列号登录的设备类型
    // 如果是设备类型特定的话,固定一个就可以了
    private List<FunVideoView> videoViews = new ArrayList<>();
    private FunVideoView mFunVideoView1, mFunVideoView2, mFunVideoView3;
    private Handler handler;
    private HandlerThread handlerThread;
    private CameraHelper cm;

    public synchronized void captureImage(boolean open) {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < cm.devices.size(); i++) {
            String path = btnCapture(i);
            paths.add(path);
        }

        //做一个延时,需要避免在主线程，新开一个线程
        try {
            Thread.sleep(paths.size() * 200);
        } catch (Exception e) {
            e.printStackTrace();
            ILog.d("delay exception:" + e.getMessage());
        }
        final List<RetailInputParam> params = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {//在这里把图片和摄像头编号对应
            String path = paths.get(i);
            if (!TextUtils.isEmpty(path)) {
                Bitmap bitmap = cm.path2Bitmap(path);
                ILog.d("open?" + open + ":bitmap:" + bitmap + ":path:" + path);
                if (bitmap != null) {
                    RetailInputParam param = new RetailInputParam(bitmap, "just_test");
                    params.add(param);
                }
            }
        }

        if (open) {
            runInBackground(new Runnable() {
                @Override
                public void run() {
                    ILog.d("start to recognize open picture");
                    RetailVisManager.openDoor("just_test", params);
                    List<Classifier.Recognition> recognitions = RetailVisManager.getOpenClassify(0);
                    Toast.makeText(CameraActivity.this, "recognitions open:" + recognitions.size(), Toast.LENGTH_SHORT).show();
                    ILog.d("recognitions open:" + recognitions.size());
                }
            });
        } else {
            runInBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        ILog.d("start to recognize close picture");
                        RetailVisManager.closeDoor("just_test", params);
                        List<Classifier.Recognition> recognitions = RetailVisManager.getOpenClassify(0);
                        Toast.makeText(CameraActivity.this, "recognitions close:" + recognitions.size(), Toast.LENGTH_SHORT).show();
                        ILog.d("recognitions close:" + recognitions.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                        ILog.d("recognize close picture excetion:" + e.getMessage());
                    }
                    ILog.d("close picture recognize end!");
                }
            });
        }

        //保存图片
        cm.saveImages(paths);
    }

    private void initVideoView() {
        for (int i = 0; i < cm.devices.size(); i++) {
            FunDevice device = cm.devices.get(i);
            final FunVideoView view = videoViews.get(i);
            if (device.devType == FunDevType.EE_DEV_LAMP_FISHEYE) {
                // 鱼眼灯泡,设置鱼眼效果
                view.setFishEye(true);
            }

            // 如果支持云台控制，显示方向键和预置点按钮
            if (device.isSupportPTZ()) {
                mSplitView.setVisibility(View.VISIBLE);
                mLayoutDirectionControl.setVisibility(View.VISIBLE);
            }
            view.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    ILog.d("media player on prepared!!!num:" + findVideoViewNum(view));
                }
            });

            view.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 播放失败
                    showToast(getResources().getString(R.string.media_play_error) + " : " + FunError.getErrorStr(extra));
                    ILog.d("onError,error:" + FunError.getErrorStr(extra));
                    if (FunError.EE_TPS_NOT_SUP_MAIN == extra
                            || FunError.EE_DSS_NOT_SUP_MAIN == extra) {
                        // 不支持高清码流,设置为标清码流重新播放
                        if (null != view) {
                            ILog.d("onError,码流由高清切换为标清:");
                            view.setStreamType(FunStreamType.STREAM_SECONDARY);
                            int num = findVideoViewNum(view);
                            playRealMedia(num);
                        }
                    }
                    return true;
                }
            });

            view.setOnInfoListener(new OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {//需要更新
                    int num = findVideoViewNum(view);
                    TextView tvStat = statTvs.get(num);
                    if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                        tvStat.setText(R.string.media_player_buffering);
                        tvStat.setVisibility(View.VISIBLE);
                    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                        tvStat.setVisibility(View.GONE);
                    }
                    return true;
                }
            });
        }
    }

    private int findVideoViewNum(FunVideoView videoView) {
        int num = 0;
        for (int j = 0; j < videoViews.size(); j++) {
            if (videoView == videoViews.get(j)) {
                num = j;
                break;
            }
        }
        return num;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.takephoto_layout);
        cm = new CameraHelper(this);
        cm.initDevice();
        mFunDevice = cm.devices.get(0);
        initView();
        // 允许横竖屏切换
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
//        showVideoControlBar();
        mTalkManager = new TalkManager(mFunDevice);
        mCanToPlay = false;

        cm.startConnect();
//        BdModule.load(this, new BdCallback());
        doorStatus = new DoorStatus();
        OsModule.get().addDoorListener(doorStatus);

        //bd
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        cm.initBdConfig();
    }

    private void initView() {
        mLayoutTop = (RelativeLayout) findViewById(R.id.layoutTop);
        mTextTitle = (TextView) findViewById(R.id.textViewInTopLayout);
        mBtnBack = (ImageButton) findViewById(R.id.backBtnInTopLayout);
        mBtnBack.setOnClickListener(this);
        mLayoutVideoWnd = (RelativeLayout) findViewById(R.id.layoutPlayWnd);
        mBtnScreenRatio = (Button) findViewById(R.id.btnScreenRatio);
        mBtnScreenRatio1 = (Button) findViewById(R.id.btnScreenRatio1);
        mBtnScreenRatio2 = (Button) findViewById(R.id.btnScreenRatio2);
        mBtnScreenRatio3 = (Button) findViewById(R.id.btnScreenRatio3);
        //设置videoview按钮事件
        findViewById(R.id.btnPlay).setOnClickListener(this);
        findViewById(R.id.btnStop).setOnClickListener(this);
        findViewById(R.id.btnStream).setOnClickListener(this);
        findViewById(R.id.btnCapture).setOnClickListener(this);
        findViewById(R.id.btnRecord).setOnClickListener(this);
        findViewById(R.id.btnScreenRatio).setOnClickListener(this);
        findViewById(R.id.btnFishEyeInfo).setOnClickListener(this);
        findViewById(R.id.btnPlay1).setOnClickListener(this);
        findViewById(R.id.btnStop1).setOnClickListener(this);
        findViewById(R.id.btnStream1).setOnClickListener(this);
        findViewById(R.id.btnCapture1).setOnClickListener(this);
        findViewById(R.id.btnRecord1).setOnClickListener(this);
        findViewById(R.id.btnScreenRatio1).setOnClickListener(this);
        findViewById(R.id.btnFishEyeInfo1).setOnClickListener(this);
        findViewById(R.id.btnPlay2).setOnClickListener(this);
        findViewById(R.id.btnStop2).setOnClickListener(this);
        findViewById(R.id.btnStream2).setOnClickListener(this);
        findViewById(R.id.btnCapture2).setOnClickListener(this);
        findViewById(R.id.btnRecord2).setOnClickListener(this);
        findViewById(R.id.btnScreenRatio2).setOnClickListener(this);
        findViewById(R.id.btnFishEyeInfo2).setOnClickListener(this);
        findViewById(R.id.btnPlay3).setOnClickListener(this);
        findViewById(R.id.btnStop3).setOnClickListener(this);
        findViewById(R.id.btnStream3).setOnClickListener(this);
        findViewById(R.id.btnCapture3).setOnClickListener(this);
        findViewById(R.id.btnRecord3).setOnClickListener(this);
        findViewById(R.id.btnScreenRatio3).setOnClickListener(this);
        findViewById(R.id.btnFishEyeInfo3).setOnClickListener(this);

        mLayoutRecording = (RelativeLayout) findViewById(R.id.layout_recording);
        mTextVideoStat = (TextView) findViewById(R.id.textVideoStat);
        mTextVideoStat1 = (TextView) findViewById(R.id.textVideoStat1);
        mTextVideoStat2 = (TextView) findViewById(R.id.textVideoStat2);
        mTextVideoStat3 = (TextView) findViewById(R.id.textVideoStat3);
        statTvs.add(mTextVideoStat);
        statTvs.add(mTextVideoStat1);
        statTvs.add(mTextVideoStat2);
        statTvs.add(mTextVideoStat3);

        mBtnVoiceTalk = (RelativeLayout) findViewById(R.id.btnVoiceTalk);
        mBtnVoice = (Button) findViewById(R.id.Btn_Talk_Switch);
        mBtnQuitVoice = (ImageButton) findViewById(R.id.btn_quit_voice);
        mBtnDevCapture = (ImageButton) findViewById(R.id.btnDevCapture);
        mBtnDevRecord = (ImageButton) findViewById(R.id.btnDevRecord);
        mBtnGetPreset = (Button) findViewById(R.id.btnGetPreset);
        mBtnSetPreset = (Button) findViewById(R.id.btnSetPreset);
        mSplitView = findViewById(R.id.splitView);

        mLayoutDirectionControl = (RelativeLayout) findViewById(R.id.layoutDirectionControl);
        mPtz_up = (ImageButton) findViewById(R.id.ptz_up);
        mPtz_down = (ImageButton) findViewById(R.id.ptz_down);
        mPtz_left = (ImageButton) findViewById(R.id.ptz_left);
        mPtz_right = (ImageButton) findViewById(R.id.ptz_right);
        mBtnVoiceTalk.setOnClickListener(this);
        mBtnVoiceTalk.setOnTouchListener(mIntercomTouchLs);
        mBtnVoice.setOnClickListener(this);
        mBtnQuitVoice.setOnClickListener(this);
        mBtnDevCapture.setOnClickListener(this);
        mBtnDevRecord.setOnClickListener(this);
        mBtnGetPreset.setOnClickListener(this);
        mBtnSetPreset.setOnClickListener(this);

        mPtz_up.setOnTouchListener(onPtz_up);
        mPtz_down.setOnTouchListener(onPtz_down);
        mPtz_left.setOnTouchListener(onPtz_left);
        mPtz_right.setOnTouchListener(onPtz_right);

        mLayoutControls = (LinearLayout) findViewById(R.id.layoutFunctionControl);
        mLayoutChannel = (LinearLayout) findViewById(R.id.layoutChannelBtn);

//        mFunVideoView = (FunVideoView) findViewById(R.id.funVideoView);
        mFunVideoView = (FunVideoView) findViewById(R.id.funVideoView);
        mFunVideoView1 = (FunVideoView) findViewById(R.id.funVideoView1);
        mFunVideoView2 = (FunVideoView) findViewById(R.id.funVideoView2);
        mFunVideoView3 = (FunVideoView) findViewById(R.id.funVideoView3);
        videoViews.add(mFunVideoView);
        videoViews.add(mFunVideoView1);
        videoViews.add(mFunVideoView2);
        videoViews.add(mFunVideoView3);
        initVideoView();

        mVideoControlLayout = (LinearLayout) findViewById(R.id.layoutVideoControl);
        mTextStreamType = (TextView) findViewById(R.id.textStreamStat);

        setNavagateRightButton(R.layout.imagebutton_settings);
        mBtnSetup = (ImageButton) findViewById(R.id.btnSettings);
        mBtnSetup.setOnClickListener(this);

        // 注册设备操作回调
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);
        mTextTitle.setText(mFunDevice.devName);
    }

    @Override
    protected void onDestroy() {
//        stopMedia();
        stopAllMedia();
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        OsModule.get().removeDoorListener(doorStatus);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (mCanToPlay) {
            playRealMedia();
        }
        super.onResume();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    protected void onPause() {
        stopTalk(0);
        CloseVoiceChannel(0);
//        stopMedia();
//		 pauseMedia();
        stopAllMedia();
        //bd
        if (!isFinishing()) {
            finish();
        }
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // 如果当前是横屏，返回时先回到竖屏
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // 检测屏幕的方向：纵向或横向
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            // 当前为横屏， 在此处添加额外的处理代码
            showAsLandscape();
        } else if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            // 当前为竖屏， 在此处添加额外的处理代码
            showAsPortrait();
        }
        super.onConfigurationChanged(newConfig);
    }

    private void playRealMedia(int i) {
        ILog.d("playRealMedia:I:" + i);
        FunDevice device = cm.devices.get(i);
        FunVideoView videoView = videoViews.get(i);

        //需要更新
        // 显示状态: 正在打开视频...
        TextView statTv = statTvs.get(i);
        statTv.setText(R.string.media_player_opening);
        statTv.setVisibility(View.VISIBLE);

        if (device.isRemote) {
            videoView.setRealDevice(device.getDevSn(), device.CurrChannel);
        } else {
            String deviceIp = FunSupport.getInstance().getDeviceWifiManager().getGatewayIp();
            videoView.setRealDevice(deviceIp, device.CurrChannel);
        }

        // 打开声音
        videoView.setMediaSound(true);

        // 设置当前播放的码流类型
        if (FunStreamType.STREAM_SECONDARY == mFunVideoView.getStreamType()) {
            mTextStreamType.setText(R.string.media_stream_secondary);
        } else {
            mTextStreamType.setText(R.string.media_stream_main);
        }
    }

    private void btnPlay(int i) {
        ILog.d("btnPlay,I:" + i);
        videoViews.get(i).stopPlayback();
//        mFunVideoView.stopPlayback();
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_MEDIA + (16 * i), 1000);//乘以16，防止出现等同
    }

    private void btnStop(int i) {
        ILog.d("btnStop,I:" + i);
        videoViews.get(i).stopPlayback();
        videoViews.get(i).stopRecordVideo();
    }

    private void btnStream(int i) {
        ILog.d("btnStream,I:" + i);
        FunVideoView videoView = videoViews.get(i);
        if (null != videoView) {
            if (FunStreamType.STREAM_MAIN == videoView.getStreamType()) {
                videoView.setStreamType(FunStreamType.STREAM_SECONDARY);
            } else {
                videoView.setStreamType(FunStreamType.STREAM_MAIN);
            }

            // 重新播放
            videoView.stopPlayback();
            playRealMedia(i);
        }
    }

    /**
     * 视频截图,并延时一会提示截图对话框
     */
    private String btnCapture(int i) {//需要和摄像头编号对应
        ILog.d("btnCapture,I:" + i);
        FunVideoView videoView = videoViews.get(i);
        if (!videoView.isPlaying()) {
            showToast(R.string.media_capture_failure_need_playing);
            return null;
        }
        final String path = videoView.captureImage(null);    //图片异步保存
        if (!TextUtils.isEmpty(path)) {//预览
            Message message = new Message();
            message.what = MESSAGE_TOAST_SCREENSHOT_PREVIEW;
            message.obj = path;
            mHandler.sendMessageDelayed(message, 200);            //此处延时一定时间等待图片保存完成后显示，也可以在回调成功后显示
        }

        return path;
    }


    private void btnRecord(int i) {
        ILog.d("btnRecord,I:" + i);
        FunVideoView videoView = videoViews.get(i);
        if (!videoView.isPlaying() || videoView.isPaused()) {
            showToast(R.string.media_record_failure_need_playing);
            return;
        }

        if (videoView.bRecord) {
            videoView.stopRecordVideo();
            mLayoutRecording.setVisibility(View.INVISIBLE);//需要修改
            toastRecordSucess(videoView.getFilePath());
        } else {
            videoView.startRecordVideo(null);
            mLayoutRecording.setVisibility(View.VISIBLE);
            showToast(R.string.media_record_start);
        }

    }

    /**
     * 切换视频全屏/小视频窗口(以切横竖屏切换替代)
     */
    private void btnScreenRatio(int i) {
        // 横竖屏切换
        ILog.d("btnScreenRatio,I:" + i);
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void btnFishEyeInfo(int i) {
        ILog.d("btnFishEyeInfo,I:" + i);
        FunVideoView videoView = videoViews.get(i);
        FunDevice device = cm.devices.get(i);
        if (null != videoView) {
            String fishEyeInfo = videoView.getFishEyeFrameJSONString();
            Intent intent = new Intent();
            intent.setClass(this, ActivityDeviceFishEyeInfo.class);
            intent.putExtra("FISH_EYE_INFO", fishEyeInfo);
            intent.putExtra("DEVICE_SN", device.getDevSn());
            this.startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() >= 1000 && v.getId() < 1000 + mChannelCount) {
            mFunDevice.CurrChannel = v.getId() - 1000;
            mFunVideoView.stopPlayback();
            playRealMedia();
        }
        switch (v.getId()) {
            case 1101: {
                startDevicesPreview();
            }
            break;
            case R.id.backBtnInTopLayout: {
                // 返回/退出
                onBackPressed();
            }
            break;

            //开始
            case R.id.btnPlay: // 开始播放
            {
                btnPlay(0);
            }
            break;
            case R.id.btnStop: // 停止播放
            {
                btnStop(0);
//                stopMedia();
            }
            break;
            case R.id.btnStream: // 切换码流
            {
                btnStream(0);
            }
            break;
            case R.id.btnCapture: // 截图
            {
                btnCapture(0);
            }
            break;
            case R.id.btnRecord: // 录像
            {
                captureImage(true);
            }
            break;
            case R.id.btnScreenRatio: // 横竖屏切换
            {
                btnScreenRatio(0);
            }
            break;
            case R.id.btnFishEyeInfo: {// 显示鱼眼信息
                btnFishEyeInfo(0);
            }
            break;

            case R.id.btnPlay1: // 开始播放
            {
                btnPlay(1);
            }
            break;
            case R.id.btnStop1: // 停止播放
            {
                btnStop(1);
            }
            break;
            case R.id.btnStream1: // 切换码流
            {
                btnStream(1);
            }
            break;
            case R.id.btnCapture1: // 截图
            {
                btnCapture(1);
            }
            break;
            case R.id.btnRecord1: // 录像
            {
                captureImage(false);
            }
            break;
            case R.id.btnScreenRatio1: // 横竖屏切换
            {
                btnScreenRatio(1);
            }
            break;
            case R.id.btnFishEyeInfo1: {// 显示鱼眼信息
                btnFishEyeInfo(1);
            }
            break;

            case R.id.btnPlay2: // 开始播放
            {
                btnPlay(2);
            }
            break;
            case R.id.btnStop2: // 停止播放
            {
                btnStop(2);
            }
            break;
            case R.id.btnStream2: // 切换码流
            {
                btnStream(2);
            }
            break;
            case R.id.btnCapture2: // 截图
            {
                btnCapture(2);
            }
            break;
            case R.id.btnRecord2: // 录像
            {
                btnRecord(2);
            }
            break;
            case R.id.btnScreenRatio2: // 横竖屏切换
            {
                btnScreenRatio(2);
            }
            break;
            case R.id.btnFishEyeInfo2: {// 显示鱼眼信息
                btnFishEyeInfo(2);
            }
            break;

            case R.id.btnPlay3: // 开始播放
            {
                btnPlay(3);
            }
            break;
            case R.id.btnStop3: // 停止播放
            {
                btnStop(3);
            }
            break;
            case R.id.btnStream3: // 切换码流
            {
                btnStream(3);
            }
            break;
            case R.id.btnCapture3: // 截图
            {
                btnCapture(3);
            }
            break;
            case R.id.btnRecord3: // 录像
            {
                btnRecord(3);
            }
            break;
            case R.id.btnScreenRatio3: // 横竖屏切换
            {
                btnScreenRatio(3);
            }
            break;
            case R.id.btnFishEyeInfo3: {// 显示鱼眼信息
                btnFishEyeInfo(3);
            }
            break;
//完


            case R.id.btnSettings: // 系统设置/系统信息
            {
                startDeviceSetup();
            }
            break;
            case R.id.Btn_Talk_Switch: {
                OpenVoiceChannel();
            }
            break;
            case R.id.btn_quit_voice: {
                CloseVoiceChannel(500);
            }
            break;
            case R.id.btnDevCapture: // 远程设备图像列表
            {
                startPictureList();
            }
            break;
            case R.id.btnDevRecord: // 远程设备录像列表
            {
                startRecordList();
            }
            break;

            case R.id.btnSetPreset: {
                final EditText editText = new EditText(this);
                int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL;
                editText.setInputType(inputType);
                new AlertDialog.Builder(this).setTitle(R.string.user_input_preset_number)
                        .setView(editText)
                        .setPositiveButton(R.string.common_confirm, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                int i = 0;
                                String preset = editText.getText().toString();
                                if (TextUtils.isEmpty(preset)) {
                                    i = 1;
                                } else {
                                    i = Integer.parseInt(preset);
                                }
                                if (i > 200) {
                                    Toast.makeText(getApplicationContext(), R.string.user_input_preset_number_warn, Toast.LENGTH_SHORT).show();
                                } else {
                                    // 注意：如果是IPC/摇头机,channel = 0, 否则channel=-1，以实际使用设备为准，如果需要兼容，可以两条命令同时发送
                                    OPPTZControl cmd = new OPPTZControl(OPPTZControl.CMD_SET_PRESET, 0, i);
                                    FunSupport.getInstance().requestDeviceCmdGeneral(mFunDevice, cmd);

                                    // for Demo, 为了兼容设备，cmd2和cmd一起发送，两条命令的差别是channel值不同
                                    OPPTZControl cmd2 = new OPPTZControl(OPPTZControl.CMD_SET_PRESET, -1, i);
                                    FunSupport.getInstance().requestDeviceCmdGeneral(mFunDevice, cmd2);
                                }
                            }

                        })
                        .setNegativeButton(R.string.common_cancel, null).show();
            }
            break;
            case R.id.btnGetPreset: {
                OPPTZPreset oPPTZPreset = (OPPTZPreset) mFunDevice.getConfig(OPPTZPreset.CONFIG_NAME);
                if (null != oPPTZPreset) {
                    int[] ids = oPPTZPreset.getIds();
                    int index = 0;
                    preset = null;
                    Arrays.sort(ids);
                    if (ids != null && ids.length > 0) {
                        final String[] idStrs = new String[ids.length];
                        for (int i = 0; i < ids.length; i++) {
                            idStrs[i] = (Integer.toString(ids[i]));
                        }
                        alert = null;
                        builder = new AlertDialog.Builder(this);
                        alert = builder
                                .setTitle(R.string.user_select_preset)
                                .setSingleChoiceItems(idStrs, index, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        preset = idStrs[which];
                                    }
                                })
                                .setPositiveButton(R.string.common_skip, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (TextUtils.isEmpty(preset)) {
                                            preset = idStrs[0];
                                        }
                                        which = Integer.parseInt(preset);
                                        OPPTZControl cmd = new OPPTZControl(OPPTZControl.CMD_GO_TO_PRESET, 0, which);
                                        FunSupport.getInstance().requestDeviceCmdGeneral(mFunDevice, cmd);
                                    }
                                })
                                .setNegativeButton(R.string.common_delete, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (TextUtils.isEmpty(preset)) {
                                            preset = idStrs[0];
                                        }
                                        which = Integer.parseInt(preset);
                                        OPPTZControl cmd = new OPPTZControl(OPPTZControl.CMD_CLEAR_PRESET, 0, which);
                                        FunSupport.getInstance().requestDeviceCmdGeneral(mFunDevice, cmd);
                                    }
                                }).setNeutralButton(R.string.common_correct, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        OPPTZControl cmd = new OPPTZControl(OPPTZControl.CMD_CORRECT, 0, 0);
                                        FunSupport.getInstance().requestDeviceCmdGeneral(mFunDevice, cmd);
                                    }
                                }).create();
                        alert.show();
                    }
                }
            }
            break;

            default:
                break;
        }
    }

    /**
     * 显示截图成功对话框
     *
     * @param path
     */
    private void toastScreenShotPreview(final String path) {
        View view = getLayoutInflater().inflate(R.layout.screenshot_preview, null, false);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_screenshot_preview);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        iv.setImageBitmap(bitmap);
        new AlertDialog.Builder(this)
                .setTitle(R.string.device_socket_capture_preview)
                .setView(view)
                .setPositiveButton(R.string.device_socket_capture_save,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cm.saveImage(path);
                            }
                        })
                .setNegativeButton(R.string.device_socket_capture_delete,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FunPath.deleteFile(path);
                                showToast(R.string.device_socket_capture_delete_success);
                            }
                        })
                .show();
    }

    /**
     * 显示录像成功对话框
     *
     * @param path
     */
    private void toastRecordSucess(final String path) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.device_sport_camera_record_success)
                .setMessage(getString(R.string.media_record_stop) + path)
                .setPositiveButton(R.string.device_sport_camera_record_success_open,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent("android.intent.action.VIEW");
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                String type = "video/*";
                                Uri uri = Uri.fromFile(new File(path));
                                intent.setDataAndType(uri, type);
                                startActivity(intent);
                                FunLog.e("test", "------------startActivity------" + uri.toString());
                            }
                        })
                .setNegativeButton(R.string.device_sport_camera_record_success_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .show();
    }

//    private void showVideoControlBar() {
//        if (mVideoControlLayout.getVisibility() != View.VISIBLE) {
//            TranslateAnimation ani = new TranslateAnimation(0, 0, UIFactory.dip2px(this, 42), 0);
//            ani.setDuration(200);
//            mVideoControlLayout.startAnimation(ani);
//            mVideoControlLayout.setVisibility(View.VISIBLE);
//        }
//
//        if (getResources().getConfiguration().orientation
//                == Configuration.ORIENTATION_LANDSCAPE) {
//            // 横屏情况下,顶部标题栏也动画显示
//            TranslateAnimation ani = new TranslateAnimation(0, 0, -UIFactory.dip2px(this, 48), 0);
//            ani.setDuration(200);
//            mLayoutTop.startAnimation(ani);
//            mLayoutTop.setVisibility(View.VISIBLE);
//        } else {
//            mLayoutTop.setVisibility(View.VISIBLE);
//        }
//
////        // 显示后设置10秒后自动隐藏
////        mHandler.removeMessages(MESSAGE_AUTO_HIDE_CONTROL_BAR);
////        mHandler.sendEmptyMessageDelayed(MESSAGE_AUTO_HIDE_CONTROL_BAR, AUTO_HIDE_CONTROL_BAR_DURATION);
//    }
//
//    private void hideVideoControlBar() {
//        if (mVideoControlLayout.getVisibility() != View.GONE) {
//            TranslateAnimation ani = new TranslateAnimation(0, 0, 0, UIFactory.dip2px(this, 42));
//            ani.setDuration(200);
//            mVideoControlLayout.startAnimation(ani);
//            mVideoControlLayout.setVisibility(View.GONE);
//        }
//
//        if (getResources().getConfiguration().orientation
//                == Configuration.ORIENTATION_LANDSCAPE) {
//            // 横屏情况下,顶部标题栏也隐藏
//            TranslateAnimation ani = new TranslateAnimation(0, 0, 0, -UIFactory.dip2px(this, 48));
//            ani.setDuration(200);
//            mLayoutTop.startAnimation(ani);
//            mLayoutTop.setVisibility(View.GONE);
//        }
//
//        // 隐藏后清空自动隐藏的消息
//        mHandler.removeMessages(MESSAGE_AUTO_HIDE_CONTROL_BAR);
//    }

    private void showAsLandscape() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 隐藏底部的控制按钮区域
        mLayoutControls.setVisibility(View.GONE);
        // 视频窗口全屏显示
        RelativeLayout.LayoutParams lpWnd = (RelativeLayout.LayoutParams) mLayoutVideoWnd.getLayoutParams();
        lpWnd.height = LayoutParams.MATCH_PARENT;
        // lpWnd.removeRule(RelativeLayout.BELOW);
        lpWnd.topMargin = 0;
        mLayoutVideoWnd.setLayoutParams(lpWnd);
        // 上面标题半透明背景
        mLayoutTop.setBackgroundColor(0x40000000);
        mBtnScreenRatio.setText(R.string.device_opt_smallscreen);
    }

    private void showAsPortrait() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 还原上面标题栏背景
        mLayoutTop.setBackgroundColor(getResources().getColor(R.color.theme_color));
        mLayoutTop.setVisibility(View.VISIBLE);
        // 视频显示为小窗口
        RelativeLayout.LayoutParams lpWnd = (RelativeLayout.LayoutParams) mLayoutVideoWnd.getLayoutParams();
        lpWnd.height = UIFactory.dip2px(this, 240);
        lpWnd.topMargin = UIFactory.dip2px(this, 48);
        // lpWnd.addRule(RelativeLayout.BELOW, mLayoutTop.getId());
        mLayoutVideoWnd.setLayoutParams(lpWnd);
        // 显示底部的控制按钮区域
        mLayoutControls.setVisibility(View.VISIBLE);
        mBtnScreenRatio.setText(R.string.device_opt_fullscreen);
    }

    /**
     * 切换视频全屏/小视频窗口(以切横竖屏切换替代)
     */
    private void switchOrientation() {
        // 横竖屏切换
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                && getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * 打开设备配置
     */
    private void startDeviceSetup() {
        Intent intent = new Intent();
        intent.putExtra("FUN_DEVICE_ID", mFunDevice.getId());
        intent.setClass(this, ActivityGuideDeviceSetup.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /***
     * 打开 多通道预览
     */
    private void startDevicesPreview() {
        Intent intent = new Intent();
        intent.putExtra("FUNDEVICE_ID", mFunDevice.getId());
        intent.setClass(this, ActivityGuideDevicePreview.class);
        startActivityForResult(intent, 0);
    }

    private void loginDevice() {
        showWaitDialog();
        FunSupport.getInstance().requestDeviceLogin(mFunDevice);
    }


    private void startPictureList() {
        Intent intent = new Intent();
        intent.putExtra("FUN_DEVICE_ID", mFunDevice.getId());
        intent.putExtra("FILE_TYPE", "jpg");
        if (mFunDevice.devType == EE_DEV_SPORTCAMERA) {
            intent.setClass(this, ActivityGuideDeviceSportPicList.class);
        } else {
            intent.setClass(this, ActivityGuideDevicePictureList.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startRecordList() {
        Intent intent = new Intent();
        intent.putExtra("FUN_DEVICE_ID", mFunDevice.getId());
        intent.putExtra("FILE_TYPE", "h264;mp4");
        intent.setClass(this, ActivityGuideDeviceRecordList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void playRealMedia() {
        // 显示状态: 正在打开视频...
//        mTextVideoStat.setText(R.string.media_player_opening);//单独的关闭这里的控制
//        mTextVideoStat.setVisibility(View.VISIBLE);

        if (mFunDevice.isRemote) {
            mFunVideoView.setRealDevice(mFunDevice.getDevSn(), mFunDevice.CurrChannel);
        } else {
            String deviceIp = FunSupport.getInstance().getDeviceWifiManager().getGatewayIp();
            mFunVideoView.setRealDevice(deviceIp, mFunDevice.CurrChannel);
        }

        // 打开声音
        mFunVideoView.setMediaSound(true);

        // 设置当前播放的码流类型
        if (FunStreamType.STREAM_SECONDARY == mFunVideoView.getStreamType()) {
            mTextStreamType.setText(R.string.media_stream_secondary);
        } else {
            mTextStreamType.setText(R.string.media_stream_main);
        }
    }

    // 添加通道选择按钮
    @SuppressWarnings("ResourceType")
    private void addChannelBtn(int channelCount) {

        int m = UIFactory.dip2px(this, 5);
        int p = UIFactory.dip2px(this, 3);
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams layoutParamsT = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParamsT.setMargins(m, m, m, m);
        textView.setLayoutParams(layoutParamsT);
        textView.setText(R.string.device_opt_channel);
        textView.setTextSize(UIFactory.dip2px(this, 10));
        textView.setTextColor(getResources().getColor(R.color.theme_color));
        mLayoutChannel.addView(textView);

        Button bt = new Button(this);
        bt.setId(1101);
        bt.setTextColor(getResources().getColor(R.color.theme_color));
        bt.setPadding(p, p, p, p);
        bt.setLayoutParams(layoutParamsT);
        bt.setText(R.string.device_camera_channels_preview_title);
        bt.setOnClickListener(this);
        mLayoutChannel.addView(bt);

        for (int i = 0; i < channelCount; i++) {
            Button btn = new Button(this);
            btn.setId(1000 + i);
            btn.setTextColor(getResources().getColor(R.color.theme_color));
            btn.setPadding(p, p, p, p);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(UIFactory.dip2px(this, 40),
                    UIFactory.dip2px(this, 40));
            layoutParams.setMargins(m, m, m, m);
            btn.setLayoutParams(layoutParams);
            btn.setText(String.valueOf(i));
            btn.setOnClickListener(this);
            mLayoutChannel.addView(btn);
        }
    }

    private void stopAllMedia() {
        for (int i = 0; i < videoViews.size(); i++) {
            FunVideoView videoView = videoViews.get(i);
            if (null != videoView) {
                videoView.stopPlayback();
                videoView.stopRecordVideo();
            }
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PLAY_MEDIA: {
                    playRealMedia();
                }
                break;
                case MESSAGE_AUTO_HIDE_CONTROL_BAR: {
//                    hideVideoControlBar();
                }
                break;
                case MESSAGE_TOAST_SCREENSHOT_PREVIEW: {
                    String path = (String) msg.obj;
                    toastScreenShotPreview(path);
                }
                break;
                case MESSAGE_OPEN_VOICE: {
                    mFunVideoView.setMediaSound(true);
                }
                default:
                    break;
            }
        }
    };

    private OnTouchListener mIntercomTouchLs = new OnTouchListener() {

        @Override
        public boolean onTouch(View arg0, MotionEvent arg1) {
            try {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    startTalk();
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    stopTalk(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    };

    private void startTalk() {
        if (mTalkManager != null && mHandler != null && mFunVideoView != null) {
            mTalkManager.onStartThread();
            mTalkManager.setTalkSound(false);
        }
    }

    private void stopTalk(int delayTime) {
        if (mTalkManager != null && mHandler != null && mFunVideoView != null) {
            mTalkManager.onStopThread();
            mTalkManager.setTalkSound(true);
        }
    }

    private void OpenVoiceChannel() {

        if (mBtnVoice.getVisibility() == View.VISIBLE) {
            TranslateAnimation ani = new TranslateAnimation(0, 0, UIFactory.dip2px(this, 100), 0);
            ani.setDuration(200);
            mBtnVoiceTalk.setAnimation(ani);
            mBtnVoiceTalk.setVisibility(View.VISIBLE);
            mBtnVoice.setVisibility(View.GONE);

            mFunVideoView.setMediaSound(false);            //关闭本地音频

            mTalkManager.onStartTalk();
            mTalkManager.setTalkSound(true);
        }
    }

    private void CloseVoiceChannel(int delayTime) {
        if (mBtnVoiceTalk.getVisibility() == View.VISIBLE) {
            TranslateAnimation ani = new TranslateAnimation(0, 0, 0, UIFactory.dip2px(this, 100));
            ani.setDuration(200);
            mBtnVoiceTalk.setAnimation(ani);
            mBtnVoiceTalk.setVisibility(View.GONE);
            mBtnVoice.setVisibility(View.VISIBLE);

            mTalkManager.onStopTalk();
            mHandler.sendEmptyMessageDelayed(MESSAGE_OPEN_VOICE, delayTime);
        }
    }

    /**
     * 显示输入设备密码对话框
     */
    private void showInputPasswordDialog() {
        DialogInputPasswd inputDialog = new DialogInputPasswd(this,
                getResources().getString(R.string.device_login_input_password), "", R.string.common_confirm,
                R.string.common_cancel) {

            @Override
            public boolean confirm(String editText) {
                // 重新以新的密码登录
                if (null != mFunDevice) {
                    NativeLoginPsw = editText;
                    onDeviceSaveNativePws();
                    // 重新登录
                    loginDevice();
                }
                return super.confirm(editText);
            }

            @Override
            public void cancel() {
                super.cancel();

                // 取消输入密码,直接退出
                finish();
            }

        };

        inputDialog.show();
    }

    public void onDeviceSaveNativePws() {
        FunDevicePassword.getInstance().saveDevicePassword(mFunDevice.getDevSn(),
                NativeLoginPsw);
        // 库函数方式本地保存密码
        if (FunSupport.getInstance().getSaveNativePassword()) {
            FunSDK.DevSetLocalPwd(mFunDevice.getDevSn(), "admin", NativeLoginPsw);
            // 如果设置了使用本地保存密码，则将密码保存到本地文件
        }
    }

    @Override
    public void onDeviceLoginSuccess(final FunDevice funDevice) {
        System.out.println("TTT---->>>> loginsuccess");
        int num = cm.deviceMap.get(funDevice);
        ILog.d("onDeviceLoginSuccess,num:" + num);
        if (num >= 0) {
            cm.requestSystemInfo(num);
        }
    }

    @Override
    public void onDeviceLoginFailed(final FunDevice funDevice, final Integer errCode) {
        // 设备登录失败
        ILog.d("onDeviceLoginFailed,i:" + cm.deviceMap.get(funDevice) + ",error:" + FunError.getErrorStr(errCode) + ":sn:" + funDevice.getDevSn());
        hideWaitDialog();
//        showToast(FunError.getErrorStr(errCode));//不然总是弹出来
        // 如果账号密码不正确,那么需要提示用户,输入密码重新登录
        if (errCode == FunError.EE_DVR_PASSWORD_NOT_VALID) {
            showInputPasswordDialog();
        }
        //尝试重新连接
        mHandler.postDelayed(new ReloginRunnable(cm.deviceMap.get(funDevice)), cm.reloginDelay);
    }

    public class ReloginRunnable implements Runnable {
        private int num = 0;

        public ReloginRunnable(int n) {
            num = n;
            ILog.d("ReloginRunnable(int n):" + num);
        }

        @Override
        public void run() {
            ILog.d("restart login,device num:" + num);
            cm.loginDevice(num);
        }
    }

    @Override
    public void onDeviceGetConfigSuccess(final FunDevice funDevice, final String configName, final int nSeq) {
        int channelCount = 0;
        int num = cm.deviceMap.get(funDevice);
        ILog.d("onDeviceGetConfigSuccess,i:" + cm.deviceMap.get(funDevice) + ":isGetSysFirst:" + isGetSysFirst + ":channel:" + funDevice.channel + ":CONFIG_NAME:" + SystemInfo.CONFIG_NAME + ":configName:" + configName);
        if (SystemInfo.CONFIG_NAME.equals(configName)) {
//            if (!isGetSysFirst) {
//                return;
//            }
            //开始，通道处理关闭
//            // 更新UI
//            //此处为示例如何取通道信息，可能会增加打开视频的时间，可根据需求自行修改代码逻辑
//            if (funDevice.channel == null) {
//                FunSupport.getInstance().requestGetDevChnName(funDevice);
//                requestSystemInfo();
//                return;
//            }
//            channelCount = funDevice.channel.nChnCount;
//            // if (channelCount >= 5) {
//            // channelCount = 5;
//            // }
//            if (channelCount > 1) {
//                mChannelCount = channelCount;
//                addChannelBtn(channelCount);
//            }
            //结束

            hideWaitDialog();
            // 设置允许播放标志
            mCanToPlay = true;
            isGetSysFirst = false;
            showToast(cm.getType(funDevice.getNetConnectType()));

            // 获取信息成功后,如果WiFi连接了就自动播放
            // 此处逻辑客户自定义
//			if (MyUtils.detectWifiNetwork(this)) {
            playRealMedia(num);
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    captureImage(true);
//                }
//            }, 3000);
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    captureImage(false);
//                }
//            }, 7000);
            // 如果支持云台控制,在获取到SystemInfo之后,获取预置点信息,如果不需要云台控制/预置点功能功能,可忽略之
            if (funDevice.isSupportPTZ()) {
                cm.requestPTZPreset(num);
            }
        } else if (OPPTZPreset.CONFIG_NAME.equals(configName)) {

        } else if (OPPTZControl.CONFIG_NAME.equals(configName)) {
            Toast.makeText(getApplicationContext(), R.string.user_set_preset_succeed, Toast.LENGTH_SHORT).show();
            // 重新获取预置点列表
//			requestPTZPreset();
        }
    }

    @Override
    public void onDeviceGetConfigFailed(final FunDevice funDevice, final Integer errCode) {
        ILog.d("onDeviceGetConfigFailed,i:" + cm.deviceMap.get(funDevice));
        showToast(FunError.getErrorStr(errCode));
        if (errCode == -11406) {
            funDevice.invalidConfig(OPPTZPreset.CONFIG_NAME);
        }

        int num = cm.findDeviceNum(funDevice);
        FunVideoView videoView = videoViews.get(num);
        if (funDevice.hasLogin()) {
            if (!videoView.isPlaying()) {
                ILog.d("re playRealMedia ,device sn:" + funDevice.getDevSn());
                playRealMedia(num);
            }
        } else {
            ILog.d("re loginDevice,device sn:" + funDevice.getDevSn());
            mHandler.postDelayed(new ReloginRunnable(cm.findDeviceNum(funDevice)), cm.reloginDelay);
//            loginDevice(num);
        }
    }

    @Override
    public void onDeviceSetConfigSuccess(final FunDevice funDevice,
                                         final String configName) {

    }

    @Override
    public void onDeviceSetConfigFailed(final FunDevice funDevice,
                                        final String configName, final Integer errCode) {
        ILog.d("onDeviceSetConfigFailed,i:" + cm.deviceMap.get(funDevice));
        if (OPPTZControl.CONFIG_NAME.equals(configName)) {
            Toast.makeText(getApplicationContext(), R.string.user_set_preset_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private OnTouchListener onPtz_up = new OnTouchListener() {

        // @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent arg1) {
            boolean bstop = true;
            int nPTZCommand = -1;
            // return false;
            switch (arg1.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    Log.i("test", "onPtz_up -- KeyEvent.ACTION_DOWN");
                    bstop = false;
                    nPTZCommand = EPTZCMD.TILT_UP;
                    break;
                case KeyEvent.ACTION_UP:
                    Log.i("test", "onPtz_up -- KeyEvent.ACTION_UP");
                    nPTZCommand = EPTZCMD.TILT_UP;
                    bstop = true;
                    break;
                case KeyEvent.ACTION_MULTIPLE:
                    nPTZCommand = EPTZCMD.TILT_UP;
                    bstop = Math.abs(arg1.getX()) > v.getWidth()
                            || Math.abs(arg1.getY()) > v.getHeight();
                    break;
                default:
                    break;
            }
            onContrlPTZ1(nPTZCommand, bstop);
            return false;
        }
    };
    private OnTouchListener onPtz_down = new OnTouchListener() {

        // @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent arg1) {
            boolean bstop = true;
            int nPTZCommand = -1;
            // return false;
            switch (arg1.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    bstop = false;
                    nPTZCommand = EPTZCMD.TILT_DOWN;
                    break;
                case KeyEvent.ACTION_UP:
                    bstop = true;
                    nPTZCommand = EPTZCMD.TILT_DOWN;
                    onContrlPTZ1(nPTZCommand, bstop);
                    break;
                case KeyEvent.ACTION_MULTIPLE:
                    nPTZCommand = EPTZCMD.TILT_DOWN;
                    bstop = Math.abs(arg1.getX()) > v.getWidth()
                            || Math.abs(arg1.getY()) > v.getHeight();
                    break;
                default:
                    break;
            }
            onContrlPTZ1(nPTZCommand, bstop);
            return false;
        }
    };
    private OnTouchListener onPtz_left = new OnTouchListener() {

        // @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent arg1) {
            boolean bstop = true;
            int nPTZCommand = -1;
            // return false;
            switch (arg1.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    bstop = false;
                    nPTZCommand = EPTZCMD.PAN_LEFT;
                    break;
                case KeyEvent.ACTION_UP:
                    bstop = true;
                    nPTZCommand = EPTZCMD.PAN_LEFT;
                    break;
                case KeyEvent.ACTION_MULTIPLE:
                    nPTZCommand = EPTZCMD.PAN_LEFT;
                    bstop = Math.abs(arg1.getX()) > v.getWidth()
                            || Math.abs(arg1.getY()) > v.getHeight();
                    break;
                default:
                    break;
            }
            onContrlPTZ1(nPTZCommand, bstop);
            return false;
        }
    };
    private OnTouchListener onPtz_right = new OnTouchListener() {

        // @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent arg1) {
            boolean bstop = true;
            int nPTZCommand = -1;
            // return false;
            switch (arg1.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    bstop = false;
                    nPTZCommand = EPTZCMD.PAN_RIGHT;
                    break;
                case KeyEvent.ACTION_UP:
                    bstop = true;
                    nPTZCommand = EPTZCMD.PAN_RIGHT;
                    break;
                case KeyEvent.ACTION_MULTIPLE:
                    nPTZCommand = EPTZCMD.PAN_RIGHT;
                    bstop = Math.abs(arg1.getX()) > v.getWidth()
                            || Math.abs(arg1.getY()) > v.getHeight();
                    break;
                default:
                    break;
            }
            onContrlPTZ1(nPTZCommand, bstop);
            return false;
        }
    };

    private void onContrlPTZ1(int nPTZCommand, boolean bStop) {
        FunSupport.getInstance().requestDevicePTZControl(mFunDevice,
                nPTZCommand, bStop, mFunDevice.CurrChannel);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        mFunDevice.CurrChannel = arg1;
        System.out.println("TTTT----" + mFunDevice.CurrChannel);
        if (mCanToPlay) {
            playRealMedia();
        }
    }

    @Override
    public void onDeviceFileListGetFailed(FunDevice funDevice) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceChangeInfoSuccess(final FunDevice funDevice) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeviceChangeInfoFailed(final FunDevice funDevice, final Integer errCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeviceOptionSuccess(final FunDevice funDevice, final String option) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeviceOptionFailed(final FunDevice funDevice, final String option, final Integer errCode) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice, H264_DVR_FILE_DATA[] datas) {

    }

    private class DoorStatus implements OsModule.OnDoorStatusListener {

        @Override
        public void onDoorClose() {
            captureImage(false);
        }

        @Override
        public void onDoorOpen() {
            captureImage(true);
        }
    }
}

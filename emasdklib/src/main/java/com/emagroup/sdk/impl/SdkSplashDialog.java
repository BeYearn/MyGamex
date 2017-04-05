package com.emagroup.sdk.impl;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.emagroup.sdk.ResourceManager;

import java.util.Timer;
import java.util.TimerTask;

public class SdkSplashDialog extends Dialog {

    private static final String TAG = "SdkSplashDialog";
    private static final int DISMISS_NOW = 11;
    private static final int DISMISS = 10;
    private final String splashName;
    private Activity mActivity;
    private ResourceManager mResourceManager;

    private Timer mTimer;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS:
                    dismissDelay(msg.arg1);
                    break;
                case DISMISS_NOW:
                    SdkSplashDialog.this.dismiss();
                    break;
            }
        }
    };


    public SdkSplashDialog(Context context, String splashName) {
        super(context, ResourceManager.getInstance(context).getIdentifier("ema_dialog", "style"));
        mActivity = (Activity) context;
        this.splashName=splashName;
        mResourceManager = ResourceManager.getInstance(mActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        this.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        int type = mActivity.getResources().getConfiguration().orientation;
        View view = mResourceManager.getLayout("ema_splash");
        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        int drawableId = 0;
        if (type == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "landscape");
            drawableId = mResourceManager.getIdentifier(splashName, "drawable");
        } else if (type == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "portrait");
            drawableId = mResourceManager.getIdentifier(splashName, "drawable");
        }
        ImageView imageView = (ImageView) view.findViewById(mResourceManager.getIdentifier("ema_splash_imageview", "id"));

        imageView.setImageResource(drawableId);
        this.setContentView(view);
    }

    /**
     * 开始显示闪屏，并在3秒后关闭闪屏
     */
    public void start() {
        if (this.isShowing()) {
            return;
        }
        this.show();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(DISMISS_NOW);
            }
        }, 3000);
    }

    /**
     * 延长3000-delta ms后关闭
     *
     * @param delayTime
     */
    public void dismissDelay(int delayTime) {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(DISMISS_NOW);
            }
        }, 3000 - delayTime);
    }

}

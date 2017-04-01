package com.emagroup.sdk.impl;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.emagroup.sdk.ResourceManager;
import com.emagroup.sdk.ToastHelper;

import java.util.HashMap;

import static com.emagroup.sdk.EmaSDK.mActivity;

class YybLoginDialog extends Dialog implements View.OnClickListener {
    private final ResourceManager mResourceManager;
    private HashMap<String, Integer> mIDmap;
    private static YybLoginDialog mInstance;


    public static YybLoginDialog getInstance(Context context){
        if(mInstance==null){
            mInstance=new YybLoginDialog(context);
        }
        return mInstance;
    }


    private YybLoginDialog(Context context) {
        super(context, ResourceManager.getInstance(context).getIdentifier("ema_activity_dialog", "style"));
        mActivity = (Activity) context;
        mResourceManager = ResourceManager.getInstance(mActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏

        this.setCanceledOnTouchOutside(false);

        initView();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        setContentView(mResourceManager.getIdentifier("yyb_login_dialog", "layout"));

        Button mBtnQQ = (Button) findViewById(getId("login_qq_btn"));
        Button mBtnWX = (Button) findViewById(getId("login_wx_btn"));
        mBtnQQ.setOnClickListener(this);
        mBtnWX.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getId("login_qq_btn")) {
            if(!YSDKApi.isPlatformInstalled(ePlatform.QQ)){
                ToastHelper.toast(mActivity,"请安装QQ后再试");
            }
            YSDKApi.login(ePlatform.QQ);
            EmaUtilsImpl.getInstance(mActivity).platform=ePlatform.QQ;
        } else if (id == getId("login_wx_btn")) {
            if(YSDKApi.isPlatformInstalled(ePlatform.WX)){
                YSDKApi.login(ePlatform.WX);
                EmaUtilsImpl.getInstance(mActivity).platform=ePlatform.WX;
            }else {
                ToastHelper.toast(mActivity,"请安装微信后再试");
            }
        }
        this.dismiss();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /**
     * 方法目的（为了防止重复去获取资源ID）
     */
    private int getId(String key) {
        if (mIDmap == null) {
            mIDmap = new HashMap<>();
        }
        if (!mIDmap.containsKey(key)) {
            mIDmap.put(key, mResourceManager.getIdentifier(key, "id"));
        }
        return mIDmap.get(key);
    }
}

package com.emagroup.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKParam;
import com.anysdk.framework.java.AnySDKUser;
import com.anysdk.framework.java.ToolBarPlaceEnum;
import com.igexin.sdk.PushManager;

import java.util.Map;

/**
 * Created by Young on 2016/7/9.
 */
public class EmaSDK {
    private static EmaSDK instance = null;
    public static Activity mActivity = null;
    private EmaSDKListener listener;
    private static EmaSDKListener reciveMsgListener;

    public static EmaSDK getInstance() {
        if (instance == null) {
            instance=new EmaSDK();
        }
        return instance;
    }


    //绑定服务
    public ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
        }
    };

    public void init(String appKey,Activity activity, EmaSDKListener listener) {

        EmaUser.getInstance().setAppkey(appKey);
        this.mActivity = activity;
        this.listener = listener;

        //原来的anysdk初始化放着里面了
        EmaUtils.getInstance(activity).checkSDKStatus(listener);

        //个推初始化
        PushManager.getInstance().initialize(activity.getApplicationContext());
    }


    public void doLogin(){
        //弱账户
        EmaSDKUser.getInstance().creatWeakAccount();  // 在这其中包含any真正的登录（写在里面是想要两个透传参数）

    }

    public void doLogout() {
        AnySDKUser.getInstance().callFunction("logout");
    }

    public void doPay(Map<String,String> info,EmaSDKListener listener){

        //在这里把这个map转化到emapayinfo里面  目前需要 商品pid，数量
        EmaPayInfo emaPayInfo = new EmaPayInfo();

        for (Map.Entry<String,String> entry :info.entrySet()){
            String infoValue=entry.getValue();
            switch (entry.getKey()){
                case EmaConst.EMA_PAYINFO_PRODUCT_ID:
                    emaPayInfo.setProductId(infoValue);
                    break;
                case EmaConst.EMA_PAYINFO_PRODUCT_COUNT:
                    emaPayInfo.setProductNum(infoValue);
                    break;
                case EmaConst.EMA_GAMETRANS_CODE:
                    emaPayInfo.setGameTransCode(infoValue);
                    break;
            }
        }
        EmaPay.getInstance(mActivity).pay(emaPayInfo,listener);
    }

    public void doShowToolbar() {
        AnySDKParam param = new AnySDKParam(ToolBarPlaceEnum.kToolBarTopLeft.getPlace());
        AnySDKUser.getInstance().callFunction("showToolBar", param);
    }


    public void doHideToobar() {
        if (AnySDKUser.getInstance().isFunctionSupported("hideToolBar")) {
            AnySDKUser.getInstance().callFunction("hideToolBar");
        }
    }


    public void doSetRecivePushListner(EmaSDKListener listener){
        this.reciveMsgListener=listener;
    }

    /**
     * 个推的reciver收到透传消息后回调该方法
     * @param msgCode
     * @param msgObj
     */
    public void makeCallBack(int msgCode, String msgObj){
        if(reciveMsgListener == null){
            Log.w("warn", "未设置回调");
            return;
        }
        reciveMsgListener.onCallBack(msgCode,msgObj);
    }


    public String getChannelId(){
        return ULocalUtils.getChannelId(mActivity);
    }


    public void onResume() {
        PluginWrapper.onResume();
    }

    public void onPause() {
        PluginWrapper.onPause();
    }

    public void onStop() {
        PluginWrapper.onStop();
    }

    public void onDestroy(){
        PluginWrapper.onDestroy();
        AnySDK.getInstance().release();
    }

}

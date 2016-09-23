package com.emagroup.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKListener;
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
    private ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
        }
    };

    public void init(final Activity activity, EmaSDKListener listener) {

        this.mActivity = activity;
        ULocalUtils.EmaSdkInfo.readXml("ema_over.xml", activity);
        String appKey = ULocalUtils.EmaSdkInfo.getStringFromMetaData(activity,"appKey");
        String appSecret = ULocalUtils.EmaSdkInfo.getStringFromMetaData(activity,"appSecret");
        String privateKey = ULocalUtils.EmaSdkInfo.getStringFromMetaData(activity,"privateKey");
        String authLoginServer = ULocalUtils.EmaSdkInfo.getStringFromMetaData(activity,"authLoginServer");
        AnySDK.getInstance().init(activity, appKey, appSecret, privateKey, authLoginServer);

        this.listener = listener;
        AnySDKUser.getInstance().setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int i, String s) {
                Log.e("EMASDK",s+"+++++++++++++++++++++++++++++++ "+i);
                if (EmaSDK.this.listener != null) {
                    EmaSDK.this.listener.onCallBack(i, s);

                    //登录成功后
                    if(EmaCallBackConst.LOGINSUCCESS==i){
                        //显示toolbar
                        EmaSDK.getInstance().doShowToolbar();

                        //绑定服务
                        Intent serviceIntent = new Intent(activity, EmaService.class);
                        activity.bindService(serviceIntent, mServiceCon, Context.BIND_AUTO_CREATE);

                        //补充弱账户信息
                        EmaSDKUser.updateWeakAccount(ULocalUtils.getAppKey(activity),ULocalUtils.getAllienceId(),ULocalUtils.getIMEI(activity),EmaUser.getInstance().getmUid());
                    }
                }
            }
        });

        //个推初始化
        PushManager.getInstance().initialize(activity.getApplicationContext());
    }


    public void doLogin(){
        EmaSDKUser.getInstance().login();
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
        return AnySDK.getInstance().getChannelId();
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

package com.example.sdk.emasdk;

import android.app.Activity;
import android.util.Log;

import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKParam;
import com.anysdk.framework.java.AnySDKUser;
import com.anysdk.framework.java.ToolBarPlaceEnum;
import com.example.sdk.emasdk.utils.ULocalUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Young on 2016/7/9.
 */
public class EmaSDK {
    private static EmaSDK instance = null;
    public static Activity mActivity = null;
    private EmaSDKListener listener;

    public static EmaSDK getInstance() {
        if (instance == null) {
            return new EmaSDK();
        }
        return instance;
    }


    public void init(Activity activity,EmaSDKListener listener) {

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
                if (EmaSDK.this.listener != null) {
                    EmaSDK.this.listener.onCallBack(i, s);
                }
            }
        });
    }


    public void doLogin(){
        EmaSDKUser.getInstance().login();
    }

    public void doLogout() {
        AnySDKUser.getInstance().callFunction("logout");
    }

    public void doPay(Map map,EmaSDKListener listener){
        EmaSDKIAP iap = EmaSDKIAP.getInstance();
        ArrayList<String> idArrayList = iap.getPluginId();
        iap.payForProduct(idArrayList.get(0), map,listener);
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

    public  void EmaDebug(String s1, String s2) {
        Log.e(s1, s2);
    }

}

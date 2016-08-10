package com.example.sdk.emasdk;

import android.app.Activity;
import android.util.Log;

import com.anysdk.framework.java.AnySDK;

/**
 * Created by Young on 2016/7/9.
 */
public class EmaSDK {
    private static EmaSDK instance = null;
    public static Activity mActivity = null;

    public static EmaSDK getInstance() {
        if (instance == null) {
            return new EmaSDK();
        }
        return instance;
    }


    public void init(Activity activity) {

        this.mActivity = activity;
        ULocalUtils.EmaSdkInfo.readXml("ema_over.xml", activity);
        String appKey = ULocalUtils.EmaSdkInfo.getStringFromXML("appKey");
        String appSecret = ULocalUtils.EmaSdkInfo.getStringFromXML("appSecret");
        String privateKey = ULocalUtils.EmaSdkInfo.getStringFromXML("privateKey");
        String authLoginServer = ULocalUtils.EmaSdkInfo.getStringFromXML("authLoginServer");
        AnySDK.getInstance().init(activity, appKey, appSecret, privateKey, authLoginServer);
    }


    public void release() {
        AnySDK.getInstance().release();
    }


    public  void EmaDebug(String s1, String s2) {
        Log.e(s1, s2);
    }
}

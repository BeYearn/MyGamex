package com.example.sdk.emasdk;

import android.app.Activity;

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


    public void init(Activity activity, String appKey, String appSecret, String privateKey, String authLoginServer) {
        this.mActivity = activity;
        AnySDK.getInstance().init(activity, appKey, appSecret, privateKey, authLoginServer);
    }


    public void release() {
        AnySDK.getInstance().release();
    }
}

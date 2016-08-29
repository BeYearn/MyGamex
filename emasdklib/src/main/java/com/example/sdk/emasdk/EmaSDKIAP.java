package com.example.sdk.emasdk;

import android.app.Activity;

import com.anysdk.framework.java.AnySDKIAP;
import com.anysdk.framework.java.AnySDKListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/5.
 */
public class EmaSDKIAP {
    private static EmaSDKIAP instance = null;
    private static Activity mActivity = null;
    private static AnySDKIAP anySDKIAP;
    private EmaSDKListener listener;

    public static EmaSDKIAP getInstance() {
        if (instance == null) {
            instance = new EmaSDKIAP();
        }
        anySDKIAP = AnySDKIAP.getInstance();
        mActivity = EmaSDK.mActivity;
        return instance;
    }


    /*public void setListener(EmaSDKListener listener) {
        this.listener = listener;
        anySDKIAP.setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int i, String s) {
                if (EmaSDKIAP.this.listener != null) {
                    EmaSDKIAP.this.listener.onCallBack(i, s);
                }
            }
        });
    }*/

    public ArrayList<String> getPluginId() {
        return anySDKIAP.getPluginId();
    }

    public void payForProduct(String pluginID, Map<String, String> orders,EmaSDKListener listener) {
        /*if(channel==70){

        }*/
        this.listener=listener;
        AnySDKIAP.getInstance().setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int i, String s) {
                if (EmaSDKIAP.this.listener != null) {
                    EmaSDKIAP.this.listener.onCallBack(i, s);
                }
            }
        });
        anySDKIAP.payForProduct(pluginID, orders);
    }
}

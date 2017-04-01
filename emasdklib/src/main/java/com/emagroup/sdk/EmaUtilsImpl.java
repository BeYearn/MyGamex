package com.emagroup.sdk;

import android.app.Activity;

import org.json.JSONObject;

import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface{

    private static EmaUtilsImpl instance;
    private Activity mActivity;


    public static EmaUtilsImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsImpl(activity);
        }
        return instance;
    }

    private EmaUtilsImpl(Activity activity) {
        this.mActivity = activity;
    }

    public void realInit(final EmaSDKListener listener, JSONObject data) {

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {

    }

    /**
     * 用于支付前的一些操作
     *
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {

    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

    }


    public void logout() {

    }

    public void swichAccount() {

    }

    public void doShowToolbar() {
    }

    public void doHideToobar() {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public void onBackPressed(final EmaBackPressedAction action) {

    }

    public void submitGameRole(Map<String, String> data) {

    }

    //-----------------------------------xxx的网络请求方法-------------------------------------------------------------------------




    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String getMhrSignJson() {
        return Url.getServerUrl() + "/ema-platform/extra/mhrCreateOrder";
    }
}

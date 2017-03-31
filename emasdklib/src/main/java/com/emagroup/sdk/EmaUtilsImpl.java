package com.emagroup.sdk;

import android.app.Activity;

import com.emagroup.sdk.sdkcom.EmaBackPressedAction;
import com.emagroup.sdk.sdkcom.EmaPayInfo;
import com.emagroup.sdk.sdkcom.EmaSDKListener;
import com.emagroup.sdk.sdkcom.Url;

import org.json.JSONObject;

import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl {

    private static EmaUtilsImpl instance;

    private Activity mActivity;
    private String mChannelAppId; //uc的gameID
    private EmaPayInfo mPayInfo;

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
     * xiaomi的监听在发起支付时已设置好 此处空实现
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

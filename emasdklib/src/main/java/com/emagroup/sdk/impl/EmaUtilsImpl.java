package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaSDKUser;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;

import org.json.JSONObject;

import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;

    public static final int EMA_LOGIN_REQUEST_CODE = 101;
    private EmaSDKListener mInitLoginListener;

    public static EmaUtilsImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsImpl(activity);
        }
        return instance;
    }

    private EmaUtilsImpl(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void immediateInit(EmaSDKListener listener) {
        this.mInitLoginListener = listener;
    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {

    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        Intent intent = new Intent(mActivity, EmaLoginActivity.class);
        mActivity.startActivityForResult(intent,EMA_LOGIN_REQUEST_CODE);
    }

    /**
     * 用于支付前的一些操作
     *
     * @param listener
     */
    @Override
    public void doPayPre(final EmaSDKListener listener) {

    }

    @Override
    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

    }

    @Override
    public void logout() {

    }

    @Override
    public void swichAccount() {

    }

    @Override
    public void doShowToolbar() {
    }

    @Override
    public void doHideToobar() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onBackPressed(final EmaBackPressedAction action) {

    }

    @Override
    public void submitGameRole(Map<String, String> data) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //从启动的登录页面返回的
        if(resultCode== EmaCallBackConst.LOGINSUCCESS){
            //补充弱账户信息
            EmaSDKUser.getInstance(mActivity).updateWeakAccount(mInitLoginListener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());
            Log.e("googleplay", "loginsusscess");
        }else if(resultCode == EmaCallBackConst.LOGINFALIED){
            mInitLoginListener.onCallBack(EmaCallBackConst.LOGINFALIED, "登录失败");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onRestart() {

    }

    //-----------------------------------xxx的网络请求方法-------------------------------------------------------------------------


    public void gpCallBack(int type,EmaSDKListener listener){

    }



    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String getMhrSignJson() {
        return Url.getServerUrl() + "/ema-platform/extra/mhrCreateOrder";
    }
}

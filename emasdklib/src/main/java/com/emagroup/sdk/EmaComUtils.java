package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

/**
 * Created by Administrator on 2016/12/27.
 */

public class EmaComUtils implements EmaUtilsInterface{

    protected void binderServiceAndUpdateWeakAccount(EmaSDKListener listener,Activity activity) {
        //绑定服务
        Intent serviceIntent = new Intent(activity, EmaService.class);
        activity.bindService(serviceIntent, EmaUtils.getInstance(activity).mServiceCon, Context.BIND_AUTO_CREATE);

        //补充弱账户信息
        EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(activity), ULocalUtils.getChannelId(activity),
                ULocalUtils.getChannelTag(activity), ULocalUtils.getDeviceId(activity), EmaUser.getInstance().getAllianceUid());
    }

    @Override
    public void realInit(EmaSDKListener listener, JSONObject data) {

    }

    @Override
    public void onBackPressed(EmaBackPressedAction action) {

    }

    @Override
    public void realLogin(EmaSDKListener listener) {

    }

    @Override
    public void logout() {

    }

    @Override
    public void swichAccount() {

    }

    @Override
    public void realPay(EmaSDKListener listener, EmaPayInfo emaPayInfo) {

    }

    @Override
    public void doPayPre(EmaSDKListener listener) {

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
    public void onRestart() {

    }

    @Override
    public void onDestroy() {

    }
}

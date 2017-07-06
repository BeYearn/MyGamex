package com.emagroup.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.Map;


/**
 * Created by Young on 2016/7/9.
 */
public class EmaSDK {
    private static EmaSDK instance = null;
    public static Activity mActivity = null;
    private static EmaSDKListener reciveMsgListener;

    public static EmaSDK getInstance() {
        if (instance == null) {
            instance = new EmaSDK();
        }
        return instance;
    }


    public void init(String appKey, Activity activity, EmaSDKListener listener) {

        EmaUser.getInstance().setAppkey(appKey);
        this.mActivity = activity;

        InitCheck.getInstance(activity).initServerUrl();

        EmaUtils.getInstance(activity).immediateInit(listener);

        EmaUtils.getInstance(activity).initBroadcastRevicer(listener);

        //sdk初始化，先sdk初始化，完了再请求公告更新等信息
        InitCheck.getInstance(activity).getChannelKeyInfo(listener);

        //个推初始化
        InitCheck.getInstance(activity).initGeTui(activity);
    }


    public void doLogin() {      // 具体的实现则用EmaUtils  通用的用com包里的
        Log.e("emasdk1.0", "login");
        EmaUtils.getInstance(mActivity).realLogin("", "");
    }

    public void doLogout() {
        EmaUtils.getInstance(mActivity).logout();
        EmaUser.getInstance().clearUserInfo();
    }

    public void doSwichAccount() {
        EmaUtils.getInstance(mActivity).swichAccount();
        EmaUser.getInstance().clearUserInfo();
    }

    public void doPay(Map<String, String> info, EmaSDKListener listener) {

        EmaUtils.getInstance(mActivity).doPayPre(listener);

        //在这里把这个map转化到emapayinfo里面  目前需要 商品pid，数量
        EmaPayInfo emaPayInfo = new EmaPayInfo();

        for (Map.Entry<String, String> entry : info.entrySet()) {
            String infoValue = entry.getValue();
            switch (entry.getKey()) {
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
        EmaPay.getInstance(mActivity).pay(emaPayInfo, listener);
    }

    public void doShowToolbar() {
        EmaUtils.getInstance(mActivity).doShowToolbar();
    }


    public void doHideToobar() {
        EmaUtils.getInstance(mActivity).doHideToobar();
    }


    public void doSetRecivePushListner(EmaSDKListener listener) {
        this.reciveMsgListener = listener;
    }

    /**
     * 个推的reciver收到透传消息后回调该方法
     *
     * @param msgCode
     * @param msgObj
     */
    public void makeCallBack(int msgCode, String msgObj) {
        if (reciveMsgListener == null) {
            Log.w("warn", "未设置回调");
            return;
        }
        reciveMsgListener.onCallBack(msgCode, msgObj);
    }

    public boolean isEma() {
        return !(ULocalUtils.getChannelId(mActivity).length() == 6);
    }

    public String getChannelId() {
        return ULocalUtils.getChannelId(mActivity);
    }

    public String getChannelTag() {
        return ULocalUtils.getChannelTag(mActivity);
    }

    public void doShareImage(final Activity activity, final EmaSDKListener listener, final Bitmap bitmap) {
        ULocalUtils.doShare(activity, listener, bitmap);
    }

    public void doShareText(final Activity activity, final EmaSDKListener listener, final String text) {
        ULocalUtils.doShare(activity, listener, text);
    }

    public void doShareWebPage(final Activity activity, final EmaSDKListener listener, final String url,
                               final String title, final String description, final Bitmap bitmap) {
        ULocalUtils.doShare(activity, listener, url,title,description,bitmap);
    }

    public void onNewIntent(Intent intent) {
        EmaUtils.getInstance(mActivity).onNewIntent(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        EmaUtils.getInstance(mActivity).onActivityResult(requestCode, resultCode, data);
    }


    public void onResume() {
        EmaUtils.getInstance(mActivity).onResume();
    }

    public void onPause() {
        EmaUtils.getInstance(mActivity).onPause();
    }

    public void onStop() {
        EmaUtils.getInstance(mActivity).onStop();
    }

    public void onRestart() {
        EmaUtils.getInstance(mActivity).onRestart();
    }

    public void onDestroy() {
        EmaUtils.getInstance(mActivity).onDestroy();
    }

    /**
     * 统计游戏角色信息
     */
    public void submitGameRole(Map<String, String> data) {
        EmaUtils.getInstance(EmaSDK.mActivity).submitGameRole(data);
    }

    public void onBackPressed(EmaBackPressedAction action) {
        EmaUtils.getInstance(mActivity).onBackPressed(action);
    }
}

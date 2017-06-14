package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaSDKUser;
import com.emagroup.sdk.EmaService;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.oacg.gamesdk.OACGGameSDK;
import com.oacg.gamesdk.login.OnLoginListener;
import com.oacg.gamesdk.pay.OnPayListener;
import com.oacg.gamesdk.pay.PayRequestData;
import com.oacg.oacguaa.cbdata.CbUserInfoData;

import org.json.JSONObject;

import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;
    private EmaSDKListener mILlistener;


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
        this.mILlistener=listener;
    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {

        try {
            String gameId = data.getString("channelAppId");  //"1052"; //从data来
            boolean isLandscape = mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE; //是否是横屏
            OACGGameSDK.getInstance().init(mActivity, gameId, isLandscape);
            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
        }
    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        OACGGameSDK.getInstance().login(mActivity, new OnLoginListener() {
            @Override
            public void onLoginComplete(CbUserInfoData cbUserInfoData) {
                String name = cbUserInfoData.getName();
                String openid = cbUserInfoData.getOpenid();
                EmaUser.getInstance().setNickName(name);
                EmaUser.getInstance().setAllianceUid(openid);

                //绑定服务
                Intent serviceIntent = new Intent(mActivity, EmaService.class);
                mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                //补充弱账户信息
                EmaSDKUser.getInstance(mActivity).updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());
            }

            @Override
            public void onLoginCancel() {
                listener.onCallBack(EmaCallBackConst.LOGINCANELL, "登录取消");
            }

            @Override
            public void onLoginError(String msg) {
                listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
            }
        });
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
        //创建支付订单
        PayRequestData data = new PayRequestData();
        data.setAmount(emaPayInfo.getPrice()); //金额(元) 支付金额不能小于 1， 否则将无法生成订单
        data.setOrderId(emaPayInfo.getOrderId()); //订单号
        data.setSubject(emaPayInfo.getProductName()); //订单名
        data.setDescript(emaPayInfo.getDescription()); //订单简介
        data.setOrderType("充值"); //订单的类型
        //调用 SDK 支付接口
        OACGGameSDK.getInstance().pay(mActivity, data, new OnPayListener() {
            @Override
            public void onPayComplete(String orderid) {
                listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "支付成功");
            }

            @Override
            public void onPayErr(String errorinfo) {
                // call一次取消订单
                EmaPay.getInstance(mActivity).cancelOrder();
                listener.onCallBack(EmaCallBackConst.PAYFALIED, "支付失败");
            }

            @Override
            public void onPayCancel() {
                // call一次取消订单
                EmaPay.getInstance(mActivity).cancelOrder();
                listener.onCallBack(EmaCallBackConst.PAYCANELI, "支付取消");
            }
        });
    }

    @Override
    public void logout() {
        OACGGameSDK.getInstance().logout();
        mILlistener.onCallBack(EmaCallBackConst.LOGOUTSUCCESS,"登出成功");
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
        OACGGameSDK.getInstance().appExit();
    }

    @Override
    public void onBackPressed(final EmaBackPressedAction action) {

    }

    @Override
    public void submitGameRole(Map<String, String> data) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onRestart() {

    }

    //-----------------------------------xxx的网络请求方法-------------------------------------------------------------------------


    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String getMhrSignJson() {
        return Url.getServerUrl() + "/ema-platform/extra/mhrCreateOrder";
    }
}

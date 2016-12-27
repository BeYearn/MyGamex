package com.emagroup.sdk.oppo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

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
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.util.AppUtil;
import com.nearme.platform.opensdk.pay.PayResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/12/13.
 */

public class EmaUtilsOppoImpl implements EmaUtilsInterface {
    private Activity mActivity;
    private static EmaUtilsOppoImpl mInstance;

    public static EmaUtilsOppoImpl getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new EmaUtilsOppoImpl(activity);
        }
        return mInstance;
    }

    public EmaUtilsOppoImpl(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void realInit(EmaSDKListener listener, JSONObject data) {
        try {
            String channelAppSecret = data.getString("channelAppSecret");
            GameCenterSDK.init(channelAppSecret, mActivity);
            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
            //初始化成功之后再检查公告更新等信息
            EmaUtils.getInstance(mActivity).checkSDKStatus();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");

        }

    }

    @Override
    public void onBackPressed(final EmaBackPressedAction action) {
        GameCenterSDK.getInstance().onExit(mActivity,
                new GameExitCallback() {

                    @Override
                    public void exitGame() {
                        // CP 实现游戏退出操作，也可以直接调用
                        // AppUtil工具类里面的实现直接强杀进程~
                        // action.doBackPressedAction();
                        AppUtil.exitGameProcess(mActivity);
                    }
                });

    }

    @Override
    public void realLogin(final EmaSDKListener listener) {
        GameCenterSDK.getInstance().doLogin(mActivity, new ApiCallback() {

            @Override
            public void onSuccess(String resultMsg) {

                GameCenterSDK.getInstance().doGetTokenAndSsoid(new ApiCallback() {

                    @Override
                    public void onSuccess(String resultMsg) {
                        try {
                            JSONObject json = new JSONObject(resultMsg);
                            String ssoid = json.getString("ssoid");
                            EmaUser.getInstance().setAllianceUid(ssoid + "");
                            //绑定服务
                            Intent serviceIntent = new Intent(mActivity, EmaService.class);
                            mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                            //补充弱账户信息
                            EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());

                        } catch (JSONException e) {
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String content, int resultCode) {
                        listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                    }
                });

            }

            @Override
            public void onFailure(String resultMsg, int resultCode) {

            }
        });
    }

    @Override
    public void logout() {

    }

    @Override
    public void swichAccount() {

    }

    @Override
    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {
        // CP 支付参数
        int amount = emaPayInfo.getPrice() * 100; // 支付金额，单位分
        PayInfo payInfo = new PayInfo(emaPayInfo.getOrderId(), EmaUser.getInstance().getAllianceUid(), amount);
        payInfo.setProductDesc(emaPayInfo.getDescription());
        payInfo.setProductName(emaPayInfo.getProductName());
        payInfo.setCallbackUrl(Url.getOppoCallbackUrl());

        GameCenterSDK.getInstance().doPay(mActivity, payInfo, new ApiCallback() {

            @Override
            public void onSuccess(String resultMsg) {
                listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");

            }

            @Override
            public void onFailure(String resultMsg, int resultCode) {
                if (PayResponse.CODE_CANCEL != resultCode) {
                    EmaPay.getInstance(mActivity).cancelOrder();

                    listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                } else {
                    // 取消支付处理
                    EmaPay.getInstance(mActivity).cancelOrder();

                    listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                }
            }
        });
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
          /*  new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                SystemClock.sleep(1000);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                GameCenterSDK.getInstance().onResume(mActivity);
            }
        }.execute();*/
        SystemClock.sleep(1000);
        GameCenterSDK.getInstance().onResume(mActivity);
    }

    @Override
    public void onPause() {
        GameCenterSDK.getInstance().onPause();
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

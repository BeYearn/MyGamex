package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.xiaomi.gamecenter.sdk.GameInfoField;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfoOnline;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

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

    @Override
    public void immediateInit(EmaSDKListener listener) {

    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {
        try {
            String channelAppId = data.getString("channelAppId");
            String channelAppKey = data.getString("channelAppKey");
            MiAppInfo appInfo = new MiAppInfo();
            appInfo.setAppId(channelAppId);
            appInfo.setAppKey(channelAppKey);
            MiCommplatform.Init(mActivity, appInfo);

            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功",null);

        } catch (JSONException e) {
            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITFALIED, "初始化失败",null);
            e.printStackTrace();
        }
    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        //可以通过实现OnLoginProcessListener接口来捕获登录结果
        MiCommplatform.getInstance().miLogin(mActivity, new OnLoginProcessListener() {
            @Override
            public void finishLoginProcess(int i, MiAccountInfo miAccountInfo) {
                Log.e("misdk login:",i+"");
                switch (i) {
                    case MiErrorCode.MI_XIAOMI_GAMECENTER_SUCCESS:
                        // 登陆成功
                        //登录成功回调放在下面updateWeakAccount和docallback成功以后在回调

                        //获取用户的登陆后的 UID(即用户唯一标识)
                        long uid = miAccountInfo.getUid();
                        String nikename = miAccountInfo.getNikename();

                        HashMap<String, String> data = new HashMap<>();
                        data.put(EmaConst.ALLIANCE_UID,uid+"");
                        data.put(EmaConst.NICK_NAME,nikename);

                        EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINSUCCESS_CHANNEL, "渠道登录成功",data);

                        //获取用户的登陆的 Session(请参考 3.3用户session验证接口)
                        String session = miAccountInfo.getSessionId();//若没有登录返回 null
                        //请开发者完成将uid和session提交给开发者自己服务器进行session验证
                        break;
                    case MiErrorCode.MI_XIAOMI_GAMECENTER_ERROR_LOGIN_FAIL:
                        // 登陆失败
                        EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调",null);
                        break;
                    case MiErrorCode.MI_XIAOMI_GAMECENTER_ERROR_CANCEL:
                        // 取消登录
                        EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消回调",null);
                        break;
                    default:
                        // 登录失败
                        EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调",null);
                        break;
                }
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

        MiBuyInfoOnline online = new MiBuyInfoOnline();
        online.setCpOrderId(emaPayInfo.getOrderId()); //订单号唯一(不为空)
        online.setCpUserInfo("cpUserInfo"); //此参数在用户支付成功后会透传给CP的服务器
        online.setMiBi(emaPayInfo.getPrice()); //必须是大于1的整数, 10代表10米币,即10元人民币(不为空)

        Bundle mBundle = new Bundle();
        mBundle.putString(GameInfoField.GAME_USER_BALANCE, "66");  //用户余额
        mBundle.putString(GameInfoField.GAME_USER_GAMER_VIP, "vip0");  //vip 等级
        mBundle.putString(GameInfoField.GAME_USER_LV, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_LEVEL, ""));          //角色等级
        mBundle.putString(GameInfoField.GAME_USER_PARTY_NAME, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ZONE_NAME, ""));  //工会，帮派
        mBundle.putString(GameInfoField.GAME_USER_ROLE_NAME, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_NAME, "")); //角色名称
        mBundle.putString(GameInfoField.GAME_USER_ROLEID, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_ID, ""));   //角色id
        mBundle.putString(GameInfoField.GAME_USER_SERVER_NAME, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ZONE_NAME, ""));  //所在服务器
        MiCommplatform.getInstance().miUniPayOnline(mActivity, online, mBundle,
                new OnPayProcessListener() {
                    @Override
                    public void finishPayProcess(int code) {
                        switch (code) {
                            case MiErrorCode.MI_XIAOMI_GAMECENTER_SUCCESS:
                                // 购买成功
                                listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                                break;
                            case MiErrorCode.MI_XIAOMI_GAMECENTER_ERROR_PAY_CANCEL:
                                // 取消购买
                                //call一次取消订单
                                EmaPay.getInstance(mActivity).cancelOrder();

                                listener.onCallBack(EmaCallBackConst.PAYCANELI, "取消购买");
                                break;
                            case MiErrorCode.MI_XIAOMI_GAMECENTER_ERROR_PAY_FAILURE:
                                // 购买失败
                                //call一次取消订单
                                EmaPay.getInstance(mActivity).cancelOrder();

                                listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                                break;
                            case MiErrorCode.MI_XIAOMI_GAMECENTER_ERROR_ACTION_EXECUTED:
                                //操作正在进行中
                                //统一的emasdk中没有这个回调，不设置了
                                break;
                            default:
                                // 购买失败
                                //call一次取消订单
                                EmaPay.getInstance(mActivity).cancelOrder();

                                listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                                break;
                        }
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
        action.doBackPressedAction();
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

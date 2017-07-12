package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.InitCheck;
import com.emagroup.sdk.ToastHelper;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.hanfeng.guildsdk.Constants;
import com.hanfeng.nsdk.NSdk;
import com.hanfeng.nsdk.NSdkListener;
import com.hanfeng.nsdk.NSdkStatusCode;
import com.hanfeng.nsdk.bean.NSAppInfo;
import com.hanfeng.nsdk.bean.NSLoginResult;
import com.hanfeng.nsdk.bean.NSPayInfo;
import com.hanfeng.nsdk.bean.NSRoleInfo;
import com.hanfeng.nsdk.exception.NSdkException;

import org.json.JSONObject;

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

        //汉风SDK默认情况下是横屏页面.如需要设置为竖屏界面:
        Configuration mConfiguration = mActivity.getResources().getConfiguration(); //获取设置的配置信息
        if (mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Constants.isPORTRAIT = true;
        }
    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                NSAppInfo appinfo = new NSAppInfo();
                appinfo.appId = "10000";
                appinfo.appKey = "424ae8a4fe9a342a1fbb64";
                try {
                    NSdk.getInstance().init(mActivity, appinfo, new NSdkListener<String>() {
                        @Override
                        public void callback(int code, String response) {
                            if (code == NSdkStatusCode.INIT_SUCCESS) {
                                // 初始化成功处理，可按照以下方式处理:
                                listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
                                //初始化成功之后再检查公告更新等信息
                                InitCheck.getInstance(mActivity).checkSDKStatus();
                            } else {
                                listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                            }
                        }
                    });
                } catch (Exception e) {
                    listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        try {
            NSdk.getInstance().login(mActivity, new NSdkListener<NSLoginResult>() {
                @Override
                public void callback(int code, NSLoginResult result) {
                    Log.e("yhlogin", result.msg);
                    switch (code) {
                        case NSdkStatusCode.LOGIN_SUCCESS:
                            // TODO: 获取并处理result.sid
                            doResultHFsid(result.sid);
                            break;
                        case NSdkStatusCode.LOGIN_CANCLE:
                            listener.onCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消");
                            break;
                        case NSdkStatusCode.LOGIN_FAILURE:
                            listener.onCallBack(EmaCallBackConst.LOGOUTFALIED, "登陆失败");
                            break;
                        default:
                            listener.onCallBack(EmaCallBackConst.LOGOUTFALIED, result.msg);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            Log.e("yhlogin", "拉起登录界面出现异常：", e);
            e.printStackTrace();
        }

        //设置切换账号回调侦听
        NSdk.getInstance().setAccountSwitchListener(new NSdkListener<String>() {
            @Override
            public void callback(int i, String s) {
                switch (i) {
                    case NSdkStatusCode.SWITCH_SUCCESS:
                        listener.onCallBack(EmaCallBackConst.LOGOUTSUCCESS, "登出成功");
                        break;
                    case NSdkStatusCode.SWITCH_FAILURE:
                        listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登出失败");
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

        try {
            NSPayInfo payInfo = new NSPayInfo();
            payInfo.gameName = ULocalUtils.getApplicationName(mActivity);
            payInfo.productId = emaPayInfo.getProductId();
            payInfo.productName = emaPayInfo.getProductName();
            payInfo.productDesc = emaPayInfo.getDescription();
            payInfo.price = emaPayInfo.getPrice() / Integer.parseInt(emaPayInfo.getProductNum());    //商品价格 单位分
            payInfo.ratio = 10;                                                                    //比率 瞎填
            payInfo.buyNum = Integer.parseInt(emaPayInfo.getProductNum()); //购买数量
            payInfo.coinNum = 2000;                                                                     //金币数量  瞎填
            payInfo.serverId = (int) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ZONE_ID, "");
            payInfo.uid = EmaUser.getInstance().getAllianceUid();
            payInfo.roleId = (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_ID, "");
            payInfo.roleName = (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_NAME, "");
            payInfo.roleLevel = (int) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_LEVEL, 1);

            NSdk.getInstance().pay(mActivity, payInfo, new NSdkListener<String>() {
                @Override
                public void callback(int code, String response) {
                    Log.e("yhPay", "code=" + code + ", response=" + response);
                    switch (code) {
                        case NSdkStatusCode.PAY_SUCCESS:
                            listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                            break;
                        case NSdkStatusCode.PAY_FAILURE:
                            listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                            break;
                        case NSdkStatusCode.PAY_CANCLE:
                            listener.onCallBack(EmaCallBackConst.PAYCANELI, "购买取消");
                            break;
                        case NSdkStatusCode.PAY_PAYING:
                            // 正在支付，支付结果未知结果处理
                            ToastHelper.toast(mActivity,"正在处理该笔订单");
                            break;
                        case NSdkStatusCode.PAY_NOCALLBACK:
                            // 某些渠道支付无回调情况处理
                            ToastHelper.toast(mActivity,"正在处理该笔订单");
                            break;
                        default:
                            // TODO: 其他支付错误回调结果
                            ToastHelper.toast(mActivity,response);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("yhPay", "支付出现异常：", e);
        }

    }

    @Override
    public void logout() {
        //该接口用于切换账号，会先注销已登录账号并拉起登录页面
        NSdk.getInstance().accountSwitch(mActivity);
    }

    @Override
    public void swichAccount() {

    }

    @Override
    public void doShowToolbar() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NSdk.getInstance().showToolBar(mActivity);
            }
        });
    }

    @Override
    public void doHideToobar() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NSdk.getInstance().hideToolBar(mActivity);
            }
        });
    }

    @Override
    public void onResume() {
        NSdk.getInstance().onResume(mActivity);
    }

    @Override
    public void onPause() {
        NSdk.getInstance().onPause(mActivity);
    }

    @Override
    public void onStop() {
        NSdk.getInstance().onStop(mActivity);
    }

    @Override
    public void onDestroy() {
        NSdk.getInstance().onDestroy(mActivity);
    }

    @Override
    public void onBackPressed(final EmaBackPressedAction action) {
        try {
            NSdk.getInstance().exit(mActivity, new NSdkListener<Void>() {
                @Override
                public void callback(int code, Void response) {
                    if (code == NSdkStatusCode.EXIT_COMFIRM) {
                        //应用注销（非账号注销）
                        NSdk.getInstance().logout(mActivity);
                        System.exit(0);
                    } else {
                        //用户取消登录操作
                    }
                }
            });
        } catch (NSdkException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void submitGameRole(Map<String, String> data) {
        NSRoleInfo roleinfo = new NSRoleInfo();
        roleinfo.roleId = data.get(EmaConst.SUBMIT_ROLE_ID);
        roleinfo.roleName = data.get(EmaConst.SUBMIT_ROLE_NAME);
        roleinfo.roleLevel = data.get(EmaConst.SUBMIT_ROLE_LEVEL);
        roleinfo.zoneId = data.get(EmaConst.SUBMIT_ZONE_ID);
        roleinfo.zoneName = data.get(EmaConst.SUBMIT_ZONE_NAME);
        roleinfo.dataType = data.get(EmaConst.SUBMIT_DATA_TYPE);
        roleinfo.userId= EmaUser.getInstance().getAllianceUid();
        roleinfo.ext = "";

        NSdk.getInstance().submitGameInfo(mActivity, roleinfo);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        NSdk.getInstance().onActivityResult(mActivity, requestCode, resultCode, data);
    }

    @Override
    public void onNewIntent(Intent intent) {
        NSdk.getInstance().onNewIntent(mActivity, intent);
    }

    @Override
    public void onRestart() {
        NSdk.getInstance().onRestart(mActivity);
    }

    //-----------------------------------xxx的网络请求方法-------------------------------------------------------------------------

    /**
     * 获取并处理result.uid
     *
     * @param sid 在游戏会话验证（验证sid）失败时，要调用此接口，不要调用login，防止出现用户修改密码后，一直验证失败的情况。
     */
    private void doResultHFsid(String sid) {

    }

    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String getMhrSignJson() {
        return Url.getServerUrl() + "/ema-platform/extra/mhrCreateOrder";
    }
}

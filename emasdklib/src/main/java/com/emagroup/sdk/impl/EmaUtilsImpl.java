package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.HttpRequestor;
import com.emagroup.sdk.InitCheck;
import com.emagroup.sdk.ThreadUtil;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.uc.gamesdk.UCGameSdk;
import cn.uc.gamesdk.even.SDKEventKey;
import cn.uc.gamesdk.even.SDKEventReceiver;
import cn.uc.gamesdk.even.Subscribe;
import cn.uc.gamesdk.open.GameParamInfo;
import cn.uc.gamesdk.open.OrderInfo;
import cn.uc.gamesdk.open.UCLogLevel;
import cn.uc.gamesdk.open.UCOrientation;
import cn.uc.gamesdk.param.SDKParamKey;
import cn.uc.gamesdk.param.SDKParams;

import static cn.uc.gamesdk.param.SDKParamKey.ACCOUNT_ID;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;
    private String mChannelAppId;
    private SDKEventReceiver mUcReciverIL;
    private SDKEventReceiver mUcReciverPay;


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
        try {

            mChannelAppId = data.getString("channelAppId");

            mUcReciverIL = new SDKEventReceiver() {
                @Subscribe(event = SDKEventKey.ON_INIT_SUCC)
                private void onInitSucc() {

                    listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
                    //初始化成功之后再检查公告更新等信息
                    InitCheck.getInstance(mActivity).checkSDKStatus();

                    //uc特有 用于创建悬浮窗 悬浮按钮须指定一个 Activity 与关联，一个游戏中可以有多个 Activity 拥有悬浮按钮，彼此独立操作
                    //UCGameSdk.defaultSdk().createFloatButton(mActivity);
                }

                @Subscribe(event = SDKEventKey.ON_INIT_FAILED)
                private void onInitFailed() {
                    listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                }

                @Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
                private void onLoginSucc(String sid) {
                    // 登陆成功
                    //登录成功回调放在下面updateWeakAccount和docallback成功以后在回调

                    Log.e("emasdk UCsid", sid);
                    getUCAccontInfo(sid, listener);  // 绑定和补充弱账户在这里面了
                }

                @Subscribe(event = SDKEventKey.ON_LOGIN_FAILED)
                private void onLoginFailed(String desc) {
                    // 登陆失败
                    listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                }

                @Subscribe(event = SDKEventKey.ON_LOGOUT_SUCC)
                private void onLogoutSucc() {
                    listener.onCallBack(EmaCallBackConst.LOGOUTSUCCESS, "登出成功回调");
                    // 切换账号时，可以再次调起登录接口
                }

                @Subscribe(event = SDKEventKey.ON_LOGOUT_FAILED)
                private void onLogoutFailed() {
                    listener.onCallBack(EmaCallBackConst.LOGOUTFALIED, "登出失败回调");
                }

                @Subscribe(event = SDKEventKey.ON_EXIT_SUCC)
                private void onExitSucc() {
                    mActivity.finish();
                    System.exit(0);
                }

                @Subscribe(event = SDKEventKey.ON_EXIT_CANCELED)
                private void onExitCanceled() {
                    //appendText("放弃退出，继续游戏");
                }

            };

            UCGameSdk.defaultSdk().registeSDKEventReceiver(mUcReciverIL);

            GameParamInfo gpi = new GameParamInfo();
            gpi.setGameId(Integer.parseInt(mChannelAppId)); // 从UC九游开放平台获取自己游戏的参数信息
            gpi.setEnablePayHistory(true);//开启查询充值历史功能
            gpi.setEnableUserChange(false);//开启账号切换功能
            gpi.setOrientation(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                    ? UCOrientation.LANDSCAPE : UCOrientation.PORTRAIT);//LANDSCAPE：横屏，横屏游戏必须设置为横屏 PORTRAIT： 竖屏

            SDKParams sdkParams = new SDKParams();
            sdkParams.put(SDKParamKey.LOG_LEVEL, UCLogLevel.DEBUG);
            sdkParams.put(SDKParamKey.DEBUG_MODE, false);   // false则需要真实的了
            sdkParams.put(SDKParamKey.GAME_PARAMS, gpi);
            UCGameSdk.defaultSdk().initSdk(mActivity, sdkParams);


        } catch (Exception e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
            e.printStackTrace();
        }
    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        try {

            UCGameSdk.defaultSdk().login(mActivity, null);

        } catch (Exception e) {//异常处理
            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
            e.printStackTrace();
        }
    }

    /**
     * 用于支付前的一些操作
     *
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {

        mUcReciverPay = new SDKEventReceiver() {

            @Subscribe(event = SDKEventKey.ON_CREATE_ORDER_SUCC)
            private void onPaySucc(OrderInfo orderInfo) {
                if (orderInfo != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("'orderId':'%s'", orderInfo.getOrderId()));
                    sb.append(String.format("'orderAmount':'%s'", orderInfo.getOrderAmount()));
                    sb.append(String.format("'payWay':'%s'", orderInfo.getPayWay()));
                    sb.append(String.format("'payWayName':'%s'", orderInfo.getPayWayName()));
                    Log.i("UC pay", "支付下单成功: callback orderInfo = " + sb);

                    //订单生成生成，非充值成功，充值结果由服务端回调判断,请勿显示充值成功的弹窗或toast
                    if (orderInfo != null) {
                        String ordered = orderInfo.getOrderId();//获取订单号
                        float amount = orderInfo.getOrderAmount();//获取订单金额
                        int payWay = orderInfo.getPayWay();//获取充值类型，具体可参考支付通道编码列表
                        String payWayName = orderInfo.getPayWayName();//充值类型的中文名称
                    }
                    try {
                        Thread.sleep(1000);
                        // 购买成功
                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");  //注掉是因为界面上有提示，而自己并不知道支付状态，所以直接不给了
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Subscribe(event = SDKEventKey.ON_PAY_USER_EXIT)
            private void onPayUserExit(OrderInfo orderInfo) {
                if (orderInfo != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("'orderId':'%s'", orderInfo.getOrderId()));
                    sb.append(String.format("'orderAmount':'%s'", orderInfo.getOrderAmount()));
                    sb.append(String.format("'payWay':'%s'", orderInfo.getPayWay()));
                    sb.append(String.format("'payWayName':'%s'", orderInfo.getPayWayName()));

                    Log.i("UC pay", "支付界面关闭: callback orderInfo = " + sb);
                }
                //listener.onCallBack(EmaCallBackConst.PAYCANELI, "支付取消");
                //EmaPay.getInstance(mActivity).cancelOrder();
            }

        };

        UCGameSdk.defaultSdk().registeSDKEventReceiver(mUcReciverPay);

    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {
        SDKParams sdkParams = new SDKParams();
        //sdkParams.put(SDKParamKey.CALLBACK_INFO, "");
        sdkParams.put(SDKParamKey.SERVER_ID, (String) ULocalUtils.spGet(mActivity, "zoneId_R", ""));
        sdkParams.put(SDKParamKey.ROLE_ID, (String) ULocalUtils.spGet(mActivity, "roleId_R", ""));
        sdkParams.put(SDKParamKey.ROLE_NAME, (String) ULocalUtils.spGet(mActivity, "roleName_R", ""));
        sdkParams.put(SDKParamKey.GRADE, (String) ULocalUtils.spGet(mActivity, "roleLevel_R", ""));
        //sdkParams.put(SDKParamKey.NOTIFY_URL, "");服务器通知地址，如果为空以服务端配置地址作为通知地址，
        sdkParams.put(SDKParamKey.AMOUNT, emaPayInfo.getPrice() + "");
        sdkParams.put(SDKParamKey.CP_ORDER_ID, emaPayInfo.getOrderShortId());
        sdkParams.put(ACCOUNT_ID, EmaUser.getInstance().getAllianceUid());
        sdkParams.put(SDKParamKey.SIGN_TYPE, "MD5");

        String rawSign = "accountId=" + EmaUser.getInstance().getAllianceUid() +
                "amount=" + emaPayInfo.getPrice() +
                "cpOrderId=" + emaPayInfo.getOrderShortId() +
                "grade=" + ULocalUtils.spGet(mActivity, "roleLevel_R", "") +
                "roleId=" + ULocalUtils.spGet(mActivity, "roleId_R", "") +
                "roleName=" + ULocalUtils.spGet(mActivity, "roleName_R", "") +
                "serverId=" + ULocalUtils.spGet(mActivity, "zoneId_R", "");

        Log.e("sign", rawSign);

        signAndPay(rawSign, sdkParams, listener);
    }


    public void logout() {
        try {
            UCGameSdk.defaultSdk().logout(mActivity, null);
        } catch (Exception e) {
            //activity为空，异常处理
        }
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
        UCGameSdk.defaultSdk().unregisterSDKEventReceiver(mUcReciverIL);
        UCGameSdk.defaultSdk().unregisterSDKEventReceiver(mUcReciverPay);

        //UCGameSdk.defaultSdk().destoryFloatButton(mActivity);
    }

    public void onBackPressed(final EmaBackPressedAction action) {
        //action.doBackPressedAction();  uc有自己的逻辑

        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        UCGameSdk.defaultSdk().exit(mActivity, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    /**
     * uc要求的必接的角色信息提交
     */
    public void submitGameRole(Map<String, String> data) {
        try {

            if ("2".equals(data.get("dataType"))) {  // 2 是指角色创建时，就记录下这个时间，以前的就算了，取当前的好了
                ULocalUtils.spPut(mActivity, "uid" + EmaUser.getInstance().getmUid(), System.currentTimeMillis() / 1000 + "");
            }

            //角色登录成功或升级时调用此段，请根据实际业务数据传入真实数据，
            SDKParams params = new SDKParams();
            params.put(SDKParamKey.STRING_ROLE_ID, data.get("roleId"));
            params.put(SDKParamKey.STRING_ROLE_NAME, data.get("roleName"));
            params.put(SDKParamKey.LONG_ROLE_LEVEL, Long.parseLong(data.get("roleLevel")));   // 坑爹！！的long型
            params.put(SDKParamKey.LONG_ROLE_CTIME, getCTime());
            params.put(SDKParamKey.STRING_ZONE_ID, data.get("zoneId"));
            params.put(SDKParamKey.STRING_ZONE_NAME, data.get("zoneName"));
            try {
                UCGameSdk.defaultSdk().submitRoleData(mActivity, params);
                Log.e("submitRoleData", params.toString());
            } catch (Exception e) {
                //传入参数错误异常处理
                Log.e("submitRoleData error", e.toString());
            }

        } catch (Exception e) {
            //处理异常
            e.printStackTrace();
        }
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

    //-----------------------------------uc的网络请求方法-------------------------------------------------------------------------

    public void getUCAccontInfo(final String sid, final EmaSDKListener listener) {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String url = getUCAccontInfo();

                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("gameId", mChannelAppId);
                    paramMap.put("sid", sid);
                    paramMap.put("appId", ULocalUtils.getAppId(mActivity));
                    paramMap.put("channelId", ULocalUtils.getChannelId(mActivity));

                    String result = new HttpRequestor().doPost(url, paramMap);
                    Log.e("emasdkgetUCAccontInfo", result);

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");

                    JSONObject datadata = data.getJSONObject("data");

                    String accountId = datadata.getString("accountId");
                    String nickName = datadata.getString("nickName");

                    EmaUser.getInstance().setAllianceUid(accountId);
                    EmaUser.getInstance().setNickName(nickName);

                    Intent intent = new Intent(EmaConst.EMA_BC_LOGIN_OK_ACTION);
                    mActivity.sendBroadcast(intent);

                    Log.e("getUCAccontInfo", "结果:" + accountId + ".." + nickName);


                } catch (Exception e) {
                    listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                    Log.e("getUCAccontInfo", "maybe is SocketTimeoutException");
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 返回创角时间
     */
    private Long getCTime() {
        String ctime = (String) ULocalUtils.spGet(mActivity, "uid" + EmaUser.getInstance().getmUid(), "");
        if (TextUtils.isEmpty(ctime)) {
            String ct = System.currentTimeMillis() / 1000 + "";
            ULocalUtils.spPut(mActivity, "uid" + EmaUser.getInstance().getmUid(), ct);
            return Long.parseLong(ct);
        } else {
            return Long.parseLong(ctime);
        }
    }

    /**
     * 带上sign并发支付
     */
    private void signAndPay(final String rawSign, final SDKParams sdkParams, final EmaSDKListener listener) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getUCSignAndPay();
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("signSrc", rawSign);
                    paramMap.put("appId", ULocalUtils.getAppId(mActivity));
                    paramMap.put("allianceId", ULocalUtils.getChannelId(mActivity));

                    String result = new HttpRequestor().doPost(url, paramMap);
                    JSONObject jsonObject = new JSONObject(result);
                    String sign = jsonObject.getString("message");

                    sdkParams.put(SDKParamKey.SIGN, sign);

                    UCGameSdk.defaultSdk().pay(mActivity, sdkParams);

                } catch (Exception e) {
                    //listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                    //EmaPay.getInstance(mActivity).cancelOrder();
                }
            }
        });

    }


    //-----------------------------------UC 特有接口---------------------------------------------------
    public String getUCAccontInfo() {
        return Url.getServerUrl() + "/ema-platform/channelLogin/uc";
    }

    public String getUCSignAndPay() {
        return Url.getServerUrl() + "/ema-platform/pay/ucsign";
    }
}

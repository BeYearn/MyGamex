package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaSDKUser;
import com.emagroup.sdk.EmaService;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.HttpRequestor;
import com.emagroup.sdk.InitCheck;
import com.emagroup.sdk.ThreadUtil;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shunwang.bluestack.entity.BaseInfo;
import com.shunwang.bluestack.open.BSCallbackListener;
import com.shunwang.bluestack.open.BSDataPayCallback;
import com.shunwang.bluestack.open.BSEnvironment;
import com.shunwang.bluestack.open.BSGameSDK;
import com.shunwang.bluestack.open.BSGameSdkStatusCode;
import com.shunwang.bluestack.open.BSOrientation;
import com.shunwang.bluestack.open.LoginResult;
import com.shunwang.bluestack.open.OrderResult;
import com.shunwang.bluestack.util.LogUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;
    private boolean isInitSuccess = false;
    private boolean isPaySucc = false;

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
        BSGameSDK.getInstance().initSdk(mActivity, BSOrientation.SENSOR, new
                BSCallbackListener<String>() {
                    @Override
                    public void callback(int code, String t) {
                        LogUtil.i("BSGameSdk初始化接口返回数据 code:" + code + ";msg:" + t);
                        switch (code) {
                            case BSGameSdkStatusCode.FAIL:
                                // 初始化失败，请检查log，定位原因
                                isInitSuccess = false;
                                break;
                            case BSGameSdkStatusCode.SUCCESS:
                                // 初始化成功
                                isInitSuccess = true;
                                break;
                        }
                    }
                }, BSEnvironment.DEVELOPMENT);     //BSEnvironment.ONLINE 正式环境
    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {
        if (isInitSuccess) {
            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
            //初始化成功之后再检查公告更新等信息
            InitCheck.getInstance(mActivity).checkSDKStatus();
        } else {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
        }
    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        BSGameSDK.getInstance().login(mActivity, new BSCallbackListener<LoginResult>() {
            @Override
            public void callback(int code, LoginResult loginResult) {
                LogUtil.e("BSGameSdk登录接口返回数据 code:" + code);
                switch (code) {
                    case BSGameSdkStatusCode.NO_INIT:
                        // 没有初始化或初始化失败，请调用sdk初始化接口
                        break;
                    case BSGameSdkStatusCode.SUCCESS:
                        //登录成功(loginResult可获取guid和accessToken)，请cp自行处理自己的登录逻辑
                        String guid = loginResult.getGuid();
                        EmaUser.getInstance().setAllianceUid(guid);

                        //绑定服务
                        Intent serviceIntent = new Intent(mActivity, EmaService.class);
                        mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                        //补充弱账户信息
                        EmaSDKUser.getInstance(mActivity).updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());

                        break;
                    case BSGameSdkStatusCode.LOGIN_EXIT:
                        // 登录界面关闭，游戏需判断此时是否已登录成功进行相应操作
                        break;
                    default:
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
    public void realPay(final EmaSDKListener listener, final EmaPayInfo emaPayInfo) {
        isPaySucc = false;
        BSGameSDK.getInstance().pay(mActivity, 10, new BSCallbackListener<Object>() {
            @Override
            public void callback(int code, Object object) {
                LogUtil.e("BSGameSdk充值接口返回数据 code:" + code + ";msg:" + object);
                switch (code) {
                    case BSGameSdkStatusCode.NO_INIT:
                        // 没有初始化或初始化失败，请调用sdk初始化接口
                        break;
                    case BSGameSdkStatusCode.NO_LOGIN:
                        // 还没有登录
                        break;
                    case BSGameSdkStatusCode.CONTINUE_PAY_ORDERCREATE:
                        //创建订单
                        createOrderByServer(object, emaPayInfo);
                        break;
                    case BSGameSdkStatusCode.SUCCESS:

                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "支付成功");
                        isPaySucc = true;
                        // 支付成功
                        break;
                    case BSGameSdkStatusCode.PAY_EXIT:
                        // 支付界面退出，此时应该判断是支付成功或失败
                        if (!isPaySucc) {
                            listener.onCallBack(EmaCallBackConst.PAYCANELI, "支付取消");
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void logout() {
        BSGameSDK.getInstance().logout(mActivity, new BSCallbackListener<String>() {
            @Override
            public void callback(int code, String t) {
                LogUtil.e("BSGameSdk注销接口返回数据 code:" + code + ";msg:" + t);
                switch (code) {
                    case BSGameSdkStatusCode.NO_INIT:
                        // 没有初始化或初始化失败，请调用sdk初始化接口
                        break;
                    case BSGameSdkStatusCode.NO_LOGIN:
                        // 还没有登录或者已经注销
                        break;
                    case BSGameSdkStatusCode.SUCCESS:
                        // 注销成功
                        break;
                    case BSGameSdkStatusCode.FAIL:
                        // 注销失败
                        break;
                }
            }
        });
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

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onRestart() {

    }

    //-----------------------------------bluestack 的网络请求方法-------------------------------------------------------------------------


    private void createOrderByServer(Object object, final EmaPayInfo emaPayInfo) {
        final BSDataPayCallback bsData = (BSDataPayCallback) object;

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> params = new HashMap<>();
                params.put("orderId", emaPayInfo.getOrderId());
                params.put("guid", EmaUser.getInstance().getAllianceUid());
                params.put("payType", bsData.getModekey());
                params.put("channelName", bsData.getChannelName());
                params.put("propId", bsData.getCouponId());
                params.put("roleName", (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_NAME, "rolename"));
                params.put("ticketMoney", bsData.getCouponMoney() + "");
                try {
                    String result = new HttpRequestor().doPost(getBSCreatOrder(), params);
                    JSONObject jsonObject = new JSONObject(result);
                    String data = jsonObject.getString("data");
                    BaseInfo<OrderResult> baseInfo = new Gson().fromJson(data, new TypeToken<BaseInfo<OrderResult>>() {
                    }.getType());
                    if (baseInfo.isSuccess()) {
                        OrderResult orderResult = baseInfo.getResult();
                        LogUtil.i("订单创建成功");
                        //异步通知sdk
                        bsData.getListener().onOrderCreated(orderResult);
                    } else {
                        LogUtil.e("订单创建失败,错误码：" + baseInfo.getMsgId() + ",错误信息：" + baseInfo.getMsg());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String getBSCreatOrder() {
        return Url.getServerUrl() + "/ema-platform/extra/blueStacksCreateOrder";
        //return "http://192.168.10.104:8080/ema-platform/extra/blueStacksCreateOrder";
    }
}

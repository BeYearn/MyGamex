package com.emagroup.sdk.mhr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaSDKUser;
import com.emagroup.sdk.EmaService;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.HttpRequestor;
import com.emagroup.sdk.ThreadUtil;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.yingqidm.gamesdk.IPayCallbackListener;
import com.yingqidm.gamesdk.MHRCallbackListener;
import com.yingqidm.gamesdk.MHRGameSDK;
import com.yingqidm.gamesdk.MHRGameSDKStatusCode;
import com.yingqidm.gamesdk.MHROrientation;
import com.yingqidm.gamesdk.info.GameParamInfo;
import com.yingqidm.gamesdk.info.PaymentInfo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsMhrImpl {

    private static EmaUtilsMhrImpl instance;

    private Activity mActivity;
    private String mChannelAppId; //uc的gameID
    private EmaPayInfo mPayInfo;

    public static EmaUtilsMhrImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsMhrImpl(activity);
        }
        return instance;
    }

    private EmaUtilsMhrImpl(Activity activity) {
        this.mActivity = activity;
    }

    public void realInit(final EmaSDKListener listener, JSONObject data) {
        try {

            mChannelAppId = data.getString("channelAppId");


            final GameParamInfo gameParamInfo = new GameParamInfo();
            gameParamInfo.setGameId(mChannelAppId); //"GameId 游戏商接入时漫画人提供的ID"

            MHRGameSDK.defaultSDK().setOrientation(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                    ? MHROrientation.LANDSCAPE : MHROrientation.PORTRAIT);

            MHRGameSDK.defaultSDK().initSDK(mActivity, gameParamInfo, true, new MHRCallbackListener<String>() {//true为是否debug,上线前,记得改为false /为true 方便调试,有日志打印
                @Override
                public void callback(MHRGameSDKStatusCode statusCode, String result) {
                    if (statusCode == MHRGameSDKStatusCode.LOGIN_SUCCESS) {
                        System.out.println("用户登录ID=================" + result);

                        EmaUser.getInstance().setAllianceUid(result);
                        EmaUser.getInstance().setNickName("");

                        //绑定服务
                        Intent serviceIntent = new Intent(mActivity, EmaService.class);
                        mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);
                        //补充弱账户信息
                        EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());
                    }
                }
            });


            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
            //初始化成功之后再检查公告更新等信息
            EmaUtils.getInstance(mActivity).checkSDKStatus();

        } catch (Exception e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
            e.printStackTrace();
        }

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        try {

            MHRGameSDK.defaultSDK().startGuide();

        } catch (Exception e) {//异常处理
            listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
            e.printStackTrace();
        }
    }

    /**
     * xiaomi的监听在发起支付时已设置好 此处空实现
     *
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {

        MHRGameSDK.defaultSDK().setPayCallbackListener(new IPayCallbackListener() {
            @Override
            public void payResult(String buyerId, final String channel, int proxyType) {
                //获取到买家ID,支付渠道,支付类型,调用服务器接口,配置支付json,并签名
                //返回支付json和签名,调用2.8

                realRealPay(buyerId,channel,proxyType);
            }

            @Override
            public void payResponse(MHRGameSDKStatusCode payStatus, String payOrderInfo) {
                String message = "";
                switch (payStatus) {
                    case PAY_SUCCESS:
                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                        break;
                    case PAY_FAIL:
                        listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                        break;
                    case PAY_CANCEL:
                        listener.onCallBack(EmaCallBackConst.PAYCANELI, "购买取消");
                        break;
                    case PAY_SUBMIT_ORDER:
                        listener.onCallBack(EmaCallBackConst.PAYREPEAT, "订单已经提交，支付结果未知");
                        break;
                }
                //(message +"\t\n支付数据:"+payOrderInfo);
            }

        });

    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

        mPayInfo=emaPayInfo;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setGoodsInfo(emaPayInfo.getProductName());//显示的商品名字
        paymentInfo.setSumInfo(Integer.parseInt(emaPayInfo.getProductNum()));//显示商品数量
        MHRGameSDK.defaultSDK().pay(paymentInfo);

    }


    public void logout() {
        try {
        } catch (Exception e) {
            //activity为空，异常处理
        }

    }

    public void swichAccount() {

    }

    public void doShowToolbar() {
        //显示悬浮窗(如果没有,则创建并显示,翻转屏幕会隐藏)
        MHRGameSDK.defaultSDK().showSuspendWindow();
    }

    public void doHideToobar() {
        //隐藏悬浮窗(翻转屏幕会再次显示)
        MHRGameSDK.defaultSDK().hideSuspendWindow();
    }

    public void onResume() {
        //游戏界面被隐藏,onResume 调用
        //MHRGameSDK.defaultSDK().onResumeSuspendWindow();
    }

    public void onPause() {
        //游戏界面被隐藏,onPause 调用
        MHRGameSDK.defaultSDK().onPauseSuspendWindow();
    }

    public void onStop() {
    }

    public void onDestroy() {
        MHRGameSDK.defaultSDK().exitGameSDK();
    }

    public void onBackPressed(final EmaBackPressedAction action) {
        //action.doBackPressedAction();  uc有自己的逻辑

        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        action.doBackPressedAction();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //-----------------------------------mhr的网络请求方法-------------------------------------------------------------------------

    /**
     * 带上sign并发支付
     */
    private void realRealPay(final String buyerId, final String channel, final int proxyType) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = Url.getMhrSignJson();
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("orderId", mPayInfo.getOrderId());
                    paramMap.put("payChannel", channel);
                    paramMap.put("proxyType", proxyType+"");
                    paramMap.put("buyerId",buyerId);

                    String result = new HttpRequestor().doPost(url, paramMap);
                    JSONObject jsonObject = new JSONObject(result);
                    if(jsonObject.getInt("status")==0){


                        JSONObject data = jsonObject.getJSONObject("data");
                        final String json = data.getString("json");
                        final String sign = data.getString("sign");

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //获取到支付json 和签名信息后调用支付
                                MHRGameSDK.defaultSDK().orderInfoSuccess(channel,sign, json);
                            }
                        });

                    }else {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //获取到支付json 和签名信息失败后调用
                                MHRGameSDK.defaultSDK().orderInfoFailure("调用支付失败");
                            }
                        });
                    }
                } catch (Exception e) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //获取到支付json 和签名信息失败后调用
                            MHRGameSDK.defaultSDK().orderInfoFailure("调用支付失败");
                        }
                    });
                    e.printStackTrace();
                }
            }
        });

    }

    public void submitGameRole(Map<String, String> data) {

    }
}

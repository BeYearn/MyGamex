package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

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
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;
    private String mChannelAppId;
    private EmaPayInfo mPayInfo;


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


            final GameParamInfo gameParamInfo = new GameParamInfo();
            gameParamInfo.setGameId(mChannelAppId); //"GameId 游戏商接入时漫画人提供的ID"

            MHRGameSDK.defaultSDK().setOrientation(mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                    ? MHROrientation.LANDSCAPE : MHROrientation.PORTRAIT);

            MHRGameSDK.defaultSDK().initSDK(mActivity, gameParamInfo, false, new MHRCallbackListener<String>() {//true为是否debug,上线前,记得改为false /为true 方便调试,有日志打印
                @Override
                public void callback(MHRGameSDKStatusCode statusCode, String result) {
                    if (statusCode == MHRGameSDKStatusCode.LOGIN_SUCCESS) {
                        System.out.println("用户登录ID=================" + result);

                        EmaUser.getInstance().setAllianceUid(result);
                        EmaUser.getInstance().setNickName("");

                        Intent intent = new Intent(EmaConst.EMA_BC_LOGIN_OK_ACTION);
                        mActivity.sendBroadcast(intent);

                    }
                }
            });


            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
            //初始化成功之后再检查公告更新等信息
            InitCheck.getInstance(mActivity).checkSDKStatus();

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
     * 用于支付前的一些操作
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

    public void submitGameRole(Map<String, String> data) {

    }

    //-----------------------------------xxx的网络请求方法-------------------------------------------------------------------------

    /**
     * 带上sign并发支付
     */
    private void realRealPay(final String buyerId, final String channel, final int proxyType) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getMhrSignJson();
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


    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String getMhrSignJson(){
        return Url.getServerUrl()+"/ema-platform/extra/mhrCreateOrder";
    }
}

package com.emagroup.sdk.coolpad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.coolcloud.uac.android.api.Coolcloud;
import com.coolcloud.uac.android.api.ErrInfo;
import com.coolcloud.uac.android.api.OnResultListener;
import com.coolcloud.uac.android.api.ResultFuture;
import com.coolcloud.uac.android.common.Constants;
import com.coolcloud.uac.android.common.Params;
import com.coolcloud.uac.android.gameassistplug.GameAssistApi;
import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPay;
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
import com.yulong.paysdk.beens.CoolPayResult;
import com.yulong.paysdk.beens.CoolYunAccessInfo;
import com.yulong.paysdk.beens.PayInfo;
import com.yulong.paysdk.coolpayapi.CoolpayApi;
import com.yulong.paysdk.payinterface.IPayResult;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.os.Looper.getMainLooper;

/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsCoolPadImpl {

    private static EmaUtilsCoolPadImpl instance;

    private Activity mActivity;
    private Coolcloud mCoolcloud;
    private String mChannelAppId;
    private CoolpayApi mCoolPay;
    private IPayResult mPayResult;
    private String mChannelAppKey;
    private String mAccessToken;
    private String mExpires_in;
    private String mOpenId;
    private String mRefresh_token;
    private String mChannelPayKey;
    private GameAssistApi mGameAssistApi;
    private EmaSDKListener mListenerLogin;

    public static EmaUtilsCoolPadImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsCoolPadImpl(activity);
        }
        return instance;
    }

    private EmaUtilsCoolPadImpl(Activity activity) {
        this.mActivity = activity;
    }

    public void realInit(EmaSDKListener listener, JSONObject data) {
        try {
            //酷派的 appid 5000005536
            mChannelAppId = data.getString("channelAppId");
            Log.e("cpid",mChannelAppId);
            mChannelAppKey = data.getString("channelAppKey");
            mChannelPayKey = data.getString("channelAppSecret");

            mCoolcloud = Coolcloud.get(mActivity, mChannelAppId);  //账户用api

            if (mGameAssistApi == null) {                        //悬浮窗
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mGameAssistApi = (GameAssistApi) mCoolcloud.getGameAssistApi(mActivity);
                        mGameAssistApi.addOnSwitchingAccountListen(new GameAssistApi.SwitchingAccount() {
                            @Override
                            public void onSwitchingAccounts() {
                                swichAccount();
                            }
                        });
                    }
                });
            }


            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");

            //初始化成功之后再检查公告更新等信息
            EmaUtils.getInstance(mActivity).checkSDKStatus();

        } catch (Exception e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
            e.printStackTrace();
        }

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        mListenerLogin=listener;

        Bundle input = new Bundle();
        // 设置横屏显示
        input.putInt(Constants.KEY_SCREEN_ORIENTATION, mActivity.getResources().getConfiguration().orientation);
        // 设置申请的接口列表
        input.putString(Constants.KEY_SCOPE, "get_basic_userinfo");
        //获取类型为AuthCode
        input.putString(Constants.KEY_RESPONSE_TYPE, Constants.RESPONSE_TYPE_CODE);
        // 调用登录并授权接口，这里使用回调接口的方式
        ResultFuture<Bundle> future = mCoolcloud.login(mActivity, input,
                new Handler(getMainLooper()), new OnResultListener() {
                    @Override
                    public void onResult(Bundle result) {
                        // 返回成功，获取授权码
                        String code = result.getString(Params.KEY_AUTHCODE);
                        Log.e("coolPadloginCode",code);
                        getCoolPAccountInfo(code,listener);
                    }

                    @Override
                    public void onError(ErrInfo error) {
                        // 出现错误，通过error.getError()和error.getMessage()获取错误信息
                        Log.e("coolpLoginerror",error.getError()+".."+error.getMessage());
                        // 登陆失败
                        listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                    }

                    @Override
                    public void onCancel() {
                        // 操作被取消
                        // 取消登录
                        listener.onCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消回调");
                    }
                });

    }

    /**
     *
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {
        mCoolPay = CoolpayApi.createCoolpayApi(mActivity,mChannelAppId);

        // 支付结果回调示例
         mPayResult = new IPayResult() {
            @Override
            public void onResult(CoolPayResult result) {
                if (null != result) {
                    String resultStr = result.getResult();
                    int resultStatus = result.getResultStatus();
                    Log.d("coolPadPay", "resultStr:" + resultStr+ "ResultStatus:" + resultStatus);
                    try {
                        if(0==resultStatus){    // 支付成功
                            // 购买成功
                            listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                        }else if(-1==resultStatus){   // z支付失败
                            // 购买失败
                            //call一次取消订单
                            EmaPay.getInstance(mActivity).cancelOrder();

                            listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                        }else if(-2==resultStatus){   // 支付取消
                            // 取消购买
                            //call一次取消订单
                            EmaPay.getInstance(mActivity).cancelOrder();

                            listener.onCallBack(EmaCallBackConst.PAYCANELI, "取消购买");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

        PayInfo payInfo = new PayInfo();
        payInfo.setPrice(emaPayInfo.getPrice()); // 支付价格 单位为 分
        payInfo.setAppId(mChannelAppId);
        payInfo.setPayKey(mChannelPayKey);
        payInfo.setName(emaPayInfo.getProductName());
        payInfo.setPoint(Integer.parseInt(emaPayInfo.getProductId()));
        payInfo.setQuantity(1);
        payInfo.setCpOrder(emaPayInfo.getOrderId());
        payInfo.setCpPrivate(mOpenId);   // 透传信息

        //将获取的酷云账号信息传递给支付SDK
        CoolYunAccessInfo accessInfo = new CoolYunAccessInfo();
        accessInfo.setAccessToken(mAccessToken);
        accessInfo.setExpiresIn(mExpires_in);
        accessInfo.setOpenId(mOpenId);
        accessInfo.setRefreshToken(mRefresh_token);


        mCoolPay.startPay(mActivity, payInfo, accessInfo, mPayResult, CoolpayApi.PAY_STYLE_DIALOG, mActivity.getResources().getConfiguration().orientation);

    }


    public void logout() {
        Log.e("coolpad","logout");
        mCoolcloud.logout(mActivity);
    }

    public void swichAccount() {
        Log.e("coolpad","swichAccount");
        logout();
        mListenerLogin.onCallBack(EmaCallBackConst.LOGOUTSUCCESS,"登出成功");
        /*Bundle input = new Bundle();
        // 设置屏幕横竖屏默认为竖屏
        input.putInt(Constants.KEY_SCREEN_ORIENTATION, mActivity.getResources().getConfiguration().orientation);
        // 设置需要权限 一般都为get_basic_userinfo这个常量
        input.putString(Constants.KEY_SCOPE, "get_basic_userinfo");
        //获取类型为AuthCode
        input.putString(Constants.KEY_RESPONSE_TYPE, Constants.RESPONSE_TYPE_CODE);

        mCoolcloud.loginNew(mActivity, input, new Handler(), new OnResultListener() {
            @Override
            public void onResult(Bundle result) {
                // 返回成功，获取授权码
                String code = result.getString(Params.KEY_AUTHCODE);
                Log.e("coolPadloginCode",code);
                getCoolPAccountInfo(code,mListenerLogin);
            }
            @Override
            public void onError(ErrInfo error) {
                // 出现错误，通过error.getError()和error.getMessage()获取错误信息
                Log.e("coolpLoginerror",error.getError()+".."+error.getMessage());
                // 登陆失败
                mListenerLogin.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
            }

            @Override
            public void onCancel() {
                // 操作被取消
                // 取消登录
                mListenerLogin.onCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消回调");
            }
        });*/
    }

    public void doShowToolbar() {
    }

    public void doHideToobar() {
    }

    public void onResume() {
        if (mGameAssistApi != null) {
            mGameAssistApi.onResume();
        }
    }

    public void onPause() {
        if (mGameAssistApi != null) {
            mGameAssistApi.onPause();
        }
    }

    public void onStop() {
    }

    public void onDestroy() {
    }

    public void onBackPressed(EmaBackPressedAction action) {
        action.doBackPressedAction();
    }



    //------------------------------------coolpad的方法--------------------------------------------------------------------------------


    /**
     * 根据得到的sid来获取token和id
     * @param code
     * @param listener
     */
    private void getCoolPAccountInfo(final String code, final EmaSDKListener listener) {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    openProgressDialog();

                    String url = Url.getCoolPadAccontInfo();

                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("authCode", code);
                    paramMap.put("appId", ULocalUtils.getAppId(mActivity));
                    paramMap.put("channelId", ULocalUtils.getChannelId(mActivity));

                    String result = new HttpRequestor().doPost(url, paramMap);
                    Log.e("getCoolPadAccountInfo",result);

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");

                    //授权令牌
                    mAccessToken = data.getString("access_token");
                    //该access token的有效期，单位为秒。默认2592000秒（30天）
                    mExpires_in = data.getString("expires_in");
                    //用户唯一标识
                    mOpenId = data.getString("openid");
                    //在授权自动续期步骤中，获取新的Access_Token时需要提供的参数。
                    mRefresh_token = data.getString("refresh_token");

                    EmaUser.getInstance().setAllianceUid(mOpenId);
                    //EmaUser.getInstance().setNickName(nickName);

                    //绑定服务
                    Intent serviceIntent = new Intent(mActivity, EmaService.class);
                    mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);
                    //补充弱账户信息
                    EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());

                    Log.e("getCoolPadAccontInfo", "结果:" + mOpenId + "..");

                } catch (Exception e) {
                    listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                    Log.e("getUCAccontInfo", "maybe is SocketTimeoutException");
                    e.printStackTrace();

                    closeProgressDialog();
                }

            }
        });
    }

    private void openProgressDialog(){
        Intent intent = new Intent(EmaConst.EMA_BC_PROGRESS_ACTION);
        intent.putExtra(EmaConst.EMA_BC_PROGRESS_STATE,EmaConst.EMA_BC_PROGRESS_START);
        mActivity.sendBroadcast(intent);
    }

    private void closeProgressDialog(){
        Intent intent = new Intent(EmaConst.EMA_BC_PROGRESS_ACTION);
        intent.putExtra(EmaConst.EMA_BC_PROGRESS_STATE,EmaConst.EMA_BC_PROGRESS_CLOSE);
        mActivity.sendBroadcast(intent);
    }
}

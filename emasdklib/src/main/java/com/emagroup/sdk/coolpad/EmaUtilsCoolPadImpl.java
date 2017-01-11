package com.emagroup.sdk.coolpad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.coolcloud.uac.android.api.Coolcloud;
import com.coolcloud.uac.android.api.ErrInfo;
import com.coolcloud.uac.android.api.OnResultListener;
import com.coolcloud.uac.android.api.ResultFuture;
import com.coolcloud.uac.android.common.Constants;
import com.coolcloud.uac.android.common.Params;
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
import com.yulong.paysdk.beens.CoolPayResult;
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

            mCoolcloud = Coolcloud.get(mActivity, mChannelAppId);  //账户用api

            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");

            //初始化成功之后再检查公告更新等信息
            EmaUtils.getInstance(mActivity).checkSDKStatus();

        } catch (Exception e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
            e.printStackTrace();
        }

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {

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


       /* //可以通过实现OnLoginProcessListener接口来捕获登录结果
        MiCommplatform.getInstance().miLogin(mActivity, new OnLoginProcessListener() {
            @Override
            public void finishLoginProcess(int i, MiAccountInfo miAccountInfo) {
                switch (i) {
                    case MiErrorCode.MI_XIAOMI_GAMECENTER_SUCCESS:
                        // 登陆成功
                        //登录成功回调放在下面updateWeakAccount和docallback成功以后在回调

                        //获取用户的登陆后的 UID(即用户唯一标识)
                        long uid = miAccountInfo.getUid();
                        String nikename = miAccountInfo.getNikename();
                        EmaUser.getInstance().setAllianceUid(uid + "");
                        EmaUser.getInstance().setNickName(nikename);

                        //获取用户的登陆的 Session(请参考 3.3用户session验证接口)
                        String session = miAccountInfo.getSessionId();//若没有登录返回 null
                        //请开发者完成将uid和session提交给开发者自己服务器进行session验证

                        //绑定服务
                        Intent serviceIntent = new Intent(mActivity, EmaService.class);
                        mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                        //补充弱账户信息
                        EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());

                        break;


                }
            }
        });*/
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
                    Log.d("ss77", "resultStr:" + resultStr);
                    Log.d("swx", "ResultStatus:" + result.getResultStatus());
                    try {


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
        payInfo.setPayKey(mChannelAppKey);
        payInfo.setName(emaPayInfo.getProductName());
        payInfo.setPoint(Integer.parseInt(emaPayInfo.getProductId()));
        payInfo.setQuantity(1);
        payInfo.setCpOrder(emaPayInfo.getOrderId());

        //mCoolPay.startPay(mActivity, payInfo, accessInfo, mPayResult, CoolpayApi.PAY_STYLE_DIALOG, mActivity.getResources().getConfiguration().orientation);












        /*MiBuyInfoOnline online = new MiBuyInfoOnline();
        online.setCpOrderId(emaPayInfo.getOrderId()); //订单号唯一(不为空)
        online.setCpUserInfo("cpUserInfo"); //此参数在用户支付成功后会透传给CP的服务器
        online.setMiBi(emaPayInfo.getPrice()); //必须是大于1的整数, 10代表10米币,即10元人民币(不为空)

        Bundle mBundle = new Bundle();
        mBundle.putString(GameInfoField.GAME_USER_BALANCE, "1000");  //用户余额
        mBundle.putString(GameInfoField.GAME_USER_GAMER_VIP, "vip0");  //vip 等级
        mBundle.putString(GameInfoField.GAME_USER_LV, "20");          //角色等级
        mBundle.putString(GameInfoField.GAME_USER_PARTY_NAME, "猎人");  //工会，帮派
        mBundle.putString(GameInfoField.GAME_USER_ROLE_NAME, "meteor"); //角色名称
        mBundle.putString(GameInfoField.GAME_USER_ROLEID, "123456");   //角色id
        mBundle.putString(GameInfoField.GAME_USER_SERVER_NAME, "峡谷");  //所在服务器
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
                });*/
    }


    public void logout() {
        Log.e("coolpad","logout");
        mCoolcloud.logout(mActivity);
    }

    public void swichAccount() {
        Bundle input = new Bundle();
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
            }
            @Override
            public void onError(ErrInfo s) {
                // 登录失败
                Toast.makeText(mActivity, s.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                // 登录被取消
                Toast.makeText(mActivity, "login cancel...", Toast.LENGTH_SHORT).show();
            }
        });
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
                    //耗时操作 阻塞
                    String url = Url.getCoolPadAccontInfo();

                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("authCode", code);
                    paramMap.put("appId", ULocalUtils.getAppId(mActivity));
                    paramMap.put("channelId", ULocalUtils.getChannelId(mActivity));

                    String result = new HttpRequestor().doPost(url, paramMap);
                    Log.e("getCoolPadAccountInfo",result);

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");

                    JSONObject datadata = data.getJSONObject("data");

                    String accountId = datadata.getString("accountId");
                    String nickName = datadata.getString("nickName");

                    EmaUser.getInstance().setAllianceUid(accountId);
                    EmaUser.getInstance().setNickName(nickName);

                    //绑定服务
                    Intent serviceIntent = new Intent(mActivity, EmaService.class);
                    mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);
                    //补充弱账户信息
                    EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());

                    Log.e("getUCAccontInfo", "结果:" + accountId + ".." + nickName);

                } catch (Exception e) {
                    listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                    Log.e("getUCAccontInfo", "maybe is SocketTimeoutException");
                    e.printStackTrace();
                }

            }
        });
    }


}

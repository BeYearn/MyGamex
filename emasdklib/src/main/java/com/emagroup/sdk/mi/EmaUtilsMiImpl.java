package com.emagroup.sdk.mi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaSDKUser;
import com.emagroup.sdk.EmaService;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.ULocalUtils;
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

/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsMiImpl {

    private static EmaUtilsMiImpl instance;

    private Activity mActivity;

    public static EmaUtilsMiImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsMiImpl(activity);
        }
        return instance;
    }

    private EmaUtilsMiImpl(Activity activity) {
        this.mActivity = activity;
    }

    public void realInit(EmaSDKListener listener, JSONObject data) {
        try {
            String channelAppId = data.getString("channelAppId");
            String channelAppKey = data.getString("channelAppKey");
            MiAppInfo appInfo = new MiAppInfo();
            appInfo.setAppId(channelAppId);
            appInfo.setAppKey(channelAppKey);
            MiCommplatform.Init(mActivity, appInfo);

            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
            //初始化成功之后再检查公告更新等信息
            EmaUtils.getInstance(mActivity).checkSDKStatus();

        } catch (JSONException e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
            e.printStackTrace();
        }

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        //可以通过实现OnLoginProcessListener接口来捕获登录结果
        MiCommplatform.getInstance().miLogin(mActivity, new OnLoginProcessListener() {
            @Override
            public void finishLoginProcess(int i, MiAccountInfo miAccountInfo) {
                switch (i) {
                    case MiErrorCode.MI_XIAOMI_GAMECENTER_SUCCESS:
                        // 登陆成功
                        listener.onCallBack(EmaCallBackConst.LOGINSUCCESS, "登陆成功回调");

                        //获取用户的登陆后的 UID(即用户唯一标识)
                        long uid = miAccountInfo.getUid();
                        String nikename = miAccountInfo.getNikename();
                        EmaUser.getInstance().setmUid(uid + "");
                        EmaUser.getInstance().setNickName(nikename);

                        //获取用户的登陆的 Session(请参考 3.3用户session验证接口)
                        String session = miAccountInfo.getSessionId();//若没有登录返回 null
                        //请开发者完成将uid和session提交给开发者自己服务器进行session验证

                        //绑定服务
                        Intent serviceIntent = new Intent(mActivity, EmaService.class);
                        mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                        //补充弱账户信息
                        EmaSDKUser.getInstance().updateWeakAccount(ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getIMEI(mActivity), EmaUser.getInstance().getAllianceUid());

                        break;
                    case MiErrorCode.MI_XIAOMI_GAMECENTER_ERROR_LOGIN_FAIL:
                        // 登陆失败
                        listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                        break;
                    case MiErrorCode.MI_XIAOMI_GAMECENTER_ERROR_CANCEL:
                        // 取消登录
                        listener.onCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消回调");
                        break;
                    default:
                        // 登录失败
                        listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                        break;
                }
            }
        });
    }

    /**
     * xiaomi的监听在发起支付时已设置好 此处空实现
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {}

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

        MiBuyInfoOnline online = new MiBuyInfoOnline();
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
                });
    }


    public void logout() {
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
}

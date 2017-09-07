package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.yyjia.sdk.center.GMcenter;
import com.yyjia.sdk.data.Information;
import com.yyjia.sdk.listener.InitListener;
import com.yyjia.sdk.listener.LoginListener;
import com.yyjia.sdk.listener.PayListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;
    private GMcenter mCenter;


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
        mCenter = GMcenter.getInstance(mActivity);
        mCenter.onCreate(mActivity);

        mCenter.setInitListener(new InitListener() {
            @Override
            public void initSuccessed(String code) {
                if (code == Information.INITSUCCESSEDS) {
                    //初始化成功调用登录接口
                    //mCenter.checkLogin();
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功", null);
                } else {
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITFALIED, "初始化失败", null);
                }
            }
        });

        mCenter.setLoginListener(new LoginListener() {
            @Override
            public void loginSuccessed(String code) {
                if (code == Information.LOGIN_SUSECCEDS) {
                    //String sid = mCenter.getSid();//取得sessionid
                    int sid =mCenter.getUid();
                    HashMap<String, String> userData = new HashMap<>();
                    userData.put(EmaConst.ALLIANCE_UID, sid+"");
                    userData.put(EmaConst.NICK_NAME, "");

                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINSUCCESS_CHANNEL, "渠道登录成功", userData);
                } else {
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINFALIED, "登录失败", null);
                }
            }

            @Override
            public void logcancelSuccessed(String code) {
                if (code == Information.LOGCANCEL_SUSECCED) {
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINCANELL, "登录取消", null);
                }
            }

            @Override
            public void logoutSuccessed(String code) {
                if (code == Information.LOGOUT_SUSECCED) {
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGOUTSUCCESS, "登出成功", null);
                } else {
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGOUTFALIED, "登出失败", null);
                }
            }
        });
    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {

    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCenter.checkLogin();
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

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Float money = Float.valueOf(emaPayInfo.getPrice());
                String productname = emaPayInfo.getProductName();
                String serverId = (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ZONE_ID, "");
                String charId = EmaUser.getInstance().getAllianceUid();
                String cporderId = emaPayInfo.getOrderId();
                String callbackInfo = "ext";

                mCenter.pay(mActivity, money, productname, serverId, charId, cporderId, callbackInfo, new PayListener() {
                    @Override
                    public void paySuccessed(String code, String cporderid) {
                        if (code == Information.PAY_SUSECCED) {
                            listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                        } else {
                            listener.onCallBack(EmaCallBackConst.PAYFALIED, "支付失败");
                        }
                    }

                    @Override
                    public void payGoBack() {
                        listener.onCallBack(EmaCallBackConst.PAYCANELI, "支付取消");
                        EmaPay.getInstance(mActivity).cancelOrder();
                    }
                });

            }
        });
    }

    @Override
    public void logout() {
        mCenter.logout();
    }

    @Override
    public void swichAccount() {

    }

    @Override
    public void doShowToolbar() {
        mCenter.showFloatingView();
    }

    @Override
    public void doHideToobar() {
        mCenter.hideFloatingView();
    }

    @Override
    public void onResume() {
        if (mCenter != null) {
            mCenter.showFloatingView();
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
        if (mCenter != null) {
            mCenter.hideFloatingView();
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onBackPressed(final EmaBackPressedAction action) {
        action.doBackPressedAction();
        mCenter.exitGame();
    }

    @Override
    public void submitGameRole(Map<String, String> data) {
        String serverId = data.get(EmaConst.SUBMIT_ZONE_ID);
        String serverName = data.get(EmaConst.SUBMIT_ZONE_NAME);
        String roleId = data.get(EmaConst.SUBMIT_ROLE_ID);
        String roleName = data.get(EmaConst.SUBMIT_ROLE_NAME);
        String roleLevel = data.get(EmaConst.SUBMIT_ROLE_LEVEL);
        String roleCTime = data.get(EmaConst.SUBMIT_ROLE_CT);
        mCenter.submitRoleInfo(serverId, serverName, roleId, roleName, roleLevel, roleCTime);
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

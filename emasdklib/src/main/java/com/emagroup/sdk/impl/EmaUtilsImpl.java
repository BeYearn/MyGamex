package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDK;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.game.handler.LoginHandler;
import com.huawei.android.hms.agent.game.handler.SaveInfoHandler;
import com.huawei.android.hms.agent.pay.PaySignUtil;
import com.huawei.android.hms.agent.pay.handler.PayHandler;
import com.huawei.hms.support.api.entity.game.GamePlayerInfo;
import com.huawei.hms.support.api.entity.game.GameUserData;
import com.huawei.hms.support.api.entity.pay.PayReq;
import com.huawei.hms.support.api.entity.pay.PayStatusCodes;
import com.huawei.hms.support.api.pay.PayResultInfo;

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
    private String appId;
    private String channelAppKey;
    private String pay_rsa_private;
    private String pay_rsa_public;
    private String buo_secret;
    private String cp_id;
    private String pay_id;


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
        HMSAgent.init(mActivity);
        HMSAgent.connect(mActivity, new ConnectHandler() {
            @Override
            public void onConnect(int rst) {
                Log.e("hw connect", ": " + rst);
            }
        });
        HMSAgent.checkUpdate(mActivity);
    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {
        try {
            appId = data.getString("channelAppId");
            channelAppKey = data.getString("channelAppKey");//登录鉴权公钥？？？？
            pay_rsa_private = data.getString("channelAppPrivate");
            pay_rsa_public = data.getString("channelAppSecret");

            JSONObject paramsMap = data.getJSONObject("paramsMap");

            pay_id = paramsMap.getString("PAY_ID");
            cp_id = paramsMap.getString("CP_ID");
            buo_secret = paramsMap.getString("BUO_SECRET"); //浮标密钥

            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功", null);
        } catch (JSONException e) {
            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITFALIED, "HW初始化SDK失败", null);
            e.printStackTrace();
        }
    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {

        HMSAgent.Game.login(new LoginHandler() {
            @Override
            public void onResult(int retCode, GameUserData userData) {
                if (retCode == HMSAgent.AgentResultCode.HMSAGENT_SUCCESS && userData != null) {
                    Log.e("HW login:", " onResult: retCode=" + retCode + "  user=" + userData.getDisplayName() + "|" + userData.getPlayerId()
                            + "|" + userData.getIsAuth() + "|" + userData.getPlayerLevel());
                    // 当登录成功时，此方法会回调2次
                    // 第1次：只回调了playerid；特点：速度快；在要求快速登录，并且对安全要求不高时可以用此playerid登录
                    // 第2次：回调了所有信息，userData.getIsAuth()为1；此时需要对登录结果进行验签
                    if (userData.getIsAuth() == 1) {
                        // 如果需要对登录结果进行验签，请发送请求到开发者服务器进行（安全起见，私钥要放在服务端保存）。
                        // 下面工具方法仅为了展示验签请求的逻辑，实际实现应该放在开发者服务端。
                        /*GameLoginSignUtil.checkLoginSign(appId, cp_id, game_priv_key, game_public_key, userData, new ICheckLoginSignHandler() {
                            @Override
                            public void onCheckResult(String code, String resultDesc, boolean isCheckSuccess) {
                                showLog("game login check sign: onResult: retCode=" + code + "  resultDesc=" + resultDesc + "  isCheckSuccess=" + isCheckSuccess);
                            }
                        });*/
                    }

                    //显示toolbar
                    EmaSDK.getInstance().doShowToolbar();

                    HashMap<String, String> data = new HashMap<>();
                    data.put(EmaConst.ALLIANCE_UID, userData.getPlayerId());
                    data.put(EmaConst.NICK_NAME, userData.getDisplayName());

                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINSUCCESS_CHANNEL, "渠道登录成功", data);

                    //华为上传游戏信息
                    addPlayerInfo();

                } else {
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调", null);
                    Log.e("HWlogin:", "onResult: retCode=" + retCode);
                }
            }

            @Override
            public void onChange() {
                // 此处帐号登录发生变化，需要重新登录
                Log.e("HWlogin", " login changed!");
                EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGOUTSUCCESS, "登出成功", null);
            }
        }, 1);

    }

    private void addPlayerInfo() {
        GamePlayerInfo gpi = new GamePlayerInfo();
        gpi.area = (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ZONE_NAME, "");
        gpi.rank = (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_LEVEL, "");
        gpi.role = (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_NAME, "");
        HMSAgent.Game.savePlayerInfo(gpi, new SaveInfoHandler() {
            @Override
            public void onResult(int retCode) {
                Log.e("HWsavePlayerInfo:", "onResult=" + retCode);
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
        PayReq payReq = new PayReq();

        /**
         * 生成requestId
         */
        /*DateFormat format = new java.text.SimpleDateFormat("yyyyMMddhhmmssSSS");
        int random= new SecureRandom().nextInt() % 100000;
        random = random < 0 ? -random : random;
        String requestId = format.format(new Date());
        requestId = String.format("%s%05d", requestId, random);*/
        String requestId = emaPayInfo.getOrderShortId();

        /**
         * 生成总金额
         */
        String amount = emaPayInfo.getPrice()+".00";

        //商品名称
        payReq.productName = emaPayInfo.getProductName();
        //商品描述
        payReq.productDesc = emaPayInfo.getDescription();
        // 商户ID，来源于开发者联盟的“支付ID”
        payReq.merchantId = cp_id;
        // 应用ID，来源于开发者联盟
        payReq.applicationID = appId;
        // 支付金额
        payReq.amount = amount;
        // 商户订单号：开发者在支付前生成，用来唯一标识一次支付请求
        payReq.requestId = requestId;
        // 国家码
        payReq.country = "CN";
        //币种
        payReq.currency = "CNY";
        // 渠道号 1 代表应用市场渠道
        payReq.sdkChannel = 1;
        // 回调接口版本号 如果传值则固定值传2
        payReq.urlVer = "2";
        // 商户名称，必填，不参与签名。开发者注册的公司名称
        payReq.merchantName = "乌鲁木齐柠檬水网络科技有限公司";
        //分类，必填，不参与签名。该字段会影响风控策略
        // X4：主题,X5：应用商店,  X6：游戏,X7：天际通,X8：云空间,X9：电子书,X10：华为学习,X11：音乐,X12 视频,
        // X31 话费充值,X32 机票/酒店,X33 电影票,X34 团购,X35 手机预购,X36 公共缴费,X39 流量充值
        payReq.serviceCatalog = "X6";
        //商户保留信息，选填不参与签名，支付成功后会华为支付平台会原样 回调CP服务端
        payReq.extReserved = "ext_word";
        //对单机应用可以直接调用此方法对请求信息签名，非单机应用一定要在服务器端储存签名私钥，并在服务器端进行签名操作。
        //下面调用的工具方法，供实现参考
        payReq.sign = PaySignUtil.calculateSignString(payReq, pay_rsa_private);


        HMSAgent.Pay.pay(payReq, new PayHandler() {
            @Override
            public void onResult(int retCode, PayResultInfo payInfo) {
                Log.e("HWPay restCode:",retCode+"");
                if (retCode == HMSAgent.AgentResultCode.HMSAGENT_SUCCESS && payInfo != null) {
                    boolean checkRst = PaySignUtil.checkSign(payInfo, pay_rsa_public);
                    Log.e("hwPay", "game pay: onResult: pay success and checksign=" + checkRst);
                    if (checkRst) {
                        // 支付成功并且验签成功，发放商品
                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                    } else {
                        // 签名失败，需要查询订单状态：对于没有服务器的单机应用，调用查询订单接口查询；其他应用到开发者服务器查询订单状态。
                        listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                    }
                } else if (retCode == HMSAgent.AgentResultCode.ON_ACTIVITY_RESULT_ERROR
                        || retCode == PayStatusCodes.PAY_STATE_TIME_OUT
                        || retCode == PayStatusCodes.PAY_STATE_NET_ERROR) {
                    // 需要查询订单状态：对于没有服务器的单机应用，调用查询订单接口查询；其他应用到开发者服务器查询订单状态。
                    listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                } else {
                    Log.e("hw pay:", "onResult: pay fail=" + retCode);
                    // 其他错误码意义参照支付api参考
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
        // 在界面恢复的时候显示浮标，和onPause配合使用
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HMSAgent.Game.showFloatWindow(mActivity);
            }
        });
    }

    @Override
    public void doHideToobar() {
        HMSAgent.Game.hideFloatWindow(mActivity);
    }

    @Override
    public void onResume() {
        doShowToolbar();
    }

    @Override
    public void onPause() {
        doHideToobar();
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

    //-----------------------------------xxx的网络请求方法-------------------------------------------------------------------------


    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String getMhrSignJson() {
        return Url.getServerUrl() + "/ema-platform/extra/mhrCreateOrder";
    }
}

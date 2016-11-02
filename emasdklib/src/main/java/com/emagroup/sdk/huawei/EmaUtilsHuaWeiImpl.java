package com.emagroup.sdk.huawei;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.android.huawei.pay.plugin.PayParameters;
import com.android.huawei.pay.util.HuaweiPayUtil;
import com.android.huawei.pay.util.Rsa;
import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDK;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaSDKUser;
import com.emagroup.sdk.EmaService;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.ULocalUtils;
import com.huawei.gameservice.sdk.GameServiceSDK;
import com.huawei.gameservice.sdk.api.GameEventHandler;
import com.huawei.gameservice.sdk.api.PayResult;
import com.huawei.gameservice.sdk.api.Result;
import com.huawei.gameservice.sdk.api.UserResult;
import com.huawei.gameservice.sdk.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EmaUtilsHuaWeiImpl {

    private static EmaUtilsHuaWeiImpl instance;

    private Activity mActivity;
    private String appId;
    private String channelAppKey;
    private String pay_rsa_private;
    private String pay_rsa_public;
    private String buo_secret;
    private String cp_id;
    private String pay_id;

    public static EmaUtilsHuaWeiImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsHuaWeiImpl(activity);
        }
        return instance;
    }

    private EmaUtilsHuaWeiImpl(Activity activity) {
        this.mActivity = activity;
    }

    public void realInit(final EmaSDKListener listener, JSONObject data) {
        try {
            appId = data.getString("channelAppId");
            channelAppKey = data.getString("channelAppKey");//登录鉴权公钥？？？？
            pay_rsa_private = data.getString("channelAppPrivate");
            pay_rsa_public = data.getString("channelAppSecret");

            JSONObject paramsMap = data.getJSONObject("paramsMap");

            buo_secret = paramsMap.getString("BUO_SECRET"); //浮标密钥
            cp_id = paramsMap.getString("CP_ID");
            pay_id = paramsMap.getString("PAY_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GameServiceSDK.init(mActivity, appId, cp_id, "com.huawei.gb.huawei.installnewtype.provider", new GameEventHandler() {
            @Override
            public void onResult(Result result) {// 判断如果初始化成功，则此处调用登录接口并继续加载游戏资源
                if (result.rtnCode == Result.RESULT_OK) {

                    listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
                    Log.e("EmaAnySDK", "HW初始化成功");

                    //初始化成功之后再检查公告更新等信息
                    EmaUtils.getInstance(mActivity).checkSDKStatus();

                    //华为的检测游戏更新（注意我们的就给华为特殊配一下不走我们的了）// TODO: 2016/11/1 暂时屏蔽掉
                    //checkUpdate();

                } else {
                    listener.onCallBack(EmaCallBackConst.INITFALIED, "HW初始化SDK失败");
                }
            }

            @Override
            public String getGameSign(String appId, String cpId, String ts) {//返回游戏签名，签名算法见4.1章节
                return createGameSign(appId + cpId + ts);
            }
        });

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        GameServiceSDK.login(mActivity, new GameEventHandler() {
            @Override
            public void onResult(Result result) {
                UserResult userResult = (UserResult) result;
                if (userResult.rtnCode == Result.RESULT_ERR_NOT_INIT) {// 未初始化，需要先调用初始化接口
                    return;
                }
                if (userResult.rtnCode != Result.RESULT_OK) {//登录失败
                    listener.onCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调");
                    return;
                }
                if (userResult.rtnCode == Result.RESULT_OK && userResult.isAuth != null && userResult.isAuth == 0) {// 场景一： 登录成功
                    //登录成功回调放在下面updateWeakAccount和docallback成功以后在回调

                    EmaUser.getInstance().setmUid(userResult.playerId);
                    EmaUser.getInstance().setNickName(userResult.displayName);

                    //显示toolbar
                    EmaSDK.getInstance().doShowToolbar();

                    //绑定服务
                    Intent serviceIntent = new Intent(mActivity, EmaService.class);
                    mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                    //补充弱账户信息
                    EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getIMEI(mActivity), EmaUser.getInstance().getAllianceUid());
                    return;
                }

                if (userResult.rtnCode == Result.RESULT_OK && userResult.isAuth != null && userResult.isAuth == 1) {// 场景二： 通知鉴权签名校验
                    // 收到SDK的鉴权签名校验通知， CP可通过服务端校验该返回值，参见4.4.1.5
                    Log.e("emasdk", "鉴权签名校验完成。。");
                    return;
                }
                if (userResult.rtnCode == Result.RESULT_OK && userResult.isChange != null && userResult.isChange == 1) {// 场景三： 通知帐号变换
                    // 收到SDK的帐号变更通知，退出游戏重新登录
                    listener.onCallBack(EmaCallBackConst.ACCOUNTSWITCHSUCCESS, "切换成功回调");
                    return;
                }
            }

            @Override
            public String getGameSign(String appId, String cpId, String ts) {
                return createGameSign(appId + cpId + ts);
            }
        }, 1);
    }


    /**
     * 因为any的支付监听是单独先设置的
     *
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {

    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

        Map<String, String> params = new HashMap<>();
        // 必填字段，不能为null或者""，请填写从联盟获取的支付ID
        // the pay ID is required and can not be null or ""
        params.put("userID", pay_id);
        // 必填字段，不能为null或者""，请填写从联盟获取的应用ID
        // the APP ID is required and can not be null or ""
        params.put("applicationID", appId);
        // 必填字段，不能为null或者""，单位是元，精确到小数点后两位，如1.00
        // the amount (accurate to two decimal places) is required
        params.put("amount", emaPayInfo.getPrice() + ".00");
        // 必填字段，不能为null或者""，道具名称
        // the product name is required and can not be null or ""
        params.put("productName", emaPayInfo.getProductName());
        // 必填字段，不能为null或者""，道具描述
        // the product description is required and can not be null or ""
        params.put("productDesc", emaPayInfo.getDescription());
        // 必填字段，不能为null或者""，最长30字节，不能重复，否则订单会失败
        // the request ID is required and can not be null or "". Also it must be unique.
        params.put("requestId", emaPayInfo.getOrderShortId());

        String noSign = HuaweiPayUtil.getSignData(params);
        LogUtil.d("startPay", "noSign：" + noSign);

        // CP必须把参数传递到服务端，在服务端进行签名，然后把sign传递下来使用；服务端签名的代码和客户端一致
        // the CP need to send the params to the server and sign the params on the server ,
        // then the server passes down the sign to client;
        String sign = Rsa.sign(noSign, pay_rsa_private);
        LogUtil.d("startPay", "sign： " + sign);


        Map<String, Object> payInfo = new HashMap<>();
        // 必填字段，不能为null或者""
        // the amount is required and can not be null or ""
        payInfo.put("amount", emaPayInfo.getPrice() + ".00");
        // 必填字段，不能为null或者""
        // the product name is required and can not be null or ""
        payInfo.put("productName", emaPayInfo.getProductName());
        // 必填字段，不能为null或者""
        // the request ID is required and can not be null or ""
        payInfo.put("requestId", emaPayInfo.getOrderShortId());
        // 必填字段，不能为null或者""
        // the product description is required and can not be null or ""
        payInfo.put("productDesc", emaPayInfo.getDescription());
        // 必填字段，不能为null或者""，请填写自己的公司名称
        // the user name is required and can not be null or "". Input the company name of CP.
        payInfo.put("userName", "emagroup");
        // 必填字段，不能为null或者""
        // the APP ID is required and can not be null or "".
        payInfo.put("applicationID", appId);
        // 必填字段，不能为null或者""
        // the user ID is required and can not be null or "".
        payInfo.put("userID", pay_id);
        // 必填字段，不能为null或者""
        // the sign is required and can not be null or "".
        payInfo.put("sign", sign);

        // 必填字段，不能为null或者""，此处写死X6
        // the service catalog is required and can not be null or "".
        payInfo.put("serviceCatalog", "X6");


        // 调试期可打开日志，发布时注释掉
        //payInfo.put("showLog", true);

        if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            payInfo.put("screentOrient", 2);
        } else {
            payInfo.put("screentOrient", 1);
        }

            //kaishi zhifu
        GameServiceSDK.startPay(mActivity, payInfo, new GameEventHandler() {
            @Override
            public String getGameSign(String appId, String cpId, String ts) {
                return null;
            }

            @Override
            public void onResult(Result result)
            {
                Map<String, String> payResp = ((PayResult)result).getResultMap();
                // 支付成功，进行验签
                Log.e("emasdkhuawPay",PayParameters.errMsg);
                if ("0".equals(payResp.get(PayParameters.returnCode))) {
                    if ("success".equals(payResp.get(PayParameters.errMsg))) {
                        // 支付成功，验证信息的安全性；待验签字符串中如果有isCheckReturnCode参数且为yes，则去除isCheckReturnCode参数
                        if (payResp.containsKey("isCheckReturnCode") && "yes".equals(payResp.get("isCheckReturnCode"))) {
                            payResp.remove("isCheckReturnCode");
                        }
                        // 支付成功，验证信息的安全性；待验签字符串中如果没有isCheckReturnCode参数活着不为yes，则去除isCheckReturnCode和returnCode参数
                        else {
                            payResp.remove("isCheckReturnCode");
                            payResp.remove(PayParameters.returnCode);
                        }
                        // 支付成功，验证信息的安全性；待验签字符串需要去除sign参数
                        String sign = payResp.remove(PayParameters.sign);

                        String noSigna = HuaweiPayUtil.getSignData(payResp);

                        // 使用公钥进行验签
                        // check the sign using RSA public key
                        boolean s = Rsa.doCheck(noSigna, sign, pay_rsa_public);  //对于支付成功但验签失败的订单，建议去服务器查询此笔订单的支付状态，并且以服务器查询结果为准

                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS,"购买成功");
                    }
                } else if ("30000".equals(payResp.get(PayParameters.returnCode))) {

                    // 取消购买 //call一次取消订单
                    EmaPay.getInstance(mActivity).cancelOrder();
                    listener.onCallBack(EmaCallBackConst.PAYCANELI,"取消购买");

                } else {
                    EmaPay.getInstance(mActivity).cancelOrder();
                    listener.onCallBack(EmaCallBackConst.PAYFALIED,"购买失败");
                }
            }
        });
    }

    public void logout() {

    }

    public void swichAccount() {

    }

    public void doShowToolbar() {
        // 在界面恢复的时候显示浮标，和onPause配合使用
        GameServiceSDK.showFloatWindow(mActivity);
    }

    public void doHideToobar() {
        // 在界面暂停的时候，隐藏悬浮按钮，和onResume配合使用
        GameServiceSDK.hideFloatWindow(mActivity);
    }

    public void onResume() {
        doShowToolbar();
    }

    public void onPause() {
        doHideToobar();
    }

    public void onStop() {
    }

    public void onDestroy() {
        GameServiceSDK.destroy(mActivity);
    }

    public void onBackPressed(EmaBackPressedAction action) {
            action.doBackPressedAction();
    }


//------------------------------------华为的方法------------------------------------------------------------------------------------------

    /**
     * 生成游戏签名
     * generate the game sign
     */
    private String createGameSign(String data) {

        // 为了安全把浮标密钥放到服务端，并使用https的方式获取下来存储到内存中，CP可以使用自己的安全方式处理
        // For safety, buoy key put into the server and use the https way to get down into the client's memory.
        // By the way CP can also use their safe approach.

        String str = data;
        try {
            String result = RSAUtil.sha256WithRsa(str.getBytes("UTF-8"), buo_secret);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检测游戏更新
     * check the update for game
     */
    private void checkUpdate() {
        GameServiceSDK.checkUpdate(mActivity, new GameEventHandler() {

            @Override
            public void onResult(Result result) {
                if (result.rtnCode != Result.RESULT_OK) {

                }
            }

            @Override
            public String getGameSign(String appId, String cpId, String ts) {
                return createGameSign(appId + cpId + ts);
            }

        });
    }
}

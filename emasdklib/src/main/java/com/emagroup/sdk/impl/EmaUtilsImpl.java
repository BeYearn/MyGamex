package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.android.huawei.pay.plugin.PayParameters;
import com.android.huawei.pay.util.HuaweiPayUtil;
import com.android.huawei.pay.util.Rsa;
import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDK;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.HttpRequestor;
import com.emagroup.sdk.ThreadUtil;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.huawei.gameservice.sdk.GameServiceSDK;
import com.huawei.gameservice.sdk.control.GameEventHandler;
import com.huawei.gameservice.sdk.model.PayResult;
import com.huawei.gameservice.sdk.model.Result;
import com.huawei.gameservice.sdk.model.RoleInfo;
import com.huawei.gameservice.sdk.model.UserResult;
import com.huawei.gameservice.sdk.util.LogUtil;

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
    private EmaSDKListener mListener;
    private JSONObject mData;
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

    }

    @Override
    public void realInit(final EmaSDKListener listener, JSONObject data) {
        this.mListener = listener;
        this.mData = data;
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
                Log.e("huaweiinit", result.toString());
                if (result.rtnCode == Result.RESULT_OK) {

                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功",null);
                    Log.e("EmaAnySDK", "HW初始化成功");

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //华为的检测游戏更新（注意我们的就给华为特殊配一下不走我们的了）  这个真心蛋疼！！
                            checkUpdate();
                        }
                    });
                } else {
                    //listener.onCallBack(EmaCallBackConst.INITFALIED, "HW初始化SDK失败");
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功",null); // 华为蛋疼！！  因为他还有二次机会（但游戏要是失败了，就不再给调别的了）
                }
            }

            @Override
            public String getGameSign(String appId, String cpId, String ts) {//返回游戏签名，签名算法见4.1章节
                return createGameSign(appId + cpId + ts);
            }
        });
    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        GameServiceSDK.login(mActivity, new GameEventHandler() {
            @Override
            public void onResult(Result result) {
                UserResult userResult = (UserResult) result;
                Log.e("huaweirealLogin", userResult.toString());
                if (userResult.rtnCode == Result.RESULT_ERR_NOT_INIT) {// 未初始化，需要先调用初始化接口
                    realInit(mListener, mData);
                    return;
                }
                if (userResult.rtnCode == Result.RESULT_ERR_COMM) {// 安装游戏中心取消掉了
                    realInit(mListener, mData);
                    Log.e("huawei", "re Init");
                    return;
                }
                if (userResult.rtnCode == Result.RESULT_ERR_CANCEL) {
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消回调",null);
                    return;
                }
                if (userResult.rtnCode != Result.RESULT_OK) {//登录失败
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调",null);
                    return;
                }
                if (userResult.rtnCode == Result.RESULT_OK && userResult.isAuth != null && userResult.isAuth == 0) {// 场景一： 登录成功
                    //显示toolbar
                    EmaSDK.getInstance().doShowToolbar();

                    HashMap<String, String> data = new HashMap<>();
                    data.put(EmaConst.ALLIANCE_UID,userResult.playerId);
                    data.put(EmaConst.NICK_NAME,userResult.displayName);

                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINSUCCESS_CHANNEL, "渠道登录成功",data);

                    //华为上传游戏信息
                    addPlayerInfo();
                }

                if (userResult.rtnCode == Result.RESULT_OK && userResult.isAuth != null && userResult.isAuth == 1) {// 场景二： 通知鉴权签名校验
                    // 收到SDK的鉴权签名校验通知， CP可通过服务端校验该返回值，参见4.4.1.5

                    checkSign(userResult.gameAuthSign, userResult.playerId, appId, userResult.ts);
                }
                if (userResult.rtnCode == Result.RESULT_OK && userResult.isChange != null && userResult.isChange == 1) {// 场景三： 通知帐号变换
                    // 收到SDK的帐号变更通知，退出游戏重新登录
                    EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.ACCOUNTSWITCHSUCCESS, "切换成功回调",null);
                    //显示toolbar
                    EmaSDK.getInstance().doShowToolbar();
                }
            }

            @Override
            public String getGameSign(String appId, String cpId, String ts) {
                return createGameSign(appId + cpId + ts);
            }
        }, 1);
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
            public void onResult(Result result) {
                Map<String, String> payResp = ((PayResult) result).getResultMap();
                // 支付成功，进行验签
                Log.e("emasdkhuawPay", PayParameters.errMsg);
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

                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                    }
                } else if ("30000".equals(payResp.get(PayParameters.returnCode))) {

                    // 取消购买 //call一次取消订单
                    EmaPay.getInstance(mActivity).cancelOrder();
                    listener.onCallBack(EmaCallBackConst.PAYCANELI, "取消购买");

                } else {
                    EmaPay.getInstance(mActivity).cancelOrder();
                    listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
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
                GameServiceSDK.showFloatWindow(mActivity);
            }
        });
    }

    @Override
    public void doHideToobar() {
        // 在界面暂停的时候，隐藏悬浮按钮，和onResume配合使用
        GameServiceSDK.hideFloatWindow(mActivity);
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
        GameServiceSDK.destroy(mActivity);
    }

    @Override
    public void onBackPressed(final EmaBackPressedAction action) {
        action.doBackPressedAction();
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
                Log.e("huaweicheckUpdate", result.toString());
                if (result.rtnCode != Result.RESULT_OK) {
                    Log.e("huawei", "check update failed");
                }
            }

            @Override
            public String getGameSign(String appId, String cpId, String ts) {
                return createGameSign(appId + cpId + ts);
            }

        });
    }


    private void checkSign(final String gameAuthSign, final String playerId, final String appId, final String ts) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String url = checkSign();

                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("gameAuthSign", gameAuthSign);
                    paramMap.put("playerId", playerId);
                    paramMap.put("appId", appId);
                    paramMap.put("ts", ts);

                    String result = new HttpRequestor().doPost(url, paramMap);

                    JSONObject json = new JSONObject(result);
                    String dataStr = json.getString("data");

                    if ("true".equals(dataStr)) {

                    }
                    Log.e("huaweicheckSign", "checkSign" + dataStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("update弱账户创建", "maybe is SocketTimeoutException");
                }

            }
        });
    }

    private void addPlayerInfo() {
        /**
         * 将用户的等级等信息保存起来，必须的参数为RoleInfo.GAME_RANK(等
         级)/RoleInfo.GAME_ROLE(角色名称)/RoleInfo.GAME_AREA(角色所属区)
         /RoleInfo.GAME_SOCIATY(角色所属公会名称)
         * 全部使用String类型存放
         */
        HashMap<String, String> playerInfo = new HashMap<>();
        playerInfo.put(RoleInfo.GAME_RANK, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_LEVEL, ""));
        playerInfo.put(RoleInfo.GAME_ROLE, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_NAME, ""));
        playerInfo.put(RoleInfo.USER_ID, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_ID, ""));
        playerInfo.put(RoleInfo.GAME_AREA, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ZONE_NAME, ""));
        playerInfo.put(RoleInfo.CREATE_TIME, (String) ULocalUtils.spGet(mActivity, EmaConst.SUBMIT_ROLE_CT, ""));
        //playerInfo.put(RoleInfo.GAME_SOCIATY, (String) ULocalUtils.spGet(mActivity,"zoneId_R",""));

        // 存储用户当前的角色信息
        GameServiceSDK.addPlayerInfo(mActivity, playerInfo, new GameEventHandler() {
            @Override
            public void onResult(Result result) {
                // 返回操作结果
                Log.e("huaweiaddPlayerInfo", result.toString());
            }

            @Override
            public String getGameSign(String appId, String cpId, String ts) {
                return createGameSign(appId + cpId + ts);
            }
        });
    }

    //-----------------------------------xxx 特有接口---------------------------------------------------

    public static String checkSign() {
        return Url.getServerUrl() + "/ema-platform/channelLogin/huawei";
    }
}

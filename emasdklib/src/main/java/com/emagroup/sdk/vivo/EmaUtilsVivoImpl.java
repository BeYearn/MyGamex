package com.emagroup.sdk.vivo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bbk.payment.payment.OnVivoPayResultListener;
import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
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
import com.vivo.sdkplugin.accounts.OnVivoAccountChangedListener;
import com.vivo.sdkplugin.aidl.VivoUnionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsVivoImpl {

    private static EmaUtilsVivoImpl instance;

    private Activity mActivity;
    private VivoUnionManager mVivoUnionManager;
    private String appId;
    private OnVivoAccountChangedListener mAccountListener;
    private OnVivoPayResultListener mPayResultListener;

    public static EmaUtilsVivoImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsVivoImpl(activity);
        }
        return instance;
    }

    private EmaUtilsVivoImpl(Activity activity) {
        this.mActivity = activity;
    }

    public void realInit(final EmaSDKListener listener, JSONObject data) {
        try {

            JSONObject paramsMap = data.getJSONObject("paramsMap");

            //浮标密钥
            appId = paramsMap.getString("AppId");
            String cpId = paramsMap.getString("CpId");
            String cpKey = paramsMap.getString("CpKey");


            mVivoUnionManager = new VivoUnionManager(mActivity);

            mAccountListener = new OnVivoAccountChangedListener() {
                //通过该方法获取用户信息
                @Override
                public void onAccountLogin(String name, String openid, String authtoken) {
                    //authtoken：第三方游戏用此token到vivo帐户系统服务端校验帐户信息//openid：帐户唯一标识//name:帐户名

                    // 登陆成功//登录成功回调放在下面updateWeakAccount和docallback成功以后在回调
                    //获取用户的登陆后的 UID(即用户唯一标识)
                    EmaUser.getInstance().setAllianceUid(openid);
                    EmaUser.getInstance().setNickName(name);

                    //绑定服务
                    Intent serviceIntent = new Intent(mActivity, EmaService.class);
                    mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                    //补充弱账户信息
                    EmaSDKUser.getInstance().updateWeakAccount(listener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());

                }

                //第三方游戏不需要使用此回调方法
                @Override
                public void onAccountRemove(boolean isRemoved) {
                }

                @Override
                //取消登录的回调方法
                public void onAccountLoginCancled() {
                    listener.onCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消回调");
                }
            };

            mVivoUnionManager.registVivoAccountChangeListener(mAccountListener);
            mVivoUnionManager.bindUnionService();

            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");
            //初始化成功之后再检查公告更新等信息
            EmaUtils.getInstance(mActivity).checkSDKStatus();

        } catch (JSONException e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
            e.printStackTrace();
        }

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {
        mVivoUnionManager.startLogin(appId);
    }

    /**
     * xiaomi的监听在发起支付时已设置好 此处空实现
     *
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {

    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

        vivoCreateOrder(emaPayInfo);

        mVivoUnionManager.initVivoPaymentAndRecharge(mActivity, mPayResultListener);
        mPayResultListener = new OnVivoPayResultListener() {
            //通过该回调方法获取支付结果
            @Override
            public void payResult(String transNo, boolean pay_result, String result_code, String pay_msg) {
                // transNo: 交易编号// pay_result:交易结果// result_code：状态码（参考附录“状态码(res_code)及描述”）// pay_msg:结果描述
                switch (result_code){
                    case "9000":
                        // 购买成功
                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                        break;
                    case "6001":
                        // 取消购买
                        //call一次取消订单
                        EmaPay.getInstance(mActivity).cancelOrder();
                        listener.onCallBack(EmaCallBackConst.PAYCANELI, "取消购买");
                        break;
                    default:
                        // 购买失败
                        //call一次取消订单
                        EmaPay.getInstance(mActivity).cancelOrder();

                        listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                        break;
                }

            }
            //第三方游戏不需要使用此回调方法
            @Override
            public void rechargeResult(String openid, boolean pay_result, String result_code, String pay_msg) {}
        };
    }


    public void logout() {
    }

    public void swichAccount() {

    }

    public void doShowToolbar() {
        mVivoUnionManager.showVivoAssitView(mActivity);
    }

    public void doHideToobar() {
        mVivoUnionManager.hideVivoAssitView(mActivity);
    }

    public void onResume() {
        if(null!=mVivoUnionManager){
            mVivoUnionManager.showVivoAssitView(mActivity);
        }
    }

    public void onPause() {
    }

    public void onStop() {
    }

    public void onDestroy() {
        mVivoUnionManager.cancelVivoPaymentAndRecharge(mPayResultListener);
        mVivoUnionManager.hideVivoAssitView(mActivity);
        mVivoUnionManager.unRegistVivoAccountChangeListener(mAccountListener);
    }

    public void onBackPressed(EmaBackPressedAction action) {
        action.doBackPressedAction();
    }


    //-----------------------------------vivo的各种网络请求方法-------------------------------------------------------------------------

    private void vivoCreateOrder(final EmaPayInfo emaPayInfo) {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String url = Url.vivoCreateOrder();

                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("orderId", emaPayInfo.getOrderId());

                    String result = new HttpRequestor().doPost(url, paramMap);

                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject data = jsonObject.getJSONObject("data");

                    String orderNumber = data.getString("orderNumber");
                    String accessKey = data.getString("accessKey");

                    //组织调用支付接口需要的参数，参考附录“启动vivo支付中心参数表”
                    Bundle localBundle = new Bundle();
                    localBundle.putString("transNo", orderNumber); //订单推送接口返回的vivo订单号
                    localBundle.putString("accessKey", accessKey); //订单推送接口返回的accessKey
                    localBundle.putString("appId", appId); //在vivo开发者平台注册应用后获取到的appId
                    localBundle.putString("productName", emaPayInfo.getProductName()); //商品名称
                    localBundle.putString("productDes", emaPayInfo.getDescription());//商品描述
                    localBundle.putLong("price", emaPayInfo.getPrice());//商品价格，单位为分（1000即10.00元）

                    // 以下为可选参数，能收集到务必填写，如未填写，掉单、用户密码找回等问题可能无法解决。
                    localBundle.putString("blance", "100元宝");
                    localBundle.putString("vip", "vip2");
                    localBundle.putInt("level", 35);
                    localBundle.putString("party", "精英联盟");
                    localBundle.putString("roleId", EmaUser.getInstance().getAllianceUid());
                    localBundle.putString("roleName",EmaUser.getInstance().getNickName());
                    localBundle.putString("serverName", "天空之城");
                    localBundle.putString("extInfo", "扩展参数");
                    localBundle.putBoolean("logOnOff", false); // CP在接入过程请传true值,接入完成后在改为false, 传true会在支付SDK打印大量日志信息
                    //调用支付接口进行支付
                    mVivoUnionManager.payment(mActivity,  localBundle);

                } catch (Exception e) {
                    Log.e("getUCAccontInfo", "maybe is SocketTimeoutException");
                    e.printStackTrace();
                }

            }
        });
    }

    public void onRestart() {

    }

    public void onNewIntent(Intent intent) {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
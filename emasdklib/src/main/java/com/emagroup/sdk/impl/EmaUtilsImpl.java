package com.emagroup.sdk.impl;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaConst;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.EmaUtilsInterface;
import com.emagroup.sdk.ULocalUtils;
import com.emagroup.sdk.Url;
import com.wuming.platform.api.WMPlatform;
import com.wuming.platform.listener.WMLoginListener;
import com.wuming.platform.listener.WMPayListener;
import com.wuming.platform.model.WMError;
import com.wuming.platform.model.WMPayInfo;
import com.wuming.platform.model.WMUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/10/9.
 *
 */
public class EmaUtilsImpl implements EmaUtilsInterface {

    private static EmaUtilsImpl instance;
    private Activity mActivity;


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
        try {
            boolean isLandScape = mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            String channelAppKey = data.getString("channelAppKey");
            String channelAppSecret = data.getString("channelAppSecret");

            WMPlatform.getInstance().init(mActivity,
                    isLandScape? WMPlatform.WMPlatformDirection.Landscape: WMPlatform.WMPlatformDirection.Portrait,
                    channelAppKey,
                    channelAppSecret,
                    ULocalUtils.getApplicationName(mActivity),
                    mActivity.getPackageName()
            );

            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功",null);
        } catch (JSONException e) {
            EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.INITFALIED, "初始化失败",null);
            e.printStackTrace();
        }
    }

    @Override
    public void realLogin(final EmaSDKListener listener, String userid, String deviceId) {

        WMPlatform.getInstance().doLogin(new WMLoginListener() {

            @Override
            public void onLoginFailed(WMError error) {
                //登录失败执行此回调
                //失败信息 error.message
                Log.e("WMLoginFail",error.message);
                EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINFALIED, "登陆失败回调",null);
            }

            @Override
            public void onLoginCompleted(WMUser user){
                //登录成功执行此回调 用户信息的访问方式如下：
                //用户唯一标识 user.userId
                //用户帐号 user.userName
                //客户端表示 user.client
                //时间戳秒数   user.tokenTime
                //验证加密串   user.token
                HashMap<String, String> userData = new HashMap<>();
                userData.put(EmaConst.ALLIANCE_UID,user.userId);
                userData.put(EmaConst.NICK_NAME,user.userName);

                EmaUtils.getInstance(mActivity).makeUserCallBack(EmaCallBackConst.LOGINSUCCESS_CHANNEL, "渠道登录成功",userData);
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

        WMPayInfo payInfo = new WMPayInfo();
        payInfo.setAmount(emaPayInfo.getPrice()+"");//支付金额  单位：元(人民币) 注：正整数 请勿使用小数点
        payInfo.setName(emaPayInfo.getProductName());//设置购买货币单位名称
        payInfo.setServerId(ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ZONE_NAME,"100")+"");//设置区服
        payInfo.setOrderId(emaPayInfo.getOrderId());//设置订单号 请勿重复
        payInfo.setDescription(emaPayInfo.getProductNum()+"份");//货币数量描述，不要带货币单位
        payInfo.setExtendInfo("extend_info");//扩展字段，发货通知时会回传（app_ext字段）

        WMPlatform.getInstance().doPay(payInfo, new WMPayListener() {
            @Override
            public void onPayFailed(WMError error) {
                // 支付失败时回调此函数 应用在此函数中接管用户交互
                Log.e("WMPayFail",error.message);
                listener.onCallBack(EmaCallBackConst.PAYFALIED, "支付失败");
            }

            @Override
            public void onPayCompleted() {
                // 支付成功时回调此函数 应用在此函数中接管用户交互(具体发货逻辑请等待服务端回调，前端成功返回不保证一定成功)
                listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
            }
        });


    }

    @Override
    public void logout() {
        WMPlatform.getInstance().doLogout();
    }

    @Override
    public void swichAccount() {

    }

    @Override
    public void doShowToolbar() {
    }

    @Override
    public void doHideToobar() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
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
        String dataType = data.get(EmaConst.SUBMIT_DATA_TYPE);
        if("1".equals(dataType)){

            HashMap<String, String> map = new HashMap<>();
            map.put("uid", EmaUser.getInstance().getAllianceUid());
            map.put("sid",ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ZONE_ID,"")+"");
            map.put("sname",ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ZONE_NAME,"")+"");
            map.put("roleid",ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ROLE_ID,"")+"");
            map.put("rolename",ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ROLE_NAME,"")+"");
            map.put("rolelevel",ULocalUtils.spGet(mActivity,EmaConst.SUBMIT_ROLE_LEVEL,"")+"");

            WMPlatform.statistics("stat/role", map);
        }
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

package com.emagroup.sdk.m4399;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;

import com.emagroup.sdk.EmaBackPressedAction;
import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaPay;
import com.emagroup.sdk.EmaPayInfo;
import com.emagroup.sdk.EmaSDKListener;
import com.emagroup.sdk.EmaSDKUser;
import com.emagroup.sdk.EmaService;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.EmaUtils;
import com.emagroup.sdk.ThreadUtil;
import com.emagroup.sdk.ULocalUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.m4399.operate.OperateCenter;
import cn.m4399.operate.OperateCenterConfig;
import cn.m4399.operate.User;

/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtils4399Impl {

    private static EmaUtils4399Impl instance;

    private Activity mActivity;

    public static EmaUtils4399Impl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtils4399Impl(activity);
        }
        return instance;
    }

    private EmaUtils4399Impl(Activity activity){
        this.mActivity =activity;
    }

    public void realInit(final EmaSDKListener listener, JSONObject data){

        try {
            String channelAppKey = data.getString("channelAppKey");

            final OperateCenter mOpeCenter = OperateCenter.getInstance();
            OperateCenterConfig mOpeConfig = new OperateCenterConfig.Builder(mActivity)
                    .setGameKey(channelAppKey)     //设置AppKey
                    .setDebugEnabled(false)     //设置DEBUG模式,用于接入过程中开关日志输出，发布前必须设置为false或删除该行。默认为false。
                    .setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)  //设置横竖屏方向，默认为横屏，现支持横竖屏，和180度旋转
                    .setSupportExcess(true)     //设置服务端是否支持处理超出部分金额，默认为false
                    .setPopLogoStyle(OperateCenterConfig.PopLogoStyle.POPLOGOSTYLE_ONE) //设置悬浮窗样式，现有四种可选
                    .setPopWinPosition(OperateCenterConfig.PopWinPosition.POS_LEFT) //设置悬浮窗默认显示位置，现有四种可选
                    .build();
            mOpeCenter.setConfig(mOpeConfig);
            ThreadUtil.runInUiThread(new Runnable() {  //4399的sdk水平还不如我，设计太烂
                @Override
                public void run() {
                    mOpeCenter.init(mActivity,new OperateCenter.OnInitGloabListener() {
                        // 初始化结束执行后回调
                        @Override
                        public void onInitFinished(boolean isLogin, User userInfo) {
                            listener.onCallBack(EmaCallBackConst.INITSUCCESS, "初始化成功");

                            /*if(mOpeCenter.getIsLogin()){  //注：登录后如果未注销，登录状态将一直保持直至登录凭证过期或失效（若用户修改平台账户密码，所有游戏授权凭证将失效，需重新登录）。 建议游戏在初始化完成后调用登录状态查询接口查询用户当前登录状态。
                                User currentAccount = mOpeCenter.getCurrentAccount();
                                String uid = currentAccount.getUid();
                                String nikename = currentAccount.getName();
                                EmaUser.getInstance().setmUid(uid);
                                EmaUser.getInstance().setNickName(nikename);
                            }*/

                            //初始化成功之后再检查公告更新等信息
                            EmaUtils.getInstance(mActivity).checkSDKStatus();
                        }

                        // 注销帐号的回调， 包括个人中心里的注销和logout()注销方式
                        // fromUserCenter区分是否是从个人中心注销的，若是则为true，不是为false
                        @Override
                        public void onUserAccountLogout(boolean fromUserCenter, int resultCode) {
                        }

                        // 个人中心里切换帐号的回调
                        @Override
                        public void onSwitchUserAccountFinished(User userInfo) {
                        }
                    });
                }
            });
        } catch (JSONException e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
            e.printStackTrace();
        }

    }


    public void realLogin(final EmaSDKListener listener, String userid, String deviceId){
        OperateCenter.getInstance().login(mActivity, new OperateCenter.OnLoginFinishedListener() {
            @Override
            public void onLoginFinished(boolean success, int resultCode, User userInfo)
            {
                String resultMessage = OperateCenter.getResultMsg(resultCode);
                Log.e("4399 loginresultMessage",resultCode+".."+resultMessage);
                //登录结束后的游戏逻辑
                if(16==resultCode){
                    // 登陆成功
                    //登录成功回调放在下面updateWeakAccount和docallback成功以后在回调

                    //获取用户的登陆后的 UID(即用户唯一标识)
                    String uid = userInfo.getUid();
                    String nikename = userInfo.getName();
                    EmaUser.getInstance().setmUid(uid);
                    EmaUser.getInstance().setNickName(nikename);

                    //User类型的用户信息中将包含State登录凭证，该信息可用于游戏服务端进行用户信息二次验证
                    String state = userInfo.getState();

                    //绑定服务
                    Intent serviceIntent = new Intent(mActivity, EmaService.class);
                    mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                    //补充弱账户信息
                    EmaSDKUser.getInstance().updateWeakAccount(listener,ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getIMEI(mActivity), EmaUser.getInstance().getAllianceUid());

                }else if(18==resultCode){
                    // 取消登录
                    listener.onCallBack(EmaCallBackConst.LOGINCANELL, "登陆取消回调");
                }
            }
        });
    }


    /**
     * 无
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {

    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {
        OperateCenter.getInstance().recharge(mActivity,emaPayInfo.getPrice(),emaPayInfo.getOrderId().substring(32),emaPayInfo.getProductName(), new OperateCenter.OnRechargeFinishedListener() {
                    @Override
                    public void onRechargeFinished(boolean success, int resultCode, String msg) {
                        Log.e("4399 Rechargeresult",resultCode+"..."+msg);
                        if(success){
                            //请求游戏服，获取充值结果

                            // 购买成功
                            listener.onCallBack(EmaCallBackConst.PAYSUCCESS, "购买成功");
                        }else{
                            //充值失败逻辑

                            // 购买失败
                            //call一次取消订单
                            EmaPay.getInstance(mActivity).cancelOrder();

                            listener.onCallBack(EmaCallBackConst.PAYFALIED, "购买失败");
                        }
                    }
                });

    }

    public void logout() {
        OperateCenter.getInstance().logout();
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
        OperateCenter.getInstance().logout();  //4399在没有logout的情况下，会一直保留登录信息直至失效，因为我们不能保证两边的token失效时间一致（）
    }

    public void onDestroy() {
        OperateCenter.getInstance().destroy();
    }

    public void onBackPressed(EmaBackPressedAction action) {
        action.doBackPressedAction();
    }
}

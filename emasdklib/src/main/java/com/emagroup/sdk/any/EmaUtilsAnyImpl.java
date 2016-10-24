package com.emagroup.sdk.any;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.anysdk.framework.IAPWrapper;
import com.anysdk.framework.PluginWrapper;
import com.anysdk.framework.UserWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKIAP;
import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKParam;
import com.anysdk.framework.java.AnySDKUser;
import com.anysdk.framework.java.ToolBarPlaceEnum;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsAnyImpl {

    private static EmaUtilsAnyImpl instance;

    private Activity mActivity;

    public static EmaUtilsAnyImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsAnyImpl(activity);
        }
        return instance;
    }

    private EmaUtilsAnyImpl(Activity activity){
        this.mActivity =activity;
    }

    public void realInit(final EmaSDKListener listener, JSONObject data){

        try {
            String channelAppKey = data.getString("channelAppKey");
            String channelAppSecret = data.getString("channelAppSecret");
            String channelAppPrivate = data.getString("channelAppPrivate");
            AnySDK.getInstance().init(mActivity, channelAppKey, channelAppSecret, channelAppPrivate, "https://platform.lemonade-game.com/ema-platform/authLogin.jsp");
            //这里之所以不回调“初始化成功”  是因为any本身就有成功回调，让它来吧；
        } catch (JSONException e) {
            listener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
            e.printStackTrace();
        }

        AnySDKUser.getInstance().setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int i, String s) {
                if (listener != null) {
                    switch(i) {
                        case UserWrapper.ACTION_RET_INIT_SUCCESS://初始化成功
                            listener.onCallBack(EmaCallBackConst.INITSUCCESS,"初始化成功");
                            Log.e("EmaAnySDK","初始化成功");

                            //初始化成功之后再检查公告更新等信息
                            EmaUtils.getInstance(mActivity).checkSDKStatus();

                            break;
                        case UserWrapper.ACTION_RET_INIT_FAIL://初始化SDK失败回调
                            listener.onCallBack(EmaCallBackConst.INITFALIED,"初始化SDK失败回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGIN_SUCCESS://登陆成功回调

                            //登录成功回调放在下面updateWeakAccount和docallback成功以后在回调

                            EmaUser.getInstance().setmUid(AnySDKUser.getInstance().getUserID());
                            EmaUser.getInstance().setNickName("");

                            //显示toolbar
                            EmaSDK.getInstance().doShowToolbar();

                            //绑定服务
                            Intent serviceIntent = new Intent(mActivity, EmaService.class);
                            mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                            //补充弱账户信息
                            EmaSDKUser.getInstance().updateWeakAccount(listener,ULocalUtils.getAppId(mActivity),ULocalUtils.getChannelId(mActivity),ULocalUtils.getChannelTag(mActivity),ULocalUtils.getIMEI(mActivity), EmaUser.getInstance().getAllianceUid());

                            break;
                        case UserWrapper.ACTION_RET_LOGIN_CANCEL://登陆取消回调
                            listener.onCallBack(EmaCallBackConst.LOGINCANELL,"登陆取消回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGIN_FAIL://登陆失败回调
                            listener.onCallBack(EmaCallBackConst.LOGINFALIED,"登陆失败回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGOUT_SUCCESS://登出成功回调
                            listener.onCallBack(EmaCallBackConst.LOGOUTSUCCESS,"登出成功回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGOUT_FAIL://登出失败回调
                            listener.onCallBack(EmaCallBackConst.LOGOUTFALIED,"登出失败回调");
                            break;
                        case UserWrapper.ACTION_RET_ACCOUNTSWITCH_SUCCESS://切换账号成功回调
                            listener.onCallBack(EmaCallBackConst.ACCOUNTSWITCHSUCCESS,"切换成功回调");
                            break;
                        case UserWrapper.ACTION_RET_ACCOUNTSWITCH_FAIL://切换账号失败回调
                            listener.onCallBack(EmaCallBackConst.ACCOUNTSWITCHFAIL,"切换失败回调");
                            break;
                        case UserWrapper.ACTION_RET_EXIT_PAGE://退出游戏回调
                            if(s == "onGameExit" || s == "onNo3rdExiterProvide") {
                                //弹出游戏退出界面
                                Log.e("tuichu","1");
                            } else {
                                Log.e("tuichu","2");
                                //执行游戏退出逻辑
                                /*System.exit(0);
                                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                                am.killBackgroundProcesses (getPackageName());*/
                                //android.os.Process.killProcess(android.os.Process.myPid());
                                mActivity.finish();
                                System.exit(0);
                            }
                            break;
                    }
                }
            }
        });
    }


    public void realLogin(EmaSDKListener listener, String userid, String deviceId){
        Map<String, String> info = new HashMap<String, String>();
        info.put("device_info", deviceId);
        info.put("uid", userid);
        AnySDKUser.getInstance().login(info);
    }


    /**
     * 因为any的支付监听是单独先设置的
     * @param listener
     */
    public void doPayPre(final EmaSDKListener listener) {
        AnySDKIAP.getInstance().setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int arg0, String arg1) {
                switch(arg0)
                {
                    case IAPWrapper.PAYRESULT_SUCCESS://支付成功回调
                        // 购买成功
                        listener.onCallBack(EmaCallBackConst.PAYSUCCESS,"购买成功");
                        break;
                    case IAPWrapper.PAYRESULT_FAIL://支付失败回调
                        // 购买失败
                        //call一次取消订单
                        EmaPay.getInstance(mActivity).cancelOrder();

                        listener.onCallBack(EmaCallBackConst.PAYFALIED,"购买失败");
                        break;
                    case IAPWrapper.PAYRESULT_CANCEL://支付取消回调
                        // 取消购买
                        //call一次取消订单
                        EmaPay.getInstance(mActivity).cancelOrder();

                        listener.onCallBack(EmaCallBackConst.PAYCANELI,"取消购买");
                        break;
                    case IAPWrapper.PAYRESULT_NETWORK_ERROR://支付超时回调
                        //统一接口里面没有
                        break;
                    case IAPWrapper.PAYRESULT_PRODUCTIONINFOR_INCOMPLETE://支付信息提供不完全回调
                        //统一接口里面没有
                        break;
                    default:
                        //购买失败
                        //call一次取消订单
                        EmaPay.getInstance(mActivity).cancelOrder();

                        listener.onCallBack(EmaCallBackConst.PAYFALIED,"购买失败");
                        break;
                }
            }
        });
    }

    public void realPay(final EmaSDKListener listener, EmaPayInfo emaPayInfo) {

        Map<String, String> anyPayInfo = new HashMap();
        anyPayInfo.put("Product_Price", emaPayInfo.getPrice()+"");
        anyPayInfo.put("Product_Id",emaPayInfo.getProductId());
        anyPayInfo.put("Product_Name",emaPayInfo.getProductName());
        anyPayInfo.put("Product_Count", emaPayInfo.getProductNum());
        anyPayInfo.put("EXT",emaPayInfo.getOrderId());

        anyPayInfo.put("Coin_Name", "coin");
        anyPayInfo.put("Server_Id", "1");
        anyPayInfo.put("Role_Id","1");
        anyPayInfo.put("Role_Name", "16");
        anyPayInfo.put("Role_Grade", "12");
        anyPayInfo.put("Server_Name", "lemonade-game.com");
        anyPayInfo.put("Role_Balance", "10");

        /*EmaSDKIAP iap = EmaSDKIAP.getInstance();
        ArrayList<String> idArrayList = iap.getPluginId();
        iap.payForProduct(idArrayList.get(0), anyPayInfo,listener);*/
        ArrayList<String> idArrayList =  AnySDKIAP.getInstance().getPluginId();
        AnySDKIAP.getInstance().payForProduct(idArrayList.get(0), anyPayInfo);
        Log.e("dopay","dopay");
    }

    public void logout() {
        if (AnySDKUser.getInstance().isFunctionSupported("logout")) {
            AnySDKUser.getInstance().callFunction("logout");
        }
    }

    public void swichAccount() {
        if (AnySDKUser.getInstance().isFunctionSupported("accountSwitch")) {
            AnySDKUser.getInstance().callFunction("accountSwitch");
        }
    }

    public void doShowToolbar() {
        AnySDKParam param = new AnySDKParam(ToolBarPlaceEnum.kToolBarTopLeft.getPlace());
        AnySDKUser.getInstance().callFunction("showToolBar", param);
    }

    public void doHideToobar() {
        if (AnySDKUser.getInstance().isFunctionSupported("hideToolBar")) {
            AnySDKUser.getInstance().callFunction("hideToolBar");
        }
    }

    public void onResume() {
        PluginWrapper.onResume();
    }

    public void onPause() {
        PluginWrapper.onPause();
    }

    public void onStop() {
        PluginWrapper.onStop();
    }

    public void onDestroy() {
        PluginWrapper.onDestroy();
        AnySDK.getInstance().release();
    }

    public void onBackPressed(EmaBackPressedAction action) {
        if (AnySDKUser.getInstance().isFunctionSupported("exit")) {
			AnySDKUser.getInstance().callFunction("exit");
		}else {
            action.doBackPressedAction();
        }
    }
}

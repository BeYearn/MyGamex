package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.anysdk.framework.IAPWrapper;
import com.anysdk.framework.UserWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKIAP;
import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/9.
 */
public class EmaUtilsImpl {

    private static EmaUtilsImpl instance;

    private Activity mActivity;

    public static EmaUtilsImpl getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtilsImpl(activity);
        }
        return instance;
    }

    private EmaUtilsImpl(Activity activity){
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
                Log.e("EMASDK",s+"+++++++++++++++++++++++++++++++ "+i);
                if (listener != null) {
                    switch(i) {
                        case UserWrapper.ACTION_RET_INIT_SUCCESS://初始化成功
                            listener.onCallBack(EmaCallBackConst.INITSUCCESS,"初始化成功");
                            break;
                        case UserWrapper.ACTION_RET_INIT_FAIL://初始化SDK失败回调
                            listener.onCallBack(EmaCallBackConst.INITFALIED,"初始化SDK失败回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGIN_SUCCESS://登陆成功回调
                            listener.onCallBack(EmaCallBackConst.LOGINSUCCESS,"登陆成功回调");


                            //显示toolbar
                            EmaSDK.getInstance().doShowToolbar();

                            //绑定服务
                            Intent serviceIntent = new Intent(mActivity, EmaService.class);
                            mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                            //补充弱账户信息
                            EmaSDKUser.updateWeakAccount(ULocalUtils.getAppId(mActivity),ULocalUtils.getChannelId(mActivity),ULocalUtils.getChannelTag(mActivity),ULocalUtils.getIMEI(mActivity),EmaUser.getInstance().getAllianceUid());

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
                        listener.onCallBack(EmaCallBackConst.PAYFALIED,"购买失败");
                        break;
                    case IAPWrapper.PAYRESULT_CANCEL://支付取消回调
                        // 取消购买
                        listener.onCallBack(EmaCallBackConst.PAYCANELI,"取消购买");
                        break;
                    case IAPWrapper.PAYRESULT_NETWORK_ERROR://支付超时回调
                        //统一接口里面没有
                        break;
                    case IAPWrapper.PAYRESULT_PRODUCTIONINFOR_INCOMPLETE://支付信息提供不完全回调
                        //统一接口里面没有
                        break;
                    default:
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
}

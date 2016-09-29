package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.anysdk.framework.UserWrapper;
import com.anysdk.framework.java.AnySDK;
import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKUser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/27.
 */
public class EmaUtils {
    private static EmaUtils instance;
    private Activity activity;

    private static final int DISMISS_NOW = 11;
    private static final int DISMISS = 10;
    private static final int ALERT_SHOW = 13;
    private EmaSDKListener mListener;

    private static final int ALERT_WEBVIEW_SHOW = 21;
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DISMISS:
                    //dismissDelay(msg.arg1);
                    break;
                case ALERT_SHOW:
                    new EmaAlertDialog(activity,null,(Map) msg.obj,msg.arg1,msg.arg2).show();
                    break;
                case DISMISS_NOW:
                    //SplashDialog.this.dismiss();
                    break;
                case ALERT_WEBVIEW_SHOW:
                    new EmaWebviewDialog(activity,null,(Map) msg.obj,msg.arg1,msg.arg2,mHandler).show();
                    break;
            }
        }
    };


    private EmaUtils(Activity activity) {
        this.activity=activity;
    }

    public static EmaUtils getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtils(activity);
        }
        return instance;
    }


    /**
     * 检查sdk是否维护状态，并能拿到appkey
     * @param listener
     */
    public void checkSDKStatus(EmaSDKListener listener) {
        this.mListener=listener;
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {

                Map<String,String> params = new HashMap<>();
                params.put("appId",ULocalUtils.getAppId(activity));
                params.put("channelId",EmaSDK.getInstance().getChannelId());
                Message message = Message.obtain();
                try {
                    String result = new HttpRequestor().doPost(Instants.SDK_STATUS_URL, params);

                    Log.e("xxxxx",result);
                    Log.e("xxxxxx",ULocalUtils.getAppId(activity)+"///"+EmaSDK.getInstance().getChannelId());

                    JSONObject json = new JSONObject(result);
                    int resultCode = json.getInt("status");

                    HashMap<String,String> contentMap = new HashMap<>();

                    switch (resultCode) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS:// 请求状态成功
                            Log.d("检查维护状态", "请求状态成功！！");

                            getChannelKeyInfo();


                            JSONObject dataObj = json.getJSONObject("data");
                            JSONObject appVersionInfo = dataObj.getJSONObject("appVersionInfo");
                            JSONObject maintainInfo = dataObj.getJSONObject("maintainInfo");

                            int necessary = appVersionInfo.getInt("necessary");
                            String updateUrl = appVersionInfo.getString("updateUrl");
                            int version = appVersionInfo.getInt("version");

                            String maintainBg = maintainInfo.getString("maintainBg");
                            String maintainContent = maintainInfo.getString("maintainContent");
                            String showStatus = maintainInfo.getString("status"); // 0-维护/1-公告

                            contentMap.put("updateUrl",updateUrl);
                            contentMap.put("maintainContent",maintainContent);
                            contentMap.put("whichUpdate","none");

                            if("1".equals(showStatus)){ //显示公告

                                message.what=ALERT_WEBVIEW_SHOW;
                                message.arg1=1;               //显示形式 1只有确定按钮
                                message.arg2=2;					//------2确定按钮按下顺利进  3有更新，有后续dialog
                                message.obj=contentMap; //内容

                                if(ULocalUtils.getVersionCode(activity)<version){ // 需要更新
                                    Log.e("gengxin",ULocalUtils.getVersionCode(activity)+"..."+version);
                                    if(1==necessary){  //necessary 1强更
                                        contentMap.put("whichUpdate","hard");
                                    }else {
                                        contentMap.put("whichUpdate","soft");
                                    }
                                }
                                mHandler.sendMessage(message);

                            }else if("0".equals(showStatus)){ //维护状态
                                message.what=ALERT_WEBVIEW_SHOW;
                                message.arg1=1;               //显示形式 1只有确定按钮
                                message.arg2=1;					//-------1确定按钮按下退出
                                message.obj=contentMap; //内容
                                mHandler.sendMessage(message);
                            }
                            break;
                        case HttpInvokerConst.SDK_RESULT_FAILED://
                            Log.e("Emautils", "请求状态失败！！");
                            ToastHelper.toast(activity,json.getString("message"));
                            mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                            break;
                        default:
                            Log.e("Emautils", json.getString("message"));
                            mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                            break;
                    }
                } catch (Exception e) {
                    Log.w("error", "sdk status error", e);
                    mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                }
            }
        });
    }


    private void getChannelKeyInfo() {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> params = new HashMap<>();
                params.put("appId",ULocalUtils.getAppId(activity));
                params.put("channelId",EmaSDK.getInstance().getChannelId());
                try {
                    String result = new HttpRequestor().doPost(Instants.GET_KEY_INFO, params);
                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.getInt("status");
                    switch (status) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS:// 请求状态成功
                            Log.d("getkeyinfo", "请求状态成功！！");
                            JSONObject data = jsonObject.getJSONObject("data");
                            String channelAppKey = data.getString("channelAppKey");
                            String channelAppSecret = data.getString("channelAppSecret");
                            String channelAppPrivate = data.getString("channelAppPrivate");

                            realInitialAny(channelAppKey,channelAppSecret,channelAppPrivate,"https://platform.lemonade-game.com/ema-platform/authLogin.jsp");
                            //这里之所以不回调“初始化成功”  是因为any本身就有成功回调，让它来吧；
                            break;
                        case HttpInvokerConst.SDK_RESULT_FAILED://
                            Log.e("getkeyinfo", "请求状态失败！！");
                            mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                            break;
                        default:
                            Log.d("getkeyinfo", jsonObject.getString("message"));
                            mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                            break;
                    }
                } catch (Exception e) {
                    mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                    e.printStackTrace();
                }

            }
        });

    }


    private void realInitialAny(String appKey,String appSecret,String privateKey,String authLoginServer){

        AnySDK.getInstance().init(activity, appKey, appSecret, privateKey, authLoginServer);

        AnySDKUser.getInstance().setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int i, String s) {
                Log.e("EMASDK",s+"+++++++++++++++++++++++++++++++ "+i);
                if (mListener != null) {
                    switch(i)
                    {
                        case UserWrapper.ACTION_RET_INIT_SUCCESS://初始化成功
                            mListener.onCallBack(EmaCallBackConst.INITSUCCESS,"初始化成功");
                            break;
                        case UserWrapper.ACTION_RET_INIT_FAIL://初始化SDK失败回调
                            mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化SDK失败回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGIN_SUCCESS://登陆成功回调
                            mListener.onCallBack(EmaCallBackConst.LOGINSUCCESS,"登陆成功回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGIN_CANCEL://登陆取消回调
                            mListener.onCallBack(EmaCallBackConst.LOGINCANELL,"登陆取消回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGIN_FAIL://登陆失败回调
                            mListener.onCallBack(EmaCallBackConst.LOGINFALIED,"登陆失败回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGOUT_SUCCESS://登出成功回调
                            mListener.onCallBack(EmaCallBackConst.LOGOUTSUCCESS,"登出成功回调");
                            break;
                        case UserWrapper.ACTION_RET_LOGOUT_FAIL://登出失败回调
                            mListener.onCallBack(EmaCallBackConst.LOGOUTFALIED,"登出失败回调");
                            break;
                    }
                    //登录成功后
                    if(EmaCallBackConst.LOGINSUCCESS==i){
                        //显示toolbar
                        EmaSDK.getInstance().doShowToolbar();

                        //绑定服务
                        Intent serviceIntent = new Intent(activity, EmaService.class);
                        activity.bindService(serviceIntent, EmaSDK.getInstance().mServiceCon, Context.BIND_AUTO_CREATE);

                        //补充弱账户信息
                        EmaSDKUser.updateWeakAccount(ULocalUtils.getAppId(activity),ULocalUtils.getChannelId(activity),ULocalUtils.getChannelTag(activity),ULocalUtils.getIMEI(activity),EmaUser.getInstance().getmUid());
                    }
                }
            }
        });
    }
}

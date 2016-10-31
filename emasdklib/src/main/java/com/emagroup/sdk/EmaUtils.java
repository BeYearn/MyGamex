package com.emagroup.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.emagroup.sdk.any.EmaUtilsAnyImpl;
import com.emagroup.sdk.m4399.EmaUtils4399Impl;
import com.emagroup.sdk.mi.EmaUtilsMiImpl;
import com.igexin.sdk.PushManager;

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
                    break;
                case ALERT_WEBVIEW_SHOW:
                    new EmaWebviewDialog(activity,null,(Map) msg.obj,msg.arg1,msg.arg2,mHandler).show();
                    break;
            }
        }
    };

    private int necessary;
    private String updateUrl;
    private int version;
    private String maintainBg;
    private String maintainContent;
    private String showStatus;

    //绑定服务
    public ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
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
     * 初始化服务器地址，在sdk初始化的时候做
     */
    public void initServerUrl(){
        String emaEnvi =ULocalUtils.EmaSdkInfo.getStringFromMetaData(activity,"EMA_WHICH_ENVI");
        if("staging".equals(emaEnvi)){
            Url.setServerUrl(Url.STAGING_SERVER_URL);
        }else if("testing".equals(emaEnvi)){
            Url.setServerUrl(Url.TESTING_SERVER_URL);
        }else{
            Url.setServerUrl(Url.PRODUCTION_SERVER_URL);
        }
    }

    /**
     * 检查sdk是否维护状态，并能拿到appkey
     */
    public void checkSDKStatus() {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {

                Map<String,String> params = new HashMap<>();
                params.put("appId",ULocalUtils.getAppId(activity));
                params.put("channelId",ULocalUtils.getChannelId(activity));

                String sign =ULocalUtils.getAppId(activity)+EmaSDK.getInstance().getChannelId()+EmaUser.getInstance().getAppkey();
                //LOG.e("rawSign",sign);
                sign = ULocalUtils.MD5(sign);
                params.put("sign", sign);

                Message message = Message.obtain();
                try {
                    String result = new HttpRequestor().doPost(Url.getSystemInfo(), params);

                    Log.e("xxxxx",result);
                    Log.e("xxxxxx",ULocalUtils.getAppId(activity)+"///"+EmaSDK.getInstance().getChannelId());

                    JSONObject json = new JSONObject(result);
                    int resultCode = json.getInt("status");

                    HashMap<String,String> contentMap = new HashMap<>();

                    switch (resultCode) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS:// 请求状态成功
                            Log.d("1.0检查维护状态", "请求状态成功！！");


                            JSONObject dataObj = json.getJSONObject("data");
                            try{
                                JSONObject appVersionInfo = dataObj.getJSONObject("appVersionInfo");
                                necessary = appVersionInfo.getInt("necessary");
                                updateUrl = appVersionInfo.getString("updateUrl");
                                version = appVersionInfo.getInt("version");
                            }catch (Exception e) {
                                Log.w("检查维护状态", "jiexi appVersionInfo error", e);
                            }

                            try {
                                JSONObject maintainInfo = dataObj.getJSONObject("maintainInfo");
                                maintainBg = maintainInfo.getString("maintainBg");
                                maintainContent = maintainInfo.getString("maintainContent");
                                showStatus = maintainInfo.getString("status");// 0-维护/1-公告
                            }catch (Exception e) {
                                Log.w("检查维护状态", "jiexi maintainInfo error", e);
                            }


                            contentMap.put("updateUrl",updateUrl);
                            contentMap.put("maintainContent",maintainContent);
                            contentMap.put("whichUpdate","none");

                            if(TextUtils.isEmpty(showStatus)){

                                if(!TextUtils.isEmpty(updateUrl)){
                                    HashMap<String, String> updateMap = new HashMap<>();
                                    updateMap.put("updateUrl",updateUrl);

                                    if(ULocalUtils.getVersionCode(activity)<version){ // 需要更新
                                        Log.e("gengxin",ULocalUtils.getVersionCode(activity)+"..."+version);
                                        if(1==necessary){  //necessary 1强更
                                            message.arg2=2;
                                        }else {
                                            message.arg2=1;
                                        }

                                        message.what=ALERT_SHOW;
                                        message.arg1=2;               //显示形式 1只有确定按钮
                                        message.obj=updateMap; 		//内容
                                        mHandler.sendMessage(message);
                                    }
                                }

                            }else if("1".equals(showStatus)){ //显示公告

                                message.what=ALERT_WEBVIEW_SHOW;
                                message.arg1=1;               //显示形式 1只有确定按钮
                                message.arg2=2;					//------2确定按钮按下顺利进  3有更新，有后续dialog
                                message.obj=contentMap; //内容

                                if(!TextUtils.isEmpty(updateUrl)){
                                    if(ULocalUtils.getVersionCode(activity)<version){ // 需要更新
                                        Log.e("gengxin",ULocalUtils.getVersionCode(activity)+"..."+version);
                                        if(1==necessary){  //necessary 1强更
                                            contentMap.put("whichUpdate","hard");
                                        }else {
                                            contentMap.put("whichUpdate","soft");
                                        }
                                    }
                                }else {
                                    contentMap.put("whichUpdate","none");
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

                            //初始化失败
                            mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                            break;
                        default:
                            //初始化失败
                            Log.e("Emautils", json.getString("message"));
                            mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                            break;
                    }
                } catch (Exception e) {
                    //初始化失败
                    mListener.onCallBack(EmaCallBackConst.INITFALIED,"初始化失败");
                    Log.w("error", "sdk status error", e);
                }
            }
        });
    }


    public void getChannelKeyInfo(EmaSDKListener listener) {
        this.mListener=listener;
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                Map<String,String> params = new HashMap<>();
                params.put("appId",ULocalUtils.getAppId(activity));
                params.put("channelId",ULocalUtils.getChannelId(activity));
                try {
                    String result = new HttpRequestor().doPost(Url.channelKeyInfo(), params);
                    Log.e("getChannelKeyInfo", Url.channelKeyInfo()+"///"+result);
                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.getInt("status");
                    switch (status) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS:// 请求状态成功
                            Log.d("getkeyinfo", "1.0请求状态成功！！");
                            JSONObject data = jsonObject.getJSONObject("data");
                            realInit(data);
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

    /**
     * 初始化个推
     * @param activity
     */
    public void initGeTui(Activity activity){
        PushManager.getInstance().initialize(activity.getApplicationContext());
    }

    private void realInit(JSONObject data) {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).realInit(mListener,data);
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).realInit(mListener,data);
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).realInit(mListener,data);
        }
    }

    /**
     * 登录
     * @param listener
     * @param userid
     * @param deviceKey
     */
    public void realLogin(EmaSDKListener listener, String userid, String deviceKey) {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).realLogin(listener,userid,deviceKey);
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).realLogin(listener,userid,deviceKey);
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).realLogin(listener,userid,deviceKey);
        }

    }

    public void logout() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).logout();
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).logout();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).logout();
        }
    }

    public void swichAccount() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).swichAccount();
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).swichAccount();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).swichAccount();
        }
    }

    public void doPayPre(EmaSDKListener listener) {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).doPayPre(listener);
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).doPayPre(listener);
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).doPayPre(listener);
        }
    }

    public void realPay(EmaSDKListener listener, EmaPayInfo emaPayInfo) {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).realPay(listener,emaPayInfo);
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).realPay(listener,emaPayInfo);
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).realPay(listener,emaPayInfo);
        }
    }

    public void doShowToolbar() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).doShowToolbar();
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).doShowToolbar();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).doShowToolbar();
        }
    }

    public void doHideToobar() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).doHideToobar();
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).doHideToobar();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).doHideToobar();
        }
    }

    public void onResume() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onResume();
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).onResume();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onResume();
        }
    }

    public void onPause() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onPause();
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).onPause();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onPause();
        }
    }

    public void onStop() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onStop();
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).onStop();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onStop();
        }
    }

    public void onDestroy() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onDestroy();
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).onDestroy();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onDestroy();
        }
    }

    public void onBackPressed(EmaBackPressedAction action) {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onBackPressed(action);
        }else if("000108".equals(ULocalUtils.getChannelId(activity))){   //4399
            EmaUtils4399Impl.getInstance(activity).onBackPressed(action);
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onBackPressed(action);
        }
    }

}

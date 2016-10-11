package com.emagroup.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.emagroup.sdk.any.EmaUtilsAnyImpl;
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
                    //SplashDialog.this.dismiss();
                    break;
                case ALERT_WEBVIEW_SHOW:
                    new EmaWebviewDialog(activity,null,(Map) msg.obj,msg.arg1,msg.arg2,mHandler).show();
                    break;
            }
        }
    };


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

                String sign =ULocalUtils.getAppId(activity)+EmaSDK.getInstance().getChannelId()+EmaUser.getInstance().getAppkey();
                //LOG.e("rawSign",sign);
                sign = ULocalUtils.MD5(sign);
                params.put("sign", sign);

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

    private void realInit(JSONObject data) {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).realInit(mListener,data);
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).realInit(mListener,data);
        }
    }

    /**
     * 初始化个推
     * @param activity
     */
    public void initGeTui(Activity activity){
        PushManager.getInstance().initialize(activity.getApplicationContext());
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
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).realLogin(listener,userid,deviceKey);
        }

    }

    public void logout() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).logout();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).logout();
        }
    }

    public void doPayPre(EmaSDKListener listener) {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).doPayPre(listener);
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).doPayPre(listener);
        }
    }

    public void realPay(EmaSDKListener listener, EmaPayInfo emaPayInfo) {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).realPay(listener,emaPayInfo);
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).realPay(listener,emaPayInfo);
        }
    }

    public void doShowToolbar() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).doShowToolbar();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).doShowToolbar();
        }
    }

    public void doHideToobar() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).doHideToobar();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).doHideToobar();
        }
    }

    public void onResume() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onResume();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onResume();
        }
    }

    public void onPause() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onPause();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onPause();
        }
    }

    public void onStop() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onStop();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onStop();
        }
    }

    public void onDestroy() {
        if("000066".equals(ULocalUtils.getChannelId(activity))){   //小米渠道
            EmaUtilsMiImpl.getInstance(activity).onDestroy();
        }else{  //否则走any渠道
            EmaUtilsAnyImpl.getInstance(activity).onDestroy();
        }
    }
}

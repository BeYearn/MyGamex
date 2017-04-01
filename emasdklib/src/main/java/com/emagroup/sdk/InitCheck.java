package com.emagroup.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.igexin.sdk.PushManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/27.
 */
public class InitCheck {
    private static InitCheck instance;
    private Activity mActivity;

    private static final int DISMISS_NOW = 11;
    private static final int DISMISS = 10;
    private static final int ALERT_SHOW = 13;
    private static final int ALERT_WEBVIEW_SHOW = 21;

    private static final int GET_CHANNRLKEY_OK = 31;


    private EmaSDKListener mListener;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS:
                    //dismissDelay(msg.arg1);
                    break;
                case ALERT_SHOW:
                    new EmaAlertDialog(mActivity, null, (Map) msg.obj, msg.arg1, msg.arg2).show();
                    break;
                case DISMISS_NOW:
                    //SplashDialog.this.dismiss();
                    break;
                case ALERT_WEBVIEW_SHOW:
                    new EmaWebviewDialog(mActivity, null, (Map) msg.obj, msg.arg1, msg.arg2, mHandler).show();
                    break;
                case GET_CHANNRLKEY_OK:
                    //发一个继续初始化的广播
                    Intent intent = new Intent(EmaConst.EMA_BC_GETCHANNEL_OK_ACTION);
                    intent.putExtra(EmaConst.EMA_BC_CHANNEL_INFO,(String)msg.obj);
                    mActivity.sendBroadcast(intent);
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


    public static InitCheck getInstance(Activity activity) {
        if (instance == null) {
            instance = new InitCheck(activity);
        }
        return instance;
    }

    private InitCheck(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * 初始化服务器地址，在sdk初始化的时候做
     */
    public void initServerUrl() {
        String emaEnvi = ULocalUtils.EmaSdkInfo.getStringFromMetaData(mActivity, "EMA_WHICH_ENVI");
        if ("staging".equals(emaEnvi)) {
            Url.setServerUrl(Url.STAGING_SERVER_URL);
        } else if ("testing".equals(emaEnvi)) {
            Url.setServerUrl(Url.TESTING_SERVER_URL);
        } else {
            Url.setServerUrl(Url.PRODUCTION_SERVER_URL);
        }
    }


    /**
     * 初始化个推
     *
     * @param activity
     */
    public void initGeTui(Activity activity) {
        PushManager.getInstance().initialize(activity.getApplicationContext());
    }


    /**
     * 检查sdk是否维护状态，并能拿到appkey
     */
    public void checkSDKStatus() {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {

              /*  Map<String,String> params = new HashMap<>();
                params.put("appId",ULocalUtils.getAppId(mActivity));
                params.put("channelId",ULocalUtils.getChannelId(mActivity));

                String sign =ULocalUtils.getAppId(mActivity)+EmaSDK.getInstance().getChannelId()+EmaUser.getInstance().getAppkey();
                //LOG.e("rawSign",sign);
                sign = ULocalUtils.MD5(sign);
                params.put("sign", sign);*/

                Map<String, String> params = new HashMap<>();
                params.put("appId", ULocalUtils.getAppId(mActivity));
                params.put("channelId", ULocalUtils.getChannelId(mActivity));

                params.put("channelTag", ULocalUtils.getChannelTag(mActivity));
                params.put("deviceId", ULocalUtils.getDeviceId(mActivity));
                String sign = ULocalUtils.getAppId(mActivity) + ULocalUtils.getChannelId(mActivity)
                        + ULocalUtils.getChannelTag(mActivity) + ULocalUtils.getDeviceId(mActivity)
                        + EmaUser.getInstance().getAppkey();

                //String sign =ConfigManager.getInstance(mActivity).getAppId()+ConfigManager.getInstance(mActivity).getChannel()+EmaUser.getInstance().getAppKey();
                //LOG.e("rawSign",sign);
                sign = ULocalUtils.MD5(sign);
                params.put("sign", sign);

                Message message = Message.obtain();
                try {
                    String result = new HttpRequestor().doPost(Url.getSystemInfo(), params);

                    Log.e("xxxxx", result);
                    Log.e("xxxxxx", ULocalUtils.getAppId(mActivity) + "///" + ULocalUtils.getChannelId(mActivity));

                    JSONObject json = new JSONObject(result);
                    int resultCode = json.getInt("status");

                    HashMap<String, String> contentMap = new HashMap<>();

                    switch (resultCode) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS:// 请求状态成功
                            Log.d("1.0检查维护状态", "请求状态成功！！");


                            JSONObject dataObj = json.getJSONObject("data");
                            try {
                                JSONObject appVersionInfo = dataObj.getJSONObject("appVersionInfo");
                                necessary = appVersionInfo.getInt("necessary");
                                updateUrl = appVersionInfo.getString("updateUrl");
                                version = appVersionInfo.getInt("version");
                            } catch (Exception e) {
                                Log.w("检查维护状态", "jiexi appVersionInfo error", e);
                            }

                            try {
                                JSONObject maintainInfo = dataObj.getJSONObject("maintainInfo");
                                maintainBg = maintainInfo.getString("maintainBg");
                                maintainContent = maintainInfo.getString("maintainContent");
                                showStatus = maintainInfo.getString("status");// 0-维护/1-公告
                            } catch (Exception e) {
                                Log.w("检查维护状态", "jiexi maintainInfo error", e);
                            }


                            contentMap.put("updateUrl", updateUrl);
                            contentMap.put("maintainContent", maintainContent);
                            contentMap.put("whichUpdate", "none");

                            if (TextUtils.isEmpty(showStatus)) {

                                if (!TextUtils.isEmpty(updateUrl)) {
                                    HashMap<String, String> updateMap = new HashMap<>();
                                    updateMap.put("updateUrl", updateUrl);

                                    if (ULocalUtils.getVersionCode(mActivity) < version) { // 需要更新
                                        Log.e("gengxin", ULocalUtils.getVersionCode(mActivity) + "..." + version);
                                        if (1 == necessary) {  //necessary 1强更
                                            message.arg2 = 2;
                                        } else {
                                            message.arg2 = 1;
                                        }

                                        message.what = ALERT_SHOW;
                                        message.arg1 = 2;               //显示形式 1只有确定按钮
                                        message.obj = updateMap;        //内容
                                        mHandler.sendMessage(message);
                                    }
                                }

                            } else if ("1".equals(showStatus)) { //显示公告

                                message.what = ALERT_WEBVIEW_SHOW;
                                message.arg1 = 1;               //显示形式 1只有确定按钮
                                message.arg2 = 2;                    //------2确定按钮按下顺利进  3有更新，有后续dialog
                                message.obj = contentMap; //内容

                                if (!TextUtils.isEmpty(updateUrl)) {
                                    if (ULocalUtils.getVersionCode(mActivity) < version) { // 需要更新
                                        Log.e("gengxin", ULocalUtils.getVersionCode(mActivity) + "..." + version);
                                        if (1 == necessary) {  //necessary 1强更
                                            contentMap.put("whichUpdate", "hard");
                                        } else {
                                            contentMap.put("whichUpdate", "soft");
                                        }
                                    }
                                } else {
                                    contentMap.put("whichUpdate", "none");
                                }
                                mHandler.sendMessage(message);

                            } else if ("0".equals(showStatus)) { //维护状态
                                message.what = ALERT_WEBVIEW_SHOW;
                                message.arg1 = 1;               //显示形式 1只有确定按钮
                                message.arg2 = 1;                    //-------1确定按钮按下退出
                                message.obj = contentMap; //内容
                                mHandler.sendMessage(message);
                            }
                            break;
                        case HttpInvokerConst.SDK_RESULT_FAILED://
                            Log.e("Emautils", "请求状态失败！！");
                            ToastHelper.toast(mActivity, json.getString("message"));

                            //初始化失败
                            mListener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                            break;
                        default:
                            //初始化失败
                            Log.e("Emautils", json.getString("message"));
                            mListener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                            break;
                    }
                } catch (Exception e) {
                    //初始化失败
                    mListener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                    Log.w("error", "sdk status error", e);
                }
            }
        });
    }


    public void getChannelKeyInfo(EmaSDKListener listener) {
        this.mListener = listener;
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("appId", ULocalUtils.getAppId(mActivity));
                params.put("channelId", ULocalUtils.getChannelId(mActivity));
                try {
                    String result = new HttpRequestor().doPost(Url.channelKeyInfo(), params);
                    Log.e("getChannelKeyInfo", Url.channelKeyInfo() + "///" + result);
                    JSONObject jsonObject = new JSONObject(result);
                    int status = jsonObject.getInt("status");
                    switch (status) {
                        case HttpInvokerConst.SDK_RESULT_SUCCESS:// 请求状态成功
                            Log.d("getkeyinfo", "1.0请求状态成功！！");
                            JSONObject data = jsonObject.getJSONObject("data");

                            Message message = Message.obtain();
                            message.what = GET_CHANNRLKEY_OK;
                            message.obj = data.toString();
                            mHandler.sendMessage(message);
                            break;
                        case HttpInvokerConst.SDK_RESULT_FAILED://
                            Log.e("getkeyinfo", "请求状态失败！！");
                            mListener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                            break;
                        default:
                            Log.d("getkeyinfo", jsonObject.getString("message"));
                            mListener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                            break;
                    }
                } catch (Exception e) {
                    mListener.onCallBack(EmaCallBackConst.INITFALIED, "初始化失败");
                    e.printStackTrace();
                }

            }
        });

    }

}

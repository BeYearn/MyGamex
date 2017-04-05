package com.emagroup.sdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.emagroup.sdk.impl.EmaUtilsImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static com.emagroup.sdk.EmaSDK.mActivity;


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
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS:
                    //dismissDelay(msg.arg1);
                    break;
                case ALERT_SHOW:
                    new EmaAlertDialog(activity, null, (Map) msg.obj, msg.arg1, msg.arg2).show();
                    break;
                case DISMISS_NOW:
                    //SplashDialog.this.dismiss();
                    break;
                case ALERT_WEBVIEW_SHOW:
                    new EmaWebviewDialog(activity, null, (Map) msg.obj, msg.arg1, msg.arg2, mHandler).show();
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


    /**
     * 广播接收，
     * 1用来进一步初始化
     * 2登录成功后的逻辑
     */
    private BroadcastReceiver getkeyOkReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case EmaConst.EMA_BC_GETCHANNEL_OK_ACTION:
                    String data = intent.getStringExtra(EmaConst.EMA_BC_CHANNEL_INFO);
                    try {
                        JSONObject object = new JSONObject(data);
                        realInit(object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case EmaConst.EMA_BC_LOGIN_OK_ACTION:

                    //绑定服务
                    Intent serviceIntent = new Intent(mActivity, EmaService.class);
                    mActivity.bindService(serviceIntent, InitCheck.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);
                    //补充弱账户信息
                    EmaSDKUser.getInstance(mActivity).updateWeakAccount(mListener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());
                    break;
            }
        }
    };


    public void initBroadcastRevicer(EmaSDKListener listener) {

        this.mListener = listener;  // 顺便用来传listener(初始化+登录)

        //注册可以进一步初始化广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(EmaConst.EMA_BC_GETCHANNEL_OK_ACTION);
        filter.addAction(EmaConst.EMA_BC_LOGIN_OK_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        mActivity.registerReceiver(getkeyOkReciver, filter);
    }


    private EmaUtils(Activity activity) {
        this.activity = activity;
    }


    public static EmaUtils getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaUtils(activity);
        }
        return instance;
    }


    private void realInit(JSONObject data) {
        EmaUtilsImpl.getInstance(activity).realInit(mListener, data);
    }

    /**
     * 登录
     *
     * @param userid
     * @param deviceKey
     */
    public void realLogin(String userid, String deviceKey) {
        EmaUtilsImpl.getInstance(activity).realLogin(mListener, userid, deviceKey);

    }

    public void logout() {
        EmaUtilsImpl.getInstance(activity).logout();
    }

    public void swichAccount() {
        EmaUtilsImpl.getInstance(activity).swichAccount();
    }

    public void doPayPre(EmaSDKListener listener) {
        EmaUtilsImpl.getInstance(activity).doPayPre(listener);
    }

    public void realPay(EmaSDKListener listener, EmaPayInfo emaPayInfo) {
        EmaUtilsImpl.getInstance(activity).realPay(listener, emaPayInfo);
    }

    public void doShowToolbar() {
        EmaUtilsImpl.getInstance(activity).doShowToolbar();
    }

    public void doHideToobar() {
        EmaUtilsImpl.getInstance(activity).doHideToobar();
    }

    public void onResume() {
        EmaUtilsImpl.getInstance(activity).onResume();
    }

    public void onPause() {
        EmaUtilsImpl.getInstance(activity).onPause();
    }

    public void onStop() {
        EmaUtilsImpl.getInstance(activity).onStop();
    }

    public void onDestroy() {
        EmaUtilsImpl.getInstance(activity).onDestroy();
    }

    public void onBackPressed(EmaBackPressedAction action) {
        EmaUtilsImpl.getInstance(activity).onBackPressed(action);
    }

    public void onNewIntent(Intent intent) {
        EmaUtilsImpl.getInstance(activity).onNewIntent(intent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        EmaUtilsImpl.getInstance(activity).onActivityResult(requestCode, resultCode, data);
    }

    public void onRestart() {
        EmaUtilsImpl.getInstance(activity).onRestart();
    }


    public void submitGameRole(Map<String, String> data) {

        String roleId_R = data.get("roleId");
        String roleName_R = data.get("roleName");
        String roleLevel_R = data.get("roleLevel");
        String zoneId_R = data.get("zoneId");
        String zoneName_R = data.get("zoneName");
        String dataType_R = data.get("dataType");
        String ext_R = data.get("ext");

        for (Map.Entry<String, String> entry : data.entrySet()) {
            Log.e("//" + entry.getKey(), entry.getValue());
        }

        ULocalUtils.spPut(mActivity, "roleId_R", roleId_R);
        ULocalUtils.spPut(mActivity, "roleName_R", roleName_R);
        ULocalUtils.spPut(mActivity, "roleLevel_R", roleLevel_R);
        ULocalUtils.spPut(mActivity, "zoneId_R", zoneId_R);
        ULocalUtils.spPut(mActivity, "zoneName_R", zoneName_R);
        ULocalUtils.spPut(mActivity, "dataType_R", dataType_R);
        ULocalUtils.spPut(mActivity, "ext_R", ext_R);

        EmaUtilsImpl.getInstance(activity).submitGameRole(data);


    }
}

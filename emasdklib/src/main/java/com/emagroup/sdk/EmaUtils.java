package com.emagroup.sdk;

import android.app.Activity;
import android.app.ProgressDialog;
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

    private EmaService mEmaService; //拿到的心跳服务实例

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISMISS:
                    //dismissDelay(msg.arg1);
                    break;
                case ALERT_SHOW:
                    new EmaAlertDialog(activity, (Map) msg.obj, msg.arg1, msg.arg2).show();
                    break;
                case DISMISS_NOW:
                    //SplashDialog.this.dismiss();
                    break;
                case ALERT_WEBVIEW_SHOW:
                    new EmaWebviewDialog(activity, (Map) msg.obj, msg.arg1, msg.arg2, mHandler).show();
                    break;
            }
        }
    };


    /**
     * 设置 初始化 和 登录 信息回调
     *
     * @param msgCode 标识码
     * @param msg     标识信息
     * @param data 用途:1 在登录成功时,拿到username,userid
     */
    public void makeUserCallBack(int msgCode, String msg, Map<String, String> data) {     //// TODO: 2017/8/10 支付的回调也可以在EmaPay中像这样统一管理
        if (mListener == null) {
            Log.e("makeUserCallBack", "未设置回调");
            return;
        }

        if (msgCode == EmaCallBackConst.INITSUCCESS) {
            //初始化成功之后再检查公告更新等信息
            InitCheck.getInstance(mActivity).checkSDKStatus();

        } else if (msgCode == EmaCallBackConst.LOGINSUCCESS_CHANNEL){  //渠道登录成功后进一步平台登录

            if(data!=null){
                String allianceUid = data.get(EmaConst.ALLIANCE_UID);
                String nickName = data.get(EmaConst.NICK_NAME);
                EmaUser.getInstance().setAllianceUid(allianceUid);
                EmaUser.getInstance().setNickName(nickName);
                //补充弱账户信息
                EmaSDKUser.getInstance(mActivity).updateWeakAccount(mListener, ULocalUtils.getAppId(mActivity), ULocalUtils.getChannelId(mActivity), ULocalUtils.getChannelTag(mActivity), ULocalUtils.getDeviceId(mActivity), EmaUser.getInstance().getAllianceUid());
            }else {
                Log.e("callback","loginsuccess data null");
            }
        }

    }

    //绑定服务
    public ServiceConnection mServiceCon = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
            mEmaService = ((EmaService.LocalBinder) ibinder).getService();
        }
    };


    private ProgressDialog progressDialog;
    /**
     * 广播接收，
     * 1用来进一步初始化
     * 2登录成功后的逻辑
     * 3全局控制菊花窗
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

                case EmaConst.EMA_BC_PROGRESS_ACTION:

                    String progressState = intent.getStringExtra(EmaConst.EMA_BC_PROGRESS_STATE);
                    Log.e("dialogBCReciver", progressState);

                    if (null == progressDialog) {
                        progressDialog = new ProgressDialog(activity);
                        progressDialog.setCanceledOnTouchOutside(false);
                    }

                    if (EmaConst.EMA_BC_PROGRESS_START.equals(progressState)) {
                        progressDialog.show();
                    } else if (EmaConst.EMA_BC_PROGRESS_CLOSE.equals(progressState)) {
                        progressDialog.dismiss();
                    }
                    break;
            }
        }
    };

    public void initBroadcastRevicer(EmaSDKListener listener) {

        this.mListener = listener;  // 顺便用来传listener(初始化+登录)

        //注册可以进一步初始化广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(EmaConst.EMA_BC_GETCHANNEL_OK_ACTION);
        filter.addAction(EmaConst.EMA_BC_PROGRESS_ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        mActivity.registerReceiver(getkeyOkReciver, filter);
    }

    public void openProgressDialog() {
        Intent intent = new Intent(EmaConst.EMA_BC_PROGRESS_ACTION);
        intent.putExtra(EmaConst.EMA_BC_PROGRESS_STATE, EmaConst.EMA_BC_PROGRESS_START);
        mActivity.sendBroadcast(intent);
    }

    public void closeProgressDialog() {
        Intent intent = new Intent(EmaConst.EMA_BC_PROGRESS_ACTION);
        intent.putExtra(EmaConst.EMA_BC_PROGRESS_STATE, EmaConst.EMA_BC_PROGRESS_CLOSE);
        mActivity.sendBroadcast(intent);
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

    public void immediateInit(EmaSDKListener listener) {
        EmaUtilsImpl.getInstance(activity).immediateInit(listener);
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

        //解绑心跳服务
        activity.unbindService(mServiceCon);

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

        //回到前台时重新走心跳间隔逻辑
        if (null != mEmaService) {
            mEmaService.reStartHeart();
        }

        EmaUtilsImpl.getInstance(activity).onRestart();
    }


    public void submitGameRole(Map<String, String> data) {

        String roleId_R = data.get(EmaConst.SUBMIT_ROLE_ID);
        String roleName_R = data.get(EmaConst.SUBMIT_ROLE_NAME);
        String roleLevel_R = data.get(EmaConst.SUBMIT_ROLE_LEVEL);
        String zoneId_R = data.get(EmaConst.SUBMIT_ZONE_ID);
        String zoneName_R = data.get(EmaConst.SUBMIT_ZONE_NAME);
        String roleCt_R = data.get(EmaConst.SUBMIT_ROLE_CT);
        String dataType_R = data.get(EmaConst.SUBMIT_DATA_TYPE);
        String ext_R = data.get(EmaConst.SUBMIT_EXT);

        for (Map.Entry<String, String> entry : data.entrySet()) {
            Log.e("//" + entry.getKey(), entry.getValue());
        }

        ULocalUtils.spPut(mActivity, EmaConst.SUBMIT_ROLE_ID, roleId_R);
        ULocalUtils.spPut(mActivity, EmaConst.SUBMIT_ROLE_NAME, roleName_R);
        ULocalUtils.spPut(mActivity, EmaConst.SUBMIT_ROLE_LEVEL, roleLevel_R);
        ULocalUtils.spPut(mActivity, EmaConst.SUBMIT_ZONE_ID, zoneId_R);
        ULocalUtils.spPut(mActivity, EmaConst.SUBMIT_ZONE_NAME, zoneName_R);
        ULocalUtils.spPut(mActivity, EmaConst.SUBMIT_ROLE_CT, roleCt_R);
        ULocalUtils.spPut(mActivity, EmaConst.SUBMIT_DATA_TYPE, dataType_R);
        ULocalUtils.spPut(mActivity, EmaConst.SUBMIT_EXT, ext_R);

        EmaUtilsImpl.getInstance(activity).submitGameRole(data);


    }
}

package com.example.sdk.emasdk;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKUser;
import com.example.sdk.emasdk.http.HttpRequestor;
import com.example.sdk.emasdk.http.ThreadUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/5.
 */
public class EmaSDKUser {
    private static EmaSDKUser instance = null;
    private static Activity mActivity = null;
    private static AnySDKUser anySDKUser;
    private EmaSDKListener listener;
    private String userid;
    private String deviceId;
    private String mSzAndroidID;

    public static EmaSDKUser getInstance() {
        if (instance == null) {
            instance = new EmaSDKUser();
        }
        anySDKUser = AnySDKUser.getInstance();
        mActivity = EmaSDK.mActivity;
        return instance;
    }


    public void setListener(EmaSDKListener listener) {
        this.listener = listener;
        anySDKUser.setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int i, String s) {
                if (EmaSDKUser.this.listener != null) {
                    EmaSDKUser.this.listener.onCallBack(i, s);
                }
                /*//登录成功后，创建弱账号
                if(i== UserWrapper.ACTION_RET_LOGIN_SUCCESS){
                    creatWeakAccount();
                }*/
            }
        });
    }

    private void creatWeakAccount() {

        //1.获取deviceID 其实是IMEI
        TelephonyManager tm = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = tm.getDeviceId();

        //2.获取Android ID  不可靠，可能为null，如果恢复出厂设置会改变，root的话可以任意改变
        mSzAndroidID = Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.ANDROID_ID);

        //请求测试(未来改为call我们的接口)
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String urlGet = "http://192.168.10.80:8080/ema-platform/member/createWeakAccount?deviceType=android&deviceKey=";
                    String strGet = new HttpRequestor().doGet(urlGet + deviceId);
                    Log.e("baidu", strGet);

                    JSONObject jsonObject = new JSONObject(strGet);
                    JSONObject data = jsonObject.getJSONObject("data");
                    userid = data.getString("userid");
                    Log.e("_____userid____", userid);
                    Log.e("imei", deviceId);
                    /*//Thread.sleep(4000); 模拟睡4s
                    Message message = ThreadUtil.handler.obtainMessage();
                    message.what = 1;
                    message.obj = strGet;
                    ThreadUtil.handler.sendMessage(message);*/

                    loginActual(userid,deviceId);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void loginActual(String userid,String deviceId) {
        Map<String, String> info = new HashMap<String, String>();
        info.put("device_info", deviceId);
        info.put("uid", userid);
        anySDKUser.login(info);
    }

    public void login() {
        creatWeakAccount();

    }
}

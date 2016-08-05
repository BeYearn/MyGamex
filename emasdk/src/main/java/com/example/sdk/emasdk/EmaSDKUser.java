package com.example.sdk.emasdk;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.anysdk.framework.java.AnySDKListener;
import com.anysdk.framework.java.AnySDKUser;
import com.example.sdk.emasdk.http.HttpRequestor;
import com.example.sdk.emasdk.http.ThreadUtil;

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
            }
        });
    }

    public String login() {
        //1.获取deviceID 其实是IMEI
        TelephonyManager tm = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        String DEVICE_ID = tm.getDeviceId();
        //2.获取Android ID  不可靠，可能为null，如果恢复出厂设置会改变，root的话可以任意改变
        String m_szAndroidID = Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.ANDROID_ID);


        Log.e("imei+androidid", DEVICE_ID + ".." + m_szAndroidID);

        Map<String, String> info = new HashMap<String, String>();
        info.put("device_info", DEVICE_ID + m_szAndroidID);
        //info.put("key2", "value2");
        anySDKUser.login(info);

        //请求测试(未来改为call我们的接口)
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String str = new HttpRequestor().doGet("https://www.baidu.com/");
                    Log.e("baidu", str);
                    //Thread.sleep(4000); 模拟睡4s
                    Message message = ThreadUtil.handler.obtainMessage();
                    message.what = 1;
                    message.obj = str;
                    ThreadUtil.handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return DEVICE_ID + m_szAndroidID;
    }
}

package com.emagroup.sdk;

import android.app.Activity;
import android.util.Log;

import com.anysdk.framework.java.AnySDKUser;

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

    public static EmaSDKUser getInstance() {
        if (instance == null) {
            instance = new EmaSDKUser();
        }
        anySDKUser = AnySDKUser.getInstance();
        mActivity = EmaSDK.mActivity;
        return instance;
    }


    /*public void setListener(EmaSDKListener listener) {
        this.listener = listener;
        anySDKUser.setListener(new AnySDKListener() {
            @Override
            public void onCallBack(int i, String s) {
                if (EmaSDKUser.this.listener != null) {
                    EmaSDKUser.this.listener.onCallBack(i, s);
                }
            }
        });
    }*/

    private void creatWeakAccount() {

        deviceId = ULocalUtils.getIMEI(mActivity);

        //请求测试(未来改为call我们的接口)
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String urlGet = Instants.CREAT_WEAKCOUNT_URL;
                    String strGet = new HttpRequestor().doGet(urlGet + deviceId);

                    JSONObject jsonObject = new JSONObject(strGet);
                    JSONObject data = jsonObject.getJSONObject("data");
                    userid = data.getString("userid");
                    /*//Thread.sleep(4000); 模拟睡4s
                    Message message = ThreadUtil.handler.obtainMessage();
                    message.what = 1;
                    message.obj = strGet;
                    ThreadUtil.handler.sendMessage(message);*/

                    Log.e("EmaSdkUser","弱账户创建成功");

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("error","maybe is SocketTimeoutException");
                }

            }
        });

        loginActual(userid, deviceId);
    }

    private void loginActual(String userid, String deviceId) {

        Map<String, String> info = new HashMap<String, String>();
        info.put("device_info", deviceId);
        info.put("uid", userid);
        anySDKUser.login(info);
    }

    public void login() {
        creatWeakAccount();
    }

    public String getUserID() {
        return anySDKUser.getUserID();
    }
}

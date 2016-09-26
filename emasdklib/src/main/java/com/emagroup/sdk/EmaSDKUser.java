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
    private String deviceKey;
    private String EmaAppKey;
    private String allienceId;
    private String channelTag;

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

        deviceKey = ULocalUtils.getIMEI(mActivity);
        EmaAppKey= ULocalUtils.getAppKey(mActivity);
        allienceId=ULocalUtils.getChannelId();
        channelTag=ULocalUtils.getChannelTag(mActivity);
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String urlGet = Instants.CREAT_WEAKCOUNT_URL;

                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("deviceType","android");
                    paramMap.put("appKey",EmaAppKey);
                    paramMap.put("allianceId",allienceId);
                    paramMap.put("channelTag",channelTag);
                    paramMap.put("deviceKey",deviceKey);

                    Log.e("创建弱账户","deviceKey:"+ deviceKey+".."+EmaAppKey+"..."+allienceId);

                    String strGet = new HttpRequestor().doPost(urlGet,paramMap);

                    JSONObject jsonObject = new JSONObject(strGet);
                    JSONObject data = jsonObject.getJSONObject("data");
                    userid = data.getString("userid");

                    Log.e("User creatweakAccount","弱账户创建成功:"+userid);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("creatweakAccount","maybe is SocketTimeoutException");
                }

            }
        });

        loginActual(userid, deviceKey);
    }

    /**
     * 在登录成功之后再call一次，将渠道uid传过去
     */
    public static void updateWeakAccount(final String appKey, final String allianceId, final String channelTag,final String deviceKey,final String allianceUId){
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String url = Instants.UPDATE_WEAKCOUT_URL;

                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("deviceType","android");
                    paramMap.put("appKey",appKey);
                    paramMap.put("allianceId",allianceId);
                    paramMap.put("channelTag",channelTag);
                    paramMap.put("deviceKey",deviceKey);
                    paramMap.put("allianceUId",allianceUId);

                    Log.e("update弱账户","....:"+appKey+"..."+allianceId+"..."+deviceKey+"..."+allianceUId);

                    String restult = new HttpRequestor().doPost(url,paramMap);

                    JSONObject jsonObject = new JSONObject(restult);
                    String token = jsonObject.getString("data");
                    EmaUser.getInstance().setToken(token);
                    EmaUser.getInstance().setIsLogin(true);
                    Log.e("update弱账户创建:","结果:"+token);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("update弱账户创建","maybe is SocketTimeoutException");
                }

            }
        });
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

}

package com.emagroup.sdk;

import android.app.Activity;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/5.
 */
public class EmaSDKUser {
    private static EmaSDKUser instance = null;
    private static Activity mActivity = null;
    private String deviceKey;
    private String allienceId;
    private String allienceTag;
    private String appId;

    public static EmaSDKUser getInstance() {
        if (instance == null) {
            instance = new EmaSDKUser();
        }
        mActivity = EmaSDK.mActivity;
        return instance;
    }

    public void creatWeakAccount(EmaSDKListener listener) {

        /*deviceKey = ULocalUtils.getIMEI(mActivity);
        appId= ULocalUtils.getAppId(mActivity);
        allienceId= ULocalUtils.getChannelId(mActivity);
        allienceTag= ULocalUtils.getChannelTag(mActivity);
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("deviceType","android");
                    paramMap.put("appKey",appId);
                    paramMap.put("allianceId",allienceId);
                    paramMap.put("channelTag",allienceTag);
                    paramMap.put("deviceKey",deviceKey);

                    Log.e("创建弱账户","deviceKey:"+ deviceKey+".."+appId+"..."+allienceId);

                    String strGet = new HttpRequestor().doPost(Instants.CREAT_WEAKCOUNT_URL,paramMap);

                    JSONObject jsonObject = new JSONObject(strGet);
                    JSONObject data = jsonObject.getJSONObject("data");
                    String userid = data.getString("userid");
                    //EmaUser.getInstance().setmUid(userid);

                    Log.e("User creatweakAccount","弱账户创建成功:"+userid);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("creatweakAccount","maybe is SocketTimeoutException");
                }

            }
        });*/

        EmaUtils.getInstance(mActivity).realLogin(listener,"", "");
    }

    /**
     * 在登录成功之后再call一次，将渠道uid传过去
     */
    public void updateWeakAccount(final EmaSDKListener listener, final String appId, final String allianceId, final String channelTag, final String deviceKey, final String allianceUid){
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    String url = Instants.UPDATE_WEAKCOUT_URL;

                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("deviceType","android");
                    paramMap.put("appKey",appId);
                    paramMap.put("allianceId",allianceId);
                    paramMap.put("channelTag",channelTag);
                    paramMap.put("deviceKey",deviceKey);
                    paramMap.put("allianceUid",allianceUid);

                    String sign=allianceId+allianceUid+appId+channelTag+deviceKey+"android"+ EmaUser.getInstance().getAppkey();
                    sign = ULocalUtils.MD5(sign);
                    paramMap.put("sign",sign);

                    Log.e("update弱账户","....:"+appId+"..."+allianceId+"..."+deviceKey+"..."+allianceUid);

                    String result = new HttpRequestor().doPost(url,paramMap);

                    /*JSONObject jsonObject = new JSONObject(result);
                    String token = jsonObject.getString("data");
                    EmaUser.getInstance().setToken(token);
                    EmaUser.getInstance().setIsLogin(true);*/

                    JSONObject json = new JSONObject(result);
                    String dataStr=json.getString("data");

                    int resultCode = json.getInt("status");
                    JSONObject data = json.getJSONObject("data");

                    String aUid = data.getString("allianceUid");
                    EmaUser.getInstance().setmUid(aUid);
                    String nickname = data.getString("nickname");
                    EmaUser.getInstance().setNickName(nickname);
                    String authCode = data.getString("authCode");
                    String callbackUrl = data.getString("callbackUrl");

                    Log.e("update弱账户","结果:"+aUid+".."+nickname+".."+authCode+".."+callbackUrl);

                    doCallbackUrl(listener,dataStr,callbackUrl);

                    Log.e("doCallbackUrl",dataStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("update弱账户创建","maybe is SocketTimeoutException");
                }

            }
        });
    }

    private void doCallbackUrl(final EmaSDKListener listener, final String dataStr, final String callbackUrl) {

        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //耗时操作 阻塞
                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("data",dataStr);

                    String result = new HttpRequestor().doPost(callbackUrl,paramMap);
                    JSONObject jsonObject = new JSONObject(result);
                    int resultCode = jsonObject.getInt("status");
                    JSONObject data = jsonObject.getJSONObject("data");

                    String token = data.getString("token");
                    Log.e("doCallbackUrl:token", token);
                    EmaUser.getInstance().setToken(token);
                    EmaUser.getInstance().setIsLogin(true);
                    listener.onCallBack(EmaCallBackConst.LOGINSUCCESS,"登陆成功回调");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("doCallbackUrl","maybe is SocketTimeoutException");
                }

            }
        });
    }

    public void logout() {
        EmaUtils.getInstance(mActivity).logout();
        EmaUser.getInstance().setIsLogin(false);
    }
}

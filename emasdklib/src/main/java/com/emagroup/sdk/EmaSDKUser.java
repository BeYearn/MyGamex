package com.emagroup.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/5.
 */
public class EmaSDKUser {
    private static EmaSDKUser instance = null;
    private Activity mActivity = null;
    private String deviceKey;
    private String allienceId;
    private String allienceTag;
    private String appId;

    public static EmaSDKUser getInstance(Activity activity) {
        if (instance == null) {
            instance = new EmaSDKUser(activity);
        }
        return instance;
    }

    private EmaSDKUser (Activity activity){
        this.mActivity=activity;
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
                    String url = Url.updateAlianceAccount();

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

                    String uid = data.getString("uid");
                    EmaUser.getInstance().setmUid(uid);

                    String aUid = data.getString("allianceUid");
                    EmaUser.getInstance().setAllianceUid(aUid);

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

                    //绑定服务
                    Intent serviceIntent = new Intent(mActivity, EmaService.class);
                    mActivity.bindService(serviceIntent, EmaUtils.getInstance(mActivity).mServiceCon, Context.BIND_AUTO_CREATE);

                    EmaUtils.getInstance(mActivity).closeProgressDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("doCallbackUrl","maybe is SocketTimeoutException");

                    EmaUtils.getInstance(mActivity).closeProgressDialog();
                }

            }
        });
    }
}
